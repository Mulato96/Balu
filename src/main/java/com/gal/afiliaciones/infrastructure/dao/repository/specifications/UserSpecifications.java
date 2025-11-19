package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.domain.model.Card;
import com.gal.afiliaciones.domain.model.CodeValidCertificate;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.Certificate;
import com.gal.afiliaciones.infrastructure.dto.card.UserCardDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

public class UserSpecifications {

    private static final String USER_TYPE_LABEL = "userType";
    private static final String IDENTIFICATION_TYPE_LABEL = "identificationType";
    private static final String CODE_CIIU = "codeCIIU";
    private static final String ECONOMIC_ACTIVITY_CODE = "economicActivityCode";

    private UserSpecifications() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<UserMain> hasDocumentTypeAndNumber(String identificationType, String identification) {
        return (root, query, criteriaBuilder) -> {
            Predicate documentTypePredicate = criteriaBuilder.equal(root.get(IDENTIFICATION_TYPE_LABEL), identificationType);
            Predicate documentNumberPredicate = criteriaBuilder.equal(root.get(Constant.FIELD_IDENTIFICATION), identification);
            return criteriaBuilder.and(documentTypePredicate, documentNumberPredicate);
        };
    }

    public static Specification<UserMain> byEmail(String email) {
        return (root, query, cb) -> cb.equal(root.get("email"), email);
    }

    public static Specification<UserMain> byIdentification(String identification) {
        return (root, query, cb) -> cb.equal(root.get(Constant.FIELD_IDENTIFICATION), identification);
    }

    public static Specification<UserMain> byPhone1(String phoneNumber) {
        return (root, query, cb) -> cb.equal(root.get("phoneNumber"), phoneNumber);
    }

    public static Specification<UserMain> usersNotCompletedAffiliation(LocalDateTime timeThreshold) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.lessThan(root.get("lastAffiliationAttempt"), timeThreshold),
                criteriaBuilder.isFalse(root.get("statusActive"))
        );
    }

    public static Specification<CodeValidCertificate> codeCertificated(String year) {
        return (root, query, criteriaBuilder) -> {
            Predicate yearPredicate = criteriaBuilder.equal(root.get("startSequence"), year);
            return criteriaBuilder.and(yearPredicate);
        };
    }

    public static Specification<Certificate> findUserCertificate(String numberDocument, String typeDocument){
        return (root, query, criteriaBuilder) -> {
            Predicate numberDocumentPredicate = criteriaBuilder.equal(root.get("numberDocument"), numberDocument);
            Predicate typeDocumentPredicate = criteriaBuilder.equal(root.get("typeDocument"), typeDocument);
            return criteriaBuilder.and(numberDocumentPredicate, typeDocumentPredicate);
        };
    }

    public static Specification<Card> findCard(UserCardDTO userCardDTO){
        return (root, query, criteriaBuilder) -> {
            Predicate typeDocumentWorkerPredicate = criteriaBuilder.equal(root.get("typeDocumentWorker"), userCardDTO.getTypeDocumentWorker());
            Predicate numberDocumentWorkerPredicate = criteriaBuilder.equal(root.get("numberDocumentWorker"), userCardDTO.getNumberDocumentWorker());
            Predicate nameFactoryPredicate = criteriaBuilder.equal(root.get("company"), userCardDTO.getCompany());
            Predicate nitFactoryPredicate = criteriaBuilder.equal(root.get("nitCompany"), userCardDTO.getNitCompany());
            Predicate dateAffiliationPredicate = criteriaBuilder.equal(root.get("dateAffiliation"), userCardDTO.getDateAffiliation());
            Predicate typeAffiliationPredicate = criteriaBuilder.equal(root.get("typeAffiliation"), userCardDTO.getTypeAffiliation());
            return criteriaBuilder.and(typeDocumentWorkerPredicate,numberDocumentWorkerPredicate,nameFactoryPredicate, nitFactoryPredicate, dateAffiliationPredicate, typeAffiliationPredicate);
        };
   }

    public static Specification<Card> findCardById(String id){
        return (root, query, criteriaBuilder) -> {
            Predicate idPredicate = criteriaBuilder.equal(root.get("id"), id);
            return criteriaBuilder.and(idPredicate);
       };
   }

    public static Specification<Card> findCardByidentification(String numberDocument, String tyoeDocument){
        return (root, query, criteriaBuilder) -> {
            Predicate numberDocumentPredicate = criteriaBuilder.equal(root.get("numberDocumentWorker"),numberDocument);
            Predicate typeDocumentPredicate = criteriaBuilder.equal(root.get("typeDocumentWorker"), tyoeDocument);
            return criteriaBuilder.and(numberDocumentPredicate, typeDocumentPredicate);
        };
   }

    public static Specification<EconomicActivity> findActivityEconomicByCodeCIIU(String codeCIIUDecree) {
        return (root, query, criteriaBuilder) -> 
            codeCIIUDecree != null && (codeCIIUDecree.length() == 7 || codeCIIUDecree.length() == 4) ?
            		criteriaBuilder.or(
            				criteriaBuilder.equal(root.get(codeCIIUDecree.length() == 7 ? UserSpecifications.ECONOMIC_ACTIVITY_CODE : UserSpecifications.CODE_CIIU), codeCIIUDecree),
            				criteriaBuilder.equal(root.get(UserSpecifications.CODE_CIIU), codeCIIUDecree)
                )
            : criteriaBuilder.conjunction();
    }


    public static Specification<EconomicActivity> findActivityEconomicByRiskCodeCIIUCodeAdditional(String risk, String codeCIIU, String codeAdditional){
        return (root, query, criteriaBuilder) -> {
            Predicate riskPredicate = criteriaBuilder.equal(root.get("classRisk"), risk);
            Predicate codeCIIUPredicate = criteriaBuilder.equal(root.get(CODE_CIIU), codeCIIU);
            Predicate codeAdditionalPredicate = criteriaBuilder.equal(root.get("additionalCode"), codeAdditional);
            return criteriaBuilder.and(riskPredicate, codeCIIUPredicate, codeAdditionalPredicate);
        };
    }

    public static Specification<EconomicActivity> findActivityEconomicByDescription(String description) {
        return (root, query, criteriaBuilder) -> {
            if (description == null || description.length() < 4) {
                return criteriaBuilder.conjunction();
            }
            Expression<String> normalizedColumnName = criteriaBuilder.function("unaccent", String.class,
                    criteriaBuilder.lower(criteriaBuilder.function("unaccent", String.class, root.get("description"))));
            String normalizedInput = description.toLowerCase().replaceAll("[^\\p{IsAlphabetic}]", "");
            normalizedInput = StringUtils.stripAccents(normalizedInput);

            return criteriaBuilder.like(normalizedColumnName, "%" + normalizedInput + "%");
        };
    }

    public static Specification<Affiliate> findAffiliateByIdentification(String documentNumber, String documentType){
        return (root, query, criteriaBuilder) -> {
            Predicate numberDocumentPredicate = criteriaBuilder.equal(root.get("documentNumber"),documentNumber);
            Predicate typeDocumentPredicate = criteriaBuilder.equal(root.get("documentType"), documentType);
            return criteriaBuilder.and(typeDocumentPredicate, numberDocumentPredicate);
        };
    }

    public static Specification<Affiliate> hasIdentification(String documentNumber) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("documentNumber"), documentNumber);
    }

    public static Specification<Card> findCardByFiledNumber(String filedNumber){
        return (root, query, criteriaBuilder) -> {
            Predicate idPredicate = criteriaBuilder.equal(root.get("filedNumber"), filedNumber);
            return criteriaBuilder.and(idPredicate);
        };
    }

    public static Specification<UserMain> findExternalUserByDocumentTypeAndNumber(String identificationType, String identification) {
        return (root, query, criteriaBuilder) -> {
            Predicate documentTypePredicate = criteriaBuilder.equal(root.get(IDENTIFICATION_TYPE_LABEL), identificationType);
            Predicate documentNumberPredicate = criteriaBuilder.equal(root.get(Constant.FIELD_IDENTIFICATION), identification);
            Predicate externalPredicate = criteriaBuilder.equal(root.get(USER_TYPE_LABEL), 2L);
            return criteriaBuilder.and(documentTypePredicate, documentNumberPredicate, externalPredicate);
        };
    }

    public static Specification<UserMain> usersPasswordExpired(LocalDateTime timePasswordExpired) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.lessThan(root.get("lastPasswordUpdate"), timePasswordExpired)
        );
    }

    public static Specification<UserMain> findExternalUser(String identificationType, String identification) {
        return (root, query, criteriaBuilder) -> {
            Predicate documentTypePredicate = criteriaBuilder.equal(root.get(IDENTIFICATION_TYPE_LABEL), identificationType);
            Predicate documentNumberPredicate = criteriaBuilder.equal(root.get(Constant.FIELD_IDENTIFICATION), identification);
            Predicate userTypePredicate = criteriaBuilder.equal(root.get(USER_TYPE_LABEL), Constant.EXTERNAL_USER_TYPE);

            return criteriaBuilder.and(documentTypePredicate, documentNumberPredicate, userTypePredicate);
        };
    }

    public static Specification<UserMain> byUsername(String userName) {
        return (root, query, cb) -> cb.equal(root.get("userName"), userName);
    }

    public static Specification<UserMain> findOfficierById(Long idUser){
        return (root, query, criteriaBuilder) -> {
            Predicate idUserPredicate = criteriaBuilder.equal(root.get("id"), idUser);
            Predicate typeUserPredicate = criteriaBuilder.equal(root.get(USER_TYPE_LABEL), Constant.INTERNAL_USER_TYPE);
            return criteriaBuilder.and(idUserPredicate, typeUserPredicate);
        };
    }

}
