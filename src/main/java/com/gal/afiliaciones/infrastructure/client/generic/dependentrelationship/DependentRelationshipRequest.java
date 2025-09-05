package com.gal.afiliaciones.infrastructure.client.generic.dependentrelationship;

import lombok.Data;

@Data
public class DependentRelationshipRequest {
    private String idTipoDoc;
    private String idPersona;
    private String idTipoDocEmp;
    private String idEmpresa;
    private int indVinculacionLaboral;
    private int idOcupacion;
    private double salarioMensual;
    private int idActividadEconomica;
    private int idDepartamento;
    private int idMunicipio;
    private int idSede;
    private int idCentroTrabajo;
    private String fechaInicioVinculacion;
    private int teletrabajo;
    private int idTipoVinculado;
    private int subEmpresa;
}
