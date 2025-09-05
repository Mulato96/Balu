package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class ErrorNumberAttemptsExceeded extends AffiliationsExceptionBase {

    public ErrorNumberAttemptsExceeded(String message){
        super(Error.builder()
                .type(Error.Type.LIMIT_ATTEMPTS_EXCEEDED)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }
}
