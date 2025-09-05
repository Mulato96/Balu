package com.gal.afiliaciones.application.service.workermanagement;

import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.domain.model.affiliate.RecordMassiveUpdateWorker;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.ResponseServiceDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.EmployerCertificateRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.FiltersWorkerManagementDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.WorkerManagementDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface WorkerManagementService {

    List<WorkerManagementDTO> findWorkersByEmployer(FiltersWorkerManagementDTO filters);
    BodyResponseConfig<AffiliationDependent> findDataDependentById(String filedNumber);
    ResponseServiceDTO massiveUpdateWorkers(MultipartFile file, String documentType, String documentNumber);
    String downloadTemplateMassiveUpdate();
    String downloadTemplateGuide();
    List<RecordMassiveUpdateWorker> findAllByIdUser(Long idUser);
    ExportDocumentsDTO createDocument(Long id);
    String generateEmloyerCertificate(EmployerCertificateRequestDTO requestDTO);

}
