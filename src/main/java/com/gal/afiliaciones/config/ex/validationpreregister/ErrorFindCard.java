package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class ErrorFindCard extends AffiliationsExceptionBase {

    public ErrorFindCard(String message){
        super(Error.builder()
                   .type(Error.Type.ERROR_NOT_FOUND_CARD)
                   .message(message)
                   .build(), HttpStatus.NOT_FOUND);

    }
}
