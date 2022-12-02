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

import javax.security.auth.x500.X500Principal;

import org.keysupport.api.singletons.HTTPClientSingleton;
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
	private final Logger LOG = LoggerFactory.getLogger(IntermediateCacheSingleton.class);

	private final String mdUri = "https://raw.githubusercontent.com/GSA/ficam-playbooks/staging/_fpki/2b_pivcas.md";

	private final String p7Uri = "https://raw.githubusercontent.com/GSA/ficam-playbooks/federalist-pages/_fpki/tools/CACertificatesValidatingToFederalCommonPolicyG2.p7b";

	private CertStore intermediates = null;

	private CertStore intermediatecrls = null;

	private IntermediateCacheSingleton() {
	}

	public void updateIntermediates() {
		/*
		 * Download the CMS object
		 */
		HTTPClientSingleton client = HTTPClientSingleton.getInstance();
		URI uri = URI.create(p7Uri);
		CertPath cp = client.getCms(uri);
		@SuppressWarnings("unchecked")
		List<X509Certificate> certs = (List<X509Certificate>) cp.getCertificates();
		LOG.info("CMS object contains " + certs.size() + " certificates");
		List<X509Certificate> filteredCerts = new ArrayList<>();
		/*
		 * Filter the Intermediates we received
		 */
		for (X509Certificate cert : certs) {
			X500Principal fbcag4 = new X500Principal("CN=Federal Bridge CA G4,OU=FPKI,O=U.S. Government,C=US");
			X500Principal fcpcag2 = new X500Principal("CN=Federal Common Policy CA G2,OU=FPKI,O=U.S. Government,C=US");
			X500Principal subject = cert.getSubjectX500Principal();
			X500Principal issuer = cert.getIssuerX500Principal();
			LOG.info("CMS Cert: " + subject.getName() + " signed by " + issuer.getName());
			/*
			 * Filter out any certificate issued to FCPCAG2, including the FCPCAG2, and;
			 * Filter out any FBCAG4 issued by any other issue except FCPCAG2.
			 */
			if (subject.equals(fcpcag2)) {
				LOG.info("Filtering out " + subject.getName() + " signed by " + issuer.getName());
			} else if (subject.equals(fbcag4)) {
				if (issuer.equals(fcpcag2)) {
					filteredCerts.add(cert);
				} else {
					LOG.info("Filtering out " + subject.getName() + " signed by " + issuer.getName());
				}
			} else {
				filteredCerts.add(cert);
			}
		}
		/*
		 * Place certificates into a Collection CertStore
		 */
		CertStoreParameters cparam = new CollectionCertStoreParameters(filteredCerts);
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

	private class SingletonHelper {
		private static final IntermediateCacheSingleton INSTANCE = new IntermediateCacheSingleton();
	}

	public static IntermediateCacheSingleton getInstance() {
		return SingletonHelper.INSTANCE;
	}

	public CertStore getIntermediates() {
		return intermediates;
	}

}
