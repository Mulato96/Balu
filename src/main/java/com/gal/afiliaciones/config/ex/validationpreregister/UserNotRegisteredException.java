package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class UserNotRegisteredException extends AffiliationsExceptionBase {
    public UserNotRegisteredException(String message){
        super(Error.builder()
                .type(Error.Type.PASSWORD_INCORRECT)
                .message(message)
                .build(), HttpStatus.UNAUTHORIZED);
    }
}