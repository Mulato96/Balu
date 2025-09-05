package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.affiliate.DetailRecordMassiveUpdateWorker;
import com.gal.afiliaciones.domain.model.affiliate.RecordMassiveUpdateWorker;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class RecordMassiveUpdateWorkerSpecification {

    private RecordMassiveUpdateWorkerSpecification(){throw new IllegalStateException("Class Utility");}

    public static Specification<DetailRecordMassiveUpdateWorker> findByIdRecordLoadBulk(Long id){
        return ((root, query, criteriaBuilder) -> {

            Predicate idRecordLoadBulkPredicate = criteriaBuilder.equal(root.get("idRecordLoadBulk"), id);

            return criteriaBuilder.and(idRecordLoadBulkPredicate);
        });
    }

    public static Specification<RecordMassiveUpdateWorker> findByIdUser(Long id){
        return ((root, query, criteriaBuilder) -> {

            Predicate idRecordLoadBulkPredicate = criteriaBuilder.equal(root.get("idUserLoad"), id);

            return criteriaBuilder.and(idRecordLoadBulkPredicate);
        });
    }

}
