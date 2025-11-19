package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class AffiliateMercantileSpecification {

    private static final String STAGE_MANAGEMENT = "stageManagement";
    private static final String IDENTIFICATION_TYPE = "typeDocumentIdentification";
    private static final String IDENTIFICATION_NUMBER = "numberIdentification";
    private static final String FILED_NUMBER = "filedNumber";

    private AffiliateMercantileSpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<AffiliateMercantile> findByNumberAndTypeDocument(String numberDocument, String typeDocument){
        return (root, query, criteriaBuilder) ->{

            Predicate numberDocumentPredicate = criteriaBuilder.equal(root.get(IDENTIFICATION_TYPE), typeDocument);
            Predicate typeDocumentPredicate = criteriaBuilder.equal(root.get(IDENTIFICATION_NUMBER), numberDocument);

            return criteriaBuilder.and(numberDocumentPredicate, typeDocumentPredicate);
        };
    }

    public static Specification<AffiliateMercantile> findByFieldNumber(String fieldNumber){
        return (root, query, criteriaBuilder) ->{

            Predicate numberDocumentPredicate = criteriaBuilder.equal(root.get(FILED_NUMBER), fieldNumber);
            return criteriaBuilder.and(numberDocumentPredicate);
        };
    }

    public static Specification<AffiliateMercantile> findByActivityEconomic(Long id){
        return (root, query, criteriaBuilder) -> {
            var economicActivityJoin = root.join("economicActivity");
            var activityEconomicJoin = economicActivityJoin.join("activityEconomic");

            return criteriaBuilder.equal(activityEconomicJoin.get("id"), id);
        };
    }

    public static Specification<AffiliateMercantile> findByFiledNumberNull(){
        return((root, query, criteriaBuilder) ->  criteriaBuilder.isNull(root.get(FILED_NUMBER)));
    }

    public static Specification<AffiliateMercantile> findByPersonResponsible(String numberDocument, String typeDocument){
        return (root, query, criteriaBuilder) ->{

            Predicate numberDocumentPredicate = criteriaBuilder.equal(root.get("typeDocumentPersonResponsible"), typeDocument);
            Predicate typeDocumentPredicate = criteriaBuilder.equal(root.get("numberDocumentPersonResponsible"), numberDocument);

            return criteriaBuilder.and(numberDocumentPredicate, typeDocumentPredicate);
        };
    }

    public static Specification<AffiliateMercantile> hasFindStageManagement(String stageManagement) {
        return (root, query, criteriaBuilder) -> {
            Predicate documentTypePredicate = criteriaBuilder.equal(root.get(STAGE_MANAGEMENT), stageManagement);
            return criteriaBuilder.and(documentTypePredicate);
        };
    }

    public static Specification<AffiliateMercantile> regularizationNotCompleted(LocalDateTime limitRegularization) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.lessThan(root.get("dateRegularization"), limitRegularization),
                criteriaBuilder.equal(root.get(STAGE_MANAGEMENT), Constant.REGULARIZATION)
        );
    }

    public static Specification<AffiliateMercantile> employerAffiliationComplete(String numberDocument, String typeDocument){
        return (root, query, criteriaBuilder) ->{

            Predicate numberDocumentPredicate = criteriaBuilder.equal(root.get(IDENTIFICATION_TYPE), typeDocument);
            Predicate typeDocumentPredicate = criteriaBuilder.equal(root.get(IDENTIFICATION_NUMBER), numberDocument);
            Predicate completePredicate = criteriaBuilder.equal(root.get(STAGE_MANAGEMENT), Constant.ACCEPT_AFFILIATION);

            return criteriaBuilder.and(numberDocumentPredicate, typeDocumentPredicate, completePredicate);
        };
    }

    public static Specification<AffiliateMercantile> findByNumberAndTypeDocumentAndDecentralizedConsecutive(
            String numberDocument, String typeDocument, Long descentralizedConsecutive){
        return (root, query, criteriaBuilder) ->{

            Predicate numberDocumentPredicate = criteriaBuilder.equal(root.get(IDENTIFICATION_TYPE), typeDocument);
            Predicate typeDocumentPredicate = criteriaBuilder.equal(root.get(IDENTIFICATION_NUMBER), numberDocument);
            Predicate consecutivePredicate = criteriaBuilder.equal(root.get("decentralizedConsecutive"), descentralizedConsecutive);
            Predicate filedNumberPredicate = criteriaBuilder.isNotNull(root.get(FILED_NUMBER));

            return criteriaBuilder.and(numberDocumentPredicate, typeDocumentPredicate, consecutivePredicate, filedNumberPredicate);
        };
    }

    public static Specification<AffiliateMercantile> findByNumberAndTypeDocumentAndDecentralizedBusinessName(
            String numberDocument, String typeDocument, String businessName){
        return (root, query, criteriaBuilder) ->{

            Predicate numberDocumentPredicate = criteriaBuilder.equal(root.get(IDENTIFICATION_TYPE), typeDocument);
            Predicate typeDocumentPredicate = criteriaBuilder.equal(root.get(IDENTIFICATION_NUMBER), numberDocument);
            Predicate businessNamePredicate = criteriaBuilder.equal(root.get("businessName"), businessName);

            Predicate criteriaAnd = criteriaBuilder.and(numberDocumentPredicate,typeDocumentPredicate);
            return criteriaBuilder.or(criteriaAnd, businessNamePredicate);
        };
    }

}
