package org.keysupport.api.pojo.vss;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.constraints.NotBlank;

/*
 * Based on java.security.cert.PolicyNode
 * 
 * No support for parent or policyQualifiers
 */
@JsonComponent
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "depth", "critical", "expectedPolicies", "validPolicy", "children" })
public class PKIXPolicyNode {

	/**
	 * Field children.
	 */
	@JsonProperty("children")
	public Set<PKIXPolicyNode> children;

	/**
	 * Field depth
	 */
	@NotBlank
	@JsonProperty("depth")
	public int depth;

	/**
	 * Field expectedPolicies.
	 */
	@NotBlank
	@JsonProperty("expectedPolicies")
	public Set<String> expectedPolicies;

	/**
	 * Field validPolicy.
	 */
	@NotBlank
	@JsonProperty("validPolicy")
	public String validPolicy;

	/**
	 * Field critical.
	 */
	@NotBlank
	@JsonProperty("critical")
	public Boolean critical;
	
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