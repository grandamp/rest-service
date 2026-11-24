package org.keysupport.api.controller.vss;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.keysupport.api.config.ServiceValidationPolicies;
import org.keysupport.api.pkix.ValidatePKIX;
import org.keysupport.api.pkix.X509Util;
import org.keysupport.api.pojo.vss.Fail;
import org.keysupport.api.pojo.vss.JsonX509Certificate;
import org.keysupport.api.pojo.vss.Success;
import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.keysupport.api.pojo.vss.VssResponse;
import org.keysupport.api.singletons.IntermediateCacheSingleton;
import org.keysupport.api.singletons.ValidationCacheSingleton;
import org.keysupport.api.testutil.CertFixtures;
import org.keysupport.api.testutil.CertFixtures.CertFixture;
import org.mockito.MockedStatic;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import tools.jackson.databind.ObjectMapper;

class VssControllersTest {

	private static final String AAL3_POLICY_ID = "2.16.840.1.101.10.2.18.2.1.4";
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static ValidationPolicies policies;
	private static ValidationPolicy aal3Policy;
	private static CertFixture dodCa65Fixture;
	private static X509Certificate dodCa65;
	private static String dodCa65B64;

	@BeforeEach
	@AfterEach
	void clearValidationCache() {
		ValidationCacheSingleton cache = ValidationCacheSingleton.getInstance();
		cache.clear();
		cache.configure(
				ValidationCacheSingleton.DEFAULT_ENABLED,
				ValidationCacheSingleton.DEFAULT_SUCCESS_TTL_SECONDS,
				ValidationCacheSingleton.DEFAULT_AFFIRMATIVELY_INVALID_TTL_SECONDS,
				ValidationCacheSingleton.DEFAULT_MAX_ENTRIES);
	}

	@BeforeAll
	static void setUp() throws Exception {
		policies = MAPPER.readValue(Files.readString(Path.of("configuration/policies.json")), ValidationPolicies.class);
		aal3Policy = policies.validationPolicies.get(1);
		dodCa65Fixture = CertFixtures.load("ca/dod-id-ca-65.pem");
		dodCa65 = dodCa65Fixture.cert();
		dodCa65B64 = dodCa65Fixture.base64Der();
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
					.andExpect(jsonPath("$[0].x509Certificate").value(dodCa65B64));
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

			// Malformed non-OID policy -> 400 ApiError (observed in production logs)
			mvc.perform(post("/vss/v2/validate")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"validationPolicyId\":\"not-an-oid\",\"x509Certificate\":\"" + dodCa65B64 + "\"}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("validationPolicyId must be an Object Identifier"));

			// Unknown policy -> 400 ApiError
			mvc.perform(post("/vss/v2/validate")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"validationPolicyId\":\"1.2.3.4\",\"x509Certificate\":\"" + dodCa65B64 + "\"}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Invalid Policy Specified"));

			// Invalid base64 -> 400 ApiError
			mvc.perform(post("/vss/v2/validate")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"validationPolicyId\":\"" + AAL3_POLICY_ID + "\",\"x509Certificate\":\"not!valid!b64\"}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Error decoding x509Certificate"));

			// Valid base64 but non-X.509 DER payload -> 400 ApiError (observed in production logs)
			mvc.perform(post("/vss/v2/validate")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"validationPolicyId\":\"" + AAL3_POLICY_ID + "\",\"x509Certificate\":\"SGVsbG8gd29ybGQ=\"}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.message").value("Error decoding x509Certificate"));

			// Success response
			VssResponse okResp = new VssResponse();
			okResp.x5tS256 = "abc123";
			okResp.x509SerialNumber = "1356";
			okResp.isCA = true;
			Success success = new Success();
			JsonX509Certificate pathCert = new JsonX509Certificate();
			pathCert.x509Certificate = dodCa65B64;
			success.x509CertificatePath = List.of(pathCert);
			okResp.validationResult = success;

			pkixMock.when(() -> ValidatePKIX.validate(any(), any(), eq(aal3Policy), any())).thenReturn(okResp);

			mvc.perform(post("/vss/v2/validate")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"validationPolicyId\":\"" + AAL3_POLICY_ID + "\",\"x509Certificate\":\"" + dodCa65B64 + "\"}"))
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
			ValidationCacheSingleton.getInstance().clear();

			mvc.perform(post("/vss/v2/validate")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"validationPolicyId\":\"" + AAL3_POLICY_ID + "\",\"x509Certificate\":\"" + dodCa65B64 + "\"}"))
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

		ValidationCacheSingleton.getInstance().configure(false, 0L, 0L, 100);
		try (MockedStatic<ServiceValidationPolicies> polMock = mockStatic(ServiceValidationPolicies.class);
				MockedStatic<ValidatePKIX> pkixMock = mockStatic(ValidatePKIX.class)) {

			polMock.when(() -> ServiceValidationPolicies.getPolicy(AAL3_POLICY_ID)).thenReturn(aal3Policy);

			VssResponse okResp = new VssResponse();
			okResp.x5tS256 = "AQID";
			okResp.x509SerialNumber = "1356";
			Success success = new Success();
			JsonX509Certificate pathCert = new JsonX509Certificate();
			pathCert.x509Certificate = dodCa65B64;
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
					+ "\"x509CertificateList\":[{\"x509Certificate\":\"" + dodCa65B64 + "\"},{\"x509Certificate\":\"" + dodCa65B64 + "\"}]"
					+ "}";

			mvc.perform(post("/vss/rest")
							.contentType(MediaType.APPLICATION_JSON)
							.content(body))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.transactionResult.transactionResultToken").value("SUCCESS"))
					.andExpect(jsonPath("$.validationResult.resultsByCertificateList", hasSize(2)))
					.andExpect(jsonPath("$.validationResult.resultsByCertificateList[0].resultByCertificate.vssCertId").value("010203"))
					.andExpect(jsonPath("$.validationResult.resultsByCertificateList[0].resultByCertificate.validationResultToken").value("SUCCESS"))
					.andExpect(jsonPath("$.validationResult.resultsByCertificateList[0].resultByCertificate.validationSuccessData.wantBackResultList[0].certPath.x509CertificateList[0].x509Certificate").value(dodCa65B64))
					.andExpect(jsonPath("$.validationResult.resultsByCertificateList[1].resultByCertificate.validationResultToken").value("FAIL"))
					.andExpect(jsonPath("$.validationResult.resultsByCertificateList[1].resultByCertificate.validationFailureData.invalidityReasonList[0].invalidityReasonText").value("Revoked"));
		}
	}

	@Test
	void validateController_v2_cacheHitSkipsValidatePkix() throws Exception {
		MockMvc mvc = MockMvcBuilders.standaloneSetup(new ValidateController()).build();
		try (MockedStatic<ServiceValidationPolicies> polMock = mockStatic(ServiceValidationPolicies.class);
				MockedStatic<ValidatePKIX> pkixMock = mockStatic(ValidatePKIX.class)) {

			polMock.when(() -> ServiceValidationPolicies.getPolicy(AAL3_POLICY_ID)).thenReturn(aal3Policy);

			VssResponse okResp = new VssResponse();
			okResp.x5tS256 = "abc123";
			okResp.x509SerialNumber = "1356";
			okResp.isCA = true;
			okResp.validationResult = new Success();

			pkixMock.when(() -> ValidatePKIX.validate(any(), any(), eq(aal3Policy), any())).thenReturn(okResp);

			ValidationCacheSingleton cache = ValidationCacheSingleton.getInstance();
			String body = "{\"validationPolicyId\":\"" + AAL3_POLICY_ID + "\",\"x509Certificate\":\"" + dodCa65B64 + "\"}";
			mvc.perform(post("/vss/v2/validate").contentType(MediaType.APPLICATION_JSON).content(body))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.validationResult.result").value("SUCCESS"));
			assertEquals(0L, cache.getHitCount());
			assertEquals(1L, cache.getMissCount());

			mvc.perform(post("/vss/v2/validate").contentType(MediaType.APPLICATION_JSON).content(body))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.validationResult.result").value("SUCCESS"));
			assertEquals(1L, cache.getHitCount());
			assertEquals(1L, cache.getMissCount());

			pkixMock.verify(() -> ValidatePKIX.validate(any(), any(), eq(aal3Policy), any()), org.mockito.Mockito.times(1));
		}
	}

	static Stream<CertFixture> pemFixtures() throws Exception {
		return CertFixtures.allStream();
	}

	@ParameterizedTest(name = "PEM fixture v2 validate & intermediates: {0}")
	@MethodSource("pemFixtures")
	void pemFixture_v2ValidateAndIntermediatesRoundTrip(CertFixture fixture) throws Exception {
		MockMvc intermediatesMvc = MockMvcBuilders.standaloneSetup(new IntermediatesController()).build();
		IntermediateCacheSingleton cache = mock(IntermediateCacheSingleton.class);
		CertStore store = CertStore.getInstance("Collection",
				new CollectionCertStoreParameters(List.of(fixture.cert())));

		try (MockedStatic<IntermediateCacheSingleton> mocked = mockStatic(IntermediateCacheSingleton.class)) {
			mocked.when(IntermediateCacheSingleton::getInstance).thenReturn(cache);
			when(cache.getIntermediates()).thenReturn(store);

			intermediatesMvc.perform(get("/vss/v2/intermediates"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$", hasSize(1)))
					.andExpect(jsonPath("$[0].x509Certificate").value(fixture.base64Der()));
		}

		MockMvc validateMvc = MockMvcBuilders.standaloneSetup(new ValidateController()).build();
		try (MockedStatic<ServiceValidationPolicies> polMock = mockStatic(ServiceValidationPolicies.class);
				MockedStatic<ValidatePKIX> pkixMock = mockStatic(ValidatePKIX.class)) {

			polMock.when(() -> ServiceValidationPolicies.getPolicy(AAL3_POLICY_ID)).thenReturn(aal3Policy);

			String expectedX5t = X509Util.x5tS256(fixture.cert());
			String expectedSerial = fixture.cert().getSerialNumber().toString();

			VssResponse resp = new VssResponse();
			resp.x5tS256 = expectedX5t;
			resp.x509SerialNumber = expectedSerial;
			resp.isCA = fixture.isCa();
			Success success = new Success();
			JsonX509Certificate pathCert = new JsonX509Certificate();
			pathCert.x509Certificate = fixture.base64Der();
			success.x509CertificatePath = List.of(pathCert);
			resp.validationResult = success;

			pkixMock.when(() -> ValidatePKIX.validate(any(), any(), eq(aal3Policy), any())).thenReturn(resp);

			validateMvc.perform(post("/vss/v2/validate")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"validationPolicyId\":\"" + AAL3_POLICY_ID + "\",\"x509Certificate\":\""
									+ fixture.base64Der() + "\"}"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.x5t#S256").value(expectedX5t))
					.andExpect(jsonPath("$.x509SerialNumber").value(expectedSerial))
					.andExpect(jsonPath("$.isCA").value(fixture.isCa()))
					.andExpect(jsonPath("$.validationResult.result").value("SUCCESS"));
		}
	}

	@Test
	void pemFixtures_v1BatchProcessesAllDirectoryCerts() throws Exception {
		List<CertFixture> fixtures = CertFixtures.loadAll();
		MockMvc mvc = MockMvcBuilders.standaloneSetup(new ValidateController()).build();

		try (MockedStatic<ServiceValidationPolicies> polMock = mockStatic(ServiceValidationPolicies.class);
				MockedStatic<ValidatePKIX> pkixMock = mockStatic(ValidatePKIX.class)) {

			polMock.when(() -> ServiceValidationPolicies.getPolicy(AAL3_POLICY_ID)).thenReturn(aal3Policy);

			VssResponse[] responses = fixtures.stream().map(f -> {
				VssResponse r = new VssResponse();
				r.x5tS256 = X509Util.x5tS256(f.cert());
				r.x509SerialNumber = f.cert().getSerialNumber().toString();
				r.isCA = f.isCa();
				Success s = new Success();
				JsonX509Certificate c = new JsonX509Certificate();
				c.x509Certificate = f.base64Der();
				s.x509CertificatePath = List.of(c);
				r.validationResult = s;
				return r;
			}).toArray(VssResponse[]::new);

			pkixMock.when(() -> ValidatePKIX.validate(any(), any(), eq(aal3Policy), any()))
					.thenAnswer(inv -> {
						X509Certificate cert = inv.getArgument(0);
						String serial = cert.getSerialNumber().toString();
						for (VssResponse r : responses) {
							if (serial.equals(r.x509SerialNumber)) {
								return r;
							}
						}
						return responses[0];
					});

			List<String> certEntries = fixtures.stream()
					.map(f -> "{\"x509Certificate\":\"" + f.base64Der() + "\"}")
					.toList();
			String body = "{"
					+ "\"validationPolicy\":\"" + AAL3_POLICY_ID + "\","
					+ "\"wantBackList\":[{\"wantBackTypeToken\":\"certPath\"}],"
					+ "\"x509CertificateList\":[" + String.join(",", certEntries) + "]"
					+ "}";

			mvc.perform(post("/vss/rest")
							.contentType(MediaType.APPLICATION_JSON)
							.content(body))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.transactionResult.transactionResultToken").value("SUCCESS"))
					.andExpect(jsonPath("$.validationResult.resultsByCertificateList", hasSize(fixtures.size())));
		}
	}
}
