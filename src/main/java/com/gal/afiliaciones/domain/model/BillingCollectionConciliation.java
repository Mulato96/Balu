package com.gal.afiliaciones.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.gal.afiliaciones.application.job.KeycloakScheduler.TIME_ZONE;


@Data
@Entity
@Table(name = "factura_conciliacion")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillingCollectionConciliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con la entidad IndividualCollection (recaudo)
    @Column(name = "recaudo_id")
    private Long collection;

    @Column(name = "factura_id")
    private Long billingId;
    @Column(name = "sucursal")
    private String branch;
    @Column(name = "poliza_id")
    private String policyId;
    @Column(name = "tipo_identificacion_aportante")
    private String contributorType;
    @Column(name = "numero_identificacion_aportante")
    private String contributorId;
    @Column(name = "valor_facturacion")
    private BigDecimal billingAmount;

    @Column(name = "estado")
    private String status;
    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(ZoneId.of(TIME_ZONE));
    }

}
