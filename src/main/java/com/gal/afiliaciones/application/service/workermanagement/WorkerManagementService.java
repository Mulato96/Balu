package com.gal.afiliaciones.application.service.workermanagement;

import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.domain.model.affiliate.RecordMassiveUpdateWorker;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.ResponseServiceDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.EmployerCertificateRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.FiltersWorkerManagementDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.UpdateContractDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.UpdateContractResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.UpdateWorkerCoverageDateDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.UpdateWorkerCoverageDateResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.WorkerDetailDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.WorkerManagementDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.WorkerManagementPaginatedResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.WorkerSearchFilterDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.WorkerSearchResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface WorkerManagementService {

    List<WorkerManagementDTO> findWorkersByEmployer(FiltersWorkerManagementDTO filters);
    WorkerManagementPaginatedResponseDTO findWorkersByEmployerPaginated(FiltersWorkerManagementDTO filters);
    BodyResponseConfig<AffiliationDependent> findDataDependentById(String filedNumber);
    ResponseServiceDTO massiveUpdateWorkers(MultipartFile file, Long idUser, Long idAffiliateEmployer);
    String downloadTemplateMassiveUpdate();
    String downloadTemplateGuide();
    List<RecordMassiveUpdateWorker> findAllByIdUser(Long idUser);
    ExportDocumentsDTO createDocument(Long id);
    String generateEmloyerCertificate(EmployerCertificateRequestDTO requestDTO);
    List<WorkerSearchResponseDTO> getWorkersByDocument(WorkerSearchFilterDTO filter);
    UpdateWorkerCoverageDateResponseDTO updateWorkerCoverageDate(UpdateWorkerCoverageDateDTO dto);
    WorkerDetailDTO getWorkerDetail(Long idAffiliate);
    UpdateContractResponseDTO updateContract(UpdateContractDTO dto);

}
