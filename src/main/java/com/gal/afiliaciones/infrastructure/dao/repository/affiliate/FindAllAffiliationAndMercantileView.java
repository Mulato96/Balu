package com.gal.afiliaciones.infrastructure.dao.repository.affiliate;

public interface FindAllAffiliationAndMercantileView {

    Long getId();
    String getField();
    String getDateRequest();
    String getNumberDocument();
    String getNameOrSocialReason();
    String getTypeAffiliation();
    String getStageManagement();
    String getDateInterview();
    boolean getCancelled();

}
