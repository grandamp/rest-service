package org.keysupport.api.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.keysupport.api.pkix.X509Util;
import org.keysupport.api.pojo.vss.Fail;
import org.keysupport.api.pojo.vss.SANValue;
import org.keysupport.api.pojo.vss.Success;
import org.keysupport.api.pojo.vss.VssResponse;
import org.keysupport.api.testutil.CertFixtures;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

class RestClientTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Test
	void vssRequest_roundTripsSuccessAndHandlesErrors() throws Exception {
		X509Certificate cert = CertFixtures.load("ca/dod-id-ca-65.pem").cert();
		String x5t = X509Util.x5tS256(cert);

		AtomicInteger mode = new AtomicInteger(0);
		AtomicReference<String> capturedBody = new AtomicReference<>();

		HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/vss/v2/validate", exchange -> {
			capturedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
			int m = mode.get();
			if (m == 0) {
				VssResponse ok = new VssResponse();
				ok.x5tS256 = x5t;
				ok.x509SubjectName = cert.getSubjectX500Principal().getName();
				ok.x509IssuerName = cert.getIssuerX500Principal().getName();
				ok.x509SerialNumber = cert.getSerialNumber().toString();
				SANValue san = new SANValue();
				san.type = "dNSName";
				san.value = "keysupport.net";
				ok.x509SubjectAltName = List.of(san);
				ok.validationResult = new Success();
				byte[] body = MAPPER.writeValueAsBytes(ok);
				exchange.getResponseHeaders().add("Content-Type", "application/json");
				exchange.sendResponseHeaders(200, body.length);
				exchange.getResponseBody().write(body);
			} else if (m == 1) {
				VssResponse mismatch = new VssResponse();
				mismatch.x5tS256 = "WRONG_DIGEST";
				mismatch.validationResult = new Success();
				byte[] body = MAPPER.writeValueAsBytes(mismatch);
				exchange.getResponseHeaders().add("Content-Type", "application/json");
				exchange.sendResponseHeaders(200, body.length);
				exchange.getResponseBody().write(body);
			} else if (m == 2) {
				byte[] body = "{\"status\":\"400 BAD_REQUEST\"}".getBytes(StandardCharsets.UTF_8);
				exchange.sendResponseHeaders(400, body.length);
				exchange.getResponseBody().write(body);
			} else if (m == 3) {
				VssResponse fail = new VssResponse();
				fail.x5tS256 = x5t;
				fail.x509SubjectName = cert.getSubjectX500Principal().getName();
				Fail f = new Fail();
				f.isAffirmativelyInvalid = true;
				f.invalidityReasonText = "Certificate Revoked";
				fail.validationResult = f;
				byte[] body = MAPPER.writeValueAsBytes(fail);
				exchange.getResponseHeaders().add("Content-Type", "application/json");
				exchange.sendResponseHeaders(200, body.length);
				exchange.getResponseBody().write(body);
			} else {
				byte[] body = "not-json".getBytes(StandardCharsets.UTF_8);
				exchange.sendResponseHeaders(200, body.length);
				exchange.getResponseBody().write(body);
			}
			exchange.close();
		});
		server.start();
		try {
			String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/vss/v2/validate";
			RestClient client = new RestClient();

			assertThrows(RestClientException.class, () -> client.vssRequest(null, "1.3.6.1.5.5.7.19.1", cert));
			assertThrows(RestClientException.class, () -> client.vssRequest(url, "1.3.6.1.5.5.7.19.1", null));
			assertThrows(IllegalArgumentException.class, () -> new RestClient(null));

			X509Certificate badCert = mock(X509Certificate.class);
			when(badCert.getEncoded()).thenThrow(new CertificateEncodingException("bad"));
			assertThrows(RestClientException.class, () -> client.vssRequest(url, "1.3.6.1.5.5.7.19.1", badCert));

			VssResponse res = client.vssRequest(url, "1.3.6.1.5.5.7.19.1", cert);
			assertNotNull(res);
			assertEquals(x5t, res.x5tS256);
			assertTrue(res.validationResult instanceof Success);
			assertTrue(capturedBody.get().contains(x5t));

			mode.set(1);
			RestClientException mismatchEx = assertThrows(RestClientException.class,
					() -> client.vssRequest(url, "1.3.6.1.5.5.7.19.1", cert));
			assertTrue(mismatchEx.getMessage().contains("does not match"));

			mode.set(2);
			RestClientException httpEx = assertThrows(RestClientException.class,
					() -> client.vssRequest(url, "1.3.6.1.5.5.7.19.1", cert));
			assertTrue(httpEx.getMessage().contains("Status Code of 400"));

			mode.set(4);
			assertThrows(RestClientException.class,
					() -> client.vssRequest(url, "1.3.6.1.5.5.7.19.1", cert));

			// CLI tests
			ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
			ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
			PrintStream outPs = new PrintStream(outBuf, true, StandardCharsets.UTF_8);
			PrintStream errPs = new PrintStream(errBuf, true, StandardCharsets.UTF_8);

			assertEquals(2, RestClient.runCli(new String[0], client, outPs, errPs));

			Path pemPath = Path.of("src/test/resources/certs/ca/dod-id-ca-65.pem");
			mode.set(0);
			assertEquals(0, RestClient.runCli(new String[] { url, "1.3.6.1.5.5.7.19.1", pemPath.toString() },
					client, outPs, errPs));
			String cliOut = outBuf.toString(StandardCharsets.UTF_8);
			assertTrue(cliOut.contains("[SUCCESS]"));
			assertTrue(cliOut.contains("SAN (dNSName): keysupport.net"));

			mode.set(3);
			assertEquals(1, RestClient.runCli(new String[] { url, "1.3.6.1.5.5.7.19.1", pemPath.toString() },
					client, outPs, errPs));
			assertTrue(errBuf.toString(StandardCharsets.UTF_8).contains("[FAIL]"));
			assertTrue(errBuf.toString(StandardCharsets.UTF_8).contains("Certificate Revoked"));

			assertEquals(2, RestClient.runCli(new String[] { url, "1.3.6.1.5.5.7.19.1", "/nonexistent/cert.pem" },
					client, outPs, errPs));

			RestClientException exWithCause = new RestClientException("msg", new RuntimeException("cause"));
			assertEquals("msg", exWithCause.getMessage());
			assertNotNull(exWithCause.getCause());
		} finally {
			server.stop(0);
		}
	}

	@Test
	void vssRequest_restoresInterruptAndWrapsIoErrors(@TempDir Path tempDir) throws Exception {
		X509Certificate cert = CertFixtures.load("ca/dod-id-ca-65.pem").cert();

		HttpClient ioFailClient = mock(HttpClient.class);
		when(ioFailClient.send(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
				.thenThrow(new IOException("conn refused"));
		RestClient c1 = new RestClient(ioFailClient);
		assertThrows(RestClientException.class,
				() -> c1.vssRequest("http://127.0.0.1:1/vss/v2/validate", "1.3.6.1.5.5.7.19.1", cert));

		HttpClient intClient = mock(HttpClient.class);
		when(intClient.send(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
				.thenThrow(new InterruptedException("interrupted"));
		RestClient c2 = new RestClient(intClient);
		try {
			assertThrows(RestClientException.class,
					() -> c2.vssRequest("http://127.0.0.1:1/vss/v2/validate", "1.3.6.1.5.5.7.19.1", cert));
			assertTrue(Thread.currentThread().isInterrupted());
		} finally {
			Thread.interrupted(); // clear interrupt flag for subsequent tests
		}

		Path emptyFile = tempDir.resolve("empty.pem");
		Files.writeString(emptyFile, "");
		ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
		ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
		assertEquals(2, RestClient.runCli(
				new String[] { "http://127.0.0.1:1/vss/v2/validate", "1.3.6.1.5.5.7.19.1", emptyFile.toString() },
				c1, new PrintStream(outBuf), new PrintStream(errBuf)));
	}
}
