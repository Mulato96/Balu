package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.EconomicActivity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class EconomicActivitySpecification {

    private EconomicActivitySpecification(){throw new IllegalStateException("Utility class");}

    public static Specification<EconomicActivity> findEconomicActivity(String classRisk, String codeCIIU, String additionalCode){
        return ((root, query, criteriaBuilder) -> {

            Predicate classRiskPredicate = criteriaBuilder.equal(root.get(""), classRisk);
            Predicate codeCIIUPredicate = criteriaBuilder.equal(root.get(""), codeCIIU);
            Predicate additionalCodePredicate = criteriaBuilder.equal(root.get(""), additionalCode);

            return criteriaBuilder.and(classRiskPredicate, codeCIIUPredicate, additionalCodePredicate);

        });
    }

    public static Specification<EconomicActivity> findByIds(List<Long> ids){
        return (root, query, criteriaBuilder) -> root.get("id").in(ids);
    }
}
