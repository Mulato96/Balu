package com.gal.afiliaciones.infrastructure.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public enum FieldsExcelLoadIndependent{

    DOCUMENT_TYPE_CODE(                      "TIPO DOCUMENTO DE IDENTIFICACIÓN",                      "A", "Validar información del campo Tipo documento identificación; puedes apoyarte en la tabla Tipos documento de identificación del documento guía para diligenciar el archivo."),
    DOCUMENT_NUMBER(                         "NÚMERO DOCUMENTO IDENTIFICACIÓN",                       "B", "Validar información del campo Número documento identificación."),
    FIRST_SURNAME(                           "PRIMER APELLIDO",                                       "C", "Validar información del campo Primer apellido"),
    SECOND_SURNAME(                          "SEGUNDO APELLIDO",                                      "D", "Validar información del campo Segundo apellido"),
    FIRST_NAME(                              "PRIMER NOMBRE",                                         "E",  "Validar información del campo Primer nombre"),
    SECOND_NAME(                             "SEGUNDO NOMBRE",                                        "F",  "Validar información del campo Segundo nombre "),
    BIRTH_DATE(                              "FECHA DE NACIMIENTO",                                   "G", "Validar información del campo Fecha de nacimiento."),
    GENDER_CODE(                             "SEXO",                                                  "H", "Validar información del campo Sexo; puedes apoyarte en la tabla Sexo del documento guía para diligenciar el archivo."),
    EMAIL(                                   "CORREO ELECTRÓNICO INDEPENDIENTE",                      "I", "Validar información del campo Correo electrónico"),
    DEPARTMENT_CODE(                         "DEPARTAMENTO TRABAJADOR",                               "J", "Validar información del campo departamento; puedes apoyarte en la tabla Departamento del documento guía para diligenciar el archivo."),
    RESIDENCE_MUNICIPALITY_CODE(             "MUNICIPIO RESIDENCIA TRABAJADOR",                       "K", "Validar información del campo municipio; puedes apoyarte en la tabla Municipio del documento guía para diligenciar el archivo."),
    ADDRESS(                                 "DIRECCION",                                             "L", "Validar información del campo Dirección, campo vacio"),
    PHONE(                                   "CELULAR O TELÉFONO",                                    "M", "Validar información del campo Celular o teléfono"),
    OCCUPATION(                              "CARGO U OCUPACIÓN",                                     "N", "Validar información del campo cargo u ocupación; puedes apoyarte en la tabla Cargo-ocupación del documento guía para diligenciar el archivo."),
    EPS_CODE(                                "CODIGO EPS",                                            "O", "Validar información del campo EPS trabajador; puedes apoyarte en la tabla EPS del documento guía para diligenciar el archivo."),
    AFP_CODE(                                "CODIGO AFP",                                            "P", "Validar información del campo AFP trabajador; puedes apoyarte en la tabla AFP del documento guía para diligenciar el archivo."),
    TYPE_CONTRACT_CODE(                      "TIPO DE CONTRATO",                                      "Q", "Validar información del campo tipo de contrato; puedes apoyarte en la tabla Tipo de contrato del documento guía para diligenciar el archivo."),
    NATURE_OF_THE_CONTRACT(                  "NATURALEZA DEL CONTRATO",                               "R", "Validar información del campo NATURALEZA DEL CONTRATO, campo vacio"),
    CODE_SUPPLIES_TRANSPORTATION(            "SUMINISTRA TRANSPORTE",                                 "S", "Validar información del campo suministra transporte; puedes apoyarte en la tabla Suministra transporte del documento guía para diligenciar el archivo."),
    START_DATE(                              "FECHA DE INICIO CONTRATO",                              "T", "Validar información del campo Fecha de inicio."),
    CONTRACT_END_DATE(                       "FECHA DE TERMINACION CONTRATO",                         "U", "Validar información del campo Fecha de terminación contrato."),
    TOTAL_VALUE_CONTRACT(                    "VALOR TOTAL DEL CONTRATO",                              "V", "Validar información del campo valor total del contrato"),
    CODE_CONTRACTING_ECONOMIC_ACTIVITY(      "CÓDIGO DE ACTIVIDAD ECONÓMICA A EJECUTAR",              "W", "Validar información del campo Actividad económica; puedes apoyarte en la tabla Actividades Económicas del documento guía para diligenciar el archivo."),
    DEPARTMENT_WORK(                         "DEPARTAMENTO DONDE LABORA",                             "X", "Validar información del campo DEPARTAMENTO DONDE LABORA, campo vacio"),
    MUNICIPALITY_WORK(                       "MUNICIPIO DONDE LABORA",                                "Y", "Validar información del campo MUNICIPIO DONDE LABORA, campo vacio"),
    COVERAGE_START_DATE(                     "FECHA INICIO COBERTURA",                                "Z", "Validar información del campo fecha inicio de cobertura."),
    EMPLOYER_DOCUMENT_TYPE_CODE(             "TIPO DOCUMENTO CONTRATANTE",                            "AA", "Validar información del campo Tipo documento identificación; puedes apoyarte en la tabla Tipos documento de identificación del documento guía para diligenciar el archivo."),
    EMPLOYER_DOCUMENT(                       "NÚMERO DOCUMENTO CONTRATANTE",                          "AB", "Validar información del campo Número documento empleador."),
    SUB_COMPANY(                             "SUB EMPRESA",                                           "AC", "Validar información del campo SUB EMPRESA, campo vacio"),
    CODE_WORK_CONTRACTING_ECONOMIC_ACTIVITY( "ACTIVIDAD ECONOMICA CENTRO DE TRABAJO DEL CONTRATANTE", "AD", "Validar información del campo ACTIVIDAD ECONOMICA CENTRO DE TRABAJO DEL CONTRATANTE; puedes apoyarte en la tabla Actividades Económicas del documento guía para diligenciar el archivo."),
    IS_TAXI(                                 "LA AFILIACION ES DE TAXISTA",                           "AE", "Validar información del campo SUB EMPRESA, campo vacio");


    private final String description;
    private final String letter;
    private final String error;

    FieldsExcelLoadIndependent(String description, String letter, String error) {
        this.description = description;
        this.letter = letter;
        this.error = error;
    }

    public static FieldsExcelLoadIndependent findByDescription(String description){

        for(FieldsExcelLoadIndependent field : FieldsExcelLoadIndependent.values()){
            if(field.getDescription().equals(description)){
                return field;
            }
        }

        return null;
    }

    public static List<String> getDescripcion() {

        List<String> listDescription = new ArrayList<>();

        for(FieldsExcelLoadIndependent field : FieldsExcelLoadIndependent.values()){
           listDescription.add(field.getDescription());
        }

        return listDescription;
    }

    public static FieldsExcelLoadIndependent  findByLetter(String letter){

        for(FieldsExcelLoadIndependent field : FieldsExcelLoadIndependent.values()){
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
