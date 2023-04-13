package org.keysupport.api.config;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.keysupport.api.pkix.cache.singletons.IntermediateCacheSingleton;
import org.keysupport.api.pojo.vss.ValidationPolicy;
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
	 * TODO: Right now we fetch policy every 15 minutes
	 * 
	 * We should create deployment variables for the policy source URI and refresh rate.
	 */
	@Scheduled(timeUnit = TimeUnit.MINUTES, fixedRate = 15)
	private static void refreshIntermediates() {
		/*
		 * Update validation policies from project main branch
		 */
		ValidationPoliciesSingleton policy = ValidationPoliciesSingleton.getInstance();
		policy.updateValidationPolicies();
		/*
		 * Update intermediate cache from policy defined hints
		 */
		IntermediateCacheSingleton cache = IntermediateCacheSingleton.getInstance();
		List<ValidationPolicy> valPols = policy.getValidationPolicies().validationPolicies;
		for (ValidationPolicy valPol: valPols) {
			cache.updateIntermediates(valPol);
		}
	}

}