package com.gal.afiliaciones.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "retirement")
public class Retirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "identification_type")
    private String identificationDocumentType;
    @Column(name = "identification_number")
    private String identificationDocumentNumber;
    @Column(name = "name_or_company_name")
    private String completeName;
    @Column(name = "affiliation_type")
    private String affiliationType;
    @Column(name = "affiliation_subtype")
    private String affiliationSubType;
    @Column(name = "retirement_date")
    private LocalDate retirementDate;
    @Column(name = "filed_number")
    private String filedNumber;
    @Column(name = "id_affiliate")
    private Long idAffiliate;
    @Column(name = "id_retirement_reason")
    private Long idRetirementReason;

}
