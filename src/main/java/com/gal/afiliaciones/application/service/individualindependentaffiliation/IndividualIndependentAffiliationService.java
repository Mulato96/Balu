package com.gal.afiliaciones.application.service.individualindependentaffiliation;

import com.gal.afiliaciones.infrastructure.dto.individualindependentaffiliation.IndividualIndependentAffiliationDTO;
import net.sf.jasperreports.engine.JRException;

public interface IndividualIndependentAffiliationService {

    String generatePdfReport(IndividualIndependentAffiliationDTO dto) throws JRException;
}