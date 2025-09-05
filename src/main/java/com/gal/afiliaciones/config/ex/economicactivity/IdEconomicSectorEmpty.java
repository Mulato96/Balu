package com.gal.afiliaciones.config.ex.economicactivity;

import org.springframework.http.HttpStatus;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;

public class IdEconomicSectorEmpty extends AffiliationsExceptionBase {
    public IdEconomicSectorEmpty(String message){
        super(Error.builder()
                .type(Error.Type.INVALID_ARGUMENT)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }
}
