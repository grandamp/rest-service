package org.keysupport.api;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestServiceApplication {

	/*
	 * TODO:  Define Spring Boot Properties:  https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html
	 * 
	 *   - AWS Elasic Beanstalk expects the server is running on port 5000 -vs- 8080
	 *     - server.port=5000
	 *   
	 *   - For testing, our base URL may not be known until the associated environment is establshed
	 *     - Base URL Hard Coded in a few classes as:  http://api-keysupport-rest-dev.us-east-1.elasticbeanstalk.com/
	 */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(RestServiceApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);       
    }

}
