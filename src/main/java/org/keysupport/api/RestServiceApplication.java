package org.keysupport.api;

import org.keysupport.api.singletons.IntermediateCacheSingleton;
import org.keysupport.api.singletons.ValidationPoliciesSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestServiceApplication {

	private final static Logger LOG = LoggerFactory.getLogger(RestServiceApplication.class);

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

        /*
         * Quick hack to get env and properties.  line.seperator with trash the JSON
         */
//      StringBuffer sb = new StringBuffer();
//    	Map<String, String> env = System.getenv();
//    	sb.append("{\"systemEnvironment\":{");
//    	env.forEach((k, v) -> sb.append("\"" + k  + "\"" + ":" + "\"" + v + "\","));
//    	sb.append("\"currentNanoTime\":" + System.nanoTime());
//    	sb.append("},");
//    	sb.append("\"systemProperties\":{");
//    	System.getProperties().forEach((k, v) -> sb.append("\"" + k  + "\"" + ":" + "\"" + v + "\","));
//    	sb.append("\"currentNanoTime\":" + System.nanoTime());
//    	sb.append("}}");
//    	LOG.info(sb.toString());

        /*
         * Set our policies singleton
         */
        @SuppressWarnings("unused")
		ValidationPoliciesSingleton validationPoliciesSingleton = ValidationPoliciesSingleton.getInstance();

        /*
         * Set our prototype cached Intermediate CertStore singleton
         */
        @SuppressWarnings("unused")
		IntermediateCacheSingleton intermediateCacheSingleton = IntermediateCacheSingleton.getInstance();

    }

}
