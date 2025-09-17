package com.gal.afiliaciones.infrastructure.security;

public class ExternalServiceException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;

    public ExternalServiceException(int statusCode, String responseBody) {
        super("External service error: status=" + statusCode + ", body=" + responseBody);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
