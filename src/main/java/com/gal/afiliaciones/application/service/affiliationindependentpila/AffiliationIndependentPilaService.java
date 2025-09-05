package com.gal.afiliaciones.application.service.affiliationindependentpila;

import com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyIndependentRequestDTO;

public interface AffiliationIndependentPilaService {

    Long createAffiliationProvisionServicePila(NoveltyIndependentRequestDTO dto);
    Long createAffiliationTaxiDriverPila(NoveltyIndependentRequestDTO dto);
    Long createAffiliationCouncillorPila(NoveltyIndependentRequestDTO dto);
    Long createAffiliationVolunteerPila(NoveltyIndependentRequestDTO dto);

}
