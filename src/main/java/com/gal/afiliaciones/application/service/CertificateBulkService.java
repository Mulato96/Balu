package com.gal.afiliaciones.application.service;

import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.infrastructure.dto.certificate.ResponseBulkDTO;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CertificateBulkService {
    ResponseBulkDTO generateMassiveWorkerCertificates(MultipartFile file, String numberDocument, String typeDocument);
    void deleteRecords(String idDocument);
    MultipartFile createCertificatesMassive(String idDocument);
    void createCertificatesMassive(String type, LocalDate date, UserMain userMain);
    void deleteRecordsCertificate();
    List<Map<String, Object>> recordsBulkMassive();
    MultipartFile downloadDocumentZip(String idDocument);
    String getTemplate();
    UserMain getUserPreRegister();
}
