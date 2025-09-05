package com.gal.afiliaciones.application.service.impl.certicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.application.service.CodeValidCertificationService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.helper.CertificateServiceHelper;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.config.ex.certificate.CertificateNotFoundException;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorCodeValidationExpired;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.QrDocument;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.Certificate;
import com.gal.afiliaciones.domain.model.affiliate.FindAffiliateReqDTO;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ICardRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IQrRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.CertificateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dto.certificate.QrDTO;

@ExtendWith(MockitoExtension.class)
public class CertificateServiceImplTest {

    @Mock
    private CertificateRepository certificateRepository;
    @Mock
    private CodeValidCertificationService codeValidCertificationService;
    @Mock
    private AffiliateRepository affiliateRepository;
    @Mock
    private GenericWebClient genericWebClient;
    @Mock
    private CertificateServiceHelper certificateServiceHelper;
    @Mock
    private ArlInformationDao arlInformationDao;
    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;
    @Mock
    private AffiliateMercantileRepository affiliateMercantileRepository;

    @Mock
    private IQrRepository iQrRepository;
    @Mock
    private ICardRepository iCardRepository;
    @Mock
    private AffiliationDependentRepository affiliationDependentRepository;
    @Mock
    private OccupationRepository occupationRepository;
    @Mock
    private IEconomicActivityRepository economicActivityRepository;
    @Mock
    private IUserPreRegisterRepository iUserPreRegisterRepository;
    @Mock
    private FiledService filedService;

    @InjectMocks
    private CertificateServiceImpl certificateService;

    private Affiliate affiliate;
    private Certificate certificate;
    private FindAffiliateReqDTO findAffiliateReqDTO;

    @BeforeEach
    void setUp() {
        affiliate = new Affiliate();
        affiliate.setDocumentNumber("12345");
        affiliate.setDocumentType("CC");
        affiliate.setFiledNumber("F123");
        affiliate.setAffiliationDate(LocalDateTime.now());
        affiliate.setAffiliationType("Independiente");
        affiliate.setAffiliationStatus("Activo");
        affiliate.setCompany("Test Company");
        affiliate.setNitCompany("900123456-1");

        certificate = new Certificate();
        certificate.setNumberDocument("12345");
        certificate.setValidatorCode("VALID123");
        certificate.setVinculationType("Independiente");

        findAffiliateReqDTO = new FindAffiliateReqDTO();
        findAffiliateReqDTO.setIdAffiliate(1);
    }

    @Test
    void createAndGenerateCertificate_whenAffiliateNotFound_shouldReturnMessage() {
        when(affiliateRepository.findById(1L)).thenReturn(Optional.empty());

        String result = certificateService.createAndGenerateCertificate(findAffiliateReqDTO);

        assertEquals("Affiliate not found with the provided criteria.", result);
        verify(affiliateRepository).findById(1L);
        verify(certificateRepository, never()).save(any());
    }

    @Test
    void findByTypeDocumentAndNumberDocument_whenCertificatesFound_shouldReturnList() {
        when(certificateRepository.findByTypeDocumentAndNumberDocument("CC", "12345"))
                .thenReturn(Collections.singletonList(certificate));

        List<Certificate> result = certificateService.findByTypeDocumentAndNumberDocument("CC", "12345");

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(certificateRepository).findByTypeDocumentAndNumberDocument("CC", "12345");
    }

    @Test
    void findByTypeDocumentAndNumberDocument_whenNoCertificatesFound_shouldThrowException() {
        when(certificateRepository.findByTypeDocumentAndNumberDocument("CC", "12345"))
                .thenReturn(Collections.emptyList());

        assertThrows(CertificateNotFoundException.class, () -> {
            certificateService.findByTypeDocumentAndNumberDocument("CC", "12345");
        });
    }

    @Test
    void generateReportCertificate_whenCertificateNotFound_shouldThrowException() {
        when(certificateRepository.findByNumberDocumentAndValidatorCode(anyString(), anyString())).thenReturn(null);

        assertThrows(CertificateNotFoundException.class, () -> {
            certificateService.generateReportCertificate("12345", "INVALID");
        });
    }

    @Test
    void getValidateCodeCerticate_whenCodeIsInvalid_shouldThrowException() {
        when(certificateRepository.findByValidatorCode(anyString())).thenReturn(null);

        assertThrows(CertificateNotFoundException.class, () -> {
            certificateService.getValidateCodeCerticate("INVALID");
        });
    }

    @Test
    void getValidateCodeCerticate_whenCodeIsExpired_shouldThrowException() {
        String expiredCode = "VA"
                + LocalDate.now().minusMonths(2).format(java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy"))
                + "123";
        when(certificateRepository.findByValidatorCode(expiredCode)).thenReturn(new Certificate());

        assertThrows(ErrorCodeValidationExpired.class, () -> {
            certificateService.getValidateCodeCerticate(expiredCode);
        });
    }

    @Test
    void formatDate_shouldReturnCorrectFormat() {
        LocalDate date = LocalDate.of(2024, 7, 23);
        String formattedDate = CertificateServiceImpl.formatDate(date);
        assertEquals("23 días del mes de julio del 2024", formattedDate);
    }

    @Test
    void getValidateCodeQR_whenQrNotFound_shouldThrowException() {
        UUID qrId = UUID.randomUUID();
        when(iQrRepository.findById(qrId)).thenReturn(Optional.empty());

        assertThrows(CertificateNotFoundException.class, () -> {
            certificateService.getValidateCodeQR(qrId.toString());
        });
    }

    @Test
    void getValidateCodeQR_whenQrIsExpired_shouldThrowException() {
        UUID qrId = UUID.randomUUID();
        QrDocument qrDocument = new QrDocument();
        qrDocument.setIssueDate(LocalDateTime.now().minusDays(31));
        when(iQrRepository.findById(qrId)).thenReturn(Optional.of(qrDocument));

        CertificateNotFoundException exception = assertThrows(CertificateNotFoundException.class, () -> {
            certificateService.getValidateCodeQR(qrId.toString());
        });
        assertEquals("Qr vencido", exception.getMessage());
    }


    @Test
    void getValidateCodeQR_forCertificateType_whenCertificateFound_shouldReturnCorrectQrDTO() {
        UUID qrId = UUID.randomUUID();
        String validatorCode = "VALID123";

        QrDocument qrDocument = new QrDocument();
        qrDocument.setName("certificado");
        qrDocument.setIdentificationNumber(validatorCode);
        qrDocument.setIssueDate(LocalDateTime.now());
        when(iQrRepository.findById(qrId)).thenReturn(Optional.of(qrDocument));

        Certificate certificate = new Certificate();
        certificate.setValidatorCode(validatorCode);
        certificate.setName("Test User");
        certificate.setTypeDocument("CC");
        certificate.setNumberDocument("12345");
        certificate.setCompany("Test Company");
        certificate.setNitContrator("900123456-1");
        when(certificateRepository.findByValidatorCode(validatorCode)).thenReturn(certificate);

        QrDTO result = certificateService.getValidateCodeQR(qrId.toString());

        assertEquals("certificado", result.getName());
        assertEquals("certificado", result.getType());
        assertEquals("Test User", result.getData().get("nombre"));
        assertEquals(validatorCode, result.getData().get("cod_validacion"));
        verify(iQrRepository).findById(qrId);
        verify(certificateRepository).findByValidatorCode(validatorCode);
    }

    @Test
    void getValidateCodeQR_forCertificateType_whenCertificateNotFound_shouldThrowException() {
        UUID qrId = UUID.randomUUID();
        String validatorCode = "INVALID123";

        QrDocument qrDocument = new QrDocument();
        qrDocument.setName("certificado");
        qrDocument.setIdentificationNumber(validatorCode);
        qrDocument.setIssueDate(LocalDateTime.now());
        when(iQrRepository.findById(qrId)).thenReturn(Optional.of(qrDocument));
        when(certificateRepository.findByValidatorCode(validatorCode)).thenReturn(null);

        assertThrows(UserNotFoundInDataBase.class, () -> {
            certificateService.getValidateCodeQR(qrId.toString());
        });
    }

    @Test
    void getValidateCodeQR_forNoAffiliateCertificate_shouldReturnCorrectQrDTO() {
        UUID qrId = UUID.randomUUID();
        String validatorCode = "NOAFFILIATE123";

        QrDocument qrDocument = new QrDocument();
        qrDocument.setName("certificado no afiliado");
        qrDocument.setIdentificationNumber(validatorCode);
        qrDocument.setIdentificationType("CC");
        qrDocument.setIssueDate(LocalDateTime.now());
        when(iQrRepository.findById(qrId)).thenReturn(Optional.of(qrDocument));
        when(certificateRepository.findByValidatorCode(validatorCode)).thenReturn(null);

        QrDTO result = certificateService.getValidateCodeQR(qrId.toString());

        assertEquals("certificado no afiliado", result.getName());
        assertEquals("certificado", result.getType());
        assertEquals(validatorCode, result.getData().get("cod_validacion"));
        assertEquals("certificado no afiliado", result.getData().get("nombre"));
        verify(iQrRepository).findById(qrId);
        verify(certificateRepository).findByValidatorCode(validatorCode);
    }

    @Test
    void getValidateCodeQR_forFormularioType_whenAffiliationFound_shouldReturnCorrectQrDTO() {
        UUID qrId = UUID.randomUUID();
        String filedNumber = "F12345";

        QrDocument qrDocument = new QrDocument();
        qrDocument.setName("formulario de afiliacion");
        qrDocument.setIdentificationNumber(filedNumber);
        qrDocument.setIssueDate(LocalDateTime.now());
        when(iQrRepository.findById(qrId)).thenReturn(Optional.of(qrDocument));

        Affiliation affiliation = new Affiliation();
        affiliation.setFirstName("John");
        affiliation.setSecondName("Fitzgerald");
        affiliation.setSurname("Kennedy");
        affiliation.setSecondSurname(null);
        affiliation.setIdentificationDocumentType("CC");
        affiliation.setIdentificationDocumentNumber("123456789");
        affiliation.setFiledNumber(filedNumber);
        when(affiliationRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.of(affiliation));

        QrDTO result = certificateService.getValidateCodeQR(qrId.toString());

        assertEquals("formulario de afiliacion", result.getName());
        assertEquals("formulario", result.getType());
        assertEquals(filedNumber, result.getData().get("cod_validacion"));
        verify(iQrRepository).findById(qrId);
        verify(affiliationRepository).findByFiledNumber(filedNumber);
    }

    @Test
    void saveMercantileCertificate_whenDataIsValid_shouldSaveAndReturnCertificate() throws Exception {
        // Arrange
        Long userPreRegisterId = 1L;
        Long economicActivityId = 10L;
        String documentNumber = "123456789";
        String documentType = "CC";
        String filedNumber = "F-CERT-001";
        String validationCode = "VC12345";

        Certificate certificate = new Certificate();

        Affiliate affiliate = new Affiliate();
        affiliate.setDocumentNumber(documentNumber);
        affiliate.setDocumentType(documentType);
        affiliate.setAffiliationType("Mercantil");
        affiliate.setCompany("Test Company");
        affiliate.setNitCompany("900123456-1");
        affiliate.setRetirementDate(null);
        affiliate.setAffiliationStatus("Activo");
        affiliate.setAffiliationDate(LocalDateTime.now().minusDays(5));

        UserMain userMain = new UserMain();
        userMain.setFirstName("John");
        userMain.setSurname("Doe");

        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setId(economicActivityId);
        economicActivity.setDescription("Test Activity");
        economicActivity.setClassRisk("IV");
        economicActivity.setCodeCIIU("1234");
        economicActivity.setAdditionalCode("A");

        AffiliateActivityEconomic primaryActivity = new AffiliateActivityEconomic();
        primaryActivity.setIsPrimary(true);
        primaryActivity.setActivityEconomic(economicActivity);

        AffiliateMercantile affiliation = new AffiliateMercantile();
        affiliation.setIdUserPreRegister(userPreRegisterId);
        affiliation.setEconomicActivity(Collections.singletonList(primaryActivity));

        when(iUserPreRegisterRepository.findById(userPreRegisterId)).thenReturn(Optional.of(userMain));
        when(economicActivityRepository.findById(economicActivityId)).thenReturn(Optional.of(economicActivity));
        when(codeValidCertificationService.consultCode(documentNumber, documentType)).thenReturn(validationCode);
        when(filedService.getNextFiledNumberCertificate()).thenReturn(filedNumber);
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        java.lang.reflect.Method method = CertificateServiceImpl.class.getDeclaredMethod("saveMercantileCertificate",
                Certificate.class, Affiliate.class, AffiliateMercantile.class);
        method.setAccessible(true);
        Certificate result = (Certificate) method.invoke(certificateService, certificate, affiliate, affiliation);

        // Assert
        verify(iUserPreRegisterRepository).findById(userPreRegisterId);
        verify(economicActivityRepository, org.mockito.Mockito.times(3)).findById(economicActivityId);
        verify(codeValidCertificationService).consultCode(documentNumber, documentType);
        verify(filedService).getNextFiledNumberCertificate();
        verify(certificateRepository, org.mockito.Mockito.times(2)).save(any(Certificate.class));

        assertEquals(documentType, result.getTypeDocument());
        assertEquals("John Doe", result.getName());
        assertEquals("Mercantil", result.getVinculationType());
        assertEquals("Test Company", result.getCompany());
        assertEquals("900123456-1", result.getNitContrator());
        assertEquals("Sin retiro", result.getRetirementDate());
        assertEquals("Activo", result.getStatus());
        assertEquals("IV", result.getRisk());
        assertEquals("Test Activity", result.getNameActivityEconomic());
        assertEquals("IV1234A", result.getCodeActivityEconomicPrimary());
        assertEquals(filedNumber, result.getFiledNumber());
    }

    @Test
    void saveIndependentAndDomesticCertificate_whenDataIsValid_shouldSaveAndReturnCertificate() throws Exception {
        // Arrange
        Certificate certificate = new Certificate();
        Affiliate affiliate = new Affiliate();
        affiliate.setDocumentNumber("123456789");
        affiliate.setDocumentType("CC");
        affiliate.setAffiliationType("Independiente");
        affiliate.setCompany("Test Company");
        affiliate.setNitCompany("900123456-1");
        affiliate.setRetirementDate(null);
        affiliate.setAffiliationStatus("Activo");
        affiliate.setAffiliationDate(LocalDateTime.now());
        affiliate.setCoverageStartDate(LocalDate.now().plusDays(1));

        Affiliation affiliation = new Affiliation();
        affiliation.setFirstName("John");
        affiliation.setSecondName("Fitzgerald");
        affiliation.setSurname("Kennedy");
        affiliation.setSecondSurname("Jr");
        affiliation.setOccupation("Developer");
        affiliation.setRisk("V");
        affiliation.setContractEndDate(LocalDate.now().plusYears(1));

        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setDescription("Software Development");
        economicActivity.setEconomicActivityCode("J6201");

        AffiliateActivityEconomic affiliateActivityEconomic = new AffiliateActivityEconomic();
        affiliateActivityEconomic.setIsPrimary(true);
        affiliateActivityEconomic.setActivityEconomic(economicActivity);
        affiliation.setEconomicActivity(Collections.singletonList(affiliateActivityEconomic));

        when(codeValidCertificationService.consultCode(anyString(), anyString())).thenReturn("VALIDCODE123");
        when(filedService.getNextFiledNumberCertificate()).thenReturn("F-CERT-2024-001");
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        java.lang.reflect.Method method = CertificateServiceImpl.class.getDeclaredMethod(
                "saveIndependentAndDomesticCertificate", Certificate.class, Affiliate.class, Affiliation.class);
        method.setAccessible(true);
        Certificate result = (Certificate) method.invoke(certificateService, certificate, affiliate, affiliation);

        // Assert
        assertEquals(affiliate.getDocumentType(), result.getTypeDocument());
        assertEquals("John Fitzgerald Kennedy Jr", result.getName());
        assertEquals(affiliate.getAffiliationType(), result.getVinculationType());
        assertEquals(affiliate.getCompany(), result.getCompany());
        assertEquals(affiliate.getNitCompany(), result.getNitContrator());
        assertEquals("Sin retiro", result.getRetirementDate());
        assertEquals(affiliate.getAffiliationStatus(), result.getStatus());
        assertEquals("F-CERT-2024-001", result.getFiledNumber());
        assertEquals(affiliation.getOccupation(), result.getPosition());
        assertEquals(affiliation.getRisk(), result.getRisk());
        assertEquals("Software Development", result.getNameActivityEconomic());
        assertEquals("J6201", result.getCodeActivityEconomicPrimary());
        assertEquals(affiliation.getContractEndDate().toString(), result.getEndContractDate());

        verify(filedService).getNextFiledNumberCertificate();
        verify(certificateRepository, org.mockito.Mockito.times(2)).save(any(Certificate.class));
    }

    @Test
    void createCertificate_whenNoAffiliationFound_shouldThrowException() {
        // Arrange
        String addressedTo = "Fonodo Nacional del Ahorro";
        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("F-UNKNOWN-001");
        affiliate.setDocumentNumber("99999");
        affiliate.setDocumentType("CC");

        ArlInformation arlInfo = new ArlInformation();
        arlInfo.setName("ARL Test");
        arlInfo.setNit("900123456-1");

        when(arlInformationDao.findAllArlInformation()).thenReturn(Collections.singletonList(arlInfo));
        when(affiliationRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliationDependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            certificateService.createCertificate(affiliate, addressedTo);
        });

        assertEquals(AffiliateNotFoundException.class, exception.getCause().getClass());

        verify(arlInformationDao).findAllArlInformation();
        verify(affiliationRepository).findOne(any(Specification.class));
        verify(affiliateMercantileRepository).findOne(any(Specification.class));
        verify(affiliationDependentRepository).findOne(any(Specification.class));
        verify(certificateRepository, never()).save(any(Certificate.class));
    }
}
