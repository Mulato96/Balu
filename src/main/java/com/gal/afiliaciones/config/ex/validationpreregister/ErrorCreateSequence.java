package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class ErrorCreateSequence extends AffiliationsExceptionBase {

    public ErrorCreateSequence(String message){
        super(Error.builder()
                .type(Error.Type.ERROR_CREATING_SEQUENCE)
                .message(message)
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
