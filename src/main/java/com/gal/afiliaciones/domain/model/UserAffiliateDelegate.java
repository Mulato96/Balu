package com.gal.afiliaciones.domain.model;

import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_affiliate_delegate")
public class UserAffiliateDelegate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "id_affiliate_employer")
    private Long idAffiliateEmployer;

    @Column(name = "delegate_type")
    private String delegateType; // Tipo de delegado: PRINCIPAL, SECUNDARIO, etc.

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "assigned_date")
    private LocalDateTime assignedDate;

    @Column(name = "revoked_date")
    private LocalDateTime revokedDate;

    @Column(name = "assigned_by_user_id")
    private Long assignedByUserId; // ID del usuario que asignó esta delegación

    @Column(name = "revoked_by_user_id")
    private Long revokedByUserId; // ID del usuario que revocó esta delegación

    @Column(name = "observations")
    private String observations;

    @Column(name = "filed_number")
    private String filedNumber;

    @PrePersist
    protected void onCreate() {
        if (assignedDate == null) {
            assignedDate = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
    }

}
