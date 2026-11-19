package org.keysupport.api.testutil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

public final class CertFixtures {

	public static final Path CERTS_ROOT = Path.of("src/test/resources/certs");

	public record CertFixture(String name, Path path, X509Certificate cert, String base64Der, boolean isCa) {
		@Override
		public String toString() {
			return name;
		}
	}

	private CertFixtures() {
	}

	public static List<CertFixture> loadAll() throws IOException, CertificateException {
		return loadFrom(CERTS_ROOT);
	}

	public static Stream<CertFixture> allStream() throws IOException, CertificateException {
		return loadAll().stream();
	}

	private static List<CertFixture> loadFrom(Path root) throws IOException, CertificateException {
		if (!Files.isDirectory(root)) {
			throw new IOException("Certificate fixture directory not found: " + root.toAbsolutePath());
		}
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		List<Path> files;
		try (Stream<Path> walk = Files.walk(root)) {
			files = walk
					.filter(Files::isRegularFile)
					.filter(p -> p.getFileName().toString().endsWith(".pem"))
					.sorted()
					.toList();
		}
		List<CertFixture> fixtures = new ArrayList<>(files.size());
		for (Path p : files) {
			try (InputStream in = Files.newInputStream(p)) {
				X509Certificate cert = (X509Certificate) cf.generateCertificate(in);
				String b64 = Base64.getEncoder().encodeToString(cert.getEncoded());
				boolean isCa = cert.getBasicConstraints() >= 0;
				fixtures.add(new CertFixture(root.relativize(p).toString(), p, cert, b64, isCa));
			} catch (CertificateEncodingException e) {
				throw new CertificateException("Failed to encode fixture " + p, e);
			}
		}
		return fixtures;
	}
}
