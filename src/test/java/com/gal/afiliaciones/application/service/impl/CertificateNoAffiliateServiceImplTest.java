package com.gal.afiliaciones.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.application.service.CodeValidCertificationService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.QrDocument;
import com.gal.afiliaciones.domain.model.affiliate.Certificate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IQrRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.CertificateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateReportRequestDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

public class CertificateNoAffiliateServiceImplTest {

    private GenericWebClient genericWebClient;
    private CodeValidCertificationService codeValidCertificationService;
    private CertificateRepository certificateRepository;
    private IQrRepository qrRepository;
    private ArlInformationDao arlInformationDao;
    private FiledService filedService;

    private CertificateNoAffiliateServiceImpl service;

    @BeforeEach
    void setUp() {
        genericWebClient = mock(GenericWebClient.class);
        codeValidCertificationService = mock(CodeValidCertificationService.class);
        certificateRepository = mock(CertificateRepository.class);
        qrRepository = mock(IQrRepository.class);
        arlInformationDao = mock(ArlInformationDao.class);
        filedService = mock(FiledService.class);

        service = new CertificateNoAffiliateServiceImpl(
                genericWebClient,
                codeValidCertificationService,
                certificateRepository,
                qrRepository,
                arlInformationDao,
                filedService
        );
    }

    @Test
    void validateNonAffiliateCertificate_existingCertificateNotExpired_returnsGeneratedReport() {
        String identification = "123";
        String typeIdentification = "CC";

        Certificate certificate = new Certificate();
        certificate.setNumberDocument(typeIdentification);
        certificate.setTypeDocument(identification);
        certificate.setValidatorCode("valCode");
        certificate.setExpeditionDate(LocalDate.now().minusDays(10).toString());
        certificate.setFiledNumber("filed123");
        certificate.setNameSignatureARL("signature");

        Optional<Certificate> optionalCertificate = Optional.of(certificate);

        when(certificateRepository.findByNumberDocumentAndTypeDocument(typeIdentification, identification))
                .thenReturn(optionalCertificate);

        ArlInformation arlInformation = new ArlInformation();
        arlInformation.setName("ARL Name");
        arlInformation.setNit("123456789");
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));

        when(genericWebClient.generateReportCertificate(any())).thenReturn("reportContent");

        String result = service.validateNonAffiliateCertificate(identification, typeIdentification);

        assertEquals("reportContent", result);

        // Verify no deletion happened because certificate is not expired (less than 1 month)
        verify(qrRepository, never()).findByIdentificationNumberAndIssueDate(anyString(), any());
        verify(qrRepository, never()).delete(any());
        verify(certificateRepository, never()).delete(any(Specification.class));

        // Verify generateReportCertificate called with correct parameters
        ArgumentCaptor<CertificateReportRequestDTO> captor = ArgumentCaptor.forClass(CertificateReportRequestDTO.class);
        verify(genericWebClient).generateReportCertificate(captor.capture());
        CertificateReportRequestDTO dto = captor.getValue();
        assertEquals("certificado no afiliado", dto.getReportName());
        assertNotNull(dto.getParameters());
        assertEquals("valCode", dto.getParameters().get("validatorCode"));
        assertEquals("filed123", dto.getParameters().get("consecutivoDoc"));
        assertEquals("signature", dto.getParameters().get("nameSignatureARL"));
        assertEquals("ARL Name", dto.getParameters().get(Constant.NAME_ARL_LABEL));
        assertEquals("123456789", dto.getParameters().get(Constant.NIT));
    }

    @Test
    void validateNonAffiliateCertificate_existingCertificateExpired_deletesAndGeneratesNewReport() {
        String identification = "123";
        String typeIdentification = "CC";

        LocalDate expiredDate = LocalDate.now().minusMonths(2);

        Certificate certificate = new Certificate();
        certificate.setNumberDocument(typeIdentification);
        certificate.setTypeDocument(identification);
        certificate.setValidatorCode("valCode");
        certificate.setExpeditionDate(expiredDate.toString());
        certificate.setFiledNumber("filed123");

        Optional<Certificate> optionalCertificate = Optional.of(certificate);

        when(certificateRepository.findByNumberDocumentAndTypeDocument(typeIdentification, identification))
                .thenReturn(optionalCertificate);

        QrDocument qrDocument = new QrDocument();
        when(qrRepository.findByIdentificationNumberAndIssueDate("valCode", expiredDate))
                .thenReturn(List.of(qrDocument));

        ArlInformation arlInformation = new ArlInformation();
        arlInformation.setName("ARL Name");
        arlInformation.setNit("123456789");
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));

        when(genericWebClient.generateReportCertificate(any())).thenReturn("reportContent");

        String result = service.validateNonAffiliateCertificate(identification, typeIdentification);

        assertEquals("reportContent", result);

        // Verify deletion of QR documents and certificate
        verify(qrRepository).findByIdentificationNumberAndIssueDate("valCode", expiredDate);
        verify(qrRepository).delete(qrDocument);
        verify(certificateRepository).delete(certificate);

        // Verify generateReportCertificate called
        verify(genericWebClient).generateReportCertificate(any());
    }

    @Test
    void validateNonAffiliateCertificate_newCertificate_createsAndGeneratesReport() {
        String identification = "123";
        String typeIdentification = "CC";

        when(certificateRepository.findByNumberDocumentAndTypeDocument(typeIdentification, identification))
                .thenReturn(Optional.empty());

        when(codeValidCertificationService.consultCode(identification, typeIdentification, true))
                .thenReturn("codeVal");

        when(filedService.getNextFiledNumberCertificate())
                .thenReturn("filedNum");

        ArlInformation arlInformation = new ArlInformation();
        arlInformation.setName("ARL Name");
        arlInformation.setNit("123456789");
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));

        when(genericWebClient.generateReportCertificate(any())).thenReturn("reportContent");

        String result = service.validateNonAffiliateCertificate(identification, typeIdentification);

        assertEquals("reportContent", result);

        ArgumentCaptor<Certificate> certificateCaptor = ArgumentCaptor.forClass(Certificate.class);
        verify(certificateRepository).save(certificateCaptor.capture());
        Certificate savedCertificate = certificateCaptor.getValue();

        assertEquals(identification, savedCertificate.getNumberDocument());
        assertEquals(typeIdentification, savedCertificate.getTypeDocument());
        assertEquals("codeVal", savedCertificate.getValidatorCode());
        assertEquals(Constant.TYPE_NOT_AFFILLATE, savedCertificate.getVinculationType());
        assertEquals("filedNum", savedCertificate.getFiledNumber());
        assertNotNull(savedCertificate.getExpeditionDate());

        // Verify generateReportCertificate called with correct parameters
        ArgumentCaptor<CertificateReportRequestDTO> captor = ArgumentCaptor.forClass(CertificateReportRequestDTO.class);
        verify(genericWebClient).generateReportCertificate(captor.capture());
        CertificateReportRequestDTO dto = captor.getValue();
        assertEquals("certificado no afiliado", dto.getReportName());
        assertNotNull(dto.getParameters());
        assertEquals("codeVal", dto.getParameters().get("validatorCode"));
        assertEquals(typeIdentification, dto.getParameters().get("identificationType"));
        assertEquals(identification, dto.getParameters().get("identification"));
        assertEquals("filedNum", dto.getParameters().get("consecutivoDoc"));
        assertEquals("", dto.getParameters().get("nameSignatureARL"));
        assertEquals("ARL Name", dto.getParameters().get(Constant.NAME_ARL_LABEL));
        assertEquals("123456789", dto.getParameters().get(Constant.NIT));
    }
}