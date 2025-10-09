package com.gal.afiliaciones.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDtoApiRegistry {
    private Long id;
    private String firstName;
    private String secondName;
    private String surname;
    private String secondSurname;
    private String companyName;
    private Long userType;
    private Long status;
    private String email;
    private String phoneNumber;
    private String password;
    private String pin;
    private Timestamp createDate;
    private String identificationType;
    private String identification;
    private Integer verificationDigit;
    private LocalDate dateBirth;
    private String gender;
    private String idStatus;
    private int errorCode;

    //Campos Missings
    private String issuingMunicipality;
    private String issuingDepartment;
    private String expeditionDate;
    private String resolutionNumber;
    private String resolutionYear;
    private String informate;
    private String serialNumber;
    private String deathDate;
    private String referenceDate;
    private String affectingDate;
}
