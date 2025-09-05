package com.gal.afiliaciones.infrastructure.dto.affiliationindependentcouncillor;

import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ContractorDataStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.InformationIndependentWorkerDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationCouncillorStep1DTO {

    private Long id;
    private String identificationDocumentType;
    private String identificationDocumentNumber;
    private ContractorDataStep1DTO contractorDataDTO;
    private InformationIndependentWorkerDTO informationIndependentWorkerDTO;
    private Boolean is723;

}
