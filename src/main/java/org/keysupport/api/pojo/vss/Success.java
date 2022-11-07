package org.keysupport.api.pojo.vss;

import java.util.HashMap;
import java.util.List;
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
@JsonPropertyOrder({ "x509CertificatePath" })
public class Success extends ValidationResult {

	/**
	 * Static String value for "SUCCESS" result.
	 */
	@JsonProperty("result")
	public static final String result = "SUCCESS";
	
	/**
	 * Field x509CertificatePath
	 */
	@JsonProperty("x509CertificatePath")
	public List<JsonX509Certificate> x509CertificatePath;

	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}