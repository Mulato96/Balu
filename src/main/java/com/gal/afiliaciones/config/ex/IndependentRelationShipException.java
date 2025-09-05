package com.gal.afiliaciones.config.ex;

import org.springframework.http.HttpStatus;

public class IndependentRelationShipException extends AffiliationsExceptionBase{
    public IndependentRelationShipException(String message) {
        super(Error.builder().type(Error.Type.INDEPENDENT_RELATIONSHIP).message(message).build(), HttpStatus.BAD_REQUEST);
    }
}
