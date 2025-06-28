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
import java.util.List;
import java.util.Map;

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
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestClient {

	private final static Logger LOG = LoggerFactory.getLogger(RestClient.class);

	private final static Logger LOGMAIN = LoggerFactory.getLogger(RestClient.class.getName().concat(".main()"));

	public VssResponse vssRequest(final String url, final String policy, final X509Certificate certificate) throws RestClientException {
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
		String certDigestS256 = X509Util.x5tS256(certificate);
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
		metaData.put("x5t#S256", certDigestS256);
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
		LOG.info("VSSRequest: " + jsonRequest);
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
		LOG.info("VSSResponse: " + jsonString);
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
		 * Ensure the certificate we requested (certDigestS256) matches the response (x5tS256)
		 */
		if (!certDigestS256.equals(vssResponse.x5tS256)) {
			String reqResErr = "Requested cert digest (" + certDigestS256 + ") does not match the certificate digest in the response (" + vssResponse.x5tS256 + ")";
			LOG.error(reqResErr);
			throw new RestClientException(reqResErr);
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
		/*
		 * An expired EE Certificate
		 */
		String EE_CERT1 = "-----BEGIN CERTIFICATE-----\n"
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
		mainValidate(VSS_URL, VAL_POL, EE_CERT1);
		/*
		 * *Possibly* valid EE Certificate
		 */
		String EE_CERT2 = "-----BEGIN CERTIFICATE-----\n"
				+ "MIIIyDCCBrCgAwIBAgIEYk1z8jANBgkqhkiG9w0BAQsFADCBgjELMAkGA1UEBhMC\n"
				+ "VVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDEjMCEGA1UECxMaRGVwYXJ0bWVu\n"
				+ "dCBvZiB0aGUgVHJlYXN1cnkxIjAgBgNVBAsTGUNlcnRpZmljYXRpb24gQXV0aG9y\n"
				+ "aXRpZXMxEDAOBgNVBAsTB09DSU8gQ0EwHhcNMjUwMzIzMTcxODQ3WhcNMjcwODAx\n"
				+ "MTc0NzE0WjCBrTELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVu\n"
				+ "dDEjMCEGA1UECxMaRGVwYXJ0bWVudCBvZiB0aGUgVHJlYXN1cnkxJTAjBgNVBAsT\n"
				+ "HEJ1cmVhdSBvZiB0aGUgRmlzY2FsIFNlcnZpY2UxDzANBgNVBAsTBlBlb3BsZTEn\n"
				+ "MA0GA1UEBRMGNDAzNjExMBYGA1UEAxMPVG9kZCBFLiBKb2huc29uMIIBIjANBgkq\n"
				+ "hkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq6iTFYh8wCmn5gedP2u+48qiqzdWQkMJ\n"
				+ "okQS36O3hoULIWFT5GQyDk6DfdWhut8t9PuW0Pcxrw+y37c1C1usF8EKYiJPd6KW\n"
				+ "F3PUwodgYMYFwtkc5w/YZhPXuxwEjYb1WkQ7WJKVtOv3u2Iv0ja6jSD601Z7Rl0M\n"
				+ "HL5ZwTRbOeapA+PxG3l9vPtlYOOhT6HZqjubyoxifI/Cq9kIZ0uEw12rXDG2ouL/\n"
				+ "jyP/0ine6BMAeYkrfiIEeTMFUXmydMqjIqQN+OvqjNcs6WtZDvn/pheojva2rjrg\n"
				+ "FTqoc3KP/rgkWoXYACiIV3W+TNJ2KgCMyzAzpXuXsR7MOc9R/dSqAQIDAQABo4IE\n"
				+ "FzCCBBMwDgYDVR0PAQH/BAQDAgeAMBcGA1UdIAQQMA4wDAYKYIZIAWUDAgEDDTAy\n"
				+ "BgNVHSUEKzApBgorBgEEAYI3FAICBggrBgEFBQcDAgYHKwYBBQIDBAYIKwYBBQUH\n"
				+ "AxUwEAYJYIZIAWUDBgkBBAMBAQAwggETBggrBgEFBQcBAQSCAQUwggEBMDMGCCsG\n"
				+ "AQUFBzAChidodHRwOi8vcGtpLnRyZWFzdXJ5Lmdvdi90b2NhX2VlX2FpYS5wN2Mw\n"
				+ "gaMGCCsGAQUFBzAChoGWbGRhcDovL2xkYXAudHJlYXN1cnkuZ292L291PU9DSU8l\n"
				+ "MjBDQSxvdT1DZXJ0aWZpY2F0aW9uJTIwQXV0aG9yaXRpZXMsb3U9RGVwYXJ0bWVu\n"
				+ "dCUyMG9mJTIwdGhlJTIwVHJlYXN1cnksbz1VLlMuJTIwR292ZXJubWVudCxjPVVT\n"
				+ "P2NBQ2VydGlmaWNhdGU7YmluYXJ5MCQGCCsGAQUFBzABhhhodHRwOi8vb2NzcC50\n"
				+ "cmVhc3VyeS5nb3YwgbcGA1UdEQSBrzCBrKAwBgorBgEEAYI3FAIDoCIMIFRPREQu\n"
				+ "Sk9ITlNPTkBGSVNDQUwuVFJFQVNVUlkuR09WgSBUb2RkLkpvaG5zb25AZmlzY2Fs\n"
				+ "LnRyZWFzdXJ5LmdvdqAnBghghkgBZQMGBqAbBBnSAkRYIQts1DYBDaFoWgEIQ5IR\n"
				+ "pIICEMPrhi11cm46dXVpZDoxYTM5ZjkxZC1hODNjLTRmNGUtYWViMy0yYzdhZWZk\n"
				+ "YThkYWIwggGPBgNVHR8EggGGMIIBgjAqoCigJoYkaHR0cDovL3BraS50cmVhc3Vy\n"
				+ "eS5nb3YvT0NJT19DQTYuY3JsMIIBUqCCAU6gggFKpIGXMIGUMQswCQYDVQQGEwJV\n"
				+ "UzEYMBYGA1UEChMPVS5TLiBHb3Zlcm5tZW50MSMwIQYDVQQLExpEZXBhcnRtZW50\n"
				+ "IG9mIHRoZSBUcmVhc3VyeTEiMCAGA1UECxMZQ2VydGlmaWNhdGlvbiBBdXRob3Jp\n"
				+ "dGllczEQMA4GA1UECxMHT0NJTyBDQTEQMA4GA1UEAxMHQ1JMNDAyN4aBrWxkYXA6\n"
				+ "Ly9sZGFwLnRyZWFzdXJ5Lmdvdi9jbj1DUkw0MDI3LG91PU9DSU8lMjBDQSxvdT1D\n"
				+ "ZXJ0aWZpY2F0aW9uJTIwQXV0aG9yaXRpZXMsb3U9RGVwYXJ0bWVudCUyMG9mJTIw\n"
				+ "dGhlJTIwVHJlYXN1cnksbz1VLlMuJTIwR292ZXJubWVudCxjPVVTP2NlcnRpZmlj\n"
				+ "YXRlUmV2b2NhdGlvbkxpc3Q7YmluYXJ5MB8GA1UdIwQYMBaAFDiAH3dn6Zc1M+6w\n"
				+ "XhsHJgiWJ0XaMB0GA1UdDgQWBBTQ0qoY95LSVqPnU53dXbgC3tFFLjANBgkqhkiG\n"
				+ "9w0BAQsFAAOCAgEAF5xWY8+eDZZjzyyPfYSt7DRO+P8HaBTynux0YYz2UUnN+3kW\n"
				+ "O6peLtVyv/K3CDj/I2+t1O1eSFTMFt1WDQLQXpKr1LLHzKWab9M0qeuAO1AaKJex\n"
				+ "+lVNeBLXbjFxg5uY7aHDACWXxtGUKUIKuIM4PTZtq21ygy+UABpit083xlviUFhz\n"
				+ "764v67vhEn96q7LxGE1z3MvdjB3IdDmkXBpCkDX/mKy1f8Hx1tJZAsyATgQ6MLi4\n"
				+ "AL/gsC+ONi31eXQxYqB41MN8LiK8NIydGLbvYZiHIUhU0iO6t7p8XPzectP09zNJ\n"
				+ "0RsLctG9cbWQr0t186kZNqCmqUrMnMLxGZpeHGeEgSuTpNyToiVRtyVXz+2C6+Xs\n"
				+ "G7VjjtGPJnCa7w7WvpaqiQ5wQLEJUmUS/SyogMqkZYxmEEGLj+pldeQFHX6fLx/6\n"
				+ "nzblSrvg5ignxVKmNjvm5mXyJh1p1VFHchtU26n+GCZTCcLzDraydxjSgW2z5lXW\n"
				+ "9v32pX1GaSxdUW52132pE6F2OOGIDE0yMEdkCxzmK8RwUEWxGEghNCuBmkT996vd\n"
				+ "XXhyRsX0ltPUHre0mapQCTafDmhcaFIe9EMC0jt5AfiGU1mkR4VMALV9j5hIR0KV\n"
				+ "g6di2Q9V3TkYt0a25JlseH9RL7rKf+7R+0fcjygmT68sGEfRL4vx9Xu4Ezo=\n"
				+ "-----END CERTIFICATE-----";
		mainValidate(VSS_URL, VAL_POL, EE_CERT2);
	}
	
	public static void mainValidate(String url, String policy, String pemCert) {
		Long start = System.currentTimeMillis();
		/*
		 * Convert the certificate to an X509Certificate to ensure it is a valid X509Certificate
		 */
		CertificateFactory cf = null;
		ByteArrayInputStream bais = null;
		X509Certificate certificate = null;
		try {
			cf = CertificateFactory.getInstance("X509");
			bais = new ByteArrayInputStream(pemCert.getBytes());
			certificate = (X509Certificate) cf.generateCertificate(bais);
		} catch (CertificateException e) {
			LOGMAIN.error("Error Decoding/Encoding Certificate", e);
		}
		/*
		 * Send the rest request
		 */
		RestClient client = new RestClient();
		VssResponse response = client.vssRequest(url, policy, certificate);
		/*
		 * Convert the POJO response to JSON for logging
		 */
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			LOGMAIN.info(objectMapper.writeValueAsString(response));
		} catch (JsonProcessingException e) {
			LOGMAIN.error("Error converting POJO to JSON", e);
		}
		/*
		 * Start processing the response data
		 * 
		 * First, we will log the SHA-256 digest of the certificate in HEX and Base64
		 */
		LOGMAIN.info("SHA-256 Digest (x5t#S256):" + response.x5tS256);
		byte[] sha256Digest = Base64.getUrlDecoder().decode(response.x5tS256);			
		LOGMAIN.info("SHA-256 Digest (hex):" + X509Util.byteArrayToHexString(sha256Digest));
		/*
		 * Process result
		 */
		ValidationResult res = response.validationResult;
		if (res instanceof Success) {
			/*
			 * If successful, indicate as much
			 * We may not need the success data if we don't care about the `x509CertificatePath` or `policyTree`
			 * 
			 * If we do, then cast the result to a Success and fetch the objects:
			 * 
			 * Success success = (Success)res;
			 */
			Success success = (Success)res;
			LOGMAIN.info("Certificate Validation Success: " + response.x509SubjectName);
			LOGMAIN.info("isCaCertificate: " + response.isCA);
			LOGMAIN.info("CLAIM: x509SubjectName: " + response.x509SubjectName);
			LOGMAIN.info("CLAIM: x509IssuerName: " + response.x509IssuerName);
			LOGMAIN.info("CLAIM: x509SerialNumber: " + response.x509SerialNumber);
			List<SANValue> sans = response.x509SubjectAltName;
			// TODO: Some claims like `otherName:pivFASC-N` can be further parsed, and offered as additional claims
			for (SANValue san: sans) {
				LOGMAIN.info("CLAIM: " + san.type + ": " + san.value);
			}
		} else {
			/*
			 * If *not* successful, indicate as much
			 */
			Fail fail = (Fail)res;
			LOGMAIN.error("Certificate Validation Failed: " + response.x509SubjectName + ": " + fail.invalidityReasonText);
			LOGMAIN.info("isCaCertificate: " + response.isCA);
			
		}
		Long end = System.currentTimeMillis();
		Long duration = end - start;
		LOGMAIN.info("Completed in " + duration + "ms");
	}

}
