package com.gal.afiliaciones.infrastructure.client.generic.businessgroup;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BusinessGroupResponse {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("nombre")
    private String name;

    @JsonProperty("tipoDocEmp")
    private String employerDocType;

    @JsonProperty("numeDocEmp")
    private String employerDocNumber;

    @JsonProperty("razonSocial")
    private String companyName;
}
