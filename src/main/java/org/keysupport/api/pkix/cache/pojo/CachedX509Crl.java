package org.keysupport.api.pkix.cache.pojo;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "uri", "thisUpdate", "nextUpdate", "httpLastModifiedDate", "eTag", "authorityKeyIdentifier", "issuerName", "numEntries" })
public class CachedX509Crl {

	/**
	 * Field uri
	 */
	@JsonProperty("uri")
	public URI uri;

	/**
	 * Field thisUpdate
	 */
	@JsonProperty("thisUpdate")
	public String thisUpdate;

	/**
	 * Field nextUpdate
	 */
	@JsonProperty("nextUpdate")
	public String nextUpdate;

	/**
	 * Field httpLastModifiedDate
	 */
	@JsonProperty("httpLastModifiedDate")
	public String httpLastModifiedDate;

	/**
	 * Field eTag
	 */
	@JsonProperty("eTag")
	public String eTag;

	/**
	 * Field authorityKeyIdentifier
	 */
	@JsonProperty("authorityKeyIdentifier")
	public byte[] authorityKeyIdentifier;

	/**
	 * Field issuerName
	 */
	@JsonProperty("issuerName")
	public byte[] issuerName;

	/**
	 * Field numEntries
	 */
	@JsonProperty("numEntries")
	public int numEntries;
	
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