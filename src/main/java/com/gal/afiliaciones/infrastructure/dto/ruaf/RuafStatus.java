package com.gal.afiliaciones.infrastructure.dto.ruaf;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RuafStatus {

    COMPLETE("Completado"),
    ERROR("Error");

    private final String value;

}
