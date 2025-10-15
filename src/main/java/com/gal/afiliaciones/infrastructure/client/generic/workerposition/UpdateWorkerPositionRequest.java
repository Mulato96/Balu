package com.gal.afiliaciones.infrastructure.client.generic.workerposition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateWorkerPositionRequest {
    private String idTipoDoc;
    private String idPersona;
    private String idTipoDocEmp;
    private String idEmpresa;
    private Integer subEmpresa;
    private Integer idTipoVinculacion;
    private Integer idOcupacion;
}

