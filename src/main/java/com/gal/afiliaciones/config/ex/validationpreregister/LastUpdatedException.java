package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class LastUpdatedException extends AffiliationsExceptionBase {
    public LastUpdatedException(String message){
        super(Error.builder()
                .type(Error.Type.LAST_UPDATED_ERROR)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }
}
