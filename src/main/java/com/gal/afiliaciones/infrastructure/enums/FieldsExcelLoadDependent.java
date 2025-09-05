package com.gal.afiliaciones.infrastructure.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum FieldsExcelLoadDependent{

    AFFILIATION_TYPE_CODE("CÓDIGO TIPO DE VINCULACIÓN", "A", "Validar información del campo tipo de vinculación; puedes apoyarte en la tabla Tipo de vinculación del documento guía para diligenciar el archivo"),
    COVERAGE_START_DATE("FECHA INICIO COBERTURA", "B", "Validar información del campo fecha inicio de cobertura."),
    DOCUMENT_TYPE_CODE("CÓDIGO TIPO DOCUMENTO DE IDENTIFICACIÓN", "C", "Validar información del campo Tipo documento identificación; puedes apoyarte en la tabla Tipos documento de identificación del documento guía para diligenciar el archivo."),
    DOCUMENT_NUMBER("NÚMERO DOCUMENTO IDENTIFICACIÓN", "D", "Validar información del campo Número documento identificación."),
    FIRST_NAME("PRIMER NOMBRE", "E", "Validar información del campo Primer nombre"),
    SECOND_NAME("SEGUNDO NOMBRE", "F", "Validar información del campo Segundo nombre"),
    FIRST_SURNAME("PRIMER APELLIDO", "G", "Validar información del campo Primer apellido"),
    SECOND_SURNAME("SEGUNDO APELLIDO", "H", "Validar información del campo Segundo apellido"),
    BIRTH_DATE("FECHA DE NACIMIENTO", "I", "Validar información del campo Fecha de nacimiento."),
    GENDER_CODE("CÓDIGO SEXO", "J", "Validar información del campo Código Sexo; puedes apoyarte en la tabla Sexo del documento guía para diligenciar el archivo."),
    GENDER("SEXO", "K", "Validar información del campo Sexo Otro; ya que no puede estar vacío."),
    NATIONALITY("CÓDIGO NACIONALIDAD", "L", "Validar información del campo Código nacionalidad; puedes apoyarte en la tabla Nacionalidad del documento guía para diligenciar el archivo."),
    EPS_CODE("CÓDIGO EPS TRABAJADOR", "M", "Validar información del campo Código EPS trabajador; puedes apoyarte en la tabla EPS del documento guía para diligenciar el archivo."),
    AFP_CODE("CÓDIGO AFP TRABAJADOR", "N", "Validar información del campo Código AFP trabajador; puedes apoyarte en la tabla AFP del documento guía para diligenciar el archivo."),
    ARL_CODE("CÓDIGO ARL ACTUAL", "O", "Validar información del campo Código ARL trabajador; puedes apoyarte en la tabla ARL del documento guía para diligenciar el archivo."),
    DEPARTMENT_CODE("CÓDIGO DEPARTAMENTO TRABAJADOR", "P", "Validar información del campo Código departamento; puedes apoyarte en la tabla Departamento del documento guía para diligenciar el archivo."),
    RESIDENCE_MUNICIPALITY_CODE("CÓDIGO MUNICIPIO RESIDENCIA TRABAJADOR", "Q", "Validar información del campo Código municipio; puedes apoyarte en la tabla Municipio del documento guía para diligenciar el archivo."),
    PHONE("CELULAR O TELÉFONO", "R", "Validar información del campo Celular o teléfono"),
    WORK_MODALITY_CODE("CÓDIGO MODALIDAD DE TRABAJO", "S", "Validar información del campo Código modalidad de trabajo; puedes apoyarte en la tabla Modalidad de trabajo del documento guía para diligenciar el archivo."),
    SALARY("SALARIO", "T", "Validar información del campo Salario"),
    OCCUPATION("CÓDIGO CARGO U OCUPACIÓN", "U", "Validar información del campo Código cargo u ocupación; puedes apoyarte en la tabla Cargo-ocupación del documento guía para diligenciar el archivo."),
    CONTRACT_END_DATE("FECHA FIN DE CONTRATO", "V", "Validar información del campo Fecha fin de contrato."),
    ECONOMIC_ACTIVITY_CODE("CÓDIGO ACTIVIDAD ECONOMICA", "W", "Validar información del campo Actividad económica; puedes apoyarte en la tabla Actividades Económicas del documento guía para diligenciar el archivo."),
    HEADQUARTERS_CODE("CÓDIGO SEDE", "X", "Validar información del campo Código departamento sede y Código municipio sede ya que no existe sede del empleador en el lugar en donde labora el trabajador;  puedes apoyarte en las tablas Departamento y Municipios y dirección completa del documento guía para diligenciar el archivo."),
    EMPLOYER_DOCUMENT_TYPE_CODE("CÓDIGO TIPO DOCUMENTO EMPLEADOR", "Y", "Validar información del campo Tipo documento identificación; puedes apoyarte en la tabla Tipos documento de identificación del documento guía para diligenciar el archivo."),
EMPLOYER_DOCUMENT("DOCUMENTO EMPLEADOR", "Z", "Validar información del campo Número documento empleador.");


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
}
