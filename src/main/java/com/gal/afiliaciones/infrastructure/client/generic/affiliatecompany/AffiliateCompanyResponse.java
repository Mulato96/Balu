package com.gal.afiliaciones.infrastructure.client.generic.affiliatecompany;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gal.afiliaciones.infrastructure.client.generic.BaseResponseDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AffiliateCompanyResponse extends BaseResponseDTO  {


    @JsonProperty("departamento")
    private String departamento;

    @JsonProperty("municipio")
    private String municipio;

    @JsonProperty("ocupacion")
    private String ocupacion;

    @JsonProperty("estado")
    private String estadoRl;

    @JsonProperty("nomVinLaboral")
    private String nomVinLaboral;

    @JsonProperty("fechaInicioVinculacion")
    private String fechaInicioVinculacion;

    @JsonProperty("fechaFinVinculacion")
    private String fechaFinVinculacion;

    @JsonProperty("fechaInicioContrato")
    private String fechaInicioContrato;

    @JsonProperty("fechaFinContrato")
    private String fechaFinContrato;

    @JsonProperty("afp")
    private Integer afp;

    @JsonProperty("nombreAfp")
    private String nombreAfp;


    @JsonProperty("idArl")
    private String idArl;

    @JsonProperty("nombreArl")
    private String nombreArl;

    @JsonProperty("salario")
    private Double salario;


    @JsonProperty("tpDocEmpresa")
    private String tpDocEmpresa;

    @JsonProperty("idDepartamentoEmp")
    private Integer idDepartamentoEmp;

    @JsonProperty("departamentoEmp")
    private String departamentoEmp;

    @JsonProperty("idMunicipioEmp")
    private Integer idMunicipioEmp;

    @JsonProperty("municipioEmp")
    private String municipioEmp;

    @JsonProperty("nomActEco")
    private String nomActEco;

    @JsonProperty("idCentroTrabajo")
    private Integer idCentroTrabajo;


    @JsonProperty("idSectorEconomico")
    private Integer idSectorEconomico;

    @JsonProperty("estadoContrato")
    private String estadoContrato;

    @JsonProperty("codigoSubempresa")
    private String codigoSubempresa;

    @JsonProperty("idCargo")
    private Integer idCargo;

    @JsonProperty("cargo")
    private String cargo;

    @JsonProperty("idSede")
    private Integer idSede;

    @JsonProperty("idTipoDocEmp")
    private String idTipoDocEmp;

    @JsonProperty("ocupacionVoluntario")
    private String ocupacionVoluntario;

    @JsonProperty("codigoOcupacion")
    private String codigoOcupacion;

    @JsonProperty("idTipoVinculado")
    private Integer idTipoVinculado;

    @JsonProperty("tipoVinculado")
    private String tipoVinculado;

}
