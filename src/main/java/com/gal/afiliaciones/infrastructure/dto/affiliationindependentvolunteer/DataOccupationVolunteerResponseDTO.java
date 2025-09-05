package com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataOccupationVolunteerResponseDTO {

    private Long departmentEmployer;
    private Long municipalityEmployer;
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
    private String occupation;
    private String secondaryPhone1;
    private String secondaryPhone2;
    private BigDecimal contractIbcValue;

}
