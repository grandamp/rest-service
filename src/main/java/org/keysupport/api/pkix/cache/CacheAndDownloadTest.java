package org.keysupport.api.pkix.cache;

import java.net.URI;
import java.security.cert.CRLException;
import java.security.cert.X509CRL;
import java.util.List;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.keysupport.api.singletons.HTTPClientSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheAndDownloadTest {

	private final static Logger LOG = LoggerFactory.getLogger(CacheAndDownloadTest.class);

	private static final String mdUri = "https://raw.githubusercontent.com/GSA/ficam-playbooks/staging/_fpki/2b_pivcas.md";

	public static void getCRLsFromMd() {
		/*
		 * Get and Parse FPKI Playbook markdown, looking for CRLs.
		 */
		HTTPClientSingleton client = HTTPClientSingleton.getInstance();
		URI uri = URI.create(mdUri);
		String md = client.getText(uri);
		LOG.info("File is " + md.getBytes().length + " bytes in size");
		Parser mdParser = Parser.builder().build();
		Node fpkiPlaybookDocument = mdParser.parse(md);
		HttpCrlParsingVisitor visitor = new HttpCrlParsingVisitor();
		fpkiPlaybookDocument.accept(visitor);
		List<URI> crlUris = visitor.getCrlUris();
		if (null == crlUris) {
			LOG.error("Failed to parse HTTP CRLs from FPKI Playbook!");
		} else {
			for (URI currentUri : crlUris) {
				LOG.info("Parsed CRL URI: " + currentUri.toASCIIString());
			}
		}
		/*
		 * Download 32nd CRL (http://pki.treasury.gov/DHS_CA2.crl)
		 */
		URI downloadURI = crlUris.get(31);
		LOG.info("Downloading CRL data: " + downloadURI.toASCIIString());
		long now = System.currentTimeMillis();
		X509CRL currentCrl = client.getCrl(downloadURI);
		byte[] data = null;
		try {
			data = currentCrl.getEncoded();
		} catch (CRLException e) {
			LOG.error("Failed to convert CRL object to byte[]", e);
		}
		LOG.info("File is " + data.length + " bytes in size");
		long later = System.currentTimeMillis();
		LOG.info("HTTP fetch time: " + (later - now) + "ms");
		/*
		 * Memcached Start
		 * 
		 * Clear our cache for local testing.
		 */
		ElasticacheClient mcClient = new ElasticacheClient(System.getenv("MEMCACHED_CNF"));
		mcClient.clear();
		mcClient.close();
		/*
		 * Memcached End
		 */
	}

}
