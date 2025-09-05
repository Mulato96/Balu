package com.gal.afiliaciones.infrastructure.dto.affiliate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAffiliateDTO {

    private Boolean affiliationCancelled;
    private LocalDateTime affiliationDate;
    private String affiliationStatus;
    private String affiliationSubType;
    private String affiliationType;
    private String company;
    private LocalDate coverageStartDate;
    private LocalDateTime dateAffiliateSuspend;
    private String documentNumber;
    private String documentType;
    private String filedNumber;
    private Long idAffiliate;
    private Long idPolicy;
    private String nitCompany;
    private String observation;
    private String position;
    private String retirementDate;
    private String risk;
    private Boolean statusDocument;
    private Long userId;

}
