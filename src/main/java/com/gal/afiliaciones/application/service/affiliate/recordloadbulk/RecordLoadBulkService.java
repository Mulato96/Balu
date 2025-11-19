package com.gal.afiliaciones.application.service.affiliate.recordloadbulk;

import java.util.List;
import java.util.Optional;

import com.gal.afiliaciones.domain.model.affiliate.RecordLoadBulk;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;

public interface RecordLoadBulkService {

    RecordLoadBulk save (RecordLoadBulk recordLoadBulk);
    List<RecordLoadBulk> findAllByIdUser(Long idUser, Long idAffiliateEmployer);
    Optional<RecordLoadBulk> findById(Long id);
    void updateStatus(Long id, String status);
    ExportDocumentsDTO createDocument(Long id);

}
