package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class PhoneAlreadyExists extends AffiliationsExceptionBase {
    public PhoneAlreadyExists(String message){
        super(Error.builder()
                .type(Error.Type.PHONE1_ALREADY_EXISTS)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }
}
