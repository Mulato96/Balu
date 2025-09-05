package com.gal.afiliaciones.config.ex;

import org.springframework.http.HttpStatus;

public class PolicyException extends AffiliationsExceptionBase{

    public PolicyException(Error.Type errorType) {
        super(Error.builder().type(errorType).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
