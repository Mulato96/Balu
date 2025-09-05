package com.gal.afiliaciones.infrastructure.dto.wsconfecamaras;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRecordDTO {

    private Long id;
    private String chamberCode;
    private String chamber;
    private String registrationNumber;
    private String proponentRegistration;
    private String companyName;
    private String identificationTypeCode;
    private String identificationType;
    private String identificationNumber;
    private String verificationDigit;
    private String registrationStatusCode;
    private String registrationStatus;
    private String societyTypeCode;
    private String societyType;
    private String legalOrganizationCode;
    private String legalOrganization;
    private String registrationCategoryCode;
    private String registrationCategory;
    private String lastRenewedYear;
    private String renewalDate;
    private String registrationDate;
    private String cancellationDate;
    private String gender;
    private String numberOfFemaleEmployees;
    private String numberOfFemaleDirectors;
    private String companySizeCode;
    private String emailAuthorization;
    private String commercialAddress;
    private String commercialZone;
    private String commercialPostalCode;
    private String commercialMunicipalityCode;
    private String commercialMunicipality;
    private String commercialDepartment;
    private String commercialPhone1;
    private String commercialEmail;
    private String fiscalAddress;
    private String fiscalZone;
    private String fiscalPostalCode;
    private String fiscalMunicipalityCode;
    private String fiscalMunicipality;
    private String fiscalDepartment;
    private String fiscalPhone1;
    private String fiscalEmail;
    private String primaryEconomicActivityCode;
    private String primaryEconomicActivityDescription;
    private String secondaryEconomicActivityCode;
    private String secondaryEconomicActivityDescription;
    private String majorIncomeCiiu;
    private List<BondDTO> bonds;
    private List<EstablishmentDTO> establishments;

}
