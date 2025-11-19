package com.gal.afiliaciones.infrastructure.dto.workermanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkerSearchResponseDTO {

    private String identificationDocumentType;
    private String identificationDocumentNumber;
    private String completeName;
    private String occupation;
    private String startContractDate;
    private String endContractDate;
    private String status;
    private String filedNumber;
    private String affiliationType;
    private String affiliationSubType;
    private Long idAffiliate;
    private String retiredWorker;
    private String company;
    private String coverageDate;
    private String employerCoverageDate;

}

