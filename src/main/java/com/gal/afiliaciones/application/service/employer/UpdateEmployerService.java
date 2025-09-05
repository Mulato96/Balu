package com.gal.afiliaciones.application.service.employer;

import com.gal.afiliaciones.infrastructure.dto.employer.RequestUpdateDataBasicDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.RequestUpdateLegalRepresentativeDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.UpdateEmployerDataBasicDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.UpdateLegalRepresentativeDataDTO;

public interface
UpdateEmployerService {

    Boolean updateEmployerDataBasic(RequestUpdateDataBasicDTO dto);
    UpdateEmployerDataBasicDTO searchEmployerDataBasic(String documentType, String documentNumber,
                                                       String affiliationSubType);
    UpdateLegalRepresentativeDataDTO searchLegalRepresentativeData(String documentType, String documentNumber,
                                                                   String affiliationSubType);
    Boolean updateLegalRepresentativeData(RequestUpdateLegalRepresentativeDTO dto);

}
