package org.keysupport.api.controller.oauth;

import java.util.ArrayList;
import java.util.List;

import org.keysupport.api.pojo.oauth.OpenIDAutoDiscovery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenIDAutoDiscoveryController {

	/*
	 * Example of our autodiscovery data based on production Login.gov autodiscovery data:
	 * 
	 * {
	 * 	"acr_values_supported": ["http://idmanagement.gov/ns/assurance/loa/1", "http://idmanagement.gov/ns/assurance/loa/3", "http://idmanagement.gov/ns/assurance/ial/1", "http://idmanagement.gov/ns/assurance/ial/2", "http://idmanagement.gov/ns/assurance/ial/0", "http://idmanagement.gov/ns/assurance/ial/2?strict=true", "urn:gov:gsa:ac:classes:sp:PasswordProtectedTransport:duo", "http://idmanagement.gov/ns/assurance/aal/2", "http://idmanagement.gov/ns/assurance/aal/3", "http://idmanagement.gov/ns/assurance/aal/3?hspd12=true", "http://idmanagement.gov/ns/assurance/aal/2?phishing_resistant=true", "http://idmanagement.gov/ns/assurance/aal/2?hspd12=true"],
	 * 	"claims_supported": ["iss", "sub", "email", "email_verified", "all_emails", "address", "phone", "phone_verified", "given_name", "family_name", "birthdate", "verified_at", "social_security_number", "x509_subject", "x509_presented", "x509_issuer"],
	 * 	"grant_types_supported": ["authorization_code"],
	 * 	"response_types_supported": ["code"],
	 * 	"scopes_supported": ["email", "all_emails", "openid", "profile:verified_at", "x509", "x509:subject", "x509:issuer", "x509:presented", "address", "phone", "profile", "profile:name", "profile:birthdate", "social_security_number"],
	 * 	"subject_types_supported": ["pairwise"],
	 * 	"authorization_endpoint": "https://idp.int.identitysandbox.gov/openid_connect/authorize",
	 * 	"issuer": "https://idp.int.identitysandbox.gov/",
	 * 	"jwks_uri": "https://idp.int.identitysandbox.gov/api/openid_connect/certs",
	 * 	"service_documentation": "https://developers.login.gov/",
	 * 	"token_endpoint": "https://idp.int.identitysandbox.gov/api/openid_connect/token",
	 * 	"userinfo_endpoint": "https://idp.int.identitysandbox.gov/api/openid_connect/userinfo",
	 * 	"end_session_endpoint": "https://idp.int.identitysandbox.gov/openid_connect/logout",
	 * 	"id_token_signing_alg_values_supported": ["RS256"],
	 * 	"token_endpoint_auth_methods_supported": ["private_key_jwt"],
	 * 	"token_endpoint_auth_signing_alg_values_supported": ["RS256"]
	 * }
	 * 
	 * This class & method is merely an example.  We probably want to see the request for context
	 */
	@GetMapping("/.well-known/openid-configuration")
	public OpenIDAutoDiscovery config() {
		OpenIDAutoDiscovery config = new OpenIDAutoDiscovery();
		/*
		 * Enumerate ACRs to indicate potential flows
		 * 
		 * Notes:
		 * 
		 *   - AAL2 & AAL3 human use-cases should probably require content security headers for "User-Agent"'s used by humans
		 *   - IAL NPE use-cases should probably use RFC 8705 for all interactions
		 *   
		 *   - For actual OAuth functionality, Spring provides the following example:
		 *     - https://docs.spring.io/spring-authorization-server/docs/current/reference/html/getting-started.html
		 */
		List<String> supportedACRs = new ArrayList<String>();
		supportedACRs.add("http://idmanagement.gov/ns/assurance/aal/2");
		supportedACRs.add("http://idmanagement.gov/ns/assurance/aal/3");
		supportedACRs.add("http://idmanagement.gov/ns/assurance/aal/2/fal/1");
		supportedACRs.add("http://idmanagement.gov/ns/assurance/aal/2/fal/2");
		supportedACRs.add("http://idmanagement.gov/ns/assurance/aal/2/fal/3");
		supportedACRs.add("http://idmanagement.gov/ns/assurance/aal/3/fal/1");
		supportedACRs.add("http://idmanagement.gov/ns/assurance/aal/3/fal/2");
		supportedACRs.add("http://idmanagement.gov/ns/assurance/aal/3/fal/3");
		supportedACRs.add("http://idmanagement.gov/ns/assurance/ial/npe/aal/2");
		supportedACRs.add("http://idmanagement.gov/ns/assurance/ial/npe/aal/3");
		supportedACRs.add("http://idmanagement.gov/ns/assurance/ial/npe/aal/2/fal/1");
		supportedACRs.add("http://idmanagement.gov/ns/assurance/ial/npe/aal/2/fal/2");
		supportedACRs.add("http://idmanagement.gov/ns/assurance/ial/npe/aal/2/fal/3");
		supportedACRs.add("http://idmanagement.gov/ns/assurance/ial/npe/aal/3/fal/1");
		supportedACRs.add("http://idmanagement.gov/ns/assurance/ial/npe/aal/3/fal/2");
		supportedACRs.add("http://idmanagement.gov/ns/assurance/ial/npe/aal/3/fal/3");
		config.acrValuesSupported = supportedACRs;
		/*
		 * Controlled via AWS API Gateway Mapping
		 */
		config.jwksUri = "http://api-keysupport-rest-dev.us-east-1.elasticbeanstalk.com/openid_connect/certs";
		config.authorizationEndpoint = "http://api-keysupport-rest-dev.us-east-1.elasticbeanstalk.com/openid_connect/authorize";
		config.serviceDocumentation = "http://api-keysupport-rest-dev.us-east-1.elasticbeanstalk.com/developers";
		
		return config;
	}

}
