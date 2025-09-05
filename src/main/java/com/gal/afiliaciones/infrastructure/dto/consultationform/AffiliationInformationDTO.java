package com.gal.afiliaciones.infrastructure.dto.consultationform;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationInformationDTO {

    private String documentType;
    private String documentNumber;
    private String firstName;
    private String middleName;
    private String firstLastName;
    private String secondLastName;
    private LocalDate birthDate;
    private String gender;
    private String nationality;

    // Health Information
    private Long afp;
    private Long eps;

    // Affiliation Information
    private LocalDate affiliationDate;
    private String companyName;
    private String workerType;
    private String economicActivity;
    private String branchOffice;
}
