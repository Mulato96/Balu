package com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative;

import lombok.Data;

@Data
public class LegalRepresentativeRequest {
    private String idTipoDoc;
    private String idPersona;
    private String nombre1;
    private String nombre2;
    private String apellido1;
    private String apellido2;
    private String idTipoDocEmp;
    private String idEmpresa;
    private Integer subEmpresa;
    private String emailRepresentateLegal;
    private String usuarioAud;
    private String maquinaAud;
}
