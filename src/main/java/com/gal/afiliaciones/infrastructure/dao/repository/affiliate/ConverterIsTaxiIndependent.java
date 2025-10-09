package com.gal.afiliaciones.infrastructure.dao.repository.affiliate;

import jakarta.persistence.AttributeConverter;

public class ConverterIsTaxiIndependent implements AttributeConverter<Boolean, String> {

    private static final String YES = "si";
    private static final String NO = "no";


    @Override
    public String convertToDatabaseColumn(Boolean aBoolean) {
        return aBoolean != null && aBoolean ? YES : NO;
    }

    @Override
    public Boolean convertToEntityAttribute(String type) {
        return type != null && type.equalsIgnoreCase(YES);
    }
}
