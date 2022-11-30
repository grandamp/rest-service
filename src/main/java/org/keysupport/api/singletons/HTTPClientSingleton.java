package org.keysupport.api.singletons;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.util.List;
import java.util.Optional;

import org.keysupport.api.pkix.X509Util;
import org.keysupport.api.pkix.cache.ElasticacheClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

/**
 * This class uses a singleton pattern to manage our HTTP client needs.
 */
public class HTTPClientSingleton {

	private final static Logger LOG = LoggerFactory.getLogger(HTTPClientSingleton.class);

	private final static String USER_AGENT = "https://api.keysupport.org/swagger-ui/index.html";

	private final static String accept = HttpHeaders.ACCEPT;

	private final static String mimeCrl = "application/pkix-crl";

	private final static String mimeCms = "application/pkcs7-mime";

	private final static String mimeTextPlainUtf8 = "text/plain; charset=utf-8";

	private HttpClient client = null;

	private HTTPClientSingleton() {
		/*
		 * Create HTTP Client
		 */
		client = HttpClient.newHttpClient();
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
		/*
		 * Set a custom User-Agent to identify calls from this code
		 *
		 * TODO: Eventually change to use build info
		 */
		/*
		 * Initial caching test, where we will cache all data via the call with the
		 * default 1hr TTL.
		 */
		ElasticacheClient mcClient = new ElasticacheClient("127.0.0.1");
		byte[] cacheResponse = mcClient.get(uri.toASCIIString());
		if (null != cacheResponse) {
			mcClient.close();
			return cacheResponse;
		} else {
			HttpRequest request = HttpRequest.newBuilder().uri(uri).setHeader("User-Agent", USER_AGENT)
					.setHeader(accept, mimeType).build();
			HttpResponse<byte[]> response = null;
			try {
				response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
			} catch (IOException e) {
				LOG.error("Error GETing data", e);
			} catch (InterruptedException e) {
				LOG.error("Error GETing data", e);
			}
			java.net.http.HttpHeaders headers = response.headers();
			List<String> cControl = headers.allValues("Cache-Control");
			for (String curVal: cControl) {
				LOG.info("Header:Cache-Control:value: " + curVal);
			}
			Optional<String> eTag = headers.firstValue("ETag");
			if (eTag.isPresent()) {
				LOG.info("Header:ETag:value: " + eTag.get());
			}
			Optional<String> lastModified = headers.firstValue("Last-Modified");
			if (lastModified.isPresent()) {
				LOG.info("Header:Last-Modified:value: " + X509Util.ISO8601DateStringFromHttpHeader(lastModified.get()));
			}
			
			/*
			 * Cache the response, and return to the client
			 */
			mcClient.put(uri.toASCIIString(), response.body());
			mcClient.close();
			return response.body();
		}
	}

	public X509CRL getCrl(URI uri) {
		byte[] crlBytes = getData(uri, mimeCrl);
		X509CRL crl = null;
		CertificateFactory cf = null;
		try {
			cf = CertificateFactory.getInstance("X.509");
		} catch (CertificateException e) {
			LOG.error("Failed to create CertificateFactory", e);
		}
		try {
			crl = (X509CRL) cf.generateCRL(new ByteArrayInputStream(crlBytes));
		} catch (CRLException e) {
			LOG.error("Failed to render CRL", e);
		}
		return crl;
	}

	public CertPath getCms(URI uri) {
		byte[] cmsBytes = getData(uri, mimeCms);
		CertPath cp = null;
		CertificateFactory cf = null;
		try {
			cf = CertificateFactory.getInstance("X.509");
		} catch (CertificateException e) {
			LOG.error("Failed to create CertificateFactory", e);
		}
		try {
			cp = cf.generateCertPath(new ByteArrayInputStream(cmsBytes), "PKCS7");
		} catch (CertificateException e) {
			LOG.error("Failed to parse CMS object", e);
		}
		return cp;
	}

	public String getText(URI uri) {
		byte[] textBytes = getData(uri, mimeTextPlainUtf8);
		return new String(textBytes, StandardCharsets.UTF_8);
	}

}
