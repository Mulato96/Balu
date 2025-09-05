package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class ErrorUpdateUserKeycloak extends AffiliationsExceptionBase {

    public ErrorUpdateUserKeycloak(String message){
        super(Error.builder()
                .type(Error.Type.ERROR_UPDATE_USER_KEYCLOAK)
                .message(message)
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
