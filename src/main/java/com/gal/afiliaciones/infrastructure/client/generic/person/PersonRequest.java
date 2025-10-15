package com.gal.afiliaciones.infrastructure.client.generic.person;

import lombok.Data;

@Data
public class PersonRequest {
    private String idTipoDoc;
    private String idPersona;
    private int idAfp;
    private int idPais;
    private int idDepartamento;
    private int idMunicipio;
    private String idEps;
    private String nombre1;
    private String nombre2;
    private String apellido1;
    private String apellido2;
    private String fechaNacimiento;
    private String fechaFallecimiento;
    private String sexo;
    private String indZona;
    private String telefonoPersona;
    private String faxPersona;
    private String direccionPersona;
    private String emailPersona;
    private String usuarioAud;
    private String maquinaAud;
}
