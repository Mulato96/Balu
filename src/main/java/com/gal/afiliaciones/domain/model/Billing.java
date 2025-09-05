package com.gal.afiliaciones.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "facturacion")
public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con la entidad Policy (Póliza)
    @ManyToOne
    @JoinColumn(name = "poliza_id")
    private Policy policy;

    // Sucursal relacionada
    @Column(name = "sucursal")
    private String branch;

    // Ramo de seguros
    @Column(name = "ramo")
    private String insuranceBranch;

    // Fecha de vigencia desde la facturación
    @Column(name = "vigencia_desde_facturacion")
    private LocalDate billingEffectiveDateFrom;

    // Fecha de vigencia hasta la facturación
    @Column(name = "vigencia_hasta_facturacion")
    private LocalDate billingEffectiveDateTo;

    // Tipo de identificación del aportante
    @Column(name = "tipo_identificacion_aportante")
    private String contributorType;

    // Número de identificación del aportante
    @Column(name = "numero_identificacion_aportante")
    private String contributorId;

    // Salario del cotizante (o base de cotización)
    @Column(name = "salario")
    private BigDecimal salary;

    // Tarifa de riesgo para la facturación
    @Column(name = "tarifa_riesgo")
    private BigDecimal riskRate;

    // Número de días de facturación
    @Column(name = "dias_facturacion")
    private Integer billingDays;

    // Valor de la facturación calculada
    @Column(name = "valor_facturacion")
    private BigDecimal billingAmount;

    //Periodo de pago para calculo de fecha de pago
    @Column(name = "periodo_pago")
    private String paymentPeriod;

    //Cantidad de cotizantes
    @Column(name = "cotizantes_liquidados")
    private Integer liquidatedContributors;
}