package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.workerdisplacementnotification.WorkerDisplacementNotification;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class WorkerDisplacementNotificationSpecification {

    private WorkerDisplacementNotificationSpecification() { throw new IllegalStateException("Utility class"); }

    private static final String FIELD_STATUS = "status";
    private static final String FIELD_LIFECYCLE_STATUS = "lifecycleStatus";
    private static final String FIELD_ID = "id";
    private static final String FIELD_WORKER_AFFILIATE = "workerAffiliate";
    private static final String FIELD_EMPLOYER_AFFILIATE = "employerAffiliate";
    private static final String FIELD_DOCUMENT_TYPE = "documentType";
    private static final String FIELD_DOCUMENT_NUMBER = "documentNumber";
    private static final String FIELD_NIT_COMPANY = "nitCompany";
    private static final String FIELD_DISPLACEMENT_START_DATE = "displacementStartDate";
    private static final String FIELD_DISPLACEMENT_END_DATE = "displacementEndDate";
    private static final String FIELD_DISPLACEMENT_DEPARTMENT = "displacementDepartment";
    private static final String FIELD_DISPLACEMENT_MUNICIPALITY = "displacementMunicipality";
    private static final String LIFECYCLE_ACTIVE = "ACTIVO";
    private static final String STATUS_CULMINADO = "CULMINADO";
    private static final String STATUS_TERMINADO = "TERMINADO";

    public static Specification<WorkerDisplacementNotification> byWorker(String docType, String docNumber) {
        return (root, query, cb) -> {
            var wa = root.join(FIELD_WORKER_AFFILIATE);
            return cb.and(
                    cb.equal(wa.get(FIELD_DOCUMENT_TYPE), docType),
                    cb.equal(wa.get(FIELD_DOCUMENT_NUMBER), docNumber)
            );
        };
    }

    public static Specification<WorkerDisplacementNotification> byEmployerNit(String employerNit) {
        return (root, query, cb) -> {
            var ea = root.join(FIELD_EMPLOYER_AFFILIATE);
            return cb.equal(ea.get(FIELD_NIT_COMPANY), employerNit);
        };
    }

    public static Specification<WorkerDisplacementNotification> statusEq(String status) {
        return (root, query, cb) -> cb.equal(root.get(FIELD_STATUS), status);
    }

    public static Specification<WorkerDisplacementNotification> active() {
        return (root, query, cb) -> cb.equal(root.get(FIELD_LIFECYCLE_STATUS), LIFECYCLE_ACTIVE);
    }

    // notDeleted removed by business change. Lists now include INACTIVO as well.

    /**
     * Records whose startDate <= date and endDate >= date
     */
    public static Specification<WorkerDisplacementNotification> currentDateWithinRange(LocalDate date) {
        return (root, query, cb) -> cb.and(
                cb.lessThanOrEqualTo(root.get(FIELD_DISPLACEMENT_START_DATE), date),
                cb.greaterThanOrEqualTo(root.get(FIELD_DISPLACEMENT_END_DATE), date)
        );
    }

    /**
     * Records that are not in a final state (not Culminado or Terminado)
     */
    public static Specification<WorkerDisplacementNotification> notFinalized() {
        return (root, query, cb) -> cb.and(
                cb.notEqual(root.get(FIELD_STATUS), STATUS_CULMINADO),
                cb.notEqual(root.get(FIELD_STATUS), STATUS_TERMINADO)
        );
    }

    public static Specification<WorkerDisplacementNotification> excludeId(Long excludeId) {
        if (excludeId == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.notEqual(root.get(FIELD_ID), excludeId);
    }

    // Overlap rule aligned with repository query:
    // start <= :end AND (end IS NULL OR end >= :start)
    public static Specification<WorkerDisplacementNotification> overlapping(LocalDate start, LocalDate end) {
        return (root, query, cb) -> cb.and(
                cb.lessThanOrEqualTo(root.get(FIELD_DISPLACEMENT_START_DATE), end),
                cb.or(
                        cb.isNull(root.get(FIELD_DISPLACEMENT_END_DATE)),
                        cb.greaterThanOrEqualTo(root.get(FIELD_DISPLACEMENT_END_DATE), start)
                )
        );
    }

    public static Specification<WorkerDisplacementNotification> endDateBefore(LocalDate date) {
        return (root, query, cb) -> cb.lessThan(root.get(FIELD_DISPLACEMENT_END_DATE), date);
    }

    // Fetch joins only for non-pageable use cases
    public static Specification<WorkerDisplacementNotification> withFetchJoins() {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType())) {
                root.fetch(FIELD_WORKER_AFFILIATE, JoinType.INNER);
                root.fetch(FIELD_EMPLOYER_AFFILIATE, JoinType.INNER);
                root.fetch(FIELD_DISPLACEMENT_DEPARTMENT, JoinType.LEFT);
                root.fetch(FIELD_DISPLACEMENT_MUNICIPALITY, JoinType.LEFT);
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }
}



