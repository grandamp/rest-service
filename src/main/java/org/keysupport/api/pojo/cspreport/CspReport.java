package org.keysupport.api.pojo.cspreport;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "blocked-uri", "document-uri", "original-policy", "referrer", "violated-directive" })
public class CspReport {

	@JsonProperty("blocked-uri")
	public String blockedUri;
	@JsonProperty("document-uri")
	public String documentUri;
	@JsonProperty("original-policy")
	public String originalPolicy;
	@JsonProperty("referrer")
	public String referrer;
	@JsonProperty("violated-directive")
	public String violatedDirective;
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