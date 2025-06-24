package org.keysupport.api.controller.vss;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.keysupport.api.ApiError;
import org.keysupport.api.LoggingUtil;
import org.keysupport.api.config.ServiceValidationPolicies;
import org.keysupport.api.controller.ServiceException;
import org.keysupport.api.pkix.ValidatePKIX;
import org.keysupport.api.pkix.X509Util;
import org.keysupport.api.pojo.vss.Fail;
import org.keysupport.api.pojo.vss.JsonX509Certificate;
import org.keysupport.api.pojo.vss.Success;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.keysupport.api.pojo.vss.ValidationResult;
import org.keysupport.api.pojo.vss.VssRequest;
import org.keysupport.api.pojo.vss.VssResponse;
import org.keysupport.api.pojo.vss.v1.V1InvalidityReason;
import org.keysupport.api.pojo.vss.v1.V1ResultByCertificate;
import org.keysupport.api.pojo.vss.v1.V1ResultByCertificateData;
import org.keysupport.api.pojo.vss.v1.V1TransactionResult;
import org.keysupport.api.pojo.vss.v1.V1VSSRequest;
import org.keysupport.api.pojo.vss.v1.V1VSSResponse;
import org.keysupport.api.pojo.vss.v1.V1ValidationFailureData;
import org.keysupport.api.pojo.vss.v1.V1ValidationResult;
import org.keysupport.api.pojo.vss.v1.V1ValidationSuccessData;
import org.keysupport.api.pojo.vss.v1.V1WantBack;
import org.keysupport.api.pojo.vss.v1.V1WantBackTypeToken;
import org.keysupport.api.pojo.vss.v1.V1X509CertificateList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "validate", description = "Validate a certificate based on the specified validation policy")
public class ValidateController {

	private final static Logger LOG = LoggerFactory.getLogger(ValidateController.class);

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
					+ "  \"validationPolicyId\": \"2.16.840.1.101.10.2.18.2.1.4\",\n"
					+ "  \"x509Certificate\": \"MIIEuTCCA6GgAwIBAgICBUwwDQYJKoZIhvcNAQELBQAwWzELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDEMMAoGA1UECxMDRG9EMQwwCgYDVQQLEwNQS0kxFjAUBgNVBAMTDURvRCBSb290IENBIDMwHhcNMjEwNjAxMTQxMTIzWhcNMjcwNjAyMTQxMTIzWjBaMQswCQYDVQQGEwJVUzEYMBYGA1UEChMPVS5TLiBHb3Zlcm5tZW50MQwwCgYDVQQLEwNEb0QxDDAKBgNVBAsTA1BLSTEVMBMGA1UEAxMMRE9EIElEIENBLTY1MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnkK9OCQ+D0b/7SLsEs0LCElhKIzGtiZDBw9VLqCaxTHlxaYEPV/B/X9NGoP5PE4ToBOSramLCMPbwjadhNk8O0gEInZCuEzV17vvx6O4xg+FJ9OO76LU1KeXJnnvX1YnCKz3uxrn3sw1jQugEEd1yPwKoHMjJ2Sr7Vgrm1e983EgiRint9lble7x/MDLvEZDELeeqhPZvRiz1qwVG+/p2ks980qFLFLl1INOUSPnSLIbafg7cWE9yTC5i99s4pJnP2ThyBv6JsgFzbbj9FEYGyh75GjIMEv8ulcQ3ATOSBREUPzrd6sQmideeqvxXrDYxo8Qel6brZiti+5vEr3OzQIDAQABo4IBhjCCAYIwHwYDVR0jBBgwFoAUbIqUonexgHIdgXoWqvLczmbuRcAwHQYDVR0OBBYEFGLgSDhWbW9rJb67w4hYsaycQ8lbMA4GA1UdDwEB/wQEAwIBhjBnBgNVHSAEYDBeMAsGCWCGSAFlAgELJDALBglghkgBZQIBCycwCwYJYIZIAWUCAQsqMAsGCWCGSAFlAgELOzAMBgpghkgBZQMCAQMNMAwGCmCGSAFlAwIBAxEwDAYKYIZIAWUDAgEDJzASBgNVHRMBAf8ECDAGAQH/AgEAMAwGA1UdJAQFMAOAAQAwNwYDVR0fBDAwLjAsoCqgKIYmaHR0cDovL2NybC5kaXNhLm1pbC9jcmwvRE9EUk9PVENBMy5jcmwwbAYIKwYBBQUHAQEEYDBeMDoGCCsGAQUFBzAChi5odHRwOi8vY3JsLmRpc2EubWlsL2lzc3VlZHRvL0RPRFJPT1RDQTNfSVQucDdjMCAGCCsGAQUFBzABhhRodHRwOi8vb2NzcC5kaXNhLm1pbDANBgkqhkiG9w0BAQsFAAOCAQEAF8Uj33K0ZM9adtfd8IM2ebqwgbgRxi22Pb6bKkKOkGV2NU4wMckpuRpUrQGJmy6CIXZ84QWz9DZSNAU0nyHXB6PLbSV0jnzKygWO7IOv83M6dcnCG8QUP1o20V0NGhzNBEtKjxWENZCYHEruxm+2rB+MBngPhkBgdni2npetHX2e1cmsgMS6G1PUh2idhZ8Mpdofnr+V0GuKLpwiNz3hLnKehl2Bs6aHG2IIOm/PdzvsKCP2eiGzS3SiiCf6fukYoYBNedL8fHfFNyM4UPNgc4eG+bu0GJK4wKPVjiX7xYDdGaYZ2m4Y++zrKuMq+Oar6DQGq3SERMAZCDYsEt3z2g==\"\n"
					+ "}"),
			@ExampleObject(name = "A validation request using an revoked end-entity certificate", value = "{\n"
					+ "  \"validationPolicyId\": \"2.16.840.1.101.10.2.18.2.1.4\",\n"
					+ "  \"x509Certificate\": \"MIIKNzCCCB+gAwIBAgIEYIWQKTANBgkqhkiG9w0BAQsFADCBuzETMBEGCgmSJomT8ixkARkWA3NidTEVMBMGCgmSJomT8ixkARkWBXN0YXRlMRYwFAYDVQQDDA1Db25maWd1cmF0aW9uMREwDwYDVQQDDAhTZXJ2aWNlczEcMBoGA1UEAwwTUHVibGljIEtleSBTZXJ2aWNlczEMMAoGA1UEAwwDQUlBMTYwNAYDVQQDDC1VLlMuIERlcGFydG1lbnQgb2YgU3RhdGUgQUQgSGlnaCBBc3N1cmFuY2UgQ0EwHhcNMjIwNjI5MTIzNjA3WhcNMjUwNjI5MTMwNjA3WjCBljETMBEGCgmSJomT8ixkARkWA3NidTEVMBMGCgmSJomT8ixkARkWBXN0YXRlMRswGQYKCZImiZPyLGQBGRYLYXBwc2VydmljZXMxHDAaBgNVBAsME0VudGVycHJpc2UgU2VydmljZXMxDDAKBgNVBAsMA1BLSTEMMAoGA1UECwwDVEFQMREwDwYDVQQDDAhUQVBUZXN0NDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALScG/lyri4gpn3ZEX1KCmulT7zpjGjhZtB+2nzd6LM3ulj5PavgKE7Y2kOs5mge691a6RaW89u/xubvHxDL/FEzd2jxvfjJST64bejhVjqIc1zt/E1YZlv1ClK927wWqmq4PsyP63FV1UyEOO9HvrUZ5AFI/n1HMJm+zuYVXiPCgUMOmqAkceN5GznoN9AVbNgkz2tI/3PqYCtfpy6CYs+LCpXyx7/Wcf65W3PJy6xr6hVUBSKUCryl76A5xS+eM5cvF1C0BfSPBtQBbpdbGJTKs3oh4DhLNfFbBPG6Es6LicHAHpQywiCE6X0ia+btzIe4CJoOPn1NGHwtGViBJzsCAwEAAaOCBWQwggVgMA4GA1UdDwEB/wQEAwIGwDApBgNVHSUEIjAgBggrBgEFBQcDAgYKKwYBBAGCNxQCAgYIKwYBBQUHAwQwQQYDVR0gBDowODAMBgpghkgBZQMCAQYBMAwGCmCGSAFlAwIBBgIwDAYKYIZIAWUDAgEGAzAMBgpghkgBZQMCAQYEMIICMgYIKwYBBQUHAQEEggIkMIICIDBEBggrBgEFBQcwAoY4aHR0cDovL2NybHMucGtpLnN0YXRlLmdvdi9BSUEvQ2VydHNJc3N1ZWRUb0RvU0FESEFDQS5wN2MwgcgGCCsGAQUFBzAChoG7bGRhcDovL2Rpci5wa2kuc3RhdGUuZ292L2NuPVUuUy4lMjBEZXBhcnRtZW50JTIwb2YlMjBTdGF0ZSUyMEFEJTIwSGlnaCUyMEFzc3VyYW5jZSUyMENBLGNuPUFJQSxjbj1QdWJsaWMlMjBLZXklMjBTZXJ2aWNlcyxjbj1TZXJ2aWNlcyxjbj1Db25maWd1cmF0aW9uLGRjPXN0YXRlLGRjPXNidT9jQUNlcnRpZmljYXRlO2JpbmFyeTCBzwYIKwYBBQUHMAKGgcJsZGFwOi8vZGlyLnBraS5zdGF0ZS5nb3YvY249VS5TLiUyMERlcGFydG1lbnQlMjBvZiUyMFN0YXRlJTIwQUQlMjBIaWdoJTIwQXNzdXJhbmNlJTIwQ0EsY249QUlBLGNuPVB1YmxpYyUyMEtleSUyMFNlcnZpY2VzLGNuPVNlcnZpY2VzLGNuPUNvbmZpZ3VyYXRpb24sZGM9c3RhdGUsZGM9c2J1P2Nyb3NzQ2VydGlmaWNhdGVQYWlyO2JpbmFyeTA7BggrBgEFBQcwAYYvaHR0cDovL29jc3AucGtpLnN0YXRlLmdvdi9PQ1NQL0RvU09DU1BSZXNwb25kZXIwLQYDVR0RBCYwJKAiBgorBgEEAYI3FAIDoBQMElRhcFRlc3Q0QHN0YXRlLmdvdjCCAgEGA1UdHwSCAfgwggH0MIIBFaCCARGgggENhjFodHRwOi8vY3Jscy5wa2kuc3RhdGUuZ292L2NybHMvRG9TQURQS0lIQUNBLTEuY3JshoHXbGRhcDovL2Rpci5wa2kuc3RhdGUuZ292L2NuPVdpbkNvbWJpbmVkMSxjbj1VLlMuJTIwRGVwYXJ0bWVudCUyMG9mJTIwU3RhdGUlMjBBRCUyMEhpZ2glMjBBc3N1cmFuY2UlMjBDQSxjbj1BSUEsY249UHVibGljJTIwS2V5JTIwU2VydmljZXMsY249U2VydmljZXMsY249Q29uZmlndXJhdGlvbixkYz1zdGF0ZSxkYz1zYnU/Y2VydGlmaWNhdGVSZXZvY2F0aW9uTGlzdDtiaW5hcnkwgdiggdWggdKkgc8wgcwxEzARBgoJkiaJk/IsZAEZFgNzYnUxFTATBgoJkiaJk/IsZAEZFgVzdGF0ZTEWMBQGA1UEAwwNQ29uZmlndXJhdGlvbjERMA8GA1UEAwwIU2VydmljZXMxHDAaBgNVBAMME1B1YmxpYyBLZXkgU2VydmljZXMxDDAKBgNVBAMMA0FJQTE2MDQGA1UEAwwtVS5TLiBEZXBhcnRtZW50IG9mIFN0YXRlIEFEIEhpZ2ggQXNzdXJhbmNlIENBMQ8wDQYDVQQDDAZDUkw4NzQwKwYDVR0QBCQwIoAPMjAyMjA2MjkxMjM2MDdagQ8yMDI0MDgwNDE3MDYwN1owHwYDVR0jBBgwFoAUheMozyV6pgGNxW/+xVJyqp6jTE4wHQYDVR0OBBYEFJEQPx/gZOO50vXJRGxBPNbmm2XEMAkGA1UdEwQCMAAwDQYJKoZIhvcNAQELBQADggIBAFxgss5EJJ0dBpgN3Rh+F2o65DxUrRYSxK++wPQuJ/YJmyUf2ns0581nfAVLDutuL0MjDdZqeT2WkUNTvvVVcYrHvainF9ZTwHpZ0XNhAOJ6IbVGiz1bhQBAi9MSMenAgnXYZ18IFUq8Sg/EyutvDlKtydMPzqJXJW/f7+0r95HJnuvVZPhsB3YdHotxQ0GXAdQhRLKHWR2Grx/NcWtFG9mMNsMez0DeY8EuOqllx34gCvgHwQ/UP8Ng2oJBkRMMEVKCzCnQVHEirpV0PM8LRrjeLeu2+Zsx1Cwf/AhgcYQIhCLWJjXyBKeD8joVmyuWBrsP4JPrxgp/tSMDhfOuS+w3pllVHK53CyXWigxXdolEeePUpAh3jmlm0nK3vUIbz0hHJf1bwhJJAEl+F1QMiDTHfbB2xFHnYlN48AU6hK6g9eG49t05NcEbPCTCUjE+T6gi+qqvjXeZ+685fTHjgeBCaKSord0RgUhYrwHOYeErW8Oih4SB+5ryl5pvKAzwvgJ34LsTxcVK1QOZsIovTIH1pWEYBhd9YWWF6zevQKkxcnMRa+YOGIzPJwH/RMr86mEoU02JGCCoNw89raetcSNBt7ikDiJ5EWcrb7VmqXCEEwZl8mea7I7/NhktQQC5wqq9uArf/mU279MzfHSxH1VosCLWBXWxUcWhKW6eMxry\"\n"
					+ "}"),
			@ExampleObject(name = "A validation request using a revoked end-entity certificate from a different CA", value = "{\n"
					+ "  \"validationPolicyId\": \"2.16.840.1.101.10.2.18.2.1.4\",\n"
					+ "  \"x509Certificate\": \"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\"\n"
					+ "}"),
			@ExampleObject(name = "A validation request using a valid end-entity certificate", value = "{\n"
					+ "  \"validationPolicyId\": \"2.16.840.1.101.10.2.18.2.1.4\",\n"
					+ "  \"x509Certificate\": \"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\"\n"
					+ "}"),
			@ExampleObject(name = "A validation request using an expired end-entity certificate", value = "{\n"
					+ "  \"validationPolicyId\": \"2.16.840.1.101.10.2.18.2.1.4\",\n"
					+ "  \"x509Certificate\": \"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\"\n"
					+ "}"),
			@ExampleObject(name = "A validation request using a public trust certificate that is expired", value = "{\n"
					+ "  \"validationPolicyId\": \"2.16.840.1.101.10.2.18.2.1.4\",\n"
					+ "  \"x509Certificate\": \"MIIFYjCCBEqgAwIBAgIRAOgIWMWQtieIECed/Q2JONIwDQYJKoZIhvcNAQELBQAwRjELMAkGA1UEBhMCVVMxIjAgBgNVBAoTGUdvb2dsZSBUcnVzdCBTZXJ2aWNlcyBMTEMxEzARBgNVBAMTCkdUUyBDQSAxRDQwHhcNMjIxMDEwMjAwMDU2WhcNMjMwMTA4MjAwMDU1WjAZMRcwFQYDVQQDEw5rZXlzdXBwb3J0Lm9yZzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALftackigK3dmFReOqr4txPVJrdTPRMbsz3+4ueBGtBd2OIKcFXDi8apCf76Fh8WBheDVOyDzUhkag+mTnPw0BXqYNyFntPcwosDkv7kviJgF4KmSWf3myTYmHoc0CAqoRqYsfTFkB7fK83OBnevqSZ8pgc2X7h6qSgx9Z/0dprl+2mWndIB82SZSCzpuaGXs5SPFDZVrB8Y7fUWqu1nPaTLfOzIlAIf8SHf1+f81D0Mg1rm/rAAi9HFwx+fLPnFvZUUWeErPEi9JmBJzU+TJdF3dDRldtJVgFdQXcvMMwgSjmlL+ToOwf/VRU/usIVGxLI/qoxLj+Y97ht5tzMILXMCAwEAAaOCAnYwggJyMA4GA1UdDwEB/wQEAwIFoDATBgNVHSUEDDAKBggrBgEFBQcDATAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBQHEqvLbOftuY4bf2Ojy3InQEZctzAfBgNVHSMEGDAWgBQl4hgOsleRlCrl1F2GkIPeU7O4kjB4BggrBgEFBQcBAQRsMGowNQYIKwYBBQUHMAGGKWh0dHA6Ly9vY3NwLnBraS5nb29nL3MvZ3RzMWQ0L1NRMUZ6enViUFlNMDEGCCsGAQUFBzAChiVodHRwOi8vcGtpLmdvb2cvcmVwby9jZXJ0cy9ndHMxZDQuZGVyMBkGA1UdEQQSMBCCDmtleXN1cHBvcnQub3JnMCEGA1UdIAQaMBgwCAYGZ4EMAQIBMAwGCisGAQQB1nkCBQMwPAYDVR0fBDUwMzAxoC+gLYYraHR0cDovL2NybHMucGtpLmdvb2cvZ3RzMWQ0L0oyajNSQ2lFeTA0LmNybDCCAQUGCisGAQQB1nkCBAIEgfYEgfMA8QB3AHoyjFTYty22IOo44FIe6YQWcDIThU070ivBOlejUutSAAABg8PAFU0AAAQDAEgwRgIhAInKXSrciK8OHtFWvQienDeIwBGCfDojh62EeoD62ngyAiEA6HO5HRCE00qCIKNCx6CuS9gDkvgrNm4xIS+gPc5Z8i8AdgCt9776fP8QyIudPZwePhhqtGcpXc+xDCTKhYY069yCigAAAYPDwBVHAAAEAwBHMEUCIQDOGktHy094r8OdqDhS7IPj44qJ1V9RRdR2ZEq4Sz5WOwIgcAlszeHnUNnp/jlDJPk1vIBJxh8/lokFwDBFiUU1EhAwDQYJKoZIhvcNAQELBQADggEBAGiaDoyFu4aLlLvsYnIVlTvf6bfUZ0xf3F9gSyjJBxFxFMF4BTiWWbCXGCpBT7+hHLSvq2fKKzKn2bR17XnDJkcVNBA5kxc9MQu6YE26+QkV4nk23hNidmaRimuiq4FLivsp/9gDrU+q5lutXP6n7EYI0ibXtuGk+Tx/qW9xvT4V9snhhw+4ks0Yw0B3AcdwNqRgmKYOftOLDPivLe5+lTmxbiMIPXSvkXzGVDFwj8/D8APp173Jg6XmHPHC9DSYF33e0q3nmmKQOV3PjR4mlFkY5Ewv9pKLJRnbwp01CArMs2YYdSqERZnLCH69lcHM/kuEKgMmdgLnEky2Xa03c84=\"\n"
					+ "}"),
			@ExampleObject(name = "A validation request using another random FPKI certificate", value = "{\n"
					+ "  \"validationPolicyId\": \"2.16.840.1.101.10.2.18.2.1.4\",\n"
					+ "  \"x509Certificate\": \"MIIIpTCCBo2gAwIBAgIEYkyVyDANBgkqhkiG9w0BAQsFADCBgjELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDEjMCEGA1UECxMaRGVwYXJ0bWVudCBvZiB0aGUgVHJlYXN1cnkxIjAgBgNVBAsTGUNlcnRpZmljYXRpb24gQXV0aG9yaXRpZXMxEDAOBgNVBAsTB09DSU8gQ0EwHhcNMjUwMTIxMTQwOTM4WhcNMjgwMTIxMTQzODE3WjCBoTELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDEjMCEGA1UECxMaRGVwYXJ0bWVudCBvZiB0aGUgVHJlYXN1cnkxHTAbBgNVBAsTFERlcGFydG1lbnRhbCBPZmZpY2VzMQ8wDQYDVQQLEwZQZW9wbGUxIzAOBgNVBAUTBzExNzI4MTkwEQYDVQQDEwpNYXJrbyBFbGV6MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwRxbuRLUu97/wekTDESmPRiHNyBaAvf327beedEyuPPI/kBfWR8uO5b4krdtm/a1YK63vZjxcep//CSoUSnanWMhULa2Yq4y9gJIJpdQam648a8l3MXQ39YiY2XLszVYxQ+JhowVhjRWELw6QSgaQ673LDbVObA6Fna1lQ1SaS9XS1hpgEVJLGMVi25OxXuRXrpp4hkeh6DRlnJP3Otwk/5cAr5pzSJxP3zPX8BP6U3QpD3tfWTY5tcXXwFnrGwUT69kh22AQkeccAa86/NZVaNLXrnD6u1Y3D1Mj+2+aDu9wU6uTcZ1gLUFQkTPCgwz91HuQ98XAFmgocB4WHDr4QIDAQABo4IEADCCA/wwDgYDVR0PAQH/BAQDAgeAMBcGA1UdIAQQMA4wDAYKYIZIAWUDAgEDDTAyBgNVHSUEKzApBgorBgEEAYI3FAICBggrBgEFBQcDAgYHKwYBBQIDBAYIKwYBBQUHAxUwEAYJYIZIAWUDBgkBBAMBAf8wggETBggrBgEFBQcBAQSCAQUwggEBMDMGCCsGAQUFBzAChidodHRwOi8vcGtpLnRyZWFzdXJ5Lmdvdi90b2NhX2VlX2FpYS5wN2MwgaMGCCsGAQUFBzAChoGWbGRhcDovL2xkYXAudHJlYXN1cnkuZ292L291PU9DSU8lMjBDQSxvdT1DZXJ0aWZpY2F0aW9uJTIwQXV0aG9yaXRpZXMsb3U9RGVwYXJ0bWVudCUyMG9mJTIwdGhlJTIwVHJlYXN1cnksbz1VLlMuJTIwR292ZXJubWVudCxjPVVTP2NBQ2VydGlmaWNhdGU7YmluYXJ5MCQGCCsGAQUFBzABhhhodHRwOi8vb2NzcC50cmVhc3VyeS5nb3YwgaAGA1UdEQSBmDCBlaAiBgorBgEEAYI3FAIDoBQMEkVMRVpNQERPLlRSRUFTLkdPVoEXTWFya28uRWxlekB0cmVhc3VyeS5nb3agJwYIYIZIAWUDBgagGwQZ0gIYWCEIrNySBM2haFoBDXkJkDCCAhDD84YtdXJuOnV1aWQ6ZjM3MDgwN2YtNTFmZS0xMjRiLTg0N2MtYmJjMDRkMDY3ODdmMIIBjwYDVR0fBIIBhjCCAYIwKqAooCaGJGh0dHA6Ly9wa2kudHJlYXN1cnkuZ292L09DSU9fQ0E2LmNybDCCAVKgggFOoIIBSqSBlzCBlDELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDEjMCEGA1UECxMaRGVwYXJ0bWVudCBvZiB0aGUgVHJlYXN1cnkxIjAgBgNVBAsTGUNlcnRpZmljYXRpb24gQXV0aG9yaXRpZXMxEDAOBgNVBAsTB09DSU8gQ0ExEDAOBgNVBAMTB0NSTDM5NTeGga1sZGFwOi8vbGRhcC50cmVhc3VyeS5nb3YvY249Q1JMMzk1NyxvdT1PQ0lPJTIwQ0Esb3U9Q2VydGlmaWNhdGlvbiUyMEF1dGhvcml0aWVzLG91PURlcGFydG1lbnQlMjBvZiUyMHRoZSUyMFRyZWFzdXJ5LG89VS5TLiUyMEdvdmVybm1lbnQsYz1VUz9jZXJ0aWZpY2F0ZVJldm9jYXRpb25MaXN0O2JpbmFyeTAfBgNVHSMEGDAWgBQ4gB93Z+mXNTPusF4bByYIlidF2jAdBgNVHQ4EFgQUPOACdmtBnsdhLHd9YPQLhXh8YHswDQYJKoZIhvcNAQELBQADggIBAD5xSngN1gC8DRHtDPY/eS0lSkqnT3jwANmddEN/XcTkw4WVskVf/ZuyYwySoKyxDn6jkyyLIF19mcTRBHz8Bm8pArRMFqx/N/sN2JFcdkEL44ZmqbOiEizpfPLwR6RwW5ecq3lgIL88wTpz9T+jg7QmYOP6JVl+ArhIqIBKyylWFryZF/4mBkdIGmlj8pCqAQha+Iyd/LNsPUIoAR//TB7DhrMID3lsAUnHTKlgTgzWU4mbUgKwLYAhIJFZW8DAjUdjCJQ0HC9ZXDyHT8ufVB08HCVXB+WQihllytZ77enj0b9U1sor/I7a6Pa93tDpITx+Y/xYLAPdwS3a5rHQT9oojnmxD1ipgG2jxGjwY0+meY3RX07no5iFm7QpNla7VQZ3wEh6LHSgx295/eKkItGuhAAuNhjrauXhl5YhXyEa0mFLHTn9p9zgBoy1iUpGJFw3xz+qJlBoiZ6Ve100mE16rMIK2nhyfxn+U6NGsHbzY6SMTGJ+zJV07AWWPQ2qTYZdYkGBh9UCl1IdYpsa8kpU4SSpy/5NdJ5n/x6Oi//5CuEzyTmLdT2NETJw7KTGGB6CLf+CFubNbUwUOFLWi+BZebuwEd9Mz8M+dW0wxUEyI8SsTZMBEaRACtBvAjsnfXFCPlKeEzwZoMZRbaC80AB/EwRBmhAdHE8ZtRBgZ/Gp\"\n"
					+ "}"),
			@ExampleObject(name = "A validation request using Valid - Recently Issued - 2025 FCPCAG2 Signed Treasury Root - IAL3/AAL3 Policy", value = "{\n"
					+ "  \"validationPolicyId\": \"2.16.840.1.101.10.2.18.2.1.4\",\n"
					+ "  \"x509Certificate\": \"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\"\n"
					+ "}") }))
	@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = VssResponse.class)))
	@ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ApiError.class)))
	@CrossOrigin(origins = "*")
	ResponseEntity<VssResponse> validate(@RequestBody VssRequest request, @RequestHeader Map<String, String> headers) {
		ASN1ObjectIdentifier validationPolicyId = null;
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
				validationPolicyId = new ASN1ObjectIdentifier(request.validationPolicyId);
			} catch (IllegalArgumentException e) {
				LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Error decoding Object Identifier", "validationPolicyId", request.validationPolicyId, "stacktrace", LoggingUtil.stackTraceToString(e))));
				throw new ServiceException("validationPolicyId must be an Object Identifier");
			}
		}
		/*
		 * Check to see if we have the policy, otherwise throw an error
		 */
		ValidationPolicy valPol = ServiceValidationPolicies.getPolicy(validationPolicyId.toString());
		if (null == valPol) {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Invalid Policy Specified: " + validationPolicyId.toString())));
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
				LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Error decoding certificate, returning SERVICEFAIL", "stacktrace", LoggingUtil.stackTraceToString(e))));
				throw new ServiceException("Error decoding x509Certificate");
			}
			if (null != certBytes) {
				cf = CertificateFactory.getInstance("X509");
				bais = new ByteArrayInputStream(certBytes);
				clientCert = (X509Certificate) cf.generateCertificate(bais);
			} else {
				LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Error decoding certificate base64 (null result), returning SERVICEFAIL")));
				throw new ServiceException("Error decoding x509Certificate");
			}
		} catch (CertificateException e) {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Error decoding certificate, returning SERVICEFAIL", "stacktrace", LoggingUtil.stackTraceToString(e))));
			throw new ServiceException("Error decoding x509Certificate");
		}
		/*
		 * Derive x5t#S256
		 */
		x5tS256 = X509Util.x5tS256(clientCert);
		/*
		 * Derive requestId.
		 *
		 * The client can also derive the requestId in order to attempt a GET request,
		 * before POST, *if* this implementation supports caching.
		 *
		 * This would yield a much faster response from the cache, or; a 404 if not
		 * cached.
		 *
		 * - Client derives s5t#S256, which is the base64 encoded, SHA-256 digest of the
		 * certificate. - Client digests the UTF-8 value of of the String:
		 * "<x5t#S256 Base64 Value>:<validationPolicyId>"
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
		LOG.info(LoggingUtil.pojoToJson(request));
		/*
		 * Validate, log, and; return the result
		 */
		Instant vNow = Instant.now();
		long lNow = vNow.toEpochMilli();
		Date dNow = new Date(lNow);
		response = ValidatePKIX.validate(clientCert, x5tS256, valPol, dNow);
		ValidationResult respResult = response.validationResult;
		response.validationTime = X509Util.ISO8601DateString(dNow);
		/*
		 * nextUpdate is based on the validation policy.
		 */
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dNow);
		response.nextUpdate = X509Util.ISO8601DateString(calendar.getTime());
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
		if (respResult != null) {
			if (respResult instanceof Success) {
				LOG.info(LoggingUtil.pojoToJson(response));
				return ResponseEntity.ok().body(response);
			} else if (respResult instanceof Fail) {
				LOG.info(LoggingUtil.pojoToJson(response));
				return ResponseEntity.ok().body(response);
			} else {
				ResponseEntity.badRequest();
			}
		}
		LOG.info(LoggingUtil.pojoToJson(response));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/*
	 * Supports V1 endpoint logging so it may be filtered if desired
	 * 
	 * This logger name corresponds to the `validateV1` method below
	 */
	private final static Logger V1LOG = LoggerFactory.getLogger(ValidateController.class.getName().concat(".validateV1"));

	@PostMapping(path = "/vss/rest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Certificate Validation Request", required = true, content = @Content(schema = @Schema(implementation = VssRequest.class), mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
			@ExampleObject(name = "A validation request using a valid end-entity certificate", value = "{\n"
					+ "    \"validationPolicy\": \"2.16.840.1.101.10.2.18.2.1.4\",\n"
					+ "    \"wantBackList\": [\n"
					+ "        {\n"
					+ "            \"wantBackTypeToken\": \"certPath\"\n"
					+ "        }\n"
					+ "    ],\n"
					+ "    \"x509CertificateList\": [\n"
					+ "        {\n"
					+ "            \"x509Certificate\": \"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\"\n"
					+ "        }\n"
					+ "    ]\n"
					+ "}"),
			@ExampleObject(name = "A validation request using another random FPKI certificate", value = "{\n"
					+ "    \"validationPolicy\": \"2.16.840.1.101.10.2.18.2.1.4\",\n"
					+ "    \"wantBackList\": [\n"
					+ "        {\n"
					+ "            \"wantBackTypeToken\": \"certPath\"\n"
					+ "        }\n"
					+ "    ],\n"
					+ "    \"x509CertificateList\": [\n"
					+ "        {\n"
					+ "            \"x509Certificate\": \"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\"\n"
					+ "        }\n"
					+ "    ]\n"
					+ "}"),
			@ExampleObject(name = "A batch validation request both certificates", value = "{\n"
					+ "    \"validationPolicy\": \"2.16.840.1.101.10.2.18.2.1.4\",\n"
					+ "    \"wantBackList\": [\n"
					+ "        {\n"
					+ "            \"wantBackTypeToken\": \"certPath\"\n"
					+ "        }\n"
					+ "    ],\n"
					+ "    \"x509CertificateList\": [\n"
					+ "        {\n"
					+ "            \"x509Certificate\": \"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\"\n"
					+ "        },\n"
					+ "        {\n"
					+ "            \"x509Certificate\": \"MIIIpTCCBo2gAwIBAgIEYkyVyDANBgkqhkiG9w0BAQsFADCBgjELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDEjMCEGA1UECxMaRGVwYXJ0bWVudCBvZiB0aGUgVHJlYXN1cnkxIjAgBgNVBAsTGUNlcnRpZmljYXRpb24gQXV0aG9yaXRpZXMxEDAOBgNVBAsTB09DSU8gQ0EwHhcNMjUwMTIxMTQwOTM4WhcNMjgwMTIxMTQzODE3WjCBoTELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDEjMCEGA1UECxMaRGVwYXJ0bWVudCBvZiB0aGUgVHJlYXN1cnkxHTAbBgNVBAsTFERlcGFydG1lbnRhbCBPZmZpY2VzMQ8wDQYDVQQLEwZQZW9wbGUxIzAOBgNVBAUTBzExNzI4MTkwEQYDVQQDEwpNYXJrbyBFbGV6MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwRxbuRLUu97/wekTDESmPRiHNyBaAvf327beedEyuPPI/kBfWR8uO5b4krdtm/a1YK63vZjxcep//CSoUSnanWMhULa2Yq4y9gJIJpdQam648a8l3MXQ39YiY2XLszVYxQ+JhowVhjRWELw6QSgaQ673LDbVObA6Fna1lQ1SaS9XS1hpgEVJLGMVi25OxXuRXrpp4hkeh6DRlnJP3Otwk/5cAr5pzSJxP3zPX8BP6U3QpD3tfWTY5tcXXwFnrGwUT69kh22AQkeccAa86/NZVaNLXrnD6u1Y3D1Mj+2+aDu9wU6uTcZ1gLUFQkTPCgwz91HuQ98XAFmgocB4WHDr4QIDAQABo4IEADCCA/wwDgYDVR0PAQH/BAQDAgeAMBcGA1UdIAQQMA4wDAYKYIZIAWUDAgEDDTAyBgNVHSUEKzApBgorBgEEAYI3FAICBggrBgEFBQcDAgYHKwYBBQIDBAYIKwYBBQUHAxUwEAYJYIZIAWUDBgkBBAMBAf8wggETBggrBgEFBQcBAQSCAQUwggEBMDMGCCsGAQUFBzAChidodHRwOi8vcGtpLnRyZWFzdXJ5Lmdvdi90b2NhX2VlX2FpYS5wN2MwgaMGCCsGAQUFBzAChoGWbGRhcDovL2xkYXAudHJlYXN1cnkuZ292L291PU9DSU8lMjBDQSxvdT1DZXJ0aWZpY2F0aW9uJTIwQXV0aG9yaXRpZXMsb3U9RGVwYXJ0bWVudCUyMG9mJTIwdGhlJTIwVHJlYXN1cnksbz1VLlMuJTIwR292ZXJubWVudCxjPVVTP2NBQ2VydGlmaWNhdGU7YmluYXJ5MCQGCCsGAQUFBzABhhhodHRwOi8vb2NzcC50cmVhc3VyeS5nb3YwgaAGA1UdEQSBmDCBlaAiBgorBgEEAYI3FAIDoBQMEkVMRVpNQERPLlRSRUFTLkdPVoEXTWFya28uRWxlekB0cmVhc3VyeS5nb3agJwYIYIZIAWUDBgagGwQZ0gIYWCEIrNySBM2haFoBDXkJkDCCAhDD84YtdXJuOnV1aWQ6ZjM3MDgwN2YtNTFmZS0xMjRiLTg0N2MtYmJjMDRkMDY3ODdmMIIBjwYDVR0fBIIBhjCCAYIwKqAooCaGJGh0dHA6Ly9wa2kudHJlYXN1cnkuZ292L09DSU9fQ0E2LmNybDCCAVKgggFOoIIBSqSBlzCBlDELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDEjMCEGA1UECxMaRGVwYXJ0bWVudCBvZiB0aGUgVHJlYXN1cnkxIjAgBgNVBAsTGUNlcnRpZmljYXRpb24gQXV0aG9yaXRpZXMxEDAOBgNVBAsTB09DSU8gQ0ExEDAOBgNVBAMTB0NSTDM5NTeGga1sZGFwOi8vbGRhcC50cmVhc3VyeS5nb3YvY249Q1JMMzk1NyxvdT1PQ0lPJTIwQ0Esb3U9Q2VydGlmaWNhdGlvbiUyMEF1dGhvcml0aWVzLG91PURlcGFydG1lbnQlMjBvZiUyMHRoZSUyMFRyZWFzdXJ5LG89VS5TLiUyMEdvdmVybm1lbnQsYz1VUz9jZXJ0aWZpY2F0ZVJldm9jYXRpb25MaXN0O2JpbmFyeTAfBgNVHSMEGDAWgBQ4gB93Z+mXNTPusF4bByYIlidF2jAdBgNVHQ4EFgQUPOACdmtBnsdhLHd9YPQLhXh8YHswDQYJKoZIhvcNAQELBQADggIBAD5xSngN1gC8DRHtDPY/eS0lSkqnT3jwANmddEN/XcTkw4WVskVf/ZuyYwySoKyxDn6jkyyLIF19mcTRBHz8Bm8pArRMFqx/N/sN2JFcdkEL44ZmqbOiEizpfPLwR6RwW5ecq3lgIL88wTpz9T+jg7QmYOP6JVl+ArhIqIBKyylWFryZF/4mBkdIGmlj8pCqAQha+Iyd/LNsPUIoAR//TB7DhrMID3lsAUnHTKlgTgzWU4mbUgKwLYAhIJFZW8DAjUdjCJQ0HC9ZXDyHT8ufVB08HCVXB+WQihllytZ77enj0b9U1sor/I7a6Pa93tDpITx+Y/xYLAPdwS3a5rHQT9oojnmxD1ipgG2jxGjwY0+meY3RX07no5iFm7QpNla7VQZ3wEh6LHSgx295/eKkItGuhAAuNhjrauXhl5YhXyEa0mFLHTn9p9zgBoy1iUpGJFw3xz+qJlBoiZ6Ve100mE16rMIK2nhyfxn+U6NGsHbzY6SMTGJ+zJV07AWWPQ2qTYZdYkGBh9UCl1IdYpsa8kpU4SSpy/5NdJ5n/x6Oi//5CuEzyTmLdT2NETJw7KTGGB6CLf+CFubNbUwUOFLWi+BZebuwEd9Mz8M+dW0wxUEyI8SsTZMBEaRACtBvAjsnfXFCPlKeEzwZoMZRbaC80AB/EwRBmhAdHE8ZtRBgZ/Gp\"\n"
					+ "        }\n"
					+ "    ]\n"
					+ "}")}))
	@CrossOrigin(origins = "*")
	@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = V1VSSResponse.class)))
	ResponseEntity<V1VSSResponse> validateV1(@RequestBody V1VSSRequest request, @RequestHeader Map<String, String> headers) {
		/*
		 * Log the request
		 */
		V1LOG.info(LoggingUtil.pojoToJson(request));
		/*
		 * Process v1 request
		 */
		V1VSSResponse response = new V1VSSResponse();
		V1TransactionResult txResult = new V1TransactionResult();
		/*
		 * Ensure we have validationPolicy, wantBackList, and x509CertificateList
		 */
		if (null == request || null == request.validationPolicy || null == request.x509CertificateList
				|| null == request.wantBackList) {
			txResult.transactionResultToken = "SERVICEFAIL";
			txResult.transactionResultText = "Request must include validationPolicy, wantBackList, and x509CertificateList";
			response.transactionResult = txResult;
			V1LOG.warn(LoggingUtil.pojoToJson(Map.of("error", "Request is not a valid v1 request, returning SERVICEFAIL")));
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		boolean certPathWantBack = false;
		List<V1WantBackTypeToken> wantBackList = request.wantBackList;
		for (V1WantBackTypeToken wantBack: wantBackList) {
			if (!wantBack.wantBackTypeToken.equalsIgnoreCase("certPath")) {
				txResult.transactionResultToken = "SERVICEFAIL";
				txResult.transactionResultText = "Unsupported WantBack";
				response.transactionResult = txResult;
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else {
				certPathWantBack = true;
			}
		}
		/*
		 * Process an array of requests, and recombine into a v1 response
		 */
		List<JsonX509Certificate> batchCerts = request.x509CertificateList;
		V1ValidationResult validationResult = new V1ValidationResult();
		List<V1ResultByCertificate> resultsByCertificateList = new ArrayList<>();
		for (JsonX509Certificate toValidate: batchCerts) {
			/*
			 * Validate the certificate in the V1 request using the V2 endpoint and map the results.
			 */
			VssRequest v2Request = new VssRequest();
			v2Request.validationPolicyId = request.validationPolicy;
			v2Request.x509Certificate = toValidate.x509Certificate;
			VssResponse v2Response = null;
			try {
				v2Response = validate(v2Request, headers).getBody();
			} catch(ServiceException e) {
				txResult.transactionResultToken = "SERVICEFAIL";
				txResult.transactionResultText = e.getMessage();
				response.transactionResult = txResult;
				V1LOG.warn(LoggingUtil.pojoToJson(Map.of("error", "Exception thrown via V2 endpoint, returning SERVICEFAIL", "stacktrace", LoggingUtil.stackTraceToString(e))));
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			txResult.transactionResultToken = "SUCCESS";
			txResult.transactionResultText = "Validation Operation Completed Successfully";
			response.transactionResult = txResult;
			V1ResultByCertificate result = new V1ResultByCertificate();
			V1ResultByCertificateData resultData = new V1ResultByCertificateData();
			/*
			 * Convert "x5t#S256" that is URL encoded base64 to "vssCertId" which is Hex
			 */
			byte[] s256Digest = Base64.getUrlDecoder().decode(v2Response.x5tS256);
			resultData.vssCertId = X509Util.byteArrayToHexString(s256Digest);
			resultData.x509SubjectName = v2Response.x509SubjectName;
			resultData.x509IssuerName = v2Response.x509IssuerName;
			resultData.x509SerialNumber = v2Response.x509SerialNumber;
			resultData.x509SubjectAltName = v2Response.x509SubjectAltName;
			resultData.validationTime = v2Response.validationTime;
			resultData.nextUpdate = v2Response.nextUpdate;
			ValidationResult res = v2Response.validationResult;
			if (res instanceof Success) {
				resultData.validationResultToken = ValidationResult.SUCCESS_VALUE;
				Success success = (Success)res;
				V1ValidationSuccessData successData = new V1ValidationSuccessData();
				List<V1WantBack> wantBackResultList = new ArrayList<>();
				/*
				 * Check wantBack, and add CertPath if requested
				 */
				if (certPathWantBack) {
					List<JsonX509Certificate> certPath = success.x509CertificatePath;
					V1X509CertificateList certPathWantBackList = new V1X509CertificateList();
					V1WantBack certPathWb = new V1WantBack();
					certPathWantBackList.x509CertificateList = certPath;
					certPathWb.certPath = certPathWantBackList;
					wantBackResultList.add(certPathWb);
				}
				/*
				 * Add the wantback
				 */
				successData.wantBackResultList = wantBackResultList;
				resultData.validationSuccessData = successData;
			} else {
				resultData.validationResultToken = ValidationResult.FAIL_VALUE;
				Fail fail = (Fail)res;
				V1ValidationFailureData failDetail = new V1ValidationFailureData();
				failDetail.isAffirmativelyInvalid = fail.isAffirmativelyInvalid;
				V1InvalidityReason reason = new V1InvalidityReason();
				reason.invalidityReasonToken = ValidationResult.FAIL_VALUE;
				reason.invalidityReasonText = fail.invalidityReasonText;
				List<V1InvalidityReason> reasonList = new ArrayList<>();
				reasonList.add(reason);
				failDetail.invalidityReasonList = reasonList;
				resultData.validationFailureData = failDetail;
			}
			result.resultByCertificate = resultData;
			resultsByCertificateList.add(result);
		}
		validationResult.resultsByCertificateList = resultsByCertificateList;
		response.validationResult = validationResult;
		/*
		 * Log the response
		 */
		V1LOG.info(LoggingUtil.pojoToJson(response));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@ExceptionHandler({ Exception.class })
	public ResponseEntity<Object> handleAll(Exception e, WebRequest request) {
		ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

}
