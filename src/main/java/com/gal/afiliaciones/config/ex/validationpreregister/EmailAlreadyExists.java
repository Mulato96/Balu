package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExists extends AffiliationsExceptionBase {
    public EmailAlreadyExists(String message){
        super(Error.builder()
                .type(Error.Type.EMAIL_ALREADY_EXISTS)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }
}
