package com.gal.afiliaciones.application.service.employer;

import com.gal.afiliaciones.infrastructure.dto.employer.DataBasicEmployerMigratedDTO;

import java.util.List;

public interface SearchEmployerMigratedService {
    List<DataBasicEmployerMigratedDTO> searchEmployerDataBasic(String documentType, String documentNumber, String userType);
}
