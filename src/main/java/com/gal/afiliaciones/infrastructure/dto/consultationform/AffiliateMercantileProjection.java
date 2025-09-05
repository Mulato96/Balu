package com.gal.afiliaciones.infrastructure.dto.consultationform;

public interface AffiliateMercantileProjection {
    String getNumberIdentification();
    Integer getDigitVerificationDV();
    String getActivityEconomicPrimary();
    String getDepartment();
    String getCityMunicipality();
    String getAddress();
    String getPhoneOne();
    String getPhoneTwo();
    String getEmail();
    String getTypeDocumentPersonResponsible();
    String getNumberDocumentPersonResponsible();
    String getLegalStatus();
}