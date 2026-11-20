package org.keysupport.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

/*
 * SpringDoc Docs:  https://springdoc.org/
 */
@Configuration
public class ConfigurationSwagger {

	private final String BASE_URI = System.getenv("BASE_URI");

	public static String resolveVersion() {
		return resolveVersion(System.getenv("PIPELINE_ID"));
	}

	public static String resolveVersion(String pipelineId) {
		if (pipelineId != null && !pipelineId.isBlank() && !"unknown".equalsIgnoreCase(pipelineId)) {
			String shortSha = pipelineId.length() > 7 ? pipelineId.substring(0, 7) : pipelineId;
			return "ghcr.io/grandamp/rest-service:sha-" + shortSha;
		}
		return "https://github.com/grandamp/rest-service/";
	}

	@Bean
	public OpenAPI springShopOpenAPI() {
		String apiVersion = resolveVersion();

		OpenAPI openApi = new OpenAPI()
				.info(new Info().title("Certificate Validation Service API")
						.description("Current main branch of Spring Boot reference implementation for the Certificate Validation Service API")
						.contact(new Contact().name("Todd E. Johnson").email("todd@keysupport.net")
								.url("https://keysupport.net/contact/"))
						.license(new License().name("CC0 1.0 Universal")
								.url("https://creativecommons.org/publicdomain/zero/1.0/"))
						.version(apiVersion));
		if (BASE_URI != null && !BASE_URI.isBlank()) {
			openApi.addServersItem(new Server().url(BASE_URI));
		}
		return openApi;
	}
}