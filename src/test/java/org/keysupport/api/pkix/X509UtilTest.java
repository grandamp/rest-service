package org.keysupport.api.pkix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Security;
import java.security.cert.PolicyNode;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.keysupport.api.pojo.vss.PKIXPolicyNode;
import org.keysupport.api.pojo.vss.SANValue;
import org.keysupport.api.testutil.CertFixtures;
import org.keysupport.api.testutil.CertFixtures.CertFixture;

class X509UtilTest {

	private static X509Certificate dodIdCa65;
	private static X509Certificate tejPiv;
	private static X509Certificate keysupportOrg;

	@BeforeAll
	static void setUp() throws Exception {
		if (Security.getProvider(BouncyCastleFipsProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleFipsProvider());
		}
		dodIdCa65 = CertFixtures.load("ca/dod-id-ca-65.pem").cert();
		tejPiv = CertFixtures.load("ee/todd-johnson-fiscal-piv.pem").cert();
		keysupportOrg = CertFixtures.load("ee/keysupport-org-tls-dns.pem").cert();
	}

	@Test
	void getSubjectAlternativeNames_realCerts() throws Exception {
		assertTrue(X509Util.getSubjectAlternativeNames(dodIdCa65).isEmpty());

		List<SANValue> tejSans = X509Util.getSubjectAlternativeNames(tejPiv);
		assertEquals(4, tejSans.size());
		assertTrue(tejSans.stream().anyMatch(s -> "otherName:userPrincipalName".equals(s.type)
				&& "TODD.JOHNSON@FISCAL.TREASURY.GOV".equals(s.value)));
		assertTrue(tejSans.stream().anyMatch(s -> "rfc822Name".equals(s.type)
				&& "Todd.Johnson@fiscal.treasury.gov".equals(s.value)));
		assertTrue(tejSans.stream().anyMatch(s -> "otherName:pivFASC-N".equals(s.type)
				&& !s.value.startsWith("#") && s.value.matches("[0-9A-F]+")));
		assertTrue(tejSans.stream().anyMatch(s -> "uniformResourceIdentifier".equals(s.type)
				&& s.value.startsWith("urn:uuid:")));

		List<SANValue> dnsSans = X509Util.getSubjectAlternativeNames(keysupportOrg);
		assertEquals(1, dnsSans.size());
		assertEquals("dNSName", dnsSans.get(0).type);
		assertEquals("keysupport.org", dnsSans.get(0).value);
	}

	@Test
	void getSubjectAlternativeNames_syntheticAllGeneralNameTypes() throws Exception {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair kp = kpg.generateKeyPair();

		ASN1EncodableVector otherNameVec = new ASN1EncodableVector();
		otherNameVec.add(new ASN1ObjectIdentifier("1.2.3.4.5.6"));
		otherNameVec.add(new DERTaggedObject(true, 0, new DERUTF8String("custom-other")));

		GeneralNames generalNames = new GeneralNames(new GeneralName[] {
				new GeneralName(GeneralName.otherName, new DERSequence(otherNameVec)),
				new GeneralName(GeneralName.rfc822Name, new DERIA5String("user@example.gov")),
				new GeneralName(GeneralName.dNSName, new DERIA5String("vss.example.gov")),
				new GeneralName(GeneralName.x400Address, new DERSequence()),
				new GeneralName(GeneralName.directoryName, new X500Name("CN=Test Dir")),
				new GeneralName(GeneralName.ediPartyName, new DERSequence(new DERTaggedObject(false, 1, new DERUTF8String("EDI")))),
				new GeneralName(GeneralName.uniformResourceIdentifier, new DERIA5String("https://example.gov")),
				new GeneralName(GeneralName.iPAddress, new DEROctetString(new byte[] { 127, 0, 0, 1 })),
				new GeneralName(GeneralName.registeredID, new ASN1ObjectIdentifier("2.16.840.1.101.3.2.1.3.1"))
		});

		JcaX509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
				new X500Name("CN=Test CA"),
				BigInteger.ONE,
				new Date(0),
				new Date(System.currentTimeMillis() + 86_400_000L),
				new X500Name("CN=Test Leaf"),
				kp.getPublic());
		builder.addExtension(Extension.subjectAlternativeName, false, generalNames);

		ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
				.setProvider(BouncyCastleFipsProvider.PROVIDER_NAME)
				.build(kp.getPrivate());
		X509Certificate cert = new JcaX509CertificateConverter()
				.setProvider(BouncyCastleFipsProvider.PROVIDER_NAME)
				.getCertificate(builder.build(signer));

		List<SANValue> sans = X509Util.getSubjectAlternativeNames(cert);
		assertEquals(9, sans.size());
		assertEquals("otherName", sans.get(0).type);
		assertEquals("rfc822Name", sans.get(1).type);
		assertEquals("user@example.gov", sans.get(1).value);
		assertEquals("dNSName", sans.get(2).type);
		assertEquals("vss.example.gov", sans.get(2).value);
		assertEquals("x400Address", sans.get(3).type);
		assertEquals("directoryName", sans.get(4).type);
		assertEquals("ediPartyName", sans.get(5).type);
		assertEquals("uniformResourceIdentifier", sans.get(6).type);
		assertEquals("https://example.gov", sans.get(6).value);
		assertEquals("iPAddress", sans.get(7).type);
		assertEquals("registeredID", sans.get(8).type);
		assertEquals("2.16.840.1.101.3.2.1.3.1", sans.get(8).value);
	}

	@Test
	void strS256HexString_matchesKnownSha256() {
		assertEquals("E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855",
				X509Util.strS256HexString(""));
		assertEquals("BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD",
				X509Util.strS256HexString("abc"));
	}

	@Test
	void x5tS256_and_byteArrayToHexString_consistentWithSha256() throws Exception {
		String x5t = X509Util.x5tS256(tejPiv);
		assertNotNull(x5t);
		assertEquals(43, x5t.length());
		assertFalse(x5t.contains("="));
		assertFalse(x5t.contains("+"));
		assertFalse(x5t.contains("/"));

		byte[] expectedDigest = MessageDigest.getInstance("SHA-256").digest(tejPiv.getEncoded());
		byte[] decodedFromX5t = Base64.getUrlDecoder().decode(x5t);
		assertEquals(X509Util.byteArrayToHexString(expectedDigest), X509Util.byteArrayToHexString(decodedFromX5t));

		assertEquals("", X509Util.byteArrayToHexString(null));
		assertEquals("", X509Util.byteArrayToHexString(new byte[0]));
		assertEquals("000F10FF", X509Util.byteArrayToHexString(new byte[] { 0x00, 0x0f, 0x10, (byte) 0xff }));
	}

	@Test
	void iso8601DateString_formatsUtcWithOffset() {
		assertEquals("1970-01-01T00:00:00.000+0000", X509Util.ISO8601DateString(new Date(0L)));
		assertEquals("2025-01-07T14:47:19.123+0000", X509Util.ISO8601DateString(new Date(1736261239123L)));
	}

	@Test
	void policyNodeToJSON_convertsTreeAndHandlesNull() {
		assertNull(X509Util.policyNodeToJSON(null));

		PolicyNode leaf = new StubPolicyNode(null, "2.16.840.1.101.3.2.1.3.13", 1, true,
				Set.of("2.16.840.1.101.3.2.1.3.13"), Collections.emptyList());
		PolicyNode root = new StubPolicyNode(null, "2.5.29.32.0", 0, false,
				Set.of("2.16.840.1.101.3.2.1.3.13"), List.of(leaf));

		PKIXPolicyNode jsonRoot = X509Util.policyNodeToJSON(root);
		assertNotNull(jsonRoot);
		assertEquals("2.5.29.32.0", jsonRoot.validPolicy);
		assertFalse(jsonRoot.critical);
		assertEquals(0, jsonRoot.depth);
		assertNotNull(jsonRoot.children);
		assertEquals(1, jsonRoot.children.size());

		PKIXPolicyNode jsonLeaf = jsonRoot.children.iterator().next();
		assertEquals("2.16.840.1.101.3.2.1.3.13", jsonLeaf.validPolicy);
		assertTrue(jsonLeaf.critical);
		assertEquals(1, jsonLeaf.depth);
		assertNull(jsonLeaf.children);
	}

	static Stream<CertFixture> pemFixtures() throws Exception {
		return CertFixtures.allStream();
	}

	@ParameterizedTest(name = "PEM fixture X509Util checks: {0}")
	@MethodSource("pemFixtures")
	void pemFixture_x5tAndSubjectAlternativeNames(CertFixture fixture) throws Exception {
		X509Certificate cert = fixture.cert();

		String x5t = X509Util.x5tS256(cert);
		assertNotNull(x5t);
		assertEquals(43, x5t.length());
		assertFalse(x5t.contains("="));
		byte[] expectedDigest = MessageDigest.getInstance("SHA-256").digest(cert.getEncoded());
		assertEquals(X509Util.byteArrayToHexString(expectedDigest),
				X509Util.byteArrayToHexString(Base64.getUrlDecoder().decode(x5t)));

		List<SANValue> sans = X509Util.getSubjectAlternativeNames(cert);
		assertNotNull(sans);
		if (cert.getSubjectAlternativeNames() != null) {
			assertEquals(cert.getSubjectAlternativeNames().size(), sans.size());
		} else {
			assertTrue(sans.isEmpty());
		}
	}

	private record StubPolicyNode(
			PolicyNode parent,
			String validPolicy,
			int depth,
			boolean critical,
			Set<String> expectedPolicies,
			List<PolicyNode> childrenList) implements PolicyNode {
		@Override public PolicyNode getParent() { return parent; }
		@Override public Iterator<? extends PolicyNode> getChildren() { return childrenList.iterator(); }
		@Override public int getDepth() { return depth; }
		@Override public String getValidPolicy() { return validPolicy; }
		@Override public Set<? extends java.security.cert.PolicyQualifierInfo> getPolicyQualifiers() { return Collections.emptySet(); }
		@Override public Set<String> getExpectedPolicies() { return expectedPolicies; }
		@Override public boolean isCritical() { return critical; }
	}
}
