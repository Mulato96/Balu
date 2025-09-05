package com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PsychosocialDanger {

    private Boolean isLotsComplexInformation;
    private Boolean isHandleMoney;
    private Boolean isPhysicalEffort;
    private Boolean isNegativeTreatmentByPublic;
    private Boolean isLittleCommunication;
    private Boolean isLongWorkDay;
    private Boolean isInsufficientTimeWork;
    private Boolean isTimeForBreaks;
    private Boolean isImpedesSkillDevelopment;
    private Boolean isLittleTimeToDedicateToFamily;
    private Boolean isConflictiveFamilyRelationships;
    private Boolean isComplicatedEconomicSituation;
    private Boolean isLongTravelHomeWorkplace;
    private Boolean isWorkPerformedNotCorrespondingToSalary;

}
