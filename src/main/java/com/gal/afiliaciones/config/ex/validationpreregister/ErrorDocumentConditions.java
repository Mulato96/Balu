package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class ErrorDocumentConditions extends AffiliationsExceptionBase {
    public ErrorDocumentConditions(String message) {
        super(Error.builder()
                .type(Error.Type.INVALID_DOCUMENT_CONDITIONS)
                .message(message).build(), HttpStatus.BAD_REQUEST);
    }
}
