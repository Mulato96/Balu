package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class AffiliationEmployerDomesticServiceIndependentSpecifications {

    private static final String STAGE_MANAGEMENT = "stageManagement";
    private static final String IDENTIFICATION_NUMBER = "identificationDocumentNumber";
    private static final String IDENTIFICATION_TYPE = "identificationDocumentType";
    private static final String FILED_NUMBER = "filedNumber";

    private AffiliationEmployerDomesticServiceIndependentSpecifications() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<Affiliation> hasFindStageManagement(String stageManagement) {
        return (root, query, criteriaBuilder) -> {
            Predicate documentTypePredicate = criteriaBuilder.equal(root.get(STAGE_MANAGEMENT), stageManagement);
            return criteriaBuilder.and(documentTypePredicate);
        };
    }

    public static Specification<Affiliation> hasDocumentTypeNumber(String number, String type){

        return (root, query, criteriaBuilder) -> {

            Predicate documentNumberPredicate = criteriaBuilder.equal(root.get(IDENTIFICATION_NUMBER), number);
            Predicate documentTypePredicate = criteriaBuilder.equal(root.get(IDENTIFICATION_TYPE), type);
            Predicate affiliationAccept = criteriaBuilder.notEqual(root.get(STAGE_MANAGEMENT), Constant.ACCEPT_AFFILIATION);
            return criteriaBuilder.and(documentNumberPredicate, documentTypePredicate, affiliationAccept);
        };
    }

    public static Specification<Affiliation> hasFieldNumber(String fieldNumber){
        return (root, query, criteriaBuilder) -> {
            Predicate documentNumberPredicate = criteriaBuilder.equal(root.get(FILED_NUMBER), fieldNumber);
            return criteriaBuilder.and(documentNumberPredicate);
        };
    }

    public static Specification<Affiliation> hasFindById(Long id){

        return (root, query, criteriaBuilder) -> {
            Predicate findByIdPredicate = criteriaBuilder.equal(root.get("id"), id);
            return criteriaBuilder.and(findByIdPredicate);
        };
    }

    public static Specification<Affiliation> hasFilter(String search){

        return (root, query, criteriaBuilder) -> {
            Predicate fieldPredicate = criteriaBuilder.like(root.get(FILED_NUMBER), "%" + search + "%");
            Predicate dateRequestPredicate = criteriaBuilder.like(root.get("dateRequest"), "%" + search + "%");
            Predicate numberDocumentPredicate = criteriaBuilder.like(root.get(IDENTIFICATION_NUMBER), "%" + search + "%");
            Predicate nameOrSocialReasonPredicate = criteriaBuilder.like(root.get("firstName"), "%" + search + "%");
            Predicate typeAffiliationPredicate = criteriaBuilder.like(root.get("typeAffiliation"), "%" + search + "%");
            Predicate stageManagementPredicate = criteriaBuilder.like(root.get(STAGE_MANAGEMENT), "%" + search + "%");
            Predicate searchPredicate = criteriaBuilder
                    .or(fieldPredicate, dateRequestPredicate, numberDocumentPredicate, nameOrSocialReasonPredicate, typeAffiliationPredicate, stageManagementPredicate);

            Predicate affiliationAccept = criteriaBuilder.notEqual(root.get(STAGE_MANAGEMENT), Constant.ACCEPT_AFFILIATION);


            return criteriaBuilder.and(searchPredicate, affiliationAccept);
        };
    }

    public static Specification<Affiliation> hasEmployer(String number, String type){
        return (root, query, criteriaBuilder) -> {
            Predicate documentNumberPredicate = criteriaBuilder.equal(root.get(IDENTIFICATION_NUMBER), number);
            Predicate documentTypePredicate = criteriaBuilder.equal(root.get(IDENTIFICATION_TYPE), type);
            Predicate affiliationCompleted = criteriaBuilder.equal(root.get(STAGE_MANAGEMENT), Constant.ACCEPT_AFFILIATION);
            return criteriaBuilder.and(documentNumberPredicate, documentTypePredicate, affiliationCompleted);
        };
    }

    public static Specification<Affiliation> hasFiledNumber(String filedNumber){
        return (root, query, criteriaBuilder) -> {
            Predicate findByFiledNumberPredicate = criteriaBuilder.equal(root.get(FILED_NUMBER), filedNumber);
            return criteriaBuilder.and(findByFiledNumberPredicate);
        };
    }

    public static Specification<Affiliation> findOnlyByDocumentTypeAndNumber(String number, String type){

        return (root, query, criteriaBuilder) -> {

            Predicate documentNumberPredicate = criteriaBuilder.equal(root.get(IDENTIFICATION_NUMBER), number);
            Predicate documentTypePredicate = criteriaBuilder.equal(root.get(IDENTIFICATION_TYPE), type);
            return criteriaBuilder.and(documentNumberPredicate, documentTypePredicate);
        };
    }

    public static Specification<Affiliation> findByNullFiledNumber(){
        return((root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get(FILED_NUMBER)));
    }

    public static Specification<Affiliation> regularizationNotCompleted(LocalDateTime limitRegularization) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.lessThan(root.get("dateRegularization"), limitRegularization),
                criteriaBuilder.equal(root.get(STAGE_MANAGEMENT), Constant.REGULARIZATION)
        );
    }

}
