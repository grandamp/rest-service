package org.keysupport.api.singletons;

import java.net.URI;

import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class uses a singleton pattern to store the configured Validation Policies with Trust Anchors.
 */
public class ValidationPoliciesSingleton {

	private final Logger LOG = LoggerFactory.getLogger(ValidationPoliciesSingleton.class);

	private final String polUri = "https://raw.githubusercontent.com/grandamp/rest-service/main/configuration/policies.json";

	private ValidationPolicies validationPolicies = null;

	private ValidationPoliciesSingleton() {
	}

	public void updateValidationPolicies() {
		HTTPClientSingleton client = HTTPClientSingleton.getInstance();
		URI uri = URI.create(polUri);
		String validationPoliciesJson = client.getText(uri);
		ObjectMapper mapper = new ObjectMapper();
		try {
			validationPolicies = mapper.readValue(validationPoliciesJson, ValidationPolicies.class);
		} catch (JsonMappingException e) {
			LOG.error("Error converting JSON to POJO", e);
		} catch (JsonProcessingException e) {
			LOG.error("Error converting JSON to POJO", e);
		}
		LOG.info("ValidationPolicies: " + validationPoliciesJson);
	}

	private static class SingletonHelper {
		private static final ValidationPoliciesSingleton INSTANCE = new ValidationPoliciesSingleton();
	}

	public static ValidationPoliciesSingleton getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public ValidationPolicies getValidationPolicies() {
		return validationPolicies;
	}

}
