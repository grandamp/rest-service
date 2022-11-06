package org.keysupport.api.pkix;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import org.jose4j.base64url.internal.apache.commons.codec.binary.Base64;
import org.keysupport.api.RestServiceEventLogger;
import org.keysupport.api.controller.ServiceException;
import org.keysupport.api.pojo.vss.InvalidityReason;
import org.keysupport.api.pojo.vss.JsonX509Certificate;
import org.keysupport.api.pojo.vss.JsonX509CertificateList;
import org.keysupport.api.pojo.vss.ValidationFailureData;
import org.keysupport.api.pojo.vss.ValidationPolicy;
import org.keysupport.api.pojo.vss.ValidationSuccessData;
import org.keysupport.api.pojo.vss.VssResponse;
import org.keysupport.api.pojo.vss.WantBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 *   Original Source:  https://github.com/grandamp/KSJavaAPI/blob/master/src/org/keysupport/keystore/CertValidator.java
 * </pre>
 */

public class ValidatePKIX {

	private final static Logger LOG = LoggerFactory.getLogger(ValidatePKIX.class);

	public static VssResponse validate(X509Certificate cert, String x5tS256, ValidationPolicy valPol) {
		VssResponse response = new VssResponse();
		/*
		 * When decoding the certificate contents, don't always assume that the
		 * fields will be non-NULL. For example, cardAuth certs MAY have a NULL
		 * subject name.
		 */
		if (null != cert.getSubjectDN()) {
			response.x509SubjectName = cert.getSubjectDN().toString();
		}
		if (null != cert.getIssuerDN()) {
			response.x509IssuerName = cert.getIssuerDN().toString();
		}
		if (null != cert.getSerialNumber()) {
			response.x509SerialNumber = cert.getSerialNumber().toString();
		}
		/*
		 * Get subjectAltName values, swallow the exception as far as the
		 * consumer is concerned, but log it.
		 */
		try {
			response.x509SubjectAltName = X509Util.getSubjectAlternativeNames(cert);
		} catch (IOException e) {
			LOG.error("Error parsing Certificate SAN.", e);
		}
		/*
		 * Set validationTime and nextUpdate in the response
		 *
		 * Date Format now conforms to ISO 8601:
		 *
		 * http://xkcd.com/1179/
		 */
		Date now = new Date();
		SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		dFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		response.validationTime = dFormat.format(now);
		/*
		 * TODO: nextUpdate will be based on CRL, for now we will calculate a date that is one hour from now.
		 */
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(now);
	    calendar.add(Calendar.HOUR_OF_DAY, 1);
	    response.nextUpdate = dFormat.format(calendar.getTime());
	    /*
	     * Add x5t#S256
	     */
	    response.x5tS256 = x5tS256;
		/**
		 * <pre>
		 * Set System and Security properties to make the Sun provider: - Fetch CRLs via
		 * the CDP extension - Check OCSP via the AIA extension - Chase CA Issuers via
		 * the AIA extension
		 * 
		 * TODO: Consider writing our own provider that leverages cached objects.
		 * 
		 * The AIA and CDP chases would be valuable to update a local cache.
		 * 
		 * The OCSP responses would be valuable *if* the CA is not able to produce a CRL
		 * within 24 hours within the FPKI (or any issuing CA or intermediate the
		 * relying party is willing to trust).
		 * 
		 * 
		 * <pre>
		 */
		System.setProperty("com.sun.security.enableCRLDP", "true");
		Security.setProperty("ocsp.enable", "true");
		System.setProperty("com.sun.security.enableAIAcaIssuers", "true");
		/*
		 * 
		 */
		try {
			cert.checkValidity();
		} catch (CertificateExpiredException e) { 
			RestServiceEventLogger.logEvent(response, e);
			ValidationFailureData vfd = new ValidationFailureData();
			vfd.isAffirmativelyInvalid = true;
			List<InvalidityReason> invalidityReasonList = new ArrayList<InvalidityReason>();
			InvalidityReason iReason = new InvalidityReason();
			iReason.invalidityReasonToken = "FAIL";
			iReason.invalidityReasonText = e.getMessage();
			invalidityReasonList.add(iReason);
			vfd.invalidityReasonList = invalidityReasonList;
			response.validationFailureData = vfd;
			return response;
		} catch (CertificateNotYetValidException e) {
			RestServiceEventLogger.logEvent(response, e);
			ValidationFailureData vfd = new ValidationFailureData();
			vfd.isAffirmativelyInvalid = true;
			List<InvalidityReason> invalidityReasonList = new ArrayList<InvalidityReason>();
			InvalidityReason iReason = new InvalidityReason();
			iReason.invalidityReasonToken = "FAIL";
			iReason.invalidityReasonText = e.getMessage();
			invalidityReasonList.add(iReason);
			vfd.invalidityReasonList = invalidityReasonList;
			response.validationFailureData = vfd;
			return response;
		}
		/*
		 * Before we dive into RFC5280, we should perform a minimum algorithm/keySize check. 
		 */
		/*
		 * Set RFC 5280 Inputs based on the selected certificate and validation policy
		 */
		X509CertSelector selector = new X509CertSelector();
		selector.setCertificate(cert);
		/*
		 * Initialize the TrustAnchor via the ValidationPolicy.
		 * 
		 * TODO: It would be more efficient if the TrustAnchor was already rendered as
		 * an X509Certificate
		 */
		String pemCert = valPol.trustAnchors.get(0).x509Certificate;
		X509Certificate ta = null;
		try {
			byte[] certBytes = null;
			CertificateFactory cf = null;
			ByteArrayInputStream bais = null;
			try {
				certBytes = Base64.decodeBase64(pemCert);
			} catch (Throwable e) {
				LOG.error("Internal Validation Error", e);
				throw new ServiceException("Internal Validation Error");
			}
			if (null != certBytes) {
				cf = CertificateFactory.getInstance("X509");
				bais = new ByteArrayInputStream(certBytes);
				ta = (X509Certificate) cf.generateCertificate(bais);
			} else {
				LOG.error("Internal Validation Error, null certBytes");
				throw new ServiceException("Internal Validation Error");
			}
		} catch (CertificateException e) {
			LOG.error("Internal Validation Error", e);
			throw new ServiceException("Internal Validation Error");
		}
		LOG.debug("Trust Anchor:\n" + ta.toString());
		TrustAnchor anchor = new TrustAnchor(ta, null);
		List<Certificate> cert_list = new ArrayList<Certificate>();
		LOG.debug("Cert to validate:\n" + cert.toString());
		/*
		 * Temp debug code to see if TA signed this cert
		 */
		LOG.debug("Verifiying if TA Signed this cert:\n");
		try {
			cert.verify(ta.getPublicKey());
			LOG.debug("Cert to validate is signed by Trust Anchor");
		} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException
				| SignatureException e) {
			LOG.debug("Cert to validate is *not* signed by Trust Anchor");
		}
		cert_list.add(ta);
		cert_list.add(cert);
		CertStoreParameters cparam = new CollectionCertStoreParameters(cert_list);
		CertStore cstore = null;
		try {
			cstore = CertStore.getInstance("Collection", cparam, "SUN");
		} catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
			LOG.error("Internal Validation Error", e);
			throw new ServiceException("Internal Validation Error");
		}
		PKIXBuilderParameters params = null;
		try {
			params = new PKIXBuilderParameters(Collections.singleton(anchor), selector);
		} catch (InvalidAlgorithmParameterException e) {
			LOG.error("Internal Validation Error", e);
			throw new ServiceException("Internal Validation Error");
		}
		params.setInitialPolicies(new HashSet<>(valPol.userPolicySet));
		params.setPolicyMappingInhibited(valPol.inhibitPolicyMapping);
		params.setExplicitPolicyRequired(valPol.requireExplicitPolicy);
		params.setAnyPolicyInhibited(valPol.inhibitAnyPolicy);
		params.addCertStore(cstore);
		/**
		 * <pre>
		 * TODO:  Add FPKI Intermediate Store
		 * 
		 * - https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/cert/CertStore.html
		 * </pre>
		 */
		/*
		 * Build the certificate path
		 */
		CertPathBuilder cpb = null;
		try {
			cpb = CertPathBuilder.getInstance("PKIX", "SUN");
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			LOG.error("Internal Validation Error", e);
			throw new ServiceException("Internal Validation Error");
		}
		PKIXCertPathBuilderResult result = null;
		/*
		 * TODO: The path construction may fail if the path can not be built, which we
		 * should immediately return a response to the client.
		 * 
		 * TODO: Consider isolating the following into it's own method, or class that
		 * accommodates multiple exception types.
		 */
		try {
			result = (PKIXCertPathBuilderResult) cpb.build(params);
		} catch (InvalidAlgorithmParameterException e) {
			LOG.error("Error with CertPathBuilder", e);
		} catch (CertPathBuilderException e) {
			RestServiceEventLogger.logEvent(response, e);
			/*
			 * Construct and return validation response.
			 * 
			 * To be safe, we will flag `isAffirmativelyInvalid` as `true`.
			 * 
			 * Otherwise, we need to instrument more data from e.getCause() and make a
			 * decision.
			 */
			ValidationFailureData vfd = new ValidationFailureData();
			vfd.isAffirmativelyInvalid = true;
			List<InvalidityReason> invalidityReasonList = new ArrayList<InvalidityReason>();
			InvalidityReason iReason = new InvalidityReason();
			iReason.invalidityReasonToken = "FAIL";
			iReason.invalidityReasonText = e.getCause().getMessage();
			invalidityReasonList.add(iReason);
			vfd.invalidityReasonList = invalidityReasonList;
			response.validationFailureData = vfd;
			return response;
		}
		CertPath cp = result.getCertPath();
		/*
		 * Validate the certificate path, return result if invalid
		 */
		CertPathValidator cpv = null;
		try {
			cpv = CertPathValidator.getInstance("PKIX");
		} catch (NoSuchAlgorithmException e) {
			LOG.error("Internal Validation Error", e);
			throw new ServiceException("Internal Validation Error");
		}
		PKIXCertPathValidatorResult pvr = null;
		try {
			pvr = (PKIXCertPathValidatorResult) cpv.validate(cp, params);
		} catch (CertPathValidatorException e) {
			RestServiceEventLogger.logEvent(response, e);
			ValidationFailureData vfd = new ValidationFailureData();
			vfd.isAffirmativelyInvalid = true;
			List<InvalidityReason> invalidityReasonList = new ArrayList<InvalidityReason>();
			InvalidityReason iReason = new InvalidityReason();
			iReason.invalidityReasonToken = "FAIL";
			iReason.invalidityReasonText = e.getMessage();
			invalidityReasonList.add(iReason);
			vfd.invalidityReasonList = invalidityReasonList;
			response.validationFailureData = vfd;
			return response;
		} catch (InvalidAlgorithmParameterException e) {
			LOG.error("Internal Validation Error", e);
			throw new ServiceException("Internal Validation Error");
		}
		LOG.debug(pvr.getPolicyTree().toString());
		/**
		 * <pre>
		 * TODO: Check the certificate for revocation, return final result
		 * 
		 * - We can store CRLs in an CertStore implementation, or store observed serial numbers for revoked 
		 *   certificates in database tables
		 *   - https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/cert/CertStore.html
		 * </pre>
		 */
		/*
		 * If we got this far, the certificate is valid.
		 * 
		 * Populate the ValidationSuccessData, add it to the result, and; return the
		 * result.
		 * 
		 * TODO: For now, add the certpath even if the client didn't request it.
		 */
		response.validationResultToken = "SUCCESS";
		ValidationSuccessData vsd = new ValidationSuccessData();
		List<JsonX509Certificate> x509CertificateList = new ArrayList<JsonX509Certificate>();
		for (Certificate currentCert : cp.getCertificates()) {
			JsonX509Certificate bCert = new JsonX509Certificate();
			try {
				bCert.x509Certificate = Base64.encodeBase64String(currentCert.getEncoded());
				LOG.debug("Path Cert:\n" + currentCert.toString());
			} catch (CertificateEncodingException e) {
				LOG.error("Error Base64 encoding certificate from ReplyWantBack", e);
			}
			x509CertificateList.add(bCert);
		}
		JsonX509CertificateList bList = new JsonX509CertificateList();
		bList.x509CertificateList = x509CertificateList;
		WantBack wb = new WantBack();
		wb.certPath = bList;
		List<WantBack> wbList = new ArrayList<WantBack>();
		wbList.add(wb);
		vsd.wantBackResultList = wbList;
		response.validationSuccessData = vsd;
		return response;
	}
}
