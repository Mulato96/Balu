package com.gal.afiliaciones.infrastructure.dto.actualizacionfechas;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class UpdateCoverageDateDto {
    private Long idVinculacion;
    private String tipoVinculacion;
    private LocalDate nuevaFechaCobertura;
    private String causalNovedad;
    private String observaciones;
    private Integer idSubEmpresa;
}
