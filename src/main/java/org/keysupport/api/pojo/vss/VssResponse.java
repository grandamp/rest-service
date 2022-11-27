package org.keysupport.api.pojo.vss;

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

import jakarta.validation.constraints.NotBlank;

@JsonComponent
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "x5t#S256", "x509SubjectName", "x509IssuerName", "x509SerialNumber", "x509SubjectAltName",
		"validationTime", "nextUpdate", "validationResultToken", "validationFailureData", "validationSuccessData" })
public class VssResponse {

	/**
	 * Field x5t#S256.
	 */
	@NotBlank
	@JsonProperty("x5t#S256")
	public String x5tS256;

	/**
	 * Field X509SubjectName.
	 */
	@JsonProperty("x509SubjectName")
	public String x509SubjectName;

	/**
	 * Field X509IssuerName.
	 */
	@JsonProperty("x509IssuerName")
	public String x509IssuerName;

	/**
	 * Field X509SerialNumber.
	 */
	@NotBlank
	@JsonProperty("x509SerialNumber")
	public String x509SerialNumber;

	/**
	 * Field x509SubjectAltName
	 */
	@JsonProperty("x509SubjectAltName")
	public List<SANValue> x509SubjectAltName;

	/**
	 * Field validationTime.
	 */
	@NotBlank
	@JsonProperty("validationTime")
	public String validationTime;

	/**
	 * Field nextUpdate.
	 */
	@NotBlank
	@JsonProperty("nextUpdate")
	public String nextUpdate;

	/**
	 * Field validationResult.
	 */
	@NotBlank
	@JsonProperty("validationResult")
	public ValidationResult validationResult;

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