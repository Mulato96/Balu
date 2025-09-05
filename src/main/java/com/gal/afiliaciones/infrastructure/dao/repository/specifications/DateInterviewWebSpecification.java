package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.DateInterviewWeb;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;

public class DateInterviewWebSpecification {

    private DateInterviewWebSpecification() {
        throw new IllegalStateException("Utility class");
    }

    private static final String ID_OFFICIAL = "idOfficial";

    public static Specification<DateInterviewWeb> findByAffiliation(String idAffiliation){
        return( (root, query, criteriaBuilder) -> {

            Predicate idAffiliationPredicate = criteriaBuilder.equal(root.get("idAffiliate"), idAffiliation);
            return criteriaBuilder.and(idAffiliationPredicate);
        });
    }

    public static Specification<DateInterviewWeb> findByOfficial(Long idOfficial){
        return ((root, query, criteriaBuilder) -> {

            Predicate idOfficialPredicate = criteriaBuilder.equal(root.get(ID_OFFICIAL), idOfficial);
            return criteriaBuilder.and(idOfficialPredicate);
        });
    }

    public static Specification<DateInterviewWeb> findByOfficialAndIdAffiliate(Long idOfficial, String idAffiliate){
        return ((root, query, criteriaBuilder) -> {

            Predicate idOfficialPredicate = criteriaBuilder.equal(root.get(ID_OFFICIAL), idOfficial);
            Predicate idAffiliatePredicate = criteriaBuilder.equal(root.get("idAffiliate"), idAffiliate);
            return criteriaBuilder.and(idOfficialPredicate, idAffiliatePredicate);
        });
    }

    public static Specification<DateInterviewWeb> findByOfficialAndDay(Long idOfficial, LocalDate day){
        return ((root, query, criteriaBuilder) -> {

            Predicate idOfficialPredicate = criteriaBuilder.equal(root.get(ID_OFFICIAL), idOfficial);
            Predicate dayPredicate = criteriaBuilder.equal(root.get("day"), day);
            return criteriaBuilder.and(idOfficialPredicate, dayPredicate);
        });
    }

    public static Specification<DateInterviewWeb> findByHourStart(LocalTime hourStart, LocalDate day){
        return ((root, query, criteriaBuilder) ->{
            Predicate dateStartPredicate = criteriaBuilder.equal(root.get("hourStart"), hourStart);
            Predicate dayPredicate = criteriaBuilder.equal(root.get("day"), day);
            return criteriaBuilder.and(dateStartPredicate, dayPredicate);
        });
    }

    public static Specification<DateInterviewWeb> findByDay(LocalDate day){
        return ((root, query, criteriaBuilder) -> {
            Predicate dayPredicate = criteriaBuilder.equal(root.get("day"), day);
            return criteriaBuilder.and(dayPredicate);
        });

    }
}
