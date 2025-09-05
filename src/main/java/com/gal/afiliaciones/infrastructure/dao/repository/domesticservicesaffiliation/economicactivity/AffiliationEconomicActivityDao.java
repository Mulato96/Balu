package com.gal.afiliaciones.infrastructure.dao.repository.domesticservicesaffiliation.economicactivity;

import com.gal.afiliaciones.domain.model.EconomicActivity;

public interface AffiliationEconomicActivityDao {
    EconomicActivity findEconomicActivity(String codeMainEconomicActivity);
}