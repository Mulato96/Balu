package com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataExcelIndependentDTO {

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

    @JsonProperty("EPS TRABAJADOR")
    private String healthPromotingEntity;

    @JsonProperty("AFP TRABAJADOR")
    private String pensionFundAdministrator;

    @JsonProperty("DEPARTAMENTO TRABAJADOR")
    private String idDepartment;

    @JsonProperty("MUNICIPIO RESIDENCIA TRABAJADOR")
    private String idCity;

    @JsonProperty("CELULAR O TELÉFONO")
    private String phone1;

    @JsonProperty("CORREO ELECTRÓNICO INDEPENDIENTE")
    private String email;

    @JsonProperty("CARGO U OCUPACIÓN")
    private String idOccupation;

    @JsonProperty("TIPO DE CONTRATO")
    private String contractType;

    @JsonProperty("SUMINISTRA TRANSPORTE")
    private String transportSupply;

    @JsonProperty("FECHA DE INICIO CONTRATO")
    private String startDate;

    @JsonProperty("FECHA DE TERMINACION CONTRATO")
    private String endDate;

    @JsonProperty("VALOR TOTAL DEL CONTRATO")
    private String contractTotalValue;

    @JsonProperty("CÓDIGO DE ACTIVIDAD ECONÓMICA DEL CONTRATO")
    private String codeActivityContract;

    @JsonProperty("ACTIVIDAD ECONOMICA CENTRO DE TRABAJO DEL CONTRATANTE")
    private String codeActivityEmployer;

    @JsonProperty("TIPO DOCUMENTO CONTRATANTE")
    private String employerDocumentTypeCodeContractor;

    @JsonProperty("NÚMERO DOCUMENTO CONTRATANTE")
    private String employerDocumentNumber;

    @JsonProperty("DIRECCION")
    private String address;

    @JsonProperty("SUB EMPRESA")
    private String subCompany;

    @JsonProperty("LA AFILIACION ES DE TAXISTA")
    private String isTaxi;

    @JsonProperty("NATURALEZA DEL CONTRATO")
    private String natureContract;

    @JsonProperty("DEPARTAMENTO DONDE LABORA")
    private String departmentWork;

    @JsonProperty("MUNICIPIO DONDE LABORA")
    private String municipalityWork;

    @JsonProperty("ERROR")
    private String error;

}
