package org.keysupport.api.controller.vss;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.keysupport.api.ApiError;
import org.keysupport.api.config.ConfigurationPolicies;
import org.keysupport.api.controller.ServiceException;
import org.keysupport.api.pkix.ValidatePKIX;
import org.keysupport.api.pkix.X509Util;
import org.keysupport.api.pkix.cache.ElasticacheClient;
import org.keysupport.api.pojo.vss.Fail;
import org.keysupport.api.pojo.vss.Success;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.keysupport.api.pojo.vss.ValidationResult;
import org.keysupport.api.pojo.vss.VssRequest;
import org.keysupport.api.pojo.vss.VssResponse;
import org.keysupport.api.singletons.HTTPClientSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "validate", description = "Validate a certificate based on the specified validation policy")
public class ValidateController {

	private final static Logger LOG = LoggerFactory.getLogger(ValidateController.class);

	private final String BASE_URI = System.getenv("BASE_URI");

	/**
	 * Field POL_SIZE_LIMIT
	 */
	private final int POL_SIZE_LIMIT = 50;

	/**
	 * Field PEM_SIZE_LIMIT
	 */
	private final int PEM_SIZE_LIMIT = 8192;

	@Autowired
	private VssResponse response;

	@PostMapping(path = "/vss/v2/validate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Certificate Validation Request", required = true, content = @Content(schema = @Schema(implementation = VssRequest.class), mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
			@ExampleObject(name = "A validation request using a valid DoD Issuing CA certificate", value = "{\n"
					+ "  \"validationPolicyId\": \"c21f969b-5f03-333d-83e0-4f8f136e7682\",\n"
					+ "  \"x509Certificate\": \"MIIEuTCCA6GgAwIBAgICBUwwDQYJKoZIhvcNAQELBQAwWzELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDEMMAoGA1UECxMDRG9EMQwwCgYDVQQLEwNQS0kxFjAUBgNVBAMTDURvRCBSb290IENBIDMwHhcNMjEwNjAxMTQxMTIzWhcNMjcwNjAyMTQxMTIzWjBaMQswCQYDVQQGEwJVUzEYMBYGA1UEChMPVS5TLiBHb3Zlcm5tZW50MQwwCgYDVQQLEwNEb0QxDDAKBgNVBAsTA1BLSTEVMBMGA1UEAxMMRE9EIElEIENBLTY1MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnkK9OCQ+D0b/7SLsEs0LCElhKIzGtiZDBw9VLqCaxTHlxaYEPV/B/X9NGoP5PE4ToBOSramLCMPbwjadhNk8O0gEInZCuEzV17vvx6O4xg+FJ9OO76LU1KeXJnnvX1YnCKz3uxrn3sw1jQugEEd1yPwKoHMjJ2Sr7Vgrm1e983EgiRint9lble7x/MDLvEZDELeeqhPZvRiz1qwVG+/p2ks980qFLFLl1INOUSPnSLIbafg7cWE9yTC5i99s4pJnP2ThyBv6JsgFzbbj9FEYGyh75GjIMEv8ulcQ3ATOSBREUPzrd6sQmideeqvxXrDYxo8Qel6brZiti+5vEr3OzQIDAQABo4IBhjCCAYIwHwYDVR0jBBgwFoAUbIqUonexgHIdgXoWqvLczmbuRcAwHQYDVR0OBBYEFGLgSDhWbW9rJb67w4hYsaycQ8lbMA4GA1UdDwEB/wQEAwIBhjBnBgNVHSAEYDBeMAsGCWCGSAFlAgELJDALBglghkgBZQIBCycwCwYJYIZIAWUCAQsqMAsGCWCGSAFlAgELOzAMBgpghkgBZQMCAQMNMAwGCmCGSAFlAwIBAxEwDAYKYIZIAWUDAgEDJzASBgNVHRMBAf8ECDAGAQH/AgEAMAwGA1UdJAQFMAOAAQAwNwYDVR0fBDAwLjAsoCqgKIYmaHR0cDovL2NybC5kaXNhLm1pbC9jcmwvRE9EUk9PVENBMy5jcmwwbAYIKwYBBQUHAQEEYDBeMDoGCCsGAQUFBzAChi5odHRwOi8vY3JsLmRpc2EubWlsL2lzc3VlZHRvL0RPRFJPT1RDQTNfSVQucDdjMCAGCCsGAQUFBzABhhRodHRwOi8vb2NzcC5kaXNhLm1pbDANBgkqhkiG9w0BAQsFAAOCAQEAF8Uj33K0ZM9adtfd8IM2ebqwgbgRxi22Pb6bKkKOkGV2NU4wMckpuRpUrQGJmy6CIXZ84QWz9DZSNAU0nyHXB6PLbSV0jnzKygWO7IOv83M6dcnCG8QUP1o20V0NGhzNBEtKjxWENZCYHEruxm+2rB+MBngPhkBgdni2npetHX2e1cmsgMS6G1PUh2idhZ8Mpdofnr+V0GuKLpwiNz3hLnKehl2Bs6aHG2IIOm/PdzvsKCP2eiGzS3SiiCf6fukYoYBNedL8fHfFNyM4UPNgc4eG+bu0GJK4wKPVjiX7xYDdGaYZ2m4Y++zrKuMq+Oar6DQGq3SERMAZCDYsEt3z2g==\"\n"
					+ "}"),
			@ExampleObject(name = "A validation request using an invalid end-entity certificate", value = "{\n"
					+ "  \"validationPolicyId\": \"c21f969b-5f03-333d-83e0-4f8f136e7682\",\n"
					+ "  \"x509Certificate\": \"MIIKNzCCCB+gAwIBAgIEYIWQKTANBgkqhkiG9w0BAQsFADCBuzETMBEGCgmSJomT8ixkARkWA3NidTEVMBMGCgmSJomT8ixkARkWBXN0YXRlMRYwFAYDVQQDDA1Db25maWd1cmF0aW9uMREwDwYDVQQDDAhTZXJ2aWNlczEcMBoGA1UEAwwTUHVibGljIEtleSBTZXJ2aWNlczEMMAoGA1UEAwwDQUlBMTYwNAYDVQQDDC1VLlMuIERlcGFydG1lbnQgb2YgU3RhdGUgQUQgSGlnaCBBc3N1cmFuY2UgQ0EwHhcNMjIwNjI5MTIzNjA3WhcNMjUwNjI5MTMwNjA3WjCBljETMBEGCgmSJomT8ixkARkWA3NidTEVMBMGCgmSJomT8ixkARkWBXN0YXRlMRswGQYKCZImiZPyLGQBGRYLYXBwc2VydmljZXMxHDAaBgNVBAsME0VudGVycHJpc2UgU2VydmljZXMxDDAKBgNVBAsMA1BLSTEMMAoGA1UECwwDVEFQMREwDwYDVQQDDAhUQVBUZXN0NDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALScG/lyri4gpn3ZEX1KCmulT7zpjGjhZtB+2nzd6LM3ulj5PavgKE7Y2kOs5mge691a6RaW89u/xubvHxDL/FEzd2jxvfjJST64bejhVjqIc1zt/E1YZlv1ClK927wWqmq4PsyP63FV1UyEOO9HvrUZ5AFI/n1HMJm+zuYVXiPCgUMOmqAkceN5GznoN9AVbNgkz2tI/3PqYCtfpy6CYs+LCpXyx7/Wcf65W3PJy6xr6hVUBSKUCryl76A5xS+eM5cvF1C0BfSPBtQBbpdbGJTKs3oh4DhLNfFbBPG6Es6LicHAHpQywiCE6X0ia+btzIe4CJoOPn1NGHwtGViBJzsCAwEAAaOCBWQwggVgMA4GA1UdDwEB/wQEAwIGwDApBgNVHSUEIjAgBggrBgEFBQcDAgYKKwYBBAGCNxQCAgYIKwYBBQUHAwQwQQYDVR0gBDowODAMBgpghkgBZQMCAQYBMAwGCmCGSAFlAwIBBgIwDAYKYIZIAWUDAgEGAzAMBgpghkgBZQMCAQYEMIICMgYIKwYBBQUHAQEEggIkMIICIDBEBggrBgEFBQcwAoY4aHR0cDovL2NybHMucGtpLnN0YXRlLmdvdi9BSUEvQ2VydHNJc3N1ZWRUb0RvU0FESEFDQS5wN2MwgcgGCCsGAQUFBzAChoG7bGRhcDovL2Rpci5wa2kuc3RhdGUuZ292L2NuPVUuUy4lMjBEZXBhcnRtZW50JTIwb2YlMjBTdGF0ZSUyMEFEJTIwSGlnaCUyMEFzc3VyYW5jZSUyMENBLGNuPUFJQSxjbj1QdWJsaWMlMjBLZXklMjBTZXJ2aWNlcyxjbj1TZXJ2aWNlcyxjbj1Db25maWd1cmF0aW9uLGRjPXN0YXRlLGRjPXNidT9jQUNlcnRpZmljYXRlO2JpbmFyeTCBzwYIKwYBBQUHMAKGgcJsZGFwOi8vZGlyLnBraS5zdGF0ZS5nb3YvY249VS5TLiUyMERlcGFydG1lbnQlMjBvZiUyMFN0YXRlJTIwQUQlMjBIaWdoJTIwQXNzdXJhbmNlJTIwQ0EsY249QUlBLGNuPVB1YmxpYyUyMEtleSUyMFNlcnZpY2VzLGNuPVNlcnZpY2VzLGNuPUNvbmZpZ3VyYXRpb24sZGM9c3RhdGUsZGM9c2J1P2Nyb3NzQ2VydGlmaWNhdGVQYWlyO2JpbmFyeTA7BggrBgEFBQcwAYYvaHR0cDovL29jc3AucGtpLnN0YXRlLmdvdi9PQ1NQL0RvU09DU1BSZXNwb25kZXIwLQYDVR0RBCYwJKAiBgorBgEEAYI3FAIDoBQMElRhcFRlc3Q0QHN0YXRlLmdvdjCCAgEGA1UdHwSCAfgwggH0MIIBFaCCARGgggENhjFodHRwOi8vY3Jscy5wa2kuc3RhdGUuZ292L2NybHMvRG9TQURQS0lIQUNBLTEuY3JshoHXbGRhcDovL2Rpci5wa2kuc3RhdGUuZ292L2NuPVdpbkNvbWJpbmVkMSxjbj1VLlMuJTIwRGVwYXJ0bWVudCUyMG9mJTIwU3RhdGUlMjBBRCUyMEhpZ2glMjBBc3N1cmFuY2UlMjBDQSxjbj1BSUEsY249UHVibGljJTIwS2V5JTIwU2VydmljZXMsY249U2VydmljZXMsY249Q29uZmlndXJhdGlvbixkYz1zdGF0ZSxkYz1zYnU/Y2VydGlmaWNhdGVSZXZvY2F0aW9uTGlzdDtiaW5hcnkwgdiggdWggdKkgc8wgcwxEzARBgoJkiaJk/IsZAEZFgNzYnUxFTATBgoJkiaJk/IsZAEZFgVzdGF0ZTEWMBQGA1UEAwwNQ29uZmlndXJhdGlvbjERMA8GA1UEAwwIU2VydmljZXMxHDAaBgNVBAMME1B1YmxpYyBLZXkgU2VydmljZXMxDDAKBgNVBAMMA0FJQTE2MDQGA1UEAwwtVS5TLiBEZXBhcnRtZW50IG9mIFN0YXRlIEFEIEhpZ2ggQXNzdXJhbmNlIENBMQ8wDQYDVQQDDAZDUkw4NzQwKwYDVR0QBCQwIoAPMjAyMjA2MjkxMjM2MDdagQ8yMDI0MDgwNDE3MDYwN1owHwYDVR0jBBgwFoAUheMozyV6pgGNxW/+xVJyqp6jTE4wHQYDVR0OBBYEFJEQPx/gZOO50vXJRGxBPNbmm2XEMAkGA1UdEwQCMAAwDQYJKoZIhvcNAQELBQADggIBAFxgss5EJJ0dBpgN3Rh+F2o65DxUrRYSxK++wPQuJ/YJmyUf2ns0581nfAVLDutuL0MjDdZqeT2WkUNTvvVVcYrHvainF9ZTwHpZ0XNhAOJ6IbVGiz1bhQBAi9MSMenAgnXYZ18IFUq8Sg/EyutvDlKtydMPzqJXJW/f7+0r95HJnuvVZPhsB3YdHotxQ0GXAdQhRLKHWR2Grx/NcWtFG9mMNsMez0DeY8EuOqllx34gCvgHwQ/UP8Ng2oJBkRMMEVKCzCnQVHEirpV0PM8LRrjeLeu2+Zsx1Cwf/AhgcYQIhCLWJjXyBKeD8joVmyuWBrsP4JPrxgp/tSMDhfOuS+w3pllVHK53CyXWigxXdolEeePUpAh3jmlm0nK3vUIbz0hHJf1bwhJJAEl+F1QMiDTHfbB2xFHnYlN48AU6hK6g9eG49t05NcEbPCTCUjE+T6gi+qqvjXeZ+685fTHjgeBCaKSord0RgUhYrwHOYeErW8Oih4SB+5ryl5pvKAzwvgJ34LsTxcVK1QOZsIovTIH1pWEYBhd9YWWF6zevQKkxcnMRa+YOGIzPJwH/RMr86mEoU02JGCCoNw89raetcSNBt7ikDiJ5EWcrb7VmqXCEEwZl8mea7I7/NhktQQC5wqq9uArf/mU279MzfHSxH1VosCLWBXWxUcWhKW6eMxry\"\n"
					+ "}"),
			@ExampleObject(name = "A validation request using a valid end-entity certificate", value = "{\n"
					+ "  \"validationPolicyId\": \"c21f969b-5f03-333d-83e0-4f8f136e7682\",\n"
					+ "  \"x509Certificate\": \"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\"\n"
					+ "}"),
			@ExampleObject(name = "A validation request using an expired end-entity certificate", value = "{\n"
					+ "  \"validationPolicyId\": \"c21f969b-5f03-333d-83e0-4f8f136e7682\",\n"
					+ "  \"x509Certificate\": \"MIIHuDCCBqCgAwIBAgIEWckNVjANBgkqhkiG9w0BAQsFADCBgjELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDEjMCEGA1UECxMaRGVwYXJ0bWVudCBvZiB0aGUgVHJlYXN1cnkxIjAgBgNVBAsTGUNlcnRpZmljYXRpb24gQXV0aG9yaXRpZXMxEDAOBgNVBAsTB09DSU8gQ0EwHhcNMTcxMjEyMTQyNjQxWhcNMjAxMjExMTQ1NDIyWjCBrTELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDEjMCEGA1UECxMaRGVwYXJ0bWVudCBvZiB0aGUgVHJlYXN1cnkxJTAjBgNVBAsTHEJ1cmVhdSBvZiB0aGUgRmlzY2FsIFNlcnZpY2UxDzANBgNVBAsTBlBlb3BsZTEnMA0GA1UEBRMGNDAzNjExMBYGA1UEAxMPVG9kZCBFLiBKb2huc29uMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAreEzlhcFpsnua16/J1CjtfIEgNJHRV1+4qINmYQjeTN3KoOiaSYxIwp1BE/2yNmT86o+wciqXl9btHPt2R8Xyama2/4JaKmcU2zk8I+a7E/NQdDsc5bpYqzv5/tBGni97nIhV1Iuw3EqAe9Kc0w+0U+/yqz5kJ1cN6gJg6WTpAAS8VJ3aefvvTbtXYMqk2Yjvj50liDjj//k4U4VPA+VKgX8qpJaTKvp7PWyopnfUfr0wRd4H6/SMbRkPWKLanUwZgFbfIUkyavkPnE1zFaSMQe+/UAP8I+r9zZKuaqciMl7IHh+EBVGa//wU7/idQV8rpDDdSELRGHNzGoJXmDTFwIDAQABo4IEBzCCBAMwDgYDVR0PAQH/BAQDAgeAMCUGA1UdIAQeMBwwDAYKYIZIAWUDAgEDDTAMBgpghkgBZQMCAQUEMCUGA1UdJQQeMBwGCisGAQQBgjcUAgIGCCsGAQUFBwMCBgRVHSUAMBAGCWCGSAFlAwYJAQQDAQEAMIIBCAYIKwYBBQUHAQEEgfswgfgwMAYIKwYBBQUHMAKGJGh0dHA6Ly9wa2kudHJlYXMuZ292L3RvY2FfZWVfYWlhLnA3YzCBoAYIKwYBBQUHMAKGgZNsZGFwOi8vbGRhcC50cmVhcy5nb3Yvb3U9T0NJTyUyMENBLG91PUNlcnRpZmljYXRpb24lMjBBdXRob3JpdGllcyxvdT1EZXBhcnRtZW50JTIwb2YlMjB0aGUlMjBUcmVhc3VyeSxvPVUuUy4lMjBHb3Zlcm5tZW50LGM9VVM/Y0FDZXJ0aWZpY2F0ZTtiaW5hcnkwIQYIKwYBBQUHMAGGFWh0dHA6Ly9vY3NwLnRyZWFzLmdvdjCBtwYDVR0RBIGvMIGsoDAGCisGAQQBgjcUAgOgIgwgVE9ERC5KT0hOU09OQEZJU0NBTC5UUkVBU1VSWS5HT1aBIFRvZGQuSm9obnNvbkBmaXNjYWwudHJlYXN1cnkuZ292oCcGCGCGSAFlAwYGoBsEGdICRFghCSzYVyiFoWhaAQhDkhGkggIQw/OGLXVybjp1dWlkOjhhNTM4NDQ5LWRmOTYtMjg0Ny1hODgzLThmOGQ5YjQ2NDMyNzCCAYkGA1UdHwSCAYAwggF8MCegJaAjhiFodHRwOi8vcGtpLnRyZWFzLmdvdi9PQ0lPX0NBNC5jcmwwggFPoIIBS6CCAUekgZcwgZQxCzAJBgNVBAYTAlVTMRgwFgYDVQQKEw9VLlMuIEdvdmVybm1lbnQxIzAhBgNVBAsTGkRlcGFydG1lbnQgb2YgdGhlIFRyZWFzdXJ5MSIwIAYDVQQLExlDZXJ0aWZpY2F0aW9uIEF1dGhvcml0aWVzMRAwDgYDVQQLEwdPQ0lPIENBMRAwDgYDVQQDEwdDUkwyMDE5hoGqbGRhcDovL2xkYXAudHJlYXMuZ292L2NuPUNSTDIwMTksb3U9T0NJTyUyMENBLG91PUNlcnRpZmljYXRpb24lMjBBdXRob3JpdGllcyxvdT1EZXBhcnRtZW50JTIwb2YlMjB0aGUlMjBUcmVhc3VyeSxvPVUuUy4lMjBHb3Zlcm5tZW50LGM9VVM/Y2VydGlmaWNhdGVSZXZvY2F0aW9uTGlzdDtiaW5hcnkwHwYDVR0jBBgwFoAU184oTMgkalZGW3Vli2fE+sjgiKUwHQYDVR0OBBYEFNwN2tw0jITHNFZMNFTGPFCz9odHMA0GCSqGSIb3DQEBCwUAA4IBAQCUAOWFYhyFKUlJu13v1rY0u1YWhzr4F2mXLvT1Vnt0QuJvw7umUikK6bXRXbkVvtnMhrVYx/j7eEU3xx3NQA58bnwpH8dk+My7cAPqQHAsS5MKCYbZ4X5v1Gtin2NK90DlOW5WzbAuCoEwFRMuUc7swbu2ND/DXCdy8T+/DGIaP0eI4gvkA2AClsSow6EF7LqCmMuhvPBn684zZ2pMb4GAi0v/H1gAwib4+lZNnnXvnCAYvCWteFwaaqaL4bJ2HK2vicL7sj7naroU9cBopwXLLcSIL9eEcKATpmfzjskE1NbehWEY/NnHuIJ5QdZ2gmIJvO7MxFOdRh2ZAHxqrSYF\"\n"
					+ "}"),
			@ExampleObject(name = "A validation request using a public trust certificate", value = "{\n"
					+ "  \"validationPolicyId\": \"c21f969b-5f03-333d-83e0-4f8f136e7682\",\n"
					+ "  \"x509Certificate\": \"MIIFYjCCBEqgAwIBAgIRAOgIWMWQtieIECed/Q2JONIwDQYJKoZIhvcNAQELBQAwRjELMAkGA1UEBhMCVVMxIjAgBgNVBAoTGUdvb2dsZSBUcnVzdCBTZXJ2aWNlcyBMTEMxEzARBgNVBAMTCkdUUyBDQSAxRDQwHhcNMjIxMDEwMjAwMDU2WhcNMjMwMTA4MjAwMDU1WjAZMRcwFQYDVQQDEw5rZXlzdXBwb3J0Lm9yZzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALftackigK3dmFReOqr4txPVJrdTPRMbsz3+4ueBGtBd2OIKcFXDi8apCf76Fh8WBheDVOyDzUhkag+mTnPw0BXqYNyFntPcwosDkv7kviJgF4KmSWf3myTYmHoc0CAqoRqYsfTFkB7fK83OBnevqSZ8pgc2X7h6qSgx9Z/0dprl+2mWndIB82SZSCzpuaGXs5SPFDZVrB8Y7fUWqu1nPaTLfOzIlAIf8SHf1+f81D0Mg1rm/rAAi9HFwx+fLPnFvZUUWeErPEi9JmBJzU+TJdF3dDRldtJVgFdQXcvMMwgSjmlL+ToOwf/VRU/usIVGxLI/qoxLj+Y97ht5tzMILXMCAwEAAaOCAnYwggJyMA4GA1UdDwEB/wQEAwIFoDATBgNVHSUEDDAKBggrBgEFBQcDATAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBQHEqvLbOftuY4bf2Ojy3InQEZctzAfBgNVHSMEGDAWgBQl4hgOsleRlCrl1F2GkIPeU7O4kjB4BggrBgEFBQcBAQRsMGowNQYIKwYBBQUHMAGGKWh0dHA6Ly9vY3NwLnBraS5nb29nL3MvZ3RzMWQ0L1NRMUZ6enViUFlNMDEGCCsGAQUFBzAChiVodHRwOi8vcGtpLmdvb2cvcmVwby9jZXJ0cy9ndHMxZDQuZGVyMBkGA1UdEQQSMBCCDmtleXN1cHBvcnQub3JnMCEGA1UdIAQaMBgwCAYGZ4EMAQIBMAwGCisGAQQB1nkCBQMwPAYDVR0fBDUwMzAxoC+gLYYraHR0cDovL2NybHMucGtpLmdvb2cvZ3RzMWQ0L0oyajNSQ2lFeTA0LmNybDCCAQUGCisGAQQB1nkCBAIEgfYEgfMA8QB3AHoyjFTYty22IOo44FIe6YQWcDIThU070ivBOlejUutSAAABg8PAFU0AAAQDAEgwRgIhAInKXSrciK8OHtFWvQienDeIwBGCfDojh62EeoD62ngyAiEA6HO5HRCE00qCIKNCx6CuS9gDkvgrNm4xIS+gPc5Z8i8AdgCt9776fP8QyIudPZwePhhqtGcpXc+xDCTKhYY069yCigAAAYPDwBVHAAAEAwBHMEUCIQDOGktHy094r8OdqDhS7IPj44qJ1V9RRdR2ZEq4Sz5WOwIgcAlszeHnUNnp/jlDJPk1vIBJxh8/lokFwDBFiUU1EhAwDQYJKoZIhvcNAQELBQADggEBAGiaDoyFu4aLlLvsYnIVlTvf6bfUZ0xf3F9gSyjJBxFxFMF4BTiWWbCXGCpBT7+hHLSvq2fKKzKn2bR17XnDJkcVNBA5kxc9MQu6YE26+QkV4nk23hNidmaRimuiq4FLivsp/9gDrU+q5lutXP6n7EYI0ibXtuGk+Tx/qW9xvT4V9snhhw+4ks0Yw0B3AcdwNqRgmKYOftOLDPivLe5+lTmxbiMIPXSvkXzGVDFwj8/D8APp173Jg6XmHPHC9DSYF33e0q3nmmKQOV3PjR4mlFkY5Ewv9pKLJRnbwp01CArMs2YYdSqERZnLCH69lcHM/kuEKgMmdgLnEky2Xa03c84=\"\n"
					+ "}") }))
	@CrossOrigin(origins = "*")
	ResponseEntity<VssResponse> validate(@RequestBody VssRequest request, @RequestHeader Map<String, String> headers) {
		ObjectMapper mapper = new ObjectMapper();
		UUID validationPolicyId = null;
		X509Certificate clientCert = null;
		CertificateFactory cf = null;
		ByteArrayInputStream bais = null;
		String x5tS256 = null;
		String requestId = null;
		/*
		 * First, lets validate the request.
		 *
		 * Ensure we have validationPolicyId and clientCertificate
		 */
		if (null == request || null == request.validationPolicyId || null == request.x509Certificate) {
			throw new ServiceException("Request must include validationPolicyId and x509Certificate");
		}
		/*
		 * Check the validationPolicyId
		 */
		if (request.validationPolicyId.length() >= POL_SIZE_LIMIT) {
			throw new ServiceException("Size limit for validationPolicyId Object Identifier exceeded");
		} else {
			try {
				validationPolicyId = UUID.fromString(request.validationPolicyId);
			} catch (IllegalArgumentException e) {
				throw new ServiceException("validationPolicyId must be an Object Identifier");
			}
		}
		/*
		 * Check to see if we have the policy, otherwise throw an error
		 */
		ValidationPolicy valPol = ConfigurationPolicies.getPolicy(validationPolicyId.toString());
		if (null == valPol) {
			LOG.error("Invalid Policy Specified: " + validationPolicyId.toString());
			throw new ServiceException("Invalid Policy Specified");
		}

		/*
		 * Check the x509Certificate
		 */
		String pemCert = request.x509Certificate;
		try {
			if (pemCert.length() >= PEM_SIZE_LIMIT) {
				throw new ServiceException("Size limit for x509Certificate exceeded");
			}
			byte[] certBytes = null;
			try {
				certBytes = Base64.getDecoder().decode(pemCert);
			} catch (Throwable e) {
				LOG.error("Error decoding certificate, returning SERVICEFAIL", e);
				throw new ServiceException("Error decoding x509Certificate");
			}
			if (null != certBytes) {
				cf = CertificateFactory.getInstance("X509");
				bais = new ByteArrayInputStream(certBytes);
				clientCert = (X509Certificate) cf.generateCertificate(bais);
			} else {
				LOG.error("Error decoding certificate base64 (null result), returning SERVICEFAIL");
				throw new ServiceException("Error decoding x509Certificate");
			}
		} catch (CertificateException e) {
			LOG.error("Error decoding certificate, returning SERVICEFAIL", e);
			throw new ServiceException("Error decoding x509Certificate");
		}

		/*
		 * Derive x5t#S256
		 */
		x5tS256 = X509Util.x5tS256(clientCert);
		/*
		 * Derive requestId
		 */
		requestId = X509Util.strS256HexString(x5tS256 + ":" + valPol.validationPolicyId);

		/*
		 * Add metadata to the request via `additionalProperties` so we can log it.
		 */
		request.setAdditionalProperty("requestHeaders", headers);
		request.requestId = requestId;
		/*
		 * Log the request in JSON
		 */
		try {
			String output = mapper.writeValueAsString(request);
			LOG.info("{\"ValidationRequest\":" + output + "}");
		} catch (JsonGenerationException e) {
			LOG.error("Error converting POJO to JSON", e);
		} catch (JsonMappingException e) {
			LOG.error("Error converting POJO to JSON", e);
		} catch (IOException e) {
			LOG.error("Error converting POJO to JSON", e);
		}
		/*
		 * Check our cache for a response first, if cached, return immediately
		 */
		HTTPClientSingleton client = HTTPClientSingleton.getInstance();
		ElasticacheClient mcClient = client.getCacheClient();
		byte[] cachedResponse = mcClient.get(requestId);
		if (null != cachedResponse) {
			LOG.info("We found a cached response, attempting to return to client");
			String strResponse = new String(cachedResponse, StandardCharsets.UTF_8);
			try {
				response = mapper.readValue(strResponse, VssResponse.class);
			} catch (JsonMappingException e) {
				LOG.error("Error converting JSON to POJO", e);
			} catch (JsonProcessingException e) {
				LOG.error("Error converting JSON to POJO", e);
			}
			if (null != response) {
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
		}
		LOG.info("No cached response found, validating certificate per clients request");
		/*
		 * Validate, log, and; return the result
		 */
		Instant vNow = Instant.now();
		long lNow = vNow.toEpochMilli();
		Date dNow = new Date(lNow);
		response = ValidatePKIX.validate(clientCert, x5tS256, valPol, dNow);
		ValidationResult respResult = response.validationResult;
		/*
		 * Set validationTime and nextUpdate in the response
		 *
		 * Date Format now conforms to ISO 8601:
		 *
		 * http://xkcd.com/1179/
		 */
		SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		dFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		response.validationTime = dFormat.format(dNow);
		/*
		 * TODO: nextUpdate will be based on CRL, for now we will calculate a date that
		 * is one hour from now.
		 */
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dNow);
		calendar.add(Calendar.HOUR_OF_DAY, 1);
		response.nextUpdate = dFormat.format(calendar.getTime());
		String validationNow = X509Util.ISO8601DateString(dNow);
		if (respResult != null) {
			if (respResult instanceof Success) {
				/*
				 * validationTime should be used as Last-Modified
				 */
				response.requestId = requestId;
				response.validationTime = validationNow;
			} else {
				/*
				 * Set nextUpdate to null to inform the client that we will *not* perform any
				 * more operations on this requestId
				 */
				response.nextUpdate = null;
			}
		}
		String output = null;
		try {
			output = mapper.writeValueAsString(response);
			LOG.info("{\"ValidationResponse\":" + output + "}");
			/*
			 * Cache the response:
			 * 
			 * - CREATED for valid certificates, for 15 minutes
			 * - OK for invalid certificates, for 24 hours
			 * 
			 * TODO: Address invalidity reasons that may arise due to lack of intermediate
			 * or revocation data.
			 */
			if (respResult != null) {
				if (respResult instanceof Success) {
					LOG.info("Caching valid response with 15min TTL, with Key: " + requestId);
					mcClient.putWithTtl(requestId, 900, output.getBytes(StandardCharsets.UTF_8));
					/*
					 * Created response requires a URI to GET the entity, by requestId
					 */
					String getUri = BASE_URI + "/vss/v2/validate/getByRequestId/" + requestId;
					return ResponseEntity.created(URI.create(getUri))
							.cacheControl(CacheControl.maxAge(15, TimeUnit.MINUTES).cachePublic()).eTag(requestId).lastModified(vNow).body(response);
				} else if (respResult instanceof Fail) {
					/*
					 * We won't perform a validation operation on a certificate we've deemed invalid for a period of time:
					 * 
					 * - 24 Hours is the current default
					 * - The notAfter date *could* be used, but also abused via certificates that have no valid path
					 * 
					 * TODO: We should consider caching revoked certificate validations based on the certificate Expiration date.
					 */
					LOG.info("Caching invalid response with 24hr TTL, with Key: " + requestId);
					mcClient.putWithTtl(requestId, 86400, output.getBytes(StandardCharsets.UTF_8));
					return ResponseEntity.ok()
							.cacheControl(CacheControl.maxAge(24, TimeUnit.HOURS).cachePublic()).eTag(requestId).lastModified(vNow).body(response);
				} else {
					ResponseEntity.badRequest();
				}
			}
		} catch (JsonGenerationException e) {
			LOG.error("Error converting POJO to JSON", e);
		} catch (JsonMappingException e) {
			LOG.error("Error converting POJO to JSON", e);
		} catch (IOException e) {
			LOG.error("Error converting POJO to JSON", e);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@GetMapping(path = "/vss/v2/validate/getByRequestId/{requestId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@CrossOrigin(origins = "*")
	ResponseEntity<VssResponse> validate(@PathVariable String requestId, @RequestHeader Map<String, String> headers) {
		ObjectMapper mapper = new ObjectMapper();
		HTTPClientSingleton client = HTTPClientSingleton.getInstance();
		ElasticacheClient mcClient = client.getCacheClient();
		/*
		 * TODO: Make sure this endpoint can't be used to harvest non-VssResponse data
		 * from our cache.
		 * 
		 * For now, we will check to ensure the requestId is limited to a Hex SHA-256
		 * value @ 64 characters
		 */
		String headerJson = null;
		try {
			headerJson = mapper.writeValueAsString(headers);
		} catch (JsonProcessingException e) {
			LOG.error("Error converting POJO to JSON", e);
		}
		LOG.info("{\"getByRequestId\":\"" + requestId + "\",\"headers\":" + headerJson + "}");
		if (requestId.length() != 64) {
			LOG.error("Returning 404: Client request does not appear to be a legitimate requestId: \"" + requestId
					+ "\": {\"headers\":" + headerJson + "}");
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		} else {
			byte[] cachedResponse = mcClient.get(requestId);
			if (null != cachedResponse) {
				LOG.info("We found a cached response, attempting to return to client");
				String strResponse = new String(cachedResponse, StandardCharsets.UTF_8);
				try {
					response = mapper.readValue(strResponse, VssResponse.class);
				} catch (JsonMappingException e) {
					LOG.error("Error converting JSON to POJO", e);
				} catch (JsonProcessingException e) {
					LOG.error("Error converting JSON to POJO", e);
				}
				if (null != response) {
					Date lmDate = X509Util.ISO8601DateFromString(response.validationTime);
					return ResponseEntity.ok().cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic()).eTag(requestId)
							/*
							 * Serialize validationTime within the Cached response into the Last-Modified header.
							 */
							.lastModified(Instant.ofEpochMilli(lmDate.getTime())).body(response);
				} else {
					return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
				}
			} else {
				return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
			}
		}
	}

	/*
	 * TODO: Expand on error handling based on the exception or logic error
	 */
	@ExceptionHandler({ Exception.class })
	public ResponseEntity<Object> handleAll(Exception e, WebRequest request) {
		ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

}
