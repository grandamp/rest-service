package org.keysupport.api.pojo.vss;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;

import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonComponent
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "validationPolicy", "wantBackList", "x509Certificate" })
public class VssRequest {

	/**
	 * Field validationPolicy.
	 */
	@NotBlank
	@JsonProperty("validationPolicy")
	public String validationPolicy;

	/**
	 * Field wantBackList.
	 */
	@JsonProperty("wantBackList")
	public List<WantBackTypeToken> wantBackList;

	/**
	 * Field x509Certificate
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