package com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataExcelDependentDTO{

    @JsonProperty("ID REGISTRO")
    private Integer idRecord;

    @JsonProperty("FECHA INICIO COBERTURA")
    private String coverageDate;

    @JsonProperty("TIPO DOCUMENTO DE IDENTIFICACIÓN")
    private String identificationDocumentType;

    @JsonProperty("NÚMERO DOCUMENTO IDENTIFICACIÓN")
    private String identificationDocumentNumber;

    @JsonProperty("PRIMER NOMBRE")
    private String firstName;

    @JsonProperty("SEGUNDO NOMBRE")
    private String secondName;

    @JsonProperty("PRIMER APELLIDO")
    private String surname;

    @JsonProperty("SEGUNDO APELLIDO")
    private String secondSurname;

    @JsonProperty("FECHA DE NACIMIENTO")
    private String dateOfBirth;

    @JsonProperty("SEXO")
    private String gender;

    @JsonProperty("CODIGO EPS")
    private String healthPromotingEntity;

    @JsonProperty("CODIGO AFP")
    private String pensionFundAdministrator;

    @JsonProperty("DEPARTAMENTO TRABAJADOR")
    private String idDepartment;

    @JsonProperty("MUNICIPIO RESIDENCIA TRABAJADOR")
    private String idCity;

    @JsonProperty("CELULAR O TELÉFONO")
    private String phone1;

    @JsonProperty("MODALIDAD DE TRABAJO")
    private String idWorkModality;

    @JsonProperty("DIRECCION")
    private String address;

    @JsonProperty("SALARIO IBC")
    private String salary;

    @JsonProperty("CARGO U OCUPACIÓN")
    private String idOccupation;

    @JsonProperty("CÓDIGO ACTIVIDAD ECONOMICA")
    private String economicActivityCode;

    @JsonProperty("TIPO DOCUMENTO EMPLEADOR")
    private String employerDocumentTypeCodeContractor;

    @JsonProperty("NÚMERO DOCUMENTO EMPLEADOR")
    private String identificationDocumentNumberContractor;

    @JsonProperty("SUB EMPRESA")
    private String subCompany;

    @JsonProperty("DEPARTAMENTO DONDE LABORA")
    private String departmentWork;

    @JsonProperty("MUNICIPIO DONDE LABORA")
    private String municipalityWork;

    @JsonProperty("ERROR")
    private String error;
}
