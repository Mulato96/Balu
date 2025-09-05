package com.gal.afiliaciones.infrastructure.dto.employerreport;

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
public class ExportEmployerReportResponseDTO {

    @JsonProperty("Identificación")
    private String identification;

    @JsonProperty("Nombres y Apellidos")
    private String fullName;

    @JsonProperty("Cargo")
    private String occupation;

    @JsonProperty("Tipo de vinculación")
    private String affiliationType;

    @JsonProperty("Tipo de novedad")
    private String noveltyType;

    @JsonProperty("Fecha de afiliación")
    private String affiliationDate;

    @JsonProperty("Estado de afiliación")
    private String affiliationStatus;

}