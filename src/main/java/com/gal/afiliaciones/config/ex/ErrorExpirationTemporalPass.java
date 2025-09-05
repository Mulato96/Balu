package com.gal.afiliaciones.config.ex;

import org.springframework.http.HttpStatus;

public class ErrorExpirationTemporalPass extends AffiliationsExceptionBase {
    public ErrorExpirationTemporalPass(String message) {
        super(Error.builder()
                .message(message)
                .type(Error.Type.TEMPORAL_PASSWORD_EXPIRED)
                .build(), HttpStatus.CONFLICT);
    }
}
