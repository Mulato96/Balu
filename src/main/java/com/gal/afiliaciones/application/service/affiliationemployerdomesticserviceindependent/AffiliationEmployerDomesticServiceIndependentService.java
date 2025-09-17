package com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent;

import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.*;
import com.gal.afiliaciones.infrastructure.dto.alfresco.DocumentBase64;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AffiliationEmployerDomesticServiceIndependentService {

    VisualizationPendingPerformDTO visualizationPendingPerform();
    ResponseManagementDTO managementAffiliation(Integer page, Integer size, AffiliationsFilterDTO filter);
    ManagementDTO management(String field);
    Affiliation createAffiliationStep1(DomesticServiceAffiliationStep1DTO dto);
    Affiliation createAffiliationStep2(DomesticServiceAffiliationStep2DTO dto);
    Affiliation createAffiliationStep3(Long idAffiliation, MultipartFile document);
    List<DocumentBase64> consultDocument(String id);
    void stateAffiliation(StateAffiliation stateAffiliation) ;
    void stateDocuments(List<DocumentsDTO> listDocumentsDTOS, Long idAffiliate);
    List<DataDocumentAffiliate> findDocuments(Long idAffiliate);
    String generateExcel(AffiliationsFilterDTO filter);
    void assignTo(String filedNumber, Long usuarioId);

}
