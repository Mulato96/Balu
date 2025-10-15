package com.gal.afiliaciones.infrastructure.dto.tmp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmpAffiliateStatusDTO {

    @JsonProperty("ID_TIPO_DOC_EMP")
    private String idTipoDocEmp;

    @JsonProperty("ID_EMPRESA")
    private String idEmpresa;

    @JsonProperty("ESTADO_EMPRESA")
    private String estadoEmpresa;

    @JsonProperty("ID_TIPO_DOC_PER")
    private String idTipoDocPer;

    @JsonProperty("ID_PERSONA")
    private String idPersona;

    @JsonProperty("ESTADO_PERSONA")
    private String estadoPersona;

    @JsonProperty("APP_SOURCE")
    private String appSource;
}


