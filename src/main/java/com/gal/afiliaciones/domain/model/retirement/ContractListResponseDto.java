package com.gal.afiliaciones.domain.model.retirement;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ContractListResponseDto {
    private String tipoVinculacion;
    private String cargo;
    private LocalDate fechaVinculacionArl;
    private LocalDate fechaInicioContrato;
    private LocalDate fechaFinContrato;
    private String estado;
    private String acciones;
}