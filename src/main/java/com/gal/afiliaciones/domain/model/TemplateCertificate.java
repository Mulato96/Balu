package com.gal.afiliaciones.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

@Entity
@NoArgsConstructor
@Data
@Table(name = "tmp_Template_certificate")
public class TemplateCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToMany(mappedBy = "id", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WorkedTypeAffiliation> id_worked_type_affiliation;
    private String risk;
    private String vinculationType;
    private Date dateConsultation;
    private String CodeQR;
    private String codeValidation;
    private boolean download;
    private String cityMunicipality;
    private String signatureHolography;
    private String signatureCertified;
    private String signatureOfficial;
    private String nameOfficial;
    private String numberFiled;


}
