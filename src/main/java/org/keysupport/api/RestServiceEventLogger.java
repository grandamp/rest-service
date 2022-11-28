package org.keysupport.api;

import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateRevokedException;

import org.keysupport.api.pojo.vss.ValidationEvent;
import org.keysupport.api.pojo.vss.VssResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestServiceEventLogger {

	/*
	 * TODO: We should probably configure the eventType values as an Enum.
	 *
	 * At the moment, we are logging validation failures, but; the intent is to log
	 * all major events.
	 */

	private final static Logger LOG = LoggerFactory.getLogger(RestServiceEventLogger.class);

	/*
	 * TODO: Seems odd to have a Jackson mapper in this class, but; we will be
	 * converting the Validation Event POJO in all logging events.
	 */
	private static ObjectMapper mapper = new ObjectMapper();

	public static void logEvent(VssResponse vssResponse, CertPathBuilderException e) {
		ValidationEvent event = new ValidationEvent();
		event.x5tS256 = vssResponse.x5tS256;
		event.eventType = "VALPOL_FAIL";
		/*
		 * Check and see if this CertPathBuilderException contains a cause
		 */
		Throwable t = e.getCause();
		if (null != t) {
			if (t.getCause() instanceof CertificateRevokedException) {
				event.message = t.getMessage();
			} else {
				LOG.error("TODO: Capture this exception cause: ", t);
				LOG.error(t.getClass().getCanonicalName());
			}
		} else {
			event.message = e.getMessage();
		}
		try {
			String eventStr = mapper.writeValueAsString(event);
			LOG.warn(eventStr);
		} catch (JsonProcessingException e1) {
			LOG.error("Error writing validation event.  Looging stack trace: ", e);
		}
	}

	public static void logEvent(VssResponse vssResponse, CertificateExpiredException e) {
		ValidationEvent event = new ValidationEvent();
		event.x5tS256 = vssResponse.x5tS256;
		event.eventType = "VALPOL_FAIL";
		event.message = e.getMessage();
		try {
			String eventStr = mapper.writeValueAsString(event);
			LOG.warn(eventStr);
		} catch (JsonProcessingException e1) {
			LOG.error("Error writing validation event.  Looging stack trace: ", e);
		}
	}

	public static void logEvent(VssResponse vssResponse, CertificateNotYetValidException e) {
		ValidationEvent event = new ValidationEvent();
		event.x5tS256 = vssResponse.x5tS256;
		event.eventType = "VALPOL_FAIL";
		event.message = e.getMessage();
		try {
			String eventStr = mapper.writeValueAsString(event);
			LOG.warn(eventStr);
		} catch (JsonProcessingException e1) {
			LOG.error("Error writing validation event.  Looging stack trace: ", e);
		}
	}

	public static void logEvent(VssResponse vssResponse, CertPathValidatorException e) {
		ValidationEvent event = new ValidationEvent();
		event.x5tS256 = vssResponse.x5tS256;
		event.eventType = "VALPOL_FAIL";
		event.message = e.getMessage();
		try {
			String eventStr = mapper.writeValueAsString(event);
			LOG.warn(eventStr);
		} catch (JsonProcessingException e1) {
			LOG.error("Error writing validation event.  Looging stack trace: ", e);
		}
	}

}
