package com.gal.afiliaciones.config.ex.economicactivity;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class CodeAndDescriptionException extends AffiliationsExceptionBase {
    public CodeAndDescriptionException(String message){
        super(Error.builder()
                .type(Error.Type.CODE_DESCRIPTION_NULL)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }
}
