package com.gal.afiliaciones.config.ex;

import org.springframework.http.HttpStatus;

public class UpdateNotFoundException extends AffiliationsExceptionBase{

    public UpdateNotFoundException(String message) {
        super(Error.builder().type(Error.Type.NOT_FOUND_UPDATES).message(message).build(), HttpStatus.NOT_FOUND);
    }
}
