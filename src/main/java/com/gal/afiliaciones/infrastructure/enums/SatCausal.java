package com.gal.afiliaciones.infrastructure.enums;

import java.util.Arrays;
import java.util.Optional;

public enum SatCausal {
    NO_APLICA(0, "No aplica. La afiliación existe y la empresa cumple con los criterios de traslado."),
    NO_CUMPLE_PERMANENCIA(1, "La afiliación existe y la empresa no cumple tiempo de permanencia."),
    MORA_SIN_ACUERDO(2, "La afiliación existe y la empresa se encuentra en mora y no tiene acuerdo de pago."),
    SIN_AFILIACION_VIGENTE(3, "La empresa no tiene afiliación vigente reportada en el SAT.");

    private final int code;
    private final String message;

    SatCausal(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static Optional<SatCausal> fromCode(Integer code) {
        if (code == null) return Optional.empty();
        return Arrays.stream(values())
                .filter(c -> c.code == code)
                .findFirst();
    }
}


