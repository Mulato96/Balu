package com.gal.afiliaciones.infrastructure.client.generic.affiliatecompany;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AffiliateCompanyResponse {
    @JsonProperty("tipoDoc")
    private String tipoDoc;

    @JsonProperty("idPersona")
    private String idPersona;

    @JsonProperty("fechaNacimiento")
    private String fechaNacimiento;

    @JsonProperty("departamento")
    private String departamento;

    @JsonProperty("municipio")
    private String municipio;

    @JsonProperty("idDepartamento")
    private Integer idDepartamento;

    @JsonProperty("idMunicipio")
    private Integer idMunicipio;

    @JsonProperty("nombre1")
    private String nombre1;

    @JsonProperty("nombre2")
    private String nombre2;

    @JsonProperty("apellido1")
    private String apellido1;

    @JsonProperty("apellido2")
    private String apellido2;

    @JsonProperty("emailPersona")
    private String emailPersona;

    @JsonProperty("idOcupacion")
    private Integer idOcupacion;

    @JsonProperty("ocupacion")
    private String ocupacion;

    @JsonProperty("telefonoPersona")
    private String telefonoPersona;

    @JsonProperty("sexo")
    private String sexo;

    @JsonProperty("estadoRl")
    private String estadoRl;

    @JsonProperty("nomVinLaboral")
    private String nomVinLaboral;

    @JsonProperty("fechaInicioVinculacion")
    private String fechaInicioVinculacion;

    @JsonProperty("fechaFinVinculacion")
    private String fechaFinVinculacion;

    @JsonProperty("afp")
    private Integer afp;

    @JsonProperty("nombreAfp")
    private String nombreAfp;

    @JsonProperty("eps")
    private String eps;

    @JsonProperty("nombreEps")
    private String nombreEps;

    @JsonProperty("idArl")
    private String idArl;

    @JsonProperty("nombreArl")
    private String nombreArl;

    @JsonProperty("salario")
    private Double salario;

    @JsonProperty("direccion")
    private String direccion;

    @JsonProperty("tpDocEmpresa")
    private String tpDocEmpresa;

    @JsonProperty("idEmpresa")
    private String idEmpresa;

    @JsonProperty("razonSocial")
    private String razonSocial;

    @JsonProperty("direccionEmpresa")
    private String direccionEmpresa;

    @JsonProperty("idDepartamentoEmp")
    private Integer idDepartamentoEmp;

    @JsonProperty("departamentoEmp")
    private String departamentoEmp;

    @JsonProperty("idMunicipioEmp")
    private Integer idMunicipioEmp;

    @JsonProperty("municipioEmp")
    private String municipioEmp;

    @JsonProperty("telefonoEmpresa")
    private String telefonoEmpresa;

    @JsonProperty("emailEmpresa")
    private String emailEmpresa;

    @JsonProperty("indZona")
    private String indZona;

    @JsonProperty("idActEconomica")
    private Long idActEconomica;

    @JsonProperty("nomActEco")
    private String nomActEco;

    @JsonProperty("fechaAfiliacionEfectiva")
    private String fechaAfiliacionEfectiva;

    @JsonProperty("estadoEmpresa")
    private String estadoEmpresa;

    @JsonProperty("idCentroTrabajo")
    private Integer idCentroTrabajo;

    @JsonProperty("idSucursal")
    private Integer idSucursal;

    @JsonProperty("idSectorEconomico")
    private Integer idSectorEconomico;

    @JsonProperty("idTipoVinculado")
    private Integer idTipoVinculado;

}
