package org.keysupport.api.pkix.cache;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.keysupport.api.singletons.HTTPClientSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spy.memcached.MemcachedClient;

public class CacheAndDownloadTest {

	private final static Logger LOG = LoggerFactory.getLogger(CacheAndDownloadTest.class);

	private static final String mdUri = "https://raw.githubusercontent.com/GSA/ficam-playbooks/staging/_fpki/2b_pivcas.md";

	private static String configEndpoint = null;

	public static void getCRLsFromMd() {
		/*
		 * Get and Parse FPKI Playbook markdown, looking for CRLs.
		 */
		HTTPClientSingleton client = HTTPClientSingleton.getInstance();
		URI uri = URI.create(mdUri);
		byte[] data = client.getData(uri);
		LOG.info("File is " + data.length + " bytes in size");
		String md = new String(data, StandardCharsets.UTF_8);
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
		data = client.getData(downloadURI);
		LOG.info("File is " + data.length + " bytes in size");
		long later = System.currentTimeMillis();
		LOG.info("HTTP fetch time: " + (later - now) + "ms");
		/*
		 * Memcached Start
		 */
		System.setProperty("net.spy.verifyAliveOnConnect", "true");
		Integer clusterPort = 11211;
		configEndpoint = System.getenv("MEMCACHED_CNF");
		if (null == configEndpoint) {
			LOG.error("MEMCACHED_CNF Not Set!");
		}
		MemcachedClient mcdClient = null;
		try {
			mcdClient = new MemcachedClient(new InetSocketAddress(configEndpoint, clusterPort));
		} catch (IOException e) {
			LOG.error("Error creating memcached client", e);
		}
		LOG.info("Caching the CRL data we retrieved");
		mcdClient.set(downloadURI.toASCIIString(), 3600, data);
		now = System.currentTimeMillis();
		byte[] data2 = (byte[]) mcdClient.get(downloadURI.toASCIIString());
		LOG.info("Fetching the CRL data we retrieved");
		LOG.info("Cached Data is " + data2.length + " bytes in size");
		later = System.currentTimeMillis();
		LOG.info("Cache fetch time: " + (later - now) + "ms");
		/*
		 * Memcached End
		 */
	}

}
