package com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProvisionServiceAffiliationStep1DTO {
    private Long id;
    private String identificationDocumentType;
    private String identificationDocumentNumber;
    private ContractorDataStep1DTO contractorDataDTO;
    private InformationIndependentWorkerDTO informationIndependentWorkerDTO;
    private Boolean is723;
}
