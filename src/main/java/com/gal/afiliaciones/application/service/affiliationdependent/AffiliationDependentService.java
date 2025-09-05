package com.gal.afiliaciones.application.service.affiliationdependent;

import com.gal.afiliaciones.domain.model.BondingTypeDependent;
import com.gal.afiliaciones.domain.model.WorkModality;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.AffiliationDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.AffiliationIndependentStep1DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.AffiliationIndependentStep2DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.DependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.HeadquarterDataDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.RequestSearchEconomicActivitiesDTO;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityDTO;
import com.gal.afiliaciones.infrastructure.dto.validatecontributorelationship.ValidateContributorRequest;

import java.util.List;

public interface AffiliationDependentService {

    List<BondingTypeDependent> findAll();
    DependentWorkerDTO consultUser(ValidateContributorRequest request);
    DependentWorkerDTO preloadUserNotExists(ValidateContributorRequest request);
    HeadquarterDataDTO consultHeadquarters(String documentType, String documentNumber, String affiliationSubtype);
    AffiliationDependent createAffiliation(AffiliationDependentDTO dto);
    List<WorkModality> findAlllWorkModalities();
    AffiliationDependent createAffiliationIndependentStep1(AffiliationIndependentStep1DTO dto);
    AffiliationDependent createAffiliationIndependentStep2(AffiliationIndependentStep2DTO dto);
    List<EconomicActivityDTO> findEconomicActivitiesByEmployer(RequestSearchEconomicActivitiesDTO request);

}
