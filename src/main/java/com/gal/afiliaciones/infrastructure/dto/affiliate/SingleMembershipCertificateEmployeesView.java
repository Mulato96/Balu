package com.gal.afiliaciones.infrastructure.dto.affiliate;

import java.time.LocalDate;

public interface SingleMembershipCertificateEmployeesView {

    LocalDate getCoverageDate();
    String getIdentificationType();
    String getIdentificationNumber();
    String getFullName();
    String getRisk();
    Float getRiskRate();

}
