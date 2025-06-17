package org.keysupport.api.controller.vss;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.keysupport.api.LoggingUtil;
import org.keysupport.api.controller.ServiceException;
import org.keysupport.api.pkix.X509Util;
import org.keysupport.api.pojo.vss.Fail;
import org.keysupport.api.pojo.vss.JsonX509Certificate;
import org.keysupport.api.pojo.vss.Success;
import org.keysupport.api.pojo.vss.ValidationResult;
import org.keysupport.api.pojo.vss.VssRequest;
import org.keysupport.api.pojo.vss.VssResponse;
import org.keysupport.api.pojo.vss.v1.V1InvalidityReason;
import org.keysupport.api.pojo.vss.v1.V1ResultByCertificate;
import org.keysupport.api.pojo.vss.v1.V1ResultByCertificateData;
import org.keysupport.api.pojo.vss.v1.V1TransactionResult;
import org.keysupport.api.pojo.vss.v1.V1VSSRequest;
import org.keysupport.api.pojo.vss.v1.V1VSSResponse;
import org.keysupport.api.pojo.vss.v1.V1ValidationFailureData;
import org.keysupport.api.pojo.vss.v1.V1ValidationResult;
import org.keysupport.api.pojo.vss.v1.V1ValidationSuccessData;
import org.keysupport.api.pojo.vss.v1.V1WantBack;
import org.keysupport.api.pojo.vss.v1.V1WantBackTypeToken;
import org.keysupport.api.pojo.vss.v1.V1X509CertificateList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "rest", description = "Validate a certificate based on the specified validation policy (v1 syntax)")
public class V1RestController {

	private final static Logger V1LOG = LoggerFactory.getLogger(V1RestController.class);

	@PostMapping(path = "/vss/rest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Certificate Validation Request", required = true, content = @Content(schema = @Schema(implementation = VssRequest.class), mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
			@ExampleObject(name = "A validation request using a valid end-entity certificate", value = "{\n"
					+ "    \"validationPolicy\": \"2.16.840.1.101.10.2.18.2.1.4\",\n"
					+ "    \"wantBackList\": [\n"
					+ "        {\n"
					+ "            \"wantBackTypeToken\": \"certPath\"\n"
					+ "        }\n"
					+ "    ],\n"
					+ "    \"x509CertificateList\": [\n"
					+ "        {\n"
					+ "            \"x509Certificate\": \"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\"\n"
					+ "        }\n"
					+ "    ]\n"
					+ "}"),
			@ExampleObject(name = "A validation request using another random FPKI certificate", value = "{\n"
					+ "    \"validationPolicy\": \"2.16.840.1.101.10.2.18.2.1.4\",\n"
					+ "    \"wantBackList\": [\n"
					+ "        {\n"
					+ "            \"wantBackTypeToken\": \"certPath\"\n"
					+ "        }\n"
					+ "    ],\n"
					+ "    \"x509CertificateList\": [\n"
					+ "        {\n"
					+ "            \"x509Certificate\": \"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\"\n"
					+ "        }\n"
					+ "    ]\n"
					+ "}"),
			@ExampleObject(name = "A batch validation request both certificates", value = "{\n"
					+ "    \"validationPolicy\": \"2.16.840.1.101.10.2.18.2.1.4\",\n"
					+ "    \"wantBackList\": [\n"
					+ "        {\n"
					+ "            \"wantBackTypeToken\": \"certPath\"\n"
					+ "        }\n"
					+ "    ],\n"
					+ "    \"x509CertificateList\": [\n"
					+ "        {\n"
					+ "            \"x509Certificate\": \"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\"\n"
					+ "        },\n"
					+ "        {\n"
					+ "            \"x509Certificate\": \"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\"\n"
					+ "        }\n"
					+ "    ]\n"
					+ "}")}))
	@CrossOrigin(origins = "*")
	ResponseEntity<V1VSSResponse> validatev1(@RequestBody V1VSSRequest request, @RequestHeader Map<String, String> headers) {
		ValidateController vc = new ValidateController();
		/*
		 * Log the request
		 */
		V1LOG.info(LoggingUtil.pojoToJson(request));
		/*
		 * Process v1 request
		 */
		V1VSSResponse response = new V1VSSResponse();
		V1TransactionResult txResult = new V1TransactionResult();
		/*
		 * Ensure we have validationPolicy, wantBackList, and x509CertificateList
		 */
		if (null == request || null == request.validationPolicy || null == request.x509CertificateList
				|| null == request.wantBackList) {
			txResult.transactionResultToken = "SERVICEFAIL";
			txResult.transactionResultText = "Request must include validationPolicy, wantBackList, and x509CertificateList";
			response.transactionResult = txResult;
			V1LOG.warn(LoggingUtil.pojoToJson(Map.of("error", "Request is not a valid v1 request, returning SERVICEFAIL")));
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		boolean certPathWantBack = false;
		List<V1WantBackTypeToken> wantBackList = request.wantBackList;
		for (V1WantBackTypeToken wantBack: wantBackList) {
			if (!wantBack.wantBackTypeToken.equalsIgnoreCase("certPath")) {
				txResult.transactionResultToken = "SERVICEFAIL";
				txResult.transactionResultText = "Unsupported WantBack";
				response.transactionResult = txResult;
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else {
				certPathWantBack = true;
			}
		}
		/*
		 * Process an array of requests, and recombine into a v1 response
		 */
		List<JsonX509Certificate> batchCerts = request.x509CertificateList;
		V1ValidationResult validationResult = new V1ValidationResult();
		List<V1ResultByCertificate> resultsByCertificateList = new ArrayList<>();
		for (JsonX509Certificate toValidate: batchCerts) {
			/*
			 * Validate the certificate in the V1 request using the V2 endpoint and map the results.
			 */
			VssRequest v2Request = new VssRequest();
			v2Request.validationPolicyId = request.validationPolicy;
			v2Request.x509Certificate = toValidate.x509Certificate;
			VssResponse v2Response = null;
			try {
				v2Response = vc.validate(v2Request, headers).getBody();
			} catch(ServiceException e) {
				txResult.transactionResultToken = "SERVICEFAIL";
				txResult.transactionResultText = e.getMessage();
				response.transactionResult = txResult;
				V1LOG.warn(LoggingUtil.pojoToJson(Map.of("error", "Exception thrown via V2 endpoint, returning SERVICEFAIL", "stacktrace", LoggingUtil.stackTraceToString(e))));
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			txResult.transactionResultToken = "SUCCESS";
			txResult.transactionResultText = "Validation Operation Completed Successfully";
			response.transactionResult = txResult;
			V1ResultByCertificate result = new V1ResultByCertificate();
			V1ResultByCertificateData resultData = new V1ResultByCertificateData();
			/*
			 * Convert "x5t#S256" that is URL encoded base64 to "vssCertId" which is Hex
			 */
			byte[] s256Digest = Base64.getUrlDecoder().decode(v2Response.x5tS256);
			resultData.vssCertId = X509Util.byteArrayToHexString(s256Digest);
			resultData.x509SubjectName = v2Response.x509SubjectName;
			resultData.x509IssuerName = v2Response.x509IssuerName;
			resultData.x509SerialNumber = v2Response.x509SerialNumber;
			resultData.x509SubjectAltName = v2Response.x509SubjectAltName;
			resultData.validationTime = v2Response.validationTime;
			resultData.nextUpdate = v2Response.nextUpdate;
			ValidationResult res = v2Response.validationResult;
			if (res instanceof Success) {
				resultData.validationResultToken = ValidationResult.SUCCESS_VALUE;
				Success success = (Success)res;
				V1ValidationSuccessData successData = new V1ValidationSuccessData();
				List<V1WantBack> wantBackResultList = new ArrayList<>();
				/*
				 * Check wantBack, and add CertPath if requested
				 */
				if (certPathWantBack) {
					List<JsonX509Certificate> certPath = success.x509CertificatePath;
					V1X509CertificateList certPathWantBackList = new V1X509CertificateList();
					V1WantBack certPathWb = new V1WantBack();
					certPathWantBackList.x509CertificateList = certPath;
					certPathWb.certPath = certPathWantBackList;
					wantBackResultList.add(certPathWb);
				}
				/*
				 * Add the wantback
				 */
				successData.wantBackResultList = wantBackResultList;
				resultData.validationSuccessData = successData;
			} else {
				resultData.validationResultToken = ValidationResult.FAIL_VALUE;
				Fail fail = (Fail)res;
				V1ValidationFailureData failDetail = new V1ValidationFailureData();
				failDetail.isAffirmativelyInvalid = fail.isAffirmativelyInvalid;
				V1InvalidityReason reason = new V1InvalidityReason();
				reason.invalidityReasonToken = ValidationResult.FAIL_VALUE;
				reason.invalidityReasonText = fail.invalidityReasonText;
				List<V1InvalidityReason> reasonList = new ArrayList<>();
				reasonList.add(reason);
				failDetail.invalidityReasonList = reasonList;
				resultData.validationFailureData = failDetail;
			}
			result.resultByCertificate = resultData;
			resultsByCertificateList.add(result);
		}
		validationResult.resultsByCertificateList = resultsByCertificateList;
		response.validationResult = validationResult;
		/*
		 * Log the response
		 */
		V1LOG.info(LoggingUtil.pojoToJson(response));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}


}
