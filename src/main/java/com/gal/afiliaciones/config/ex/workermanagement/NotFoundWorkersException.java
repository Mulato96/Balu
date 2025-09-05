package com.gal.afiliaciones.config.ex.workermanagement;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class NotFoundWorkersException extends AffiliationsExceptionBase {

    public NotFoundWorkersException(Error.Type errorType) {
        super(Error.builder().type(errorType).build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
