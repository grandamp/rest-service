package org.keysupport.api;

import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateRevokedException;
import java.util.Map;

import org.keysupport.api.pojo.vss.ValidationEvent;
import org.keysupport.api.pojo.vss.VssResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestServiceEventLogger {

	/*
	 * At the moment, we are logging validation failures, but; the intent is to log
	 * all major events.
	 */

	private final static Logger LOG = LoggerFactory.getLogger(RestServiceEventLogger.class);

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
				LOG.error(LoggingUtil.pojoToJson(Map.of("error", "TODO: Capture this exception cause", "cause", t.getClass().getCanonicalName(),"stacktrace", LoggingUtil.stackTraceToString(e))));
			}
		} else {
			event.message = e.getMessage();
		}
		LOG.warn(LoggingUtil.pojoToJson(event));
	}

	public static void logEvent(VssResponse vssResponse, CertificateExpiredException e) {
		ValidationEvent event = new ValidationEvent();
		event.x5tS256 = vssResponse.x5tS256;
		event.eventType = "VALPOL_FAIL";
		event.message = e.getMessage();
		LOG.warn(LoggingUtil.pojoToJson(event));
	}

	public static void logEvent(VssResponse vssResponse, CertificateNotYetValidException e) {
		ValidationEvent event = new ValidationEvent();
		event.x5tS256 = vssResponse.x5tS256;
		event.eventType = "VALPOL_FAIL";
		event.message = e.getMessage();
		LOG.warn(LoggingUtil.pojoToJson(event));
	}

	public static void logEvent(VssResponse vssResponse, CertPathValidatorException e) {
		ValidationEvent event = new ValidationEvent();
		event.x5tS256 = vssResponse.x5tS256;
		event.eventType = "VALPOL_FAIL";
		event.message = e.getMessage();
		LOG.warn(LoggingUtil.pojoToJson(event));
	}

}
