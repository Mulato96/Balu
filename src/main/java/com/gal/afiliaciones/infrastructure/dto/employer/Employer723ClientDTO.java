package com.gal.afiliaciones.infrastructure.dto.employer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employer723ClientDTO {

    private String idTipoDoc;
    private String idEmpresa;
    private String dv;
    private String razonSocial;
    private Integer idDepartamento;
    private Integer idMunicipio;
    private String direccionEmpresa;
    private String telefonoEmpresa;
    private String emailEmpresa;
    private String idTipoDocRepLegal;
    private String idRepresentanteLegal;
    private String nombre1RepresentanteLegal;
    private String nombre2RepresentanteLegal;
    private String apellido1RepresentanteLegal;
    private String apellido2RepresentanteLegal;
    private String eps;
    private Integer afp;
    private String fechaNacimiento;
    private String sexo;

}
