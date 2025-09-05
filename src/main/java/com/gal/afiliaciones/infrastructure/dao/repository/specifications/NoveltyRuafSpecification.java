package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.noveltyruaf.NoveltyRuaf;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class NoveltyRuafSpecification {

    private NoveltyRuafSpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<NoveltyRuaf> findNoveltyDuplicated(String identificationType,
                                                                   String identificationNumber,
                                                                   String noveltyCode,
                                                                   String identificationTypeContributor,
                                                                   String identificationNumberContributor){
        return (root, query, criteriaBuilder) -> {
            Predicate identificationTypeWorkerPredicate =
                    criteriaBuilder.equal(root.get("identificationType"), identificationType);
            Predicate identificationNumberWorkerPredicate =
                    criteriaBuilder.equal(root.get("identificationNumber"), identificationNumber);
            Predicate noveltyTypeWorkerPredicate =
                    criteriaBuilder.equal(root.get("noveltyCode"), noveltyCode);
            Predicate identificationTypeEmployerPredicate =
                    criteriaBuilder.equal(root.get("identificationTypeContributor"), identificationTypeContributor);
            Predicate identificationNumberEmployerPredicate =
                    criteriaBuilder.equal(root.get("identificationNumberContributor"), identificationNumberContributor);

            return criteriaBuilder.and(identificationTypeWorkerPredicate, identificationNumberWorkerPredicate,
                    noveltyTypeWorkerPredicate, identificationTypeEmployerPredicate,
                    identificationNumberEmployerPredicate);
        };
    }

}
