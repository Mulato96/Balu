package com.gal.afiliaciones.config.ex;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class AffiliationsExceptionBase extends RuntimeException{
    private final Error error;
    private final HttpStatus httpStatus;
    private final Error.Type type;

    protected AffiliationsExceptionBase(Error error, HttpStatus httpStatus) {
        this.error = error;
        this.type = getType();
        this.httpStatus = httpStatus;
    }
}
