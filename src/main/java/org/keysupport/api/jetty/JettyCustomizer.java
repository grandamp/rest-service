package org.keysupport.api.jetty;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.boot.jetty.servlet.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JettyCustomizer {

	/*
	 * Only support TLS v1.3 to encourage use of ML-KEM
	 */
	private static final String[] TLS_SERVER_PROTOCOLS = {
			"TLSv1.3"
	};

	private static final String[] TLS_SERVER_CIPHERSUITES = {
			"TLS_AES_256_GCM_SHA384",
			"TLS_AES_128_GCM_SHA256"
	};

	/*
	 * Ensure the `early_data` extension is disabled as suggested in RFC 9151
	 */
	private static final String TLS_DISABLE_EXTENSIONS = "early_data";

	/*
	 * Set named groups to use Hybrid ML-KEM.
	 *
	 * Include `secp384r1` per FIPS / RFC 9151 alignment while ML-KEM support rolls out,
	 * and `X25519MLKEM768` only for consumer browser PQC interoperability (never bare X25519).
	 */
	private static final String TLS_NAMED_GROUPS = String.join(",",
			"SecP384r1MLKEM1024",
			"SecP256r1MLKEM768",
			"X25519MLKEM768",
			"secp384r1");

	/*
	 * Only include RSA-PSS signature schemes where digest >= SHA-384.
	 *
	 * Server RSA keys should be >= 3072 bits.
	 */
	private static final String TLS_SERVER_SIGNATURE_SCHEMES = String.join(",",
			/* RSASSA-PSS algorithms with public key OID rsaEncryption */
			"rsa_pss_rsae_sha384",
			"rsa_pss_rsae_sha512",
			/* RSASSA-PSS algorithms with public key OID RSASSA-PSS */
			"rsa_pss_pss_sha384",
			"rsa_pss_pss_sha512");

	/*
	 * Outbound client TLS signature schemes are more permissive for WebPKI interoperability.
	 * This also governs mTLS client signature verification if enabled.
	 */
	private static final String TLS_CLIENT_SIGNATURE_SCHEMES = String.join(",",
			/* RSASSA-PKCS1-v1_5 algorithms */
			"rsa_pkcs1_sha256",
			"rsa_pkcs1_sha384",
			"rsa_pkcs1_sha512",
			/* ECDSA algorithms */
			"ecdsa_secp256r1_sha256",
			"ecdsa_secp384r1_sha384",
			"ecdsa_secp521r1_sha512",
			/* RSASSA-PSS algorithms with public key OID rsaEncryption */
			"rsa_pss_rsae_sha256",
			"rsa_pss_rsae_sha384",
			"rsa_pss_rsae_sha512",
			/* RSASSA-PSS algorithms with public key OID RSASSA-PSS */
			"rsa_pss_pss_sha256",
			"rsa_pss_pss_sha384",
			"rsa_pss_pss_sha512");

	/*
	 * Session ticket timeout (seconds)
	 */
	private static final String TLS_SERVER_SESSION_TIMEOUT = "300";

	/*
	 * Disable the server session cache
	 */
	private static final String TLS_SERVER_SESSION_CACHE_SIZE = "0";

	/**
	 * Configure SunJSSE global TLS / PQC properties before any outbound HTTPS
	 * client or embedded Jetty SSL connector initializes JSSE static defaults.
	 */
	public static void configureGlobalTlsProperties() {
		System.setProperty("jdk.tls.server.disableExtensions", TLS_DISABLE_EXTENSIONS);
		System.setProperty("jdk.tls.client.disableExtensions", TLS_DISABLE_EXTENSIONS);
		System.setProperty("jdk.tls.namedGroups", TLS_NAMED_GROUPS);
		System.setProperty("jdk.tls.server.SignatureSchemes", TLS_SERVER_SIGNATURE_SCHEMES);
		System.setProperty("jdk.tls.client.SignatureSchemes", TLS_CLIENT_SIGNATURE_SCHEMES);
		System.setProperty("jdk.tls.server.sessionTicketTimeout", TLS_SERVER_SESSION_TIMEOUT);
		System.setProperty("javax.net.ssl.sessionCacheSize", TLS_SERVER_SESSION_CACHE_SIZE);
	}

	public JettyCustomizer() {
		configureGlobalTlsProperties();
	}

	@Bean
	public WebServerFactoryCustomizer<JettyServletWebServerFactory> jettyTlsCustomizer() {
		return factory -> factory.addServerCustomizers(server -> {
			configureGlobalTlsProperties();
			for (Connector connector : server.getConnectors()) {
				if (connector instanceof ServerConnector serverConnector) {
					SslConnectionFactory ssl = serverConnector.getConnectionFactory(SslConnectionFactory.class);
					if (null != ssl) {
						SslContextFactory sslContextFactory = ssl.getSslContextFactory();
						sslContextFactory.setExcludeProtocols();
						sslContextFactory.setIncludeProtocols(TLS_SERVER_PROTOCOLS);
						sslContextFactory.setExcludeCipherSuites();
						sslContextFactory.setIncludeCipherSuites(TLS_SERVER_CIPHERSUITES);
					}
				}
			}
		});
	}
}
