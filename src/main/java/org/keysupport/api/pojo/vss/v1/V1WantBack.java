package org.keysupport.api.pojo.vss.v1;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This abstract class is a Java representation of the JSON Object representing
 * a service wantBack.
 */
@JsonComponent
@JsonInclude(JsonInclude.Include.NON_NULL)
public class V1WantBack {

	/**
	 * Field certPath.
	 *
	 * Base64 Encoded
	 */
	@JsonProperty("certPath")
	public V1X509CertificateList certPath;

	/**
	 * Field revocationInfo.
	 *
	 * Base64 Encoded
	 */
	@JsonProperty("revocationInfo")
	public V1OCSPResponseList revocationInfo;

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
