package com.gal.afiliaciones.application.service.workermanagement;

public interface WorkerManagementProjection {
    String getIdentificationDocumentType();
    String getIdentificationDocumentNumber();
    String getCompleteName();
    String getStartContractDate();
    String getEndContractDate();
    String getStatus();
    String getFiledNumber();
    String getAffiliationType();
    String getAffiliationSubType();
    Long getIdAffiliate();
    Boolean getPendingCompleteFormPila();
    String getRetiredWorker();
}
