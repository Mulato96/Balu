package com.gal.afiliaciones.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "smlmv")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Smlmv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_desde", nullable = false)
    private LocalDateTime fechaDesde;

    @Column(name = "fecha_hasta", nullable = false)
    private LocalDateTime fechaHasta;

    @Column(name = "valor", nullable = false)
    private Integer valor;

    @Column(name = "denominacion", nullable = false)
    private String denominacion;

    @Column(name = "numero_actualizaciones")
    private Integer numeroActualizaciones;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
}

