package com.gal.afiliaciones.infrastructure.enums;

import lombok.Getter;

@Getter
public enum FieldsExcelLoadDependentIndependent {

    AFFILIATION_TYPE_CODE("CÓDIGO TIPO DE VINCULACIÓN", "A"),
    COVERAGE_START_DATE("FECHA INICIO COBERTURA", "B"),
    DOCUMENT_TYPE_CODE("CÓDIGO TIPO DOCUMENTO DE IDENTIFICACIÓN", "C"),
    DOCUMENT_NUMBER("NÚMERO DOCUMENTO IDENTIFICACIÓN", "D"),
    FIRST_NAME("PRIMER NOMBRE", "E"),
    SECOND_NAME("SEGUNDO NOMBRE", "F"),
    FIRST_SURNAME("PRIMER APELLIDO", "G"),
    SECOND_SURNAME("SEGUNDO APELLIDO", "H"),
    BIRTH_DATE("FECHA DE NACIMIENTO", "I"),
    GENDER_CODE("CÓDIGO SEXO", "J"),
    GENDER("SEXO", "K"),
    NATIONALITY("CÓDIGO NACIONALIDAD", "L"),
    EPS_CODE("CÓDIGO EPS TRABAJADOR", "M"),
    AFP_CODE("CÓDIGO AFP TRABAJADOR", "N"),
    ARL_CODE("CÓDIGO ARL ACTUAL", "O"),
    DEPARTMENT_CODE("CÓDIGO DEPARTAMENTO", "P"),
    RESIDENCE_MUNICIPALITY_CODE("CÓDIGO MUNICIPIO RESIDENCIA", "Q"),
    FULL_ADDRESS("DIRECCION COMPLETA", "R"),
    PHONE("CELULAR O TELÉFONO", "S"),
    WORK_MODALITY_CODE("CÓDIGO MODALIDAD DE TRABAJO", "T"),
    EMAIL("",""),
    SALARY("SALARIO", "U"),
    OCCUPATION("CÓDIGO CARGO U OCUPACIÓN", "V"),
    CONTRACT_END_DATE("FECHA FIN DE CONTRATO", "W"),
    ECONOMIC_ACTIVITY_CODE("CÓDIGO ACTIVIDAD ECONOMICA", "X"),
    DEPARTMENT("CÓDIGO DEPARTAMENTO SEDE", "Y"),
    MUNICIPALITY_HEADQUARTERS_CODE("CÓDIGO MUNICIPIO SEDE", "Z"),
    EMPLOYER_DOCUMENT_TYPE_CODE("CÓDIGO TIPO DOCUMENTO EMPLEADOR", "AA"),
    EMPLOYER_DOCUMENT("DOCUMENTO EMPLEADOR", "AB");

    private final String description;
    private final String letter;

    FieldsExcelLoadDependentIndependent(String description, String letter) {
        this.description = description;
        this.letter = letter;
    }

    public static FieldsExcelLoadDependentIndependent findByDescription(String description){

        for(FieldsExcelLoadDependentIndependent field : FieldsExcelLoadDependentIndependent.values()){
            if(field.getDescription().equals(description)){
                return field;
            }
        }

        return null;
    }

}
