package org.keysupport.api;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LoggingUtil {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingUtil.class);
    
    public static String pojoToJson(Object obj) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			LOG.error("Unable to convert object to JSON", e);
			return null;
		}

    }
    
    public static String stackTraceToString(Throwable t) {
    	return Stream.of(t.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
    }

}