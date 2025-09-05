package com.gal.afiliaciones.infrastructure.dto.sat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationWorkerIndependentArlDTO {

    private String policyNumber;
    private String responsiblePersonTypeAffiliation;
    private String responsiblePersonDocumentTypeAffiliation;
    private String responsiblePersonDocumentNumberAffiliation;
    private String responsiblePersonSocialReasonOrNameAffiliation;
    private String responsibleNaturalPersonFirstNameAffiliation;
    private String responsibleNaturalPersonLastNameAffiliation;
    private Long responsibleContributorTypeAffiliation;
    private String responsibleContributorClassAffiliation;
    private Long responsibleLegalNatureContributorAffiliation;
    private String responsibleAffiliationMunicipalityLocation;
    private String responsibleAffiliationAddressLocation;
    private String responsibleAffiliationZoneLocation;
    private Long responsibleAffiliationPhoneFixedOrMobile;
    private String responsibleAffiliationEmail;
    private String workerDocumentType;
    private String workerDocumentNumber;
    private String workerFirstName;
    private String workerLastName;
    private String workerMunicipalityWork;
    private String workerAddressWork;
    private String workerZoneWork;
    private Long workerPhoneFixedOrMobile;
    private String workerEmail;
    private Long responsibleMainEconomicActivityCodeAffiliation;
    private Long workerOccupationCode;
    private Long workerContributorType;
    private Long workerContributorSubtype;
    private Long baseContributionIncome;
    private LocalDate affiliationArlDate;

}
