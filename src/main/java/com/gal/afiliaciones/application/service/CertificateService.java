package com.gal.afiliaciones.application.service;

import java.util.List;

import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.Certificate;
import com.gal.afiliaciones.domain.model.affiliate.FindAffiliateReqDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.QrDTO;

public interface CertificateService {


    Certificate createCertificate(Affiliate affiliate, String addressedTo);
    String createAndGenerateCertificate(FindAffiliateReqDTO findAffiliateReqDTO);
    List<Certificate> findByTypeDocumentAndNumberDocument(String typeDocument, String numberDocument);
    String generateReportCertificate(String numberDocument, String documentType);
    String getValidateCodeCerticate(String validationCode);
    QrDTO getValidateCodeQR(String idCode);
}
