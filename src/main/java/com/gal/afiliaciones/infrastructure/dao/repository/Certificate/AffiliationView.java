package com.gal.afiliaciones.infrastructure.dao.repository.Certificate;

import java.time.LocalDateTime;

public interface AffiliationView {

    Long getIdAffiliate();
    LocalDateTime getAffiliationDate();
    String getDocumentNumber();
}
