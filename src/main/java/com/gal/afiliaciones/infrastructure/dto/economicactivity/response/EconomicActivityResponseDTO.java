package com.gal.afiliaciones.infrastructure.dto.economicactivity.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EconomicActivityResponseDTO {

    private Long idEconomicActivity;
    private String description;

}
