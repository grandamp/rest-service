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

	private static final Logger LOG = LoggerFactory.getLogger(RestServiceEventLogger.class);

	private RestServiceEventLogger() {
	}

	public static void logEvent(VssResponse vssResponse, CertPathBuilderException e) {
		ValidationEvent event = baseEvent(vssResponse);
		if (null != e) {
			Throwable cause = e.getCause();
			if (null != cause) {
				CertificateRevokedException revoked = findCause(cause, CertificateRevokedException.class);
				if (null != revoked) {
					event.message = null != cause.getMessage() ? cause.getMessage() : revoked.getMessage();
				} else {
					String detail = null != cause.getMessage() ? cause.getMessage() : e.getMessage();
					event.message = cause.getClass().getSimpleName()
							+ (null != detail ? ": " + detail : "");
					LOG.error(LoggingUtil.pojoToJson(Map.of(
							"error", "CertPathBuilderException cause",
							"cause", cause.getClass().getCanonicalName(),
							"message", null != detail ? detail : "",
							"stacktrace", LoggingUtil.stackTraceToString(e))));
				}
			} else {
				event.message = e.getMessage();
			}
		}
		LOG.warn(LoggingUtil.pojoToJson(event));
	}

	public static void logEvent(VssResponse vssResponse, CertificateExpiredException e) {
		ValidationEvent event = baseEvent(vssResponse);
		event.message = null != e ? e.getMessage() : null;
		LOG.warn(LoggingUtil.pojoToJson(event));
	}

	public static void logEvent(VssResponse vssResponse, CertificateNotYetValidException e) {
		ValidationEvent event = baseEvent(vssResponse);
		event.message = null != e ? e.getMessage() : null;
		LOG.warn(LoggingUtil.pojoToJson(event));
	}

	public static void logEvent(VssResponse vssResponse, CertPathValidatorException e) {
		ValidationEvent event = baseEvent(vssResponse);
		event.message = null != e ? e.getMessage() : null;
		LOG.warn(LoggingUtil.pojoToJson(event));
	}

	private static ValidationEvent baseEvent(VssResponse vssResponse) {
		ValidationEvent event = new ValidationEvent();
		event.x5tS256 = null != vssResponse ? vssResponse.x5tS256 : null;
		event.eventType = "VALPOL_FAIL";
		return event;
	}

	private static <T extends Throwable> T findCause(Throwable error, Class<T> targetType) {
		Throwable current = error;
		while (null != current) {
			if (targetType.isInstance(current)) {
				return targetType.cast(current);
			}
			current = current.getCause();
		}
		return null;
	}

}
