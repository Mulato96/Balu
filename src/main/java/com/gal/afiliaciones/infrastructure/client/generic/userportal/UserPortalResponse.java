package com.gal.afiliaciones.infrastructure.client.generic.userportal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserPortalResponse {

    @JsonProperty("idUsuario")
    private Long idUsuario;

    @JsonProperty("perfilUsuario")
    private String perfilUsuario;

    @JsonProperty("idTipoDoc")
    private String idTipoDoc;

    @JsonProperty("idPersona")
    private String idPersona;

    @JsonProperty("nombreUsuario")
    private String nombreUsuario;

    @JsonProperty("correoUsuario")
    private String correoUsuario;

    @JsonProperty("estadoRelacionLaboral")
    private String estadoRelacionLaboral;

    @JsonProperty("fechaCreacionRegUsuario")
    private String fechaCreacionRegUsuario;

    @JsonProperty("fechaUltimoIngreso")
    private String fechaUltimoIngreso;

    @JsonProperty("idTipoDocEmpresa")
    private String idTipoDocEmpresa;

    @JsonProperty("idEmpresa")
    private String idEmpresa;

    @JsonProperty("razonSocial")
    private String razonSocial;

    @JsonProperty("idSubEmpresa")
    private Integer idSubEmpresa;

    @JsonProperty("razonSocialSubempresa")
    private String razonSocialSubempresa;

    @JsonProperty("correoEmpresa")
    private String correoEmpresa;
}
