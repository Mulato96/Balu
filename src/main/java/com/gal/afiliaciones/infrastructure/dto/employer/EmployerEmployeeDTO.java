package com.gal.afiliaciones.infrastructure.dto.employer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployerEmployeeDTO {

    @JsonProperty("ID_TIPO_DOC_EMP")
    private String idTipoDocEmp;

    @JsonProperty("ID_EMPRESA")
    private String idEmpresa;

    @JsonProperty("RAZON_SOCIAL")
    private String razonSocial;

    @JsonProperty("SUB_EMPRESA")
    private Integer subEmpresa;

    @JsonProperty("ID_TIPO_DOC_PER")
    private String idTipoDocPer;

    @JsonProperty("ID_PERSONA")
    private String idPersona;

    @JsonProperty("NOMBRE1")
    private String nombre1;

    @JsonProperty("NOMBRE2")
    private String nombre2;

    @JsonProperty("APELLIDO1")
    private String apellido1;

    @JsonProperty("APELLIDO2")
    private String apellido2;

    @JsonProperty("SEXO")
    private String sexo;

    @JsonProperty("FECHA_INICIO_VINCULACION")
    private String fechaInicioVinculacion;

    @JsonProperty("FECHA_FIN_VINCULACION")
    private String fechaFinVinculacion;

    @JsonProperty("FECHA_NACIMIENTO")
    private String fechaNacimiento;

    @JsonProperty("ID_AFP")
    private Integer idAfp;

    @JsonProperty("NOMBRE_AFP")
    private String nombreAfp;

    @JsonProperty("ID_EPS")
    private String idEps;

    @JsonProperty("NOMBRE_EPS")
    private String nombreEps;

    @JsonProperty("DIRECCION_PERSONA")
    private String direccionPersona;

    @JsonProperty("ID_ARP")
    private Integer idArp;

    @JsonProperty("NOMBRE_ARP")
    private String nombreArp;

    @JsonProperty("ID_OCUPACION")
    private Integer idOcupacion;

    @JsonProperty("NOMBRE_OCUPACION")
    private String nombreOcupacion;

    @JsonProperty("SALARIO_MENSUAL")
    private Long salarioMensual;

    @JsonProperty("ID_DEPARTAMENTO")
    private Integer idDepartamento;

    @JsonProperty("NOMBRE_DEPARTAMENTO")
    private String nombreDepartamento;

    @JsonProperty("ID_MUNICIPIO")
    private Integer idMunicipio;

    @JsonProperty("NOMBRE_MUNICIPIO")
    private String nombreMunicipio;

    @JsonProperty("APP_SOURCE")
    private String appSource;
}
