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
public class ExportOfficialWorkersReportResponseDTO {

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

    @JsonProperty("Estado de afiliación")
    private String affiliationStatus;

    @JsonProperty("Fecha de afiliación")
    private String affiliationDate;

    @JsonProperty("Departamento")
    private String department;

    @JsonProperty("Ciudad/Municipio")
    private String city;
}