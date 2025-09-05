package com.gal.afiliaciones.config.ex.generalnovelty;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import com.gal.afiliaciones.config.ex.Error.Type;
import org.springframework.http.HttpStatus;

public class GeneralNoveltyException extends AffiliationsExceptionBase {

    public GeneralNoveltyException(String message, Type errorType, HttpStatus status) {
        super(Error.builder()
                .type(errorType)
                .message(message)
                .build(), status);
    }

    public static GeneralNoveltyException emptyInput(Long idAffiliate) {
        return new GeneralNoveltyException(
                "El campo '" + idAffiliate + "' no puede estar vacío o nulo.",
                Type.INVALID_ARGUMENT,
                HttpStatus.BAD_REQUEST
        );
    }

    public static GeneralNoveltyException notFoundByAffiliate(Long idAffiliate) {
        return new GeneralNoveltyException(
                "No se encontraron novedades para el afiliado con ID: " + idAffiliate,
                Type.REGISTER_NOT_FOUND,
                HttpStatus.NOT_FOUND
        );
    }

    public static GeneralNoveltyException notFoundByEmployer(String contributorIdentification) {
        return new GeneralNoveltyException(
                "No se encontraron novedades para el empleador con documento: " + contributorIdentification,
                Type.REGISTER_NOT_FOUND,
                HttpStatus.NOT_FOUND);
    }

    public static GeneralNoveltyException internalError(String message) {
        return new GeneralNoveltyException(
                message,
                Type.NOVELTY_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
