package com.gal.afiliaciones.config.ex.affiliation;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class FamilyMemeberError extends AffiliationsExceptionBase {

    public FamilyMemeberError(Error.Type errorType) {
        super(Error.builder().type(errorType).build(), HttpStatus.BAD_REQUEST);
    }

}
