package com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataExcelDependentIndependentDTO {


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

    @JsonProperty("CÓDIGO DEPARTAMENTO TRABAJADOR")
    private String idDepartment;

    @JsonProperty("CÓDIGO MUNICIPIO RESIDENCIA TRABAJADOR")
    private String idCity;

    @JsonProperty("DIRECCION COMPLETA TRABAJADOR")
    private String address;

    @JsonProperty("CELULAR O TELÉFONO")
    private String phone1;

    @JsonProperty("CÓDIGO CARGO U OCUPACIÓN")
    private String idOccupation;

    @JsonProperty("CÓDIGO DEPARTAMENTO SEDE")
    private String idDepartmentWorkCenter;

    @JsonProperty("CÓDIGO MUNICIPIO SEDE")
    private String idCityWorkCenter;

    @JsonProperty("DIRECCION COMPLETA")
    private String addressWorkCenter;

    @JsonProperty("CÓDIGO ARL ACTUAL")
    private String occupationalRiskManager;

    @JsonProperty("CÓDIGO MODALIDAD DE TRABAJO")
    private String idWorkModality;

    @JsonProperty("SALARIO")
    private String salary;

    @JsonProperty("CÓDIGO ACTIVIDAD ECONOMICA")
    private String economicActivityCode;

    @JsonProperty("CÓDIGO ACTIVIDAD ECONÓMICA CONTRATANTE")
    private String codeActivityEmployer;

    @JsonProperty("CÓDIGO DE ACTIVIDAD ECONÓMICA  DEL CONTRATO")
    private String codeActivityContract;

    @JsonProperty("CORREO ELECTRÓNICO")
    private String email;

    @JsonProperty("CÓDIGO CALIDAD DEL CONTRATO")
    private String contractQuality;

    @JsonProperty("CÓDIGO TIPO DE CONTRATO")
    private String contractType;

    @JsonProperty("CODIGO SUMINISTRA TRANSPORTE")
    private String transportSupply;

    @JsonProperty("FECHA DE INICIO")
    private String startDate;

    @JsonProperty("CÓDIGO JORNADA ESTABLECIDA")
    private String journeyEstablished;

    @JsonProperty("VALOR TOTAL DEL CONTRATO")
    private String contractTotalValue;

    @JsonProperty("FECHA FIN DE CONTRATO")
    @JsonAlias({"FECHA DE TERMINACIÓN"})
    private String endDate;

    @JsonProperty("CÓDIGO TIPO DOCUMENTO EMPLEADOR")
    private String employerDocumentTypeCodeContractor;

    @JsonProperty("DOCUMENTO EMPLEADOR")
    private String employerDocumentNumber;

    @JsonProperty("ID REGISTRO")
    private Integer idRecord;

}
