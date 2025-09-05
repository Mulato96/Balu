package com.gal.afiliaciones.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "gestionar_solicitud_devolucion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManageReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "request_collection_return_id")
    private RequestCollectionReturn requestCollectionReturn;

    @Column(name = "status")
    private String status;

    @Column(name = "observation")
    private String observation;

    @Column(name = "fecha_creacion")
    private LocalDate creationDate;

    @Column(name = "approved_value")
    private String approvedValue;

    @Column(name = "folder_id")
    private String folderId;
}