package org.keysupport.api.health;

import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.keysupport.api.singletons.ValidationPoliciesSingleton;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Actuator health indicator verifying that validation policies are loaded.
 */
@Component("validationPolicies")
public class ValidationPoliciesHealthIndicator implements HealthIndicator {

	@Override
	public Health health() {
		ValidationPolicies policies = ValidationPoliciesSingleton.getInstance().getValidationPolicies();
		if (null == policies || null == policies.validationPolicies || policies.validationPolicies.isEmpty()) {
			return Health.down().withDetail("reason", "Validation policies not loaded").build();
		}
		return Health.up().withDetail("policyCount", policies.validationPolicies.size()).build();
	}
}
