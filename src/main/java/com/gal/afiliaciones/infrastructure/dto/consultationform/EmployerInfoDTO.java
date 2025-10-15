package com.gal.afiliaciones.infrastructure.dto.consultationform;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerInfoDTO implements InfoConsultDTO {

    private String documentType;
    private String employerIdentificationNumber;
    private String verificationDigit;
    private String companyName;
    private String affiliationDate;
    private String coverageDate;
    private Long department;
    private Long city;
    private String fullAddress;
    private String phoneNumber1;
    private String phoneNumber2;
    private String email;
    private String economicActivity;
    private String legalRepresentativeDocumentType;
    private String legalRepresentativeIdentificationNumber;
    private String legalRepresentativeName;
    private Long activeDependentEmployees;
    private Long activeIndependentEmployees;
    private int totalActiveEmployees;
    private int totalInactiveEmployees;
    private Long totalEmployees;
    private String nature;
    private String typeInfo;
    private Boolean isActive;
    private String filedNumber;
    private String id_employer_size;
}