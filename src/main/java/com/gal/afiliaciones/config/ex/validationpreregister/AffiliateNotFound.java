package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class AffiliateNotFound extends AffiliationsExceptionBase {

    public AffiliateNotFound(String message){
        super(Error.builder()
                .type(Error.Type.AFFILIATION_NOT_FOUND)
                .message(message)
                .build(), HttpStatus.NOT_FOUND);
    }
}
