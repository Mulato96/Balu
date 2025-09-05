package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.AffiliationCancellationTimer;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class AffiliationCancellationTimerSpecifications {

    private AffiliationCancellationTimerSpecifications() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<AffiliationCancellationTimer> findByIdAffiliation(String idAffiliation, String typeDocument, char type){
        return (root, query, criteriaBuilder) ->{
            Predicate numberDocumentPredicate = criteriaBuilder.equal(root.get("numberDocument"), idAffiliation);
            Predicate typeDocumentPredicate = criteriaBuilder.equal(root.get("typeDocument"), typeDocument);
            Predicate typePredicate = criteriaBuilder.equal(root.get("type"), type);
            return criteriaBuilder.and(numberDocumentPredicate, typeDocumentPredicate, typePredicate);
        };
    }
}
