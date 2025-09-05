package com.gal.afiliaciones.config.ex.generalnovelty;

public class PaymentsContributorsNotFoundException extends RuntimeException {
    
    public PaymentsContributorsNotFoundException(String message) {
        super(message);
    }
    
    public PaymentsContributorsNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 