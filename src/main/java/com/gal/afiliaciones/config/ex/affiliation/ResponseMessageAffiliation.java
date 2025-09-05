package com.gal.afiliaciones.config.ex.affiliation;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class ResponseMessageAffiliation extends AffiliationsExceptionBase {
    public ResponseMessageAffiliation(String message){
        super(Error.builder()
                .type(Error.Type.MESSAGE_GENERATE_AFFILIATION)
                .message(message)
                .build(), HttpStatus.OK);
    }
}
