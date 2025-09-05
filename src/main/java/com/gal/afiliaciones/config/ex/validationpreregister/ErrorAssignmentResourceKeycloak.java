package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class ErrorAssignmentResourceKeycloak extends AffiliationsExceptionBase {

    public ErrorAssignmentResourceKeycloak(String message){
        super(Error.builder()
                .type(Error.Type.ERROR_ASSIGNMENT_RESOURCE_KEYCLOAK)
                .message(message)
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
