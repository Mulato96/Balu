package com.gal.afiliaciones.config.ex.economicactivity;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class DescriptionNotFound extends AffiliationsExceptionBase {
    public DescriptionNotFound(String message){
        super(Error.builder()
                .type(Error.Type.DESCRIPTION_NOT_FOUND)
                .message(message)
                .build(), HttpStatus.NOT_FOUND);
    }
}
