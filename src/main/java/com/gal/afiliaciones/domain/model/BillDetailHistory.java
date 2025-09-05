package com.gal.afiliaciones.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "historial_factura_detalle")
public class BillDetailHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // tipo identificacion del cotizante
    @Column(name = "tipo_identificacion_del_cotizante")
    private String identificationType;

    // tipo identificacion del cotizante
    @Column(name = "no_de_identificacion_del_cotizante")
    private String identificationNumber;

    // tipo identificacion del cotizante
    @Column(name = "valor_facturacion")
    private BigDecimal billingAmount;

    // Relación con la entidad Policy (Póliza)
    @ManyToOne
    @JoinColumn(name = "poliza_id")
    private Policy policy;

    // Fecha en la que se movió esta factura al historial
    @Column(name = "fecha_movido_historial")
    private LocalDate movedToHistoryDate;
}