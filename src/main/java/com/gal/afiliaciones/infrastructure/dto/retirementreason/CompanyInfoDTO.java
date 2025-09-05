package com.gal.afiliaciones.infrastructure.dto.retirementreason;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyInfoDTO {
    private String idDocumentType;
    private String idDocumentNumber;
    private Integer verificationDigit;
    private String businessName;
    private String legalRepDocumentType;
    private String legalRepDocumentNumber;
    private List<RegisteredAffiliationsDTO> registeredEconomicActivities;
    private String typeOfAfiliate;
}
