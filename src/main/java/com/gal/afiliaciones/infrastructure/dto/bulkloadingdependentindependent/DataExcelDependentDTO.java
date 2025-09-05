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

    @JsonProperty("CÓDIGO TIPO DE VINCULACIÓN")
    private String idBondingType;

    @JsonProperty("FECHA INICIO COBERTURA")
    private String coverageDate;

    @JsonProperty("CÓDIGO TIPO DOCUMENTO DE IDENTIFICACIÓN")
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

    @JsonProperty("CÓDIGO SEXO")
    private String gender;

    @JsonProperty("SEXO")
    private String otherGender;

    @JsonProperty("CÓDIGO NACIONALIDAD")
    private String nationality;

    @JsonProperty("CÓDIGO EPS TRABAJADOR")
    private String healthPromotingEntity;

    @JsonProperty("CÓDIGO AFP TRABAJADOR")
    private String pensionFundAdministrator;

    @JsonProperty("CÓDIGO ARL ACTUAL")
    private String occupationalRiskManager;

    @JsonProperty("CÓDIGO DEPARTAMENTO TRABAJADOR")
    private String idDepartment;

    @JsonProperty("CÓDIGO MUNICIPIO RESIDENCIA TRABAJADOR")
    private String idCity;

    @JsonProperty("CELULAR O TELÉFONO")
    private String phone1;

    @JsonProperty("CÓDIGO MODALIDAD DE TRABAJO")
    private String idWorkModality;

    @JsonProperty("SALARIO")
    private String salary;

    @JsonProperty("CÓDIGO CARGO U OCUPACIÓN")
    private String idOccupation;

    @JsonProperty("FECHA FIN DE CONTRATO")
    private String endDate;

    @JsonProperty("CÓDIGO ACTIVIDAD ECONOMICA")
    private String economicActivityCode;

    @JsonProperty("CÓDIGO SEDE")
    private String idHeadquarter;

    @JsonProperty("CÓDIGO TIPO DOCUMENTO EMPLEADOR")
    private String employerDocumentTypeCodeContractor;

    @JsonProperty("DOCUMENTO EMPLEADOR")
    private String employerDocumentNumber;
}
