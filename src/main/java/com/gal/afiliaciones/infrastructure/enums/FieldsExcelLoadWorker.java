package com.gal.afiliaciones.infrastructure.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum FieldsExcelLoadWorker {

    AFFILIATION_TYPE_CODE("CÓDIGO TIPO DE VINCULACIÓN", "A"),
    DOCUMENT_TYPE_CODE("CÓDIGO TIPO DOCUMENTO DE IDENTIFICACIÓN", "B"),
    DOCUMENT_NUMBER("NÚMERO DOCUMENTO IDENTIFICACIÓN", "C"),
    EPS_CODE("CÓDIGO EPS TRABAJADOR", "D"),
    AFP_CODE("CÓDIGO AFP TRABAJADOR", "E"),
    OCCUPATION("CÓDIGO CARGO U OCUPACIÓN", "F"),
    RISK("RIESGO", "G"),
    CONTRACT_START_DATE("FECHA INICIO DE CONTRATO", "H"),
    CONTRACT_END_DATE("FECHA FIN DE CONTRATO", "I");

    private final String description;
    private final String letter;

    FieldsExcelLoadWorker(String description, String letter) {
        this.description = description;
        this.letter = letter;
    }

    public static FieldsExcelLoadWorker findByDescription(String description){

        for(FieldsExcelLoadWorker field : FieldsExcelLoadWorker.values()){
            if(field.getDescription().equals(description)){
                return field;
            }
        }
        return null;
    }

    public static List<String> getDescripcion() {

        List<String> listDescription = new ArrayList<>();
        for(FieldsExcelLoadWorker field : FieldsExcelLoadWorker.values()){
            listDescription.add(field.getDescription());
        }
        return listDescription;

    }

}
