package org.keysupport.api.controller.vss;

import org.keysupport.api.config.ConfigurationTrustAnchors;
import org.keysupport.api.pojo.vss.TrustAnchors;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrustAnchorsController {

	@GetMapping(path = "/vss/v2/trustanchors", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<TrustAnchors> trustAnchors() {
		return new ResponseEntity<>(ConfigurationTrustAnchors.getTrustAnchors(), HttpStatus.OK);
	}

}
