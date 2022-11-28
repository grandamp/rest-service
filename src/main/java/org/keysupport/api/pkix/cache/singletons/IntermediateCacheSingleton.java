package org.keysupport.api.pkix.cache.singletons;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertPath;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class uses a singleton pattern to store the configured intermediate
 * cache.
 *
 * Since we are initially testing against the U.S. Federal PKI, we will use an
 * artifact from the Federal PKI Playbooks:
 *
 * https://playbooks.idmanagement.gov/fpki/tools/fpkigraph/
 *
 * (Source CMS CERTS-ONLY message containing all Federal PKI Intermediates)
 *
 * https://github.com/GSA/ficam-playbooks/raw/federalist-pages/_fpki/tools/CACertificatesValidatingToFederalCommonPolicyG2.p7b
 */
public class IntermediateCacheSingleton {

	private final static Logger LOG = LoggerFactory.getLogger(IntermediateCacheSingleton.class);

	private static final String p7Uri = "https://raw.githubusercontent.com/GSA/ficam-playbooks/federalist-pages/_fpki/tools/CACertificatesValidatingToFederalCommonPolicyG2.p7b";

	private static CertStore intermediates = null;

	private IntermediateCacheSingleton() {

		/*
		 * Download the CMS object
		 */
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(p7Uri)).build();
		HttpResponse<byte[]> response = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
		} catch (IOException e) {
			LOG.error("Failed to download CMS object", e);
		} catch (InterruptedException e) {
			LOG.error("Failed to download CMS object", e);
		}
		byte[] data = response.body();
		LOG.info("CMS object is " + data.length + " bytes in size");
		/*
		 * Parse the CMS object
		 */
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		CertificateFactory cf = null;
		try {
			cf = CertificateFactory.getInstance("X.509");
		} catch (CertificateException e) {
			LOG.error("Failed to parse CMS object", e);
		}
		CertPath cp = null;
		try {
			cp = cf.generateCertPath(bais, "PKCS7");
		} catch (CertificateException e) {
			LOG.error("Failed to parse CMS object", e);
		}
		List<? extends Certificate> certs = cp.getCertificates();
		LOG.info("CMS object contains " + certs.size() + " certificates");
		/*
		 * List the Intermediates we received
		 */
		for (Certificate cert : certs) {
			LOG.info("CMS Cert:");
			LOG.info(cert.toString());
		}
		/*
		 * Place certificates into a Collection CertStore
		 */
		CertStoreParameters cparam = new CollectionCertStoreParameters(certs);
		try {
			intermediates = CertStore.getInstance("Collection", cparam, "SUN");
		} catch (InvalidAlgorithmParameterException e) {
			LOG.error("Failed to create CertStore from CMS object", e);
		} catch (NoSuchAlgorithmException e) {
			LOG.error("Failed to create CertStore from CMS object", e);
		} catch (NoSuchProviderException e) {
			LOG.error("Failed to create CertStore from CMS object", e);
		}

	}

	private static class SingletonHelper {
		private static final IntermediateCacheSingleton INSTANCE = new IntermediateCacheSingleton();
	}

	public static IntermediateCacheSingleton getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public CertStore getIntermediates() {
		return intermediates;
	}

}
