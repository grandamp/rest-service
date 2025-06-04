package org.keysupport.api.singletons;

import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertPath;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.keysupport.api.LoggingUtil;
import org.keysupport.api.pkix.X509Util;
import org.keysupport.api.pojo.vss.ExcludedIntermediate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class uses a singleton pattern to store the configured intermediate
 * cache.
 */
public class IntermediateCacheSingleton {

	private final Logger LOG = LoggerFactory.getLogger(IntermediateCacheSingleton.class);

	/*
	 * Our intermediate CertStore
	 */
	private CertStore intermediateStore = null;

	ObjectMapper mapper = null;

	private IntermediateCacheSingleton() {
		mapper = new ObjectMapper();
	}

	/**
	 * @returns boolean
	 */
	private boolean excludeByTemporal(X509Certificate cert) {
		/*
		 * We will only perform Temporal validation on the proposed intermediates, since
		 * the entity responsible for crafting policy should be aware of what to
		 * include, and; deny.
		 */
		try {
			cert.checkValidity();
		} catch (CertificateExpiredException e) {
			logExcluded(cert, "Implementation Temporal Exclude: " + e.getLocalizedMessage());
			return true;
		} catch (CertificateNotYetValidException e) {
			logExcluded(cert, "Implementation Temporal Exclude: " + e.getLocalizedMessage());
			return true;
		}
		return false;
	}

	public void updateIntermediates(String intermediatesUri) {
		List<X509Certificate> filteredCerts = new ArrayList<>();
		HTTPClientSingleton client = HTTPClientSingleton.getInstance();
		URI uri = URI.create(intermediatesUri);
		CertPath cp = client.getCms(uri);
		if (null != cp) {
			List<? extends Certificate> cmsCerts = cp.getCertificates();
			List<X509Certificate> certs = new ArrayList<X509Certificate>();
			for (Certificate cmsCert : cmsCerts) {
				certs.add((X509Certificate) cmsCert);
			}
			LOG.info(LoggingUtil.pojoToJson(Map.of("cms.numcerts", certs.size())));
			/*
			 * Filter the Intermediates we received using exclusion methods
			 */
			for (X509Certificate cert : certs) {
				if (!excludeByTemporal(cert)) {
					if (!filteredCerts.contains(cert)) {
						filteredCerts.add(cert);
					} else {
						LOG.warn(LoggingUtil.pojoToJson(Map.of("error", "Excluding Duplicate Cert: " + cert.getSubjectX500Principal().toString())));
					}
				} else {
					LOG.warn(LoggingUtil.pojoToJson(Map.of("error", "Excluding Cert: " + cert.getSubjectX500Principal().toString())));
				}
			}
		} else {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Skipping invalid CMS from: " + uri.toASCIIString())));
		}
		/*
		 * Place certificates into a Collection CertStore, per `validationPolicyId`
		 */
		CertStoreParameters cparam = new CollectionCertStoreParameters(filteredCerts);
		CertStore intermediates = null;
		try {
			intermediates = CertStore.getInstance("Collection", cparam, "SUN");
		} catch (InvalidAlgorithmParameterException e) {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Failed to create CertStore from CMS object", "stacktrace", LoggingUtil.stackTraceToString(e))));
		} catch (NoSuchAlgorithmException e) {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Failed to create CertStore from CMS object", "stacktrace", LoggingUtil.stackTraceToString(e))));
		} catch (NoSuchProviderException e) {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Failed to create CertStore from CMS object", "stacktrace", LoggingUtil.stackTraceToString(e))));
		}
		intermediateStore = intermediates;
	}

	private void logExcluded(X509Certificate cert, String reason) {
		String x5tS256 = X509Util.x5tS256(cert);
		ExcludedIntermediate exclude = new ExcludedIntermediate();
		exclude.excludeReason = reason;
		exclude.x509IssuerName = cert.getIssuerX500Principal().toString();
		exclude.x509SubjectName = cert.getSubjectX500Principal().toString();
		exclude.x5tS256 = x5tS256;
		LOG.warn(LoggingUtil.pojoToJson(exclude));
	}

	private class SingletonHelper {
		private static final IntermediateCacheSingleton INSTANCE = new IntermediateCacheSingleton();
	}

	public static IntermediateCacheSingleton getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public CertStore getIntermediates() {
		return intermediateStore;
	}

}
