package org.keysupport.api.controller.vss;

import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

import org.keysupport.api.pkix.cache.singletons.IntermediateCacheSingleton;
import org.keysupport.api.pojo.vss.JsonX509Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "intermediates", description = "Obtain Cached Intermediates")
public class IntermediatesController {

	private final static Logger LOG = LoggerFactory.getLogger(IntermediatesController.class);

	@SuppressWarnings("unchecked")
	@GetMapping(path = "/vss/v2/intermediates/{validationPolicyId}", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<List<JsonX509Certificate>> intermediates(@PathVariable String validationPolicyId) {
		List<JsonX509Certificate> intermediates = new ArrayList<JsonX509Certificate>();
		IntermediateCacheSingleton intermediateCacheSingleton = IntermediateCacheSingleton.getInstance();
		CertStore intermediateStore = intermediateCacheSingleton.getIntermediates(validationPolicyId);
		Collection<X509Certificate> intermediateCerts = null;
		try {
			intermediateCerts = (Collection<X509Certificate>) intermediateStore.getCertificates(new X509CertSelector());
		} catch (CertStoreException e) {
			LOG.error("Error obtaining intermediates from CertStore", e);
		} catch (NullPointerException e) {
			LOG.info("There are no intermediates cached for the policy: " + validationPolicyId);
			return new ResponseEntity<>(intermediates, HttpStatus.NOT_FOUND);
		}
		for (X509Certificate cert: intermediateCerts) {
			JsonX509Certificate bCert = new JsonX509Certificate();
			try {
				bCert.x509Certificate = Base64.getEncoder().encodeToString(cert.getEncoded());
				LOG.debug("Intermediate Cert:\n" + cert.toString());
			} catch (CertificateEncodingException e) {
				LOG.error("Error Base64 encoding certificate from CertPath", e);
			}
			intermediates.add(bCert);
		}
		return new ResponseEntity<>(intermediates, HttpStatus.OK);
	}
}
