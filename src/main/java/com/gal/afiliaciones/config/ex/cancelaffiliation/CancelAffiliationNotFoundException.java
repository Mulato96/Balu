package com.gal.afiliaciones.config.ex.cancelaffiliation;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class CancelAffiliationNotFoundException extends AffiliationsExceptionBase {
    public CancelAffiliationNotFoundException(String message){
        super(Error.builder()
                .type(Error.Type.DATE_AFFILIATION_ERROR)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }
}
