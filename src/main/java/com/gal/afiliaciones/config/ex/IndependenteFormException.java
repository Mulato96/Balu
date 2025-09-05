package com.gal.afiliaciones.config.ex;

import org.springframework.http.HttpStatus;

public class IndependenteFormException extends AffiliationsExceptionBase {

    public IndependenteFormException(Error.Type errorType) {
        super(Error.builder().type(errorType).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
