package com.gal.afiliaciones.infrastructure.dto.generalNovelty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportWorkerNoveltyDTO {
    private String canalRadicacion;
    private String radicado;
    private String fechaRadicacion;
    private String tipoNovedad;
    private String estado;
    private String observacion;
}


