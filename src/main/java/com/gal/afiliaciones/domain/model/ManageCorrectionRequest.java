package com.gal.afiliaciones.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "gestionar_solicitud_correccion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManageCorrectionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "request_correction_id")
    private ContributionCorrection requestCorrection;

    @Column(name = "status")
    private String status;

    @Column(name = "observation")
    private String observation;

    @Column(name = "fecha_creacion")
    private LocalDate creationDate;

    @Column(name = "folder_id")
    private String folderId;
}