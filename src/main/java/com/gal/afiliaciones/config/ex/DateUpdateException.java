package com.gal.afiliaciones.config.ex;

import org.springframework.http.HttpStatus;

public class DateUpdateException extends AffiliationsExceptionBase {

    public DateUpdateException(Error.Type type, String message) {
        super(createError(type, message), determineHttpStatus(type));
    }

    private static Error createError(Error.Type type, String message) {
        return Error.builder()
                .type(type)
                .message(message)
                .build();
    }

    private static HttpStatus determineHttpStatus(Error.Type type) {
        switch (type) {
            case VINCULACION_NOT_FOUND:
                return HttpStatus.NOT_FOUND;
            case FUNCIONARIO_NO_AUTORIZADO:
                return HttpStatus.FORBIDDEN;
            default:
                return HttpStatus.BAD_REQUEST;
        }
    }
}
