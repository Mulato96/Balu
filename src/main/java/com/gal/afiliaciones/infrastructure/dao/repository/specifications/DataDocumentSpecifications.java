package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class DataDocumentSpecifications {

    private DataDocumentSpecifications() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<DataDocumentAffiliate> hasFindByIdAffiliation(Long idAffiliation) {
        return (root, query, criteriaBuilder) -> {
            Predicate idAffiliationPredicate = criteriaBuilder.equal(root.get("idAffiliate"), idAffiliation);
            return criteriaBuilder.and(idAffiliationPredicate);
        };
    }

    public static Specification<DataDocumentAffiliate> hasFindDocumentReject(Long idAffiliation){
        return (root, query, criteriaBuilder) -> {
            Predicate idAffiliationPredicate = criteriaBuilder.equal(root.get("idAffiliate"), idAffiliation);
            Predicate revisedPredicate = criteriaBuilder.equal(root.get("revised"), false);
            Predicate stateDocumentPre = criteriaBuilder.equal(root.get("state"), true);
            Predicate revisedPre = criteriaBuilder.equal(root.get("revised"), true);

            return criteriaBuilder.and(idAffiliationPredicate,  criteriaBuilder.or(revisedPredicate,criteriaBuilder.and(stateDocumentPre, revisedPre)));
        };
    }
}
