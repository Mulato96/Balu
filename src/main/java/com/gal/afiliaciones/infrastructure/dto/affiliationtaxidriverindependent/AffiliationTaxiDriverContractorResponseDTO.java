package com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationTaxiDriverContractorResponseDTO {

    private String identificationDocumentTypeContractor;
    private String identificationDocumentNumberContractor;
    private Integer dv;
    private String companyName;
    private String identificationDocumentTypeLegalRepresentative;
    private String identificationDocumentNumberContractorLegalRepresentative;
    private String legalRepFirstName;
    private String legalRepSecondName;
    private String legalRepSurname;
    private String legalRepSecondSurname;
    private String emailContractor;
    private String currentARL;

}
