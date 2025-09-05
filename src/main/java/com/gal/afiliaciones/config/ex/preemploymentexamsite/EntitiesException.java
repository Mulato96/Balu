package com.gal.afiliaciones.config.ex.preemploymentexamsite;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class EntitiesException extends AffiliationsExceptionBase {

    public EntitiesException(String message) {
        super(Error.builder()
                .type(Error.Type.ERROR_SEARCH_PREEMPLOYMENT_EXAM_SITES)
                .message(message)
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
