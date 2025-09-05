package com.gal.afiliaciones.domain.model.affiliate;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "certificate_affiliate")
public class CertificateAffiliate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "certificate_affiliate_seq")
    @SequenceGenerator(
        name = "certificate_affiliate_seq",
        sequenceName = "certificate_affiliate_id_seq",
        allocationSize = 1
    )
    private Long id;

    @Column(name = "coverage_date")
    private LocalDate coverageDate;

    @Column(name = "identification_type")
    private String identificationType;

    @Column(name = "identification_number")
    private String identificationNumber;

    @Column(name = "worker")
    private String worker;

    @Column(name = "risk")
    private String risk;

    @Column(name = "rate")
    private String rate;

    @Column(name = "certificate_id", nullable = false)
    private Long certificateId;

}