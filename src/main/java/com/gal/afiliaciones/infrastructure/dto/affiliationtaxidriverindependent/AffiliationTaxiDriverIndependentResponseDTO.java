package com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent;

import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.DataContributionVolunteerDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationTaxiDriverIndependentResponseDTO {

    private Long id;

    private String identificationDocumentType;

    private String identificationDocumentNumber;

    private AffiliationTaxiDriverContractorResponseDTO contractorData;

    private AffiliationTaxiDriverEmployedResponseDTO independentData;

    private AffiliationTaxiDriverContractResponseDTO contractData;

    private DataContributionVolunteerDTO contributionData;

    private String filedNumber;

    private String stageManagement;

    private String affiliationSubType;

}
