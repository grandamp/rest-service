package org.keysupport.api.pkix;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateRevokedException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.keysupport.api.RestServiceEventLogger;
import org.keysupport.api.controller.ServiceException;
import org.keysupport.api.pkix.cache.singletons.IntermediateCacheSingleton;
import org.keysupport.api.pojo.vss.Fail;
import org.keysupport.api.pojo.vss.JsonX509Certificate;
import org.keysupport.api.pojo.vss.PKIXPolicyNode;
import org.keysupport.api.pojo.vss.Success;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.keysupport.api.pojo.vss.VssResponse;
import org.keysupport.api.singletons.ValidationPoliciesSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 *   Original Source:  https://github.com/grandamp/KSJavaAPI/blob/master/src/org/keysupport/keystore/CertValidator.java
 * </pre>
 */

public class ValidatePKIX {

	private final static Logger LOG = LoggerFactory.getLogger(ValidatePKIX.class);

	private final static int MAX_PATH_LENGTH = 7;

	/**
	 * <pre>
	 * 
	 * For now, we will stick with the SUN provider since it can fetch CRL and
	 * OCSP data.
	 *
	 * https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/sun/security/provider/certpath/URICertStore.java
	 *
	 * If we were to create an internal variant of the SUN URICertStore, then we
	 * could use it with the BC Certpath provider.
	 * 
	 * </pre>
	 */
	private final static String CERTPATH_PROVIDER = "SUN";

	private final static String CERTPATH_ALGORITHM = "PKIX";

	/**
	 * <pre>
	 * TODO: For now, use the BC signature provider until BCFIPS is avail for
	 * OpenJDK/Corretto 17
	 * 
	 * - https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2022-45146
	 * </pre>
	 */
	private final static String JCE_PROVIDER = "BC";

	public static VssResponse validate(X509Certificate cert, String x5tS256, ValidationPolicy valPol, Date now) {
		/**
		 * <pre>
		 *  		 
		 * Begin Set JCE Signature Provider and System/Security variables
		 *
		 * Set System and Security properties to make the Sun provider:
		 * 
		 * - Fetch CRLs via the CDP extension
		 * - Check OCSP via the AIA extension
		 * - Chase CA Issuers via the AIA extension
		 *
		 * The AIA and CDP chases would be valuable to update a local cache.
		 *
		 * The OCSP responses would be valuable *if* the CA is not able to produce a CRL
		 * within 24 hours within the FPKI (or any issuing CA or intermediate the
		 * relying party is willing to trust).
		 *
		 * See:
		 * 
		 * https://docs.oracle.com/en/java/javase/11/security/java-pki-programmers-guide.html
		 *
		 * - https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/sun/security/provider/certpath/RevocationChecker.java
		 *
		 * Debug logging for CertPath can be enabled running the code via:
		 *
		 * - java -Djava.security.debug=certpath -jar target/rest-service-eb.jar
		 *
		 * </pre>
		 */
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		/*
		 * *Temporary Testing a non-revocation testing option*
		 * 
		 */
		Boolean revocationCheckingDisabled = false;
		if (revocationCheckingDisabled) {
			Security.setProperty("ocsp.enable", "false");
		} else {
			System.setProperty("com.sun.security.enableCRLDP", "true");
			Security.setProperty("ocsp.enable", "true");
		}
		/**
		 * <pre>
		 * 
		 * Draft Local OCSP Service providing responses for all FPKI Intermediates.
		 * 
		 * See:
		 * 
		 * https://docs.oracle.com/en/java/javase/17/security/java-pki-programmers-guide.html#GUID-E6E737DB-4000-4005-969E-BCD0238B1566
		 * 
		 * If enabled, we should not rely on the following properties:
		 * 
		 * System.setProperty("com.sun.security.enableCRLDP", "true");
		 * Security.setProperty("ocsp.enable", "true");
		 * 
		 * The preference is to not beacon human credential use.  
		 * 
		 * It is much more efficient for us to consider local revocation 
		 * data caching (expecting ~2Gb/24hr for FPKI) via CRLs.
		 * 
		 * Some teams *may* want to consider an internal OCSP responder, 
		 * where the revocation source data is CRL based, and; can 
		 * discover from any certificate that is successfully validated.
		 * 
		 * Below are the properties that should be set in lieu of the 
		 * System/Security properties above to enable OCSP and CRL DP Download:
		 * 
		 * Security.setProperty("ocsp.responderURL", "http://{host}/");
		 * Security.setProperty("ocsp.responderCertIssuerName", "{issuer}");
		 * Security.setProperty("ocsp.responderCertSerialNumber", "{signing-cert-serial}");
		 * 
		 * </pre>
		 */
		/*
		 * Disable AIA fetch to restrict our intermediate store
		 * 
		 * TODO: Consider this as an option
		 */
		System.setProperty("com.sun.security.enableAIAcaIssuers", "true");
		/*
		 * End Set JCE Signature Provider and System/Security variables
		 */
		/*
		 * Process the request
		 */
		VssResponse response = new VssResponse();
		/*
		 * Add Validation Policy
		 */
		response.validationPolicyId = valPol.validationPolicyId;
		/*
		 * When decoding the certificate contents, don't always assume that the fields
		 * will be non-NULL. For example, cardAuth certs MAY have a NULL subject name.
		 */
		if (null != cert.getSubjectX500Principal()) {
			response.x509SubjectName = cert.getSubjectX500Principal().toString();
		}
		if (null != cert.getIssuerX500Principal()) {
			response.x509IssuerName = cert.getIssuerX500Principal().toString();
		}
		if (null != cert.getSerialNumber()) {
			response.x509SerialNumber = cert.getSerialNumber().toString();
		}
		/*
		 * TODO: populate subjectKeyIdentifer
		 */

		if (cert.getBasicConstraints() == -1) {
			/*
			 * This is not a CA
			 */
			response.isCA = Boolean.FALSE;
		} else {
			/**
			 * <pre>
			 * This is a CA:
			 * 
			 * - https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/security/cert/V1X509Certificate.html#getBasicConstraints()
			 * 
			 * "the value of pathLenConstraint if the BasicConstraints extension is present
			 *  in the certificate and the subject of the certificate is a CA, otherwise -1."
			 * 
			 * </pre>
			 */
			response.isCA = Boolean.TRUE;
		}
		/*
		 * Get subjectAltName values, swallow the exception as far as the consumer is
		 * concerned, but log it.
		 */
		try {
			response.x509SubjectAltName = X509Util.getSubjectAlternativeNames(cert);
		} catch (IOException e) {
			LOG.error("Error parsing Certificate SAN.", e);
		}
		/*
		 * Add x5t#S256, because we are checking temporal validity next.
		 * 
		 * We can render a *much* faster validity result in this case.
		 */
		response.x5tS256 = x5tS256;
		try {
			cert.checkValidity();
		} catch (CertificateExpiredException e) {
			RestServiceEventLogger.logEvent(response, e);
			Fail fail = new Fail();
			fail.isAffirmativelyInvalid = true;
			fail.invalidityReasonText = e.getLocalizedMessage();
			response.validationResult = fail;
			return response;
		} catch (CertificateNotYetValidException e) {
			RestServiceEventLogger.logEvent(response, e);
			Fail fail = new Fail();
			fail.isAffirmativelyInvalid = true;
			fail.invalidityReasonText = e.getLocalizedMessage();
			response.validationResult = fail;
			return response;
		}
		/*
		 * TODO: Before we dive into RFC5280, we should perform a minimum
		 * algorithm/keySize check.
		 *
		 * Set RFC 5280 Inputs based on the selected certificate and validation policy
		 * 
		 * This seems easy to do with the SUN JCE Provider, but; we are using the BC
		 * provider (targeting BCFIPS)
		 */
		X509CertSelector selector = new X509CertSelector();
		selector.setCertificate(cert);
		/*
		 * Initialize the TrustAnchor via the ValidationPolicy.
		 */
		List<Certificate> cert_list = new ArrayList<>();
		ValidationPoliciesSingleton validationPoliciesSingleton = ValidationPoliciesSingleton.getInstance();
		HashSet<TrustAnchor> taList = validationPoliciesSingleton.getTrustAnchors(valPol.validationPolicyId);
		CertStoreParameters cparam = new CollectionCertStoreParameters(cert_list);
		CertStore cstore = null;
		try {
			cstore = CertStore.getInstance("Collection", cparam, CERTPATH_PROVIDER);
		} catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
			LOG.error("Internal Validation Error", e);
			throw new ServiceException("Internal Validation Error");
		}
		PKIXBuilderParameters params = null;
		try {
			params = new PKIXBuilderParameters(taList, selector);
		} catch (InvalidAlgorithmParameterException e) {
			LOG.error("Internal Validation Error", e);
			throw new ServiceException("Internal Validation Error");
		}
		params.setSigProvider(JCE_PROVIDER);
		params.setDate(now);
		params.setInitialPolicies(new HashSet<>(valPol.userPolicySet));
		params.setPolicyMappingInhibited(valPol.inhibitPolicyMapping);
		params.setExplicitPolicyRequired(valPol.requireExplicitPolicy);
		params.setAnyPolicyInhibited(valPol.inhibitAnyPolicy);
		params.setMaxPathLength(MAX_PATH_LENGTH);
		params.addCertStore(cstore);
		if (revocationCheckingDisabled) {
			params.setRevocationEnabled(false);
		} else {
			params.setRevocationEnabled(true);
		}
		/**
		 * <pre>
		 * 
		 * Add Intermediate Store from our IntermediateCacheSingleton
		 *
		 * - https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/cert/CertStore.html
		 * 
		 * </pre>
		 */
		IntermediateCacheSingleton intermediateCacheSingleton = IntermediateCacheSingleton.getInstance();
		params.addCertStore(intermediateCacheSingleton.getIntermediates(valPol.validationPolicyId));
		/*
		 * Build the certificate path
		 */
		CertPathBuilder cpb = null;
		try {
			cpb = CertPathBuilder.getInstance(CERTPATH_ALGORITHM, CERTPATH_PROVIDER);
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			LOG.error("Internal Validation Error", e);
			throw new ServiceException("Internal Validation Error");
		}
		PKIXCertPathBuilderResult result = null;
		/*
		 * TODO: Consider isolating the following into it's own method, or class that
		 * accommodates multiple exception types.
		 */
		try {
			result = (PKIXCertPathBuilderResult) cpb.build(params);
		} catch (InvalidAlgorithmParameterException e) {
			LOG.error("Error with CertPathBuilder", e);
		} catch (CertPathBuilderException e) {
			/*
			 * Construct and return validation response.
			 *
			 * Otherwise, we need to instrument more data from e.getCause() and make a
			 * decision.
			 */
			RestServiceEventLogger.logEvent(response, e);
			Fail fail = new Fail();
			response.validationResult = fail;
			/*
			 * Resolve the real reason for the failure
			 *
			 * We may want to customize the `invalidityReasonText`
			 *
			 * To be safe, we will flag `isAffirmativelyInvalid` as `true` for any
			 * CertPathBuilderException that has an explicit
			 * CertPathBuilderException.getCause().
			 *
			 * Otherwise, we should assume the possibility that our cache is out of date.
			 */
			Throwable t = e.getCause();
			if (null != t) {
				fail.isAffirmativelyInvalid = true;
				if (t.getCause() instanceof CertificateRevokedException) {
					fail.invalidityReasonText = t.getMessage();
				} else {
					fail.invalidityReasonText = t.getMessage();
				}
			} else {
				fail.isAffirmativelyInvalid = false;
				fail.invalidityReasonText = e.getMessage();
			}
			return response;
		}
		CertPath cp = result.getCertPath();
		/*
		 * Validate the certificate path, return result if invalid
		 */
		CertPathValidator cpv = null;
		try {
			cpv = CertPathValidator.getInstance(CERTPATH_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			LOG.error("Internal Validation Error", e);
			throw new ServiceException("Internal Validation Error");
		}
		PKIXCertPathValidatorResult pvr = null;
		try {
			pvr = (PKIXCertPathValidatorResult) cpv.validate(cp, params);
		} catch (CertPathValidatorException e) {
			RestServiceEventLogger.logEvent(response, e);
			Fail fail = new Fail();
			fail.isAffirmativelyInvalid = true;
			fail.invalidityReasonText = e.getLocalizedMessage();
			response.validationResult = fail;
			return response;
		} catch (InvalidAlgorithmParameterException e) {
			LOG.error("Internal Validation Error", e);
			throw new ServiceException("Internal Validation Error");
		}
		/*
		 * If we got this far, the certificate is valid. (Regardless of default
		 * revocation checking behavior)
		 *
		 * Populate the V1ValidationSuccessData, add it to the result, and; return the
		 * result.
		 * 
		 * TODO: If we disable default revocation checking by policy, then; we should
		 * consider checking revocation using a method we define (and config via policy)
		 */
		Success success = new Success();
		/*
		 * Add certPath
		 */
		List<JsonX509Certificate> x509CertificatePath = new ArrayList<>();
		for (Certificate currentCert : cp.getCertificates()) {
			JsonX509Certificate bCert = new JsonX509Certificate();
			try {
				bCert.x509Certificate = Base64.getEncoder().encodeToString(currentCert.getEncoded());
				LOG.debug("Path Cert:\n" + currentCert.toString());
			} catch (CertificateEncodingException e) {
				LOG.error("Error Base64 encoding certificate from CertPath", e);
			}
			x509CertificatePath.add(bCert);
		}
		success.x509CertificatePath = x509CertificatePath;
		/*
		 * Add policyTree
		 */
		PKIXPolicyNode policyTree = X509Util.policyNodeToJSON(pvr.getPolicyTree());
		success.policyTree = policyTree;
		response.validationResult = success;
		return response;
	}
	
}
