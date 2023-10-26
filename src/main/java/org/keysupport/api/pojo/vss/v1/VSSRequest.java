package org.keysupport.api.pojo.vss.v1;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class is a Java representation of the JSON request.
 */
@JsonComponent
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "validationPolicy", "wantBackList", "x509CertificateList" })
public class VSSRequest {

	/**
	 * Static creator for de-serialization
	 *
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@JsonCreator
	public static VSSRequest getInstance(String jsonString)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		VSSRequest req = null;
		req = mapper.readValue(jsonString, VSSRequest.class);
		return req;
	}

	/**
	 * Default Constructor
	 *
	 * @param validationPolicy
	 * @param wantBackList
	 * @param x509CertificateList
	 */
	@JsonCreator
	public VSSRequest(@JsonProperty("validationPolicy") String validationPolicy,
			@JsonProperty("wantBackList") List<WantBackTypeToken> wantBackList,
			@JsonProperty("x509CertificateList") List<X509Certificate> x509CertificateList) {
		this.validationPolicy = validationPolicy;
		this.wantBackList = wantBackList;
		this.x509CertificateList = x509CertificateList;
	}

	/**
	 * Field validationPolicy.
	 */
	@JsonProperty("validationPolicy")
	public String validationPolicy;

	/**
	 * Field wantBackList.
	 */
	@JsonProperty("wantBackList")
	public List<WantBackTypeToken> wantBackList;

	/**
	 * Field x509CertificateList
	 */
	@JsonProperty("x509CertificateList")
	public List<X509Certificate> x509CertificateList;

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
