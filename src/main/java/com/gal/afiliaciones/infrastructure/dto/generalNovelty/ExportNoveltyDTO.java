package com.gal.afiliaciones.infrastructure.dto.generalNovelty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportNoveltyDTO {
    private String canalRadicacion;
    private String fechaRecibido;
    private String identificacionCotizante;
    private String nombreCotizante;
    private String tipoNovedad;
    private String estado;
    private String causal;
}
