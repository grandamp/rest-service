package org.keysupport.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.cert.CRLReason;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateRevokedException;
import java.util.Date;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

import org.junit.jupiter.api.Test;
import org.keysupport.api.controller.ServiceException;
import org.keysupport.api.pojo.vss.VssResponse;

class RestServiceEventAndUtilTest {

	@Test
	void loggingUtil_serializesJsonHandlesUnserializableAndFormatsStackTrace() {
		String json = LoggingUtil.pojoToJson(Map.of("k", "v"));
		assertEquals("{\"k\":\"v\"}", json);

		Object selfRef = new Object() {
			@SuppressWarnings("unused")
			public Object getSelf() {
				return this;
			}
		};
		assertNull(LoggingUtil.pojoToJson(selfRef));

		assertEquals("", LoggingUtil.stackTraceToString(null));
		String trace = LoggingUtil.stackTraceToString(new RuntimeException("boom"));
		assertTrue(trace.contains("RestServiceEventAndUtilTest"));
	}

	@Test
	void restServiceEventLogger_handlesAllExceptionVariantsWithoutThrowing() {
		VssResponse resp = new VssResponse();
		resp.x5tS256 = "abc123";

		RestServiceEventLogger.logEvent(resp, new CertificateExpiredException("expired"));
		RestServiceEventLogger.logEvent(resp, new CertificateNotYetValidException("not-yet-valid"));
		RestServiceEventLogger.logEvent(resp, new CertPathValidatorException("validator-failed"));

		RestServiceEventLogger.logEvent(resp, new CertPathBuilderException("no-path"));

		CertificateRevokedException revoked = new CertificateRevokedException(
				new Date(), CRLReason.KEY_COMPROMISE, new X500Principal("CN=Test CA"), Map.of());
		CertPathValidatorException wrappedRevoked = new CertPathValidatorException("revoked", revoked);
		RestServiceEventLogger.logEvent(resp, new CertPathBuilderException("build-fail", wrappedRevoked));

		CertPathValidatorException nonRevokedCause = new CertPathValidatorException("missing OCSP responder");
		RestServiceEventLogger.logEvent(resp, new CertPathBuilderException("build-fail", nonRevokedCause));

		RestServiceEventLogger.logEvent(null, (CertPathBuilderException) null);
		RestServiceEventLogger.logEvent(null, (CertificateExpiredException) null);
		RestServiceEventLogger.logEvent(null, (CertificateNotYetValidException) null);
		RestServiceEventLogger.logEvent(null, (CertPathValidatorException) null);
	}

	@Test
	void serviceException_preservesMessageAndCause() {
		ServiceException ex1 = new ServiceException("bad request");
		assertEquals("bad request", ex1.getMessage());

		Throwable cause = new IllegalStateException("root");
		ServiceException ex2 = new ServiceException("wrapped", cause);
		assertEquals("wrapped", ex2.getMessage());
		assertSame(cause, ex2.getCause());
	}
}
