package com.gal.afiliaciones.application.service.economicactivity;

import java.util.List;

import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityDTO;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.response.EconomicActivityResponseDTO;

public interface IEconomicActivityService {
    List<EconomicActivityDTO> getEconomicActivityByCodeCIIU(String codeCIIU, String description);
    EconomicActivity getEconomicActivityByRiskCodeCIIUCodeAdditional(String risk, String codeCIIU, String codeAdditional);
    EconomicActivityDTO getEconomicActivityByCode(String code);
    List<EconomicActivity> listEconomicActivity(List<Long> ids);
    List<EconomicActivityResponseDTO> findUserEconomicActivity(String documentType, String documentNumber);

    List<EconomicActivityDTO> getEconomyActivityExcludeCurrent(String documentType, String documentNumber);
    List<EconomicActivityDTO> getEconomicActivitiesByEconomicSectorId(Long economicSectorId);
}
