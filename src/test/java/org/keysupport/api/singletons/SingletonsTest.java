package org.keysupport.api.singletons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.CertPath;
import java.security.cert.CertStore;
import java.security.cert.CertificateFactory;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keysupport.api.pojo.vss.ValidationPolicy;

import com.sun.net.httpserver.HttpServer;

class SingletonsTest {

	private static final String DEFAULT_POLICY_OID = "1.3.6.1.5.5.7.19.1";
	private static final String TEST_CERT_POLICY_OID = "2.16.840.1.101.3.2.1.3.13";

	static {
		if (null == Security.getProvider(BouncyCastleFipsProvider.PROVIDER_NAME)) {
			Security.addProvider(new BouncyCastleFipsProvider());
		}
	}

	@AfterEach
	void cleanup() {
		IntermediateCacheSingleton.getInstance().stopPeriodicRevalidation();
		System.clearProperty("com.sun.security.enableCRLDP");
	}

	@Test
	void httpClientSingleton_returnsSameInstanceHandlesHttpErrorsAndTlsFailures() throws Exception {
		HTTPClientSingleton client = HTTPClientSingleton.getInstance();
		assertSame(client, HTTPClientSingleton.getInstance());
		assertNotNull(client.getClient());
		assertNull(client.getData(null, "text/plain"));

		java.util.concurrent.atomic.AtomicReference<String> seenUserAgent = new java.util.concurrent.atomic.AtomicReference<>();
		HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/ok", ex -> {
			seenUserAgent.set(ex.getRequestHeaders().getFirst("User-Agent"));
			byte[] body = "hello-vss".getBytes(StandardCharsets.UTF_8);
			ex.sendResponseHeaders(200, body.length);
			try (OutputStream os = ex.getResponseBody()) {
				os.write(body);
			}
		});
		server.createContext("/notfound", ex -> {
			byte[] body = "missing".getBytes(StandardCharsets.UTF_8);
			ex.sendResponseHeaders(404, body.length);
			try (OutputStream os = ex.getResponseBody()) {
				os.write(body);
			}
		});
		server.createContext("/oversized", ex -> {
			byte[] body = new byte[1_000_000];
			ex.sendResponseHeaders(200, body.length);
			try (OutputStream os = ex.getResponseBody()) {
				os.write(body);
			}
		});
		server.createContext("/badcms", ex -> {
			byte[] body = "not-pkcs7".getBytes(StandardCharsets.UTF_8);
			ex.sendResponseHeaders(200, body.length);
			try (OutputStream os = ex.getResponseBody()) {
				os.write(body);
			}
		});
		server.start();

		try {
			String base = "http://127.0.0.1:" + server.getAddress().getPort();
			assertEquals("hello-vss", client.getText(URI.create(base + "/ok")));
			assertEquals(org.keysupport.api.config.ConfigurationSwagger.resolveVersion(), seenUserAgent.get());
			assertNull(client.getText(URI.create(base + "/notfound")));
			assertNull(client.getData(URI.create(base + "/oversized"), "application/octet-stream"));
			assertNull(client.getCms(URI.create(base + "/badcms")));
			assertNull(client.getCms(URI.create(base + "/notfound")));
		} finally {
			server.stop(0);
		}

		// Verify TLS handshake failures are reported cleanly without NPE or unhandled exceptions
		try (ServerSocket rawSocket = new ServerSocket(0)) {
			int port = rawSocket.getLocalPort();
			ExecutorService exec = Executors.newSingleThreadExecutor();
			exec.submit(() -> {
				try (var accepted = rawSocket.accept()) {
					// Immediately write plain HTTP text to break TLS handshake
					accepted.getOutputStream().write("NOT_TLS\r\n".getBytes(StandardCharsets.US_ASCII));
					accepted.getOutputStream().flush();
				} catch (IOException ignored) {
				}
			});
			try {
				assertNull(client.getText(URI.create("https://127.0.0.1:" + port + "/tls")));
			} finally {
				exec.shutdownNow();
			}
		}

		// Also verify untrusted SSLServerSocket handshake failure (SSLHandshakeException)
		SSLContext serverCtx = SSLContext.getDefault();
		SSLServerSocketFactory ssf = serverCtx.getServerSocketFactory();
		try (SSLServerSocket tlsServer = (SSLServerSocket) ssf.createServerSocket(0)) {
			int tlsPort = tlsServer.getLocalPort();
			ExecutorService exec = Executors.newSingleThreadExecutor();
			exec.submit(() -> {
				try (SSLSocket s = (SSLSocket) tlsServer.accept()) {
					s.startHandshake();
				} catch (IOException ignored) {
				}
			});
			try {
				assertNull(client.getText(URI.create("https://127.0.0.1:" + tlsPort + "/untrusted")));
			} finally {
				exec.shutdownNow();
			}
		}
	}

	@Test
	void policiesAreFinalAndIntermediateCacheValidatesOutOfOrderAndEvictsInvalid() throws Exception {
		ValidationPoliciesSingleton policies = ValidationPoliciesSingleton.getInstance();
		assertSame(policies, ValidationPoliciesSingleton.getInstance());
		IntermediateCacheSingleton cache = IntermediateCacheSingleton.getInstance();
		assertSame(cache, IntermediateCacheSingleton.getInstance());

		// Build synthetic PKI under BCFIPS
		KeyPair rootKp = genKeyPair();
		KeyPair ca1Kp = genKeyPair();
		KeyPair ca2Kp = genKeyPair();
		KeyPair ca3Kp = genKeyPair();
		KeyPair shortLivedKp = genKeyPair();
		KeyPair childShortKp = genKeyPair();
		KeyPair revokedKp = genKeyPair();
		KeyPair expiredKp = genKeyPair();
		KeyPair eeKp = genKeyPair();
		KeyPair rogueRootKp = genKeyPair();
		KeyPair rogueCaKp = genKeyPair();

		Date now = new Date();
		Date notBefore = new Date(now.getTime() - 60_000L);
		Date longAfter = new Date(now.getTime() + 86_400_000L);

		X509Certificate rootTa = buildCert(
				"CN=Test FPKI Root TA", rootKp,
				"CN=Test FPKI Root TA", rootKp,
				BigInteger.valueOf(1), notBefore, longAfter,
				true, null, null);

		X509Certificate rogueRoot = buildCert(
				"CN=Rogue Root", rogueRootKp,
				"CN=Rogue Root", rogueRootKp,
				BigInteger.valueOf(99), notBefore, longAfter,
				true, null, null);

		HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		String base = "http://127.0.0.1:" + server.getAddress().getPort();
		String rootCrlUrl = base + "/root.crl";
		String ca1CrlUrl = base + "/ca1.crl";
		String ca2CrlUrl = base + "/ca2.crl";
		String shortCrlUrl = base + "/short.crl";

		BigInteger revokedSerial = BigInteger.valueOf(50);
		byte[] rootCrlBytes = new JcaX509CRLConverter().setProvider(BouncyCastleFipsProvider.PROVIDER_NAME)
				.getCRL(buildCrl("CN=Test FPKI Root TA", rootKp, now, longAfter, revokedSerial)).getEncoded();
		byte[] ca1CrlBytes = new JcaX509CRLConverter().setProvider(BouncyCastleFipsProvider.PROVIDER_NAME)
				.getCRL(buildCrl("CN=Intermediate CA 1", ca1Kp, now, longAfter, null)).getEncoded();
		byte[] ca2CrlBytes = new JcaX509CRLConverter().setProvider(BouncyCastleFipsProvider.PROVIDER_NAME)
				.getCRL(buildCrl("CN=Intermediate CA 2", ca2Kp, now, longAfter, null)).getEncoded();
		byte[] shortCrlBytes = new JcaX509CRLConverter().setProvider(BouncyCastleFipsProvider.PROVIDER_NAME)
				.getCRL(buildCrl("CN=Short Lived CA", shortLivedKp, now, longAfter, null)).getEncoded();

		X509Certificate ca1 = buildCert(
				"CN=Intermediate CA 1", ca1Kp,
				"CN=Test FPKI Root TA", rootKp,
				BigInteger.valueOf(10), notBefore, longAfter,
				true, rootCrlUrl, TEST_CERT_POLICY_OID);

		X509Certificate ca2 = buildCert(
				"CN=Intermediate CA 2", ca2Kp,
				"CN=Intermediate CA 1", ca1Kp,
				BigInteger.valueOf(11), notBefore, longAfter,
				true, ca1CrlUrl, TEST_CERT_POLICY_OID);

		X509Certificate ca3 = buildCert(
				"CN=Intermediate CA 3", ca3Kp,
				"CN=Intermediate CA 2", ca2Kp,
				BigInteger.valueOf(12), notBefore, longAfter,
				true, ca2CrlUrl, TEST_CERT_POLICY_OID);

		Date shortExpiry = new Date(now.getTime() + 2_500L);
		X509Certificate shortLivedCa = buildCert(
				"CN=Short Lived CA", shortLivedKp,
				"CN=Test FPKI Root TA", rootKp,
				BigInteger.valueOf(20), notBefore, shortExpiry,
				true, rootCrlUrl, TEST_CERT_POLICY_OID);

		X509Certificate childOfShortLived = buildCert(
				"CN=Child Of Short Lived CA", childShortKp,
				"CN=Short Lived CA", shortLivedKp,
				BigInteger.valueOf(21), notBefore, longAfter,
				true, shortCrlUrl, TEST_CERT_POLICY_OID);

		X509Certificate revokedCa = buildCert(
				"CN=Revoked CA", revokedKp,
				"CN=Test FPKI Root TA", rootKp,
				revokedSerial, notBefore, longAfter,
				true, rootCrlUrl, TEST_CERT_POLICY_OID);

		X509Certificate expiredCa = buildCert(
				"CN=Expired CA", expiredKp,
				"CN=Test FPKI Root TA", rootKp,
				BigInteger.valueOf(30),
				new Date(now.getTime() - 120_000L),
				new Date(now.getTime() - 5_000L),
				true, rootCrlUrl, TEST_CERT_POLICY_OID);

		X509Certificate nonCaEe = buildCert(
				"CN=End Entity Not CA", eeKp,
				"CN=Test FPKI Root TA", rootKp,
				BigInteger.valueOf(40), notBefore, longAfter,
				false, rootCrlUrl, TEST_CERT_POLICY_OID);

		X509Certificate rogueCa = buildCert(
				"CN=Rogue CA", rogueCaKp,
				"CN=Rogue Root", rogueRootKp,
				BigInteger.valueOf(60), notBefore, longAfter,
				true, null, TEST_CERT_POLICY_OID);

		String rootTaB64 = Base64.getEncoder().encodeToString(rootTa.getEncoded());
		String policiesJson = """
				{
				  "validationPolicies": [
				    {
				      "validationPolicyId": "1.3.6.1.5.5.7.19.1",
				      "validationPolicyName": "default",
				      "validationPolicyDescription": "Default Test Policy",
				      "trustAnchors": [
				        {
				          "X509SubjectName": "CN=Test FPKI Root TA",
				          "x509Certificate": "%s"
				        }
				      ],
				      "userPolicySet": ["2.5.29.32.0"],
				      "inhibitPolicyMapping": false,
				      "requireExplicitPolicy": true,
				      "inhibitAnyPolicy": true
				    }
				  ]
				}
				""".formatted(rootTaB64);

		// Intentionally out-of-order: children before parents!
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		CertPath outOfOrderBundle = cf.generateCertPath(List.of(
				childOfShortLived,
				ca2,
				expiredCa,
				nonCaEe,
				rogueCa,
				revokedCa,
				ca1,
				shortLivedCa,
				ca1 // duplicate
		));
		byte[] p7bBytes = outOfOrderBundle.getEncoded("PKCS7");

		server.createContext("/policies.json", ex -> {
			byte[] body = policiesJson.getBytes(StandardCharsets.UTF_8);
			ex.sendResponseHeaders(200, body.length);
			try (OutputStream os = ex.getResponseBody()) {
				os.write(body);
			}
		});
		server.createContext("/bad.json", ex -> {
			byte[] body = "{not-json".getBytes(StandardCharsets.UTF_8);
			ex.sendResponseHeaders(200, body.length);
			try (OutputStream os = ex.getResponseBody()) {
				os.write(body);
			}
		});
		server.createContext("/missing", ex -> ex.sendResponseHeaders(404, -1));
		server.createContext("/intermediates.p7b", ex -> {
			ex.sendResponseHeaders(200, p7bBytes.length);
			try (OutputStream os = ex.getResponseBody()) {
				os.write(p7bBytes);
			}
		});
		server.createContext("/root.crl", ex -> {
			ex.getResponseHeaders().add("Content-Type", "application/pkix-crl");
			ex.sendResponseHeaders(200, rootCrlBytes.length);
			try (OutputStream os = ex.getResponseBody()) {
				os.write(rootCrlBytes);
			}
		});
		server.createContext("/ca1.crl", ex -> {
			ex.getResponseHeaders().add("Content-Type", "application/pkix-crl");
			ex.sendResponseHeaders(200, ca1CrlBytes.length);
			try (OutputStream os = ex.getResponseBody()) {
				os.write(ca1CrlBytes);
			}
		});
		server.createContext("/ca2.crl", ex -> {
			ex.getResponseHeaders().add("Content-Type", "application/pkix-crl");
			ex.sendResponseHeaders(200, ca2CrlBytes.length);
			try (OutputStream os = ex.getResponseBody()) {
				os.write(ca2CrlBytes);
			}
		});
		server.createContext("/short.crl", ex -> {
			ex.getResponseHeaders().add("Content-Type", "application/pkix-crl");
			ex.sendResponseHeaders(200, shortCrlBytes.length);
			try (OutputStream os = ex.getResponseBody()) {
				os.write(shortCrlBytes);
			}
		});
		server.start();

		try {
			// Failed initial policy loads throw IllegalStateException (instead of System.exit)
			assertThrows(IllegalStateException.class, () -> policies.updateValidationPolicies(base + "/missing"));
			assertThrows(IllegalStateException.class, () -> policies.updateValidationPolicies(base + "/bad.json"));

			// Successful startup policy load
			policies.updateValidationPolicies(base + "/policies.json");
			policies.setMaxPathLen(7);
			policies.setRevocationEnabled(true);
			policies.setRevocationEeOnly(true);
			policies.setCrlEnabled(true);
			policies.setOcspEnabled(false);
			policies.setCertPathProvider("SUN");
			policies.setCertPathAlgorithm("PKIX");
			policies.setIntermediateValidateOnLoad(true);
			policies.setIntermediateRevocationEnabled(true);
			policies.lockConfiguration();
			System.setProperty("com.sun.security.enableCRLDP", "true");

			// Policies and config are final after startup
			assertThrows(IllegalStateException.class, () -> policies.updateValidationPolicies(base + "/policies.json"));
			assertThrows(IllegalStateException.class, () -> policies.setMaxPathLen(3));
			assertThrows(IllegalStateException.class, () -> policies.setRevocationEnabled(false));

			Set<TrustAnchor> anchors = policies.getTrustAnchors(DEFAULT_POLICY_OID);
			assertNotNull(anchors);
			assertEquals(1, anchors.size());
			assertThrows(UnsupportedOperationException.class, () -> anchors.clear());
			assertThrows(UnsupportedOperationException.class,
					() -> policies.getValidationPolicies().validationPolicies.clear());
			ValidationPolicy defaultPolicy = policies.getDefaultPolicy();
			assertNotNull(defaultPolicy);
			assertEquals(DEFAULT_POLICY_OID, defaultPolicy.validationPolicyId);

			// Load out-of-order P7B: CA1, CA2, ShortLivedCA, ChildOfShortLivedCA are accepted;
			// ExpiredCA, NonCA, RogueCA, RevokedCA, and duplicate CA1 are rejected.
			cache.updateIntermediates(base + "/intermediates.p7b");
			CertStore store = cache.getIntermediates();
			assertNotNull(store);
			Collection<?> initialCerts = store.getCertificates(null);
			assertEquals(4, initialCerts.size());
			assertTrue(initialCerts.contains(ca1));
			assertTrue(initialCerts.contains(ca2));
			assertTrue(initialCerts.contains(shortLivedCa));
			assertTrue(initialCerts.contains(childOfShortLived));
			assertFalse(initialCerts.contains(expiredCa));
			assertFalse(initialCerts.contains(nonCaEe));
			assertFalse(initialCerts.contains(rogueCa));
			assertFalse(initialCerts.contains(revokedCa));

			// Single additions: only valid CA under default policy is admitted
			assertTrue(cache.addIntermediate(ca3));
			assertFalse(cache.addIntermediate(ca3)); // duplicate
			assertFalse(cache.addIntermediate(rogueCa));
			assertFalse(cache.addIntermediate(nonCaEe));
			assertFalse(cache.addIntermediate(expiredCa));
			assertEquals(5, cache.getIntermediates().getCertificates(null).size());

			// Wait for ShortLivedCA to expire, then verify revalidateCache evicts BOTH
			// ShortLivedCA and its orphaned child ChildOfShortLivedCA while retaining CA1/CA2/CA3!
			Thread.sleep(2_700L);
			int evicted = cache.revalidateCache();
			assertEquals(2, evicted);
			Collection<?> survivors = cache.getIntermediates().getCertificates(null);
			assertEquals(3, survivors.size());
			assertTrue(survivors.contains(ca1));
			assertTrue(survivors.contains(ca2));
			assertTrue(survivors.contains(ca3));
			assertFalse(survivors.contains(shortLivedCa));
			assertFalse(survivors.contains(childOfShortLived));
		} finally {
			server.stop(0);
		}
	}

	private static KeyPair genKeyPair() throws Exception {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", BouncyCastleFipsProvider.PROVIDER_NAME);
		kpg.initialize(2048);
		return kpg.generateKeyPair();
	}

	private static X509Certificate buildCert(
			String subjectDn,
			KeyPair subjectKp,
			String issuerDn,
			KeyPair issuerKp,
			BigInteger serial,
			Date notBefore,
			Date notAfter,
			boolean isCa,
			String crlUrl,
			String policyOid) throws Exception {
		X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
				new X500Name(issuerDn),
				serial,
				notBefore,
				notAfter,
				new X500Name(subjectDn),
				subjectKp.getPublic());
		JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
		SubjectKeyIdentifier ski = extUtils.createSubjectKeyIdentifier(subjectKp.getPublic());
		AuthorityKeyIdentifier aki = extUtils.createAuthorityKeyIdentifier(issuerKp.getPublic());
		builder.addExtension(Extension.subjectKeyIdentifier, false, ski);
		builder.addExtension(Extension.authorityKeyIdentifier, false, aki);
		builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(isCa));
		if (isCa) {
			builder.addExtension(Extension.keyUsage, true,
					new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));
		} else {
			builder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature));
		}
		if (null != crlUrl) {
			DistributionPointName dpName = new DistributionPointName(
					new GeneralNames(new GeneralName(GeneralName.uniformResourceIdentifier, crlUrl)));
			DistributionPoint dp = new DistributionPoint(dpName, null, null);
			builder.addExtension(Extension.cRLDistributionPoints, false, new CRLDistPoint(new DistributionPoint[] { dp }));
		}
		if (null != policyOid) {
			ASN1EncodableVector policies = new ASN1EncodableVector();
			policies.add(new PolicyInformation(new org.bouncycastle.asn1.ASN1ObjectIdentifier(policyOid)));
			builder.addExtension(Extension.certificatePolicies, false, new DERSequence(policies));
		}

		ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
				.setProvider(BouncyCastleFipsProvider.PROVIDER_NAME)
				.build(issuerKp.getPrivate());
		return new JcaX509CertificateConverter()
				.setProvider(BouncyCastleFipsProvider.PROVIDER_NAME)
				.getCertificate(builder.build(signer));
	}

	private static X509CRLHolder buildCrl(
			String issuerDn,
			KeyPair issuerKp,
			Date thisUpdate,
			Date nextUpdate,
			BigInteger revokedSerial) throws Exception {
		X509v2CRLBuilder crlBuilder = new X509v2CRLBuilder(new X500Name(issuerDn), thisUpdate);
		crlBuilder.setNextUpdate(nextUpdate);
		if (null != revokedSerial) {
			crlBuilder.addCRLEntry(revokedSerial, thisUpdate, CRLReason.keyCompromise);
		}
		ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
				.setProvider(BouncyCastleFipsProvider.PROVIDER_NAME)
				.build(issuerKp.getPrivate());
		return crlBuilder.build(signer);
	}
}
