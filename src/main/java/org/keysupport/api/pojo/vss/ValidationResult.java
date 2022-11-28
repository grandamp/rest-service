package org.keysupport.api.pojo.vss;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@JsonComponent
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "result")
@JsonSubTypes({ @JsonSubTypes.Type(value = Fail.class, name = "FAIL"),
		@JsonSubTypes.Type(value = Success.class, name = "SUCCESS") })
@ApiResponse(responseCode = "200", content = @Content(schema = @Schema(oneOf = { Fail.class, Success.class })))
public class ValidationResult {

	/**
	 * Static String value for "FAIL" result.
	 */
	public static final String FAIL_VALUE = "FAIL";

	/**
	 * Static String value for "SUCCESS" result.
	 */
	public static final String SUCCESS_VALUE = "SUCCESS";

	/**
	 * Field result.
	 */
	@JsonProperty("result")
	public String result;

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