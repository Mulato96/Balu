package com.gal.afiliaciones.application.service.tmp;

import com.gal.afiliaciones.infrastructure.dto.tmp.TmpExcelPersonDTO;
import com.gal.afiliaciones.infrastructure.dto.tmp.TmpAffiliateStatusDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.EmployerEmployeeDTO;

import java.util.List;

public interface ExcelPersonConsultationService {
    List<TmpExcelPersonDTO> consultPersonFromTmp(String documentType, String documentNumber);

    List<TmpAffiliateStatusDTO> consultAffiliateStatus(
            String employerDocType, String employerDocNumber,
            String personDocType, String personDocNumber
    );

    List<EmployerEmployeeDTO> consultPersonAsEmployerEmployee(
            String employerDocType, String employerDocNumber,
            String personDocType, String personDocNumber
    );
}


