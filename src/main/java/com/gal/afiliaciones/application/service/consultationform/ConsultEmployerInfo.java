package com.gal.afiliaciones.application.service.consultationform;

import com.gal.afiliaciones.domain.model.HistoryOptions;
import com.gal.afiliaciones.infrastructure.dto.consultationform.HeadquartersAffiliationDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.AffiliationInformationDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.DocumentsCollectionAffiliationDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.DocumentsOfAffiliationDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.EmployerUpdateDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.PolicyDTO;
import com.gal.afiliaciones.infrastructure.dto.retirementreason.RegisteredAffiliationsDTO;

import java.util.List;

public interface ConsultEmployerInfo {

    List<PolicyDTO> getPolicyInfo(String filedNumber );

    List<RegisteredAffiliationsDTO> getEconomyActivities(String typeIdentification, String identification);

    List<HistoryOptions> getHistoryOptions();

    AffiliationInformationDTO getAffiliationInfoEmployeer(String filedNumber);

    DocumentsOfAffiliationDTO getDocumentsAffiliation(String filedNumber);

    DocumentsCollectionAffiliationDTO getDcoumentsColection(String typeIdentification, String identification);

    List<EmployerUpdateDTO> getUpdatesWeb(String filedNumber);

    List<HeadquartersAffiliationDTO> getHeadquarters(String filedNumber);
}
