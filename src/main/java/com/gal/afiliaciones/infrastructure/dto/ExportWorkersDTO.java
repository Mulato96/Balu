package com.gal.afiliaciones.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExportWorkersDTO {

    @JsonProperty("Identificación")
    private String identification;

    @JsonProperty("Nombres y Apellidos")
    private String fullName;

    @JsonProperty("Cargo")
    private String occupation;

    @JsonProperty("Fecha de afiliación")
    private String affiliationDate;

    @JsonProperty("Fecha inicio cobertura")
    private String coverageStartDate;

    @JsonProperty("Nombre EPS")
    private String epsName;

    @JsonProperty("Nombre AFP")
    private String afpName;

    @JsonProperty("Teléfono")
    private String phone;

    @JsonProperty("Correo Electrónico")
    private String email;

    @JsonProperty("Estado de afiliación")
    private String affiliationStatus;
}
