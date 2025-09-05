package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class DuplicateSessionException extends AffiliationsExceptionBase  {

    public DuplicateSessionException(String message){
        super(Error.builder()
                .type(Error.Type.ACTIVE_SESSION)
                .message(message)
                .build(), HttpStatus.CONFLICT);
    }
}