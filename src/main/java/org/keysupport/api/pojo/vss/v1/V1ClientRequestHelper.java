package org.keysupport.api.pojo.vss.v1;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.keysupport.api.pkix.X509Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class V1ClientRequestHelper {

	private final static Logger LOG = LoggerFactory.getLogger(V1ClientRequestHelper.class);

	/**
	 * POST /vss/rest/
	 *
	 * @param cert
	 * @param valPol
	 * @param wantBackList
	 */
	public static V1VSSRequest validateCert(X509Certificate cert, String valPol,
			List<V1WantBackTypeToken> wantBackList) {

		byte[] certBytes = null;
		try {
			certBytes = cert.getEncoded();
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
		}
		V1X509Certificate certList[] = new V1X509Certificate[1];
		V1X509Certificate jsonCert = new V1X509Certificate();
		jsonCert.x509Certificate = Base64.getEncoder().encodeToString(certBytes);
		jsonCert.setAdditionalProperty("vssCertId", X509Util.getVssCertId(cert));
		certList[0] = jsonCert;
		V1VSSRequest request = new V1VSSRequest();
		request.validationPolicy = valPol;
		request.wantBackList = wantBackList;
		request.x509CertificateList = Arrays.asList(certList);
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			LOG.debug(objectMapper.writeValueAsString(request));
		} catch (JsonProcessingException e) {
			LOG.error("Error mapping request POJO", e);
		}
		return request;

	}

}