package com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndependentProvisionServiceResponseDTO {

    private Long id;
    private String identificationDocumentType;
    private String identificationDocumentNumber;
    private ContractorDataStep1DTO contractorData;
    private InformationIndependentWorkerDTO independentData;
    private AddressWorkDataCenterDTO workcenterData;
    private ContractorDataStep2DTO contractData;
    private EconomicActivityStep2 economicActivity;
    private ProvisionServiceAffiliationStep3DTO signatoryAndContribution;
    private String filedNumber;
    private String stageManagement;
    private String affiliationSubType;

}
