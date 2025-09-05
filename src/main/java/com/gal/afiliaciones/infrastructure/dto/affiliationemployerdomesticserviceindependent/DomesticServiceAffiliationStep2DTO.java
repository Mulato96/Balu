package com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DomesticServiceAffiliationStep2DTO {

    private Long idAffiliation;
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
    private String otherGender;
    private String nationality;
    private Long healthPromotingEntity;
    private Long pensionFundAdministrator;
    private Long department;
    private Long cityMunicipality;
    private Boolean isSameEmployerAddress;
    private Boolean isRuralZone;
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
