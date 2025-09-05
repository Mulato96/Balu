package com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile;

import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.DocumentsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffiliateMercantileDTO {

    private Long id;
    private String typeDocumentIdentification;
    private String numberIdentification;
    private Integer digitVerificationDV;
    private String businessName;
    private String typePerson;
    private Long numberWorkers;
    private String zoneLocationEmployer;
    private Long department;
    private Long cityMunicipality;
    private String address;
    private String phoneOne;
    private String phoneTwo;
    private String email;

    private Long departmentContactCompany;
    private Long cityMunicipalityContactCompany;
    private String addressContactCompany;
    private String phoneOneContactCompany;
    private String phoneTwoContactCompany;
    private String emailContactCompany;

    private Long idUserPreRegister;
    private Long afp;
    private Long eps;

    private String filedNumber;
    private String dateRequest = String.valueOf(LocalDate.now());
    private String stageManagement;
    private String idFolderAlfresco;

    private Boolean affiliationCancelled =  false;
    private Boolean statusDocument =  false;

    private String activityEconomicPrimary;
    private String activityEconomicSecondaryOne;
    private String activityEconomicSecondaryTwo;
    private String activityEconomicSecondaryThree;
    private String activityEconomicSecondaryFour;

    private List<DocumentsDTO> documents;
}
