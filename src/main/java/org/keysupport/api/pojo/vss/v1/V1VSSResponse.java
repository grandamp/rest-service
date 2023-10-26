package org.keysupport.api.pojo.vss.v1;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * This class is a Java representation of the JSON response.
 *
 * @author tejohnson
 *
 * @version $Revision: 1.0 $
 */
@JsonComponent
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "transactionResult", "validationResult" })
public class V1VSSResponse {

	/**
	 * Field transactionResult.
	 */
	@JsonProperty("transactionResult")
	public V1TransactionResult transactionResult;

	/**
	 * Field validationResult.
	 */
	@JsonProperty("validationResult")
	public V1ValidationResult validationResult;

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
