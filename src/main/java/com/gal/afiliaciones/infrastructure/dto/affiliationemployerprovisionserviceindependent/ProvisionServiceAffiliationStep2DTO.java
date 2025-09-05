package com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProvisionServiceAffiliationStep2DTO {
    private Long id;
    private AddressWorkDataCenterDTO addressWorkDataCenterDTO;
    private ContractorDataStep2DTO contractorDataStep2DTO;
    private Long codeMainEconomicActivity;
    private Long activityEconomicSecondaryOne;
    private Long activityEconomicSecondaryTwo;
    private Long activityEconomicSecondaryThree;
    private Long activityEconomicSecondaryFour;
}
