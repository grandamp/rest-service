package org.keysupport.api.pojo.vss.v1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * This class is a Java representation of the JSON object V1ResultByCertificate's
 * Data.
 */
@JsonComponent
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "vssCertId", "x509SubjectName", "x509IssuerName", "x509SerialNumber", "x509SubjectAltName",
		"validationTime", "nextUpdate", "validationResultToken", "validationFailureData", "validationSuccessData" })
public class V1ResultByCertificateData {

	/**
	 * Field validationResultToken.
	 *
	 */
	@JsonProperty("validationResultToken")
	public String validationResultToken;

	/**
	 * Field validationFailureData.
	 *
	 */
	@JsonProperty("validationFailureData")
	public V1ValidationFailureData validationFailureData;

	/**
	 * Field validationSuccessData.
	 *
	 */
	@JsonProperty("validationSuccessData")
	public V1ValidationSuccessData validationSuccessData;

	/**
	 * Field X509IssuerName.
	 */
	@JsonProperty("x509IssuerName")
	public String x509IssuerName;

	/**
	 * Field vssCertId.
	 */
	@JsonProperty("vssCertId")
	public String vssCertId;

	/**
	 * Field X509SerialNumber.
	 */
	@JsonProperty("x509SerialNumber")
	public String x509SerialNumber;

	/*
	 * Field x509SubjectAltName
	 */
	@JsonProperty("x509SubjectAltName")
	public List<V1SANValue> x509SubjectAltName;

	/**
	 * Field X509SubjectName.
	 */
	@JsonProperty("x509SubjectName")
	public String x509SubjectName;

	/**
	 * Field validationTime.
	 *
	 * Base64 Encoded
	 */
	@JsonProperty("validationTime")
	public String validationTime;

	/**
	 * Field nextUpdate.
	 *
	 * Base64 Encoded
	 */
	@JsonProperty("nextUpdate")
	public String nextUpdate;

	/*
	 * additionalProperties getter and setter allows us to ignore fields unknown or
	 * undefined
	 */
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<>();

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}
