package com.gal.afiliaciones.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.data.annotation.Immutable;

import java.time.LocalDateTime;

@Immutable
@Entity
@Table(name = "affiliations_view")
@Data
public class AffiliationsView {

    @Id
    private Long id;

    @Column(name = "type_affiliation")
    private String affiliationType;

    @Column(name = "filed_number")
    private String filedNumber;

    @Column(name = "stage_management")
    private String stageManagement;

    @Column(name = "date_request")
    private String dateRequest;

    @Column(name = "name_or_social_reason")
    private String nameOrSocialReason;

    @Column(name = "number_document")
    private String numberDocument;

    @Column(name = "cancelled")
    private Boolean cancelled;

    @Column(name = "date_interview")
    private LocalDateTime dateInterview;

    @Column(name = "date_regularization")
    private LocalDateTime dateRegularization;

    @Column(name = "revision_doc_asignada_a")
    private String asignadoA;

}
