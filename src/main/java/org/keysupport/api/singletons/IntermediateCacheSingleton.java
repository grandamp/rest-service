package org.keysupport.api.singletons;

import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.keysupport.api.LoggingUtil;
import org.keysupport.api.pkix.X509Util;
import org.keysupport.api.pojo.vss.ExcludedIntermediate;
import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class uses an efficient initialization-on-demand holder singleton
 * pattern to manage the intermediate CA certificate cache. Only certificates
 * that validate under the default policy are admitted (independent of bundle
 * order), and periodic re-validation evicts expired, revoked, or irrelevant
 * intermediates.
 */
public class IntermediateCacheSingleton {

	private static final Logger LOG = LoggerFactory.getLogger(IntermediateCacheSingleton.class);
	private static final String JCE_PROVIDER = BouncyCastleFipsProvider.PROVIDER_NAME;

	private final Object updateLock = new Object();

	private volatile CertStore intermediateStore = null;
	private volatile List<X509Certificate> cachedCerts = List.of();
	private volatile List<ExcludedIntermediate> lastExcludedCerts = List.of();
	private volatile ScheduledExecutorService revalidationScheduler = null;

	private IntermediateCacheSingleton() {
		ensureBcFipsProvider();
	}

	private static class SingletonHelper {
		private static final IntermediateCacheSingleton INSTANCE = new IntermediateCacheSingleton();
	}

	public static IntermediateCacheSingleton getInstance() {
		return SingletonHelper.INSTANCE;
	}

	private static void ensureBcFipsProvider() {
		if (null == Security.getProvider(JCE_PROVIDER)) {
			Security.addProvider(new BouncyCastleFipsProvider());
		}
	}

	public CertStore getIntermediates() {
		return intermediateStore;
	}

	public int getCachedIntermediateCount() {
		return cachedCerts.size();
	}

	public List<X509Certificate> getCachedCerts() {
		return cachedCerts;
	}

	public List<ExcludedIntermediate> getLastExcludedCerts() {
		return lastExcludedCerts;
	}

	/**
	 * Fetch intermediates from {@code intermediatesUri}, validate each candidate
	 * against the default policy in an order-independent fixed-point loop, and
	 * atomically swap the live {@link CertStore}.
	 */
	public void updateIntermediates(String intermediatesUri) {
		if (null == intermediatesUri || intermediatesUri.isBlank()) {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "intermediatesUri must not be null or blank")));
			return;
		}
		URI uri = URI.create(intermediatesUri);
		HTTPClientSingleton client = HTTPClientSingleton.getInstance();
		var cp = client.getCms(uri);

		synchronized (updateLock) {
			List<ExcludedIntermediate> excludedCollector = new ArrayList<>();
			if (null == cp) {
				LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Skipping invalid CMS from: " + uri.toASCIIString())));
				if (null == intermediateStore) {
					swapStore(List.of());
				} else {
					revalidateCacheLocked();
				}
				return;
			}

			List<? extends Certificate> cmsCerts = cp.getCertificates();
			LOG.info(LoggingUtil.pojoToJson(Map.of("cms.numcerts", cmsCerts.size())));

			List<X509Certificate> uniqueCandidates = new ArrayList<>();
			Set<X509Certificate> seen = new LinkedHashSet<>();
			Date now = new Date();

			for (Certificate cmsCert : cmsCerts) {
				if (!(cmsCert instanceof X509Certificate cert)) {
					continue;
				}
				if (!seen.add(cert)) {
					logExcluded(cert, "Duplicate certificate in CMS bundle", excludedCollector);
					continue;
				}
				if (!isEligibleCaCandidate(cert, now, excludedCollector)) {
					continue;
				}
				uniqueCandidates.add(cert);
			}

			ValidationPoliciesSingleton policies = ValidationPoliciesSingleton.getInstance();
			if (!policies.isIntermediateValidateOnLoad()) {
				LOG.warn(LoggingUtil.pojoToJson(Map.of(
						"warning", "service.intermediates.validate-on-load is false; skipping default policy validation")));
				this.lastExcludedCerts = List.copyOf(excludedCollector);
				swapStore(uniqueCandidates);
				LOG.info(LoggingUtil.pojoToJson(Map.of("cache.numcerts", uniqueCandidates.size())));
				return;
			}
			ValidationPolicy defaultPolicy = policies.getDefaultPolicy();
			if (null == defaultPolicy) {
				LOG.error(LoggingUtil.pojoToJson(
						Map.of("error", "No default validation policy available; refusing to cache unvalidated intermediates")));
				for (X509Certificate candidate : uniqueCandidates) {
					logExcluded(candidate, "No default validation policy configured", excludedCollector);
				}
				this.lastExcludedCerts = List.copyOf(excludedCollector);
				swapStore(List.of());
				return;
			}

			List<X509Certificate> accepted = validateCandidatesFixedPoint(
					uniqueCandidates, List.of(defaultPolicy), policies, now, excludedCollector);
			this.lastExcludedCerts = List.copyOf(excludedCollector);
			swapStore(accepted);
			LOG.info(LoggingUtil.pojoToJson(Map.of("cache.numcerts", accepted.size())));
		}
	}

	/**
	 * Attempt to add a single intermediate certificate to the cache. The
	 * certificate is admitted only if it is a currently valid CA certificate
	 * that validates under the default policy.
	 *
	 * @return {@code true} if the certificate was accepted into the cache
	 */
	public boolean addIntermediate(X509Certificate cert) {
		if (null == cert) {
			return false;
		}
		synchronized (updateLock) {
			Date now = new Date();
			if (cachedCerts.contains(cert)) {
				logExcluded(cert, "Duplicate intermediate certificate");
				return false;
			}
			if (!isEligibleCaCandidate(cert, now)) {
				return false;
			}
			ValidationPoliciesSingleton policies = ValidationPoliciesSingleton.getInstance();
			ValidationPolicy defaultPolicy = policies.getDefaultPolicy();
			if (null == defaultPolicy) {
				logExcluded(cert, "No default validation policy configured");
				return false;
			}
			CertStore currentStore = null != this.intermediateStore
					? this.intermediateStore
					: createCertStore(this.cachedCerts, policies);
			String failureReason = validateAgainstPolicies(cert, List.of(defaultPolicy), currentStore, policies, now);
			if (null != failureReason) {
				logExcluded(cert, failureReason);
				return false;
			}
			List<X509Certificate> updated = new ArrayList<>(this.cachedCerts.size() + 1);
			updated.addAll(this.cachedCerts);
			updated.add(cert);
			swapStore(updated);
			return true;
		}
	}

	/**
	 * Re-validate all currently cached intermediate certificates against the
	 * active validation policies and evict any that are expired, revoked, or no
	 * longer relevant to any defined policy.
	 *
	 * @return number of evicted certificates
	 */
	public int revalidateCache() {
		synchronized (updateLock) {
			return revalidateCacheLocked();
		}
	}

	/**
	 * Start (or replace) the background daemon scheduler that periodically
	 * re-validates the intermediate cache.
	 */
	public synchronized void startPeriodicRevalidation(long intervalSeconds) {
		stopPeriodicRevalidation();
		if (intervalSeconds <= 0) {
			return;
		}
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread t = new Thread(r, "intermediate-cache-revalidator");
			t.setDaemon(true);
			return t;
		});
		scheduler.scheduleWithFixedDelay(this::revalidateCacheSafely, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
		this.revalidationScheduler = scheduler;
	}

	public synchronized void stopPeriodicRevalidation() {
		if (null != this.revalidationScheduler) {
			this.revalidationScheduler.shutdownNow();
			this.revalidationScheduler = null;
		}
	}

	private void revalidateCacheSafely() {
		try {
			int evicted = revalidateCache();
			if (evicted > 0) {
				LOG.info(LoggingUtil.pojoToJson(Map.of("cache.evicted", evicted, "cache.numcerts", cachedCerts.size())));
			}
		} catch (RuntimeException e) {
			LOG.error(LoggingUtil.pojoToJson(Map.of(
					"error", "Periodic intermediate cache revalidation failed",
					"stacktrace", LoggingUtil.stackTraceToString(e))));
		}
	}

	private int revalidateCacheLocked() {
		List<X509Certificate> current = this.cachedCerts;
		if (current.isEmpty()) {
			return 0;
		}

		Date now = new Date();
		List<ExcludedIntermediate> excludedCollector = new ArrayList<>();
		List<X509Certificate> eligible = new ArrayList<>(current.size());
		for (X509Certificate cert : current) {
			if (isEligibleCaCandidate(cert, now, excludedCollector)) {
				eligible.add(cert);
			}
		}

		ValidationPoliciesSingleton policies = ValidationPoliciesSingleton.getInstance();
		List<ValidationPolicy> activePolicies = orderedPolicies(policies);
		if (activePolicies.isEmpty()) {
			for (X509Certificate cert : eligible) {
				logExcluded(cert, "No validation policies defined", excludedCollector);
			}
			this.lastExcludedCerts = List.copyOf(excludedCollector);
			swapStore(List.of());
			return current.size();
		}

		List<X509Certificate> survivors = validateCandidatesFixedPoint(
				eligible, activePolicies, policies, now, excludedCollector);
		if (!excludedCollector.isEmpty()) {
			this.lastExcludedCerts = List.copyOf(excludedCollector);
		}
		swapStore(survivors);
		return current.size() - survivors.size();
	}

	private List<ValidationPolicy> orderedPolicies(ValidationPoliciesSingleton policies) {
		ValidationPolicies all = policies.getValidationPolicies();
		if (null == all || null == all.validationPolicies || all.validationPolicies.isEmpty()) {
			return List.of();
		}
		List<ValidationPolicy> ordered = new ArrayList<>(all.validationPolicies.size());
		ValidationPolicy defaultPolicy = policies.getDefaultPolicy();
		if (null != defaultPolicy) {
			ordered.add(defaultPolicy);
		}
		for (ValidationPolicy policy : all.validationPolicies) {
			if (policy != defaultPolicy) {
				ordered.add(policy);
			}
		}
		return ordered;
	}

	/**
	 * Validate every candidate against the full unordered candidate set, then
	 * shrink that set until only certificates that still validate without the
	 * rejected certificates remain.
	 */
	private List<X509Certificate> validateCandidatesFixedPoint(
			List<X509Certificate> candidates,
			List<ValidationPolicy> targetPolicies,
			ValidationPoliciesSingleton policies,
			Date now,
			List<ExcludedIntermediate> excludedCollector) {
		List<X509Certificate> pending = new ArrayList<>(candidates);

		while (!pending.isEmpty()) {
			CertStore candidateStore = createCertStore(pending, policies);
			List<X509Certificate> survivors = new ArrayList<>();
			for (X509Certificate candidate : pending) {
				String failureReason = validateAgainstPolicies(
						candidate, targetPolicies, candidateStore, policies, now);
				if (null == failureReason) {
					survivors.add(candidate);
				} else {
					logExcluded(candidate, failureReason, excludedCollector);
				}
			}
			if (survivors.size() == pending.size()) {
				return survivors;
			}
			pending = survivors;
		}
		return List.of();
	}

	private String validateAgainstPolicies(
			X509Certificate cert,
			List<ValidationPolicy> targetPolicies,
			CertStore intermediateStore,
			ValidationPoliciesSingleton policies,
			Date now) {
		String lastReason = "No matching validation policy";
		for (ValidationPolicy policy : targetPolicies) {
			String reason = validateWithPolicy(cert, policy, intermediateStore, policies, now);
			if (null == reason) {
				return null;
			}
			lastReason = reason;
		}
		return lastReason;
	}

	private String validateWithPolicy(
			X509Certificate cert,
			ValidationPolicy policy,
			CertStore intermediateStore,
			ValidationPoliciesSingleton policies,
			Date now) {
		Set<TrustAnchor> taList = policies.getTrustAnchors(policy.validationPolicyId);
		if (null == taList || taList.isEmpty()) {
			return "Policy " + policy.validationPolicyId + " has no trust anchors";
		}
		for (TrustAnchor ta : taList) {
			if (cert.equals(ta.getTrustedCert())) {
				return "Certificate is already a policy Trust Anchor";
			}
		}

		ensureBcFipsProvider();
		String provider = null != policies.getCertPathProvider() && !policies.getCertPathProvider().isBlank()
				? policies.getCertPathProvider()
				: "SUN";
		String algorithm = null != policies.getCertPathAlgorithm() && !policies.getCertPathAlgorithm().isBlank()
				? policies.getCertPathAlgorithm()
				: "PKIX";

		X509CertSelector selector = new X509CertSelector();
		selector.setCertificate(cert);

		PKIXBuilderParameters params;
		try {
			params = new PKIXBuilderParameters(taList, selector);
		} catch (InvalidAlgorithmParameterException e) {
			return "Invalid PKIX parameters: " + e.getLocalizedMessage();
		}

		params.setSigProvider(JCE_PROVIDER);
		params.setDate(now);
		if (null != policy.userPolicySet) {
			params.setInitialPolicies(new HashSet<>(policy.userPolicySet));
		}
		params.setPolicyMappingInhibited(policy.inhibitPolicyMapping);
		params.setExplicitPolicyRequired(policy.requireExplicitPolicy);
		params.setAnyPolicyInhibited(policy.inhibitAnyPolicy);
		params.setMaxPathLength(policies.getMaxPathLen());
		boolean checkRevocation = policies.isIntermediateRevocationEnabled();
		params.setRevocationEnabled(checkRevocation);
		if (null != intermediateStore) {
			params.addCertStore(intermediateStore);
		}

		CertPathBuilder cpb;
		CertPathValidator cpv;
		try {
			cpb = CertPathBuilder.getInstance(algorithm, provider);
			cpv = CertPathValidator.getInstance(algorithm, provider);
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			return "PKIX provider error: " + e.getLocalizedMessage();
		}

		/*
		 * Cache admission always checks full-chain revocation when intermediate
		 * revocation checking is enabled. revocationEeOnly applies only to
		 * end-entity validation requests, not to intermediate CA cache hygiene.
		 */
		if (checkRevocation) {
			PKIXRevocationChecker rc = (PKIXRevocationChecker) cpb.getRevocationChecker();
			params.addCertPathChecker(rc);
		}

		PKIXCertPathBuilderResult builderResult;
		try {
			builderResult = (PKIXCertPathBuilderResult) cpb.build(params);
		} catch (CertPathBuilderException e) {
			Throwable cause = e.getCause();
			if (null != cause && null != cause.getMessage()) {
				return cause.getMessage();
			}
			return e.getLocalizedMessage();
		} catch (InvalidAlgorithmParameterException e) {
			return "Invalid PKIX parameters: " + e.getLocalizedMessage();
		}

		try {
			cpv.validate(builderResult.getCertPath(), params);
			return null;
		} catch (CertPathValidatorException | InvalidAlgorithmParameterException e) {
			return e.getLocalizedMessage();
		}
	}

	private boolean isEligibleCaCandidate(X509Certificate cert, Date now, List<ExcludedIntermediate> collector) {
		try {
			cert.checkValidity(now);
		} catch (CertificateExpiredException | CertificateNotYetValidException e) {
			logExcluded(cert, "Temporal Exclude: " + e.getLocalizedMessage(), collector);
			return false;
		}
		if (cert.getBasicConstraints() == -1) {
			logExcluded(cert, "Non-CA Exclude: BasicConstraints is -1", collector);
			return false;
		}
		return true;
	}

	private boolean isEligibleCaCandidate(X509Certificate cert, Date now) {
		return isEligibleCaCandidate(cert, now, null);
	}

	private void swapStore(List<X509Certificate> certs) {
		ValidationPoliciesSingleton policies = ValidationPoliciesSingleton.getInstance();
		this.intermediateStore = createCertStore(certs, policies);
		this.cachedCerts = List.copyOf(certs);
		ValidationCacheSingleton.getInstance().clear();
	}

	private CertStore createCertStore(List<X509Certificate> certs, ValidationPoliciesSingleton policies) {
		String provider = null != policies && null != policies.getCertPathProvider()
				&& !policies.getCertPathProvider().isBlank()
						? policies.getCertPathProvider()
						: "SUN";
		CertStoreParameters cparam = new CollectionCertStoreParameters(certs);
		try {
			return CertStore.getInstance("Collection", cparam, provider);
		} catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
			LOG.error(LoggingUtil.pojoToJson(Map.of(
					"error", "Failed to create CertStore from CMS object",
					"stacktrace", LoggingUtil.stackTraceToString(e))));
			return null;
		}
	}

	private void logExcluded(X509Certificate cert, String reason) {
		logExcluded(cert, reason, null);
	}

	private void logExcluded(X509Certificate cert, String reason, List<ExcludedIntermediate> collector) {
		String x5tS256 = X509Util.x5tS256(cert);
		ExcludedIntermediate exclude = new ExcludedIntermediate();
		exclude.excludeReason = reason;
		exclude.x509IssuerName = cert.getIssuerX500Principal().toString();
		exclude.x509SubjectName = cert.getSubjectX500Principal().toString();
		exclude.x5tS256 = x5tS256;
		if (null != collector) {
			collector.add(exclude);
		}
		LOG.warn(LoggingUtil.pojoToJson(exclude));
	}

}
