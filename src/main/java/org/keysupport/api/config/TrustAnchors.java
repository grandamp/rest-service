package org.keysupport.api.config;

/**
 * This class uses a singleton pattern to store the configured Trust Anchors.
 *
 */
public class TrustAnchors {

	/*
	 * TODO: When the application loads, load in the JSON config for the Trust Anchors.
	 * 
	 * https://stackoverflow.com/questions/25764459/spring-boot-application-properties-value-not-populating
	 * 
	 * Then instantiate this singleton from the property that was received.
	 * 
	 * We want the the trust anchors to be as static as possible, and available as X509Certificate objects.
	 * 
	 */
	
	private TrustAnchors() {
	}

	private static class SingletonHelper {
		private static final TrustAnchors INSTANCE = new TrustAnchors();
	}

	public static TrustAnchors getInstance() {
		return SingletonHelper.INSTANCE;
	}

}
