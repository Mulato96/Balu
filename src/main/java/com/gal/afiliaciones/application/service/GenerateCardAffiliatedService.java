package com.gal.afiliaciones.application.service;

import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.infrastructure.dto.card.ResponseGrillaCardsDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.ValidCodeCertificateDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface GenerateCardAffiliatedService {

    ValidCodeCertificateDTO consultUserCard(String numberDocument, String typeDocument);
    List<ResponseGrillaCardsDTO> createCardUser(ValidCodeCertificateDTO validCodeCertificateDTO);
    Map<String, String> consultCard(String id);
    List<ResponseGrillaCardsDTO> createCardWithoutOtp(String filedNumber);
    String consultCardByAffiliate(String filedNumber);
    ResponseEntity<ResponseGrillaCardsDTO> createCardDependent(Affiliate affiliate, String firstName, String secondName,
                                                               String surname, String secondSurname);

}
