package com.gal.afiliaciones.config.ex.sat;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class SatUpstreamError extends AffiliationsExceptionBase {

    public SatUpstreamError(String message) {
        super(Error.builder()
                .type(Error.Type.SAT_UPSTREAM_UNAVAILABLE)
                .message(message)
                .build(), HttpStatus.SERVICE_UNAVAILABLE);
    }
}


