package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class ErrorContainDataPersonal extends AffiliationsExceptionBase {

    public ErrorContainDataPersonal(String message){
        super(Error.builder()
                .type(Error.Type.USER_NOT_CONTAIN_DATA_PERSONAL)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }
}
