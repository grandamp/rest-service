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
@JsonPropertyOrder({ "validationPolicyId", "x509Certificate", "requestId" })
public class VssRequest {

	/**
	 * Field validationPolicyId.
	 */
	@NotBlank
	@JsonProperty("validationPolicyId")
	public String validationPolicyId;

	/**
	 * Field x509Certificate
	 */
	@NotBlank
	@JsonProperty("x509Certificate")
	public String x509Certificate;

	/**
	 * Field requestId.
	 */
	@JsonProperty("requestId")
	public String requestId;

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