package com.gal.afiliaciones.infrastructure.dto.workermanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkerManagementDTO {

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
    private Boolean pendingCompleteFormPila;
    private String retiredWorker;

}
