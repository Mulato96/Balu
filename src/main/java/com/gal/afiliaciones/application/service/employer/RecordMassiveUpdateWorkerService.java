package com.gal.afiliaciones.application.service.employer;

import com.gal.afiliaciones.domain.model.affiliate.RecordMassiveUpdateWorker;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;

import java.util.List;
import java.util.Optional;

public interface RecordMassiveUpdateWorkerService {

    RecordMassiveUpdateWorker save (RecordMassiveUpdateWorker recordLoadBulk);
    List<RecordMassiveUpdateWorker> findAllByIdUser(Long idUser);
    Optional<RecordMassiveUpdateWorker> findById(Long id);
    ExportDocumentsDTO createDocument(Long id);

}
