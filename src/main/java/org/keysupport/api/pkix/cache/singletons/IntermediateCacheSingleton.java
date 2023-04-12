package org.keysupport.api.pkix.cache.singletons;

import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertPath;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.x500.X500Principal;

import org.keysupport.api.pkix.X509Util;
import org.keysupport.api.pojo.vss.ExcludedIntermediate;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.keysupport.api.singletons.HTTPClientSingleton;
import org.keysupport.api.singletons.ValidationPoliciesSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class uses a singleton pattern to store the configured intermediate
 * cache.
 *
 * TODO:
 * 
 * - Download intermediate URLs for each policy;
 * - Consolidate duplicate intermediates per policy as much as possible;
 * - Periodically validate each intermediate; and;
 * - Use the validation cache for each CA as much as possible. 
 */
public class IntermediateCacheSingleton {
	
	private final Logger LOG = LoggerFactory.getLogger(IntermediateCacheSingleton.class);

	private ValidationPoliciesSingleton policy = null;
	
	/*
	 * TODO: Create a map of intermediate stores that correspond to each validation policy.
	 */
	ConcurrentHashMap<String, CertStore> intermediateMap = null;
	
	ObjectMapper mapper = null;

	private IntermediateCacheSingleton() {
		policy = ValidationPoliciesSingleton.getInstance();
		intermediateMap = new ConcurrentHashMap<String, CertStore>();
		mapper = new ObjectMapper();
	}

	private boolean excludedByPolicy(List<ExcludedIntermediate> valPolExcludeList, X509Certificate cert) {
		String x5tS256 = X509Util.x5tS256(cert);
		for (ExcludedIntermediate exclude: valPolExcludeList) {
			if (exclude.x5tS256.equalsIgnoreCase(x5tS256)) {
				String loggedExclustion = null;
				try {
					loggedExclustion = mapper.writeValueAsString(exclude);
				} catch (JsonProcessingException e) {
					LOG.error("Error mapping POJO to JSON", e);
				}
				LOG.info(loggedExclustion);
				return true;
			}
		}
		return false;
	}

	public void updateIntermediates() {
		/*
		 * WIP: For now we are only fetching the first default policy since all of our example policies leverage the same Trust Anchor
		 * 
		 * TODO:  Refactor to incorporate intermediates from multiple Trust Anchors
		 */
		ValidationPolicy valPol = policy.getValidationPolicies().validationPolicies.get(0);
		String policyUriStr = valPol.cmsIntermediateHintListUri.get(0);
		HTTPClientSingleton client = HTTPClientSingleton.getInstance();
		URI uri = URI.create(policyUriStr);
		/*
		 * Temp for testing
		 */
		//URI uri = URI.create("https://raw.githubusercontent.com/grandamp/rest-service/main/configuration/FCPCAG2-Intermediates/rejecting-wrong-direction-current.p7b");
		/*
		 * Download the CMS object
		 */
		CertPath cp = client.getCms(uri);
		@SuppressWarnings("unchecked")
		List<X509Certificate> certs = (List<X509Certificate>) cp.getCertificates();
		LOG.info("CMS object contains " + certs.size() + " certificates");
		List<X509Certificate> filteredCerts = new ArrayList<>();
		/*
		 * Filter the Intermediates we received
		 */
		for (X509Certificate cert : certs) {
			/*
			 * New filtering logic
			 * 
			 * Derive x5t#S256, and obtain subject and issuer for logging
			 */
			X500Principal subject = cert.getSubjectX500Principal();
			X500Principal issuer = cert.getIssuerX500Principal();
			LOG.info("CMS Cert: " + subject.getName() + " signed by " + issuer.getName());
			if (!excludedByPolicy(valPol.excludeIntermediates, cert)) {
				filteredCerts.add(cert);
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
