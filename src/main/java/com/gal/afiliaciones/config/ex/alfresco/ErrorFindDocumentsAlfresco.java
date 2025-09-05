package com.gal.afiliaciones.config.ex.alfresco;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class ErrorFindDocumentsAlfresco extends AffiliationsExceptionBase {

    public ErrorFindDocumentsAlfresco(String message){
        super(Error.builder()
                .type(Error.Type.ERROR_FIND_DOCUMENT_ALFRESCO)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }
}
