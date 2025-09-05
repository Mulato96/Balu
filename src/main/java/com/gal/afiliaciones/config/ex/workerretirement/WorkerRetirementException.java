package com.gal.afiliaciones.config.ex.workerretirement;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class WorkerRetirementException extends AffiliationsExceptionBase {
    public WorkerRetirementException(String message) {
        super(Error.builder()
                .type(Error.Type.WORKER_RETIREMENT_ERROR)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }
}
