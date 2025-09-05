package com.gal.afiliaciones.application.service.affiliate.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.Department;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.legalnature.LegalNature;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MainOfficeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.WorkCenterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.domesticservicesaffiliation.economicactivity.AffiliationEconomicActivityDao;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.legalnature.LegalNatureRepository;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateReportRequestDTO;

import reactor.core.publisher.Mono;

class MercantileFormServiceImplTest {

    @Mock private CollectProperties properties;
    @Mock private AlfrescoService alfrescoService;
    @Mock private GenericWebClient genericWebClient;
    @Mock private AffiliateRepository affiliateRepository;
    @Mock private MainOfficeRepository mainOfficeRepository;
    @Mock private WorkCenterRepository workCenterRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private IUserPreRegisterRepository iUserPreRegisterRepository;
    @Mock private IEconomicActivityRepository iEconomicActivityRepository;
    @Mock private AffiliateMercantileRepository affiliateMercantileRepository;
    @Mock private MunicipalityRepository municipalityRepository;
    @Mock private AffiliationEconomicActivityDao affiliationEconomicActivityDao;
    @Mock private LegalNatureRepository legalNatureRepository;
    @Mock private FiledService filedService;

    @InjectMocks
    private MercantileFormServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void reportPDF_shouldReturnPdfUrl_whenDataIsValid() {
        Long idAffiliation = 1L;
        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("F123");
        AffiliateMercantile affiliateMercantile = mockAffiliateMercantile();
        MainOffice mainOffice = mockMainOffice();
        UserMain userMain = mockUserMain();
        LegalNature legalNature = new LegalNature();
        legalNature.setId(10L);

        when(affiliateRepository.findById(idAffiliation)).thenReturn(Optional.of(affiliate));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));
        when(filedService.getNextFiledNumberForm()).thenReturn("DOC-001");
        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(mainOfficeRepository.findById(anyLong())).thenReturn(Optional.of(mainOffice));
        when(legalNatureRepository.findByDescription(anyString())).thenReturn(legalNature);
        when(departmentRepository.findById(anyLong())).thenReturn(Optional.of(mockDepartment()));
        when(municipalityRepository.findById(anyLong())).thenReturn(Optional.of(mockMunicipality()));
        when(genericWebClient.generateReportCertificate(any(CertificateReportRequestDTO.class))).thenReturn("http://pdf-url");
        when(genericWebClient.getFileBase64(anyString())).thenReturn(Mono.just("base64signature"));
        when(alfrescoService.getIdDocumentsFolder(anyString())).thenReturn(Optional.empty());

        String result = service.reportPDF(idAffiliation);

        assertEquals("http://pdf-url", result);
        verify(genericWebClient).generateReportCertificate(any(CertificateReportRequestDTO.class));
    }

    @Test
    void reportPDF_shouldThrowAffiliationError_whenAffiliateNotFound() {
        when(affiliateRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(AffiliationError.class, () -> service.reportPDF(1L));
    }

    @Test
    void reportPDF_shouldThrowAffiliationError_whenAffiliateMercantileNotFound() {
        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("F123");
        when(affiliateRepository.findById(anyLong())).thenReturn(Optional.of(affiliate));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        assertThrows(AffiliationError.class, () -> service.reportPDF(1L));
    }

    @Test
    void defaultIfNullOrEmpty_shouldReturnNA_whenNullOrEmpty() {
        assertEquals("N/A", MercantileFormServiceImpl.defaultIfNullOrEmpty(null));
        assertEquals("N/A", MercantileFormServiceImpl.defaultIfNullOrEmpty(""));
        assertEquals("value", MercantileFormServiceImpl.defaultIfNullOrEmpty("value"));
    }

    @Test
    void capitalize_shouldCapitalizeFirstLetter() {
        assertEquals("Test", MercantileFormServiceImpl.capitalize("test"));
        assertEquals("T", MercantileFormServiceImpl.capitalize("T"));
        assertEquals("", MercantileFormServiceImpl.capitalize(""));
        assertEquals("", MercantileFormServiceImpl.capitalize(null));
    }

    // --- Helper mocks ---

    private AffiliateMercantile mockAffiliateMercantile() {
        AffiliateMercantile am = new AffiliateMercantile();
        am.setFiledNumber("F123");
        am.setIdUserPreRegister(2L);
        am.setIdMainHeadquarter(3L);
        am.setLegalStatus("Sociedad");
        am.setIdProcedureType(1L);
        am.setBusinessName("Empresa S.A.");
        am.setTypeDocumentIdentification("NIT");
        am.setNumberIdentification("900123456");
        am.setDigitVerificationDV(5);
        am.setTypeDocumentPersonResponsible("CC");
        am.setNumberDocumentPersonResponsible("123456789");
        am.setNumberWorkers(10L);

        AffiliateActivityEconomic activity = new AffiliateActivityEconomic();
        activity.setIsPrimary(true);
        EconomicActivity economic = new EconomicActivity();
        economic.setDescription("Comercio");
        economic.setClassRisk("2");
        economic.setCodeCIIU("A123");
        economic.setAdditionalCode("B");
        activity.setActivityEconomic(economic);
        activity.setIdWorkCenter(4L);

        am.setEconomicActivity(List.of(activity));
        return am;
    }

    private MainOffice mockMainOffice() {
        MainOffice mo = new MainOffice();
        mo.setCode("100");
        mo.setMainOfficeName("Principal");
        mo.setAddress("Calle 1");
        mo.setIdDepartment(5L);
        mo.setIdCity(6L);
        mo.setMainOfficeZone("Urbana");
        mo.setMainOfficePhoneNumber("1234567");
        mo.setMainOfficeEmail("main@empresa.com");

        UserMain manager = mockUserMain();
        mo.setOfficeManager(manager);
        return mo;
    }

    private UserMain mockUserMain() {
        UserMain um = new UserMain();
        um.setFirstName("Juan");
        um.setSecondName("Carlos");
        um.setSurname("Pérez");
        um.setSecondSurname("Gómez");
        um.setEmail("juan@empresa.com");
        um.setIdentificationType("CC");
        um.setIdentification("123456789");
        return um;
    }

    private Department mockDepartment() {
        Department d = new Department();
        d.setDepartmentName("Cundinamarca");
        return d;
    }

    private Municipality mockMunicipality() {
        Municipality m = new Municipality();
        m.setMunicipalityName("Bogotá");
        m.setDivipolaCode("11001");
        return m;
    }

    // Mono mock for reactive return
    static class MonoJustMock<T> {
        static <T> MonoJustMock<T> just(T value) {
            return new MonoJustMock<>();
        }
        T block() { return (T) "base64signature"; }
    }
}
