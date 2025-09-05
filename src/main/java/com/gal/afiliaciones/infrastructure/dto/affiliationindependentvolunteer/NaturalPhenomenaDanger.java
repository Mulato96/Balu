package com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NaturalPhenomenaDanger {

    private Boolean isQuake;
    private Boolean isFlood;
    private Boolean isAvalanche;
    private Boolean isVolcanicEruption;
    private Boolean isTheft;
    private Boolean isManifestation;
    private Boolean isTerrorism;
    private Boolean isExtortion;
    private Boolean isKidnapping;
    private Boolean isStorm;

}
