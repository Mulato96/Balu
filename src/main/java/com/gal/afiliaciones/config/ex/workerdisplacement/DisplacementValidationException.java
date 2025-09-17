package com.gal.afiliaciones.config.ex.workerdisplacement;

public class DisplacementValidationException extends RuntimeException {
    public DisplacementValidationException(String message) {
        super(message);
    }
    public DisplacementValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}


