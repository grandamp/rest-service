package org.keysupport.api.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keysupport.api.jetty.JettyCustomizer;
import org.keysupport.api.pkix.X509Util;
import org.keysupport.api.pojo.vss.Fail;
import org.keysupport.api.pojo.vss.SANValue;
import org.keysupport.api.pojo.vss.Success;
import org.keysupport.api.pojo.vss.ValidationResult;
import org.keysupport.api.pojo.vss.VssRequest;
import org.keysupport.api.pojo.vss.VssResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestClient {

	private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);
	private static final Logger LOGMAIN = LoggerFactory.getLogger(RestClient.class.getName().concat(".main()"));
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final HttpClient client;

	public RestClient() {
		this(HttpClient.newHttpClient());
	}

	public RestClient(HttpClient client) {
		if (null == client) {
			throw new IllegalArgumentException("HttpClient must not be null");
		}
		this.client = client;
	}

	public VssResponse vssRequest(final String url, final String policy, final X509Certificate certificate)
			throws RestClientException {
		if (null == url || url.isBlank() || null == policy || policy.isBlank() || null == certificate) {
			throw new RestClientException("url, policy, and certificate must not be null or blank");
		}

		VssRequest vssRequest = new VssRequest();
		vssRequest.validationPolicyId = policy;

		byte[] certBytes;
		try {
			certBytes = certificate.getEncoded();
		} catch (CertificateEncodingException e) {
			LOG.error("Error obtaining certificate bytes", e);
			throw new RestClientException("Error obtaining certificate bytes", e);
		}
		String certDigestS256 = X509Util.x5tS256(certificate);
		if (null == certDigestS256) {
			throw new RestClientException("Error computing SHA-256 certificate digest");
		}
		vssRequest.x509Certificate = Base64.getEncoder().encodeToString(certBytes);

		Map<String, String> metaData = new HashMap<>();
		metaData.put("x5t#S256", certDigestS256);
		vssRequest.setAdditionalProperty("metaData", metaData);

		String jsonRequest;
		try {
			jsonRequest = MAPPER.writeValueAsString(vssRequest);
		} catch (JsonProcessingException e) {
			LOG.error("Error converting POJO to JSON", e);
			throw new RestClientException("Error converting POJO to JSON", e);
		}
		LOG.info("VSSRequest: {}", jsonRequest);

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
				.build();

		HttpResponse<String> response;
		try {
			response = client.send(request, BodyHandlers.ofString());
		} catch (IOException e) {
			LOG.error("Error communicating with VSS", e);
			throw new RestClientException("Error communicating with VSS", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOG.error("Interrupted while communicating with VSS", e);
			throw new RestClientException("Interrupted while communicating with VSS", e);
		}

		int statusCode = response.statusCode();
		if (statusCode < 200 || statusCode >= 300) {
			LOG.error("Unexpected Status Code of {} received from REST call of {}. Response Message = {}",
					statusCode, url, response.body());
			throw new RestClientException("Status Code of " + statusCode + " received from REST call of " + url
					+ ". Response Message = " + response.body());
		}
		String jsonString = response.body();
		LOG.info("VSSResponse: {}", jsonString);

		VssResponse vssResponse;
		try {
			vssResponse = MAPPER.readValue(jsonString, VssResponse.class);
		} catch (JsonProcessingException e) {
			LOG.error("Error parsing VSS response JSON", e);
			throw new RestClientException("Error parsing VSS response JSON", e);
		}

		if (!certDigestS256.equals(vssResponse.x5tS256)) {
			String reqResErr = "Requested cert digest (" + certDigestS256
					+ ") does not match the certificate digest in the response (" + vssResponse.x5tS256 + ")";
			LOG.error(reqResErr);
			throw new RestClientException(reqResErr);
		}
		return vssResponse;
	}

	/**
	 * CLI entry point for validating one or more X.509 certificates (.pem, .der,
	 * .cer, .crt, .p7b, .p7c) against a VSS v2 validation endpoint.
	 *
	 * Usage: RestClient &lt;validateUrl&gt; &lt;policyOid&gt; &lt;certFile&gt;
	 */
	public static void main(String[] args) {
		JettyCustomizer.configureGlobalTlsProperties();
		int exitCode = runCli(args, new RestClient(), System.out, System.err);
		System.exit(exitCode);
	}

	static int runCli(String[] args, RestClient client, PrintStream out, PrintStream err) {
		if (null == args || args.length != 3) {
			err.println("Usage: RestClient <validateUrl> <policyOid> <certFile>");
			err.println("  validateUrl  e.g. https://home.keysupport.net/vss/v2/validate");
			err.println("  policyOid    e.g. 1.3.6.1.5.5.7.19.1 or 2.16.840.1.101.10.2.18.2.1.4");
			err.println("  certFile     Path to .pem, .der, .cer, .crt, .p7b, or .p7c file");
			return 2;
		}
		String url = args[0];
		String policy = args[1];
		Path certFile = Path.of(args[2]);

		List<X509Certificate> certs;
		try {
			certs = loadCertificates(certFile);
		} catch (IOException | CertificateException e) {
			err.println("Error loading certificates from " + certFile + ": " + e.getMessage());
			LOGMAIN.error("Error loading certificates from {}", certFile, e);
			return 2;
		}
		if (certs.isEmpty()) {
			err.println("No X.509 certificates found in " + certFile);
			return 2;
		}

		boolean allValid = true;
		for (int i = 0; i < certs.size(); i++) {
			X509Certificate cert = certs.get(i);
			long startMs = System.currentTimeMillis();
			VssResponse response;
			try {
				response = client.vssRequest(url, policy, cert);
			} catch (RestClientException e) {
				err.println("Validation request error for cert #" + (i + 1) + ": " + e.getMessage());
				LOGMAIN.error("Validation request failed", e);
				return 2;
			}
			long durationMs = System.currentTimeMillis() - startMs;

			byte[] sha256Digest = Base64.getUrlDecoder().decode(response.x5tS256);
			String sha256Hex = X509Util.byteArrayToHexString(sha256Digest);
			ValidationResult res = response.validationResult;

			if (res instanceof Success) {
				out.println("[SUCCESS] (" + durationMs + "ms) " + response.x509SubjectName);
				out.println("  Issuer:       " + response.x509IssuerName);
				out.println("  Serial:       " + response.x509SerialNumber);
				out.println("  isCA:         " + response.isCA);
				out.println("  x5t#S256:     " + response.x5tS256 + " (" + sha256Hex + ")");
				if (null != response.x509SubjectAltName) {
					for (SANValue san : response.x509SubjectAltName) {
						out.println("  SAN (" + san.type + "): " + san.value);
					}
				}
			} else if (res instanceof Fail fail) {
				allValid = false;
				err.println("[FAIL] (" + durationMs + "ms) " + response.x509SubjectName
						+ " - " + fail.invalidityReasonText
						+ " (affirmativelyInvalid=" + fail.isAffirmativelyInvalid + ")");
				err.println("  x5t#S256:     " + response.x5tS256 + " (" + sha256Hex + ")");
			} else {
				allValid = false;
				err.println("[UNKNOWN] (" + durationMs + "ms) Unexpected validationResult for "
						+ response.x509SubjectName);
			}
		}
		return allValid ? 0 : 1;
	}

	static List<X509Certificate> loadCertificates(Path certFile) throws IOException, CertificateException {
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		try (InputStream in = Files.newInputStream(certFile)) {
			Collection<? extends Certificate> raw = cf.generateCertificates(in);
			List<X509Certificate> result = new ArrayList<>(raw.size());
			for (Certificate c : raw) {
				if (c instanceof X509Certificate x509) {
					result.add(x509);
				}
			}
			return result;
		}
	}

}
