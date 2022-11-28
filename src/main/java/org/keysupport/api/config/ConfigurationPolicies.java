package org.keysupport.api.config;

import java.util.List;

import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.keysupport.api.singletons.ValidationPoliciesSingleton;

/*
 * TODO:  We migrated the TrustAnchors and ValidationPolicies to JSON configs in AWS S3, so we need to figure out lifecycle rules for updates.
 */
public class ConfigurationPolicies {

	/**
	 *
	 * TODO: Document
	 *
	 * @return ValidationPolicies validationPolicies
	 */
	public static ValidationPolicies getValidationPolicies() {
		ValidationPoliciesSingleton validationPoliciesSingleton = ValidationPoliciesSingleton.getInstance();
		return validationPoliciesSingleton.getValidationPolicies();
	}

	/**
	 *
	 * TODO: Document
	 *
	 * @param policy
	 * @return
	 */
	public static ValidationPolicy getPolicy(String policy) {
		ValidationPolicies validationPolicies = ConfigurationPolicies.getValidationPolicies();
		List<ValidationPolicy> valPols = validationPolicies.validationPolicies;
		for (ValidationPolicy valPol: valPols) {
			if (valPol.validationPolicyId.equalsIgnoreCase(policy)) {
				return valPol;
			}
		}
		return null;
	}

}
