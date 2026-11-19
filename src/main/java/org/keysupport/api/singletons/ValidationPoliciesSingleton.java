package org.keysupport.api.singletons;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.keysupport.api.LoggingUtil;
import org.keysupport.api.pojo.vss.JsonTrustAnchor;
import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class uses an efficient initialization-on-demand holder singleton
 * pattern to store immutable Validation Policies and Trust Anchors loaded at
 * service startup.
 */
public class ValidationPoliciesSingleton {

	private static final Logger LOG = LoggerFactory.getLogger(ValidationPoliciesSingleton.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final String DEFAULT_POLICY_OID = "1.3.6.1.5.5.7.19.1";

	private final Object initLock = new Object();
	private final AtomicBoolean policiesLoaded = new AtomicBoolean(false);
	private final AtomicBoolean configurationLocked = new AtomicBoolean(false);

	private volatile ValidationPolicies validationPolicies = null;

	/**
	 * Unmodifiable map of TrustAnchor sets keyed by validationPolicyId.
	 */
	private volatile Map<String, Set<TrustAnchor>> trustAnchorMap = Map.of();

	private volatile int maxPathLen;
	private volatile boolean aiaChase;
	private volatile String allowedAiaLocations;
	private volatile boolean revocationEnabled;
	private volatile boolean revocationEeOnly;
	private volatile boolean ocspEnabled;
	private volatile boolean crlEnabled;
	private volatile int crlMaxSize;
	private volatile String certPathProvider;
	private volatile String certPathAlgorithm;
	private volatile boolean intermediateValidateOnLoad = true;
	private volatile boolean intermediateRevocationEnabled = true;

	private ValidationPoliciesSingleton() {
	}

	private static class SingletonHelper {
		private static final ValidationPoliciesSingleton INSTANCE = new ValidationPoliciesSingleton();
	}

	public static ValidationPoliciesSingleton getInstance() {
		return SingletonHelper.INSTANCE;
	}

	/**
	 * Load and freeze validation policies once at service startup. Any subsequent
	 * call or invalid policy payload fails immediately.
	 */
	public void updateValidationPolicies(String polUri) {
		synchronized (initLock) {
			if (policiesLoaded.get()) {
				String msg = "Validation policies are final once loaded at startup";
				LOG.error(LoggingUtil.pojoToJson(Map.of("error", msg)));
				throw new IllegalStateException(msg);
			}
			if (null == polUri || polUri.isBlank()) {
				throw new IllegalStateException("Validation policy URI must not be null or blank");
			}
			URI uri = URI.create(polUri);
			HTTPClientSingleton client = HTTPClientSingleton.getInstance();
			String validationPoliciesJson = client.getText(uri);
			if (null == validationPoliciesJson || validationPoliciesJson.isBlank()) {
				String msg = "FATAL: Failed to obtain initial ValidationPolicies JSON from \"" + uri.toASCIIString()
						+ "\", shutting down!";
				LOG.error(LoggingUtil.pojoToJson(Map.of("error", msg)));
				throw new IllegalStateException(msg);
			}

			ValidationPolicies parsed;
			try {
				parsed = MAPPER.readValue(validationPoliciesJson, ValidationPolicies.class);
			} catch (JsonProcessingException e) {
				String msg = "FATAL: Error converting ValidationPolicies JSON to POJO from \"" + uri.toASCIIString()
						+ "\"";
				LOG.error(LoggingUtil.pojoToJson(
						Map.of("error", msg, "stacktrace", LoggingUtil.stackTraceToString(e))));
				throw new IllegalStateException(msg, e);
			}

			if (null == parsed || null == parsed.validationPolicies || parsed.validationPolicies.isEmpty()) {
				String msg = "FATAL: ValidationPolicies JSON contains no validationPolicies";
				LOG.error(LoggingUtil.pojoToJson(Map.of("error", msg)));
				throw new IllegalStateException(msg);
			}

			CertificateFactory cf;
			try {
				cf = CertificateFactory.getInstance("X.509");
			} catch (CertificateException e) {
				throw new IllegalStateException("Failed to initialize X.509 CertificateFactory", e);
			}

			Map<String, Set<TrustAnchor>> builtAnchors = new HashMap<>();
			List<ValidationPolicy> immutablePolicies = new ArrayList<>(parsed.validationPolicies.size());

			for (ValidationPolicy policy : parsed.validationPolicies) {
				if (null == policy || null == policy.validationPolicyId || policy.validationPolicyId.isBlank()) {
					throw new IllegalStateException("ValidationPolicy is missing validationPolicyId");
				}
				if (null == policy.trustAnchors || policy.trustAnchors.isEmpty()) {
					throw new IllegalStateException(
							"ValidationPolicy " + policy.validationPolicyId + " has no trustAnchors");
				}
				Set<TrustAnchor> taSet = new HashSet<>();
				for (JsonTrustAnchor currentTa : policy.trustAnchors) {
					if (null == currentTa || null == currentTa.x509Certificate || currentTa.x509Certificate.isBlank()) {
						throw new IllegalStateException(
								"TrustAnchor missing x509Certificate in policy " + policy.validationPolicyId);
					}
					X509Certificate ta;
					try {
						byte[] certBytes = Base64.getDecoder().decode(currentTa.x509Certificate);
						ta = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certBytes));
					} catch (IllegalArgumentException | CertificateException e) {
						LOG.error(LoggingUtil.pojoToJson(Map.of(
								"error", "Failed to decode TrustAnchor in policy " + policy.validationPolicyId,
								"stacktrace", LoggingUtil.stackTraceToString(e))));
						throw new IllegalStateException(
								"Failed to decode TrustAnchor in policy " + policy.validationPolicyId, e);
					}
					taSet.add(new TrustAnchor(ta, null));
				}

				ValidationPolicy copy = new ValidationPolicy();
				copy.validationPolicyId = policy.validationPolicyId;
				copy.validationPolicyName = policy.validationPolicyName;
				copy.validationPolicyDescription = policy.validationPolicyDescription;
				copy.trustAnchors = List.copyOf(policy.trustAnchors);
				copy.userPolicySet = null != policy.userPolicySet ? List.copyOf(policy.userPolicySet) : List.of();
				copy.inhibitPolicyMapping = policy.inhibitPolicyMapping;
				copy.requireExplicitPolicy = policy.requireExplicitPolicy;
				copy.inhibitAnyPolicy = policy.inhibitAnyPolicy;
				immutablePolicies.add(copy);

				builtAnchors.put(policy.validationPolicyId, Set.copyOf(taSet));
			}

			ValidationPolicies immutableRoot = new ValidationPolicies();
			immutableRoot.validationPolicies = List.copyOf(immutablePolicies);

			this.trustAnchorMap = Map.copyOf(builtAnchors);
			this.validationPolicies = immutableRoot;
			this.policiesLoaded.set(true);
			LOG.info(LoggingUtil.pojoToJson(this.validationPolicies));
		}
	}

	/**
	 * Lock PKIX policy configuration parameters after startup initialization so
	 * policies and their enforcement rules remain final until JVM exit.
	 */
	public void lockConfiguration() {
		configurationLocked.set(true);
	}

	private void ensureMutableConfig() {
		if (configurationLocked.get()) {
			throw new IllegalStateException("Validation policy configuration is final once initialized");
		}
	}

	public ValidationPolicies getValidationPolicies() {
		return validationPolicies;
	}

	public Set<TrustAnchor> getTrustAnchors(String validationPolicyId) {
		if (null == validationPolicyId) {
			return null;
		}
		return trustAnchorMap.get(validationPolicyId);
	}

	public ValidationPolicy getDefaultPolicy() {
		ValidationPolicies all = this.validationPolicies;
		if (null == all || null == all.validationPolicies) {
			return null;
		}
		for (ValidationPolicy policy : all.validationPolicies) {
			if (DEFAULT_POLICY_OID.equalsIgnoreCase(policy.validationPolicyId)) {
				return policy;
			}
		}
		for (ValidationPolicy policy : all.validationPolicies) {
			if ("default".equalsIgnoreCase(policy.validationPolicyName)) {
				return policy;
			}
		}
		return null;
	}

	public int getMaxPathLen() {
		return maxPathLen;
	}

	public void setMaxPathLen(int maxPathLen) {
		ensureMutableConfig();
		this.maxPathLen = maxPathLen;
	}

	public boolean getAiaChase() {
		return aiaChase;
	}

	public void setAiaChase(boolean aiaChase) {
		ensureMutableConfig();
		this.aiaChase = aiaChase;
	}

	public String getAllowedAiaLocations() {
		return allowedAiaLocations;
	}

	public void setAllowedAiaLocations(String allowedAiaLocations) {
		ensureMutableConfig();
		this.allowedAiaLocations = allowedAiaLocations;
	}

	public boolean getRevocationEnabled() {
		return revocationEnabled;
	}

	public void setRevocationEnabled(boolean revocationEnabled) {
		ensureMutableConfig();
		this.revocationEnabled = revocationEnabled;
	}

	public boolean getRevocationEeOnly() {
		return revocationEeOnly;
	}

	public void setRevocationEeOnly(boolean revocationEeOnly) {
		ensureMutableConfig();
		this.revocationEeOnly = revocationEeOnly;
	}

	public boolean getOcspEnabled() {
		return ocspEnabled;
	}

	public void setOcspEnabled(boolean ocspEnabled) {
		ensureMutableConfig();
		this.ocspEnabled = ocspEnabled;
	}

	public boolean getCrlEnabled() {
		return crlEnabled;
	}

	public void setCrlEnabled(boolean crlEnabled) {
		ensureMutableConfig();
		this.crlEnabled = crlEnabled;
	}

	public int getCrlMaxSize() {
		return crlMaxSize;
	}

	public void setCrlMaxSize(int crlMaxSize) {
		ensureMutableConfig();
		this.crlMaxSize = crlMaxSize;
	}

	public String getCertPathProvider() {
		return certPathProvider;
	}

	public void setCertPathProvider(String certPathProvider) {
		ensureMutableConfig();
		this.certPathProvider = certPathProvider;
	}

	public String getCertPathAlgorithm() {
		return certPathAlgorithm;
	}

	public void setCertPathAlgorithm(String certPathAlgorithm) {
		ensureMutableConfig();
		this.certPathAlgorithm = certPathAlgorithm;
	}

	public boolean isIntermediateValidateOnLoad() {
		return intermediateValidateOnLoad;
	}

	public void setIntermediateValidateOnLoad(boolean intermediateValidateOnLoad) {
		ensureMutableConfig();
		this.intermediateValidateOnLoad = intermediateValidateOnLoad;
	}

	public boolean isIntermediateRevocationEnabled() {
		return intermediateRevocationEnabled;
	}

	public void setIntermediateRevocationEnabled(boolean intermediateRevocationEnabled) {
		ensureMutableConfig();
		this.intermediateRevocationEnabled = intermediateRevocationEnabled;
	}

}
