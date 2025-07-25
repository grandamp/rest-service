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
import java.security.cert.CertStoreException;
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
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.keysupport.api.LoggingUtil;
import org.keysupport.api.RestServiceEventLogger;
import org.keysupport.api.controller.ServiceException;
import org.keysupport.api.pojo.vss.Fail;
import org.keysupport.api.pojo.vss.JsonX509Certificate;
import org.keysupport.api.pojo.vss.PKIXPolicyNode;
import org.keysupport.api.pojo.vss.Success;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.keysupport.api.pojo.vss.VssResponse;
import org.keysupport.api.singletons.IntermediateCacheSingleton;
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

	/**
	 * <pre>
	 *
	 * For now, we will stick with the SUN PKIX provider since it can fetch CRL and
	 * OCSP data.
	 *
	 * Changing CERTPATH_PROVIDER & CERTPATH_ALGORITHM will likely require the re-factoring of this class.
	 *
	 * https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/sun/security/provider/certpath/URICertStore.java
	 *
	 * If we were to create an internal variant of the SUN URICertStore, then we
	 * could use it with the BC Certpath provider.
	 *
	 * Debug logging for CertPath can be enabled running the code via:
	 *
	 * - java -Djava.security.debug=certpath -jar target/rest-service-eb.jar
	 *
	 * The following Documentation may be helpful to unravel configuration options:
	 *
	 * - https://docs.oracle.com/en/java/javase/21/security/java-pki-programmers-guide.html
	 * - https://docs.oracle.com/en/java/javase/21/security/troubleshooting-security.html
	 * - https://openjdk.org/jeps/124
	 * - https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/security/cert/PKIXRevocationChecker.html
	 * - https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/security/cert/PKIXRevocationChecker.Option.html
	 *
	 * </pre>
	 */
	private final static String CERTPATH_PROVIDER = "SUN";

	private final static String CERTPATH_ALGORITHM = "PKIX";

	/*
	 * This implementation will rely on BCFIPS for cryptographic compliance.
	 */
	private final static String JCE_PROVIDER = "BCFIPS";

	public static VssResponse validate(X509Certificate cert, String x5tS256, ValidationPolicy valPol, Date now) {
		ValidationPoliciesSingleton policies = ValidationPoliciesSingleton.getInstance();
		/*
		 * Allow CRL
		 */
		if (policies.getRevocationEnabled() && policies.getCrlEnabled()) {
			System.setProperty("com.sun.security.enableCRLDP", "true");
		}
		/*
		 * Allow OCSP
		 */
		if (policies.getRevocationEnabled() && policies.getOcspEnabled()) {
			Security.setProperty("ocsp.enable", "true");
		}
		/*
		 * Allow AIA chase
		 */
		if (policies.getAiaChase()) {
			System.setProperty("com.sun.security.enableAIAcaIssuers", "true");
		}
		/*
		 * This implementation will rely on BCFIPS for cryptographic compliance.
		 */
		Security.addProvider(new BouncyCastleFipsProvider());
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
			LOG.error(LoggingUtil.pojoToJson(
					Map.of("error", "Error parsing Certificate SAN", "stacktrace", LoggingUtil.stackTraceToString(e))));
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
		X509CertSelector selector = new X509CertSelector();
		selector.setCertificate(cert);
		/*
		 * Initialize the TrustAnchor via the ValidationPolicy.
		 */
		List<Certificate> cert_list = new ArrayList<>();
		HashSet<TrustAnchor> taList = policies.getTrustAnchors(valPol.validationPolicyId);
		CertStoreParameters cparam = new CollectionCertStoreParameters(cert_list);
		CertStore cstore = null;
		try {
			cstore = CertStore.getInstance("Collection", cparam, CERTPATH_PROVIDER);
		} catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
			LOG.error(LoggingUtil.pojoToJson(
					Map.of("error", "Internal Validation Error", "stacktrace", LoggingUtil.stackTraceToString(e))));
			throw new ServiceException("Internal Validation Error");
		}
		PKIXBuilderParameters params = null;
		try {
			params = new PKIXBuilderParameters(taList, selector);
		} catch (InvalidAlgorithmParameterException e) {
			LOG.error(LoggingUtil.pojoToJson(
					Map.of("error", "Internal Validation Error", "stacktrace", LoggingUtil.stackTraceToString(e))));
			throw new ServiceException("Internal Validation Error");
		}
		params.setSigProvider(JCE_PROVIDER);
		params.setDate(now);
		params.setInitialPolicies(new HashSet<>(valPol.userPolicySet));
		params.setPolicyMappingInhibited(valPol.inhibitPolicyMapping);
		params.setExplicitPolicyRequired(valPol.requireExplicitPolicy);
		params.setAnyPolicyInhibited(valPol.inhibitAnyPolicy);
		params.setMaxPathLength(policies.getMaxPathLen());
		params.addCertStore(cstore);
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
		CertStore intermediateStore = intermediateCacheSingleton.getIntermediates();
		params.addCertStore(intermediateStore);
		/*
		 * Build the certificate path
		 */
		CertPathBuilder cpb = null;
		try {
			cpb = CertPathBuilder.getInstance(CERTPATH_ALGORITHM, CERTPATH_PROVIDER);
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			LOG.error(LoggingUtil.pojoToJson(
					Map.of("error", "Internal Validation Error", "stacktrace", LoggingUtil.stackTraceToString(e))));
			throw new ServiceException("Internal Validation Error");
		}
		/*
		 * We will add revocation options during path construction here
		 */
		if (policies.getRevocationEnabled()) {
			PKIXRevocationChecker rc = (PKIXRevocationChecker) cpb.getRevocationChecker();
			if (policies.getRevocationEeOnly()) {
				rc.setOptions(EnumSet.of(PKIXRevocationChecker.Option.ONLY_END_ENTITY));
			}
			params.addCertPathChecker(rc);
		}
		PKIXCertPathBuilderResult result = null;
		try {
			result = (PKIXCertPathBuilderResult) cpb.build(params);
		} catch (InvalidAlgorithmParameterException e) {
			LOG.error(LoggingUtil.pojoToJson(
					Map.of("error", "Error with CertPathBuilder", "stacktrace", LoggingUtil.stackTraceToString(e))));
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
					fail.invalidityReasonText = e.getLocalizedMessage();
				}
			} else {
				fail.isAffirmativelyInvalid = false;
				fail.invalidityReasonText = e.getLocalizedMessage();
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
			LOG.error(LoggingUtil.pojoToJson(
					Map.of("error", "Internal Validation Error", "stacktrace", LoggingUtil.stackTraceToString(e))));
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
			LOG.error(LoggingUtil.pojoToJson(
					Map.of("error", "Internal Validation Error", "stacktrace", LoggingUtil.stackTraceToString(e))));
			throw new ServiceException("Internal Validation Error");
		}
		@SuppressWarnings("unchecked")
		List<X509Certificate> validPath = (List<X509Certificate>) cp.getCertificates();
		/*
		 * Intermediate Certificate Discovery
		 *
		 * The intent of this logic is to see if the intermediates in the built path
		 * exist in our intermediate cache.
		 *
		 * If the certificate does not exist, then we will print a warning message
		 * suggesting the certificate addition.
		 */
		if (policies.getAiaChase()) {
			for (X509Certificate intermediate : validPath) {
				if (intermediate.getBasicConstraints() != -1) {
					X509CertSelector pathSelect = new X509CertSelector();
					pathSelect.setCertificate(intermediate);
					Collection<? extends Certificate> cachedPathCerts = null;
					try {
						cachedPathCerts = intermediateStore.getCertificates(pathSelect);
					} catch (CertStoreException e) {
						LOG.error(LoggingUtil
								.pojoToJson(Map.of("error", "Internal Intermediate Certificate Discovery Error",
										"stacktrace", LoggingUtil.stackTraceToString(e))));
					}
					if (cachedPathCerts.isEmpty()) {
						try {
							LOG.warn(LoggingUtil.pojoToJson(Map.of("warning",
									"Discovered Intermediate! (not present in cache, consider adding)",
									"x509Certificate", Base64.getEncoder().encodeToString(intermediate.getEncoded()))));
						} catch (CertificateEncodingException e) {
							LOG.error(LoggingUtil
									.pojoToJson(Map.of("error", "Error Base64 encoding certificate from CertPath",
											"stacktrace", LoggingUtil.stackTraceToString(e))));
						}
					}
				}
			}
		}
		/*
		 * If we got this far, the certificate is valid. (Regardless of default
		 * revocation checking behavior)
		 *
		 * Populate the V1ValidationSuccessData, add it to the result, and; return the
		 * result.
		 */
		Success success = new Success();
		/*
		 * Add certPath
		 */
		List<JsonX509Certificate> x509CertificatePath = new ArrayList<>();
		for (Certificate currentCert : validPath) {
			JsonX509Certificate bCert = new JsonX509Certificate();
			try {
				bCert.x509Certificate = Base64.getEncoder().encodeToString(currentCert.getEncoded());
				LOG.debug(LoggingUtil.pojoToJson(Map.of("pkix.path.cert", currentCert.toString())));
			} catch (CertificateEncodingException e) {
				LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Error Base64 encoding certificate from CertPath",
						"stacktrace", LoggingUtil.stackTraceToString(e))));
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
