package com.gal.afiliaciones.config.ex.workerdisplacement;

public class DisplacementConflictException extends RuntimeException {
    public DisplacementConflictException(String message) {
        super(message);
    }
    public DisplacementConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}


