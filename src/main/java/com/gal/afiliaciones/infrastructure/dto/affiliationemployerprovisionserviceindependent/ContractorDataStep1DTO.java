package com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractorDataStep1DTO {
    private String identificationDocumentTypeContractor;
    private String identificationDocumentNumberContractor;
    private Integer dv;
    private String companyName;
    private String identificationDocumentTypeLegalRepresentative;
    private String identificationDocumentNumberContractorLegalRepresentative;
    private String firstNameContractor;
    private String secondNameContractor;
    private String surnameContractor;
    private String secondSurnameContractor;
    private String emailContractor;
    private String currentARL;
}
