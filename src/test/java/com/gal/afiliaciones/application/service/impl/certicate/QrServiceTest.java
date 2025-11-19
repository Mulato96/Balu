package com.gal.afiliaciones.application.service.impl.certicate;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.gal.afiliaciones.application.service.CodeValidCertificationService;
import com.gal.afiliaciones.application.service.economicactivity.impl.EconomicActivityServiceImpl;
import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.helper.CertificateServiceHelper;
import com.gal.afiliaciones.config.ex.certificate.CertificateNotFoundException;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.Card;
import com.gal.afiliaciones.domain.model.QrDocument;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.Certificate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ICardRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ICertificateAffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IQrRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.RequestCollectionRequestRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.RequestCorrectionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.CertificateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.decree1563.OccupationDecree1563Repository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dto.certificate.QrDTO;

@ContextConfiguration(classes = {CertificateServiceImpl.class})
@ExtendWith(SpringExtension.class)
class QrServiceTest {

    @MockBean
    private CertificateRepository certificateRepository;

    @MockBean
    private CodeValidCertificationService codeValidCertificationService;

    @MockBean
    private AffiliateRepository affiliateRepository;

    @MockBean
    private GenericWebClient genericWebClient;

    @MockBean
    private CertificateServiceHelper certificateServiceHelper;

    @MockBean
    private IQrRepository iQrRepository;

    @MockBean
    private ICardRepository iCardRepository;

    @MockBean
    private ArlInformationDao arlInformationDao;

    @MockBean
    private EconomicActivityServiceImpl economicActivityService;

    @MockBean
    private IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;

    @MockBean
    private IEconomicActivityRepository economicActivityRepository;

    @MockBean
    private RequestCorrectionRepository requestCorrectionRepository;

    @MockBean
    private RequestCollectionRequestRepository requestCollectionRepository;

    @MockBean
    private FiledService filedService;

    @MockBean
    private IUserPreRegisterRepository iUserPreRegisterRepository;

    @MockBean
    private AffiliateMercantileRepository affiliateMercantileRepository;

    @MockBean
    private AffiliationDependentRepository affiliationDependentRepository;

    @MockBean
    private OccupationRepository occupationRepository;

    @MockBean
    private OccupationDecree1563Repository occupationDecree1563Repository;

    @MockBean
    private ICertificateAffiliateRepository certificateAffiliateRepository;

    @Autowired
    private CertificateServiceImpl qrService;

    @MockBean
    private ExcelProcessingServiceData excelProcessingServiceData;

    @MockBean
    private CollectProperties properties;


    private QrDocument qrDocument;
    private Card card;
    private Affiliate affiliate;
    private Certificate certificate;

    @BeforeEach
    void setUp() {
        // Inicializar objetos de prueba
        qrDocument = new QrDocument();
        qrDocument.setId(UUID.randomUUID());
        qrDocument.setName("carné");
        qrDocument.setIdentificationNumber("12345");
        qrDocument.setIssueDate(LocalDateTime.now());

        card = new Card();
        card.setFullNameWorked("John Doe");
        card.setTypeDocumentWorker("CC");
        card.setNumberDocumentWorker("12345");
        card.setDateAffiliation(LocalDate.now());
        card.setAddressARL("Some Address");

        affiliate = new Affiliate();
        affiliate.setCompany("Some Company");
        affiliate.setNitCompany("98765");
        affiliate.setDocumentNumber("12345");
        affiliate.setCoverageStartDate(LocalDate.now());
        affiliate.setAffiliationStatus("Active");
        affiliate.setRisk("Low");

        certificate = new Certificate();
        certificate.setValidatorCode("54321");
        certificate.setName("Certificate Name");
        certificate.setTypeDocument("CC");
        certificate.setNumberDocument("12345");
        certificate.setCompany("Company");
        certificate.setNitContrator("98765");
    }

    @Test
    void testGetValidateCodeQRWithCertificate() {
        qrDocument.setName("certificado");
        when(iQrRepository.findById(any(UUID.class))).thenReturn(Optional.of(qrDocument));
        when(certificateRepository.findByValidatorCode(anyString())).thenReturn(certificate);

        QrDTO qrDTO = qrService.getValidateCodeQR(qrDocument.getId().toString());

        assertNotNull(qrDTO);
        assertEquals("certificado", qrDTO.getType());
        assertEquals(qrDocument.getName(), qrDTO.getName());

        Map<String, String> data = qrDTO.getData();
        assertEquals(certificate.getValidatorCode(), data.get("cod_validacion"));
        assertEquals(qrDocument.getIssueDate().toString(), data.get("fecha_emision"));
        assertEquals(certificate.getName(), data.get("nombre"));
        assertEquals(certificate.getTypeDocument(), data.get("tipo_identificacion"));
        assertEquals(certificate.getNumberDocument(), data.get("numero_identificacion"));
        assertEquals(certificate.getCompany(), data.get("nombre_empleador"));
        assertEquals("NIT", data.get("tipo_identificaion_empleador"));
        assertEquals(certificate.getNitContrator(), data.get("numero_identificacion_empleador"));
    }

    @Test
    void testGetValidateCodeQRWithNonExistentQrDocument() {
        when(iQrRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(CertificateNotFoundException.class, () -> {
            qrService.getValidateCodeQR(UUID.randomUUID().toString());
        });
    }

}