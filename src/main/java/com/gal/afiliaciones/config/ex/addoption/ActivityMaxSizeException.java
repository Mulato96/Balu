package com.gal.afiliaciones.config.ex.addoption;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class ActivityMaxSizeException extends AffiliationsExceptionBase {
    public ActivityMaxSizeException(String message){
        super(Error.builder()
                .type(Error.Type.MAXIMUM_ACTIVITIES_ALLOWED)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }
}
