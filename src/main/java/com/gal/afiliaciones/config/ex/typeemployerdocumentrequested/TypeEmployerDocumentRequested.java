package com.gal.afiliaciones.config.ex.typeemployerdocumentrequested;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class TypeEmployerDocumentRequested extends AffiliationsExceptionBase {
    public TypeEmployerDocumentRequested(String message){
        super(Error.builder()
                .type(Error.Type.ERROR_TYPE_EMPLOYER_DOCUMENT_REQUESTED)
                .message(message)
                .build(), HttpStatus.NOT_FOUND);
    }
}
