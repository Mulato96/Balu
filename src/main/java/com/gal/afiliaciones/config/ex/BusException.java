package com.gal.afiliaciones.config.ex;

import org.springframework.http.HttpStatus;

public class BusException extends AffiliationsExceptionBase{
    public BusException(String message){
        super(Error.builder()
                .type(Error.Type.NOT_FOUND_IN_REGISTRY)
                .message(message)
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
