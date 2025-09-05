package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class LoginAttemptsError extends AffiliationsExceptionBase {
    public LoginAttemptsError(String message){
        super(Error.builder()
                .type(Error.Type.ATTEMPTS_EXAGGERATED)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }
}
