package com.gal.afiliaciones.domain.model.affiliate;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;

@Data
@Entity
public class Certificate {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "certificate_seq")
    @SequenceGenerator(
        name = "certificate_seq",
        sequenceName = "certificate_id_seq",
        allocationSize = 1
    )
    private Long id;

    @Column(name = "nit")
    private String nit;

    @Column(name = "name")
    private String name;

    @Column(name = "document_type_contrator")
    private String documentTypeContrator;

    @Column(name = "nit_contrator")
    private String nitContrator;

    @Column(name = "company")
    private String company;

    @Column(name = "type_document")
    private String typeDocument;

    @Column(name = "number_document")
    private String numberDocument;

    @Column(name = "retirement_date")
    private String retirementDate;

    @Column(name = "init_contract_date")
    private LocalDate initContractDate;

    @Column(name = "coverage_date")
    private LocalDate coverageDate;

    @Column(name = "end_contract_date")
    private String endContractDate;

    @Column(name = "status")
    private String status;

    @Column(name = "vinculation_type")
    private String vinculationType;

    @Column(name = "vinculation_subtype")
    private String vinculationSubType;

    @Column(name = "position")
    private String position;

    @Column(name = "risk")
    private String risk;

    @Column(name = "city")
    private String city;

    @Column(name = "expedition_date")
    private String expeditionDate;

    @Column(name = "validator_code")
    private String validatorCode;

    @Column(name = "name_signature_arl")
    private String nameSignatureARL;

    @Column(name = "name_arl")
    private String nameARL;

    @Column(name = "membership_date")
    private LocalDate membershipDate;

    @Column(name = "filed_number")
    private String filedNumber;

    @Column(name = "code_activity_economic_primary")
    private String codeActivityEconomicPrimary;

    @Column(name = "name_activity_economic")
    private String nameActivityEconomic;

    @Column(name = "addressed_to")
    private String addressedTo;

    @Column(name = "risk_rate")
    private String riskRate;

    @Column(name = "address")
    private String address;

    @Column(name = "department")
    private String department;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "dependent_workers_number")
    private Integer dependentWorkersNumber;

    @Column(name = "independent_workers_number")
    private Integer independentWorkersNumber;

}