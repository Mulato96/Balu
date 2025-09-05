package com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EconomicActivityDomesticService {

    private Boolean isHiringDomesticService;
    private int numDomesticService;
    private Boolean isHiringNurse;
    private int numNurse;
    private Boolean isHiringButler;
    private int numButler;
    private Boolean isHiringDriver;
    private int numDriver;

}
