package org.keysupport.api.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.keysupport.api.pkix.X509Util;
import org.keysupport.api.pojo.vss.Fail;
import org.keysupport.api.pojo.vss.Success;
import org.keysupport.api.pojo.vss.ValidationResult;
import org.keysupport.api.pojo.vss.VssRequest;
import org.keysupport.api.pojo.vss.VssResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestClient {

	private final static Logger LOG = LoggerFactory.getLogger(RestClient.class);

	public VssResponse vssRequest(final String url, final X509Certificate certificate, final String policy) throws RestClientException {
		ObjectMapper mapper = new ObjectMapper();
		/*
		 * Create our JDK Native Client
		 */
		HttpClient client = HttpClient.newHttpClient();
		/*
		 * Create the JSON request
		 */
		VssRequest vssRequest = new VssRequest();
		vssRequest.validationPolicyId = policy;
		byte[] certBytes = null;
		try {
			certBytes = certificate.getEncoded();
		} catch (CertificateEncodingException e) {
			LOG.error("Error obtaining certificate bytes", e);
		}
		vssRequest.x509Certificate = Base64.getEncoder().encodeToString(certBytes);
		/*
		 * Add metadata to the request
		 *
		 * x5t#S256 is the SHA-256 digest value, encoded to Base64
		 */
		Map<String, String> metaData = new HashMap<>();
		metaData.put("x5t#S256", X509Util.x5tS256(certificate));
		vssRequest.setAdditionalProperty("metaData", metaData);
		/*
		 * Post the JSON request, and obtain JSON Response
		 */
		String jsonRequest = null;
		try {
			jsonRequest = mapper.writeValueAsString(vssRequest);
		} catch (JsonProcessingException e) {
			LOG.error("Error converting POJO to JSON", e);
			throw new RestClientException("Error converting POJO to JSON", e);
		}
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(jsonRequest)).build();
		HttpResponse<String> response = null;
		try {
			response = client.send(request, BodyHandlers.ofString());
		} catch (IOException e) {
			LOG.error("Error communicating with VSS", e);
			throw new RestClientException("Error communicating with VSS", e);
		} catch (InterruptedException e) {
			LOG.error("Error communicating with VSS", e);
			throw new RestClientException("Error communicating with VSS", e);
		}
		/*
		 * Process any error handling that may have been encountered
		 */
		int statusCode = response.statusCode();
		if (statusCode < 200 || statusCode >= 300) {
			LOG.error("Unexpected Status Code of " + statusCode + " received from REST call of " + url
					+ ". Response Message = " + response.body());
			throw new RestClientException("Status Code of " + statusCode + " received from REST call of " + url
					+ ". Response Message = " + response.body());
		}
		String jsonString = response.body();
		LOG.debug("VSSResponse: " + jsonString);
		/*
		 * Render POJO Response
		 */
		VssResponse vssResponse = null;
		try {
			vssResponse = mapper.readValue(response.body(), VssResponse.class);
		} catch (JsonMappingException e) {
			LOG.error("Error communicating with VSS", e);
			throw new RestClientException("Error communicating with VSS", e);
		} catch (JsonProcessingException e) {
			LOG.error("Error communicating with VSS", e);
			throw new RestClientException("Error communicating with VSS", e);
		}
		/*
		 * Return POJO Response
		 */
		return vssResponse;
	}

	public static void main(String args[]) {
		/*
		 * Test method which calls this client and validates a certificate against VSS
		 */
		/*
		 * Set rest request details with the URL, Policy, and Certificate
		 */
		String VSS_URL = "https://home.keysupport.net/vss/v2/validate";
		String VAL_POL = "2.16.840.1.101.10.2.18.2.1.4";
		String EE_CERT = "-----BEGIN CERTIFICATE-----\n"
				+ "MIIHuDCCBqCgAwIBAgIEWckNVjANBgkqhkiG9w0BAQsFADCBgjELMAkGA1UEBhMC\n"
				+ "VVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDEjMCEGA1UECxMaRGVwYXJ0bWVu\n"
				+ "dCBvZiB0aGUgVHJlYXN1cnkxIjAgBgNVBAsTGUNlcnRpZmljYXRpb24gQXV0aG9y\n"
				+ "aXRpZXMxEDAOBgNVBAsTB09DSU8gQ0EwHhcNMTcxMjEyMTQyNjQxWhcNMjAxMjEx\n"
				+ "MTQ1NDIyWjCBrTELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVu\n"
				+ "dDEjMCEGA1UECxMaRGVwYXJ0bWVudCBvZiB0aGUgVHJlYXN1cnkxJTAjBgNVBAsT\n"
				+ "HEJ1cmVhdSBvZiB0aGUgRmlzY2FsIFNlcnZpY2UxDzANBgNVBAsTBlBlb3BsZTEn\n"
				+ "MA0GA1UEBRMGNDAzNjExMBYGA1UEAxMPVG9kZCBFLiBKb2huc29uMIIBIjANBgkq\n"
				+ "hkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAreEzlhcFpsnua16/J1CjtfIEgNJHRV1+\n"
				+ "4qINmYQjeTN3KoOiaSYxIwp1BE/2yNmT86o+wciqXl9btHPt2R8Xyama2/4JaKmc\n"
				+ "U2zk8I+a7E/NQdDsc5bpYqzv5/tBGni97nIhV1Iuw3EqAe9Kc0w+0U+/yqz5kJ1c\n"
				+ "N6gJg6WTpAAS8VJ3aefvvTbtXYMqk2Yjvj50liDjj//k4U4VPA+VKgX8qpJaTKvp\n"
				+ "7PWyopnfUfr0wRd4H6/SMbRkPWKLanUwZgFbfIUkyavkPnE1zFaSMQe+/UAP8I+r\n"
				+ "9zZKuaqciMl7IHh+EBVGa//wU7/idQV8rpDDdSELRGHNzGoJXmDTFwIDAQABo4IE\n"
				+ "BzCCBAMwDgYDVR0PAQH/BAQDAgeAMCUGA1UdIAQeMBwwDAYKYIZIAWUDAgEDDTAM\n"
				+ "BgpghkgBZQMCAQUEMCUGA1UdJQQeMBwGCisGAQQBgjcUAgIGCCsGAQUFBwMCBgRV\n"
				+ "HSUAMBAGCWCGSAFlAwYJAQQDAQEAMIIBCAYIKwYBBQUHAQEEgfswgfgwMAYIKwYB\n"
				+ "BQUHMAKGJGh0dHA6Ly9wa2kudHJlYXMuZ292L3RvY2FfZWVfYWlhLnA3YzCBoAYI\n"
				+ "KwYBBQUHMAKGgZNsZGFwOi8vbGRhcC50cmVhcy5nb3Yvb3U9T0NJTyUyMENBLG91\n"
				+ "PUNlcnRpZmljYXRpb24lMjBBdXRob3JpdGllcyxvdT1EZXBhcnRtZW50JTIwb2Yl\n"
				+ "MjB0aGUlMjBUcmVhc3VyeSxvPVUuUy4lMjBHb3Zlcm5tZW50LGM9VVM/Y0FDZXJ0\n"
				+ "aWZpY2F0ZTtiaW5hcnkwIQYIKwYBBQUHMAGGFWh0dHA6Ly9vY3NwLnRyZWFzLmdv\n"
				+ "djCBtwYDVR0RBIGvMIGsoDAGCisGAQQBgjcUAgOgIgwgVE9ERC5KT0hOU09OQEZJ\n"
				+ "U0NBTC5UUkVBU1VSWS5HT1aBIFRvZGQuSm9obnNvbkBmaXNjYWwudHJlYXN1cnku\n"
				+ "Z292oCcGCGCGSAFlAwYGoBsEGdICRFghCSzYVyiFoWhaAQhDkhGkggIQw/OGLXVy\n"
				+ "bjp1dWlkOjhhNTM4NDQ5LWRmOTYtMjg0Ny1hODgzLThmOGQ5YjQ2NDMyNzCCAYkG\n"
				+ "A1UdHwSCAYAwggF8MCegJaAjhiFodHRwOi8vcGtpLnRyZWFzLmdvdi9PQ0lPX0NB\n"
				+ "NC5jcmwwggFPoIIBS6CCAUekgZcwgZQxCzAJBgNVBAYTAlVTMRgwFgYDVQQKEw9V\n"
				+ "LlMuIEdvdmVybm1lbnQxIzAhBgNVBAsTGkRlcGFydG1lbnQgb2YgdGhlIFRyZWFz\n"
				+ "dXJ5MSIwIAYDVQQLExlDZXJ0aWZpY2F0aW9uIEF1dGhvcml0aWVzMRAwDgYDVQQL\n"
				+ "EwdPQ0lPIENBMRAwDgYDVQQDEwdDUkwyMDE5hoGqbGRhcDovL2xkYXAudHJlYXMu\n"
				+ "Z292L2NuPUNSTDIwMTksb3U9T0NJTyUyMENBLG91PUNlcnRpZmljYXRpb24lMjBB\n"
				+ "dXRob3JpdGllcyxvdT1EZXBhcnRtZW50JTIwb2YlMjB0aGUlMjBUcmVhc3VyeSxv\n"
				+ "PVUuUy4lMjBHb3Zlcm5tZW50LGM9VVM/Y2VydGlmaWNhdGVSZXZvY2F0aW9uTGlz\n"
				+ "dDtiaW5hcnkwHwYDVR0jBBgwFoAU184oTMgkalZGW3Vli2fE+sjgiKUwHQYDVR0O\n"
				+ "BBYEFNwN2tw0jITHNFZMNFTGPFCz9odHMA0GCSqGSIb3DQEBCwUAA4IBAQCUAOWF\n"
				+ "YhyFKUlJu13v1rY0u1YWhzr4F2mXLvT1Vnt0QuJvw7umUikK6bXRXbkVvtnMhrVY\n"
				+ "x/j7eEU3xx3NQA58bnwpH8dk+My7cAPqQHAsS5MKCYbZ4X5v1Gtin2NK90DlOW5W\n"
				+ "zbAuCoEwFRMuUc7swbu2ND/DXCdy8T+/DGIaP0eI4gvkA2AClsSow6EF7LqCmMuh\n"
				+ "vPBn684zZ2pMb4GAi0v/H1gAwib4+lZNnnXvnCAYvCWteFwaaqaL4bJ2HK2vicL7\n"
				+ "sj7naroU9cBopwXLLcSIL9eEcKATpmfzjskE1NbehWEY/NnHuIJ5QdZ2gmIJvO7M\n"
				+ "xFOdRh2ZAHxqrSYF\n"
				+ "-----END CERTIFICATE-----";
		/*
		 * Convert the certificate to an X509Certificate to ensure it is a valid X509Certificate
		 */
		CertificateFactory cf = null;
		ByteArrayInputStream bais = null;
		X509Certificate certificate = null;
		try {
			cf = CertificateFactory.getInstance("X509");
			bais = new ByteArrayInputStream(EE_CERT.getBytes());
			certificate = (X509Certificate) cf.generateCertificate(bais);
		} catch (CertificateException e) {
			LOG.error("Error Decoding/Encoding Certificate", e);
		}
		/*
		 * Send the rest request
		 */
		RestClient client = new RestClient();
		VssResponse response = client.vssRequest(VSS_URL, certificate, VAL_POL);
		/*
		 * Convert the POJO response to JSON for logging
		 */
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			LOG.info(objectMapper.writeValueAsString(response));
		} catch (JsonProcessingException e) {
			LOG.error("Error converting POJO to JSON", e);
		}
		/*
		 * Start processing the response data
		 * 
		 * First, we will check to see if the validation is a SUCCESS or Fail
		 */
		ValidationResult res = response.validationResult;
		if (res instanceof Success) {
			/*
			 * We may not need the success data if we don't care about the `x509CertificatePath` or `policyTree`
			 * 
			 * If we do, then cast the result to a Success and fetch the objects:
			 * 
			 * Success success = (Success)res;
			 */
			/*
			 * Get the SHA-256 digest of the certificate
			 */
			LOG.info("SHA-256 Digest (base64):" + response.x5tS256);
			byte[] sha256Digest = Base64.getUrlDecoder().decode(response.x5tS256);
			LOG.info("SHA-256 Digest (hex):" + X509Util.byteArrayToHexString(sha256Digest));
		} else {
			Fail fail = (Fail)res;
			LOG.error("Certificate Validation Failed: " + fail.invalidityReasonText);
		}
	}

}
