package org.keysupport.api.pkix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateRevokedException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.CRLReason;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.Test;
import org.keysupport.api.controller.ServiceException;
import org.keysupport.api.pojo.vss.Fail;
import org.keysupport.api.pojo.vss.Success;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.keysupport.api.pojo.vss.VssResponse;
import org.keysupport.api.singletons.IntermediateCacheSingleton;
import org.keysupport.api.singletons.PkixMetricsSingleton;
import org.keysupport.api.singletons.ValidationPoliciesSingleton;
import org.mockito.MockedStatic;

class ValidatePKIXTest {

	private static final String POLICY_ID = "1.3.6.1.5.5.7.19.1";
	private static final String CERT_POLICY_OID = "2.16.840.1.101.3.2.1.3.13";
	private static final String OTHER_POLICY_OID = "2.16.840.1.101.3.2.1.3.99";

	static {
		if (null == Security.getProvider(BouncyCastleFipsProvider.PROVIDER_NAME)) {
			Security.addProvider(new BouncyCastleFipsProvider());
		}
	}

	private static ValidationPolicy buildPolicy(String... userPolicies) {
		ValidationPolicy vp = new ValidationPolicy();
		vp.validationPolicyId = POLICY_ID;
		vp.validationPolicyName = "default";
		vp.userPolicySet = List.of(userPolicies);
		vp.inhibitPolicyMapping = false;
		vp.requireExplicitPolicy = true;
		vp.inhibitAnyPolicy = true;
		return vp;
	}

	private static KeyPair generateRsaKeyPair() throws Exception {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", BouncyCastleFipsProvider.PROVIDER_NAME);
		kpg.initialize(2048);
		return kpg.generateKeyPair();
	}

	private static X509Certificate signCert(X500Name issuer, KeyPair issuerKp, X500Name subject, KeyPair subjectKp,
			BigInteger serial, Date notBefore, Date notAfter, boolean isCa, String policyOid, String sanDns)
			throws Exception {
		X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(issuer, serial, notBefore, notAfter,
				subject, subjectKp.getPublic());
		JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
		builder.addExtension(Extension.subjectKeyIdentifier, false,
				extUtils.createSubjectKeyIdentifier(subjectKp.getPublic()));
		builder.addExtension(Extension.authorityKeyIdentifier, false,
				extUtils.createAuthorityKeyIdentifier(issuerKp.getPublic()));
		if (isCa) {
			builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
			builder.addExtension(Extension.keyUsage, true,
					new KeyUsage(KeyUsage.keyCertSign | KeyUsage.cRLSign));
		} else {
			builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
			builder.addExtension(Extension.keyUsage, true,
					new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
		}
		if (null != policyOid) {
			ASN1EncodableVector policies = new ASN1EncodableVector();
			policies.add(new PolicyInformation(new org.bouncycastle.asn1.ASN1ObjectIdentifier(policyOid)));
			builder.addExtension(Extension.certificatePolicies, false, new DERSequence(policies));
		}
		if (null != sanDns) {
			builder.addExtension(Extension.subjectAlternativeName, false,
					new GeneralNames(new GeneralName(GeneralName.dNSName, sanDns)));
		}
		ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
				.setProvider(BouncyCastleFipsProvider.PROVIDER_NAME)
				.build(issuerKp.getPrivate());
		return new JcaX509CertificateConverter()
				.setProvider(BouncyCastleFipsProvider.PROVIDER_NAME)
				.getCertificate(builder.build(signer));
	}

	private static void stubSingletons(ValidationPoliciesSingleton policiesMock, IntermediateCacheSingleton cacheMock,
			Set<TrustAnchor> trustAnchors, CertStore intermediateStore, boolean revocationEnabled, boolean aiaChase) {
		when(policiesMock.getTrustAnchors(POLICY_ID)).thenReturn(trustAnchors);
		when(policiesMock.getCertPathProvider()).thenReturn("SUN");
		when(policiesMock.getCertPathAlgorithm()).thenReturn("PKIX");
		when(policiesMock.getMaxPathLen()).thenReturn(5);
		when(policiesMock.getRevocationEnabled()).thenReturn(revocationEnabled);
		when(policiesMock.getRevocationEeOnly()).thenReturn(true);
		when(policiesMock.getAiaChase()).thenReturn(aiaChase);
		when(cacheMock.getIntermediates()).thenReturn(intermediateStore);
	}

	@Test
	void validate_validChainAndTemporalFailuresAndPolicyFailures() throws Exception {
		PkixMetricsSingleton pkixMetrics = PkixMetricsSingleton.getInstance();
		pkixMetrics.resetCountersForTesting();
		long nowMs = System.currentTimeMillis();
		Date notBefore = new Date(nowMs - 86_400_000L);
		Date notAfter = new Date(nowMs + 86_400_000L * 30L);
		Date validNow = new Date(nowMs);

		KeyPair rootKp = generateRsaKeyPair();
		KeyPair interKp = generateRsaKeyPair();
		KeyPair eeKp = generateRsaKeyPair();
		KeyPair rogueKp = generateRsaKeyPair();

		X500Name rootDn = new X500Name("CN=Test Root CA, O=KeySupport Test, C=US");
		X500Name interDn = new X500Name("CN=Test Intermediate CA, O=KeySupport Test, C=US");
		X500Name eeDn = new X500Name("CN=Test End Entity, O=KeySupport Test, C=US");
		X500Name rogueDn = new X500Name("CN=Rogue Root CA, O=KeySupport Test, C=US");

		X509Certificate rootCert = signCert(rootDn, rootKp, rootDn, rootKp, BigInteger.ONE, notBefore, notAfter, true,
				CERT_POLICY_OID, null);
		X509Certificate interCert = signCert(rootDn, rootKp, interDn, interKp, BigInteger.TWO, notBefore, notAfter,
				true, CERT_POLICY_OID, null);
		X509Certificate eeCert = signCert(interDn, interKp, eeDn, eeKp, BigInteger.valueOf(3), notBefore, notAfter,
				false, CERT_POLICY_OID, "ee.keysupport.test");
		X509Certificate rogueEeCert = signCert(rogueDn, rogueKp, eeDn, eeKp, BigInteger.valueOf(4), notBefore, notAfter,
				false, CERT_POLICY_OID, null);

		CertStore interStore = CertStore.getInstance("Collection",
				new CollectionCertStoreParameters(List.of(interCert)), "SUN");
		CertStore emptyStore = CertStore.getInstance("Collection",
				new CollectionCertStoreParameters(List.of()), "SUN");
		Set<TrustAnchor> anchors = Set.of(new TrustAnchor(rootCert, null));

		ValidationPoliciesSingleton policiesMock = mock(ValidationPoliciesSingleton.class);
		IntermediateCacheSingleton cacheMock = mock(IntermediateCacheSingleton.class);
		stubSingletons(policiesMock, cacheMock, anchors, interStore, false, true);

		try (MockedStatic<ValidationPoliciesSingleton> polStatic = mockStatic(ValidationPoliciesSingleton.class);
				MockedStatic<IntermediateCacheSingleton> cacheStatic = mockStatic(IntermediateCacheSingleton.class)) {
			polStatic.when(ValidationPoliciesSingleton::getInstance).thenReturn(policiesMock);
			cacheStatic.when(IntermediateCacheSingleton::getInstance).thenReturn(cacheMock);

			ValidationPolicy valPol = buildPolicy(CERT_POLICY_OID);

			// 1. Valid path -> Success
			VssResponse ok = ValidatePKIX.validate(eeCert, "x5t-valid", valPol, validNow);
			assertEquals(POLICY_ID, ok.validationPolicyId);
			assertEquals("x5t-valid", ok.x5tS256);
			assertFalse(ok.isCA);
			assertNotNull(ok.x509SubjectAltName);
			Success success = assertInstanceOf(Success.class, ok.validationResult);
			assertEquals(2, success.x509CertificatePath.size());
			assertNotNull(success.policyTree);

			// 2. Expired relative to Date now -> Fail (isAffirmativelyInvalid=true)
			VssResponse expired = ValidatePKIX.validate(eeCert, "x5t-expired", valPol,
					new Date(notAfter.getTime() + 60_000L));
			Fail expFail = assertInstanceOf(Fail.class, expired.validationResult);
			assertTrue(expFail.isAffirmativelyInvalid);
			assertNotNull(expFail.invalidityReasonText);

			// 3. Not yet valid relative to Date now -> Fail (isAffirmativelyInvalid=true)
			VssResponse notYet = ValidatePKIX.validate(eeCert, "x5t-notyet", valPol,
					new Date(notBefore.getTime() - 60_000L));
			Fail nyvFail = assertInstanceOf(Fail.class, notYet.validationResult);
			assertTrue(nyvFail.isAffirmativelyInvalid);
			assertNotNull(nyvFail.invalidityReasonText);

			// 4. Policy mismatch -> CertPathBuilderException with null cause -> Fail
			VssResponse wrongPol = ValidatePKIX.validate(eeCert, "x5t-pol", buildPolicy(OTHER_POLICY_OID), validNow);
			Fail polFail = assertInstanceOf(Fail.class, wrongPol.validationResult);
			assertFalse(polFail.isAffirmativelyInvalid);
			assertNotNull(polFail.invalidityReasonText);

			// 5. Unknown root / missing intermediate -> CertPathBuilderException with null cause -> isAffirmativelyInvalid=false
			VssResponse unknown = ValidatePKIX.validate(rogueEeCert, "x5t-unknown", valPol, validNow);
			Fail unkFail = assertInstanceOf(Fail.class, unknown.validationResult);
			assertFalse(unkFail.isAffirmativelyInvalid);

			// 5b. Untrusted self-signed cert -> CertPathBuilderException with null cause -> isAffirmativelyInvalid=true
			X509Certificate untrustedSelfSigned = signCert(rogueDn, rogueKp, rogueDn, rogueKp, BigInteger.valueOf(5),
					notBefore, notAfter, false, CERT_POLICY_OID, null);
			VssResponse selfSignedFailResp = ValidatePKIX.validate(untrustedSelfSigned, "x5t-self", valPol, validNow);
			Fail selfSignedFail = assertInstanceOf(Fail.class, selfSignedFailResp.validationResult);
			assertTrue(selfSignedFail.isAffirmativelyInvalid);

			// 5c. Self-issued rollover cert (Subject == Issuer, signed by different key) -> isAffirmativelyInvalid=false
			X509Certificate selfIssuedRollover = signCert(rogueDn, rogueKp, rogueDn, eeKp, BigInteger.valueOf(6),
					notBefore, notAfter, true, CERT_POLICY_OID, null);
			VssResponse rolloverResp = ValidatePKIX.validate(selfIssuedRollover, "x5t-rollover", valPol, validNow);
			Fail rolloverFail = assertInstanceOf(Fail.class, rolloverResp.validationResult);
			assertFalse(rolloverFail.isAffirmativelyInvalid);

			// 6. Intermediate discovery adds missing CA intermediate to IntermediateCacheSingleton
			// (also verifies isCA=true path)
			when(cacheMock.getIntermediates()).thenReturn(emptyStore);
			VssResponse caMissingFromStore = ValidatePKIX.validate(interCert, "x5t-ca", valPol, validNow);
			assertTrue(caMissingFromStore.isCA);
			assertInstanceOf(Success.class, caMissingFromStore.validationResult);
			org.mockito.Mockito.verify(cacheMock).addIntermediate(interCert);

			// Expired & not-yet-valid fail before discovery; 2 valid paths + 4 discovery failures ran
			assertEquals(2L, pkixMetrics.getDiscoverySuccessCount());
			assertEquals(4L, pkixMetrics.getDiscoveryFailCount());
			assertEquals(6L, pkixMetrics.getDiscoveryCount());
			assertTrue(pkixMetrics.getDiscoveryTotalNanos() > 0L);
			assertEquals(2L, pkixMetrics.getValidationSuccessCount());
			assertEquals(0L, pkixMetrics.getValidationFailCount());
			assertEquals(2L, pkixMetrics.getValidationCount());
			assertTrue(pkixMetrics.getValidationTotalNanos() > 0L);
		}
	}

	@Test
	void validate_handlesRevokedAndInvalidAlgorithmParameterAndCertStoreException() throws Exception {
		long nowMs = System.currentTimeMillis();
		Date notBefore = new Date(nowMs - 86_400_000L);
		Date notAfter = new Date(nowMs + 86_400_000L * 30L);
		Date validNow = new Date(nowMs);

		KeyPair rootKp = generateRsaKeyPair();
		X500Name rootDn = new X500Name("CN=Test Root CA 2, O=KeySupport Test, C=US");
		X509Certificate rootCert = signCert(rootDn, rootKp, rootDn, rootKp, BigInteger.ONE, notBefore, notAfter, true,
				CERT_POLICY_OID, null);
		Set<TrustAnchor> anchors = Set.of(new TrustAnchor(rootCert, null));

		ValidationPoliciesSingleton policiesMock = mock(ValidationPoliciesSingleton.class);
		IntermediateCacheSingleton cacheMock = mock(IntermediateCacheSingleton.class);
		CertStore storeMock = mock(CertStore.class);
		when(storeMock.getCertificates(org.mockito.ArgumentMatchers.any()))
				.thenThrow(new CertStoreException("simulated store failure"));
		stubSingletons(policiesMock, cacheMock, anchors, storeMock, false, true);

		ValidationPolicy valPol = buildPolicy(CERT_POLICY_OID);

		try (MockedStatic<ValidationPoliciesSingleton> polStatic = mockStatic(ValidationPoliciesSingleton.class);
				MockedStatic<IntermediateCacheSingleton> cacheStatic = mockStatic(IntermediateCacheSingleton.class)) {
			polStatic.when(ValidationPoliciesSingleton::getInstance).thenReturn(policiesMock);
			cacheStatic.when(IntermediateCacheSingleton::getInstance).thenReturn(cacheMock);

			// CertStoreException during AIA discovery must not NPE on cachedPathCerts.isEmpty()
			VssResponse storeErrResp = ValidatePKIX.validate(rootCert, "x5t-store-err", valPol, validNow);
			assertInstanceOf(Success.class, storeErrResp.validationResult);

			// Empty TrustAnchor set -> InvalidAlgorithmParameterException -> ServiceException
			when(policiesMock.getTrustAnchors(POLICY_ID)).thenReturn(Set.of());
			assertThrows(ServiceException.class,
					() -> ValidatePKIX.validate(rootCert, "x5t-empty-ta", valPol, validNow));
		}

		// Mock CertPathBuilder.build(...) throwing InvalidAlgorithmParameterException -> ServiceException (no NPE)
		// and CertificateRevokedException -> Fail with isAffirmativelyInvalid=true
		stubSingletons(policiesMock, cacheMock, anchors, storeMock, true, false);
		CertPathBuilder cpbMock = mock(CertPathBuilder.class);
		when(cpbMock.getRevocationChecker())
				.thenReturn((java.security.cert.PKIXRevocationChecker) CertPathBuilder.getInstance("PKIX", "SUN")
						.getRevocationChecker());

		try (MockedStatic<ValidationPoliciesSingleton> polStatic = mockStatic(ValidationPoliciesSingleton.class);
				MockedStatic<IntermediateCacheSingleton> cacheStatic = mockStatic(IntermediateCacheSingleton.class);
				MockedStatic<CertPathBuilder> cpbStatic = mockStatic(CertPathBuilder.class)) {
			polStatic.when(ValidationPoliciesSingleton::getInstance).thenReturn(policiesMock);
			cacheStatic.when(IntermediateCacheSingleton::getInstance).thenReturn(cacheMock);
			cpbStatic.when(() -> CertPathBuilder.getInstance("PKIX", "SUN")).thenReturn(cpbMock);

			when(cpbMock.build(org.mockito.ArgumentMatchers.any()))
					.thenThrow(new InvalidAlgorithmParameterException("bad params"));
			assertThrows(ServiceException.class,
					() -> ValidatePKIX.validate(rootCert, "x5t-iap", valPol, validNow));

			CertificateRevokedException revEx = new CertificateRevokedException(validNow, CRLReason.KEY_COMPROMISE,
					new X500Principal("CN=Test Root CA 2"), Map.of());
			CertPathBuilderException cpbe = new CertPathBuilderException("revoked", new Throwable("wrapper", revEx));
			org.mockito.Mockito.doThrow(cpbe).when(cpbMock).build(org.mockito.ArgumentMatchers.any());

			VssResponse revokedResp = ValidatePKIX.validate(rootCert, "x5t-rev", valPol, validNow);
			Fail revFail = assertInstanceOf(Fail.class, revokedResp.validationResult);
			assertTrue(revFail.isAffirmativelyInvalid);
			assertEquals("wrapper", revFail.invalidityReasonText);
		}
	}
}
