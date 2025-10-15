package com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DomesticServiceAffiliationDTO {

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
    private String departmentEmployer;
    private String municipalityEmployer;
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
    // Legal representative information
    private String identificationDocumentType;
    private String identificationDocumentNumber;
    private String personType;
    private String firstName;
    private String secondName;
    private String surname;
    private String secondSurname;
    private LocalDate dateOfBirth;
    private String age;
    private String gender;
    private String nationality;
    private String healthPromotingEntity;
    private String pensionFundAdministrator;
    private String department;
    private String cityMunicipality;
    private String address;
    private Long idMainStreet;
    private Long idNumberMainStreet;
    private Long idLetter1MainStreet;
    private Boolean isBis;
    private Long idLetter2MainStreet;
    private Long idCardinalPointMainStreet;
    private Long idNum1SecondStreet;
    private Long idLetterSecondStreet;
    private Long idNum2SecondStreet;
    private Long idCardinalPoint2;
    private Long idHorizontalProperty1;
    private Long idNumHorizontalProperty1;
    private Long idHorizontalProperty2;
    private Long idNumHorizontalProperty2;
    private Long idHorizontalProperty3;
    private Long idNumHorizontalProperty3;
    private Long idHorizontalProperty4;
    private Long idNumHorizontalProperty4;
    private String secondaryPhone1;
    private String secondaryPhone2;
    private String secondaryEmail;

}
