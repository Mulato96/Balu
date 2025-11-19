package com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent;

import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.EconomicActivityStep2;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationTaxiDriverIndependentPreLoadDTO {

    // Identificación del contratante
    private Long idAffiliateMercantile;

    // Datos de identificación del contratante
    private String contractorIdentificationType;

    private String contractorIdentificationNumber;

    private String contractorDigiteVerification;

    private String companyName;

    private Long decentralizedConsecutive;

    private Boolean isPublicEmployer;

    private String phone1;

    private String email;

    // Datos del representante legal

    private String legalRepidentificationDocumentType;

    private String legalRepidentificationDocumentNumber;

    private String legalRepFirstName;

    private String legalRepSecondName;

    private String legalRepSurname;

    private String legalRepSecondSurname;

    private String contractorEmail;

    private String actualARLContract;

    // Actividad economica principal
    private EconomicActivityStep2 economicActivity;

    private Boolean is723;

    private Long idAffiliateEmployer;

}
