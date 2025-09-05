package com.gal.afiliaciones.infrastructure.dto.ruaf;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RuafTypes {

    RMRP("RMRP"),
    RNRA("RNRA"),
    RNRE("RNRE");

    private final String value;

}
