package com.gal.afiliaciones.domain.model.affiliate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "danger")
public class Danger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long idAffiliation;
    private Boolean isNoise;
    private Boolean isVibration;
    private Boolean isExtremeAtmosphericPressures;
    private Boolean isExtremeTemperatureByCold;
    private Boolean isExtremeTemperatureByWarm;
    private Boolean isNonIonizingRadiation;
    private Boolean isIonizingRadiation;
    private Boolean isSolidAerosols;
    private Boolean isLiquidAerosols;
    private Boolean isOrganicOrInorganicGases;
    private Boolean isVapours;
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
    private Boolean isStatic;
    private Boolean isDynamic;
    private Boolean isInappropriateWorkPlans;
    private Boolean isInappropriateWorkspaces;
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
