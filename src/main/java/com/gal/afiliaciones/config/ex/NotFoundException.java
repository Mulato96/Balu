package com.gal.afiliaciones.config.ex;

import org.springframework.http.HttpStatus;

public class NotFoundException extends AffiliationsExceptionBase {
    public NotFoundException(String message) {
        super(Error.builder()
                .type(Error.Type.REGISTER_NOT_FOUND)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }
}
