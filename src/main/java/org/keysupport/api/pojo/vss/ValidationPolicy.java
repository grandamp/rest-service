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

import jakarta.validation.constraints.NotBlank;

@JsonComponent
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "validationPolicyId", "validationPolicyName", "validationPolicyDescription", "trustAnchors", "userPolicySet", "inhibitPolicyMapping", "requireExplicitPolicy", "inhibitAnyPolicy" })
public class ValidationPolicy {

	/**
	 * Field validationPolicyId
	 *
	 * TODO: Migrate to UUID
	 */
	@NotBlank
	@JsonProperty("validationPolicyId")
	public String validationPolicyId;

	/**
	 * Field validationPolicyName
	 *
	 * TODO: Integrate and document `validationPolicyName`
	 */
	@JsonProperty("validationPolicyName")
	public String validationPolicyName;

	/**
	 * Field validationPolicyDescription
	 *
	 * TODO: Integrate and document `validationPolicyDescription`
	 */
	@JsonProperty("validationPolicyDescription")
	public String validationPolicyDescription;

	/**
	 * Field trustAnchors.
	 */
	@NotBlank
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
	@NotBlank
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