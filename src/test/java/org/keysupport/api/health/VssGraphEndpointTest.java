package org.keysupport.api.health;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.keysupport.api.pkix.X509Util;
import org.keysupport.api.pojo.vss.ExcludedIntermediate;
import org.keysupport.api.pojo.vss.ValidationPolicies;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.keysupport.api.singletons.IntermediateCacheSingleton;
import org.keysupport.api.singletons.ValidationPoliciesSingleton;
import org.keysupport.api.testutil.CertFixtures;
import org.mockito.MockedStatic;

class VssGraphEndpointTest {

	@SuppressWarnings("unchecked")
	@Test
	void graph_buildsNodesVerifiedEdgesAndPerPolicyReachability() throws Exception {
		X509Certificate treasuryRoot = CertFixtures.load("ca/us-treasury-root-ca.pem").cert();
		X509Certificate ocioCa = CertFixtures.load("ca/treasury-ocio-ca.pem").cert();
		X509Certificate dhsCa4 = CertFixtures.load("ca/dhs-ca4.pem").cert();
		X509Certificate unrelatedDod = CertFixtures.load("ca/dod-root-ca-3.pem").cert();

		String rootId = X509Util.x5tS256(treasuryRoot);
		String ocioId = X509Util.x5tS256(ocioCa);
		String dhsId = X509Util.x5tS256(dhsCa4);
		String dodId = X509Util.x5tS256(unrelatedDod);

		List<X509Certificate> cached = List.of(ocioCa, dhsCa4, unrelatedDod);
		CertStore store = CertStore.getInstance("Collection", new CollectionCertStoreParameters(cached), "SUN");

		ValidationPolicy defaultPol = new ValidationPolicy();
		defaultPol.validationPolicyId = "1.3.6.1.5.5.7.19.1";
		defaultPol.validationPolicyName = "default";
		defaultPol.userPolicySet = List.of("2.5.29.32.0");
		defaultPol.requireExplicitPolicy = true;
		defaultPol.inhibitPolicyMapping = false;
		defaultPol.inhibitAnyPolicy = true;

		ValidationPolicy strictUnmatchedPol = new ValidationPolicy();
		strictUnmatchedPol.validationPolicyId = "1.2.3.4.5.99";
		strictUnmatchedPol.validationPolicyName = "strict-unmatched";
		strictUnmatchedPol.userPolicySet = List.of("1.2.3.4.5.99");
		strictUnmatchedPol.requireExplicitPolicy = true;
		strictUnmatchedPol.inhibitPolicyMapping = true;
		strictUnmatchedPol.inhibitAnyPolicy = true;

		ValidationPolicies policies = new ValidationPolicies();
		policies.validationPolicies = List.of(defaultPol, strictUnmatchedPol);

		ValidationPoliciesSingleton mockPolSingleton = mock(ValidationPoliciesSingleton.class);
		when(mockPolSingleton.getValidationPolicies()).thenReturn(policies);
		when(mockPolSingleton.getTrustAnchors("1.3.6.1.5.5.7.19.1"))
				.thenReturn(Set.of(new TrustAnchor(treasuryRoot, null)));
		when(mockPolSingleton.getTrustAnchors("1.2.3.4.5.99"))
				.thenReturn(Set.of(new TrustAnchor(treasuryRoot, null)));
		when(mockPolSingleton.getCertPathProvider()).thenReturn("SUN");
		when(mockPolSingleton.getCertPathAlgorithm()).thenReturn("PKIX");
		when(mockPolSingleton.getMaxPathLen()).thenReturn(7);

		IntermediateCacheSingleton mockIntSingleton = mock(IntermediateCacheSingleton.class);
		when(mockIntSingleton.getIntermediates()).thenReturn(store);
		when(mockIntSingleton.getCachedCerts()).thenReturn(cached);
		when(mockIntSingleton.getLastExcludedCerts()).thenReturn(List.<ExcludedIntermediate>of());

		VssGraphEndpoint endpoint = new VssGraphEndpoint();

		try (MockedStatic<ValidationPoliciesSingleton> polMock = mockStatic(ValidationPoliciesSingleton.class);
				MockedStatic<IntermediateCacheSingleton> intMock = mockStatic(IntermediateCacheSingleton.class)) {
			polMock.when(ValidationPoliciesSingleton::getInstance).thenReturn(mockPolSingleton);
			intMock.when(IntermediateCacheSingleton::getInstance).thenReturn(mockIntSingleton);

			Map<String, Object> graph = endpoint.graph();
			assertSame(graph, endpoint.graph(), "Snapshot should be cached for unchanged policies/store");

			assertEquals(4, graph.get("nodeCount"));
			assertEquals(2, graph.get("edgeCount"));

			Map<String, Map<String, Object>> nodes = (Map<String, Map<String, Object>>) graph.get("nodes");
			assertNotNull(nodes.get(ocioId));
			assertEquals("OCIO CA", nodes.get(ocioId).get("label"));
			assertFalse(((List<String>) nodes.get(ocioId).get("certificatePolicies")).isEmpty());

			List<Map<String, Object>> policyGraphs = (List<Map<String, Object>>) graph.get("policies");
			assertEquals(2, policyGraphs.size());

			Map<String, Object> defaultGraph = policyGraphs.get(0);
			List<String> reachableDefault = (List<String>) defaultGraph.get("reachableNodes");
			assertTrue(reachableDefault.contains(rootId));
			assertTrue(reachableDefault.contains(ocioId));
			assertTrue(reachableDefault.contains(dhsId));
			assertFalse(reachableDefault.contains(dodId));

			Map<String, List<String>> validPolicyOids = (Map<String, List<String>>) defaultGraph.get("validPolicyOids");
			assertNotNull(validPolicyOids.get(ocioId));
			assertFalse(validPolicyOids.get(ocioId).isEmpty());
			Map<String, List<Map<String, Object>>> policyLeaves = (Map<String, List<Map<String, Object>>>) defaultGraph.get("policyLeaves");
			assertFalse(policyLeaves.get(ocioId).isEmpty());

			List<Map<String, String>> excludedDefault = (List<Map<String, String>>) defaultGraph.get("excludedForPolicy");
			assertEquals(1, excludedDefault.size());
			assertEquals(dodId, excludedDefault.get(0).get("id"));

			Map<String, Object> strictGraph = policyGraphs.get(1);
			List<String> reachableStrict = (List<String>) strictGraph.get("reachableNodes");
			assertEquals(List.of(rootId), reachableStrict);
			assertEquals(3, ((List<?>) strictGraph.get("excludedForPolicy")).size());
		}
	}
}
