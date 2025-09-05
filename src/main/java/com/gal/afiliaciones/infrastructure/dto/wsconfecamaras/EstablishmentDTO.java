package com.gal.afiliaciones.infrastructure.dto.wsconfecamaras;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstablishmentDTO {

    private Long id;
    private String chamberCode;
    private String registrationNumber;
    private String companyName;
    private String legalOrganizationCode;
    private String registrationCategoryCode;
    private String registrationStatusCode;
    private String lastRenewedYear;
    private String renewalDate;
    private String establishmentValue;
    private String commercialMunicipality;
    private String commercialAddress;
    private String commercialNeighborhood;
    private String commercialPostalCode;
    private String commercialPhone1;
    private String commercialPhone2;
    private String commercialPhone3;
    private String commercialEmail;
    private String companyLocationCode;
    private String fiscalAddress;
    private String fiscalNeighborhood;
    private String fiscalPostalCode;
    private String fiscalMunicipality;
    private String fiscalEmail;
    private String employees;
    private String ciiu1;
    private String ciiu1Description;
    private String shd1;
    private String ciiu2;
    private String ciiu2Description;
    private String shd2;
    private String ciiu3;
    private String shd3;
    private String ciiu4;
    private String shd4;
    private String economicActivityDescription;
    private String ownerType;
    private String localTypeCode;
    private String affiliate;
    private String previousRenewedYear;
    private String previousRenewalDate;
    private String registrationDate;
    private String cancellationDate;
    private String ruesUpdateDate;
    private CompanyRecordDTO companyRecord;
}