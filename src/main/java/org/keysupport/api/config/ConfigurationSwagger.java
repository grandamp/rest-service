package org.keysupport.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/*
 * SpringFox Docs:  https://springfox.github.io/springfox/docs/current/
 *
 * TODO:  See if we should use SpringDoc instead:  https://springdoc.org/migrating-from-springfox.html
 */
@Configuration
public class ConfigurationSwagger {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
          .select()
          .apis(RequestHandlerSelectors.any())
          .paths(PathSelectors.any())
          .build();
    }
}