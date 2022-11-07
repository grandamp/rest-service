package org.keysupport.api.controller.cspreport;

import org.keysupport.api.controller.ServiceException;
import org.keysupport.api.pojo.cspreport.CspReportEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;

@RestController
public class CspReportController {

	private final static Logger LOG = LoggerFactory.getLogger(CspReportController.class);

	@Operation(hidden = true)
	@PostMapping(path = "/report", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<CspReportEntity> create(@RequestBody CspReportEntity entity) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonReport = null;
		try {
			jsonReport = mapper.writeValueAsString(entity);
		} catch (JsonProcessingException e) {
			LOG.error("Exception converting POJO to JSON:", e);
		}
		LOG.warn("Content Security Policy Violation Report Received: " + jsonReport);
		/*
		 * TODO: Capture/Persist Report rather than simply logging it?
		 */
		if (entity == null) {
			throw new ServiceException("entity was null");
		} else {
			return new ResponseEntity<>(entity, HttpStatus.CREATED);
		}
	}
}
