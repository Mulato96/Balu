package com.gal.afiliaciones.domain.model.retirement;

import lombok.Data;

@Data
public class WorkerSearchRequestDto {
    private String tipoDocumento;
    private String numeroIdentificacion;
    private String empresa;
}