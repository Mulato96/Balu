package com.gal.afiliaciones.infrastructure.dto.actualizacionfechas;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class VinculacionDto {
    private Long idVinculacion;
    private String tipoVinculacion;
    private String contratante;
    private String cargo;
    private LocalDate fechaVinculacion;
    private LocalDate fechaCobertura;
    private String estado;
    private List<SubEmpresaDto> subEmpresas;
}
