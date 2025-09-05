package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class ErrorCreateUserKeycloak extends AffiliationsExceptionBase {

    public ErrorCreateUserKeycloak(String message){
        super(Error.builder()
                .type(Error.Type.ERROR_CREATE_USER_KEYCLOAK)
                .message(message)
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
