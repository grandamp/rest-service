package org.keysupport.api.controller.vss;

import org.keysupport.api.config.ConfigurationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "policies", description = "Obtain Validation Policy information")
public class PoliciesController {

	@GetMapping(path = "/vss/v2/policies", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<ValidationPolicies> policies() {
		return new ResponseEntity<>(ConfigurationPolicies.getValidationPolicies(), HttpStatus.OK);
	}

	@GetMapping(path = "/vss/v2/policies/{validationPolicyId}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<ValidationPolicy> policy(@PathVariable String validationPolicyId) {
		ValidationPolicy requestedPolicy = ConfigurationPolicies.getPolicy(validationPolicyId);
		if (null != requestedPolicy) {
			return new ResponseEntity<>(requestedPolicy, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(requestedPolicy, HttpStatus.NOT_FOUND);
		}
	}

}
