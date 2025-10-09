package com.gal.afiliaciones.application.service.officeremployerupdate;

import com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer.*;
import java.util.Optional;

public interface EmployerLookupService {

    Optional<EmployerBasicProjection> findBasic(String docType, String docNumber);
    int updateBasic(EmployerUpdateDTO dto);
    Optional<LegalRepViewDTO> findLegalRep(String docType, String docNumber);
    int updateLegalRep(LegalRepUpdateRequestDTO dto);
}
