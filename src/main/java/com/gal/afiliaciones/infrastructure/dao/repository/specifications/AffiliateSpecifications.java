package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

public final class AffiliateSpecifications {

    private AffiliateSpecifications() {
    }

    public static Specification<Affiliate> hasDocument(String documentType, String documentNumber) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("documentType"), documentType),
                cb.equal(root.get("documentNumber"), documentNumber)
        );
    }

    public static Specification<Affiliate> hasNitCompany(String nitCompany) {
        return (root, query, cb) -> cb.equal(root.get("nitCompany"), nitCompany);
    }

    public static Specification<Affiliate> hasAffiliationTypeIn(Collection<String> affiliationTypes) {
        return (root, query, cb) -> root.get("affiliationType").in(affiliationTypes);
    }

    public static Specification<Affiliate> hasAffiliationStatus(String affiliationStatus) {
        return (root, query, cb) -> cb.equal(root.get("affiliationStatus"), affiliationStatus);
    }

    public static Specification<Affiliate> hasFiledNumber(String filedNumber) {
        return (root, query, cb) -> cb.equal(root.get("filedNumber"), filedNumber);
    }
}


