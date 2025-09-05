package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.affiliate.DetailRecordLoadBulk;
import com.gal.afiliaciones.domain.model.affiliate.RecordLoadBulk;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class RecordLoadBulkSpecification {

    private RecordLoadBulkSpecification(){throw new IllegalStateException("Class Utility");}

    public static Specification<DetailRecordLoadBulk> findByIdRecordLoadBulk(Long id){
        return ((root, query, criteriaBuilder) -> {

            Predicate idRecordLoadBulkPredicate = criteriaBuilder.equal(root.get("idRecordLoadBulk"), id);

            return criteriaBuilder.and(idRecordLoadBulkPredicate);
        });
    }

    public static Specification<RecordLoadBulk> findByIdUser(Long id){
        return ((root, query, criteriaBuilder) -> {

            Predicate idRecordLoadBulkPredicate = criteriaBuilder.equal(root.get("idUserLoad"), id);

            return criteriaBuilder.and(idRecordLoadBulkPredicate);
        });
    }
}
