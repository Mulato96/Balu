package com.gal.afiliaciones.config.ex;

import org.springframework.http.HttpStatus;

public class PasswordExpiredException extends AffiliationsExceptionBase {

    public PasswordExpiredException(String message) {
        super(Error.builder().type(Error.Type.PASSWORD_EXPIRED).message(message).build(), HttpStatus.BAD_REQUEST);
    }
}
