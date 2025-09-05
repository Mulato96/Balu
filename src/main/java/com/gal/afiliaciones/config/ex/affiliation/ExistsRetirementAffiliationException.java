package com.gal.afiliaciones.config.ex.affiliation;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class ExistsRetirementAffiliationException extends AffiliationsExceptionBase {

    public ExistsRetirementAffiliationException(String message){
        super(Error.builder()
                .type(Error.Type.AFFILIATION_RETIRED)
                .message(message)
                .build(), HttpStatus.NOT_FOUND);
    }

}
