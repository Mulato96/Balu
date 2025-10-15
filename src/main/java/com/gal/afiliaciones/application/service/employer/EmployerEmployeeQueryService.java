package com.gal.afiliaciones.application.service.employer;

import com.gal.afiliaciones.infrastructure.dto.employer.EmployerEmployeeListResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.EmployerEmployeeQueryRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.tmp.TmpAffiliateStatusDTO;

public interface EmployerEmployeeQueryService {

    /**
     * Query employee information using the 4 specific parameters
     * @param request Request with tDocEmp, idEmp, tDocAfi, idAfi
     * @return Employee information from both tables
     */
    EmployerEmployeeListResponseDTO queryEmployeeByParameters2(EmployerEmployeeQueryRequestDTO request);

    /**
     * Same as queryEmployeeByParameters2 but allows forcing source via appSource (BALU or BALU_PRE)
     */
    EmployerEmployeeListResponseDTO queryEmployeeByParameters2(EmployerEmployeeQueryRequestDTO request, String appSource);

    /**
     * Query employee/company status using the 4 specific parameters.
     * Returns a shorter status response.
     */
    java.util.List<TmpAffiliateStatusDTO> queryEmployeeByParameters1(EmployerEmployeeQueryRequestDTO request);

    /**
     * Same as queryEmployeeByParameters1 but allows forcing source via appSource (BALU or BALU_PRE)
     */
    java.util.List<TmpAffiliateStatusDTO> queryEmployeeByParameters1(EmployerEmployeeQueryRequestDTO request, String appSource);
}
