package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.affiliate.Certificate;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class CertificateSpecification {

    private CertificateSpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<Certificate> findByFieldNumber(String field){
        return (root, query, criteriaBuilder) -> {
            Predicate fieldPredicate = criteriaBuilder.equal(root.get("filedNumber"), field);
            return criteriaBuilder.and(fieldPredicate);
        };
    }

}
