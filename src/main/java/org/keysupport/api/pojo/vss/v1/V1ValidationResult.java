package org.keysupport.api.pojo.vss.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * This class is a Java representation of the JSON Object validationResult.
 */
@JsonComponent
@JsonInclude(JsonInclude.Include.NON_NULL)
public class V1ValidationResult {

	public V1ValidationResult() {
		resultsByCertificateList = new ArrayList<>();
	}

	/**
	 * Field resultsByCertificateList.
	 */
	@com.fasterxml.jackson.annotation.JsonProperty("resultsByCertificateList")
	public List<V1ResultByCertificate> resultsByCertificateList;

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
