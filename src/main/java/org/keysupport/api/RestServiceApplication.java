package org.keysupport.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class RestServiceApplication {

	private final static Logger LOG = LoggerFactory.getLogger(RestServiceApplication.class);

	/*
	 * TODO:  Define Spring Boot Properties:  https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html
	 */
    public static void main(String[] args) {
    	/*
    	 *  Set the default AWS Java2 Toolkit asynchronous HTTP client to AwsCrtAsyncHttpClient
    	 */
    	System.setProperty("software.amazon.awssdk.http.async.service.impl",
    	"software.amazon.awssdk.http.crt.AwsCrtSdkHttpService");
        SpringApplication app = new SpringApplication(RestServiceApplication.class);
        LOG.info("Service Starting");
        app.run(args);
   }

}
