package com.gal.afiliaciones.config.ex.economicactivity;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class CodeCIIUNotFound extends AffiliationsExceptionBase {
    public CodeCIIUNotFound(String message){
        super(Error.builder()
                .type(Error.Type.CODE_CIIU_NOT_FOUND)
                .message(message)
                .build(), HttpStatus.NOT_FOUND);
    }
}
