package com.gal.afiliaciones.domain.model.affiliate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "affiliate")
public class Affiliate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_affiliate")
    private Long idAffiliate;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "company")
    private String company;

    @Column(name = "nit_company")
    private String nitCompany;

    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "affiliation_type")
    private String affiliationType;

    @Column(name = "affiliation_date")
    private LocalDateTime affiliationDate;

    @Column(name = "coverage_start_date")
    private LocalDate coverageStartDate;

    @Column(name = "affiliation_status")
    private String affiliationStatus;

    @Column(name = "risk")
    private String risk;

    @Column(name = "retirement_date")
    private LocalDate retirementDate;

    @Column(name = "position")
    private String position;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "affiliation_subtype")
    private String affiliationSubType;

    @Column(name = "filed_number")
    private String filedNumber;

    @Column(name = "affiliation_cancelled")
    private Boolean affiliationCancelled;

    @Column(name = "status_document")
    private Boolean statusDocument;

    @Column(name = "observation")
    private String observation;

    @Column(name = "date_affiliate_suspended")
    private LocalDateTime dateAffiliateSuspend;

    @Column(name = "id_official")
    private Long idOfficial;

    @Column(name = "novelty_type")
    private String noveltyType;

    @Column(name = "request_channel")
    private Long requestChannel;

}
