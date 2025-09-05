package com.gal.afiliaciones.infrastructure.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public enum FieldsExcelLoadCertificates {

    TYPE_DOCUMENT("Tipo Doc Persona"),
    NUMBER_DOCUMENT("Nume Doc Persona"),
    ADDRESSED("Dirigido");

    private final String description;

    FieldsExcelLoadCertificates(String description) {
        this.description = description;
    }

    public static List<String> getDescription() {

        List<String> listDescription = new ArrayList<>();

        for(FieldsExcelLoadCertificates field : FieldsExcelLoadCertificates.values()){
            listDescription.add(field.description);
        }

        return listDescription;
    }
}
