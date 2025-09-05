package com.gal.afiliaciones.infrastructure.dto.affiliate;

import java.time.LocalDateTime;

public interface IndividualWorkerAffiliationView {

    Long getIdAffiliate();
    String getDocumentType();
    String getDocumentNumber();
    String getFirstName();
    String getSecondName();
    String getSurname();
    String getSecondSurname();
    LocalDateTime getAffiliationDate();
    String getAffiliationStatus();

}
