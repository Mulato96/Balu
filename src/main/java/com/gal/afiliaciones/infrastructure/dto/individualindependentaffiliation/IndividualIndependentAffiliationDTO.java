package com.gal.afiliaciones.infrastructure.dto.individualindependentaffiliation;

import com.gal.afiliaciones.infrastructure.dto.affiliationindependentvolunteer.AffiliationIndependentVolunteerStep2DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IndividualIndependentAffiliationDTO {

    //Encabezados de trámite
    private String filedNumber;
    private String filingDate;
    private String affiliationStartDate;
    private String affiliationEndDate;
    private String consecutiveDoc;

    //General information of the independent worker
    private String fullNameOrBusinessNameGI;
    private String identificationDocumentTypeGI;
    private String identificationDocumentNumberGI;
    private String dateOfBirthGI;
    private String genderGI;
    private String nationalityGI;
    private String currentHealthInsuranceGI;
    private String currentPensionFundGI;
    private String addressGI;
    private String departmentGI;
    private String cityOrDistrictGI;
    private String mobileOrLandlineGI;
    private String emailGI;

    //Affiliation or contract information
    private String contractTypeACI;
    private String contractQualityACI;
    private Boolean transportSupplyACI;
    private String contractStartDateACI;
    private String contractEndDateACI;
    private String numberOfMonthsACI;
    private String establishedWorkShiftACI;
    private String totalContractValueACI;
    private String monthlyContractValueACI;
    private String baseContributionIncomeACI;
    private String activityCarriedACI;
    private String economicActivityCodeACI;
    private String jobPositionACI;
    private Boolean taxiDriverACI;
    private String addressACI;
    private String departmentACI;
    private String cityOrDistrictACI;

    //contractor information
    private String fullNameOrBusinessNameCI;
    private String identificationDocumentTypeCI;
    private String identificationDocumentNumberCI;
    private String dvCI;
    private String economicActivityCodeCI;
    private String addressCI;
    private String departmentCI;
    private String cityOrDistrictCI;
    private String mobileOrLandlineCI;
    private String emailCI;

    //Information of the contract signer
    private String fullNameOrBusinessNameICS;
    private String identificationDocumentTypeICS;
    private String identificationDocumentNumberICS;

    //ARL
    private String economicActivityCodeARL;
    private String riskClassARL;
    private String feeARL;

    //Signature
    private String signatureIndependent;

    AffiliationIndependentVolunteerStep2DTO affiliationIndependentVolunteerStep2DTO;
}