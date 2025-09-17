package com.gal.afiliaciones.config.ex.workerdisplacement;

public class DisplacementNotFoundException extends RuntimeException {
    public DisplacementNotFoundException(String message) {
        super(message);
    }

    public DisplacementNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}


