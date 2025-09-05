package com.gal.afiliaciones.infrastructure.dto.workerretirement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataWorkerRetirementDTO {

    private Long idAffiliation;
    private String identificationDocumentType;
    private String identificationDocumentNumber;
    private String firstName;
    private String secondName;
    private String surname;
    private String secondSurname;
    private String bondingType;
    private String affiliationStatus;
    private LocalDate coverageDate;
    private LocalDate affiliationDate;
    private LocalDate retirementDate;
    private Long idRetirementReason;

}
