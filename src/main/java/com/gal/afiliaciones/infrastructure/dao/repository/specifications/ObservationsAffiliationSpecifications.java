package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.ObservationsAffiliation;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class ObservationsAffiliationSpecifications {

    private ObservationsAffiliationSpecifications() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<ObservationsAffiliation> findByNumberFiled(String numberFiled){
        return((root, query, criteriaBuilder) -> {
            Predicate filedNumberPredicate = criteriaBuilder.equal(root.get("filedNumber"), numberFiled);
            return criteriaBuilder.and(filedNumberPredicate);
        });
    }
}
