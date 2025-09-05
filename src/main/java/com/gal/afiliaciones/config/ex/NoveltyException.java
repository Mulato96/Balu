package com.gal.afiliaciones.config.ex;

import com.gal.afiliaciones.config.ex.Error.Type;
import org.springframework.http.HttpStatus;

public class NoveltyException extends AffiliationsExceptionBase {

    public NoveltyException(String message){
        super(Error.builder()
                .type(Type.NOVELTY_ERROR)
                .message(message)
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
