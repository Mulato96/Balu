package com.gal.afiliaciones.infrastructure.dto.affiliationdependent;

import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.DataContributionVolunteerDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationIndependentStep2DTO {

    private Long idAffiliation;
    private String identificationTypeEmployer;
    private String identificationNumberEmployer;
    private Long idAffiliateEmployer;
    private LocalDate coverageDate;
    private ContractDataIndependentDTO contractorData;
    private SignatoryDataIndependentDTO signatoryData;
    private DataContributionVolunteerDTO dataContribution;
    private Boolean fromPila;

}
