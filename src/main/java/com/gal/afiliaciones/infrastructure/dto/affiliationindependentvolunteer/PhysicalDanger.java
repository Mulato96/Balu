package com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhysicalDanger {

    private Boolean isNoise;
    private Boolean isVibration;
    private Boolean isExtremeAtmosphericPressures;
    private Boolean isExtremeTemperatureByCold;
    private Boolean isExtremeTemperatureByWarm;
    private Boolean isNonIonizingRadiation;
    private Boolean isIonizingRadiation;

}
