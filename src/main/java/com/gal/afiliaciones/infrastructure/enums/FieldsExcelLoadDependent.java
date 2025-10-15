package com.gal.afiliaciones.infrastructure.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public enum FieldsExcelLoadDependent{

    DOCUMENT_TYPE_CODE(             "TIPO DOCUMENTO DE IDENTIFICACIÓN", "A", "Validar información del campo Tipo documento identificación; puedes apoyarte en la tabla Tipos documento de identificación del documento guía para diligenciar el archivo."),
    DOCUMENT_NUMBER(                "NÚMERO DOCUMENTO IDENTIFICACIÓN",  "B", "Validar información del campo Número documento identificación."),
    FIRST_SURNAME(                  "PRIMER APELLIDO",                  "C", "Validar información del campo Primer apellido"),
    SECOND_SURNAME(                 "SEGUNDO APELLIDO",                 "D", "Validar información del campo Segundo apellido"),
    FIRST_NAME(                     "PRIMER NOMBRE",                    "E", "Validar información del campo Primer nombre"),
    SECOND_NAME(                    "SEGUNDO NOMBRE",                   "F", "Validar información del campo Segundo nombre"),
    BIRTH_DATE(                     "FECHA DE NACIMIENTO",              "G", "Validar información del campo Fecha de nacimiento."),
    GENDER_CODE(                    "SEXO",                             "H", "Validar información del campo Código Sexo; puedes apoyarte en la tabla Sexo del documento guía para diligenciar el archivo."),
    DEPARTMENT_CODE(                "DEPARTAMENTO TRABAJADOR",          "I", "Validar información del campo Código departamento; puedes apoyarte en la tabla Departamento del documento guía para diligenciar el archivo."),
    RESIDENCE_MUNICIPALITY_CODE(    "MUNICIPIO RESIDENCIA TRABAJADOR",  "J", "Validar información del campo Código municipio; puedes apoyarte en la tabla Municipio del documento guía para diligenciar el archivo."),
    ADDRESS(                        "DIRECCION",                        "K", "Validar información del campo Dirección, campo vacio"),
    PHONE(                          "CELULAR O TELÉFONO",               "L", "Validar información del campo Celular o teléfono"),
    EPS_CODE(                       "CODIGO EPS",                       "M", "Validar información del campo Código EPS trabajador; puedes apoyarte en la tabla EPS del documento guía para diligenciar el archivo."),
    AFP_CODE(                       "CODIGO AFP",                       "N", "Validar información del campo Código AFP trabajador; puedes apoyarte en la tabla AFP del documento guía para diligenciar el archivo."),
    COVERAGE_START_DATE(            "FECHA INICIO COBERTURA",           "O", "Validar información del campo fecha inicio de cobertura."),
    OCCUPATION(                     "CARGO U OCUPACIÓN",                "P", "Validar información del campo Código cargo u ocupación; puedes apoyarte en la tabla Cargo-ocupación del documento guía para diligenciar el archivo."),
    SALARY(                         "SALARIO IBC",                      "Q", "Validar información del campo Salario"),
    ECONOMIC_ACTIVITY_CODE(         "CÓDIGO ACTIVIDAD ECONOMICA",       "R", "Validar información del campo Actividad económica; puedes apoyarte en la tabla Actividades Económicas del documento guía para diligenciar el archivo."),
    DEPARTMENT_WORK(                "DEPARTAMENTO DONDE LABORA",        "S", "Validar información del campo DEPARTAMENTO DONDE LABORA, campo vacio"),
    MUNICIPALITY_WORK(              "MUNICIPIO DONDE LABORA",           "T", "Validar información del campo MUNICIPIO DONDE LABORA, campo vacio"),
    EMPLOYER_DOCUMENT_TYPE_CODE(    "TIPO DOCUMENTO EMPLEADOR",         "U", "Validar información del campo Tipo documento identificación; puedes apoyarte en la tabla Tipos documento de identificación del documento guía para diligenciar el archivo."),
    EMPLOYER_DOCUMENT(              "NÚMERO DOCUMENTO EMPLEADOR",       "V", "Validar información del campo Número documento empleador."),
    SUB_COMPANY(                    "SUB EMPRESA",                      "W", "Validar información del campo SUB EMPRESA, campo vacio"),
    WORK_MODALITY_CODE(             "MODALIDAD DE TRABAJO",             "X", "Validar información del campo Código modalidad de trabajo; puedes apoyarte en la tabla Modalidad de trabajo del documento guía para diligenciar el archivo.");

    private final String description;
    private final String letter;
    private final String error;

    FieldsExcelLoadDependent(String description, String letter, String error) {
        this.description = description;
        this.letter = letter;
        this.error = error;
    }

    public static FieldsExcelLoadDependent findByDescription(String description){

        for(FieldsExcelLoadDependent field : FieldsExcelLoadDependent.values()){
            if(field.getDescription().equals(description)){
                return field;
            }
        }

        return null;
    }


    public static List<String> getDescripcion() {

        List<String> listDescription = new ArrayList<>();

        for(FieldsExcelLoadDependent field : FieldsExcelLoadDependent.values()){
            listDescription.add(field.getDescription());
        }

        return listDescription;
    }

    public static FieldsExcelLoadDependent findByLetter(String letter){

        for(FieldsExcelLoadDependent field : FieldsExcelLoadDependent.values()){
            if(field.getLetter().equals(letter)){
                return field;
            }
        }

        return null;
    }

    public static Map<String, Integer> map(){

        Map<String, Integer> map = new HashMap<>();

        for(FieldsExcelLoadIndependent field : FieldsExcelLoadIndependent.values()){

            map.put(field.getDescription(), converterLetterInNumber(field.getLetter()));
        }

        return map;
    }

    private static int converterLetterInNumber(String letter){

        int resultado = 0;

        for (int i = 0; i < letter.length(); i++) {
            char c = letter.charAt(i);
            resultado = resultado * 26 + (c - 'A' + 1);
        }

        return resultado;
    }
}
