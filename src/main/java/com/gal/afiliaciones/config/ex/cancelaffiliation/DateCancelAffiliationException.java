package com.gal.afiliaciones.config.ex.cancelaffiliation;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class DateCancelAffiliationException extends AffiliationsExceptionBase {
    public DateCancelAffiliationException(String message){
        super(Error.builder()
                .type(Error.Type.AFFILIATE_DATE_ERROR)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }
}
