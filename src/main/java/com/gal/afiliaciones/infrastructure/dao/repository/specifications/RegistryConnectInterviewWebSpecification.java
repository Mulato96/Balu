package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.RegistryConnectInterviewWeb;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class RegistryConnectInterviewWebSpecification {

    private RegistryConnectInterviewWebSpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<RegistryConnectInterviewWeb> findByFiledNumber(String filedNumber){
        return ((root, query, criteriaBuilder) -> {

            Predicate filedNumberPredicate = criteriaBuilder.equal(root.get("numberFiled"), filedNumber);

            return criteriaBuilder.and(filedNumberPredicate);
        });
    }

}
