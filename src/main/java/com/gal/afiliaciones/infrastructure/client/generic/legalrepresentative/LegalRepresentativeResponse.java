package com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LegalRepresentativeResponse {
    @JsonProperty("idTipoDoc")
    private String idTipoDoc;

    @JsonProperty("idEmpresa")
    private String idEmpresa;

    @JsonProperty("idSubEmpresa")
    private Integer idSubEmpresa;

    @JsonProperty("razonSocialSubempresa")
    private String razonSocialSubempresa;

    @JsonProperty("idPersona")
    private String idPersona;

    @JsonProperty("nombre1")
    private String nombre1;

    @JsonProperty("nombre2")
    private String nombre2;

    @JsonProperty("apellido1")
    private String apellido1;

    @JsonProperty("apellido2")
    private String apellido2;

    @JsonProperty("emailRepresentateLegal")
    private String emailRepresentateLegal;
}
