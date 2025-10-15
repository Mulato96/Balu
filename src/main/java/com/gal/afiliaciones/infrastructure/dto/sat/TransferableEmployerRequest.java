package com.gal.afiliaciones.infrastructure.dto.sat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
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
    @JsonAlias("numeroDocumentoEmpleador")
    private String consecutivoNITEmpleador;
}


