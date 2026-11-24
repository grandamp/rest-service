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
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateRevokedException;
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
import org.keysupport.api.singletons.PkixMetricsSingleton;
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

	/*
	 * Although certpath-provider and certpath-algorithm are configurable (defaulting
	 * to "SUN" and "PKIX"), using anything other than the "SUN" provider may lead
	 * to inconsistent results because the service currently relies on SUN-specific
	 * properties (such as com.sun.security.crl.maxSize, enableCRLDP, and
	 * enableAIAcaIssuers).
	 *
	 * TODO: Consider refactoring to make this implementation friendly for use with
	 * another provider, such as BouncyCastle (BC/BCFIPS).
	 */

	/*
	 * This implementation will rely on BCFIPS for cryptographic compliance.
	 */
	private final static String JCE_PROVIDER = "BCFIPS";

	static {
		if (null == Security.getProvider(JCE_PROVIDER)) {
			Security.addProvider(new BouncyCastleFipsProvider());
		}
	}

	private ValidatePKIX() {
	}

	public static VssResponse validate(X509Certificate cert, String x5tS256, ValidationPolicy valPol, Date now) {
		ValidationPoliciesSingleton policies = ValidationPoliciesSingleton.getInstance();
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
			if (null != now) {
				cert.checkValidity(now);
			} else {
				cert.checkValidity();
			}
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
		PkixMetricsSingleton pkixMetrics = PkixMetricsSingleton.getInstance();
		final long discoveryStartNanos = System.nanoTime();
		X509CertSelector selector = new X509CertSelector();
		selector.setCertificate(cert);
		/*
		 * Initialize the TrustAnchor via the ValidationPolicy.
		 */
		Set<TrustAnchor> taList = policies.getTrustAnchors(valPol.validationPolicyId);
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
			cpb = CertPathBuilder.getInstance(policies.getCertPathAlgorithm(), policies.getCertPathProvider());
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
		} else {
			params.setRevocationEnabled(false);
		}
		PKIXCertPathBuilderResult result = null;
		try {
			result = (PKIXCertPathBuilderResult) cpb.build(params);
		} catch (InvalidAlgorithmParameterException e) {
			LOG.error(LoggingUtil.pojoToJson(
					Map.of("error", "Error with CertPathBuilder", "stacktrace", LoggingUtil.stackTraceToString(e))));
			throw new ServiceException("Internal Validation Error");
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
			 * CertPathBuilderException.getCause(), or when path building failed for a
			 * cryptographically self-signed certificate (which can never chain to an
			 * external issuer or intermediate CA).
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
				boolean selfSigned = X509Util.isSelfSigned(cert, JCE_PROVIDER);
				fail.isAffirmativelyInvalid = selfSigned;
				fail.invalidityReasonText = e.getLocalizedMessage();
				if (!selfSigned) {
					pkixMetrics.recordIndeterminateFailure(classifyIndeterminateFailure(cert, e));
				}
			}
			pkixMetrics.recordDiscovery(System.nanoTime() - discoveryStartNanos, false);
			return response;
		}
		pkixMetrics.recordDiscovery(System.nanoTime() - discoveryStartNanos, true);
		final long validationStartNanos = System.nanoTime();
		CertPath cp = result.getCertPath();
		/*
		 * Validate the certificate path, return result if invalid
		 */
		CertPathValidator cpv = null;
		try {
			cpv = CertPathValidator.getInstance(policies.getCertPathAlgorithm());
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
			pkixMetrics.recordValidation(System.nanoTime() - validationStartNanos, false);
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
		 * If AIA chase is enabled and a CA intermediate in the built path is not yet
		 * in our intermediate cache, admit it into IntermediateCacheSingleton.
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
					if (null != cachedPathCerts && cachedPathCerts.isEmpty()) {
						if (intermediateCacheSingleton.addIntermediate(intermediate)) {
							LOG.info(LoggingUtil.pojoToJson(Map.of(
									"info", "Added discovered intermediate to cache",
									"x5t#S256", X509Util.x5tS256(intermediate),
									"x509SubjectName", intermediate.getSubjectX500Principal().toString())));
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
				if (LOG.isDebugEnabled()) {
					LOG.debug(LoggingUtil.pojoToJson(Map.of("pkix.path.cert", currentCert.toString())));
				}
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
		pkixMetrics.recordValidation(System.nanoTime() - validationStartNanos, true);
		return response;
	}

	private static String classifyIndeterminateFailure(X509Certificate cert, CertPathBuilderException e) {
		String msg = e.getMessage();
		if (null != msg && msg.toLowerCase(Locale.ROOT).contains("policy")) {
			return PkixMetricsSingleton.CATEGORY_POLICY_CONSTRAINTS;
		}
		if (null != cert.getSubjectX500Principal()
				&& cert.getSubjectX500Principal().equals(cert.getIssuerX500Principal())) {
			return PkixMetricsSingleton.CATEGORY_SELF_ISSUED_ROLLOVER;
		}
		return cert.getBasicConstraints() != -1
				? PkixMetricsSingleton.CATEGORY_NO_TRUST_PATH_CA
				: PkixMetricsSingleton.CATEGORY_NO_TRUST_PATH_EE;
	}

}