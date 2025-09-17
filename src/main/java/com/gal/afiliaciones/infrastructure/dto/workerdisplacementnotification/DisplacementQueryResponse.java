package com.gal.afiliaciones.infrastructure.dto.workerdisplacementnotification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisplacementQueryResponse {
    
    private boolean workerExists;
    private boolean hasMultipleActiveRelationships;
    private String messageResponse;
    
    // Worker basic information
    private String workerDocumentType;
    private String workerDocumentNumber;
    private String workerFirstName;
    private String workerSecondName;
    private String workerFirstSurname;
    private String workerSecondSurname;
    private String workerBirthDate;
    private Integer workerAge;
    private String workerSex;
    private String workerNationality;
    private String workerAffiliationType;
    private String workerSubCompany;
    private String workerCompleteAddress;
    private String workerEps;
    private String workerAfp;
    private String workerPhone1;
    private String workerPhone2;
    private String workerEmail;
    private String workerStatus;
    
    // Additional information for displacement grid
    private Integer activeRelationshipsCount;
}
