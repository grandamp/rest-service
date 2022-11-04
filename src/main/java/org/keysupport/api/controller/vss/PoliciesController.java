package org.keysupport.api.controller.vss;

import org.keysupport.api.config.ConfigurationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PoliciesController {

	@GetMapping(path = "/vss/v2/policies", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<ValidationPolicies> policies() {
		return new ResponseEntity<>(ConfigurationPolicies.getValidationPolicies(), HttpStatus.OK);
	}

}
