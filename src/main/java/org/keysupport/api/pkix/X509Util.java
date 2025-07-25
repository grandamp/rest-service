package org.keysupport.api.pkix;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.PolicyNode;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.keysupport.api.LoggingUtil;
import org.keysupport.api.pojo.vss.PKIXPolicyNode;
import org.keysupport.api.pojo.vss.SANValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is intended to be a class with static methods to perform some basing
 * X509 parsing.
 */
public class X509Util {

	private final static Logger LOG = LoggerFactory.getLogger(X509Util.class);

	/**
	 *
	 */
	private X509Util() {
		/*
		 * Hidden constructor
		 */
	}

	/**
	 * Method getSubjectAlternativeNames.
	 *
	 * @param certificate V1X509Certificate
	 * @return Map<String,String>
	 * @throws IOException
	 */
	public static List<SANValue> getSubjectAlternativeNames(X509Certificate certificate) throws IOException {

		byte[] encodedExtension;
		List<SANValue> x509SubjectAltName = null;
		DEROctetString content;
		ASN1Sequence sequence;
		Integer tag;
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
				/*
				 * pivFASC-N (2.16.840.1.101.3.6.6)
				 */
				if (typeId.equals(new ASN1ObjectIdentifier("2.16.840.1.101.3.6.6"))) {
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
				} else if (typeId.equals(new ASN1ObjectIdentifier("1.3.6.1.4.1.311.20.2.3"))) {
					/*
					 * userPrincipalName (1.3.6.1.4.1.311.20.2.3)
					 */
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
				registeredID.type = "iPAddress";
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
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
			md.update(str.getBytes(StandardCharsets.UTF_8));
			digest = md.digest();
		} catch (NoSuchAlgorithmException e) {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Invalid Algorithm", "stacktrace", LoggingUtil.stackTraceToString(e))));
		}
		String strS256 = byteArrayToHexString(digest);
		return strS256;
	}

	/**
	 * Return Base64 String of SHA-256 digest of the certificate
	 *
	 * @param cert
	 * @return
	 */
	public static String x5tS256(X509Certificate cert) {
		byte[] digest = null;
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
			md.update(cert.getEncoded());
			digest = md.digest();
		} catch (NoSuchAlgorithmException | CertificateEncodingException e) {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Error decoding certificate", "stacktrace", LoggingUtil.stackTraceToString(e))));
		}
		String x5tS256 = null;
		try {
			x5tS256 = Base64.getUrlEncoder().encodeToString(digest).replace("=", "");
		} catch (Throwable e) {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Error base64 encoding digest result", "stacktrace", LoggingUtil.stackTraceToString(e))));
		}
		return x5tS256;
	}

	/**
	 * Return a UUID derived from the UTF-8 String provided
	 *
	 * @param name
	 * @return
	 */
	public static UUID uuidFromName(String name) {
		return UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
	}

	public static String ISO8601DateString(Date date) {
		String dateString = null;
		SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		dFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		dateString = dFormat.format(date);
		return dateString;
	}

	public static Date ISO8601DateFromString(String dateString) {
		SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		Date d = null;
		try {
			d = dFormat.parse(dateString);
		} catch (ParseException e) {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Unable to parse date", "stacktrace", LoggingUtil.stackTraceToString(e))));
		}
		return d;
	}

	/**
	 * Convert a byte array to a Hex String
	 * 
	 * The following method converts a byte[] object to a String object, where the
	 * only output characters are "0123456789ABCDEF".
	 * 
	 * @param ba A byte array
	 * 
	 * @return String Hexidecimal String object which represents the contents of the
	 *         byte array
	 */
	public static String byteArrayToHexString(byte[] ba) {
		if (ba == null) {
			return "";
		}
		StringBuffer hex = new StringBuffer(ba.length * 2);
		for (int i = 0; i < ba.length; i++) {
			hex.append(Integer.toString((ba[i] & 0xff) + 0x100, 16).substring(1));
		}
		return hex.toString().toUpperCase(Locale.US);
	}

	public static Date dateFromHttpHeader(String headerDateValue) {
		SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		Date d = null;
		try {
			d = format.parse(headerDateValue);
		} catch (ParseException e) {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Unable to parse date", "stacktrace", LoggingUtil.stackTraceToString(e))));
		}
		return d;
	}

	public static String ISO8601DateStringFromHttpHeader(String headerDateValue) {
		return ISO8601DateString(dateFromHttpHeader(headerDateValue));
	}

	public static int calculateCacheTTL(Date then) {
		Date now = new Date();
		return (int) (then.getTime() - now.getTime()) / 1000;
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
		Set<PKIXPolicyNode> children = new HashSet<PKIXPolicyNode>();
		Iterator<? extends PolicyNode> itr = policyNode.getChildren();
		while (itr.hasNext()) {
			children.add(policyNodeToJSON(itr.next()));
		}
		if (!children.isEmpty()) {
			pkixPolicyNode.children = children;
		}
		return pkixPolicyNode;
	}

	/**
	 *
	 * @param cert
	 * @return String
	 * @throws CertificateException
	 * @throws IOException
	 */
	public static String toPEM(X509Certificate cert) throws CertificateException {
		StringBuffer sb = new StringBuffer();
		sb.append("Subject=" + cert.getSubjectX500Principal().getName() + "\n");
		sb.append("Issuer=" + cert.getIssuerX500Principal().getName() + "\n");
		sb.append("NotBefore=" + cert.getNotBefore() + "\n");
		sb.append("NotAfter=" + cert.getNotAfter() + "\n");
		sb.append(toPEM(cert.getEncoded()));
		return sb.toString();
	}

	public static String toPEM(byte[] certData) {
		StringBuffer sb = new StringBuffer();
		PemObject certPem = new PemObject("CERTIFICATE", certData);
		StringWriter sw = new StringWriter();
		PemWriter writer = new PemWriter(sw);
		try {
			writer.writeObject(certPem);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Unable to encode PEM", "stacktrace", LoggingUtil.stackTraceToString(e))));
		}
		sb.append(sw.getBuffer());
		return sb.toString();
	}

	/**
	 * Return Hex String of SHA-256 digest of the certificate
	 *
	 * @param cert
	 * @return String vss_cert_id
	 */
	public static String getVssCertId(X509Certificate cert) {

		byte[] digest = null;
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
			md.update(cert.getEncoded());
			digest = md.digest();
		} catch (NoSuchAlgorithmException | CertificateEncodingException e) {
			LOG.error(LoggingUtil.pojoToJson(Map.of("error", "Error decoding certificate", "stacktrace", LoggingUtil.stackTraceToString(e))));
		}

		return X509Util.byteArrayToHexString(digest);
	}
}
