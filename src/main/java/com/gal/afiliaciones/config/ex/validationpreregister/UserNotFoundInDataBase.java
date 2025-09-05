package com.gal.afiliaciones.config.ex.validationpreregister;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class UserNotFoundInDataBase extends AffiliationsExceptionBase {
    public UserNotFoundInDataBase(String message){
        super(Error.builder()
                .type(Error.Type.USER_NOT_FOUND_IN_DATA_BASE)
                .message(message)
                .build(), HttpStatus.NOT_FOUND);
    }
}
