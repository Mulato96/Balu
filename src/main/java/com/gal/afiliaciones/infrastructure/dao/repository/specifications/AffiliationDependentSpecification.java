package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class AffiliationDependentSpecification {

    private AffiliationDependentSpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<AffiliationDependent> findByFieldNumber(String fieldNumber){
        return (root, query, criteriaBuilder) ->{

            Predicate numberDocumentPredicate = criteriaBuilder.equal(root.get("filedNumber"), fieldNumber);
            return criteriaBuilder.and(numberDocumentPredicate);
        };
    }

    public static Specification<AffiliationDependent> findByTypeAndNumberDocument(String type, String number){
        return (root, query, criteriaBuilder) -> {

            Predicate typeDocumenPredicate = criteriaBuilder.equal(root.get("identificationDocumentType"), type);
            Predicate numberDocumentPredicate = criteriaBuilder.equal(root.get("identificationDocumentNumber"), number);
            return criteriaBuilder.and(typeDocumenPredicate, numberDocumentPredicate);

        };
    }

    public static Specification<AffiliationDependent> findByEconomicActivityAndEmployer(List<String> filedNumbers){
        return (root, query, criteriaBuilder) -> root.get("filedNumber").in(filedNumbers);
    }

}
