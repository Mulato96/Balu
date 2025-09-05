package com.gal.afiliaciones.infrastructure.controller.excepctionManagment;

import com.gal.afiliaciones.config.ex.Error;
import com.gal.afiliaciones.config.ex.affiliation.ExistsRetirementAffiliationException;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AffiliateNotFound.class)
    public ResponseEntity<Error> handleAffiliateNotFound(AffiliateNotFound ex) {
        return new ResponseEntity<>(ex.getError(), ex.getHttpStatus());
    }

    @ExceptionHandler(ExistsRetirementAffiliationException.class)
    public ResponseEntity<Error> handleAffiliateRetirementExist(ExistsRetirementAffiliationException ex) {
        return new ResponseEntity<>(ex.getError(), ex.getHttpStatus());
    }


}
