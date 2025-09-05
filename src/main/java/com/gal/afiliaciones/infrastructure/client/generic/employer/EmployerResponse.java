package com.gal.afiliaciones.infrastructure.client.generic.employer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EmployerResponse {
    @JsonProperty("idTipoDoc")
    private String idTipoDoc;
    @JsonProperty("idEmpresa")
    private String idEmpresa;
    @JsonProperty("razonSocial")
    private String razonSocial;
    @JsonProperty("idSubEmpresa")
    private Integer idSubEmpresa;
    @JsonProperty("razonSocialSubempresa")
    private String razonSocialSubempresa;
    @JsonProperty("idDepartamento")
    private Integer idDepartamento;
    @JsonProperty("idMunicipio")
    private Integer idMunicipio;
    @JsonProperty("idActEconomica")
    private Long idActEconomica;
    @JsonProperty("direccionEmpresa")
    private String direccionEmpresa;
    @JsonProperty("telefonoEmpresa")
    private String telefonoEmpresa;
    @JsonProperty("emailEmpresa")
    private String emailEmpresa;
    @JsonProperty("idTipoDocRepLegal")
    private String idTipoDocRepLegal;
    @JsonProperty("idRepresentanteLegal")
    private String idRepresentanteLegal;
    @JsonProperty("representanteLegal")
    private String representanteLegal;
    @JsonProperty("estado")
    private Integer estado;
    @JsonProperty("nombreEstado")
    private String nombreEstado;
    @JsonProperty("fechaAfiliacionEfectiva")
    private String fechaAfiliacionEfectiva;
    @JsonProperty("fechaRetiroInactivacion")
    private String fechaRetiroInactivacion;
}
