package org.keysupport.api;

import org.keysupport.api.jetty.JettyCustomizer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestServiceApplication {

	public static void main(String[] args) {
		JettyCustomizer.configureGlobalTlsProperties();
		SpringApplication app = new SpringApplication(RestServiceApplication.class);
		app.run(args);
	}

}
