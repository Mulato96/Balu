package com.gal.afiliaciones.infrastructure.dto.employeeupdatemassive;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class WorkerUpdateDTO {
    private String tipoDocumento;
    private String numeroIdentificacion;
    private String eps;
    private String afp;
    private String email;
    private LocalDate fechaNacimiento;
    private String direccion;
    private String telefono;
    private String departamento;
    private String municipio;
    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private String sexo;
    private String nacionalidad;
    private String observaciones;
}
