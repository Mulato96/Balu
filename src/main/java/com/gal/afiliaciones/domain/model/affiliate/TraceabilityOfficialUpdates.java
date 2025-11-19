package com.gal.afiliaciones.domain.model.affiliate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="traceability_official_updates")
public class TraceabilityOfficialUpdates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_trace")
    private Long idTrace;

    @Column(name = "update_by", nullable = false, length = 255)
    private String updateBy;

    @Column(name = "modify_type", nullable = false, length = 255)
    private String modifyType;

    @Column(name = "id_affiliate", nullable = false)
    private Long idAffiliate;

    @Column(name = "update_date", nullable = false)
    private LocalDate updateDate;

}
