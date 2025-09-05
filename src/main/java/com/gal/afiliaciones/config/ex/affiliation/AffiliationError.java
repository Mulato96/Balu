package com.gal.afiliaciones.config.ex.affiliation;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class AffiliationError extends AffiliationsExceptionBase {

    public AffiliationError(String message) {
        super(Error.builder()
                .type(Error.Type.ERROR_AFFILIATION)
                .message(message)
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
