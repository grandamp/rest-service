package org.keysupport.api.pojo.vss.v1;

import java.io.IOException;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Locale;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

public class V1RestClientExample {

	static boolean debug = false;

	public static void main(String[] args) {

	}

	/**
	 * POST /vss/rest/
	 *
	 * @param cert
	 * @param valPol
	 * @param wantBackList
	 */
	public static V1VSSResponse validateCert(String vssHost, X509Certificate cert, String valPol,
			List<V1WantBackTypeToken> wantBackList) {
		return null;
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
			e.printStackTrace();
		}
		return byteArrayToString(digest);
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
	public static String byteArrayToString(byte[] ba) {
		if (ba == null) {
			return "";
		}
		StringBuffer hex = new StringBuffer(ba.length * 2);
		for (byte element : ba) {
			hex.append(Integer.toString((element & 0xff) + 0x100, 16).substring(1));
		}
		return hex.toString().toUpperCase(Locale.US);
	}

	private static final String numbers = "0123456789ABCDEF";

	/**
	 * Convert a Hex String to a byte array
	 *
	 * The following method converts a String object to a byte[] object, where the
	 * only valid input characters is "0123456789ABCDEF".
	 *
	 * @param s Hexidecimal String object to convert to a byte array
	 *
	 * @return byte[] A byte array
	 */
	public static byte[] stringToByteArray(String s) {
		if (s == null)
			return null;
		byte[] result = new byte[s.length() / 2];
		for (int i = 0; i < s.length(); i += 2) {
			int i1 = numbers.indexOf(s.charAt(i));
			int i2 = numbers.indexOf(s.charAt(i + 1));
			result[i / 2] = (byte) ((i1 << 4) | i2);
		}
		return result;
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
			e.printStackTrace();
		}
		sb.append(sw.getBuffer());
		return sb.toString();
	}
}