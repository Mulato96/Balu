package com.gal.afiliaciones.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "poliza")
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo")
    private String code;

    @Column(name = "tipo_identificacion")
    private String idType;

    @Column(name = "numero_identificacion")
    private String idNumber;

    @Column(name = "fecha_vigencia_desde")
    private LocalDate effectiveDateFrom;

    @Column(name = "fecha_vigencia_hasta")
    private LocalDate effectiveDateTo;

    @Column(name = "fecha_emision")
    private LocalDate issueDate;

    @Column(name = "estado")
    private String status;

    @Column(name = "tipo_poliza")
    private Long idPolicyType;

    @Column(name = "id_affiliate")
    private Long idAffiliate;

    @Column(name = "decentralized_consecutive")
    private Long decentralizedConsecutive = 0L;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "num_policy_client")
    private Long numPolicyClient;

}
