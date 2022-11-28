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

@JsonComponent
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "isAffirmativelyInvalid", "invalidityReasonText" })
public class Fail extends ValidationResult {

	/**
	 * Static String value for "FAIL" result.
	 */
	@JsonProperty("result")
	public static final String result = "FAIL";

	/**
	 * Field isAffirmativelyInvalid.
	 */
	@JsonProperty("isAffirmativelyInvalid")
	public Boolean isAffirmativelyInvalid;

	/**
	 * Field invalidityReasonText.
	 */
	@JsonProperty("invalidityReasonText")
	public String invalidityReasonText;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<>();

	@Override
	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@Override
	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}
