package com.gal.afiliaciones.infrastructure.service.retirement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRetirementDateException extends RuntimeException {
    public InvalidRetirementDateException(String message) {
        super(message);
    }
}