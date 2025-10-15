package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class AffiliationDependentSpecification {

    private static final String FILED_NUMBER = "filedNumber";
    private static final String ID_AFFILIATE_EMPLOYER = "idAffiliateEmployer";

    private AffiliationDependentSpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<AffiliationDependent> findByFieldNumber(String fieldNumber){
        return (root, query, criteriaBuilder) ->{

            Predicate numberDocumentPredicate = criteriaBuilder.equal(root.get(FILED_NUMBER), fieldNumber);
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

    public static Specification<AffiliationDependent> findByFiledNumberList(List<String> filedNumbers){
        return (root, query, criteriaBuilder) -> root.get(FILED_NUMBER).in(filedNumbers);
    }


    public static Specification<AffiliationDependent> hasIdHeadquarter(Long idHeadquarter){
        return (root, query, criteriaBuilder) -> {
            Predicate idHeadquarterPredicate = criteriaBuilder.equal(root.get("idHeadquarter"), idHeadquarter);
            return criteriaBuilder.and(idHeadquarterPredicate);
        };
    }

    public static Specification<AffiliationDependent> findByEconomicActivityAndEmployer(List<String> filedNumbers, String economicActivity){
        return (root, query, criteriaBuilder) -> {
            Predicate filedNumberPredicate = root.get(FILED_NUMBER).in(filedNumbers);
            Predicate economicActivityPredicate = criteriaBuilder.equal(root.get("economicActivityCode"), economicActivity);

            return criteriaBuilder.and(filedNumberPredicate, economicActivityPredicate);
        };
    }


    public static Specification<AffiliationDependent> findDependentsByIdAffiliateEmployer(Long idAffiliateEmployer){
        return (root, query, criteriaBuilder) -> {
            Predicate identificationPredicate = criteriaBuilder.equal(root.get(ID_AFFILIATE_EMPLOYER), idAffiliateEmployer);
            return criteriaBuilder.and(identificationPredicate);
        };
    }

    public static Specification<AffiliationDependent> findByTypeDependentAndEmployer(String type, String number, Long idAffiliateEmployer){
        return (root, query, criteriaBuilder) -> {
            Predicate typeDocumenPredicate = criteriaBuilder.equal(root.get("identificationDocumentType"), type);
            Predicate numberDocumentPredicate = criteriaBuilder.equal(root.get("identificationDocumentNumber"), number);
            Predicate employerPredicate = criteriaBuilder.equal(root.get(ID_AFFILIATE_EMPLOYER), idAffiliateEmployer);
            return criteriaBuilder.and(typeDocumenPredicate, numberDocumentPredicate, employerPredicate);

        };
    }


}
