package org.keysupport.api.singletons;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

import org.keysupport.api.LoggingUtil;
import org.keysupport.api.config.ConfigurationSwagger;
import org.keysupport.api.jetty.JettyCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/**
 * This class uses an efficient initialization-on-demand holder singleton
 * pattern to manage shared HTTP/HTTPS client connections with low latency and
 * detailed TLS/protocol error reporting.
 */
public class HTTPClientSingleton {

	private static final Logger LOG = LoggerFactory.getLogger(HTTPClientSingleton.class);

	private static final String USER_AGENT = ConfigurationSwagger.resolveVersion();
	private static final String ACCEPT_HEADER = HttpHeaders.ACCEPT;
	private static final String MIME_CMS = "application/pkcs7-mime";
	private static final String MIME_TEXT_PLAIN_UTF8 = "text/plain; charset=utf-8";
	private static final int MAX_ENTITY_SIZE = 1_000_000;
	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

	private final HttpClient client;

	private HTTPClientSingleton() {
		JettyCustomizer.configureGlobalTlsProperties();
		this.client = HttpClient.newBuilder()
				.connectTimeout(CONNECT_TIMEOUT)
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build();
	}

	private static class SingletonHelper {
		private static final HTTPClientSingleton INSTANCE = new HTTPClientSingleton();
	}

	public static HTTPClientSingleton getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public HttpClient getClient() {
		return client;
	}

	public byte[] getData(URI uri, String mimeType) {
		if (null == uri) {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "URI must not be null")));
			return null;
		}
		HttpRequest request = HttpRequest.newBuilder()
				.uri(uri)
				.timeout(REQUEST_TIMEOUT)
				.setHeader("User-Agent", USER_AGENT)
				.setHeader(ACCEPT_HEADER, mimeType)
				.build();
		HttpResponse<byte[]> response;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			reportTransportError(uri, e);
			return null;
		} catch (IOException | IllegalArgumentException | SecurityException e) {
			reportTransportError(uri, e);
			return null;
		}

		Map<String, List<String>> headers = response.headers().map();
		LOG.info(LoggingUtil.pojoToJson(headers));
		if (LOG.isDebugEnabled() && response.sslSession().isPresent()) {
			SSLSession session = response.sslSession().get();
			LOG.debug(LoggingUtil.pojoToJson(Map.of(
					"uri", uri.toASCIIString(),
					"tlsProtocol", session.getProtocol(),
					"tlsCipherSuite", session.getCipherSuite())));
		}

		if (response.statusCode() == HttpStatus.OK.value()) {
			byte[] responseBody = response.body();
			if (null == responseBody) {
				LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Received null entity from " + uri.toASCIIString())));
				return null;
			} else if (responseBody.length >= MAX_ENTITY_SIZE) {
				LOG.error(LoggingUtil.pojoToJson(
						Map.of("error", "Entity from " + uri.toASCIIString() + " exceeds " + MAX_ENTITY_SIZE + " bytes")));
				return null;
			} else {
				return responseBody;
			}
		} else {
			LOG.error(LoggingUtil.pojoToJson(
					Map.of("error", "Received HTTP " + response.statusCode() + " status from " + uri.toASCIIString())));
			return null;
		}
	}

	public CertPath getCms(URI uri) {
		byte[] cmsBytes = getData(uri, MIME_CMS);
		if (null == cmsBytes) {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "CMS not received", "uri", uri.toASCIIString())));
			return null;
		}
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			return cf.generateCertPath(new ByteArrayInputStream(cmsBytes), "PKCS7");
		} catch (CertificateException e) {
			LOG.error(LoggingUtil.pojoToJson(Map.of(
					"error", "Failed to parse CMS object",
					"uri", uri.toASCIIString(),
					"stacktrace", LoggingUtil.stackTraceToString(e))));
			return null;
		}
	}

	public String getText(URI uri) {
		byte[] textBytes = getData(uri, MIME_TEXT_PLAIN_UTF8);
		if (null != textBytes) {
			return new String(textBytes, StandardCharsets.UTF_8);
		}
		LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Unexpected null response", "uri", uri.toASCIIString())));
		return null;
	}

	private static void reportTransportError(URI uri, Throwable error) {
		SSLException tlsError = findCause(error, SSLException.class);
		Throwable root = rootCause(error);
		String message = null != root.getMessage() ? root.getMessage() : root.getClass().getName();
		Map<String, Object> logPayload = new LinkedHashMap<>();
		logPayload.put("error", message);
		logPayload.put("exception", error.getClass().getName());
		if (null != tlsError) {
			logPayload.put("tlsError", tlsError.getClass().getName()
					+ (null != tlsError.getMessage() ? ": " + tlsError.getMessage() : ""));
		}
		logPayload.put("uri", uri.toASCIIString());
		logPayload.put("stacktrace", LoggingUtil.stackTraceToString(error));
		LOG.error(LoggingUtil.pojoToJson(logPayload));
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

	private static Throwable rootCause(Throwable error) {
		Throwable current = error;
		while (null != current.getCause()) {
			current = current.getCause();
		}
		return current;
	}

}
