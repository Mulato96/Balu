package com.gal.afiliaciones.application.service.affiliate.recordloadbulk;

import com.gal.afiliaciones.domain.model.affiliate.RecordLoadBulk;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;

import java.util.List;
import java.util.Optional;

public interface RecordLoadBulkService {

    RecordLoadBulk save (RecordLoadBulk recordLoadBulk);
    List<RecordLoadBulk> findAllByIdUser(Long idUser);
    Optional<RecordLoadBulk> findById(Long id);
    ExportDocumentsDTO createDocument(Long id);

}
