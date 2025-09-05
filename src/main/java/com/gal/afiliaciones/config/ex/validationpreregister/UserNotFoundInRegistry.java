package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class UserNotFoundInRegistry extends AffiliationsExceptionBase {
    public UserNotFoundInRegistry(String message){
        super(Error.builder()
                .type(Error.Type.USER_NOT_FOUND_IN_REGISTRY)
                .message(message)
                .build(), HttpStatus.NOT_FOUND);
    }
}
