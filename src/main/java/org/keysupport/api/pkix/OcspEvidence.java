package org.keysupport.api.pkix;

import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.CertificateEncodingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReq;
import org.bouncycastle.cert.ocsp.OCSPReqBuilder;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.cert.ocsp.SingleResp;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads an OCSP {@code nextUpdate} for cache bounding. The JDK revocation
 * checker does not retain the response it just verified, so this is a second
 * request and is used only to decide how long a success may be cached. A
 * failure leaves the caller on the configured maximum.
 */
final class OcspEvidence {

	private static final Logger LOG = LoggerFactory.getLogger(OcspEvidence.class);
	private static final String OCSP_METHOD = "1.3.6.1.5.5.7.48.1";
	private static final Duration TIMEOUT = Duration.ofSeconds(5);

	private static final HttpClient HTTP = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_1_1)
			.connectTimeout(TIMEOUT)
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();

	private OcspEvidence() {
	}

	static Date nextUpdate(X509Certificate certificate, X509Certificate issuer) {
		if (null == certificate || null == issuer) {
			return null;
		}
		List<String> urls = ocspUrls(certificate);
		if (urls.isEmpty()) {
			return null;
		}
		byte[] request;
		try {
			request = buildRequest(certificate, issuer);
		} catch (OCSPException | IOException | OperatorCreationException | CertificateEncodingException e) {
			LOG.debug("Unable to build OCSP request for cache bound", e);
			return null;
		}
		for (String url : urls) {
			try {
				byte[] body = post(url, request);
				Date next = nextUpdateFromResponse(body, certificate.getSerialNumber());
				if (null != next) {
					return next;
				}
			} catch (IOException | InterruptedException | OCSPException e) {
				if (e instanceof InterruptedException) {
					Thread.currentThread().interrupt();
					return null;
				}
				LOG.debug("OCSP cache-bound query failed for {}", url, e);
			}
		}
		return null;
	}

	static Date nextUpdateFromResponse(byte[] der, BigInteger serialNumber) throws IOException, OCSPException {
		if (null == der || null == serialNumber) {
			return null;
		}
		OCSPResp resp = new OCSPResp(der);
		if (resp.getStatus() != OCSPResp.SUCCESSFUL) {
			return null;
		}
		Object parsed = resp.getResponseObject();
		if (!(parsed instanceof BasicOCSPResp basic)) {
			return null;
		}
		Date earliest = null;
		for (SingleResp single : basic.getResponses()) {
			if (null == single.getCertID() || !serialNumber.equals(single.getCertID().getSerialNumber())) {
				continue;
			}
			Date next = single.getNextUpdate();
			if (null != next && (null == earliest || next.before(earliest))) {
				earliest = next;
			}
		}
		return earliest;
	}

	private static byte[] buildRequest(X509Certificate certificate, X509Certificate issuer)
			throws OCSPException, IOException, OperatorCreationException, CertificateEncodingException {
		DigestCalculatorProvider digests = new JcaDigestCalculatorProviderBuilder()
				.setProvider(ValidatePKIX.providerName())
				.build();
		CertificateID id = new CertificateID(
				digests.get(CertificateID.HASH_SHA1),
				new X509CertificateHolder(issuer.getEncoded()),
				certificate.getSerialNumber());
		OCSPReq req = new OCSPReqBuilder().addRequest(id).build();
		return req.getEncoded();
	}

	private static byte[] post(String url, byte[] request) throws IOException, InterruptedException {
		HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(url))
				.timeout(TIMEOUT)
				.header("Content-Type", "application/ocsp-request")
				.header("Accept", "application/ocsp-response")
				.POST(HttpRequest.BodyPublishers.ofByteArray(request))
				.build();
		HttpResponse<byte[]> response = HTTP.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
		if (response.statusCode() != 200) {
			throw new IOException("OCSP responder returned HTTP " + response.statusCode());
		}
		return response.body();
	}

	private static List<String> ocspUrls(X509Certificate certificate) {
		byte[] ext = certificate.getExtensionValue(Extension.authorityInfoAccess.getId());
		if (null == ext) {
			return List.of();
		}
		try {
			ASN1OctetString oct = ASN1OctetString.getInstance(ASN1Primitive.fromByteArray(ext));
			AuthorityInformationAccess aia = AuthorityInformationAccess
					.getInstance(ASN1Primitive.fromByteArray(oct.getOctets()));
			List<String> urls = new ArrayList<>();
			for (AccessDescription ad : aia.getAccessDescriptions()) {
				if (!OCSP_METHOD.equals(ad.getAccessMethod().getId())) {
					continue;
				}
				GeneralName location = ad.getAccessLocation();
				if (location.getTagNo() != GeneralName.uniformResourceIdentifier) {
					continue;
				}
				String uri = location.getName().toString();
				if ((uri.startsWith("http://") || uri.startsWith("https://")) && !urls.contains(uri)) {
					urls.add(uri);
				}
				if (urls.size() == 2) {
					break;
				}
			}
			return urls;
		} catch (IOException | RuntimeException e) {
			return List.of();
		}
	}
}
