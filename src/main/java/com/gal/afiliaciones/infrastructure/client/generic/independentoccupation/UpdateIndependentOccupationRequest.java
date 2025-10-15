package com.gal.afiliaciones.infrastructure.client.generic.independentoccupation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateIndependentOccupationRequest {
    private String idTipoDoc;
    private String idPersona;
    private Integer idOcupacion;
    private Integer tipoNovedad;
}

