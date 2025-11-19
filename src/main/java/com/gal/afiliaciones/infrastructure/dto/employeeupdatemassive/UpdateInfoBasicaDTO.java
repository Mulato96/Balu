package com.gal.afiliaciones.infrastructure.dto.employeeupdatemassive;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UpdateInfoBasicaDTO {
    private String tipoDocumento;
    private String numeroIdentificacion;
    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private LocalDate fechaNacimiento;
    private String nacionalidad;
    private String sexo;
    private String afp;
    private String eps;
    private String email;
    private String telefono1;
    private String telefono2;
    private Integer idDepartamento;
    private Integer idCiudad;
    private String direccionTexto;
    private String observaciones;
}
