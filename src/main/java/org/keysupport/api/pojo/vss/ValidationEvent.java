package org.keysupport.api.pojo.vss;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/*
 * Initial class to log validation failures and successes.
 *
 * It would be preferable to instrument as much data as we can.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "x5tS256", "eventType", "message" })
public class ValidationEvent {

	/**
	 * Field x5tS256.
	 */
	@JsonProperty("x5tS256")
	public String x5tS256;

	/**
	 * Field eventType.
	 */
	@JsonProperty("eventType")
	public String eventType;

	/**
	 * Field message.
	 */
	@JsonProperty("message")
	public String message;

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