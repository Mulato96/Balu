package com.gal.afiliaciones.infrastructure.dao.repository.specifications;


import com.gal.afiliaciones.domain.model.Notes;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class NotesSpecification {

    private NotesSpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<Notes> findByAffiliation(String filedNumber){
        return ((root, query, criteriaBuilder) -> {

            Predicate filedNumberPredicate = criteriaBuilder.equal(root.get("filedNumberAffiliation"), filedNumber);
            return criteriaBuilder.and(filedNumberPredicate);
        });
    }


}
