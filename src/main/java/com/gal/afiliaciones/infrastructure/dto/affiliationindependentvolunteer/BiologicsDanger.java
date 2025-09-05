package com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BiologicsDanger {

    private Boolean isVirus;
    private Boolean isBacterium;
    private Boolean isRickettsias;
    private Boolean isFungus;
    private Boolean isCrustaceans;
    private Boolean isArachnids;
    private Boolean isRodents;
    private Boolean isInvertebrates;
    private Boolean isVertebrates;
    private Boolean isVegetables;

}
