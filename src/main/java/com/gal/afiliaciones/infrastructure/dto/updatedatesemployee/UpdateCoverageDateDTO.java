package com.gal.afiliaciones.infrastructure.dto.updatedatesemployee;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class UpdateCoverageDateDTO {
    private Long idVinculacion;
    private String tipoVinculacion;
    private LocalDate nuevaFechaCobertura;
    private String causalNovedad;
    private String observaciones;
    private Integer idSubEmpresa;
}
