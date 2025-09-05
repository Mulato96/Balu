package com.gal.afiliaciones.infrastructure.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum FieldsExcelLoadIndependent{

    AFFILIATION_TYPE_CODE("CÓDIGO TIPO DE VINCULACIÓN", "A", "Validar información del campo tipo de vinculación; puedes apoyarte en la tabla Tipo de vinculación del documento guía para diligenciar el archivo"),
    COVERAGE_START_DATE("FECHA INICIO COBERTURA", "B", "Validar información del campo fecha inicio de cobertura."),
    DOCUMENT_TYPE_CODE("CÓDIGO TIPO DOCUMENTO DE IDENTIFICACIÓN", "C", "Validar información del campo Tipo documento identificación; puedes apoyarte en la tabla Tipos documento de identificación del documento guía para diligenciar el archivo."),
    DOCUMENT_NUMBER("NÚMERO DOCUMENTO IDENTIFICACIÓN", "D", "Validar información del campo Número documento identificación."),
    FIRST_NAME("PRIMER NOMBRE", "E",  "Validar información del campo Primer nombre"),
    SECOND_NAME("SEGUNDO NOMBRE", "F",  "Validar información del campo Segundo nombre "),
    FIRST_SURNAME("PRIMER APELLIDO", "G", "Validar información del campo Primer apellido"),
    SECOND_SURNAME("SEGUNDO APELLIDO", "H", "Validar información del campo Segundo apellido"),
    BIRTH_DATE("FECHA DE NACIMIENTO", "I", "Validar información del campo Fecha de nacimiento."),
    GENDER_CODE("CÓDIGO SEXO", "J", "Validar información del campo Código Sexo; puedes apoyarte en la tabla Sexo del documento guía para diligenciar el archivo."),
    GENDER("SEXO", "K", "Validar información del campo Sexo Otro; ya que no puede estar vacío."),
    NATIONALITY("CÓDIGO NACIONALIDAD", "L", "Validar información del campo Código nacionalidad; puedes apoyarte en la tabla Nacionalidad del documento guía para diligenciar el archivo."),
    EPS_CODE("CÓDIGO EPS TRABAJADOR", "M", "Validar información del campo Código EPS trabajador; puedes apoyarte en la tabla EPS del documento guía para diligenciar el archivo."),
    AFP_CODE("CÓDIGO AFP TRABAJADOR", "N", "Validar información del campo Código AFP trabajador; puedes apoyarte en la tabla AFP del documento guía para diligenciar el archivo."),
    DEPARTMENT_CODE("CÓDIGO DEPARTAMENTO TRABAJADOR", "O", "Validar información del campo Código departamento; puedes apoyarte en la tabla Departamento del documento guía para diligenciar el archivo."),
    RESIDENCE_MUNICIPALITY_CODE("CÓDIGO MUNICIPIO RESIDENCIA TRABAJADOR", "P", "Validar información del campo Código municipio; puedes apoyarte en la tabla Municipio del documento guía para diligenciar el archivo."),
    PHONE("CELULAR O TELÉFONO", "Q", "Validar información del campo Celular o teléfono"),
    EMAIL("CORREO ELECTRÓNICO", "R", "Validar información del campo Correo electrónico"),
    OCCUPATION("CÓDIGO CARGO U OCUPACIÓN", "S", "Validar información del campo Código cargo u ocupación; puedes apoyarte en la tabla Cargo-ocupación del documento guía para diligenciar el archivo."),
    CONTRACT_QUALITY_CODE("CÓDIGO CALIDAD DEL CONTRATO", "T", "Validar información del campo Código calidad del contrato; puedes apoyarte en la tabla Calidad del contrato del documento guía para diligenciar el archivo."),
    TYPE_CONTRACT_CODE("CÓDIGO TIPO DE CONTRATO", "U", "Validar información del campo Código tipo de contrato; puedes apoyarte en la tabla Tipo de contrato del documento guía para diligenciar el archivo."),
    CODE_SUPPLIES_TRANSPORTATION("CODIGO SUMINISTRA TRANSPORTE", "V", "Validar información del campo suministra transporte; puedes apoyarte en la tabla Suministra transporte del documento guía para diligenciar el archivo."),
    START_DATE("FECHA DE INICIO", "W", "Validar información del campo Fecha de inicio."),
    CONTRACT_END_DATE("FECHA DE TERMINACIÓN", "X", "Validar información del campo Fecha de terminación."),
    CODE_ESTABLISHED_WORKING("CÓDIGO JORNADA ESTABLECIDA", "Y", "Validar información del campo Código jornada establecida; puedes apoyarte en la tabla Jornada establecida del documento guía para diligenciar el archivo."),
    TOTAL_VALUE_CONTRACT("VALOR TOTAL DEL CONTRATO", "Z", "Validar información del campo valor total del contrato"),
    ECONOMIC_ACTIVITY_CODE_CONTRACT("CÓDIGO DE ACTIVIDAD ECONÓMICA  DEL CONTRATO", "AA", "Validar información del campo Actividad económica; puedes apoyarte en la tabla Actividades Económicas del documento guía para diligenciar el archivo."),
    CODE_CONTRACTING_ECONOMIC_ACTIVITY("CÓDIGO ACTIVIDAD ECONÓMICA CONTRATANTE", "AB", "Validar información del campo Actividad económica; puedes apoyarte en la tabla Actividades Económicas del documento guía para diligenciar el archivo."),
    HEADQUARTERS_CODE("CÓDIGO SEDE", "AC", "Validar información del campo Código departamento sede y Código municipio sede ya que no existe sede del empleador en el lugar en donde labora el trabajador;  puedes apoyarte en las tablas Departamento y Municipios y dirección completa del documento guía para diligenciar el archivo"),
    EMPLOYER_DOCUMENT_TYPE_CODE("CÓDIGO TIPO DOCUMENTO EMPLEADOR", "AD", "Validar información del campo Tipo documento identificación; puedes apoyarte en la tabla Tipos documento de identificación del documento guía para diligenciar el archivo."),
    EMPLOYER_DOCUMENT("DOCUMENTO EMPLEADOR", "AE", "Validar información del campo Número documento empleador.");


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
}
