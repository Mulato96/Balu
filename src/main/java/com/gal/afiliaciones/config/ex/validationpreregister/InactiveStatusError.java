package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class InactiveStatusError extends AffiliationsExceptionBase {
    public InactiveStatusError(String message){
        super(Error.builder()
                .type(Error.Type.USER_INACTIVE)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }
}
