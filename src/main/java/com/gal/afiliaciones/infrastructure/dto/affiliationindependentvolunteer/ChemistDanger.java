package com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChemistDanger {

    private Boolean isSolidAerosols;
    private Boolean isLiquidAerosols;
    private Boolean isOrganicOrInorganicGases;
    private Boolean isVapours;

}
