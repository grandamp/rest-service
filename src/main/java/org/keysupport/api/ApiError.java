package org.keysupport.api;

import org.springframework.http.HttpStatus;

public class ApiError {

	private HttpStatus status;
	private String message;

	public ApiError(HttpStatus status, String message) {
		super();
		this.status = status;
		this.message = message;
	}

	public HttpStatus getStatus() {
		return this.status;
	}

	public String getMessage() {
		return this.message;
	}

}