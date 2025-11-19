package com.gal.afiliaciones.config.mapper;

import java.util.function.Function;

import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityDTO;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.response.EconomicActivityResponseDTO;

public class EconomicActivityAdapter {

    private EconomicActivityAdapter() {}

    public static final Function<EconomicActivity, EconomicActivityResponseDTO> entityToDto = (EconomicActivity entity) -> {
        if (entity == null) return null;

        return EconomicActivityResponseDTO.builder()
                .idEconomicActivity(entity.getId())
                .description(entity.getCodeCIIU() + " - " + entity.getDescription())
            .build();
    };

    public static final Function<EconomicActivity, EconomicActivityDTO> entityToEconmyActivityDto = (EconomicActivity entity) -> {
        if (entity == null) return null;

        return EconomicActivityDTO.builder()
                .id(entity.getId())
                .economicActivityCode(entity.getEconomicActivityCode())
                .classRisk(entity.getClassRisk())
                .codeCIIU(entity.getCodeCIIU())
                .additionalCode(entity.getAdditionalCode())
                .description(entity.getEconomicActivityCode() + " - " + entity.getDescription())
                .idEconomicSector(entity.getIdEconomicSector())
                .build();
    };

}
