package com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DomesticServiceAffiliationStep1DTO {

    private Long idAffiliation;
    private String identificationDocumentType;
    private String identificationDocumentNumber;
    // Economic activities
    private Boolean isHiringDomesticService;
    private int numDomesticService;
    private Boolean isHiringNurse;
    private int numNurse;
    private Boolean isHiringButler;
    private int numButler;
    private Boolean isHiringDriver;
    private int numDriver;
    // Basic information
    private Boolean isRuralZoneEmployer;
    private Long departmentEmployer;
    private Long municipalityEmployer;
    //Direccion
    private String addressEmployer;
    private Long idMainStreetEmployer;
    private Long idNumberMainStreetEmployer;
    private Long idLetter1MainStreetEmployer;
    private Boolean isBisEmployer;
    private Long idLetter2MainStreetEmployer;
    private Long idCardinalPointMainStreetEmployer;
    private Long idNum1SecondStreetEmployer;
    private Long idLetterSecondStreetEmployer;
    private Long idNum2SecondStreetEmployer;
    private Long idCardinalPoint2Employer;
    private Long idHorizontalProperty1Employer;
    private Long idNumHorizontalProperty1Employer;
    private Long idHorizontalProperty2Employer;
    private Long idNumHorizontalProperty2Employer;
    private Long idHorizontalProperty3Employer;
    private Long idNumHorizontalProperty3Employer;
    private Long idHorizontalProperty4Employer;
    private Long idNumHorizontalProperty4Employer;
    private String phone1;
    private String phone2;
    private String email;

}
