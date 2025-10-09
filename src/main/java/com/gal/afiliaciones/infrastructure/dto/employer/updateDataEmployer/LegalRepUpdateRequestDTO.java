package com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LegalRepUpdateRequestDTO {

    private String docType;
    private String docNumber;
    private String employerType;
    private String epsId;
    private String afpId;
    private String addressFull;
    private String phone1;
    private String phone2;
    private String email;
    private String causeCode;
    private String eventDate;
    private String observations;
}