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
public class ExportOfficialEmployerReportResponseDTO {

    @JsonProperty("Identificación")
    private String identification;

    @JsonProperty("Nombres Apellidos/Razón social")
    private String fullName;

    @JsonProperty("Código actividad económica")
    private String economicActivityCode;

    @JsonProperty("Descripción actividad económica")
    private String descriptionEconomicActivity;

    @JsonProperty("Tipo de persona")
    private String personType;

    @JsonProperty("Fecha de afiliación")
    private String affiliationDate;

    @JsonProperty("Fecha inicio de cobertura")
    private String coverageStartDate;

    @JsonProperty("Estado de afiliación")
    private String affiliationStatus;

    @JsonProperty("Departamento")
    private String department;

    @JsonProperty("Ciudad/Municipio")
    private String city;
}