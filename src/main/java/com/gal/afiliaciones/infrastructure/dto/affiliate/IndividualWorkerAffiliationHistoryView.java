package com.gal.afiliaciones.infrastructure.dto.affiliate;

import java.time.LocalDateTime;

public interface IndividualWorkerAffiliationHistoryView {

    Long getIdAffiliate();
    String getAffiliationType();
    String getCompanyDocumentType();
    String getCompanyDocumentNumber();
    String getCompanyName();
    String getAffiliationStatus();
    String getFirstName();
    String getSecondName();
    String getSurname();
    String getSecondSurname();
    LocalDateTime getAffiliationDate();
    

}
