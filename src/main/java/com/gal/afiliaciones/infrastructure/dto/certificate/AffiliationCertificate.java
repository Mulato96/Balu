package com.gal.afiliaciones.infrastructure.dto.certificate;

import java.time.LocalDate;

public interface AffiliationCertificate {
    String getCompany();
    String getNitCompany();
    String getAffiliationStatus();
    String getRetirementDate();
    String getAffiliationSubtype();
    String getFullName();
    String getIdentificationType();
    String getIdentificationNumber();
    LocalDate getCoverageDate();
    String getRisk();
    String getEndDate();
    String getOccupationName();
    String getIdentificationDocumentType();
}
