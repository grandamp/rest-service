package org.keysupport.api.pkix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Security;
import java.security.cert.CertificateFactory;
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

	// Real certificate from current_tests.sh: DoD ID CA-65 (no SAN extension)
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

	// Real certificate from current_tests.sh: Todd E. Johnson PIV (OCIO CA5)
	// Contains otherName:pivFASC-N, otherName:userPrincipalName, rfc822Name, uniformResourceIdentifier
	private static final String TEJ_PIV_B64 =
			"MIIHtzCCBp+gAwIBAgIEYkH8IjANBgkqhkiG9w0BAQsFADCBgjELMAkGA1UEBhMC"
			+ "VVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDEjMCEGA1UECxMaRGVwYXJ0bWVu"
			+ "dCBvZiB0aGUgVHJlYXN1cnkxIjAgBgNVBAsTGUNlcnRpZmljYXRpb24gQXV0aG9y"
			+ "aXRpZXMxEDAOBgNVBAsTB09DSU8gQ0EwHhcNMjIwODE1MTYyMzQxWhcNMjUwODE1"
			+ "MTY1MTM3WjCBrTELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVu"
			+ "dDEjMCEGA1UECxMaRGVwYXJ0bWVudCBvZiB0aGUgVHJlYXN1cnkxJTAjBgNVBAsT"
			+ "HEJ1cmVhdSBvZiB0aGUgRmlzY2FsIFNlcnZpY2UxDzANBgNVBAsTBlBlb3BsZTEn"
			+ "MA0GA1UEBRMGNDAzNjExMBYGA1UEAxMPVG9kZCBFLiBKb2huc29uMIIBIjANBgkq"
			+ "hkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArB57JliuGCIvEmVb4Ro/UMZBxkw/9RpH"
			+ "x8K39rnhfndwgo3qTmZWToJAI3EDFADUxzOhkhoL44gbzub3MjmLWTrFU9UWHCGf"
			+ "3XlMfZxO0MibtE9U2hFPQ37VtxcTMsN+DQbxEka/P6jrvzfWrI1CDDs5mvfFCJu2"
			+ "Os+xQemwm95pqvncHT3bF4Z+uC3oDmz/LW2XFrDPjH2Cy5oRTst1X0wrvQwyDBky"
			+ "FMbEqhXp+YBGLnXOO71P+9nxEUQyVeVbPu0gqT+cjhdIvPH7oppDjW6wQnh5nTkT"
			+ "ZBjek0QHx6CoOJveQjTmxeqYEcwq9G+FJJBKLWWRB1kgJwVTkb8ngQIDAQABo4IE"
			+ "BjCCBAIwDgYDVR0PAQH/BAQDAgeAMBcGA1UdIAQQMA4wDAYKYIZIAWUDAgEDDTAy"
			+ "BgNVHSUEKzApBgorBgEEAYI3FAICBggrBgEFBQcDAgYHKwYBBQIDBAYIKwYBBQUH"
			+ "AxUwEAYJYIZIAWUDBgkBBAMBAQAwggEIBggrBgEFBQcBAQSB+zCB+DAwBggrBgEF"
			+ "BQcwAoYkaHR0cDovL3BraS50cmVhcy5nb3YvdG9jYV9lZV9haWEucDdjMIGgBggr"
			+ "BgEFBQcwAoaBk2xkYXA6Ly9sZGFwLnRyZWFzLmdvdi9vdT1PQ0lPJTIwQ0Esb3U9"
			+ "Q2VydGlmaWNhdGlvbiUyMEF1dGhvcml0aWVzLG91PURlcGFydG1lbnQlMjBvZiUy"
			+ "MHRoZSUyMFRyZWFzdXJ5LG89VS5TLiUyMEdvdmVybm1lbnQsYz1VUz9jQUNlcnRp"
			+ "ZmljYXRlO2JpbmFyeTAhBggrBgEFBQcwAYYVaHR0cDovL29jc3AudHJlYXMuZ292"
			+ "MIG3BgNVHREEga8wgaygMAYKKwYBBAGCNxQCA6AiDCBUT0RELkpPSE5TT05ARklT"
			+ "Q0FMLlRSRUFTVVJZLkdPVoEgVG9kZC5Kb2huc29uQGZpc2NhbC50cmVhc3VyeS5n"
			+ "b3agJwYIYIZIAWUDBgagGwQZ0gJEWCELbNQ2AQ2haFoBCEOSEaSCAhDD64YtdXJu"
			+ "OnV1aWQ6MWEzOWY5MWQtYTgzYy00ZjRlLWFlYjMtMmM3YWVmZGE4ZGFiMIIBiQYD"
			+ "VR0fBIIBgDCCAXwwJ6AloCOGIWh0dHA6Ly9wa2kudHJlYXMuZ292L09DSU9fQ0E1"
			+ "LmNybDCCAU+gggFLoIIBR6SBlzCBlDELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1Uu"
			+ "Uy4gR292ZXJubWVudDEjMCEGA1UECxMaRGVwYXJ0bWVudCBvZiB0aGUgVHJlYXN1"
			+ "cnkxIjAgBgNVBAsTGUNlcnRpZmljYXRpb24gQXV0aG9yaXRpZXMxEDAOBgNVBAsT"
			+ "B09DSU8gQ0ExEDAOBgNVBAMTB0NSTDMxMDKGgapsZGFwOi8vbGRhcC50cmVhcy5n"
			+ "b3YvY249Q1JMMzEwMixvdT1PQ0lPJTIwQ0Esb3U9Q2VydGlmaWNhdGlvbiUyMEF1"
			+ "dGhvcml0aWVzLG91PURlcGFydG1lbnQlMjBvZiUyMHRoZSUyMFRyZWFzdXJ5LG89"
			+ "VS5TLiUyMEdvdmVybm1lbnQsYz1VUz9jZXJ0aWZpY2F0ZVJldm9jYXRpb25MaXN0"
			+ "O2JpbmFyeTAfBgNVHSMEGDAWgBTNmhxgcsHrvq7Fq6xJkOtNjvHfrjAdBgNVHQ4E"
			+ "FgQUOr9oOidr/PJmV/STexf+AdOEgNwwDQYJKoZIhvcNAQELBQADggEBABowU1EH"
			+ "NoHIB0yHpmisSJS7NDMoKQp2BVeDkwSmPsf3GN3hMXIfaNsGaGpweEUT4wM3H/C8"
			+ "3Z6NQZaJcWBm7s1jDQvGqBc+sU0YnKzPh61VCVENFDZT/tlwqS8DCEBA+Jk681mL"
			+ "dpWLhpkp5rrmkc2Hcl0RqtKX6zgBtj3WyRLqW/+zrqTXnBGl6yTbbmUxVmvck3xf"
			+ "XHe7M6ytBlzvTEOFzlzjkaDO/keYZDuutlaw/F9AqyBVOP6Jjx2JOyfx/EZTpgc4"
			+ "+rIarfRqJ+YVwRe0ULHh3+1BV0qr6apxd+02TsHgjwGSstO4jviJEBNxZZq2lalD"
			+ "WD8gVmDkhFX4UIQ=";

	// Real certificate from current_tests.sh: keysupport.org (GTS CA 1D4, dNSName SAN)
	private static final String KEYSUPPORT_ORG_B64 =
			"MIIFYjCCBEqgAwIBAgIRAOgIWMWQtieIECed/Q2JONIwDQYJKoZIhvcNAQELBQAw"
			+ "RjELMAkGA1UEBhMCVVMxIjAgBgNVBAoTGUdvb2dsZSBUcnVzdCBTZXJ2aWNlcyBM"
			+ "TEMxEzARBgNVBAMTCkdUUyBDQSAxRDQwHhcNMjIxMDEwMjAwMDU2WhcNMjMwMTA4"
			+ "MjAwMDU1WjAZMRcwFQYDVQQDEw5rZXlzdXBwb3J0Lm9yZzCCASIwDQYJKoZIhvcN"
			+ "AQEBBQADggEPADCCAQoCggEBALftackigK3dmFReOqr4txPVJrdTPRMbsz3+4ueB"
			+ "GtBd2OIKcFXDi8apCf76Fh8WBheDVOyDzUhkag+mTnPw0BXqYNyFntPcwosDkv7k"
			+ "viJgF4KmSWf3myTYmHoc0CAqoRqYsfTFkB7fK83OBnevqSZ8pgc2X7h6qSgx9Z/0"
			+ "dprl+2mWndIB82SZSCzpuaGXs5SPFDZVrB8Y7fUWqu1nPaTLfOzIlAIf8SHf1+f8"
			+ "1D0Mg1rm/rAAi9HFwx+fLPnFvZUUWeErPEi9JmBJzU+TJdF3dDRldtJVgFdQXcvM"
			+ "MwgSjmlL+ToOwf/VRU/usIVGxLI/qoxLj+Y97ht5tzMILXMCAwEAAaOCAnYwggJy"
			+ "MA4GA1UdDwEB/wQEAwIFoDATBgNVHSUEDDAKBggrBgEFBQcDATAMBgNVHRMBAf8E"
			+ "AjAAMB0GA1UdDgQWBBQHEqvLbOftuY4bf2Ojy3InQEZctzAfBgNVHSMEGDAWgBQl"
			+ "4hgOsleRlCrl1F2GkIPeU7O4kjB4BggrBgEFBQcBAQRsMGowNQYIKwYBBQUHMAGG"
			+ "KWh0dHA6Ly9vY3NwLnBraS5nb29nL3MvZ3RzMWQ0L1NRMUZ6enViUFlNMDEGCCsG"
			+ "AQUFBzAChiVodHRwOi8vcGtpLmdvb2cvcmVwby9jZXJ0cy9ndHMxZDQuZGVyMBkG"
			+ "A1UdEQQSMBCCDmtleXN1cHBvcnQub3JnMCEGA1UdIAQaMBgwCAYGZ4EMAQIBMAwG"
			+ "CisGAQQB1nkCBQMwPAYDVR0fBDUwMzAxoC+gLYYraHR0cDovL2NybHMucGtpLmdv"
			+ "b2cvZ3RzMWQ0L0oyajNSQ2lFeTA0LmNybDCCAQUGCisGAQQB1nkCBAIEgfYEgfMA"
			+ "8QB3AHoyjFTYty22IOo44FIe6YQWcDIThU070ivBOlejUutSAAABg8PAFU0AAAQD"
			+ "AEgwRgIhAInKXSrciK8OHtFWvQienDeIwBGCfDojh62EeoD62ngyAiEA6HO5HRCE"
			+ "00qCIKNCx6CuS9gDkvgrNm4xIS+gPc5Z8i8AdgCt9776fP8QyIudPZwePhhqtGcp"
			+ "Xc+xDCTKhYY069yCigAAAYPDwBVHAAAEAwBHMEUCIQDOGktHy094r8OdqDhS7IPj"
			+ "44qJ1V9RRdR2ZEq4Sz5WOwIgcAlszeHnUNnp/jlDJPk1vIBJxh8/lokFwDBFiUU1"
			+ "EhAwDQYJKoZIhvcNAQELBQADggEBAGiaDoyFu4aLlLvsYnIVlTvf6bfUZ0xf3F9g"
			+ "SyjJBxFxFMF4BTiWWbCXGCpBT7+hHLSvq2fKKzKn2bR17XnDJkcVNBA5kxc9MQu6"
			+ "YE26+QkV4nk23hNidmaRimuiq4FLivsp/9gDrU+q5lutXP6n7EYI0ibXtuGk+Tx/"
			+ "qW9xvT4V9snhhw+4ks0Yw0B3AcdwNqRgmKYOftOLDPivLe5+lTmxbiMIPXSvkXzG"
			+ "VDFwj8/D8APp173Jg6XmHPHC9DSYF33e0q3nmmKQOV3PjR4mlFkY5Ewv9pKLJRnb"
			+ "wp01CArMs2YYdSqERZnLCH69lcHM/kuEKgMmdgLnEky2Xa03c84=";

	private static CertificateFactory cf;
	private static X509Certificate dodIdCa65;
	private static X509Certificate tejPiv;
	private static X509Certificate keysupportOrg;

	@BeforeAll
	static void setUp() throws Exception {
		if (Security.getProvider(BouncyCastleFipsProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleFipsProvider());
		}
		cf = CertificateFactory.getInstance("X.509");
		dodIdCa65 = parseCert(DOD_ID_CA_65_B64);
		tejPiv = parseCert(TEJ_PIV_B64);
		keysupportOrg = parseCert(KEYSUPPORT_ORG_B64);
	}

	private static X509Certificate parseCert(String b64) throws Exception {
		byte[] der = Base64.getDecoder().decode(b64);
		return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(der));
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
