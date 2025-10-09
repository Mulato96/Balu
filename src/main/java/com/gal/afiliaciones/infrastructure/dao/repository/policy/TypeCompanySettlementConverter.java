package com.gal.afiliaciones.infrastructure.dao.repository.policy;

import com.gal.afiliaciones.infrastructure.enums.TypeCompanySettlement;
import jakarta.persistence.AttributeConverter;

public class TypeCompanySettlementConverter implements AttributeConverter<TypeCompanySettlement, Boolean> {


    @Override
    public Boolean convertToDatabaseColumn(TypeCompanySettlement typeCompanySettlement) {
        if (typeCompanySettlement == null) return false;
        return typeCompanySettlement == TypeCompanySettlement.A;
    }

    @Override
    public TypeCompanySettlement convertToEntityAttribute(Boolean aBoolean) {
        if (aBoolean == null) return null;
        return aBoolean ? TypeCompanySettlement.A : TypeCompanySettlement.N;
    }
}
