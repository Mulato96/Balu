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
public class TransferableEmployerRequest {
    @JsonProperty("TipoDocumentoEmpleador")
    private String tipoDocumentoEmpleador;
    @JsonProperty("NumeroDocumentoEmpleador")
    private String numeroDocumentoEmpleador;
    @JsonProperty("ConsecutivoNITEmpleador")
    private String consecutivoNITEmpleador;
}


