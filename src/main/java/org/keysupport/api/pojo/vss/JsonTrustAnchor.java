package org.keysupport.api.pojo.vss;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.constraints.NotBlank;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "x5t#S256", "trustAnchorSubject", "x509Certificate" })
public class JsonTrustAnchor {

	/**
	 * Field x5t#S256
	 */
	@NotBlank
	@JsonProperty("x5t#S256")
	public String x5tS256;

	/**
	 * Field trustAnchorSubject.
	 */
	@JsonProperty("trustAnchorSubject")
	public String trustAnchorSubject;

	/**
	 * Field x509Certificate.
	 */
	@NotBlank
	@JsonProperty("x509Certificate")
	public String x509Certificate;

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