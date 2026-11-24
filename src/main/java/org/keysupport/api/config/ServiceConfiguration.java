package org.keysupport.api.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.security.Security;
import java.util.Map;

import org.keysupport.api.LoggingUtil;
import org.keysupport.api.jetty.JettyCustomizer;
import org.keysupport.api.singletons.IntermediateCacheSingleton;
import org.keysupport.api.singletons.ValidationCacheSingleton;
import org.keysupport.api.singletons.ValidationPoliciesSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfiguration {

	private static final Logger LOG = LoggerFactory.getLogger(ServiceConfiguration.class);
	private static final long DEFAULT_REVALIDATE_INTERVAL_SECONDS = 3600L;

	public ServiceConfiguration(
			String polUri,
			String intermediatesUri,
			int maxPathLen,
			boolean systemLogEnabled,
			String systemLogFile,
			boolean aiaChase,
			String allowedAiaLocations,
			boolean revocationEnabled,
			boolean revocationEeOnly,
			boolean ocspEnabled,
			boolean crlEnabled,
			int crlMaxSize,
			String certPathProvider,
			String certPathAlgorithm) {
		this(polUri, intermediatesUri, maxPathLen, systemLogEnabled, systemLogFile, aiaChase, allowedAiaLocations,
				revocationEnabled, revocationEeOnly, ocspEnabled, crlEnabled, crlMaxSize, certPathProvider,
				certPathAlgorithm, true, true, DEFAULT_REVALIDATE_INTERVAL_SECONDS);
	}

	public ServiceConfiguration(
			@Value("${service.policies.uri}") String polUri,
			@Value("${service.intermediates.uri}") String intermediatesUri,
			@Value("${service.validation.pkix.max-path-length}") int maxPathLen,
			@Value("${service.systemlog.enabled}") boolean systemLogEnabled,
			@Value("${service.systemlog.file:/opt/vss/ext/logs/system.log}") String systemLogFile,
			@Value("${service.validation.pkix.aia-chase}") boolean aiaChase,
			@Value("${service.validation.pkix.allowed-aia-locations:any}") String allowedAiaLocations,
			@Value("${service.validation.pkix.revocation-enabled}") boolean revocationEnabled,
			@Value("${service.validation.pkix.revocation-ee-only}") boolean revocationEeOnly,
			@Value("${service.validation.pkix.ocsp-enabled}") boolean ocspEnabled,
			@Value("${service.validation.pkix.crl-enabled}") boolean crlEnabled,
			@Value("${service.validation.pkix.crl-max-size:52428800}") int crlMaxSize,
			@Value("${service.validation.pkix.certpath-provider:SUN}") String certPathProvider,
			@Value("${service.validation.pkix.certpath-algorithm:PKIX}") String certPathAlgorithm,
			@Value("${service.intermediates.validate-on-load:true}") boolean intermediateValidateOnLoad,
			@Value("${service.intermediates.revocation-enabled:true}") boolean intermediateRevocationEnabled,
			@Value("${service.intermediates.revalidate-interval-seconds:3600}") long revalidateIntervalSeconds) {
		this(polUri, intermediatesUri, maxPathLen, systemLogEnabled, systemLogFile, aiaChase, allowedAiaLocations,
				revocationEnabled, revocationEeOnly, ocspEnabled, crlEnabled, crlMaxSize, certPathProvider,
				certPathAlgorithm, intermediateValidateOnLoad, intermediateRevocationEnabled, revalidateIntervalSeconds,
				ValidationCacheSingleton.DEFAULT_ENABLED,
				ValidationCacheSingleton.DEFAULT_SUCCESS_TTL_SECONDS,
				ValidationCacheSingleton.DEFAULT_AFFIRMATIVELY_INVALID_TTL_SECONDS,
				ValidationCacheSingleton.DEFAULT_INDETERMINATE_FAIL_TTL_SECONDS,
				ValidationCacheSingleton.DEFAULT_MAX_ENTRIES,
				ValidationCacheSingleton.DEFAULT_COMPACT_AUDIT_LOGGING);
	}

	public ServiceConfiguration(
			@Value("${service.policies.uri}") String polUri,
			@Value("${service.intermediates.uri}") String intermediatesUri,
			@Value("${service.validation.pkix.max-path-length}") int maxPathLen,
			@Value("${service.systemlog.enabled}") boolean systemLogEnabled,
			@Value("${service.systemlog.file:/opt/vss/ext/logs/system.log}") String systemLogFile,
			@Value("${service.validation.pkix.aia-chase}") boolean aiaChase,
			@Value("${service.validation.pkix.allowed-aia-locations:any}") String allowedAiaLocations,
			@Value("${service.validation.pkix.revocation-enabled}") boolean revocationEnabled,
			@Value("${service.validation.pkix.revocation-ee-only}") boolean revocationEeOnly,
			@Value("${service.validation.pkix.ocsp-enabled}") boolean ocspEnabled,
			@Value("${service.validation.pkix.crl-enabled}") boolean crlEnabled,
			@Value("${service.validation.pkix.crl-max-size:52428800}") int crlMaxSize,
			@Value("${service.validation.pkix.certpath-provider:SUN}") String certPathProvider,
			@Value("${service.validation.pkix.certpath-algorithm:PKIX}") String certPathAlgorithm,
			@Value("${service.intermediates.validate-on-load:true}") boolean intermediateValidateOnLoad,
			@Value("${service.intermediates.revocation-enabled:true}") boolean intermediateRevocationEnabled,
			@Value("${service.intermediates.revalidate-interval-seconds:3600}") long revalidateIntervalSeconds,
			@Value("${service.validation.cache.enabled:true}") boolean validationCacheEnabled,
			@Value("${service.validation.cache.success-ttl-seconds:14400}") long validationCacheSuccessTtlSeconds,
			@Value("${service.validation.cache.affirmatively-invalid-ttl-seconds:-1}") long validationCacheInvalidTtlSeconds,
			@Value("${service.validation.cache.max-entries:10000}") int validationCacheMaxEntries) {
		this(polUri, intermediatesUri, maxPathLen, systemLogEnabled, systemLogFile, aiaChase, allowedAiaLocations,
				revocationEnabled, revocationEeOnly, ocspEnabled, crlEnabled, crlMaxSize, certPathProvider,
				certPathAlgorithm, intermediateValidateOnLoad, intermediateRevocationEnabled, revalidateIntervalSeconds,
				validationCacheEnabled, validationCacheSuccessTtlSeconds, validationCacheInvalidTtlSeconds,
				ValidationCacheSingleton.DEFAULT_INDETERMINATE_FAIL_TTL_SECONDS,
				validationCacheMaxEntries, ValidationCacheSingleton.DEFAULT_COMPACT_AUDIT_LOGGING);
	}

	public ServiceConfiguration(
			@Value("${service.policies.uri}") String polUri,
			@Value("${service.intermediates.uri}") String intermediatesUri,
			@Value("${service.validation.pkix.max-path-length}") int maxPathLen,
			@Value("${service.systemlog.enabled}") boolean systemLogEnabled,
			@Value("${service.systemlog.file:/opt/vss/ext/logs/system.log}") String systemLogFile,
			@Value("${service.validation.pkix.aia-chase}") boolean aiaChase,
			@Value("${service.validation.pkix.allowed-aia-locations:any}") String allowedAiaLocations,
			@Value("${service.validation.pkix.revocation-enabled}") boolean revocationEnabled,
			@Value("${service.validation.pkix.revocation-ee-only}") boolean revocationEeOnly,
			@Value("${service.validation.pkix.ocsp-enabled}") boolean ocspEnabled,
			@Value("${service.validation.pkix.crl-enabled}") boolean crlEnabled,
			@Value("${service.validation.pkix.crl-max-size:52428800}") int crlMaxSize,
			@Value("${service.validation.pkix.certpath-provider:SUN}") String certPathProvider,
			@Value("${service.validation.pkix.certpath-algorithm:PKIX}") String certPathAlgorithm,
			@Value("${service.intermediates.validate-on-load:true}") boolean intermediateValidateOnLoad,
			@Value("${service.intermediates.revocation-enabled:true}") boolean intermediateRevocationEnabled,
			@Value("${service.intermediates.revalidate-interval-seconds:3600}") long revalidateIntervalSeconds,
			@Value("${service.validation.cache.enabled:true}") boolean validationCacheEnabled,
			@Value("${service.validation.cache.success-ttl-seconds:14400}") long validationCacheSuccessTtlSeconds,
			@Value("${service.validation.cache.affirmatively-invalid-ttl-seconds:-1}") long validationCacheInvalidTtlSeconds,
			@Value("${service.validation.cache.max-entries:10000}") int validationCacheMaxEntries,
			@Value("${service.validation.cache.compact-audit-logging:true}") boolean validationCacheCompactAuditLogging) {
		this(polUri, intermediatesUri, maxPathLen, systemLogEnabled, systemLogFile, aiaChase, allowedAiaLocations,
				revocationEnabled, revocationEeOnly, ocspEnabled, crlEnabled, crlMaxSize, certPathProvider,
				certPathAlgorithm, intermediateValidateOnLoad, intermediateRevocationEnabled, revalidateIntervalSeconds,
				validationCacheEnabled, validationCacheSuccessTtlSeconds, validationCacheInvalidTtlSeconds,
				ValidationCacheSingleton.DEFAULT_INDETERMINATE_FAIL_TTL_SECONDS,
				validationCacheMaxEntries, validationCacheCompactAuditLogging);
	}

	@Autowired
	public ServiceConfiguration(
			@Value("${service.policies.uri}") String polUri,
			@Value("${service.intermediates.uri}") String intermediatesUri,
			@Value("${service.validation.pkix.max-path-length}") int maxPathLen,
			@Value("${service.systemlog.enabled}") boolean systemLogEnabled,
			@Value("${service.systemlog.file:/opt/vss/ext/logs/system.log}") String systemLogFile,
			@Value("${service.validation.pkix.aia-chase}") boolean aiaChase,
			@Value("${service.validation.pkix.allowed-aia-locations:any}") String allowedAiaLocations,
			@Value("${service.validation.pkix.revocation-enabled}") boolean revocationEnabled,
			@Value("${service.validation.pkix.revocation-ee-only}") boolean revocationEeOnly,
			@Value("${service.validation.pkix.ocsp-enabled}") boolean ocspEnabled,
			@Value("${service.validation.pkix.crl-enabled}") boolean crlEnabled,
			@Value("${service.validation.pkix.crl-max-size:52428800}") int crlMaxSize,
			@Value("${service.validation.pkix.certpath-provider:SUN}") String certPathProvider,
			@Value("${service.validation.pkix.certpath-algorithm:PKIX}") String certPathAlgorithm,
			@Value("${service.intermediates.validate-on-load:true}") boolean intermediateValidateOnLoad,
			@Value("${service.intermediates.revocation-enabled:true}") boolean intermediateRevocationEnabled,
			@Value("${service.intermediates.revalidate-interval-seconds:3600}") long revalidateIntervalSeconds,
			@Value("${service.validation.cache.enabled:true}") boolean validationCacheEnabled,
			@Value("${service.validation.cache.success-ttl-seconds:14400}") long validationCacheSuccessTtlSeconds,
			@Value("${service.validation.cache.affirmatively-invalid-ttl-seconds:-1}") long validationCacheInvalidTtlSeconds,
			@Value("${service.validation.cache.indeterminate-fail-ttl-seconds:120}") long validationCacheIndeterminateFailTtlSeconds,
			@Value("${service.validation.cache.max-entries:10000}") int validationCacheMaxEntries,
			@Value("${service.validation.cache.compact-audit-logging:true}") boolean validationCacheCompactAuditLogging) {
		/*
		 * Ensure PQC/FIPS JSSE properties are initialized before any outbound HTTPS fetches
		 */
		JettyCustomizer.configureGlobalTlsProperties();
		/*
		 * Ensure `service.policies.uri` is configured, otherwise fail startup cleanly
		 */
		if (null == polUri || polUri.isBlank()) {
			String msg = "service.policies.uri must not be null or blank, shutting down!";
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", msg)));
			throw new IllegalStateException(msg);
		}
		LOG.info(LoggingUtil.pojoToJson(Map.of("service.policies.uri", polUri)));
		ValidationPoliciesSingleton policy = ValidationPoliciesSingleton.getInstance();
		policy.updateValidationPolicies(polUri);
		policy.setMaxPathLen(maxPathLen);
		policy.setAiaChase(aiaChase);
		policy.setAllowedAiaLocations(allowedAiaLocations);
		policy.setRevocationEnabled(revocationEnabled);
		policy.setRevocationEeOnly(revocationEeOnly);
		policy.setOcspEnabled(ocspEnabled);
		policy.setCrlEnabled(crlEnabled);
		policy.setCrlMaxSize(crlMaxSize);
		policy.setCertPathProvider(certPathProvider);
		policy.setCertPathAlgorithm(certPathAlgorithm);
		policy.setIntermediateValidateOnLoad(intermediateValidateOnLoad);
		policy.setIntermediateRevocationEnabled(intermediateRevocationEnabled);
		policy.lockConfiguration();
		ValidationCacheSingleton.getInstance().configure(
				validationCacheEnabled,
				validationCacheSuccessTtlSeconds,
				validationCacheInvalidTtlSeconds,
				validationCacheIndeterminateFailTtlSeconds,
				validationCacheMaxEntries,
				validationCacheCompactAuditLogging);
		LOG.info(LoggingUtil.pojoToJson(Map.of(
				"service.validation.cache.enabled", validationCacheEnabled,
				"service.validation.cache.success-ttl-seconds", validationCacheSuccessTtlSeconds,
				"service.validation.cache.affirmatively-invalid-ttl-seconds", validationCacheInvalidTtlSeconds,
				"service.validation.cache.indeterminate-fail-ttl-seconds", validationCacheIndeterminateFailTtlSeconds,
				"service.validation.cache.max-entries", validationCacheMaxEntries,
				"service.validation.cache.compact-audit-logging", validationCacheCompactAuditLogging)));
		/*
		 * Set revocation & AIA JSSE properties before populating the intermediate cache
		 */
		if ((revocationEnabled || intermediateRevocationEnabled) && crlEnabled) {
			System.setProperty("com.sun.security.enableCRLDP", "true");
		}
		if ((revocationEnabled || intermediateRevocationEnabled) && ocspEnabled) {
			Security.setProperty("ocsp.enable", "true");
		}
		System.setProperty("com.sun.security.crl.maxSize", String.valueOf(crlMaxSize));
		LOG.info(LoggingUtil.pojoToJson(Map.of("com.sun.security.crl.maxSize", crlMaxSize)));
		if (crlMaxSize > 0 && crlMaxSize < 1024) {
			LOG.warn(LoggingUtil.pojoToJson(Map.of(
					"warning",
					"service.validation.pkix.crl-max-size is in bytes; value " + crlMaxSize
							+ " is under 1024 bytes and may discard real CRLs")));
		}
		if (aiaChase) {
			System.setProperty("com.sun.security.enableAIAcaIssuers", "true");
			System.setProperty("com.sun.security.allowedAIALocations", allowedAiaLocations);
			LOG.info(LoggingUtil.pojoToJson(Map.of("com.sun.security.allowedAIALocations", allowedAiaLocations)));
		}
		/*
		 * Ensure `service.intermediates.uri` is configured, otherwise fail startup cleanly
		 */
		if (null == intermediatesUri || intermediatesUri.isBlank()) {
			String msg = "service.intermediates.uri must not be null or blank, shutting down!";
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", msg)));
			throw new IllegalStateException(msg);
		}
		LOG.info(LoggingUtil.pojoToJson(Map.of("service.intermediates.uri", intermediatesUri)));
		IntermediateCacheSingleton cache = IntermediateCacheSingleton.getInstance();
		cache.updateIntermediates(intermediatesUri);
		cache.startPeriodicRevalidation(revalidateIntervalSeconds);
		/*
		 * Redirect System.out and System.err to log to the trace log if defined
		 */
		if (systemLogEnabled) {
			File traceLogFile = new File(systemLogFile);
			try {
				PrintStream logStream = new PrintStream(traceLogFile);
				System.setOut(logStream);
				System.setErr(logStream);
			} catch (FileNotFoundException e) {
				LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Error accessing tracelog file: " + systemLogFile,
						"stacktrace", LoggingUtil.stackTraceToString(e))));
			}
		}
	}

}
