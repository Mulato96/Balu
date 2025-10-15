package com.gal.afiliaciones.infrastructure.dto.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEmployerResponseDTO {

    @JsonProperty("empleador")
    private EmployerData empleador;

    @JsonProperty("empleados")
    private List<Dependiente> empleados;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployerData {
        @JsonProperty("tipoDocumento")
        private String tipoDocumento;
        @JsonProperty("numeroDocumento")
        private String numeroDocumento;
        @JsonProperty("idSubEmpresa")
        private String idSubEmpresa;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dependiente {
        @JsonProperty("tipoDocumento")
        private String tipoDocumento;
        @JsonProperty("numeroDocumento")
        private String numeroDocumento;
    }
} 