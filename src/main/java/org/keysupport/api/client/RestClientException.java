package org.keysupport.api.client;

public class RestClientException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RestClientException(String message) {
                   super(message);
    }

    public RestClientException(String message, Exception e) {
                   super(message, e);
    }

}

