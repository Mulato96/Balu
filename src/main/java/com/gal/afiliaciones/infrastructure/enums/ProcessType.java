package com.gal.afiliaciones.infrastructure.enums;

import lombok.Getter;

@Getter
public enum ProcessType {
    AFFILIATION("1"), //Afiliación
    MEMBERSHIP_TRANSFER("2"), //Traslado
    MEMBERSHIP_TERMINATION("3"); //Terminación de afiliación

    private String value;

    ProcessType(String value){
        this.value = value;
    }

    public static String findByValue(Long value) {
        for (ProcessType processType : ProcessType.values()) {
            if (processType.getValue().equalsIgnoreCase(String.valueOf(value))) {
                return processType.getValue();
            }
        }
        throw new IllegalArgumentException("No enum constant with description " + value);
    }
}
