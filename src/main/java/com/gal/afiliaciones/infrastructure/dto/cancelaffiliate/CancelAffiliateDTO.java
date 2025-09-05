package com.gal.afiliaciones.infrastructure.dto.cancelaffiliate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelAffiliateDTO {
    private Long id;
    private String identificationDocumentType;
    private String identificationDocumentNumber;
    private String firstName;
    private String secondName;
    private String surname;
    private String secondSurname;
    private String filedNumber;
    private LocalDate coverageDate;
    private String contractType;
    private String observation;
}