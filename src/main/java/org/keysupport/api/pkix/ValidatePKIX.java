package org.keysupport.api.pkix;

import java.io.ByteArrayInputStream;
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
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.keysupport.api.RestServiceEventLogger;
import org.keysupport.api.controller.ServiceException;
import org.keysupport.api.pkix.cache.singletons.IntermediateCacheSingleton;
import org.keysupport.api.pojo.vss.Fail;
import org.keysupport.api.pojo.vss.JsonX509Certificate;
import org.keysupport.api.pojo.vss.Success;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.keysupport.api.pojo.vss.VssResponse;
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

	/*
	 * TODO:  For now, we will stick with the SUN provider since it can fetch CRL and OCSP data.
	 *
	 * https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/sun/security/provider/certpath/URICertStore.java
	 *
	 * If we were to create an internal variant of the SUN URICertStore, then we could use it with the BC Certpath provider.
	 */
	private final static String CERTPATH_PROVIDER = "SUN";

	private final static String CERTPATH_ALGORITHM = "PKIX";

	/*
	 * TODO: For now, use the BC signature provider until BCFIPS is avail for OpenJDK/Corretto 17
	 */
	private final static String JCE_PROVIDER = "BC";
	
	public static VssResponse validate(X509Certificate cert, String x5tS256, ValidationPolicy valPol, Date now) {
		/*
		 * Begin Set JCE Signature Provider and System/Security variables
		 *
		 * <pre>
		 * Set System and Security properties to make the Sun provider: - Fetch CRLs via
		 * the CDP extension - Check OCSP via the AIA extension - Chase CA Issuers via
		 * the AIA extension
		 *
		 * TODO: Consider writing our own provider that leverages cached objects.
		 *
		 * The AIA and CDP chases would be valuable to update a local cache.
		 *
		 * The OCSP responses would be valuable *if* the CA is not able to produce a CRL
		 * within 24 hours within the FPKI (or any issuing CA or intermediate the
		 * relying party is willing to trust).
		 *
		 * See:
		 * https://docs.oracle.com/en/java/javase/11/security/java-pki-programmers-guide.html
		 *
		 * -
		 * https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/sun/security/provider/certpath/RevocationChecker.java
		 *
		 * Debug logging for CertPath can be enabled running the code via:
		 *
		 * - java -Djava.security.debug=certpath -jar target/rest-service-eb.jar
		 *
		 * <pre>
		 */
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		System.setProperty("com.sun.security.enableCRLDP", "true");
		Security.setProperty("ocsp.enable", "true");
		/*
		 * Draft Local OCSP Service providing responses for all FPKI Intermediates.
		 * 
		 * See: https://docs.oracle.com/en/java/javase/17/security/java-pki-programmers-guide.html#GUID-E6E737DB-4000-4005-969E-BCD0238B1566
		 * 
		 * If enabled, we should not rely on the following property:
		 * 
		 * System.setProperty("com.sun.security.enableCRLDP", "true");
		 */
		//Security.setProperty("ocsp.responderURL", "http://{host}/");
		//Security.setProperty("ocsp.responderCertIssuerName", "{issuer}");
		//Security.setProperty("ocsp.responderCertSerialNumber", "{signing-cert-serial}");
		
		/*
		 * Disable AIA fetch to restrict our intermediate store
		 */
		System.setProperty("com.sun.security.enableAIAcaIssuers", "false");
		/*
		 * End Set JCE Signature Provider and System/Security variables
		 */
		/*
		 * Process the request
		 */
		VssResponse response = new VssResponse();
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
		 * Get subjectAltName values, swallow the exception as far as the consumer is
		 * concerned, but log it.
		 */
		try {
			response.x509SubjectAltName = X509Util.getSubjectAlternativeNames(cert);
		} catch (IOException e) {
			LOG.error("Error parsing Certificate SAN.", e);
		}
		/*
		 * Add x5t#S256
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
		 * TODO: Before we dive into RFC5280, we should perform a minimum algorithm/keySize
		 * check.
		 *
		 * Set RFC 5280 Inputs based on the selected certificate and validation policy
		 */
		X509CertSelector selector = new X509CertSelector();
		selector.setCertificate(cert);
		/*
		 * Initialize the TrustAnchor via the ValidationPolicy.
		 *
		 * TODO: Address policies that indicate multiple trust anchors, for now; we will
		 * only inject the first trust anchor defined in the validation policy.
		 */
		String pemCert = valPol.trustAnchors.get(0).x509Certificate;
		X509Certificate ta = null;
		try {
			byte[] certBytes = null;
			CertificateFactory cf = null;
			ByteArrayInputStream bais = null;
			try {
				certBytes = Base64.getDecoder().decode(pemCert);
			} catch (Throwable e) {
				LOG.error("Internal Validation Error", e);
				throw new ServiceException("Internal Validation Error");
			}
			if (null != certBytes) {
				cf = CertificateFactory.getInstance("X509");
				bais = new ByteArrayInputStream(certBytes);
				ta = (X509Certificate) cf.generateCertificate(bais);
			} else {
				LOG.error("Internal Validation Error, null certBytes");
				throw new ServiceException("Internal Validation Error");
			}
		} catch (CertificateException e) {
			LOG.error("Internal Validation Error", e);
			throw new ServiceException("Internal Validation Error");
		}
		LOG.debug("Trust Anchor:\n" + ta.toString());
		TrustAnchor anchor = new TrustAnchor(ta, null);
		List<Certificate> cert_list = new ArrayList<>();
		cert_list.add(ta);
		cert_list.add(cert);
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
			params = new PKIXBuilderParameters(Collections.singleton(anchor), selector);
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
		/*
		 * <pre>
		 * Add FPKI Intermediate Store from our IntermediateCacheSingleton
		 *
		 * - https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/cert/CertStore.html
		 * </pre>
		 */
		IntermediateCacheSingleton intermediateCacheSingleton = IntermediateCacheSingleton.getInstance();
		params.addCertStore(intermediateCacheSingleton.getIntermediates());
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
		LOG.debug(pvr.getPolicyTree().toString());
		/*
		 * If we got this far, the certificate is valid.
		 *
		 * Populate the ValidationSuccessData, add it to the result, and; return the
		 * result.
		 */
		Success success = new Success();
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
		response.validationResult = success;
		return response;
	}
}
