package com.gal.afiliaciones.infrastructure.dto.novelty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerRetirementNoveltyRequest {
    private String idTipoDocEmp;
    private String idEmpresa;
    private Integer subempresa;
    private String idTipoDocPers;
    private String idPersona;
    private Integer tipoVinculacion;
    private String fechaRetiro; // yyyy-MM-dd
}


