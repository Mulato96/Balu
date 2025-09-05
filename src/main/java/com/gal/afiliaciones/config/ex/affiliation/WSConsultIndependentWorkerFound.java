package com.gal.afiliaciones.config.ex.affiliation;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class WSConsultIndependentWorkerFound extends AffiliationsExceptionBase {
    public WSConsultIndependentWorkerFound(String message){
        super(Error.builder()
                .type(Error.Type.WORKER_FOUND)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }
}
