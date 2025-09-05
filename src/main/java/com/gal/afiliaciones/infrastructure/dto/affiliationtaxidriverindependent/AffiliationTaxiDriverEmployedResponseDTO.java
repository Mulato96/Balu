package com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationTaxiDriverEmployedResponseDTO {

    private String identificationDocumentType;
    private String identificationDocumentNumber;
    private String firstName;
    private String secondName;
    private String surname;
    private String secondSurname;
    private LocalDate dateOfBirth;
    private String age;
    private Long nationality;
    private String gender;
    private String otherGender;
    private Long pensionFundAdministrator;
    private Boolean isForeignPension;
    private Long healthPromotingEntity;
    private Long department;
    private Long cityMunicipality;
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
    private String address;
    private String phone1;
    private String phone2;
    private String email;
    private String occupation;

}
