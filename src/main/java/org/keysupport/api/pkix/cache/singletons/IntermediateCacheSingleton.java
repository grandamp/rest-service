package org.keysupport.api.pkix.cache.singletons;

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
import java.util.concurrent.ConcurrentHashMap;

import org.keysupport.api.pkix.X509Util;
import org.keysupport.api.pojo.vss.ExcludedIntermediate;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.keysupport.api.singletons.HTTPClientSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class uses a singleton pattern to store the configured intermediate
 * cache.
 */
public class IntermediateCacheSingleton {

	private final Logger LOG = LoggerFactory.getLogger(IntermediateCacheSingleton.class);

	/**
	 * A map of intermediate stores that correspond to each validation policy.
	 */
	private ConcurrentHashMap<String, CertStore> intermediateMap = null;

	ObjectMapper mapper = null;

	private IntermediateCacheSingleton() {
		intermediateMap = new ConcurrentHashMap<String, CertStore>();
		mapper = new ObjectMapper();
	}

	/**
	 * @returns boolean
	 */
	private boolean excludedByPolicy(List<ExcludedIntermediate> valPolExcludeList, X509Certificate cert) {
		String x5tS256 = X509Util.x5tS256(cert);
		for (ExcludedIntermediate exclude : valPolExcludeList) {
			if (exclude.x5tS256.equalsIgnoreCase(x5tS256)) {
				logExcluded(cert, "Exclude by Policy: "+ exclude.excludeReason);
				return true;
			}
		}
		return false;
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
			logExcluded(cert, "Implementation Temporal Exclude: "+ e.getLocalizedMessage());
			return true;
		} catch (CertificateNotYetValidException e) {
			logExcluded(cert, "Implementation Temporal Exclude: "+ e.getLocalizedMessage());
			return true;
		}
		return false;
	}

	public void updateIntermediates(ValidationPolicy valPol) {
		List<String> cmsIntermediateHintListUri = valPol.cmsIntermediateHintListUri;
		List<X509Certificate> filteredCerts = new ArrayList<>();
		for (String cmsUri : cmsIntermediateHintListUri) {
			HTTPClientSingleton client = HTTPClientSingleton.getInstance();
			URI uri = URI.create(cmsUri);
			CertPath cp = client.getCms(uri);
			if (null != cp) {
				List<? extends Certificate> cmsCerts = cp.getCertificates();
				List<X509Certificate> certs = new ArrayList<X509Certificate>();
				for (Certificate cmsCert : cmsCerts) {
					certs.add((X509Certificate) cmsCert);
				}
				LOG.info("CMS object contains " + certs.size() + " certificates: " + uri.toASCIIString());
				/*
				 * Filter the Intermediates we received using exclusion methods
				 */
				for (X509Certificate cert : certs) {
					if (!excludedByPolicy(valPol.excludeIntermediates, cert) && !excludeByTemporal(cert)) {
						if (!filteredCerts.contains(cert)) {
							filteredCerts.add(cert);
						} else {
							LOG.warn("Excluding Duplicate Cert: " + cert.getSubjectX500Principal().toString());
						}
					} else {
						LOG.warn("Excluding Cert: " + cert.getSubjectX500Principal().toString());
					}
				}
			} else {
				LOG.error("Skipping invalid CMS from: " + uri.toASCIIString());
			}
		}
		/*
		 * Place certificates into a Collection CertStore, per `validationPolicyId`
		 */
		CertStoreParameters cparam = new CollectionCertStoreParameters(filteredCerts);
		CertStore intermediates = null;
		try {
			intermediates = CertStore.getInstance("Collection", cparam, "SUN");
		} catch (InvalidAlgorithmParameterException e) {
			LOG.error("Failed to create CertStore from CMS object", e);
		} catch (NoSuchAlgorithmException e) {
			LOG.error("Failed to create CertStore from CMS object", e);
		} catch (NoSuchProviderException e) {
			LOG.error("Failed to create CertStore from CMS object", e);
		}
		intermediateMap.put(valPol.validationPolicyId, intermediates);
	}
	
	private void logExcluded(X509Certificate cert, String reason) {
		String x5tS256 = X509Util.x5tS256(cert);
		ExcludedIntermediate exclude = new ExcludedIntermediate();
		exclude.excludeReason = reason;
		exclude.x509IssuerName = cert.getIssuerX500Principal().toString();
		exclude.x509SubjectName = cert.getSubjectX500Principal().toString();
		exclude.x5tS256 = x5tS256;
		String loggedExclustion = null;
		try {
			loggedExclustion = mapper.writeValueAsString(exclude);
		} catch (JsonProcessingException e1) {
			LOG.error("Error mapping POJO to JSON", e1);
		}
		LOG.warn(loggedExclustion);
	}

	private class SingletonHelper {
		private static final IntermediateCacheSingleton INSTANCE = new IntermediateCacheSingleton();
	}

	public static IntermediateCacheSingleton getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public CertStore getIntermediates(String validationPolicyId) {
		return intermediateMap.get(validationPolicyId);
	}

}
