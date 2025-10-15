package com.gal.afiliaciones.infrastructure.client.generic.independentactivity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateIndependentEconomicActivityRequest {
    private String idTipoDocEmp;
    private String idEmpresa;
    private Integer subempresa;
    private String idTipoDocPers;
    private String idPersona;
    private Integer idActEconomica;
}

