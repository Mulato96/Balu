package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import org.springframework.data.jpa.domain.Specification;

public final class AffiliationDetailSpecifications {

    private AffiliationDetailSpecifications() {
    }

    public static Specification<Affiliation> hasDocument(String documentType, String documentNumber) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("identificationDocumentType"), documentType),
                cb.equal(root.get("identificationDocumentNumber"), documentNumber)
        );
    }

    public static Specification<Affiliation> hasFiledNumber(String filedNumber) {
        return (root, query, cb) -> cb.equal(root.get("filedNumber"), filedNumber);
    }

    public static Specification<Affiliation> hasEconomicActivityCode(String code) {
        return (root, query, cb) -> cb.equal(root.get("codeMainEconomicActivity"), code);
    }
}


