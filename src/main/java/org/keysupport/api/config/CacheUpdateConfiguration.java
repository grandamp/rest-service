package org.keysupport.api.config;

import java.util.concurrent.TimeUnit;

import org.keysupport.api.pkix.cache.singletons.IntermediateCacheSingleton;
import org.keysupport.api.singletons.ValidationPoliciesSingleton;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class CacheUpdateConfiguration {

	public CacheUpdateConfiguration() {
	}

	/*
	 * Run every 15 minutes
	 */
	@Scheduled(timeUnit = TimeUnit.MINUTES, fixedRate = 15)
	private static void refreshIntermediates() {
		/*
		 * Update intermediate cache from FPKI Playbook
		 */
		IntermediateCacheSingleton cache = IntermediateCacheSingleton.getInstance();
		cache.updateIntermediates();
		/*
		 * Update validation policies from project main branch
		 */
		ValidationPoliciesSingleton policy = ValidationPoliciesSingleton.getInstance();
		policy.updateValidationPolicies();
	}

}