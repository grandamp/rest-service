package org.keysupport.api.pojo.oauth;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "acr_values_supported", "claims_supported", "grant_types_supported", "response_types_supported",
		"scopes_supported", "subject_types_supported", "authorization_endpoint", "issuer", "jwks_uri",
		"service_documentation", "token_endpoint", "userinfo_endpoint", "end_session_endpoint",
		"id_token_signing_alg_values_supported", "token_endpoint_auth_methods_supported",
		"token_endpoint_auth_signing_alg_values_supported" })
public class OpenIDAutoDiscovery {

	@JsonProperty("acr_values_supported")
	public List<String> acrValuesSupported = null;
	@JsonProperty("claims_supported")
	public List<String> claimsSupported = null;
	@JsonProperty("grant_types_supported")
	public List<String> grantTypesSupported = null;
	@JsonProperty("response_types_supported")
	public List<String> responseTypesSupported = null;
	@JsonProperty("scopes_supported")
	public List<String> scopesSupported = null;
	@JsonProperty("subject_types_supported")
	public List<String> subjectTypesSupported = null;
	@JsonProperty("authorization_endpoint")
	public String authorizationEndpoint;
	@JsonProperty("issuer")
	public String issuer;
	@JsonProperty("jwks_uri")
	public String jwksUri;
	@JsonProperty("service_documentation")
	public String serviceDocumentation;
	@JsonProperty("token_endpoint")
	public String tokenEndpoint;
	@JsonProperty("userinfo_endpoint")
	public String userinfoEndpoint;
	@JsonProperty("end_session_endpoint")
	public String endSessionEndpoint;
	@JsonProperty("id_token_signing_alg_values_supported")
	public List<String> idTokenSigningAlgValuesSupported = null;
	@JsonProperty("token_endpoint_auth_methods_supported")
	public List<String> tokenEndpointAuthMethodsSupported = null;
	@JsonProperty("token_endpoint_auth_signing_alg_values_supported")
	public List<String> tokenEndpointAuthSigningAlgValuesSupported = null;

}