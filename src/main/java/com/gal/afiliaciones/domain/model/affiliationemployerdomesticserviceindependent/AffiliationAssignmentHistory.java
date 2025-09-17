package com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent;

import com.gal.afiliaciones.domain.model.UserMain;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "affiliation_assignment_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AffiliationAssignmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affiliation_id", nullable = false)
    private Affiliation affiliation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UserMain usuario;

    @Column(name = "assignment_date")
    private LocalDateTime assignmentDate;

    @Column(name = "is_current")
    private Boolean isCurrent;
}
