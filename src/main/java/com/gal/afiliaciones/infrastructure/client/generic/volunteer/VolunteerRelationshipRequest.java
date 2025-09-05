package com.gal.afiliaciones.infrastructure.client.generic.volunteer;

import lombok.Data;

@Data
public class VolunteerRelationshipRequest {
    private String idTipoDoc;
    private String idPersona;
    private String nombre1;
    private String nombre2;
    private String apellido1;
    private String apellido2;
    private String fechaNacimiento;
    private String sexo;
    private String emailPersona;
    private int idDepartamento;
    private int idMunicipio;
    private String direccionPersona;
    private String telefonoPersona;
    private String idEps;
    private int idAfp;
    private double ibc;
    private int idOcupacion;
    private String fechaCobertura;
    private String idTipoDocConyuge;
    private String idPersonaConyuge;
    private String primerNombreConyuge;
    private String segundoNombreConyuge;
    private String primerApellidoConyuge;
    private String segundoApellidoConyuge;
    private int idDepartamentoConyuge;
    private int idMunicipioConyuge;
    private String telefonoConyuge;
}
