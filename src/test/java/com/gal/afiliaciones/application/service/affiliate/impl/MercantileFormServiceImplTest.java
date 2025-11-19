package com.gal.afiliaciones.application.service.affiliate.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gal.afiliaciones.application.service.affiliate.WorkCenterService;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.*;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.legalnature.LegalNature;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MainOfficeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.legalnature.LegalNatureRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateReportRequestDTO;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

@ContextConfiguration(classes = {MercantileFormServiceImpl.class})
@DisabledInAotMode
@ExtendWith(SpringExtension.class)
class MercantileFormServiceImplTest {
    @MockBean
    private AffiliateMercantileRepository affiliateMercantileRepository;

    @MockBean
    private AffiliateRepository affiliateRepository;

    @MockBean
    private CollectProperties collectProperties;

    @MockBean
    private DepartmentRepository departmentRepository;

    @MockBean
    private FiledService filedService;

    @MockBean
    private GenericWebClient genericWebClient;

    @MockBean
    private IUserPreRegisterRepository iUserPreRegisterRepository;

    @MockBean
    private LegalNatureRepository legalNatureRepository;

    @MockBean
    private MainOfficeRepository mainOfficeRepository;

    @Autowired
    private MercantileFormServiceImpl mercantileFormServiceImpl;

    @MockBean
    private MunicipalityRepository municipalityRepository;

    @MockBean
    private WorkCenterService workCenterService;

    @Mock
    private AlfrescoService alfrescoService;

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

        String result = mercantileFormServiceImpl.reportPDF(idAffiliation);

        assertEquals("http://pdf-url", result);
        verify(genericWebClient).generateReportCertificate(any(CertificateReportRequestDTO.class));
    }

    @Test
    void reportPDF_shouldThrowAffiliationError_whenAffiliateNotFound() {
        when(affiliateRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(AffiliationError.class, () -> mercantileFormServiceImpl.reportPDF(1L));
    }

    @Test
    void reportPDF_shouldThrowAffiliationError_whenAffiliateMercantileNotFound() {
        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("F123");
        when(affiliateRepository.findById(anyLong())).thenReturn(Optional.of(affiliate));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        assertThrows(AffiliationError.class, () -> mercantileFormServiceImpl.reportPDF(1L));
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
        static <T> MercantileFormServiceImplTest.MonoJustMock<T> just(T value) {
            return new MercantileFormServiceImplTest.MonoJustMock<>();
        }
        T block() { return (T) "base64signature"; }
    }

    /**
     * Test {@link MercantileFormServiceImpl#reportPDF(Long)}.
     *
     * <p>Method under test: {@link MercantileFormServiceImpl#reportPDF(Long)}
     */
    @Test
    @DisplayName("Test reportPDF(Long)")

    void testReportPDF() {
        // Arrange
        when(affiliateRepository.findById(Mockito.<Long>any()))
                .thenThrow(new ResourceNotFoundException("An error occurred"));

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> mercantileFormServiceImpl.reportPDF(1L));
        verify(affiliateRepository).findById(1L);
    }

    /**
     * Test {@link MercantileFormServiceImpl#reportPDF(Long)}.
     *
     * <ul>
     *   <li>Given {@link Operator#Operator()} Id is one.
     *   <li>Then calls {@link AffiliateMercantileRepository#findOne(Specification)}.
     * </ul>
     *
     * <p>Method under test: {@link MercantileFormServiceImpl#reportPDF(Long)}
     */
    @Test
    @DisplayName(
            "Test reportPDF(Long); given Operator() Id is one; then calls findOne(Specification)")

    void testReportPDF_givenOperatorIdIsOne_thenCallsFindOne() {
        // Arrange
        Operator financialOperator = new Operator();
        financialOperator.setId(1L);
        financialOperator.setNi("Ni");
        financialOperator.setOperatorCode(1L);
        financialOperator.setOperatorName("Operator Name");
        financialOperator.setOperatorType("Operator Type");

        Operator InfoOperator = new Operator();
        InfoOperator.setId(1L);
        InfoOperator.setNi("Ni");
        InfoOperator.setOperatorCode(1L);
        InfoOperator.setOperatorName("Operator Name");
        InfoOperator.setOperatorType("Operator Type");

        UserMain assignTo = new UserMain();
        assignTo.setAcceptNotification(true);
        assignTo.setAddress("42 Main St");
        assignTo.setAge(1);
        assignTo.setArea(1L);
        assignTo.setAssignedPassword(true);
        assignTo.setCodeOtp("Code Otp");
        assignTo.setCompanyName("Company Name");
        assignTo.setCreateDate(null);
        assignTo.setCreatedAtTemporalPassword(LocalDate.of(1970, 1, 1));
        assignTo.setDateBirth(LocalDate.of(1970, 1, 1));
        assignTo.setEmail("jane.doe@example.org");
        assignTo.setEmployerUpdateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        assignTo.setFinancialOperator(financialOperator);
        assignTo.setFirstName("Jane");
        assignTo.setGenerateAttempts(1);
        assignTo.setGenerateOutTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        assignTo.setHealthPromotingEntity(1L);
        assignTo.setId(1L);
        assignTo.setIdCardinalPoint2(1L);
        assignTo.setIdCardinalPointMainStreet(1L);
        assignTo.setIdCity(1L);
        assignTo.setIdDepartment(1L);
        assignTo.setIdHorizontalProperty1(1L);
        assignTo.setIdHorizontalProperty2(1L);
        assignTo.setIdHorizontalProperty3(1L);
        assignTo.setIdHorizontalProperty4(1L);
        assignTo.setIdLetter1MainStreet(1L);
        assignTo.setIdLetter2MainStreet(1L);
        assignTo.setIdLetterSecondStreet(1L);
        assignTo.setIdMainStreet(1L);
        assignTo.setIdNum1SecondStreet(1L);
        assignTo.setIdNum2SecondStreet(1L);
        assignTo.setIdNumHorizontalProperty1(1L);
        assignTo.setIdNumHorizontalProperty2(1L);
        assignTo.setIdNumHorizontalProperty3(1L);
        assignTo.setIdNumHorizontalProperty4(1L);
        assignTo.setIdNumberMainStreet(1L);
        assignTo.setIdentification("Identification");
        assignTo.setIdentificationType("Identification Type");
        assignTo.setInactiveByPendingAffiliation(true);
        assignTo.setInfoOperator(InfoOperator);
        assignTo.setIsBis(true);
        assignTo.setIsImport(true);
        assignTo.setIsInArrearsStatus(true);
        assignTo.setIsPasswordExpired(true);
        assignTo.setIsTemporalPassword(true);
        assignTo.setLastAffiliationAttempt(LocalDate.of(1970, 1, 1).atStartOfDay());
        assignTo.setLastPasswordUpdate(LocalDate.of(1970, 1, 1).atStartOfDay());
        assignTo.setLastUpdate(LocalDate.of(1970, 1, 1).atStartOfDay());
        assignTo.setLevelAuthorization("JaneDoe");
        assignTo.setLockoutTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        assignTo.setLoginAttempts(1);
        assignTo.setNationality(1L);
        assignTo.setOffice(1);
        assignTo.setOtherSex("Other Sex");
        assignTo.setPensionFundAdministrator(1L);
        assignTo.setPhoneNumber("6625550144");
        assignTo.setPhoneNumber2("6625550144");
        assignTo.setPin("Pin");
        assignTo.setPosition(1);
        assignTo.setProfile("Profile");
        assignTo.setRoles(new ArrayList<>());
        assignTo.setSecondName("Second Name");
        assignTo.setSecondSurname("Doe");
        assignTo.setSex("Sex");
        assignTo.setStatus(1L);
        assignTo.setStatusActive(true);
        assignTo.setStatusInactiveSince(LocalDate.of(1970, 1, 1).atStartOfDay());
        assignTo.setStatusPreRegister(true);
        assignTo.setStatusStartAfiiliate(true);
        assignTo.setSurname("Doe");
        assignTo.setUserName("janedoe");
        assignTo.setUserType(1L);
        assignTo.setValidAttempts(1);
        assignTo.setValidOutTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        assignTo.setVerificationDigit(1);

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(true);
        affiliate.setAffiliationDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setAffiliationStatus("Affiliation Status");
        affiliate.setAffiliationSubType("Affiliation Sub Type");
        affiliate.setAffiliationType("Affiliation Type");
        affiliate.setAssignTo(assignTo);
        affiliate.setCompany("Company");
        affiliate.setCoverageStartDate(LocalDate.of(1970, 1, 1));
        affiliate.setDateAffiliateSuspend(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliate.setDependents(new ArrayList<>());
        affiliate.setDocumenTypeCompany("Documen Type Company");
        affiliate.setDocumentNumber("42");
        affiliate.setDocumentType("Document Type");
        affiliate.setFiledNumber("42");
        affiliate.setIdAffiliate(1L);
        affiliate.setIdOfficial(1L);
        affiliate.setNitCompany("Nit Company");
        affiliate.setNoveltyType("Novelty Type");
        affiliate.setObservation("Observation");
        affiliate.setPosition("Position");
        affiliate.setRequestChannel(1L);
        affiliate.setRetirementDate(LocalDate.of(1970, 1, 1));
        affiliate.setRisk("Risk");
        affiliate.setStatusDocument(true);
        affiliate.setUserId(1L);
        Optional<Affiliate> ofResult = Optional.of(affiliate);
        when(affiliateRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenThrow(new ResourceNotFoundException("An error occurred"));

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> mercantileFormServiceImpl.reportPDF(1L));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
        verify(affiliateRepository).findById(1L);
    }

    /**
     * Test {@link MercantileFormServiceImpl#defaultIfNullOrEmpty(String)}.
     *
     * <ul>
     *   <li>When {@code 42}.
     *   <li>Then return {@code 42}.
     * </ul>
     *
     * <p>Method under test: {@link MercantileFormServiceImpl#defaultIfNullOrEmpty(String)}
     */
    @Test
    @DisplayName("Test defaultIfNullOrEmpty(String); when '42'; then return '42'")

    void testDefaultIfNullOrEmpty_when42_thenReturn42() {
        // Arrange, Act and Assert
        assertEquals("42", MercantileFormServiceImpl.defaultIfNullOrEmpty("42"));
    }

    /**
     * Test {@link MercantileFormServiceImpl#defaultIfNullOrEmpty(String)}.
     *
     * <ul>
     *   <li>When empty string.
     *   <li>Then return {@code N/A}.
     * </ul>
     *
     * <p>Method under test: {@link MercantileFormServiceImpl#defaultIfNullOrEmpty(String)}
     */
    @Test
    @DisplayName("Test defaultIfNullOrEmpty(String); when empty string; then return 'N/A'")

    void testDefaultIfNullOrEmpty_whenEmptyString_thenReturnNA() {
        // Arrange, Act and Assert
        assertEquals("N/A", MercantileFormServiceImpl.defaultIfNullOrEmpty(""));
    }

    /**
     * Test {@link MercantileFormServiceImpl#defaultIfNullOrEmpty(String)}.
     *
     * <ul>
     *   <li>When {@code null}.
     *   <li>Then return {@code N/A}.
     * </ul>
     *
     * <p>Method under test: {@link MercantileFormServiceImpl#defaultIfNullOrEmpty(String)}
     */
    @Test
    @DisplayName("Test defaultIfNullOrEmpty(String); when 'null'; then return 'N/A'")

    void testDefaultIfNullOrEmpty_whenNull_thenReturnNA() {
        // Arrange, Act and Assert
        assertEquals("N/A", MercantileFormServiceImpl.defaultIfNullOrEmpty(null));
    }

    /**
     * Test {@link MercantileFormServiceImpl#capitalize(String)}.
     *
     * <ul>
     *   <li>When empty string.
     *   <li>Then return empty string.
     * </ul>
     *
     * <p>Method under test: {@link MercantileFormServiceImpl#capitalize(String)}
     */
    @Test
    @DisplayName("Test capitalize(String); when empty string; then return empty string")

    void testCapitalize_whenEmptyString_thenReturnEmptyString() {
        // Arrange, Act and Assert
        assertEquals("", MercantileFormServiceImpl.capitalize(""));
    }

    /**
     * Test {@link MercantileFormServiceImpl#capitalize(String)}.
     *
     * <ul>
     *   <li>When {@code Input String}.
     *   <li>Then return {@code Input string}.
     * </ul>
     *
     * <p>Method under test: {@link MercantileFormServiceImpl#capitalize(String)}
     */
    @Test
    @DisplayName("Test capitalize(String); when 'Input String'; then return 'Input string'")

    void testCapitalize_whenInputString_thenReturnInputString() {
        // Arrange, Act and Assert
        assertEquals("Input string", MercantileFormServiceImpl.capitalize("Input String"));
    }

    /**
     * Test {@link MercantileFormServiceImpl#capitalize(String)}.
     *
     * <ul>
     *   <li>When {@code null}.
     *   <li>Then return empty string.
     * </ul>
     *
     * <p>Method under test: {@link MercantileFormServiceImpl#capitalize(String)}
     */
    @Test
    @DisplayName("Test capitalize(String); when 'null'; then return empty string")

    void testCapitalize_whenNull_thenReturnEmptyString() {
        // Arrange, Act and Assert
        assertEquals("", MercantileFormServiceImpl.capitalize(null));
    }
}
