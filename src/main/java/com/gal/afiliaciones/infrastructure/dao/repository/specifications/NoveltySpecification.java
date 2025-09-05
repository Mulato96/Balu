package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.affiliate.RequestChannel;
import com.gal.afiliaciones.domain.model.novelty.NoveltyStatus;
import com.gal.afiliaciones.domain.model.novelty.PermanentNovelty;
import com.gal.afiliaciones.domain.model.novelty.TypeOfUpdate;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class NoveltySpecification {

    private NoveltySpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<PermanentNovelty> findByIdentificationDocumentType(String identificationDocumentType){
        return (root, query, criteriaBuilder) -> {
            if (identificationDocumentType==null || identificationDocumentType.isBlank())
                return null;

            Predicate contributorIdentificationType = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("contributorIdentificationType")), "%" + identificationDocumentType.toLowerCase() + "%");
            Predicate contributantIdentificationType = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("contributantIdentificationType")), "%" + identificationDocumentType.toLowerCase() + "%");
            return criteriaBuilder.or(contributorIdentificationType, contributantIdentificationType);
        };
    }

    public static Specification<PermanentNovelty> findByIdentificationDocumentNumber(String identificationDocumentNumber){
        return (root, query, criteriaBuilder) -> {
            if (identificationDocumentNumber==null || identificationDocumentNumber.isBlank())
                return null;

            Predicate contributorIdentification = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("contributorIdentification")), "%" + identificationDocumentNumber.toLowerCase() + "%");
            Predicate contributantIdentification = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("contributantIdentification")), "%" + identificationDocumentNumber.toLowerCase() + "%");
            return criteriaBuilder.or(contributorIdentification, contributantIdentification);
        };

    }

    public static Specification<PermanentNovelty> findByNoveltyType(TypeOfUpdate noveltyType){
        return (root, query, criteriaBuilder) ->
                noveltyType == null ? null : criteriaBuilder.equal(root.get("noveltyType"), noveltyType);
    }

    public static Specification<PermanentNovelty> findByNoveltyStatus(NoveltyStatus noveltyStatus){
        return (root, query, criteriaBuilder) ->
                noveltyStatus == null ? null : criteriaBuilder.equal(root.get("status"), noveltyStatus);
    }

    public static Specification<PermanentNovelty> findByStartDate(LocalDate startDate){
        return (root, query, criteriaBuilder) ->
                startDate == null ? null : criteriaBuilder.greaterThanOrEqualTo(root.get("registryDate"), startDate);
    }

    public static Specification<PermanentNovelty> findByEndDate(LocalDate endDate){
        return (root, query, criteriaBuilder) ->
                endDate == null ? null : criteriaBuilder.lessThanOrEqualTo(root.get("registryDate"), endDate);
    }

    public static Specification<PermanentNovelty> findByRequestChannel(RequestChannel channel){
        return (root, query, criteriaBuilder) ->
                channel == null ? null : criteriaBuilder.equal(root.get("channel"), channel);
    }

}
