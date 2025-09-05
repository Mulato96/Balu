package com.gal.afiliaciones.infrastructure.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SearchCriteriaAffiliations {

    RADICADO(1, "Radicado"),
    NUMERO_DOCUMENTO(2, "Número de documento"),
    RAZON_SOCIAL(3, "Razón social"),
    TIPO_AFILIACION(4, "Tipo de afiliación"),
    ETAPA_GESTION(5, "Etapa de gestión"),;

    private Integer id;
    private String description;

}
