package com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityDanger {

    private Boolean isFire;
    private Boolean isBurst;
    private Boolean isChemicalLeaks;
    private Boolean isChemicalReactions;
    private Boolean isHighVoltage;
    private Boolean isMediumVoltage;
    private Boolean isLowVoltage;
    private Boolean isElectricalEquipment;
    private Boolean isElectricalInstallation;
    private Boolean isWorkAtHeights;
    private Boolean isOrderAndCleanliness;
    private Boolean isMaterialStorage;
    private Boolean isArrangementMachinesAndEquipment;
    private Boolean isInstallation;
    private Boolean isMachinesAndEquipment;
    private Boolean isTools;
    private Boolean isMechanismsInMotion;
    private Boolean isPressureEquipmentAndLines;

}
