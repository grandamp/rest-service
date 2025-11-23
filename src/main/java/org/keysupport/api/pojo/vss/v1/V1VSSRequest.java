package org.keysupport.api.pojo.vss.v1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keysupport.api.pojo.vss.JsonX509Certificate;
import org.springframework.boot.jackson.JacksonComponent;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * This class is a Java representation of the JSON request.
 */
@JacksonComponent
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "validationPolicy", "wantBackList", "x509CertificateList" })
public class V1VSSRequest {

	/**
	 * Field validationPolicy.
	 */
	@JsonProperty("validationPolicy")
	public String validationPolicy;

	/**
	 * Field wantBackList.
	 */
	@JsonProperty("wantBackList")
	public List<V1WantBackTypeToken> wantBackList;

	/**
	 * Field x509CertificateList
	 */
	@JsonProperty("x509CertificateList")
	public List<JsonX509Certificate> x509CertificateList;

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
