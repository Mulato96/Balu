package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class ErrorCreateResourceKeycloak extends AffiliationsExceptionBase {

    public ErrorCreateResourceKeycloak(String message){
        super(Error.builder()
                .type(Error.Type.ERROR_CREATE_RESOURCE_KEYCLOAK)
                .message(message)
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ErrorCreateResourceKeycloak(String message, HttpStatus status){
        super(Error.builder()
                .type(Error.Type.ERROR_CONFLICT_KEYCLOAK)
                .message(message)
                .build(), status);
    }
}
