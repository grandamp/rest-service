package org.keysupport.api.controller.vss;

import org.keysupport.api.config.ConfigurationTrustAnchors;

import org.keysupport.api.pojo.vss.TrustAnchors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TrustAnchorsController {

	private final static Logger LOG = LoggerFactory.getLogger(TrustAnchorsController.class);

	@GetMapping(path = "/vss/v2/trustanchors", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<TrustAnchors> trustAnchors() {

		return new ResponseEntity<>(ConfigurationTrustAnchors.getTrustAnchors(), HttpStatus.OK);
	}

}
