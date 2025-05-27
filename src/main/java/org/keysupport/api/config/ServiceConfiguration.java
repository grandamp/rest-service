package org.keysupport.api.config;
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
	
	public ServiceConfiguration(@Value("${service.policies.uri}") String polUri, @Value("${service.intermediates.uri}") String intermediatesUri) {
		/*
		 * Update validation policies from defined policies URI
		 * 
		 * Ensure `service.policies.uri` has been defined, and if not; terminate
		 */
		if (null == polUri) {
			LOG.error("FATAL: \"service.policies.uri\" must not be null, shutting down!");
			System.exit(0);
		}
		LOG.info("Service Policies URI: " + polUri);
		ValidationPoliciesSingleton policy = ValidationPoliciesSingleton.getInstance();
		policy.updateValidationPolicies(polUri);
		/*
		 * Update intermediate cache from policy defined inventory URI
		 * 
		 * Ensure `service.intermediates.uri` has been defined, and if not; terminate
		 */
		if (null == intermediatesUri) {
			LOG.error("FATAL: \"service.intermediates.uri\" must not be null, shutting down!");
			System.exit(0);
		}
		LOG.info("Service Intermediates URI: " + intermediatesUri);
		IntermediateCacheSingleton cache = IntermediateCacheSingleton.getInstance();
		cache.updateIntermediates(intermediatesUri);
	}

}