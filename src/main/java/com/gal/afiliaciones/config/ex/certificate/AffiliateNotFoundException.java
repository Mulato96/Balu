package com.gal.afiliaciones.config.ex.certificate;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class AffiliateNotFoundException extends AffiliationsExceptionBase {
    public AffiliateNotFoundException(String message){
        super(Error.builder()
                .type(Error.Type.EMAIL_ALREADY_EXISTS)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }
}
