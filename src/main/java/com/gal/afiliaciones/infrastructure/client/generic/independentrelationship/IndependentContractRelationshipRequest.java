package com.gal.afiliaciones.infrastructure.client.generic.independentrelationship;

import lombok.Data;

@Data
public class IndependentContractRelationshipRequest {
    private String idTipoDoc;
    private String idPersona;
    private String idTipoDocEmp;
    private String idEmpresa;
    private int indVinculacionLaboral;
    private int idOcupacion;
    private int idActividadEconomica;
    private int idDepartamento;
    private int idMunicipio;
    private int idSede;
    private int idCentroTrabajo;
    private String fechaInicioVinculacion;
    private int teletrabajo;
    private int idTipoVinculado;
    private int subEmpresa;
    private int claseContrato;
    private int tipoContrato;
    private int tipoEntidad;
    private String suministraTransporte;
    private int numeroMeses;
    private String fechaInicioContrato;
    private String fechaFinContrato;
    private double valorTotalContrato;
    private double valorMensualContrato;
    private double ibc;
    private int normalizacion;
}
