package org.keysupport.api.jetty;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jetty.JettyServerCustomizer;
import org.springframework.boot.jetty.servlet.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

class JettyCustomizerTest {

	private static final String[] TLS_PROPS = {
			"jdk.tls.server.disableExtensions",
			"jdk.tls.client.disableExtensions",
			"jdk.tls.namedGroups",
			"jdk.tls.server.SignatureSchemes",
			"jdk.tls.client.SignatureSchemes",
			"jdk.tls.server.sessionTicketTimeout",
			"javax.net.ssl.sessionCacheSize"
	};

	@AfterEach
	void clearTlsProperties() {
		for (String prop : TLS_PROPS) {
			System.clearProperty(prop);
		}
	}

	@Test
	void jettyTlsCustomizer_setsCommaDelimitedPqcAndTls13Properties() {
		JettyCustomizer customizerBean = new JettyCustomizer();
		WebServerFactoryCustomizer<JettyServletWebServerFactory> webCustomizer = customizerBean.jettyTlsCustomizer();

		JettyServletWebServerFactory factory = new JettyServletWebServerFactory(0);
		webCustomizer.customize(factory);

		SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
		Server server = new Server();
		ServerConnector sslConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, "http/1.1"));
		server.addConnector(sslConnector);

		for (JettyServerCustomizer sc : factory.getServerCustomizers()) {
			sc.customize(server);
		}

		assertArrayEquals(new String[] { "TLSv1.3" }, sslContextFactory.getIncludeProtocols());
		assertArrayEquals(new String[] {
				"TLS_AES_256_GCM_SHA384",
				"TLS_AES_128_GCM_SHA256"
		}, sslContextFactory.getIncludeCipherSuites());
		assertEquals(0, sslContextFactory.getExcludeProtocols().length);
		assertEquals(0, sslContextFactory.getExcludeCipherSuites().length);

		assertEquals("early_data", System.getProperty("jdk.tls.server.disableExtensions"));
		assertEquals("early_data", System.getProperty("jdk.tls.client.disableExtensions"));
		assertEquals("SecP384r1MLKEM1024,SecP256r1MLKEM768,X25519MLKEM768,secp384r1",
				System.getProperty("jdk.tls.namedGroups"));
		assertEquals("rsa_pss_rsae_sha384,rsa_pss_rsae_sha512,rsa_pss_pss_sha384,rsa_pss_pss_sha512",
				System.getProperty("jdk.tls.server.SignatureSchemes"));
		String clientSchemes = System.getProperty("jdk.tls.client.SignatureSchemes");
		assertTrue(clientSchemes.contains("rsa_pss_rsae_sha384,rsa_pss_rsae_sha512"));
		assertTrue(clientSchemes.contains("ecdsa_secp384r1_sha384,ecdsa_secp521r1_sha512"));
		assertFalse(clientSchemes.contains("sha256rsa"));
		assertEquals("300", System.getProperty("jdk.tls.server.sessionTicketTimeout"));
		assertEquals("0", System.getProperty("javax.net.ssl.sessionCacheSize"));
	}

	@Test
	void jettyTlsCustomizer_setsGlobalPqcPropertiesEvenOnHttpOnlyConnectorAndHandshakesTls13() throws Exception {
		JettyCustomizer customizerBean = new JettyCustomizer();
		assertEquals("SecP384r1MLKEM1024,SecP256r1MLKEM768,X25519MLKEM768,secp384r1",
				System.getProperty("jdk.tls.namedGroups"));

		if (Security.getProvider(BouncyCastleFipsProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleFipsProvider());
		}
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(3072);
		KeyPair kp = kpg.generateKeyPair();

		JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
				new X500Name("CN=localhost"),
				BigInteger.ONE,
				new Date(System.currentTimeMillis() - 60_000L),
				new Date(System.currentTimeMillis() + 86_400_000L),
				new X500Name("CN=localhost"),
				kp.getPublic());
		ContentSigner signer = new JcaContentSignerBuilder("SHA384withRSA")
				.setProvider(BouncyCastleFipsProvider.PROVIDER_NAME)
				.build(kp.getPrivate());
		X509Certificate cert = new JcaX509CertificateConverter()
				.setProvider(BouncyCastleFipsProvider.PROVIDER_NAME)
				.getCertificate(builder.build(signer));

		char[] pass = "changeit".toCharArray();
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(null, null);
		ks.setKeyEntry("server", kp.getPrivate(), pass, new Certificate[] { cert });

		SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
		sslContextFactory.setKeyStore(ks);
		sslContextFactory.setKeyStorePassword(new String(pass));

		JettyServletWebServerFactory factory = new JettyServletWebServerFactory(0);
		customizerBean.jettyTlsCustomizer().customize(factory);

		Server server = new Server();
		ServerConnector connector = new ServerConnector(server,
				new SslConnectionFactory(sslContextFactory, "http/1.1"),
				new HttpConnectionFactory());
		connector.setHost("127.0.0.1");
		connector.setPort(0);
		server.addConnector(connector);
		for (JettyServerCustomizer sc : factory.getServerCustomizers()) {
			sc.customize(server);
		}

		server.start();
		try {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ks);
			SSLContext clientCtx = SSLContext.getInstance("TLSv1.3");
			clientCtx.init(null, tmf.getTrustManagers(), null);

			try (SSLSocket socket = (SSLSocket) clientCtx.getSocketFactory()
					.createSocket("127.0.0.1", connector.getLocalPort())) {
				socket.startHandshake();
				assertEquals("TLSv1.3", socket.getSession().getProtocol());
				assertEquals("TLS_AES_256_GCM_SHA384", socket.getSession().getCipherSuite());
			}
		} finally {
			server.stop();
		}
	}
}
