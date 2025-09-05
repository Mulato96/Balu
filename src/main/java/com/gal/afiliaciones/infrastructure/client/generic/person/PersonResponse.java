package com.gal.afiliaciones.infrastructure.client.generic.person;

import lombok.Data;

@Data
public class PersonResponse {
    private String idTipoDoc;
    private String idPersona;
    private Integer idAfp;
    private Integer idPais;
    private Integer idDepartamento;
    private Integer idMunicipio;
    private String idEps;
    private String nombre1;
    private String nombre2;
    private String apellido1;
    private String apellido2;
    private String fechaNacimiento;
    private String sexo;
    private String indZona;
    private String telefonoPersona;
    private String faxPersona;
    private String direccionPersona;
    private String emailPersona;
}