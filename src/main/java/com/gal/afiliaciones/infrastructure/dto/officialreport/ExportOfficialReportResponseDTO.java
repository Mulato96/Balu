package com.gal.afiliaciones.infrastructure.dto.officialreport;

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
public class ExportOfficialReportResponseDTO {

    @JsonProperty("Identificación")
    private String identification;

    @JsonProperty("Nombres y Apellidos")
    private String fullName;

    @JsonProperty("Cargo")
    private String occupation;

    @JsonProperty("Edad")
    private String age;

    @JsonProperty("Tipo de vinculación")
    private String affiliationType;

    @JsonProperty("Tipo de novedad")
    private String noveltyType;

    @JsonProperty("Estado de afiliación")
    private String affiliationStatus;

    @JsonProperty("Fecha de afiliación")
    private String affiliationDate;

    @JsonProperty("Departamento")
    private String department;

    @JsonProperty("Ciudad/Municipio")
    private String city;

    @JsonProperty("Código actividad económica")
    private String economicActivityCode;

    @JsonProperty("Descripción actividad económica")
    private String descriptionEconomicActivity;

    @JsonProperty("Fecha inicio de cobertura")
    private String coverageStartDate;

    @JsonProperty("Fecha de novedad")
    private String noveltyDate;

    @JsonProperty("Tipo de persona")
    private String personType;
}