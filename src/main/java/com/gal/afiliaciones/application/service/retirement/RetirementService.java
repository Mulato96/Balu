package com.gal.afiliaciones.application.service.retirement;

import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.infrastructure.dto.workerretirement.DataWorkerRetirementDTO;

import java.time.LocalDate;

public interface RetirementService {

    BodyResponseConfig<DataWorkerRetirementDTO> consultWorker(String documentType, String documentNumber,
                                                              Long idAffiliateEmployer);
    String retirementWorker(DataWorkerRetirementDTO dataWorkerRetirementDTO);
    Boolean cancelRetirementWorker(Long idAffiliation);
    String createRequestRetirementWork(Long idAffiliation, LocalDate dateRetirement, String name);

}
