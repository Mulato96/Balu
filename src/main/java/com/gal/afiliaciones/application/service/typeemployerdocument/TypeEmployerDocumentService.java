package com.gal.afiliaciones.application.service.typeemployerdocument;

import com.gal.afiliaciones.domain.model.affiliate.typeemployerdocument.DocumentRequested;
import com.gal.afiliaciones.domain.model.affiliate.typeemployerdocument.SubTypeEmployer;
import com.gal.afiliaciones.domain.model.affiliate.typeemployerdocument.TypeEmployer;
import com.gal.afiliaciones.infrastructure.dto.LegalStatusDTO;
import com.gal.afiliaciones.infrastructure.dto.typeemployerdocument.TypeEmployerDocumentDTO;

import java.util.List;
import java.util.Map;

public interface TypeEmployerDocumentService {

    List<TypeEmployer> findAllTypeEmployer();
    List<SubTypeEmployer> findAllSubTypeEmployer();
    List<DocumentRequested> findAllDocumentRequested();
    List<DocumentRequested> findDocumentsRequireTrueByIdSubTypeEmployer(Long id);
    List<SubTypeEmployer> findBySubTypeEmployer(Long id);
    List<DocumentRequested> findByIdSubTypeEmployerListDocumentRequested(Long id);
    List<TypeEmployerDocumentDTO> allFind();
    TypeEmployer findById(Long id);
    Map<String, String> findNameTypeAndSubType(Long idType, Long idSubType);
    List<LegalStatusDTO> listLegalStatus();

}
