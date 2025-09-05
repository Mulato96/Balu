package com.gal.afiliaciones.application.service.affiliate.affiliationemployeractivitiesmercantile;

import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.infrastructure.dto.EmployerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DateInterviewWebDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.AffiliateMercantileDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.DataBasicCompanyDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.DataLegalRepresentativeDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.InterviewWebDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.DocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.StateAffiliation;
import com.gal.afiliaciones.infrastructure.dto.typeemployerdocument.DocumentRequestDTO;

import java.util.List;
import java.util.Map;

public interface AffiliationEmployerActivitiesMercantileService {

    DataBasicCompanyDTO validationsStepOne(String numberDocument, String typeDocument, String cv);
    AffiliateMercantile stepOne(DataBasicCompanyDTO dataBasicCompanyDTO);
    DataLegalRepresentativeDTO findUser(AffiliateMercantile affiliateMercantile);
    AffiliateMercantile stepTwo(DataLegalRepresentativeDTO dataLegalRepresentativeDTO, boolean isInterviewWeb);
    AffiliateMercantileDTO stepThree(Long idAffiliate,Long idTypeEmployer, Long idSubTypeEmployer, List<DocumentRequestDTO> files);
    void stateDocuments(List<DocumentsDTO> listDocumentsDTOS, Long idAffiliate);
    void stateAffiliation(AffiliateMercantile affiliateMercantile, StateAffiliation stateAffiliation);
    Map<String, Object> scheduleInterviewWeb(DateInterviewWebDTO dateInterviewWebDTO);
    void interviewWeb(StateAffiliation stateAffiliation);
    List<DataDocumentAffiliate> regularizationDocuments(String filedNumber, Long idTypeEmployer, Long idSubTypeEmployer, List<DocumentRequestDTO> files);
    String updateDataInterviewWeb(InterviewWebDTO interviewWebDTO);
    void changeAffiliation(String filedNumber);
    Boolean affiliateBUs(String tipoDoc, String idEmpresa, Integer idSubEmpresa);
    void consultWSConfecamaras(String numberDocument, String dv, DataBasicCompanyDTO dataBasicCompanyDTO);
}
