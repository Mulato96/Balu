package com.gal.afiliaciones.application.service.consultationform;

import com.gal.afiliaciones.infrastructure.dto.consultationform.*;
import com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker.ContractsJobRelatedDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker.HistoryJobRelatedDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker.HistoryAffiliationsWithdrawalsHistoryDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker.UpdatesWorkerHistoryDTO;
import com.gal.afiliaciones.infrastructure.dto.workerdetail.WorkerDetailDTO;

import java.util.List;

public interface ConsultationFormService {

    InfoConsultDTO getInfo(String typeIdentification, String identification, String affiliationType);

    List<JobRelationShipDTO> getJobRelatedInfo(String typeIdentification, String identification);

    List<ConsultUpdatesDTO> consultUpdates(String typeIdentification, String identification);

    List<HistoryAffiliationsWithdrawalsDTO> getHistoryAffiliationsWithdrawals(String typeIdentification, String identification);

    HistoryJobRelatedDTO getHistoryJobRelated(String filedNumber);

    List<ContractsJobRelatedDTO> getContractsJobRelated(String filedNumber);

    HistoryAffiliationsWithdrawalsHistoryDTO getAffiliationWithdrawalsHistory(String filedNumber);

    UpdatesWorkerHistoryDTO getUpdatesWorkerHistory(String filedNumber);

    DocumentsOfAffiliationDTO getDocumentAffiliationWorker(String filedNumber);

    List<GeneralConsultDTO> generalConsult(String typeIdentification, String identification);

    WorkerDetailDTO getWorkerDetails(String fileNumber);

}
