package com.gal.afiliaciones.application.service.cancelaffiliation;

import com.gal.afiliaciones.infrastructure.dto.cancelaffiliate.CancelAffiliateDTO;

public interface ICancelAffiliationService {
    CancelAffiliateDTO consultAffiliation(String documentType, String documentNumber, Long idUser, String subType);
    void updateStatusCanceledAffiliate(String numberDocument, String observation);
}
