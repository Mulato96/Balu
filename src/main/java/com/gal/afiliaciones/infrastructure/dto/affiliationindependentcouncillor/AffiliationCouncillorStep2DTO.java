package com.gal.afiliaciones.infrastructure.dto.affiliationindependentcouncillor;

import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.AddressWorkDataCenterDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ContractorDataStep2DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationCouncillorStep2DTO {

    private Long id;
    private AddressWorkDataCenterDTO addressWorkDataCenterDTO;
    private ContractorDataStep2DTO contractorDataStep2DTO;
    private Long codeMainEconomicActivity;
    private Long activityEconomicSecondaryOne;
    private Long activityEconomicSecondaryTwo;
    private Long activityEconomicSecondaryThree;
    private Long activityEconomicSecondaryFour;

}
