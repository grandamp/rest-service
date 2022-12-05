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

	@Bean
	public OpenAPI springShopOpenAPI() {

		return new OpenAPI().addServersItem(new Server().url(BASE_URI))
				.info(new Info().title("Certificate Validation Service API")
						.description("Current main branch of Spring Boot reference implementation for the Certificate Validation Service API")
						.contact(new Contact().name("Todd E. Johnson").email("grandamp@gmail.com")
								.url("https://app.ens.domains/name/cryptomeanscryptography.eth/details"))
						.license(new License().name("CC0 1.0 Universal")
								.url("https://creativecommons.org/publicdomain/zero/1.0/"))
						.version("https://github.com/grandamp/rest-service/"));
	}
}