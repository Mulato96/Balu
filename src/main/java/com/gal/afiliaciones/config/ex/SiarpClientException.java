package com.gal.afiliaciones.config.ex;

import org.springframework.http.HttpStatus;

public class SiarpClientException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String url;

    public SiarpClientException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = null;
        this.url = null;
    }

    public SiarpClientException(HttpStatus status, String url, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = status;
        this.url = url;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getUrl() {
        return url;
    }
}


