package com.gal.afiliaciones.infrastructure.dto.novelty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerDisplacementNotificationRequest {
    private String idTipoDoc;
    private String idPersona;
    private String idTipoDocEmp;
    private String idEmpresa;
    private Integer idTipoVinculacion;
    private String fechaInicioDesp; // yyyy-MM-dd
    private String fechaFinDesp;    // yyyy-MM-dd
    private Integer codigoDepartamento;
    private Integer codigoMunicipio;
    private String motivoDesp;
}


