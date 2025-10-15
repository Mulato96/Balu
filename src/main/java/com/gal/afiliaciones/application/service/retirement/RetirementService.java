package com.gal.afiliaciones.application.service.retirement;

import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.domain.model.retirement.ContractListResponseDTO;
import com.gal.afiliaciones.domain.model.retirement.RetirementRequestDTO;
import com.gal.afiliaciones.domain.model.retirement.RetirementResponseDTO;
import com.gal.afiliaciones.domain.model.retirement.WorkerSearchRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.workerretirement.DataWorkerRetirementDTO;

import java.time.LocalDate;
import java.util.List;

public interface RetirementService {

    BodyResponseConfig<DataWorkerRetirementDTO> consultWorker(String documentType, String documentNumber,
                                                              Long idAffiliateEmployer);
    String retirementWorker(DataWorkerRetirementDTO dataWorkerRetirementDTO);
    Boolean cancelRetirementWorker(Long idAffiliation);
    String createRequestRetirementWork(Long idAffiliation, LocalDate dateRetirement, String name);
    List<ContractListResponseDTO> searchWorker(WorkerSearchRequestDTO request);
    RetirementResponseDTO requestRetirement(RetirementRequestDTO request);

}
