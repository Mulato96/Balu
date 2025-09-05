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

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tmp_card")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "full_name_worked")
    private String fullNameWorked;
    @Column(name = "type_document_worker")
    private String typeDocumentWorker;
    @Column(name = "number_document_worker")
    private String numberDocumentWorker;
    @Column(name = "company")
    private String company;
    @Column(name = "document_type_employer")
    private String documentTypeEmployer;
    @Column(name = "nit_company")
    private String nitCompany;
    @Column(name = "date_affiliation")
    private LocalDate dateAffiliation;
    @Column(name = "type_affiliation")
    private String typeAffiliation;
    @Column(name = "affiliation_status")
    private String affiliationStatus;
    @Column(name = "end_contract_date")
    private String endContractDate;
    @Column(name = "code_qr")
    private String codeQR;
    @Column(name = "name_arl")
    private String nameARL;
    @Column(name = "email_arl")
    private String emailARL;
    @Column(name = "address_arl")
    private String addressARL;
    @Column(name = "page_web_arl")
    private String pageWebARL;
    @Column(name = "phone_arl")
    private String phoneArl;
    @Column(name = "filed_number")
    private String filedNumber;

}
