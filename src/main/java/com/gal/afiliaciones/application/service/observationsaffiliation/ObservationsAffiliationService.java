package com.gal.afiliaciones.application.service.observationsaffiliation;

import com.gal.afiliaciones.infrastructure.dto.observationsaffiliation.ObservationAffiliationDTO;

import java.util.List;

public interface ObservationsAffiliationService {

    ObservationAffiliationDTO create(String observation, String filedNumber, String reasonReject, Long idOfficial);
    List<ObservationAffiliationDTO> findByFiledNumber(String filedNumber);
}
