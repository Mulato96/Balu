package com.gal.afiliaciones.application.service;

import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateWorkerByEmployerResponse;

public interface CertificateWorkerARLIntegrationService {

    CertificateWorkerByEmployerResponse getCertificatesWorkerArlIntegration(String documentType, String documentNumber);
}
