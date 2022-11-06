package org.keysupport.api.config;

import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.keysupport.api.singletons.ValidationPoliciesSingleton;

/*
 * TODO:  We migrated the TrustAnchors and ValidationPolicies to JSON configs in AWS S3, so we need to figure out lifecycle rules for updates.
 */
public class ConfigurationPolicies {

	public static ValidationPolicies getValidationPolicies() {
		ValidationPoliciesSingleton validationPoliciesSingleton = ValidationPoliciesSingleton.getInstance();
		return validationPoliciesSingleton.getValidationPolicies();
	}

	public static ValidationPolicy getPolicy(String string) {
		/*
		 * TODO: Return requested policy, for now, we will return `Default policy Prod and Cite`
		 */
		return ConfigurationPolicies.getValidationPolicies().validationPolicies.get(0);
	}
}
