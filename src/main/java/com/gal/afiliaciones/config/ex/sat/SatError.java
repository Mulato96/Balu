package com.gal.afiliaciones.config.ex.sat;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class SatError extends AffiliationsExceptionBase {

    public SatError(String message) {
        super(Error.builder()
                .type(Error.Type.SAT_VALIDATION_ERROR)
                .message(message)
                .build(), HttpStatus.UNPROCESSABLE_ENTITY);
    }
}


