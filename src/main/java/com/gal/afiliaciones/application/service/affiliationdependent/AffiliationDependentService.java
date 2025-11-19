package com.gal.afiliaciones.application.service.affiliationdependent;

import java.util.List;

import com.gal.afiliaciones.domain.model.BondingTypeDependent;
import com.gal.afiliaciones.domain.model.WorkModality;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.AffiliationDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.AffiliationIndependentStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.AffiliationIndependentStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.DependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.HeadquarterDataDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.UpdateEconomicActivityRequest;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityDTO;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityHeadquarterDTO;
import com.gal.afiliaciones.infrastructure.dto.validatecontributorelationship.ValidateContributorRequest;

public interface AffiliationDependentService {

    List<BondingTypeDependent> findAll();
    DependentWorkerDTO consultUser(ValidateContributorRequest request);
    DependentWorkerDTO preloadUserNotExists(ValidateContributorRequest request);
    HeadquarterDataDTO consultHeadquarters(Long idAffiliate);
    AffiliationDependent createAffiliation(AffiliationDependentDTO dto);
    List<WorkModality> findAlllWorkModalities();
    AffiliationDependent createAffiliationIndependentStep1(AffiliationIndependentStep1DTO dto);
    AffiliationDependent createAffiliationIndependentStep2(AffiliationIndependentStep2DTO dto);
    List<EconomicActivityDTO> findEconomicActivitiesByEmployer(Long idAffiliateEmployer);
    List<EconomicActivityHeadquarterDTO> findEconomicActivitiesByHeadquarter(Long idHeadquarter);

    List<AffiliationDependent> findByIdHeadquarter(Long idHeadquarter);
    
    void updateEconomicActivityCode(UpdateEconomicActivityRequest request);

}
