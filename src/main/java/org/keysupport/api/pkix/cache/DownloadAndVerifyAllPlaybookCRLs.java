package org.keysupport.api.pkix.cache;

import java.io.IOException;
import java.net.URI;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertStoreParameters;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.keysupport.api.pkix.X509Util;
import org.keysupport.api.pkix.cache.pojo.CachedX509Crl;
import org.keysupport.api.singletons.HTTPClientSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/*
 * Intended for a local test, by running via command line or IDE
 *
 * TODO: Instument https://aws-otel.github.io/docs/getting-started/java-sdk/trace-auto-instr
 *
 */
public class DownloadAndVerifyAllPlaybookCRLs {

	private final static Logger LOG = LoggerFactory.getLogger(DownloadAndVerifyAllPlaybookCRLs.class);

	private static final String p7Uri = "https://raw.githubusercontent.com/GSA/ficam-playbooks/federalist-pages/_fpki/tools/CACertificatesValidatingToFederalCommonPolicyG2.p7b";

	private static final String mdUri = "https://raw.githubusercontent.com/GSA/ficam-playbooks/staging/_fpki/2b_pivcas.md";

	private static CertStore intermediates = null;

	private static int fpkiRevokedCertificates = 0;

	private static int fpkiCrlBytes = 0;

	public static void main(String args[]) {
		
		HTTPClientSingleton client = HTTPClientSingleton.getInstance();
		/*
		 * Get playbook intermediates
		 */
		URI uri = URI.create(p7Uri);
		CertPath cp = client.getCms(uri);
		int lenBytes = 0;
		try {
			lenBytes = cp.getEncoded().length;
		} catch (CertificateEncodingException e) {
			LOG.error("Failed to convert CMS object to byte[]", e);
		}
		LOG.info("CMS object is " + lenBytes + " bytes in size");
		/*
		 * Parse the CMS object
		 */
		@SuppressWarnings("unchecked")
		List<X509Certificate> certs = (List<X509Certificate>) cp.getCertificates();
		LOG.info("CMS object contains " + certs.size() + " certificates");
		/*
		 * List the Intermediates we received
		 */
		for (X509Certificate cert : certs) {
			LOG.info("CMS Cert: " + cert.getSubjectX500Principal().getName());
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
		/*
		 * Get and Parse FPKI Playbook markdown, looking for CRLs.
		 */
		uri = URI.create(mdUri);
		String md = client.getText(uri);
		LOG.info("File is " + md.getBytes().length + " bytes in size");
		Parser mdParser = Parser.builder().build();
		Node fpkiPlaybookDocument = mdParser.parse(md);
		HttpCrlParsingVisitor visitor = new HttpCrlParsingVisitor();
		fpkiPlaybookDocument.accept(visitor);
		List<URI> crlUris = visitor.getCrlUris();
		List<CachedX509Crl> cachedCrls = new ArrayList<CachedX509Crl>();
		if (null == crlUris) {
			LOG.error("Failed to parse HTTP CRLs from FPKI Playbook!");
		} else {
			for (URI currentUri : crlUris) {
				CachedX509Crl cCrl = new CachedX509Crl();
				cCrl.uri = currentUri;
				/*
				 * Render CRL and print information
				 */
				LOG.info("Downloading CRL data: " + currentUri.toASCIIString());
				long now = System.currentTimeMillis();
				X509CRL currentCrl = client.getCrl(currentUri);
				long later = System.currentTimeMillis();
				LOG.info("HTTP fetch time: " + (later - now) + "ms");
				if (null == currentCrl) {
					LOG.error("Failed to render CRL");
					LOG.error("We should reject this CRL: " + currentUri.toASCIIString());
				} else {
					lenBytes = 0;
					try {
						lenBytes = currentCrl.getEncoded().length;
					} catch (CRLException e) {
						LOG.error("Failed to convert CRL object to byte[]", e);
					}
					fpkiCrlBytes = fpkiCrlBytes + lenBytes;
					LOG.info("File is " + lenBytes + " bytes in size");
					cCrl.issuerName = currentCrl.getIssuerX500Principal().getEncoded();
					LOG.info("CRL Issuer: " + currentCrl.getIssuerX500Principal().getName());
					
					cCrl.thisUpdate = X509Util.ISO8601DateString(currentCrl.getThisUpdate());
					LOG.info("CRL thisUpdate: " + cCrl.thisUpdate);
					cCrl.nextUpdate = X509Util.ISO8601DateString(currentCrl.getNextUpdate());
					LOG.info("CRL nextUpdate: " + cCrl.nextUpdate);
					Set<? extends X509CRLEntry> entries = currentCrl.getRevokedCertificates();
					if (null == entries) {
						LOG.info("CRL Entries: 0");
						cCrl.numEntries = 0;
					} else {
						LOG.info("CRL Entries: " + entries.size());
						cCrl.numEntries = entries.size();
						fpkiRevokedCertificates = fpkiRevokedCertificates + entries.size();
					}
					byte[] crlAkiBytes = currentCrl.getExtensionValue(Extension.authorityKeyIdentifier.toString());
					if (null == crlAkiBytes) {
						LOG.error("CRL missing authorityKeyIdentifier extension");
					}
					/*
					 * Verify we have a signer for the CRL, and verify the CRL's Signature
					 */
					AuthorityKeyIdentifier crlAki = AuthorityKeyIdentifier
							.getInstance(ASN1OctetString.getInstance(crlAkiBytes).getOctets());
					X509CertSelector skiSelector = new X509CertSelector();
					SubjectKeyIdentifier skiVal = new SubjectKeyIdentifier(crlAki.getKeyIdentifier());
					byte[] skiBytes = null;
					try {
						skiBytes = skiVal.getEncoded();
					} catch (IOException e) {
						LOG.error("Unable to decode keyIdentifier from authorityKeyIdentifier", e);
					}
					if (null == skiBytes) {
						LOG.error("We should reject this CRL: " + currentUri.toASCIIString());
					} else {
						cCrl.authorityKeyIdentifier = skiBytes;
						skiSelector.setSubjectKeyIdentifier(skiBytes);
						Collection<? extends Certificate> certsFromSelector = null;
						try {
							certsFromSelector = intermediates.getCertificates(skiSelector);
						} catch (CertStoreException e) {
							LOG.error("Error searching intermediate store for issuer", e);
						}
						if (certsFromSelector != null && certsFromSelector.size() > 0) {
							X509Certificate signingCA = (X509Certificate) certsFromSelector.toArray()[0];
							LOG.info("Found issuer in our intermediate store: " + signingCA.getSubjectX500Principal().getName());
							try {
								currentCrl.verify(signingCA.getPublicKey());
								cachedCrls.add(cCrl);
								LOG.info("CRL Signature verified: " + currentUri.toASCIIString());
							} catch (InvalidKeyException e) {
								LOG.error("Error verifying CRL", e);
								LOG.error("We should reject this CRL: " + currentUri.toASCIIString());
							} catch (CRLException e) {
								LOG.error("Error verifying CRL", e);
								LOG.error("We should reject this CRL: " + currentUri.toASCIIString());
							} catch (NoSuchAlgorithmException e) {
								LOG.error("Error verifying CRL", e);
								LOG.error("We should reject this CRL: " + currentUri.toASCIIString());
							} catch (NoSuchProviderException e) {
								LOG.error("Error verifying CRL", e);
								LOG.error("We should reject this CRL: " + currentUri.toASCIIString());
							} catch (SignatureException e) {
								LOG.error("Error verifying CRL", e);
								LOG.error("We should reject this CRL: " + currentUri.toASCIIString());
							}
						}
					}
				}
			}
		}
		LOG.info("Total number of revoked certificates: " + fpkiRevokedCertificates);
		LOG.info("Total number of CRL Bytes: " + fpkiCrlBytes);
		ObjectMapper mapper = new ObjectMapper();
		String cachedCrlsJson = null;
		try {
			cachedCrlsJson = mapper.writeValueAsString(cachedCrls);
		} catch (JsonProcessingException e) {
			LOG.error("Error converting POJO to JSON String", e);
		}
		LOG.info(cachedCrlsJson);
	}
	
}
