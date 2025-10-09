package com.gal.afiliaciones.infrastructure.dto.updatedatesemployee;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class VinculacionDTO {
    private Long idVinculacion;
    private String tipoVinculacion;
    private String contratante;
    private String cargo;
    private LocalDate fechaVinculacion;
    private LocalDate fechaCobertura;
    private String estado;
    private List<SubEmpresaDTO> subEmpresas;
}