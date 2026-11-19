package org.keysupport.api.config;

import java.util.List;

import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.keysupport.api.singletons.ValidationPoliciesSingleton;

public class ServiceValidationPolicies {

	private ServiceValidationPolicies() {
	}

	/**
	 * This method provides a way to internally obtain all certificate validation
	 * policies that have been configured.
	 *
	 * @return ValidationPolicies validationPolicies
	 */
	public static ValidationPolicies getValidationPolicies() {
		ValidationPoliciesSingleton validationPoliciesSingleton = ValidationPoliciesSingleton.getInstance();
		return validationPoliciesSingleton.getValidationPolicies();
	}

	/**
	 * This method provides a way to internally obtain a specific validation
	 * policy.
	 *
	 * @param policy
	 * @return ValidationPolicy matching policy, or null if not found
	 */
	public static ValidationPolicy getPolicy(String policy) {
		if (null == policy) {
			return null;
		}
		ValidationPolicies validationPolicies = ServiceValidationPolicies.getValidationPolicies();
		if (null == validationPolicies || null == validationPolicies.validationPolicies) {
			return null;
		}
		List<ValidationPolicy> valPols = validationPolicies.validationPolicies;
		for (ValidationPolicy valPol : valPols) {
			if (null != valPol.validationPolicyId && valPol.validationPolicyId.equalsIgnoreCase(policy)) {
				return valPol;
			}
		}
		return null;
	}

}
