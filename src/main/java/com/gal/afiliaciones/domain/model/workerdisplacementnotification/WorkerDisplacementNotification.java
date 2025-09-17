package com.gal.afiliaciones.domain.model.workerdisplacementnotification;

import com.gal.afiliaciones.domain.model.Department;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "worker_displacement_notification")
public class WorkerDisplacementNotification {

    private static final String BUSINESS_STATUS_EN_CURSO = "EN_CURSO";
    private static final String BUSINESS_STATUS_CULMINADO = "CULMINADO";
    private static final String BUSINESS_STATUS_TERMINADO = "TERMINADO";
    private static final String BUSINESS_STATUS_REGISTRADO = "REGISTRADO";
    private static final String BUSINESS_STATUS_INACTIVO = "INACTIVO";

    private static final String LIFECYCLE_ACTIVE = "ACTIVO";
    private static final String LIFECYCLE_DELETED = "INACTIVO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filed_number")
    private String filedNumber;

    // ===== RELATIONSHIPS (No Data Duplication) =====
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_affiliate_id", nullable = false)
    private Affiliate workerAffiliate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_affiliate_id", nullable = false)
    private Affiliate employerAffiliate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "displacement_department_id")
    private Department displacementDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "displacement_municipality_id")
    private Municipality displacementMunicipality;

    // ===== DISPLACEMENT-SPECIFIC DATA =====
    
    @Column(name = "displacement_start_date", nullable = false)
    private LocalDate displacementStartDate;

    @Column(name = "displacement_end_date", nullable = false)
    private LocalDate displacementEndDate;

    @Column(name = "displacement_reason", nullable = false, columnDefinition = "TEXT")
    private String displacementReason;

    @Column(name = "status")
    private String status;

    // ===== NEW LIFECYCLE/BUSINESS-AUDIT FIELDS =====

    /**
     * Lifecycle status for soft delete/visibility control.
     * Should be 'ACTIVE' or 'DELETED'.
     */
    @Column(name = "lifecycle_status")
    @Builder.Default
    private String lifecycleStatus = LIFECYCLE_ACTIVE;

    /**
     * Captures the initially provided end date to determine manual early termination later.
     */
    @Column(name = "initial_end_date")
    private LocalDate initialEndDate;

    /**
     * Audit of when the displacement was finalized (auto or manual).
     */
    @Column(name = "finalized_at")
    private LocalDateTime finalizedAt;

    /**
     * Audit of who finalized the displacement.
     */
    @Column(name = "finalized_by_user_id")
    private Long finalizedByUserId;

    // ===== AUDIT FIELDS =====
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "updated_by_user_id")
    private Long updatedByUserId;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        if (this.lifecycleStatus == null) {
            this.lifecycleStatus = LIFECYCLE_ACTIVE;
        }
        if (this.initialEndDate == null) {
            this.initialEndDate = this.displacementEndDate;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    // ===== BUSINESS METHODS =====

    /**
     * Check if displacement can be edited based on business rules
     * Requirements: Can only edit if end date > current date and status is ACTIVO
     */
    public boolean canBeEdited() {
        return isEditable(LocalDate.now());
    }

    /**
     * Check if displacement can be inactivated
     * Requirements: Can only inactivate if status is ACTIVO
     */
    public boolean canBeInactivated() {
        return isDeletable(LocalDate.now());
    }

    /**
     * Inactivate the displacement
     */
    public void inactivate() {
        softDelete(null);
    }

    /**
     * Check if displacement should be automatically inactivated
     * Requirements: Auto-inactivate when end date is reached
     */
    public boolean shouldBeAutoInactivated() {
        return LIFECYCLE_ACTIVE.equals(this.lifecycleStatus) &&
               this.displacementEndDate != null &&
               !this.displacementEndDate.isAfter(LocalDate.now());
    }

    // ===== NEW BUSINESS HELPERS (NON-BREAKING ADDITIONS) =====

    /**
     * Determine whether the record can be edited under new rules:
     * - lifecycleStatus must be ACTIVE
     * - end date must not be greater than today (<= today)
     * - business status must not be CULMINADO (view-only)
     */
    public boolean isEditable(LocalDate today) {
        if (today == null) {
            today = LocalDate.now();
        }
        if (!LIFECYCLE_ACTIVE.equals(this.lifecycleStatus)) {
            return false;
        }
        String business = deriveBusinessStatus(today);
        return !(BUSINESS_STATUS_CULMINADO.equals(business) || BUSINESS_STATUS_TERMINADO.equals(business));
    }

    /**
     * Determine whether the record can be deleted under new rules (soft delete):
     * Same conditions as edit under current requirements.
     */
    public boolean isDeletable(LocalDate today) {
        return isEditable(today);
    }

    /**
     * Soft delete via lifecycle flag, instead of overloading business status.
     */
    public void softDelete(Long userId) {
        this.lifecycleStatus = LIFECYCLE_DELETED;
        this.status = BUSINESS_STATUS_INACTIVO;
        this.updatedByUserId = userId;
        this.updatedDate = LocalDateTime.now();
    }

    /**
     * Mark the displacement as auto-finalized (Culminado) when end date is reached.
     */
    public void markAsAutoFinalized(LocalDateTime when, Long userId) {
        if (when == null) {
            when = LocalDateTime.now();
        }
        this.status = BUSINESS_STATUS_CULMINADO;
        this.finalizedAt = when;
        this.finalizedByUserId = userId;
        this.updatedDate = when;
        this.updatedByUserId = userId;
    }

    /**
     * Mark as manually terminated (Terminado) when end date is edited to be earlier than initial.
     */
    public void markAsEarlyTerminated(LocalDate newEndDate, Long userId) {
        this.displacementEndDate = newEndDate;
        this.status = BUSINESS_STATUS_TERMINADO;
        this.finalizedAt = LocalDateTime.now();
        this.finalizedByUserId = userId;
        this.updatedDate = LocalDateTime.now();
        this.updatedByUserId = userId;
    }

    /**
     * Business status derivation for presentation:
     * - If status is CULMINADO or TERMINADO, return it.
     * - Else if today in [start, end], return EN_CURSO.
     * - Else return current status value (for backward compatibility), or null.
     */
    public String deriveBusinessStatus(LocalDate today) {
        if (today == null) {
            today = LocalDate.now();
        }
        // If lifecycle is deleted or status explicitly set to INACTIVO, return INACTIVO
        if (BUSINESS_STATUS_INACTIVO.equals(this.status) || LIFECYCLE_DELETED.equals(this.lifecycleStatus)) {
            return BUSINESS_STATUS_INACTIVO;
        }
        if (BUSINESS_STATUS_CULMINADO.equals(this.status) || BUSINESS_STATUS_TERMINADO.equals(this.status)) {
            return this.status;
        }
        if (this.displacementStartDate != null && this.displacementEndDate != null) {
            boolean startsInFuture = this.displacementStartDate.isAfter(today);
            boolean startsOnOrBefore = !this.displacementStartDate.isAfter(today);
            boolean endsOnOrAfter = !this.displacementEndDate.isBefore(today);
            if (startsInFuture) {
                return BUSINESS_STATUS_REGISTRADO;
            }
            if (startsOnOrBefore && endsOnOrAfter) {
                return BUSINESS_STATUS_EN_CURSO;
            }
        }
        return this.status;
    }
}
