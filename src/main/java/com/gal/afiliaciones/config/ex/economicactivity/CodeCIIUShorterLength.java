package com.gal.afiliaciones.config.ex.economicactivity;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class CodeCIIUShorterLength extends AffiliationsExceptionBase {
    public CodeCIIUShorterLength(String message){
        super(Error.builder()
                .type(Error.Type.CODE_SHORTER_LENGTH)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }
}
