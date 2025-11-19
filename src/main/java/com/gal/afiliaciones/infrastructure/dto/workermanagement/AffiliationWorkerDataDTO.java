package com.gal.afiliaciones.infrastructure.dto.workermanagement;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationWorkerDataDTO {

    private String identificationDocumentType;
    private String identificationDocumentNumber;
    private String firstName;
    private String secondName;
    private String surname;
    private String secondSurname;
    private LocalDate dateOfBirth;

}
