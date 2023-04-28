package org.keysupport.api.singletons;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.keysupport.api.RestServiceApplication;
import org.keysupport.api.controller.ServiceException;
import org.keysupport.api.pojo.vss.JsonTrustAnchor;
import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class uses a singleton pattern to store the configured Validation
 * Policies with Trust Anchors.
 */
public class ValidationPoliciesSingleton {

	private final Logger LOG = LoggerFactory.getLogger(ValidationPoliciesSingleton.class);

	/*
	 * TODO: Make a config option ASAP!
	 */
	private final String polUri = "https://raw.githubusercontent.com/grandamp/rest-service/main/configuration/policies.json";

	private ValidationPolicies validationPolicies = null;

	/**
	 * A map of TrustAnchor collections that correspond to each validation policy.
	 */
	private ConcurrentHashMap<String, HashSet<TrustAnchor>> trustAnchotMap = null;

	private ValidationPoliciesSingleton() {
		trustAnchotMap = new ConcurrentHashMap<String, HashSet<TrustAnchor>>();
	}

	public void updateValidationPolicies() {
		HTTPClientSingleton client = HTTPClientSingleton.getInstance();
		URI uri = URI.create(polUri);
		String validationPoliciesJson = client.getText(uri);
		if (null != validationPoliciesJson) {
			LOG.info(validationPoliciesJson);
			ObjectMapper mapper = new ObjectMapper();
			try {
				validationPolicies = mapper.readValue(validationPoliciesJson, ValidationPolicies.class);
			} catch (JsonMappingException e) {
				LOG.error("Error converting JSON to POJO", e);
			} catch (JsonProcessingException e) {
				LOG.error("Error converting JSON to POJO", e);
			}
			/*
			 * Iterate through each ValidationPolicy, and initialize the
			 * HashSet<TrustAnchor>
			 */
			List<ValidationPolicy> policies = validationPolicies.validationPolicies;
			for (ValidationPolicy policy : policies) {
				HashSet<TrustAnchor> taList = new HashSet<TrustAnchor>();
				List<JsonTrustAnchor> anchors = policy.trustAnchors;
				for (JsonTrustAnchor currentTa : anchors) {
					X509Certificate ta = null;
					try {
						byte[] certBytes = null;
						CertificateFactory cf = null;
						ByteArrayInputStream bais = null;
						try {
							certBytes = Base64.getDecoder().decode(currentTa.x509Certificate);
						} catch (Throwable e) {
							LOG.error("Internal Validation Error", e);
							throw new ServiceException("Internal Validation Error");
						}
						if (null != certBytes) {
							cf = CertificateFactory.getInstance("X509");
							bais = new ByteArrayInputStream(certBytes);
							ta = (X509Certificate) cf.generateCertificate(bais);
						} else {
							LOG.error("Internal Validation Error, null certBytes");
							throw new ServiceException("Internal Validation Error");
						}
					} catch (CertificateException e) {
						LOG.error("Internal Validation Error", e);
						throw new ServiceException("Internal Validation Error");
					}
					TrustAnchor anchor = new TrustAnchor(ta, null);
					taList.add(anchor);
				}
				trustAnchotMap.put(policy.validationPolicyId, taList);
			}
		} else {
			/*
			 * Check to see if this failure is an update, or; an initial fetch of the
			 * mandatory policies.
			 */
			if (null != validationPolicies) {
				LOG.warn("Failed to refresh ValidationPolicies JSON");
			} else {
				LOG.error("FATAL: Failed to obtain initial ValidationPolicies JSON from \"" + uri.toASCIIString()
						+ "\", shutting down!");
				SpringApplication.run(RestServiceApplication.class, (String[]) null).close();
			}
		}
	}

	private static class SingletonHelper {
		private static final ValidationPoliciesSingleton INSTANCE = new ValidationPoliciesSingleton();
	}

	public static ValidationPoliciesSingleton getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public ValidationPolicies getValidationPolicies() {
		return validationPolicies;
	}

	public HashSet<TrustAnchor> getTrustAnchors(String validationPolicyId) {
		return trustAnchotMap.get(validationPolicyId);
	}

}
