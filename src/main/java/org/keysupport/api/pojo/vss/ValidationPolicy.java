package org.keysupport.api.pojo.vss;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "validationPolicyId", "trustAnchors", "userPolicySet", "inhibitPolicyMapping", "requireExplicitPolicy", "inhibitAnyPolicy" })
public class ValidationPolicy {

	/**
	 * Field validationPolicyId
	 */
	@JsonProperty("validationPolicyId")
	public String validationPolicyId;

	/**
	 * Field trustAnchors.
	 */
	@JsonProperty("trustAnchors")
	public List<JsonTrustAnchor> trustAnchors;

	/**
	 * Field userPolicySet.
	 */
	@JsonProperty("userPolicySet")
	public List<String> userPolicySet;

	/**
	 * Field inhibitPolicyMapping
	 */
	@JsonProperty("inhibitPolicyMapping")
	public boolean inhibitPolicyMapping;

	/**
	 * Field requireExplicitPolicy
	 */
	@JsonProperty("requireExplicitPolicy")
	public boolean requireExplicitPolicy;

	/**
	 * Field inhibitAnyPolicy
	 */
	@JsonProperty("inhibitAnyPolicy")
	public boolean inhibitAnyPolicy;

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