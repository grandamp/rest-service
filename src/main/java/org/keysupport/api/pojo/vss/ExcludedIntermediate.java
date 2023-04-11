package org.keysupport.api.pojo.vss;

import java.util.HashMap;
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
@JsonPropertyOrder({ "intermediateId", "x509SubjectName", "x509IssuerName", "excludeReason" })
public class ExcludedIntermediate {

	/**
	 * Field x5t#S256
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
	 * Field excludeReason
	 */
	@NotBlank
	@JsonProperty("excludeReason")
	public String excludeReason;

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