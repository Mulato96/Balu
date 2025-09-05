package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.Policy;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class PolicySpecification {

    private PolicySpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<Policy> findPolicyByAffiliate(String idType, String idNumber, String status,
                                                               Long idPolicyType, Long idAffiliate){
        return (root, query, criteriaBuilder) -> {
            Predicate identificationTypePredicate = criteriaBuilder.equal(root.get("idType"), idType);
            Predicate identificationNumberPredicate = criteriaBuilder.equal(root.get("idNumber"), idNumber);
            Predicate statusPredicate = criteriaBuilder.equal(root.get("status"), status);
            Predicate policyTypePredicate = criteriaBuilder.equal(root.get("idPolicyType"), idPolicyType);
            Predicate affiliatePredicate = criteriaBuilder.equal(root.get("idAffiliate"), idAffiliate);
            return criteriaBuilder.and(identificationTypePredicate, identificationNumberPredicate, statusPredicate,
                    policyTypePredicate, affiliatePredicate);
        };
    }

}
