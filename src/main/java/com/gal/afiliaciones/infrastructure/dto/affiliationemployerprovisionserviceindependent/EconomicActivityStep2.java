package com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EconomicActivityStep2 {

    private Long id;
    private String classRisk;
    private String codeCIIU;
    private String additionalCode;
    private String description;
    private String economicActivityCode;

}
