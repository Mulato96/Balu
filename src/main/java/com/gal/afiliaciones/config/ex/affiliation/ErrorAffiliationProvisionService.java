package com.gal.afiliaciones.config.ex.affiliation;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class ErrorAffiliationProvisionService extends AffiliationsExceptionBase {
    public ErrorAffiliationProvisionService(String message){
        super(Error.builder()
                .type(Error.Type.ERROR_AFFILIATION)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }
}
