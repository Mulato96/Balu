package com.gal.afiliaciones.config.ex.retirement;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRetirementRequestException extends RuntimeException {
    public InvalidRetirementRequestException(String message) {
        super(message);
    }
}