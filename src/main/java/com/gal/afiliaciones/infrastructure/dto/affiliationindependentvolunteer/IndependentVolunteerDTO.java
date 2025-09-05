package com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndependentVolunteerDTO {

    private String identificationDocumentType;
    private String identificationDocumentNumber;
    private String firstName;
    private String secondName;
    private String surname;
    private String secondSurname;
    private LocalDate dateOfBirth;
    private String age;
    private String gender;
    private String otherGender;
    private Long nationality;
    private Long healthPromotingEntity;
    private Boolean isForeignPension;
    private Long pensionFundAdministrator;

}
