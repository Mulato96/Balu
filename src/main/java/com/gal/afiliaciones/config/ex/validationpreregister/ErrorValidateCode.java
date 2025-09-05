package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class ErrorValidateCode extends AffiliationsExceptionBase {
    public ErrorValidateCode(String message) {
        super(Error.builder()
                .type(Error.Type.ERROR_VALIDATE_CODE)
                .message(message)
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
