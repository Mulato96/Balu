package com.gal.afiliaciones.infrastructure.dto.sat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferableEmployerResponse {
    @JsonProperty("codigoARL")
    private String codigoArl;
    private String tipoDocumentoEmpleador;
    private String numeroDocumentoEmpleador;
    private String consecutivoNITEmpleador;
    private String empresaTrasladable;
    private Integer causal;
    private String arlAfiliacion;
}


