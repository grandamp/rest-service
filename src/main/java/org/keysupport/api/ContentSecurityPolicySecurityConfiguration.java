package org.keysupport.api;

import java.util.ArrayList;
import java.util.List;

import org.keysupport.api.pojo.cspreport.CspViolationReportTo;
import org.keysupport.api.pojo.cspreport.Endpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ContentSecurityPolicySecurityConfiguration {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    	/*
    	 * Construct ReportTo POJO
    	 */
    	CspViolationReportTo reportTo = new CspViolationReportTo();
    	reportTo.group = "csp-violation-report";
    	long maxAge = 2592000;
    	reportTo.maxAge = maxAge;
    	Endpoint self = new Endpoint();
    	/*
    	 * TODO:  Add the service URL to application properties
    	 */
    	self.url = "http://api.keysupport.org/report";
    	List<Endpoint> endpoints = new ArrayList<Endpoint>();
    	endpoints.add(self);
    	reportTo.endpoints = endpoints;
    	ObjectMapper mapper = new ObjectMapper();
    	String ReportTo = mapper.writeValueAsString(reportTo);
    	/*
    	 * Set Headers
    	 */
    	http
    	  .csrf().disable()
    	  .authorizeRequests()
    	  .anyRequest()
    	  .permitAll()
    	  .and()
    	  .headers().addHeaderWriter(new StaticHeadersWriter("Report-To", ReportTo))
    	  .xssProtection()
    	  .and()
    	  .contentSecurityPolicy("form-action 'self'; report-uri /report; report-to csp-violation-report");
        return http.build();
    }
}