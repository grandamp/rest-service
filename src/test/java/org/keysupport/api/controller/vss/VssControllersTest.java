package org.keysupport.api.controller.vss;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertStore;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keysupport.api.config.ServiceValidationPolicies;
import org.keysupport.api.pkix.ValidatePKIX;
import org.keysupport.api.pojo.vss.Fail;
import org.keysupport.api.pojo.vss.JsonX509Certificate;
import org.keysupport.api.pojo.vss.Success;
import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.keysupport.api.pojo.vss.VssResponse;
import org.keysupport.api.singletons.IntermediateCacheSingleton;
import org.mockito.MockedStatic;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

class VssControllersTest {

	private static final String DOD_ID_CA_65_B64 =
			"MIIEuTCCA6GgAwIBAgICBUwwDQYJKoZIhvcNAQELBQAwWzELMAkGA1UEBhMCVVMx"
			+ "GDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDEMMAoGA1UECxMDRG9EMQwwCgYDVQQL"
			+ "EwNQS0kxFjAUBgNVBAMTDURvRCBSb290IENBIDMwHhcNMjEwNjAxMTQxMTIzWhcN"
			+ "MjcwNjAyMTQxMTIzWjBaMQswCQYDVQQGEwJVUzEYMBYGA1UEChMPVS5TLiBHb3Zl"
			+ "cm5tZW50MQwwCgYDVQQLEwNEb0QxDDAKBgNVBAsTA1BLSTEVMBMGA1UEAxMMRE9E"
			+ "IElEIENBLTY1MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnkK9OCQ+"
			+ "D0b/7SLsEs0LCElhKIzGtiZDBw9VLqCaxTHlxaYEPV/B/X9NGoP5PE4ToBOSramL"
			+ "CMPbwjadhNk8O0gEInZCuEzV17vvx6O4xg+FJ9OO76LU1KeXJnnvX1YnCKz3uxrn"
			+ "3sw1jQugEEd1yPwKoHMjJ2Sr7Vgrm1e983EgiRint9lble7x/MDLvEZDELeeqhPZ"
			+ "vRiz1qwVG+/p2ks980qFLFLl1INOUSPnSLIbafg7cWE9yTC5i99s4pJnP2ThyBv6"
			+ "JsgFzbbj9FEYGyh75GjIMEv8ulcQ3ATOSBREUPzrd6sQmideeqvxXrDYxo8Qel6b"
			+ "rZiti+5vEr3OzQIDAQABo4IBhjCCAYIwHwYDVR0jBBgwFoAUbIqUonexgHIdgXoW"
			+ "qvLczmbuRcAwHQYDVR0OBBYEFGLgSDhWbW9rJb67w4hYsaycQ8lbMA4GA1UdDwEB"
			+ "/wQEAwIBhjBnBgNVHSAEYDBeMAsGCWCGSAFlAgELJDALBglghkgBZQIBCycwCwYJ"
			+ "YIZIAWUCAQsqMAsGCWCGSAFlAgELOzAMBgpghkgBZQMCAQMNMAwGCmCGSAFlAwIB"
			+ "AxEwDAYKYIZIAWUDAgEDJzASBgNVHRMBAf8ECDAGAQH/AgEAMAwGA1UdJAQFMAOA"
			+ "AQAwNwYDVR0fBDAwLjAsoCqgKIYmaHR0cDovL2NybC5kaXNhLm1pbC9jcmwvRE9E"
			+ "Uk9PVENBMy5jcmwwbAYIKwYBBQUHAQEEYDBeMDoGCCsGAQUFBzAChi5odHRwOi8v"
			+ "Y3JsLmRpc2EubWlsL2lzc3VlZHRvL0RPRFJPT1RDQTNfSVQucDdjMCAGCCsGAQUF"
			+ "BzABhhRodHRwOi8vb2NzcC5kaXNhLm1pbDANBgkqhkiG9w0BAQsFAAOCAQEAF8Uj"
			+ "33K0ZM9adtfd8IM2ebqwgbgRxi22Pb6bKkKOkGV2NU4wMckpuRpUrQGJmy6CIXZ8"
			+ "4QWz9DZSNAU0nyHXB6PLbSV0jnzKygWO7IOv83M6dcnCG8QUP1o20V0NGhzNBEtK"
			+ "jxWENZCYHEruxm+2rB+MBngPhkBgdni2npetHX2e1cmsgMS6G1PUh2idhZ8Mpdof"
			+ "nr+V0GuKLpwiNz3hLnKehl2Bs6aHG2IIOm/PdzvsKCP2eiGzS3SiiCf6fukYoYBN"
			+ "edL8fHfFNyM4UPNgc4eG+bu0GJK4wKPVjiX7xYDdGaYZ2m4Y++zrKuMq+Oar6DQG"
			+ "q3SERMAZCDYsEt3z2g==";

	private static final String AAL3_POLICY_ID = "2.16.840.1.101.10.2.18.2.1.4";
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static ValidationPolicies policies;
	private static ValidationPolicy aal3Policy;
	private static X509Certificate dodCa65;

	@BeforeAll
	static void setUp() throws Exception {
		policies = MAPPER.readValue(Files.readString(Path.of("configuration/policies.json")), ValidationPolicies.class);
		aal3Policy = policies.validationPolicies.get(1);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		dodCa65 = (X509Certificate) cf.generateCertificate(
				new ByteArrayInputStream(Base64.getDecoder().decode(DOD_ID_CA_65_B64)));
	}

	@Test
	void policiesController_returnsPoliciesAndPolicyById() throws Exception {
		MockMvc mvc = MockMvcBuilders.standaloneSetup(new PoliciesController()).build();
		try (MockedStatic<ServiceValidationPolicies> mocked = mockStatic(ServiceValidationPolicies.class)) {
			mocked.when(ServiceValidationPolicies::getValidationPolicies).thenReturn(policies);
			mocked.when(() -> ServiceValidationPolicies.getPolicy(AAL3_POLICY_ID)).thenReturn(aal3Policy);
			mocked.when(() -> ServiceValidationPolicies.getPolicy("1.2.3.4")).thenReturn(null);

			mvc.perform(get("/vss/v2/policies"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.validationPolicies", hasSize(3)));

			mvc.perform(get("/vss/v2/policies/" + AAL3_POLICY_ID))
					.andExpect(status().isOk())
					.andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
							"attachment; filename=" + AAL3_POLICY_ID + ".json"))
					.andExpect(jsonPath("$.validationPolicyName").value("aal3"));

			mvc.perform(get("/vss/v2/policies/1.2.3.4"))
					.andExpect(status().isNotFound());
		}
	}

	@Test
	void intermediatesController_handlesEmptyAndPopulatedStore() throws Exception {
		MockMvc mvc = MockMvcBuilders.standaloneSetup(new IntermediatesController()).build();
		IntermediateCacheSingleton cache = mock(IntermediateCacheSingleton.class);
		CertStore store = CertStore.getInstance("Collection", new CollectionCertStoreParameters(List.of(dodCa65)));

		try (MockedStatic<IntermediateCacheSingleton> mocked = mockStatic(IntermediateCacheSingleton.class)) {
			mocked.when(IntermediateCacheSingleton::getInstance).thenReturn(cache);

			when(cache.getIntermediates()).thenReturn(null);
			mvc.perform(get("/vss/v2/intermediates"))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$", hasSize(0)));

			when(cache.getIntermediates()).thenReturn(store);
			mvc.perform(get("/vss/v2/intermediates"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$", hasSize(1)))
					.andExpect(jsonPath("$[0].x509Certificate").value(DOD_ID_CA_65_B64));
		}
	}

	@Test
	void validateController_v2_validationErrorsAndSuccessAndFail() throws Exception {
		MockMvc mvc = MockMvcBuilders.standaloneSetup(new ValidateController()).build();

		// Missing fields -> 400 ApiError
		mvc.perform(post("/vss/v2/validate")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
				.andExpect(jsonPath("$.message", containsString("validationPolicyId")));

		try (MockedStatic<ServiceValidationPolicies> polMock = mockStatic(ServiceValidationPolicies.class);
				MockedStatic<ValidatePKIX> pkixMock = mockStatic(ValidatePKIX.class)) {

			polMock.when(() -> ServiceValidationPolicies.getPolicy(AAL3_POLICY_ID)).thenReturn(aal3Policy);
			polMock.when(() -> ServiceValidationPolicies.getPolicy("1.2.3.4")).thenReturn(null);

			// Unknown policy -> 400 ApiError
			mvc.perform(post("/vss/v2/validate")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"validationPolicyId\":\"1.2.3.4\",\"x509Certificate\":\"" + DOD_ID_CA_65_B64 + "\"}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Invalid Policy Specified"));

			// Invalid base64 -> 400 ApiError
			mvc.perform(post("/vss/v2/validate")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"validationPolicyId\":\"" + AAL3_POLICY_ID + "\",\"x509Certificate\":\"not!valid!b64\"}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Error decoding x509Certificate"));

			// Success response
			VssResponse okResp = new VssResponse();
			okResp.x5tS256 = "abc123";
			okResp.x509SerialNumber = "1356";
			okResp.isCA = true;
			Success success = new Success();
			JsonX509Certificate pathCert = new JsonX509Certificate();
			pathCert.x509Certificate = DOD_ID_CA_65_B64;
			success.x509CertificatePath = List.of(pathCert);
			okResp.validationResult = success;

			pkixMock.when(() -> ValidatePKIX.validate(any(), any(), eq(aal3Policy), any())).thenReturn(okResp);

			mvc.perform(post("/vss/v2/validate")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"validationPolicyId\":\"" + AAL3_POLICY_ID + "\",\"x509Certificate\":\"" + DOD_ID_CA_65_B64 + "\"}"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.requestId").isNotEmpty())
					.andExpect(jsonPath("$.validationTime").isNotEmpty())
					.andExpect(jsonPath("$.nextUpdate").isNotEmpty())
					.andExpect(jsonPath("$.isCA").value(true))
					.andExpect(jsonPath("$.validationResult.result").value("SUCCESS"));

			// Fail response -> nextUpdate omitted (null)
			VssResponse failResp = new VssResponse();
			failResp.x5tS256 = "abc123";
			failResp.x509SerialNumber = "1356";
			Fail fail = new Fail();
			fail.isAffirmativelyInvalid = true;
			fail.invalidityReasonText = "Certificate Revoked";
			failResp.validationResult = fail;

			pkixMock.when(() -> ValidatePKIX.validate(any(), any(), eq(aal3Policy), any())).thenReturn(failResp);

			mvc.perform(post("/vss/v2/validate")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"validationPolicyId\":\"" + AAL3_POLICY_ID + "\",\"x509Certificate\":\"" + DOD_ID_CA_65_B64 + "\"}"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.nextUpdate").doesNotExist())
					.andExpect(jsonPath("$.validationResult.result").value("FAIL"))
					.andExpect(jsonPath("$.validationResult.invalidityReasonText").value("Certificate Revoked"));
		}
	}

	@Test
	void validateController_v1_serviceFailAndBatchSuccessFail() throws Exception {
		MockMvc mvc = MockMvcBuilders.standaloneSetup(new ValidateController()).build();

		// Missing required fields -> 200 SERVICEFAIL
		mvc.perform(post("/vss/rest")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.transactionResult.transactionResultToken").value("SERVICEFAIL"));

		// Unsupported wantBack -> 200 SERVICEFAIL
		mvc.perform(post("/vss/rest")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"validationPolicy\":\"" + AAL3_POLICY_ID + "\",\"wantBackList\":[{\"wantBackTypeToken\":\"ocsp\"}],\"x509CertificateList\":[]}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.transactionResult.transactionResultToken").value("SERVICEFAIL"))
				.andExpect(jsonPath("$.transactionResult.transactionResultText").value("Unsupported WantBack"));

		try (MockedStatic<ServiceValidationPolicies> polMock = mockStatic(ServiceValidationPolicies.class);
				MockedStatic<ValidatePKIX> pkixMock = mockStatic(ValidatePKIX.class)) {

			polMock.when(() -> ServiceValidationPolicies.getPolicy(AAL3_POLICY_ID)).thenReturn(aal3Policy);

			VssResponse okResp = new VssResponse();
			okResp.x5tS256 = "AQID";
			okResp.x509SerialNumber = "1356";
			Success success = new Success();
			JsonX509Certificate pathCert = new JsonX509Certificate();
			pathCert.x509Certificate = DOD_ID_CA_65_B64;
			success.x509CertificatePath = List.of(pathCert);
			okResp.validationResult = success;

			VssResponse failResp = new VssResponse();
			failResp.x5tS256 = "BAUG";
			failResp.x509SerialNumber = "9999";
			Fail fail = new Fail();
			fail.isAffirmativelyInvalid = true;
			fail.invalidityReasonText = "Revoked";
			failResp.validationResult = fail;

			pkixMock.when(() -> ValidatePKIX.validate(any(), any(), eq(aal3Policy), any()))
					.thenReturn(okResp, failResp);

			String body = "{"
					+ "\"validationPolicy\":\"" + AAL3_POLICY_ID + "\","
					+ "\"wantBackList\":[{\"wantBackTypeToken\":\"certPath\"}],"
					+ "\"x509CertificateList\":[{\"x509Certificate\":\"" + DOD_ID_CA_65_B64 + "\"},{\"x509Certificate\":\"" + DOD_ID_CA_65_B64 + "\"}]"
					+ "}";

			mvc.perform(post("/vss/rest")
							.contentType(MediaType.APPLICATION_JSON)
							.content(body))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.transactionResult.transactionResultToken").value("SUCCESS"))
					.andExpect(jsonPath("$.validationResult.resultsByCertificateList", hasSize(2)))
					.andExpect(jsonPath("$.validationResult.resultsByCertificateList[0].resultByCertificate.vssCertId").value("010203"))
					.andExpect(jsonPath("$.validationResult.resultsByCertificateList[0].resultByCertificate.validationResultToken").value("SUCCESS"))
					.andExpect(jsonPath("$.validationResult.resultsByCertificateList[0].resultByCertificate.validationSuccessData.wantBackResultList[0].certPath.x509CertificateList[0].x509Certificate").value(DOD_ID_CA_65_B64))
					.andExpect(jsonPath("$.validationResult.resultsByCertificateList[1].resultByCertificate.validationResultToken").value("FAIL"))
					.andExpect(jsonPath("$.validationResult.resultsByCertificateList[1].resultByCertificate.validationFailureData.invalidityReasonList[0].invalidityReasonText").value("Revoked"));
		}
	}
}
