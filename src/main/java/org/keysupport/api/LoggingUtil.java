package org.keysupport.api;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public class LoggingUtil {

	private static final Logger LOG = LoggerFactory.getLogger(LoggingUtil.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private LoggingUtil() {
	}

	public static String pojoToJson(Object obj) {
		try {
			return MAPPER.writeValueAsString(obj);
		} catch (JacksonException e) {
			LOG.error("Unable to convert object to JSON", e);
			return null;
		}
	}

	public static String stackTraceToString(Throwable t) {
		if (null == t) {
			return "";
		}
		return Stream.of(t.getStackTrace())
				.map(StackTraceElement::toString)
				.collect(Collectors.joining("\n"));
	}

}
