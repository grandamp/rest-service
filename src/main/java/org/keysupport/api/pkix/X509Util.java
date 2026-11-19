package org.keysupport.api.pkix;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.PolicyNode;
import java.security.cert.X509Certificate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.GeneralName;
import org.keysupport.api.LoggingUtil;
import org.keysupport.api.pojo.vss.PKIXPolicyNode;
import org.keysupport.api.pojo.vss.SANValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is intended to be a class with static methods to perform some basic
 * X509 parsing.
 */
public class X509Util {

	private static final Logger LOG = LoggerFactory.getLogger(X509Util.class);

	private static final ASN1ObjectIdentifier PIV_FASCN_OID = new ASN1ObjectIdentifier("2.16.840.1.101.3.6.6");
	private static final ASN1ObjectIdentifier UPN_OID = new ASN1ObjectIdentifier("1.3.6.1.4.1.311.20.2.3");
	private static final HexFormat UPPER_HEX = HexFormat.of().withUpperCase();
	private static final DateTimeFormatter ISO8601_UTC_FORMATTER = DateTimeFormatter
			.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
			.withZone(ZoneOffset.UTC);

	private X509Util() {
		/*
		 * Hidden constructor
		 */
	}

	/**
	 * Method getSubjectAlternativeNames.
	 *
	 * @param certificate X509Certificate
	 * @return List<SANValue>
	 * @throws IOException
	 */
	public static List<SANValue> getSubjectAlternativeNames(X509Certificate certificate) throws IOException {

		byte[] encodedExtension;
		List<SANValue> x509SubjectAltName;
		DEROctetString content;
		ASN1Sequence sequence;
		int tag;
		GeneralName generalName;

		/*
		 * We are pulling the DER encoded value of subjectAltName and we are going to
		 * parse each SAN value manually
		 * 
		 * The result should be rendered compatible with JSON
		 */
		encodedExtension = certificate.getExtensionValue("2.5.29.17");
		x509SubjectAltName = new ArrayList<>();
		if (null == encodedExtension) {
			return x509SubjectAltName;
		}
		content = (DEROctetString) ASN1Primitive.fromByteArray(encodedExtension);
		sequence = (ASN1Sequence) ASN1Primitive.fromByteArray(content.getOctets());
		Enumeration<?> it = sequence.getObjects();
		while (it.hasMoreElements()) {
			generalName = GeneralName.getInstance(it.nextElement());
			tag = generalName.getTagNo();
			switch (tag) {
			case GeneralName.otherName: {
				/*
				 * otherName requires the encoding of the value be defined by the type-id, which
				 * is an ObjectIdentifier
				 */
				ASN1Sequence otherName = (ASN1Sequence) generalName.getName();
				ASN1ObjectIdentifier typeId = (ASN1ObjectIdentifier) otherName.getObjectAt(0);
				ASN1TaggedObject value = (ASN1TaggedObject) otherName.getObjectAt(1);
				if (typeId.equals(PIV_FASCN_OID)) {
					/*
					 * Remove prepended "#", if it exists. Assuming it is injected by Java code
					 * somewhere, because that character is not part of the literal encoding.
					 */
					String rawFascnValue = value.getObject().toString().toUpperCase();
					if (rawFascnValue.startsWith("#")) {
						rawFascnValue = rawFascnValue.substring(1);
					}
					SANValue fascn = new SANValue();
					fascn.type = "otherName:pivFASC-N";
					fascn.value = rawFascnValue;
					x509SubjectAltName.add(fascn);
				} else if (typeId.equals(UPN_OID)) {
					SANValue upn = new SANValue();
					upn.type = "otherName:userPrincipalName";
					upn.value = value.getObject().toString();
					x509SubjectAltName.add(upn);
				} else {
					SANValue ukOther = new SANValue();
					ukOther.type = "otherName";
					ukOther.value = generalName.getName().toString();
					x509SubjectAltName.add(ukOther);
				}
				break;
			}
			case GeneralName.rfc822Name: {
				SANValue rfc822Name = new SANValue();
				rfc822Name.type = "rfc822Name";
				rfc822Name.value = generalName.getName().toString();
				x509SubjectAltName.add(rfc822Name);
				break;
			}
			case GeneralName.dNSName: {
				SANValue dNSName = new SANValue();
				dNSName.type = "dNSName";
				dNSName.value = generalName.getName().toString();
				x509SubjectAltName.add(dNSName);
				break;
			}
			case GeneralName.x400Address: {
				SANValue x400Address = new SANValue();
				x400Address.type = "x400Address";
				x400Address.value = generalName.getName().toString();
				x509SubjectAltName.add(x400Address);
				break;
			}
			case GeneralName.directoryName: {
				SANValue directoryName = new SANValue();
				directoryName.type = "directoryName";
				directoryName.value = generalName.getName().toString();
				x509SubjectAltName.add(directoryName);
				break;
			}
			case GeneralName.ediPartyName: {
				SANValue ediPartyName = new SANValue();
				ediPartyName.type = "ediPartyName";
				ediPartyName.value = generalName.getName().toString();
				x509SubjectAltName.add(ediPartyName);
				break;
			}
			case GeneralName.uniformResourceIdentifier: {
				SANValue uniformResourceIdentifier = new SANValue();
				uniformResourceIdentifier.type = "uniformResourceIdentifier";
				uniformResourceIdentifier.value = generalName.getName().toString();
				x509SubjectAltName.add(uniformResourceIdentifier);
				break;
			}
			case GeneralName.iPAddress: {
				SANValue iPAddress = new SANValue();
				iPAddress.type = "iPAddress";
				iPAddress.value = generalName.getName().toString();
				x509SubjectAltName.add(iPAddress);
				break;
			}
			case GeneralName.registeredID: {
				SANValue registeredID = new SANValue();
				registeredID.type = "registeredID";
				registeredID.value = generalName.getName().toString();
				x509SubjectAltName.add(registeredID);
				break;
			}
			default: {
				// No real possible way to get here
				SANValue unknown = new SANValue();
				unknown.type = "unknown";
				unknown.value = generalName.getName().toString();
				x509SubjectAltName.add(unknown);
				break;
			}
			}
		}
		return x509SubjectAltName;
	}

	/**
	 * Return Hex String of SHA-256 digest of the input string
	 *
	 * @param str
	 * @return
	 */
	public static String strS256HexString(String str) {
		byte[] digest = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(str.getBytes(StandardCharsets.UTF_8));
			digest = md.digest();
		} catch (NoSuchAlgorithmException e) {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Invalid Algorithm", "stacktrace", LoggingUtil.stackTraceToString(e))));
		}
		return byteArrayToHexString(digest);
	}

	/**
	 * Return Base64url (unpadded) String of SHA-256 digest of the certificate
	 *
	 * @param cert
	 * @return
	 */
	public static String x5tS256(X509Certificate cert) {
		byte[] digest = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(cert.getEncoded());
			digest = md.digest();
		} catch (NoSuchAlgorithmException | CertificateEncodingException e) {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Error decoding certificate", "stacktrace", LoggingUtil.stackTraceToString(e))));
		}
		if (digest == null) {
			return null;
		}
		return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
	}

	public static String ISO8601DateString(Date date) {
		return ISO8601_UTC_FORMATTER.format(date.toInstant());
	}

	/**
	 * Convert a byte array to an uppercase Hex String.
	 *
	 * @param ba A byte array
	 * @return Uppercase hexadecimal string representing the byte array, or "" if null
	 */
	public static String byteArrayToHexString(byte[] ba) {
		if (ba == null) {
			return "";
		}
		return UPPER_HEX.formatHex(ba);
	}

	public static PKIXPolicyNode policyNodeToJSON(PolicyNode policyNode) {
		if (null == policyNode) {
			return null;
		}
		PKIXPolicyNode pkixPolicyNode = new PKIXPolicyNode();
		pkixPolicyNode.validPolicy = policyNode.getValidPolicy();
		pkixPolicyNode.critical = policyNode.isCritical();
		pkixPolicyNode.depth = policyNode.getDepth();
		pkixPolicyNode.expectedPolicies = policyNode.getExpectedPolicies();
		pkixPolicyNode.children = null;
		Set<PKIXPolicyNode> children = new HashSet<>();
		Iterator<? extends PolicyNode> itr = policyNode.getChildren();
		while (itr.hasNext()) {
			children.add(policyNodeToJSON(itr.next()));
		}
		if (!children.isEmpty()) {
			pkixPolicyNode.children = children;
		}
		return pkixPolicyNode;
	}
}
