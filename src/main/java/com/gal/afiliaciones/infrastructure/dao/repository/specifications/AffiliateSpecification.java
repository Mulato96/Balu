package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.FiltersWorkerManagementDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class AffiliateSpecification {

    private static final String DOCUMENT_NUMBER_TEXT = "documentNumber";
    private static final String DOCUMENT_TYPE_TEXT = "documentType";
    private static final String AFFILIATION_TYPE_TEXT = "affiliationType";
    private static final String AFFILIATION_SUBTYPE_TEXT = "affiliationSubType";
    private static final String NIT_NUMBER_TEXT = "nitCompany";
    private static final String AFFILIATION_STATUS_TEXT = "affiliationStatus";

    private AffiliateSpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<Affiliate> hasIdTypeAffiliate(Long idTypeAffiliate){

        return (root, query, criteriaBuilder) -> {

            Predicate idTypeAffiliatePredicate = criteriaBuilder.equal(root.get("idTypeAffiliate"), idTypeAffiliate);
            return criteriaBuilder.and(idTypeAffiliatePredicate);

        };
    }

    public static Specification<Affiliate> findByField(String field){
        return (root, query, criteriaBuilder) -> {
            Predicate fieldPredicate = criteriaBuilder.equal(root.get("filedNumber"), field);
            return criteriaBuilder.and(fieldPredicate);
        };
    }

    public static Specification<Affiliate> findByEmployer(String identificationNumber){
        return (root, query, criteriaBuilder) -> {
            Predicate identificationPredicate = criteriaBuilder.equal(root.get(DOCUMENT_NUMBER_TEXT), identificationNumber);
            return criteriaBuilder.and(identificationPredicate);
        };
    }

    public static Specification<Affiliate> findByEmployerByNumberDocumentAndSubType(String identificationNumber, String subType){
        return (root, query, criteriaBuilder) -> {
            Predicate identificationPredicate = criteriaBuilder.equal(root.get(DOCUMENT_NUMBER_TEXT), identificationNumber);
            Predicate dependentPredicate = criteriaBuilder.equal(root.get(AFFILIATION_SUBTYPE_TEXT), subType);
            return criteriaBuilder.and(identificationPredicate, dependentPredicate);
        };
    }

    public static Specification<Affiliate> findDependentsByEmployer(String identificationNumber){
        return (root, query, criteriaBuilder) -> {
            Predicate identificationPredicate = criteriaBuilder.equal(root.get(NIT_NUMBER_TEXT), identificationNumber);
            Predicate dependentPredicate = criteriaBuilder.equal(root.get(AFFILIATION_TYPE_TEXT), Constant.TYPE_AFFILLATE_DEPENDENT);
            return criteriaBuilder.and(identificationPredicate, dependentPredicate);
        };
    }

    public static Specification<Affiliate> findDomesticEmployerByLegalRepresentative(String identificationNumber){
        return (root, query, criteriaBuilder) -> {
            Predicate identificationPredicate = criteriaBuilder.equal(root.get(DOCUMENT_NUMBER_TEXT), identificationNumber);
            Predicate employerPredicate = criteriaBuilder.equal(root.get(AFFILIATION_TYPE_TEXT), Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
            return criteriaBuilder.and(identificationPredicate, employerPredicate);
        };
    }

    public static Specification<Affiliate> findMercantileByLegalRepresentative(String identificationNumber){
        return (root, query, criteriaBuilder) -> {
            Predicate identificationPredicate = criteriaBuilder.equal(root.get(DOCUMENT_NUMBER_TEXT), identificationNumber);
            Predicate employerPredicate = criteriaBuilder.equal(root.get(AFFILIATION_TYPE_TEXT), Constant.TYPE_AFFILLATE_EMPLOYER);
            return criteriaBuilder.and(identificationPredicate, employerPredicate);
        };
    }

    public static Specification<Affiliate> findByIdentificationTypeAndNumber(String identificationType, String identificationNumber){
        return (root, query, criteriaBuilder) -> {
            Predicate identificationTypePredicate = criteriaBuilder.equal(root.get(DOCUMENT_TYPE_TEXT), identificationType);
            Predicate identificationPredicate = criteriaBuilder.equal(root.get(DOCUMENT_NUMBER_TEXT), identificationNumber);
            return criteriaBuilder.and(identificationTypePredicate, identificationPredicate);
        };
    }

    public static Specification<Affiliate> findByNit(String nit){
        return (root, query, criteriaBuilder) -> {

            Predicate nitCompanyPredicate = criteriaBuilder.equal(root.get(NIT_NUMBER_TEXT), nit);
            Predicate employerPredicate = criteriaBuilder.equal(root.get(AFFILIATION_TYPE_TEXT), Constant.TYPE_AFFILLATE_EMPLOYER);
            return criteriaBuilder.and(nitCompanyPredicate, employerPredicate);
        };
    }

    public static Specification<Affiliate> findByEmployerAndWorker(String nitEmployer, String identificationType, String identificationNumber){
        return (root, query, criteriaBuilder) -> {
            Predicate employerPredicate = criteriaBuilder.equal(root.get(NIT_NUMBER_TEXT), nitEmployer);
            Predicate identificationTypePredicate = criteriaBuilder.equal(root.get(DOCUMENT_TYPE_TEXT), identificationType);
            Predicate identificationPredicate = criteriaBuilder.equal(root.get(DOCUMENT_NUMBER_TEXT), identificationNumber);
            return criteriaBuilder.and(employerPredicate, identificationTypePredicate, identificationPredicate);
        };
    }

    public static Specification<Affiliate> findByNitEmployer(String nit){
        return (root, query, criteriaBuilder) -> {
            Predicate nitCompanyPredicate = criteriaBuilder.equal(root.get(NIT_NUMBER_TEXT), nit);
            Predicate employerPredicate = criteriaBuilder.like(root.get(AFFILIATION_TYPE_TEXT), "%" + Constant.TYPE_AFFILLATE_EMPLOYER + "%");
            return criteriaBuilder.and(nitCompanyPredicate, employerPredicate);
        };
    }

    public static Specification<Affiliate> findByEmployerAndIdentification(String identificationType, String identificationNumber){
        return (root, query, criteriaBuilder) -> {
            Predicate identificationTypePredicate = criteriaBuilder.equal(root.get(DOCUMENT_TYPE_TEXT), identificationType);
            Predicate identificationPredicate = criteriaBuilder.equal(root.get(DOCUMENT_NUMBER_TEXT), identificationNumber);
            Predicate employerPredicate = criteriaBuilder.like(root.get(AFFILIATION_TYPE_TEXT), "%" + Constant.TYPE_AFFILLATE_EMPLOYER + "%");
            return criteriaBuilder.and(identificationTypePredicate, identificationPredicate, employerPredicate);
        };
    }

    public static Specification<Affiliate> findByIndependentEmployer(String identificationType, String identificationNumber,
                                                                     String employerName){
        return (root, query, criteriaBuilder) -> {
            Predicate identificationTypePredicate = criteriaBuilder.equal(root.get(DOCUMENT_TYPE_TEXT), identificationType);
            Predicate identificationNumberPredicate = criteriaBuilder.equal(root.get(DOCUMENT_NUMBER_TEXT), identificationNumber);
            Predicate affiliationTypePredicate = criteriaBuilder.like(root.get(AFFILIATION_TYPE_TEXT), "%" + Constant.TYPE_AFFILLATE_INDEPENDENT + "%");

            if(employerName!=null) {
                Predicate employerPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("company")), "%" + employerName.toLowerCase() + "%");
                return criteriaBuilder.and(identificationTypePredicate, identificationNumberPredicate,
                        affiliationTypePredicate, employerPredicate);
            }

            return criteriaBuilder.and(identificationTypePredicate, identificationNumberPredicate, affiliationTypePredicate);
        };
    }

    public static Specification<Affiliate> findByIdentificationEmployer(String identificationType, String identificationNumber){
        return (root, query, criteriaBuilder) -> {
            Predicate identificationTypePredicate = criteriaBuilder.equal(root.get(DOCUMENT_TYPE_TEXT), identificationType);
            Predicate identificationPredicate = criteriaBuilder.equal(root.get(DOCUMENT_NUMBER_TEXT), identificationNumber);
            Predicate affiliationTypeWorker = criteriaBuilder.like(root.get(AFFILIATION_TYPE_TEXT), "%" + Constant.EMPLOYEE + "%");
            return criteriaBuilder.and(identificationTypePredicate, identificationPredicate, affiliationTypeWorker);
        };
    }

    public static Specification<Affiliate> hasActiveStatusAndEmployer(FiltersWorkerManagementDTO filter) {
        return (root, query, cb) -> {
            // Evitar duplicados si hay múltiples dependents
            query.distinct(true);

            // Join con AffiliationDependent
            Join<Affiliate, AffiliationDependent> depJoin = root.join("dependents", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getIdAffiliateEmployer() != null) {
                predicates.add(cb.equal(depJoin.get("idAffiliateEmployer"), filter.getIdAffiliateEmployer()));
            }

            if (filter.getStartContractDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(depJoin.get("coverageDate"), filter.getStartContractDate()));
            }

            if (filter.getEndContractDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(depJoin.get("coverageDate"), filter.getEndContractDate()));
            }

            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get(AFFILIATION_STATUS_TEXT), filter.getStatus()));
            }

            if (filter.getIdentificationDocumentType() != null) {
                predicates.add(cb.equal(depJoin.get("identificationDocumentType"), filter.getIdentificationDocumentType()));
            }

            if (filter.getIdentificationDocumentNumber() != null) {
                predicates.add(cb.equal(depJoin.get("identificationDocumentNumber"), filter.getIdentificationDocumentNumber()));
            }

            if (filter.getIdbondingType() != null) {
                predicates.add(cb.equal(depJoin.get("idBondingType"), filter.getIdbondingType()));
            }

            if (filter.getUpdateRequired() != null) {
                predicates.add(cb.equal(depJoin.get("pendingCompleteFormPila"), filter.getUpdateRequired()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
