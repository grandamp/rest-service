package org.keysupport.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/*
 * SpringDoc Docs:  https://springdoc.org/
 */
@Configuration
public class ConfigurationSwagger {    
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Certificate Validation Service API")
                .description("Spring Boot reference implementation of the API")
                .version("v0.0.1")
                .license(new License()
                		.name("CC0 1.0 Universal")
                		.url("https://creativecommons.org/publicdomain/zero/1.0/")
                		)
                );
    }
}