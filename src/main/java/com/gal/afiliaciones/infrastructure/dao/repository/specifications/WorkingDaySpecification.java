package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.WorkingDay;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class WorkingDaySpecification {

    private WorkingDaySpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<WorkingDay> hasCode(Long code) {
        return (root, query, criteriaBuilder) -> {
            Predicate codePredicate = criteriaBuilder.equal(root.get("code"), code);
            return criteriaBuilder.and(codePredicate);
        };
    }
}
