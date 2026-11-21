package org.keysupport.api.health;

import org.keysupport.api.singletons.IntermediateCacheSingleton;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Actuator health indicator verifying that the intermediate CA cache is populated.
 */
@Component("intermediateCache")
public class IntermediateCacheHealthIndicator implements HealthIndicator {

	@Override
	public Health health() {
		IntermediateCacheSingleton cache = IntermediateCacheSingleton.getInstance();
		int count = cache.getCachedIntermediateCount();
		if (null == cache.getIntermediates() || count == 0) {
			return Health.down().withDetail("reason", "Intermediate CA cache is empty").build();
		}
		return Health.up().withDetail("intermediateCount", count).build();
	}
}
