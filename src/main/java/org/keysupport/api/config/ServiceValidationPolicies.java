package org.keysupport.api.config;

import java.util.List;

import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.keysupport.api.singletons.ValidationPoliciesSingleton;

public class ServiceValidationPolicies {

	/**
	 *
	 * This method provides a way to internally obtain all certificate validation policies that have been configured.
	 *
	 * @return ValidationPolicies validationPolicies
	 */
	public static ValidationPolicies getValidationPolicies() {
		ValidationPoliciesSingleton validationPoliciesSingleton = ValidationPoliciesSingleton.getInstance();
		return validationPoliciesSingleton.getValidationPolicies();
	}

	/**
	 *
	 * This method provides a way to internally obtain a specific validation policy.
	 *
	 * @param policy
	 * @return
	 */
	public static ValidationPolicy getPolicy(String policy) {
		ValidationPolicies validationPolicies = ServiceValidationPolicies.getValidationPolicies();
		List<ValidationPolicy> valPols = validationPolicies.validationPolicies;
		for (ValidationPolicy valPol: valPols) {
			if (valPol.validationPolicyId.equalsIgnoreCase(policy)) {
				return valPol;
			}
		}
		return null;
	}

}
