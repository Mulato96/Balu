package com.gal.afiliaciones.infrastructure.dao.repository.affiliationsview;

import com.gal.afiliaciones.domain.model.AffiliationsView;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.AffiliationsFilterDTO;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AffiliationsViewSpecification {

    private static final String STAGE_WEB_INTERVIEW = "entrevista web";
    private static final String STAGE_DOCUMENTAL = "Revisión documental";
    private static final String STAGE_REGULARIZATION = "Regularización";
    private static final String STAGE_SCHEDULING = "Agendamiento";
    private static final String STAGE_SIGNATURE = "firma";
    private static final String STAGE_MANAGEMENT_LABEL = "stageManagement";

    public static Specification<AffiliationsView> filter(AffiliationsFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.in(root.get(STAGE_MANAGEMENT_LABEL))
                    .value(STAGE_WEB_INTERVIEW)
                    .value(STAGE_DOCUMENTAL)
                    .value(STAGE_REGULARIZATION)
                    .value(STAGE_SCHEDULING)
                    .value(STAGE_SIGNATURE);

            if (filter != null && filter.fieldValue() != null && filter.criteria() != null) {
                String value = "%" + filter.fieldValue().toLowerCase() + "%";
                Predicate filterPredicate;

                switch (filter.criteria()) {
                    case 1 -> filterPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("filedNumber")), value);
                    case 2 -> filterPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("numberDocument")), value);
                    case 3 -> filterPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("nameOrSocialReason")), value);
                    case 4 -> filterPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("affiliationType")), value);
                    case 5 -> filterPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get(STAGE_MANAGEMENT_LABEL)), value);
                    default -> throw new IllegalStateException("Unexpected value: " + filter.criteria());
                }

                predicate =  criteriaBuilder.and(predicate, filterPredicate);
            }

            return predicate;
        };
    }

}
