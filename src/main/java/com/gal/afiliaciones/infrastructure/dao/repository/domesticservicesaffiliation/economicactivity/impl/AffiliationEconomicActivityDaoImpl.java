package com.gal.afiliaciones.infrastructure.dao.repository.domesticservicesaffiliation.economicactivity.impl;

import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.infrastructure.dao.repository.domesticservicesaffiliation.economicactivity.AffiliationEconomicActivityDao;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AffiliationEconomicActivityDaoImpl implements AffiliationEconomicActivityDao {

    private final IEconomicActivityRepository iEconomicActivityRepository;

    @Override
    public EconomicActivity findEconomicActivity(String codeMainEconomicActivity) {
        String classRisk = String.valueOf(codeMainEconomicActivity.charAt(0));
        String codeCIIU = codeMainEconomicActivity.substring(1,5);
        String additionalCode = codeMainEconomicActivity.substring(5);

        return iEconomicActivityRepository.findByClassRiskAndCodeCIIUAndAdditionalCode(
                classRisk, codeCIIU, additionalCode
        );
    }
}