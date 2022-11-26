package org.keysupport.api.pkix.cache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spy.memcached.MemcachedClient;

public class CacheAndDownloadTest {

	private final static Logger LOG = LoggerFactory.getLogger(CacheAndDownloadTest.class);

	private static final String p7Uri = "https://raw.githubusercontent.com/GSA/ficam-playbooks/federalist-pages/_fpki/tools/CACertificatesValidatingToFederalCommonPolicyG2.p7b";

	private static final String mdUri = "https://raw.githubusercontent.com/GSA/ficam-playbooks/staging/_fpki/2b_pivcas.md";

	private static String configEndpoint = null;

	/*
	 * TODO:  Centralize our HTTP Client implementation, preferably using the AWS CRT Client, for now Using Native JDK11
	 */
	public static void getP7Current() {
		/*
		 * Create HTTP Client
		 */
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(p7Uri)).build();
		/*
		 * Get and Parse FPKI Playbook Graph CMS, looking for Intermediate CAs.
		 */
		HttpResponse<byte[]> response = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
		} catch (IOException e) {
			LOG.error("Error fetching CMS object", e);
		} catch (InterruptedException e) {
			LOG.error("Error fetching CMS object", e);
		}
		byte[] data = response.body();
		LOG.info("File is " + data.length + " bytes in size");
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		CertificateFactory cf = null;
		try {
			cf = CertificateFactory.getInstance("X.509");
		} catch (CertificateException e) {
			LOG.error("Error parsing CMS object", e);
		}
		CertPath cp = null;
		try {
			cp = cf.generateCertPath(bais, "PKCS7");
		} catch (CertificateException e) {
			LOG.error("Error parsing CMS object", e);
		}
		List<? extends Certificate> certs = cp.getCertificates();
		LOG.info("File contains " + certs.size() + " certificates");
		/*
		 * List the Intermediates we received
		 */
		for (Certificate cert : certs) { System.out.println(cert); }
	}

	public static void getCRLsFromMd() {
		/*
		 * Create HTTP Client
		 */
		HttpClient client = HttpClient.newHttpClient();
		/*
		 * Get and Parse FPKI Playbook markdown, looking for CRLs.
		 */
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(mdUri)).build();
		HttpResponse<byte[]> response = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
		} catch (IOException e) {
			LOG.error("Error fetching CMS object", e);
		} catch (InterruptedException e) {
			LOG.error("Error fetching CMS object", e);
		}
		byte[] data = response.body();
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
		 * Download first CRL
		 */
		LOG.info("Downloading CRL data for the first URI we retrieved");
		long now = System.currentTimeMillis();
		request = HttpRequest.newBuilder().uri(crlUris.get(0)).build();
		response = null;
		data = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
		} catch (IOException e) {
			LOG.error("Error fetching CMS object", e);
		} catch (InterruptedException e) {
			LOG.error("Error fetching CMS object", e);
		}
		data = response.body();
		LOG.info("File is " + data.length + " bytes in size");
		long later = System.currentTimeMillis();
		LOG.info("HTTP fetch time: " + (later - now) + "ms");
		/*
		 * Memcached Start
		 */
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
		mcdClient.set("crl0", 3600, data);
		now = System.currentTimeMillis();
		byte[] data2 = (byte[]) mcdClient.get("crl0");
		LOG.info("Fetching the CRL data we retrieved");
		LOG.info("Cached Data is " + data2.length + " bytes in size");
		later = System.currentTimeMillis();
		LOG.info("Cache fetch time: " + (later - now) + "ms");
		/*
		 * Memcached End
		 */
	}

}
