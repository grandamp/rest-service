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
import java.util.List;
import java.util.Map;

import org.keysupport.api.LoggingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/**
 * This class uses a singleton pattern to manage our HTTP client needs.
 */
public class HTTPClientSingleton {

	private final Logger LOG = LoggerFactory.getLogger(HTTPClientSingleton.class);

	private final String USER_AGENT = "https://api.keysupport.org/swagger-ui/index.html";

	private final String accept = HttpHeaders.ACCEPT;

	private final String mimeCms = "application/pkcs7-mime";

	private final String mimeTextPlainUtf8 = "text/plain; charset=utf-8";

	private final int MAX_ENTITY_SIZE = 1000000;

	private HttpClient client = null;

	private HTTPClientSingleton() {
		/*
		 * Create HTTP Client
		 */
		client = HttpClient.newHttpClient();
	}

	private class SingletonHelper {
		private static final HTTPClientSingleton INSTANCE = new HTTPClientSingleton();
	}

	public static HTTPClientSingleton getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public HttpClient getClient() {
		return client;
	}

	public byte[] getData(URI uri, String mimeType) {
			HttpRequest request = HttpRequest.newBuilder().uri(uri).setHeader("User-Agent", USER_AGENT)
					.setHeader(accept, mimeType).build();
			HttpResponse<byte[]> response = null;
			try {
				response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
			} catch (IOException e) {
				LOG.error(LoggingUtil.pojoToJson(Map.of("error", e.getCause().getMessage(), "uri", uri.toASCIIString(), "stacktrace", LoggingUtil.stackTraceToString(e))));
				return null;
			} catch (InterruptedException e) {
				LOG.error(LoggingUtil.pojoToJson(Map.of("error", e.getCause().getMessage(), "uri", uri.toASCIIString(), "stacktrace", LoggingUtil.stackTraceToString(e))));
				return null;
			} catch (IllegalArgumentException  e) {
				LOG.error(LoggingUtil.pojoToJson(Map.of("error", e.getCause().getMessage(), "uri", uri.toASCIIString(), "stacktrace", LoggingUtil.stackTraceToString(e))));
				return null;
			} catch (SecurityException e) {
				LOG.error(LoggingUtil.pojoToJson(Map.of("error", e.getCause().getMessage(), "uri", uri.toASCIIString(), "stacktrace", LoggingUtil.stackTraceToString(e))));
				return null;
			}
			Map<String, List<String>> headers = response.headers().map();
			LOG.error(LoggingUtil.pojoToJson(headers));
			/*
			 * Cache the response, and return to the client, so long as we received a 200
			 *
			 */
			if (response.statusCode() == HttpStatus.OK.value()) {
				byte[] responseBody = response.body();
				if (null == responseBody) {
					LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Received null entity from " + uri.toASCIIString())));
					return null;
				} else if (responseBody.length >= MAX_ENTITY_SIZE) {
					LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Entity from " + uri.toASCIIString() + " exceeds " + MAX_ENTITY_SIZE + "bytes")));
					return null;
				} else {
					return responseBody;
				}
			} else {
				LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Received HTTP " + response.statusCode() + " status from " + uri.toASCIIString())));
				return null;
			}
	}

	public CertPath getCms(URI uri) {
		byte[] cmsBytes = getData(uri, mimeCms);
		if (null == cmsBytes) {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "CMS not received", "uri", uri.toASCIIString())));
			return null;
		}
		CertPath cp = null;
		CertificateFactory cf = null;
		try {
			cf = CertificateFactory.getInstance("X.509");
		} catch (CertificateException e) {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Failed to create CertificateFactory", "uri", uri.toASCIIString(), "stacktrace", LoggingUtil.stackTraceToString(e))));
		}
		try {
			cp = cf.generateCertPath(new ByteArrayInputStream(cmsBytes), "PKCS7");
		} catch (CertificateException e) {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Failed to parse CMS object", "uri", uri.toASCIIString(), "stacktrace", LoggingUtil.stackTraceToString(e))));
		}
		return cp;
	}

	public String getText(URI uri) {
		byte[] textBytes = getData(uri, mimeTextPlainUtf8);
		if (null != textBytes) {
			return new String(textBytes, StandardCharsets.UTF_8);
		} else {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Unexpected null response", "uri", uri.toASCIIString())));
			return null;
		}
	}
}
