package org.keysupport.api.health;

import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.keysupport.api.singletons.IntermediateCacheSingleton;
import org.keysupport.api.singletons.PkixMetricsSingleton;
import org.keysupport.api.singletons.ValidationCacheSingleton;
import org.keysupport.api.singletons.ValidationPoliciesSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * Registers lightweight Micrometer gauges and counters for VSS caches and policies.
 */
@Configuration
public class VssMetricsConfiguration {

	@Bean
	MeterBinder vssMeterBinder() {
		return registry -> {
			ValidationCacheSingleton validationCache = ValidationCacheSingleton.getInstance();
			Gauge.builder("vss.validation.cache.entries", validationCache, ValidationCacheSingleton::size)
					.description("Current number of entries in the VSS validation response cache")
					.register(registry);
			FunctionCounter.builder("vss.validation.cache.hits", validationCache, ValidationCacheSingleton::getHitCount)
					.description("Total VSS validation response cache hits")
					.register(registry);
			FunctionCounter.builder("vss.validation.cache.misses", validationCache, ValidationCacheSingleton::getMissCount)
					.description("Total VSS validation response cache misses")
					.register(registry);

			IntermediateCacheSingleton intermediateCache = IntermediateCacheSingleton.getInstance();
			Gauge.builder("vss.intermediates.cached", intermediateCache, IntermediateCacheSingleton::getCachedIntermediateCount)
					.description("Number of cached intermediate CA certificates")
					.register(registry);

			ValidationPoliciesSingleton policiesSingleton = ValidationPoliciesSingleton.getInstance();
			Gauge.builder("vss.policies.loaded", policiesSingleton, p -> {
				ValidationPolicies policies = p.getValidationPolicies();
				return null != policies && null != policies.validationPolicies
						? policies.validationPolicies.size()
						: 0;
			}).description("Number of loaded validation policies").register(registry);

			PkixMetricsSingleton.getInstance().bindTo(registry);
		};
	}
}
