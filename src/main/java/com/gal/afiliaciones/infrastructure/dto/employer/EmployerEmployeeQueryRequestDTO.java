package com.gal.afiliaciones.infrastructure.dto.employer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployerEmployeeQueryRequestDTO {

    @JsonProperty("tDocEmp")
    private String tDocEmp;

    @JsonProperty("idEmp")
    private String idEmp;

    @JsonProperty("tDocAfi")
    private String tDocAfi;

    @JsonProperty("idAfi")
    private String idAfi;
}
