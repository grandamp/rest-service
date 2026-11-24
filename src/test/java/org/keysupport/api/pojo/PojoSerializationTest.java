package org.keysupport.api.pojo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keysupport.api.pojo.vss.ExcludedIntermediate;
import org.keysupport.api.pojo.vss.Fail;
import org.keysupport.api.pojo.vss.JsonTrustAnchor;
import org.keysupport.api.pojo.vss.JsonX509Certificate;
import org.keysupport.api.pojo.vss.PKIXPolicyNode;
import org.keysupport.api.pojo.vss.SANValue;
import org.keysupport.api.pojo.vss.Success;
import org.keysupport.api.pojo.vss.ValidationEvent;
import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.keysupport.api.pojo.vss.ValidationResult;
import org.keysupport.api.pojo.vss.VssRequest;
import org.keysupport.api.pojo.vss.VssResponse;
import org.keysupport.api.pojo.vss.v1.V1InvalidityReason;
import org.keysupport.api.pojo.vss.v1.V1ResultByCertificate;
import org.keysupport.api.pojo.vss.v1.V1ResultByCertificateData;
import org.keysupport.api.pojo.vss.v1.V1TransactionResult;
import org.keysupport.api.pojo.vss.v1.V1ValidationFailureData;
import org.keysupport.api.pojo.vss.v1.V1ValidationResult;
import org.keysupport.api.pojo.vss.v1.V1ValidationSuccessData;
import org.keysupport.api.pojo.vss.v1.V1VSSRequest;
import org.keysupport.api.pojo.vss.v1.V1VSSResponse;
import org.keysupport.api.pojo.vss.v1.V1WantBack;
import org.keysupport.api.pojo.vss.v1.V1WantBackTypeToken;
import org.keysupport.api.pojo.vss.v1.V1X509CertificateList;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class PojoSerializationTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static String policiesJson;

	@BeforeAll
	static void loadPolicies() throws Exception {
		policiesJson = Files.readString(Path.of("configuration/policies.json"));
	}

	@Test
	void validationPolicies_roundTripRealDocument() throws Exception {
		ValidationPolicies policies = MAPPER.readValue(policiesJson, ValidationPolicies.class);
		assertEquals(3, policies.validationPolicies.size());

		ValidationPolicy aal3 = policies.validationPolicies.get(1);
		assertEquals("2.16.840.1.101.10.2.18.2.1.4", aal3.validationPolicyId);
		assertEquals("aal3", aal3.validationPolicyName);
		assertFalse(aal3.inhibitPolicyMapping);
		assertTrue(aal3.requireExplicitPolicy);
		assertTrue(aal3.inhibitAnyPolicy);
		assertEquals(5, aal3.userPolicySet.size());
		assertEquals(1, aal3.trustAnchors.size());

		JsonTrustAnchor anchor = aal3.trustAnchors.get(0);
		assertEquals("X5rswkYWshkTcmAN2A9t0yDIyloM638JyYXr8GlpNPw=", anchor.x5tS256);
		assertEquals("CN=Federal Common Policy CA G2, OU=FPKI, O=U.S. Government, C=US", anchor.x509SubjectName);
		assertTrue(anchor.getAdditionalProperties().isEmpty());
		assertFalse(anchor.x509Certificate.isBlank());
		assertNull(anchor.kid);

		JsonNode rewritten = MAPPER.readTree(MAPPER.writeValueAsString(policies));
		assertEquals(MAPPER.readTree(policiesJson), rewritten);
		assertTrue(rewritten.toString().contains("X509SubjectName"));
		assertFalse(rewritten.toString().contains("validCacheLifetime"));
		assertFalse(rewritten.toString().contains("excludeIntermediates"));
	}

	@Test
	void v2RequestAndPolymorphicResponse_roundTrip() throws Exception {
		VssRequest request = new VssRequest();
		request.validationPolicyId = "2.16.840.1.101.10.2.18.2.1.4";
		request.x509Certificate = "MIIB";
		request.requestId = "req-1";
		request.setAdditionalProperty("clientMetadata", "locust");

		String requestJson = MAPPER.writeValueAsString(request);
		assertEquals(List.of("validationPolicyId", "x509Certificate", "requestId", "clientMetadata"),
				fieldNames(requestJson));
		VssRequest readRequest = MAPPER.readValue(requestJson, VssRequest.class);
		assertEquals("locust", readRequest.getAdditionalProperties().get("clientMetadata"));

		SANValue san = new SANValue();
		san.type = "rfc822Name";
		san.value = "user@example.gov";

		PKIXPolicyNode child = new PKIXPolicyNode();
		child.depth = 1;
		child.critical = true;
		child.validPolicy = "2.16.840.1.101.3.2.1.3.13";
		child.expectedPolicies = Set.of("2.16.840.1.101.3.2.1.3.13");

		PKIXPolicyNode root = new PKIXPolicyNode();
		root.depth = 0;
		root.critical = false;
		root.validPolicy = "2.5.29.32.0";
		root.expectedPolicies = new LinkedHashSet<>(Set.of("2.16.840.1.101.3.2.1.3.13"));
		root.children = new LinkedHashSet<>(Set.of(child));

		JsonX509Certificate pathCert = new JsonX509Certificate();
		pathCert.x509Certificate = "MIIB";

		Success success = new Success();
		success.x509CertificatePath = List.of(pathCert);
		success.policyTree = root;

		VssResponse response = new VssResponse();
		response.requestId = "req-1";
		response.validationPolicyId = request.validationPolicyId;
		response.x5tS256 = "abc";
		response.x509SubjectName = "CN=Leaf";
		response.x509IssuerName = "CN=Issuer";
		response.x509SerialNumber = "01";
		response.x509SubjectAltName = List.of(san);
		response.subjectKeyIdentifer = "ABCD";
		response.isCA = false;
		response.validationTime = "2026-11-18T23:00:00.000+0000";
		response.nextUpdate = "2026-11-19T23:00:00.000+0000";
		response.validationResult = success;

		String responseJson = MAPPER.writeValueAsString(response);
		assertTrue(responseJson.contains("\"x5t#S256\":\"abc\""));
		assertTrue(responseJson.contains("\"subjectKeyIdentifer\":\"ABCD\""));
		assertTrue(responseJson.contains("\"result\":\"SUCCESS\""));

		VssResponse read = MAPPER.readValue(responseJson, VssResponse.class);
		Success readSuccess = assertInstanceOf(Success.class, read.validationResult);
		assertEquals(ValidationResult.SUCCESS_VALUE, readSuccess.result);
		assertEquals("rfc822Name", read.x509SubjectAltName.get(0).type);
		assertEquals("MIIB", readSuccess.x509CertificatePath.get(0).x509Certificate);
		assertEquals(1, readSuccess.policyTree.children.iterator().next().depth);

		Fail fail = new Fail();
		fail.isAffirmativelyInvalid = Boolean.TRUE;
		fail.invalidityReasonText = "revoked";
		String failJson = MAPPER.writeValueAsString(fail);
		Fail readFail = assertInstanceOf(Fail.class, MAPPER.readValue(failJson, ValidationResult.class));
		assertEquals(Boolean.TRUE, readFail.isAffirmativelyInvalid);
		assertEquals("revoked", readFail.invalidityReasonText);
	}

	@Test
	void eventAndExcludedIntermediate_roundTrip() throws Exception {
		ValidationEvent event = new ValidationEvent();
		event.x5tS256 = "abc";
		event.eventType = "SUCCESS";
		event.message = "validated";
		String eventJson = MAPPER.writeValueAsString(event);
		assertEquals(List.of("x5tS256", "eventType", "message"), fieldNames(eventJson));
		assertFalse(eventJson.contains("x5t#S256"));

		ExcludedIntermediate excluded = new ExcludedIntermediate();
		excluded.x5tS256 = "abc";
		excluded.x509SubjectName = "CN=CA";
		excluded.x509IssuerName = "CN=Root";
		excluded.excludeReason = "not in hint list";
		String excludedJson = MAPPER.writeValueAsString(excluded);
		assertTrue(excludedJson.contains("\"x5t#S256\":\"abc\""));
		assertEquals("not in hint list", MAPPER.readValue(excludedJson, ExcludedIntermediate.class).excludeReason);
	}

	@Test
	void v1RequestAndResponse_roundTrip() throws Exception {
		V1WantBackTypeToken want = new V1WantBackTypeToken();
		want.wantBackTypeToken = "certPath";
		JsonX509Certificate cert = new JsonX509Certificate();
		cert.x509Certificate = "MIIB";

		V1VSSRequest request = new V1VSSRequest();
		request.validationPolicy = "2.16.840.1.101.10.2.18.2.1.4";
		request.wantBackList = List.of(want);
		request.x509CertificateList = List.of(cert);
		String requestJson = MAPPER.writeValueAsString(request);
		assertEquals(List.of("validationPolicy", "wantBackList", "x509CertificateList"), fieldNames(requestJson));

		SANValue san = new SANValue();
		san.type = "otherName:userPrincipalName";
		san.value = "USER@EXAMPLE.GOV";

		V1InvalidityReason reason = new V1InvalidityReason();
		reason.invalidityReasonToken = "FAIL";
		reason.invalidityReasonText = "revoked";
		V1ValidationFailureData failure = new V1ValidationFailureData();
		failure.isAffirmativelyInvalid = true;
		failure.invalidityReasonList = List.of(reason);

		V1X509CertificateList path = new V1X509CertificateList();
		path.x509CertificateList = List.of(cert);
		V1WantBack wantBack = new V1WantBack();
		wantBack.certPath = path;
		V1ValidationSuccessData successData = new V1ValidationSuccessData();
		successData.wantBackResultList = List.of(wantBack);

		V1ResultByCertificateData failData = new V1ResultByCertificateData();
		failData.vssCertId = "ABCD";
		failData.x509SubjectName = "CN=Revoked";
		failData.x509IssuerName = "CN=Issuer";
		failData.x509SerialNumber = "02";
		failData.x509SubjectAltName = List.of(san);
		failData.validationTime = "2026-11-18T23:00:00.000+0000";
		failData.nextUpdate = "2026-11-19T23:00:00.000+0000";
		failData.validationResultToken = "FAIL";
		failData.validationFailureData = failure;

		V1ResultByCertificateData successCertData = new V1ResultByCertificateData();
		successCertData.vssCertId = "EF01";
		successCertData.validationResultToken = "SUCCESS";
		successCertData.validationSuccessData = successData;

		V1ResultByCertificate failResult = new V1ResultByCertificate();
		failResult.resultByCertificate = failData;
		V1ResultByCertificate successResult = new V1ResultByCertificate();
		successResult.resultByCertificate = successCertData;

		V1ValidationResult validationResult = new V1ValidationResult();
		validationResult.resultsByCertificateList = List.of(failResult, successResult);
		V1TransactionResult transaction = new V1TransactionResult();
		transaction.transactionResultToken = "SUCCESS";
		transaction.transactionResultText = "ok";

		V1VSSResponse response = new V1VSSResponse();
		response.transactionResult = transaction;
		response.validationResult = validationResult;

		String responseJson = MAPPER.writeValueAsString(response);
		V1VSSResponse read = MAPPER.readValue(responseJson, V1VSSResponse.class);
		assertEquals("SUCCESS", read.transactionResult.transactionResultToken);
		assertEquals(2, read.validationResult.resultsByCertificateList.size());
		V1ResultByCertificateData readFail = read.validationResult.resultsByCertificateList.get(0).resultByCertificate;
		assertTrue(readFail.validationFailureData.isAffirmativelyInvalid);
		assertEquals("revoked", readFail.validationFailureData.invalidityReasonList.get(0).invalidityReasonText);
		assertEquals("otherName:userPrincipalName", readFail.x509SubjectAltName.get(0).type);
		assertEquals("MIIB", read.validationResult.resultsByCertificateList.get(1)
				.resultByCertificate.validationSuccessData.wantBackResultList.get(0).certPath.x509CertificateList.get(0).x509Certificate);
	}

	@Test
	void nullFieldsAreOmitted() throws Exception {
		assertEquals("{}", MAPPER.writeValueAsString(new VssRequest()));
		assertEquals("{}", MAPPER.writeValueAsString(new V1VSSResponse()));
		// isCA is a primitive boolean, so NON_NULL cannot omit its false default.
		assertTrue(MAPPER.writeValueAsString(new VssResponse()).contains("\"isCA\":false"));
	}

	private static List<String> fieldNames(String json) throws Exception {
		return new ArrayList<>(MAPPER.readTree(json).propertyNames());
	}
}
