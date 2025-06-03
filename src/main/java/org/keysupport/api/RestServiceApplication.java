package org.keysupport.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class RestServiceApplication {

    public static void main(String[] args) {
    	LoggingUtil.logSystemOutAndErr();
        SpringApplication app = new SpringApplication(RestServiceApplication.class);
        app.run(args);
   }

}
