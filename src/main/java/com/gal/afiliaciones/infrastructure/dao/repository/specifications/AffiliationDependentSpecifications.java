package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import org.springframework.data.jpa.domain.Specification;

public final class AffiliationDependentSpecifications {

    private AffiliationDependentSpecifications() {
    }

    public static Specification<AffiliationDependent> hasDocument(String documentType, String documentNumber) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("identificationDocumentType"), documentType),
                cb.equal(root.get("identificationDocumentNumber"), documentNumber)
        );
    }

    public static Specification<AffiliationDependent> hasFiledNumber(String filedNumber) {
        return (root, query, cb) -> cb.equal(root.get("filedNumber"), filedNumber);
    }
}


