package com.gal.afiliaciones.config.ex.card;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class ErrorGeneratedCard extends AffiliationsExceptionBase {
    public ErrorGeneratedCard(String message) {
        super(Error.builder()
                .type(Error.Type.ERROR_GENERATED_CARD)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }
}
