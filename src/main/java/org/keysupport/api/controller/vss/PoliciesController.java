package org.keysupport.api.controller.vss;

import java.util.concurrent.TimeUnit;

import org.keysupport.api.config.ServiceValidationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.springframework.http.CacheControl;
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

	private static final CacheControl CACHE_CONTROL = CacheControl.maxAge(60, TimeUnit.SECONDS).cachePublic();

	@GetMapping(path = "/vss/v2/policies", produces = MediaType.APPLICATION_JSON_VALUE)
	@CrossOrigin(origins = "*")
	ResponseEntity<ValidationPolicies> policies() {
		return ResponseEntity.ok().cacheControl(CACHE_CONTROL).body(ServiceValidationPolicies.getValidationPolicies());
	}

	@GetMapping(path = "/vss/v2/policies/{validationPolicyId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@CrossOrigin(origins = "*")
	ResponseEntity<ValidationPolicy> policy(@PathVariable String validationPolicyId) {
		ValidationPolicy requestedPolicy = ServiceValidationPolicies.getPolicy(validationPolicyId);
		if (null != requestedPolicy) {
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + validationPolicyId + ".json");
			return new ResponseEntity<>(requestedPolicy, headers, HttpStatus.OK);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

}
