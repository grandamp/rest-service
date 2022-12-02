package org.keysupport.api.singletons;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class uses a singleton pattern to manage our HTTP client needs.
 */

public class HTTPClientSingleton {

	private final Logger LOG = LoggerFactory.getLogger(HTTPClientSingleton.class);

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

	public byte[] getData(URI uri) {
		HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
		HttpResponse<byte[]> response = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
		} catch (IOException e) {
			LOG.error("Error GETing data", e);
		} catch (InterruptedException e) {
			LOG.error("Error GETing data", e);
		}
		return response.body();
	}

}
