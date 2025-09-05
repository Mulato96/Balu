package com.gal.afiliaciones.infrastructure.dto.economicactivity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EconomicActivityDTO {
    private Long id;
    private String classRisk;
    private String codeCIIU;
    private String additionalCode;
    private String description;
    private String economicActivityCode;
    private Long idEconomicSector;
}
