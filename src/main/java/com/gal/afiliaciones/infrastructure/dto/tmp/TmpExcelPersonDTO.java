package com.gal.afiliaciones.infrastructure.dto.tmp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmpExcelPersonDTO {

    @JsonProperty("ID_TIPO_DOC_PER")
    private String idTipoDocPer;

    @JsonProperty("ID_PERSONA")
    private String idPersona;

    @JsonProperty("APELLIDO1")
    private String apellido1;

    @JsonProperty("APELLIDO2")
    private String apellido2;

    @JsonProperty("NOMBRE1")
    private String nombre1;

    @JsonProperty("NOMBRE2")
    private String nombre2;

    // Format: DD-MM-YYYY (string in DTO; transform later)
    @JsonProperty("FECHA_NACIMIENTO")
    private String fechaNacimiento;

    // Values: MASCULINO/FEMENINO (map to M/F later)
    @JsonProperty("SEXO")
    private String sexo;

    @JsonProperty("DIRECCION_PERSONA")
    private String direccionPersona;

    @JsonProperty("ID_DEPARTAMENTO")
    private String idDepartamento;

    @JsonProperty("ID_MUNICIPIO")
    private String idMunicipio;

    @JsonProperty("ID_EPS")
    private String idEps;

    @JsonProperty("ID_AFP")
    private String idAfp;

    // Format: DD-MM-YYYY (string in DTO; transform later)
    @JsonProperty("FECHA_INICIO_VINCULACION")
    private String fechaInicioVinculacion;

    @JsonProperty("ID_OCUPACION")
    private String idOcupacion;

    // DECIMAL as text in DTO; parse to BigDecimal later
    @JsonProperty("SALARIO_MENSUAL")
    private String salarioMensual;

    // Employer/contractor identifiers (used by both maps)
    @JsonProperty("ID_TIPO_DOC_EMP")
    private String idTipoDocEmp;

    @JsonProperty("ID_EMPRESA")
    private String idEmpresa;

    @JsonProperty("SUB_EMPRESA")
    private String subEmpresa;

    // Optional: occupation name (used for independientes mapping)
    @JsonProperty("NOMBRE_OCUPACION")
    private String nombreOcupacion;
}


