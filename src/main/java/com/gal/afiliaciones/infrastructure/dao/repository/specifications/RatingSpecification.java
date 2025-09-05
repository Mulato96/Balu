package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.Rating;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class RatingSpecification {

    private RatingSpecification(){throw new IllegalStateException("Class Utility");}

    public static Specification<Rating> findByNumberFiled(String filedNumber){
        return ((root, query, criteriaBuilder) -> {

            Predicate numberFiledPredicate = criteriaBuilder.equal(root.get("filedNumber"), filedNumber);

            return criteriaBuilder.and(numberFiledPredicate);
        });
    }

    public static Specification<Rating> findByIdUser(Long idUser){
        return ((root, query, criteriaBuilder) -> {

            Predicate idUserPredicate = criteriaBuilder.equal(root.get("idUser"), idUser);

            return criteriaBuilder.and(idUserPredicate);

        });
    }
}
