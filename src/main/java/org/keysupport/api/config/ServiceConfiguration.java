package org.keysupport.api.config;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Map;

import org.keysupport.api.LoggingUtil;
import org.keysupport.api.singletons.IntermediateCacheSingleton;
import org.keysupport.api.singletons.ValidationPoliciesSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class ServiceConfiguration {

	private final static Logger LOG = LoggerFactory.getLogger(ServiceConfiguration.class);

	public ServiceConfiguration(
			@Value("${service.policies.uri}") String polUri, 
			@Value("${service.intermediates.uri}") String intermediatesUri, 
			@Value("${service.validation.pkix.max-path-length}") int maxPathLen, 
			@Value("${service.systemlog.enabled}") boolean systemLogEnabled, 
			@Value("${service.systemlog.file:/opt/vss/ext/logs/system.log}") String systemLogFile, 
			@Value("${service.validation.pkix.aia-chase}") boolean aiaChase, 
			@Value("${service.validation.pkix.revocation-enabled}") boolean revocationEnabled, 
			@Value("${service.validation.pkix.revocation-ee-only}") boolean revocationEeOnly, 
			@Value("${service.validation.pkix.ocsp-enabled}") boolean ocspEnabled, 
			@Value("${service.validation.pkix.crl-enabled}") boolean crlEnabled) {
		/*
		 * Update validation policies from defined policies URI
		 *
		 * Ensure `service.policies.uri` has been defined, and if not; terminate
		 */
		if (null == polUri) {				
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "service.policies.uri must not be null, shutting down!")));
			System.exit(0);
		}
		LOG.info(LoggingUtil.pojoToJson(Map.of("service.policies.uri", polUri)));
		ValidationPoliciesSingleton policy = ValidationPoliciesSingleton.getInstance();
		policy.updateValidationPolicies(polUri);
		policy.setMaxPathLen(maxPathLen);
		policy.setAiaChase(aiaChase);
		policy.setRevocationEnabled(revocationEnabled);
		policy.setRevocationEeOnly(revocationEeOnly);
		policy.setOcspEnabled(ocspEnabled);
		policy.setCrlEnabled(crlEnabled);
		/*
		 * Update intermediate cache from policy defined inventory URI
		 *
		 * Ensure `service.intermediates.uri` has been defined, and if not; terminate
		 */
		if (null == intermediatesUri) {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "service.intermediates.uri must not be null, shutting down!")));
			System.exit(0);
		}
		LOG.info(LoggingUtil.pojoToJson(Map.of("service.intermediates.uri", intermediatesUri)));
		IntermediateCacheSingleton cache = IntermediateCacheSingleton.getInstance();
		cache.updateIntermediates(intermediatesUri);
		/*
		 * Redirect System.out and System.err to log to the trace log if defined
		 */
		if (systemLogEnabled) {
			File traceLogFile = new File(systemLogFile);
			PrintStream logStream = null;
			try {
				logStream = new PrintStream(traceLogFile);
			} catch (FileNotFoundException e) {
				LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Error accessing tracelog file: " + systemLogFile, "stacktrace", LoggingUtil.stackTraceToString(e))));
			}
	        System.setOut(logStream);
	        System.setErr(logStream);
		}
	}

}