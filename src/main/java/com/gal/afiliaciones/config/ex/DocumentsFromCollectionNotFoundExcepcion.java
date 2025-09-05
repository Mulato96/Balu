package com.gal.afiliaciones.config.ex;

import org.springframework.http.HttpStatus;

public class DocumentsFromCollectionNotFoundExcepcion extends AffiliationsExceptionBase{

    public DocumentsFromCollectionNotFoundExcepcion(String message) {
        super(Error.builder().type(Error.Type.NOT_FOUND_DOCUMENTS_FROM_COLLECTION).message(message).build(), HttpStatus.NOT_FOUND);
    }
}
