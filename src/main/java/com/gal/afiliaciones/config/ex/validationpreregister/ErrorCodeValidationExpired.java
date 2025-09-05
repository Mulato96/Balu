package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class ErrorCodeValidationExpired extends AffiliationsExceptionBase {

    public ErrorCodeValidationExpired(String message){
        super(Error.builder()
                .type(Error.Type.ERROR_VALIDATE_CODE)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }
}
