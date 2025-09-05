package com.gal.afiliaciones.infrastructure.dao.repository.affiliationtaxidriverindependent;

import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;

import java.util.List;
import java.util.Optional;

public interface AffiliationTaxiDriverIndependentDAO {

    Affiliation createAffiliation(Affiliation affiliation);

    List<UserMain> findPreloadedData(String identificationType, String identification);

    Optional<Affiliation> updateAffiliation(Affiliation affiliation);

    Optional<Affiliation> findAffiliationById(Long id);

}
