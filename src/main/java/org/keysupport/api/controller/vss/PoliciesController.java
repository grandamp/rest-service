package org.keysupport.api.controller.vss;

import org.keysupport.api.config.ServiceValidationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "policies", description = "Obtain Validation Policy Definitions")
public class PoliciesController {

	@GetMapping(path = "/vss/v2/policies", produces = MediaType.APPLICATION_JSON_VALUE)
	@CrossOrigin(origins = "*")
	ResponseEntity<ValidationPolicies> policies() {
		return new ResponseEntity<>(ServiceValidationPolicies.getValidationPolicies(), HttpStatus.OK);
	}

	@GetMapping(path = "/vss/v2/policies/{validationPolicyId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@CrossOrigin(origins = "*")
	ResponseEntity<ValidationPolicy> policy(@PathVariable String validationPolicyId) {
	    HttpHeaders headers = new HttpHeaders();
		ValidationPolicy requestedPolicy = ServiceValidationPolicies.getPolicy(validationPolicyId);
		if (null != requestedPolicy) {
		    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + validationPolicyId + ".json");
			return new ResponseEntity<>(requestedPolicy, headers, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(requestedPolicy, HttpStatus.NOT_FOUND);
		}
	}

}
