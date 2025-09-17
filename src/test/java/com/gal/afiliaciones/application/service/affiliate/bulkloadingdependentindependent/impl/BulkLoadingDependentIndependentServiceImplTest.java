package com.gal.afiliaciones.application.service.affiliate.bulkloadingdependentindependent.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MainOfficeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliate.recordloadbulk.RecordLoadBulkService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.policy.PolicyService;
import com.gal.afiliaciones.application.service.risk.RiskFeeService;
import com.gal.afiliaciones.application.service.workingday.WorkingDayService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.config.util.MessageErrorAge;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.DataExcelDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.DataExcelIndependentDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

@ExtendWith(MockitoExtension.class)
class BulkLoadingDependentIndependentServiceImplTest {

    @InjectMocks
    private BulkLoadingDependentIndependentServiceImpl service;

    @Mock
    private SendEmails sendEmails;
    @Mock
    private FiledService filedService;
    @Mock
    private CollectProperties properties;
    @Mock
    private RiskFeeService riskFeeService;
    @Mock
    private MessageErrorAge messageErrorAge;
    @Mock
    private AlfrescoService alfrescoService;
    @Mock
    private AffiliateService affiliateService;
    @Mock
    private GenericWebClient genericWebClient;
    @Mock
    private WorkingDayService workingDayService;
    @Mock
    private MainOfficeRepository mainOfficeRepository;
    @Mock
    private AffiliateRepository affiliateRepository;
    @Mock
    private OccupationRepository occupationRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private RecordLoadBulkService recordLoadBulkService;
    @Mock
    private MunicipalityRepository municipalityRepository;
    @Mock
    private AffiliationDependentRepository dependentRepository;
    @Mock
    private GenerateCardAffiliatedService cardAffiliatedService;
    @Mock
    private ExcelProcessingServiceData excelProcessingServiceData;
    @Mock
    private IUserPreRegisterRepository iUserPreRegisterRepository;
    @Mock
    private IEconomicActivityRepository iEconomicActivityRepository;
    @Mock
    private AffiliateMercantileRepository affiliateMercantileRepository;
    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository domesticServiceIndependentRepository;
    @Mock
    private PolicyRepository policyRepository;
    @Mock
    private PolicyService policyService;

    @Mock
    private MultipartFile mockFile;

    private UserMain userMain;
    private Affiliate affiliate;
    private SalaryDTO salaryDTO;

    private static final String DATE_FORMAT_STRING = "dd/MM/yyyy";

    @BeforeEach
    void setUp() {
        userMain = new UserMain();
        userMain.setId(1L);
        userMain.setIdentification("123456789");
        userMain.setIdentificationType("CC");

        affiliate = new Affiliate();
        affiliate.setIdAffiliate(123L);
        affiliate.setFiledNumber("F123");
        affiliate.setNitCompany("987654321");
        affiliate.setDocumentNumber("123456789");
        affiliate.setDocumentType("CC");

        salaryDTO = new SalaryDTO();
        salaryDTO.setValue(1300000L);

        service.allActivities = new ArrayList<>();
        service.allMunicipality = new ArrayList<>();
    }

    @Test
    @DisplayName("dataFile - Should throw error for wrong bonding type")
    void dataFile_shouldThrowErrorForWrongBondingType() {
        AffiliationError exception = assertThrows(AffiliationError.class, () -> {
            service.dataFile(mockFile, "WRONG_TYPE", 1L, 123L);
        });
        assertTrue(true);
    }

    @Test
    @DisplayName("dataFile - Should throw error when user not found")
    void dataFile_shouldThrowErrorWhenUserNotFound() {
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.empty());
        AffiliationError exception = assertThrows(AffiliationError.class, () -> {
            service.dataFile(mockFile, Constant.TYPE_AFFILLATE_DEPENDENT, 1L, 123L);
        });
        assertTrue(true);
    }

    @Test
    @DisplayName("dataFile - Should throw error for invalid file type")
    void dataFile_shouldThrowErrorForInvalidFileType() {
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findByIdAffiliate(123L)).thenReturn(Optional.of(affiliate));
        when(mockFile.getContentType()).thenReturn("application/pdf");
        when(mockFile.isEmpty()).thenReturn(false);

        AffiliationError exception = assertThrows(AffiliationError.class, () -> {
            service.dataFile(mockFile, Constant.TYPE_AFFILLATE_DEPENDENT, 1L, 123L);
        });
        assertTrue(true);
    }

    @Test
    @DisplayName("dataFileWithNumber - Should throw error when affiliate not found")
    void dataFileWithNumber_shouldThrowErrorWhenAffiliateNotFound() {
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        AffiliationError exception = assertThrows(AffiliationError.class, () -> {
            service.dataFileWithNumber(mockFile, "CC", "123", "CC", 1L);
        });
        assertTrue(true);
    }

    @Test
    @DisplayName("dataFileWithNumber - Should throw error when legal representative not found")
    void dataFileWithNumber_shouldThrowErrorWhenLegalRepNotFound() {
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setStageManagement(Constant.ACCEPT_AFFILIATION);
        affiliateMercantile.setTypeDocumentPersonResponsible("CC");
        affiliateMercantile.setNumberDocumentPersonResponsible("123");

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliateMercantile));
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        AffiliationError exception = assertThrows(AffiliationError.class, () -> {
            service.dataFileWithNumber(mockFile, "CC", "123", "CC", 1L);
        });
        assertTrue(true);
    }

    @Test
    @DisplayName("getTemplateByBondingType - Should return independent template id")
    void getTemplateByBondingType_shouldReturnIndependentTemplateId() {
        when(properties.getIdTemplateIndependent()).thenReturn("id_independent");
        when(alfrescoService.getDocument("id_independent")).thenReturn("base64_independent_doc");

        String result = service.getTemplateByBondingType("INDEPENDIENTE");

        assertEquals("base64_independent_doc", result);
        verify(properties, times(1)).getIdTemplateIndependent();
        verify(alfrescoService, times(1)).getDocument("id_independent");
    }

    @Test
    @DisplayName("getTemplateByBondingType - Should return dependent template id")
    void getTemplateByBondingType_shouldReturnDependentTemplateId() {
        when(properties.getIdTemplateDependent()).thenReturn("id_dependent");
        when(alfrescoService.getDocument("id_dependent")).thenReturn("base64_dependent_doc");

        String result = service.getTemplateByBondingType("DEPENDIENTE");

        assertEquals("base64_dependent_doc", result);
        verify(properties, times(1)).getIdTemplateDependent();
        verify(alfrescoService, times(1)).getDocument("id_dependent");
    }

    @Test
    @DisplayName("getTemplateByBondingType - Should throw error for empty bonding type")
    void getTemplateByBondingType_shouldThrowErrorForEmptyBondingType() {
        AffiliationError exception = assertThrows(AffiliationError.class, () -> {
            service.getTemplateByBondingType("");
        });
        assertTrue(true);
    }

    @Test
    @DisplayName("downloadTemplateGuide - Should return guide document")
    void downloadTemplateGuide_shouldReturnGuideDocument() {
        when(properties.getIdTemplateGuide()).thenReturn("id_guide");
        when(alfrescoService.getDocument("id_guide")).thenReturn("base64_guide_doc");

        String result = service.downloadTemplateGuide();

        assertEquals("base64_guide_doc", result);
        verify(properties, times(1)).getIdTemplateGuide();
        verify(alfrescoService, times(1)).getDocument("id_guide");
    }

    @Test
    @DisplayName("consultAffiliation - Should return business name on success")
    void consultAffiliation_shouldReturnBusinessNameOnSuccess() {
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setStageManagement(Constant.ACCEPT_AFFILIATION);
        affiliateMercantile.setBusinessName("Test Business");

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliateMercantile));

        String result = service.consultAffiliation("CC", "123");

        assertEquals("Test Business", result);
    }

    @Test
    @DisplayName("consultAffiliation - Should throw error if affiliation not accepted")
    void consultAffiliation_shouldThrowErrorIfNotAccepted() {
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setStageManagement("PENDING");

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliateMercantile));

        AffiliationError exception = assertThrows(AffiliationError.class, () -> {
            service.consultAffiliation("CC", "123");
        });
        assertTrue(true);
    }

    @Test
    @DisplayName("consultAffiliation - Should throw error if mercantile affiliation not found")
    void consultAffiliation_shouldThrowErrorIfMercantileNotFound() {
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        AffiliationError exception = assertThrows(AffiliationError.class, () -> {
            service.consultAffiliation("CC", "123");
        });
        assertTrue(true);
    }

    private AffiliationDependent invokeConvertDataAffiliationIndependent(DataExcelIndependentDTO dto, UserMain user) {
        try {
            Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
                    "convertDataAffiliationIndependent", DataExcelIndependentDTO.class, UserMain.class);
            method.setAccessible(true);
            return (AffiliationDependent) method.invoke(service, dto, user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private BigDecimal findExpectedSalary(DataExcelIndependentDTO dto) {
        LocalDate start = LocalDate.parse(dto.getStartDate(), DateTimeFormatter.ofPattern(DATE_FORMAT_STRING));
        LocalDate end = LocalDate.parse(dto.getEndDate(), DateTimeFormatter.ofPattern(DATE_FORMAT_STRING));
        BigDecimal total = new BigDecimal(dto.getContractTotalValue());
        int months = Period.between(start, end).getMonths();
        return months >= 1 ? total.divide(new BigDecimal(months), RoundingMode.HALF_UP) : null;
    }

    private int findExpectedRisk(DataExcelIndependentDTO dto) {
        return 4; // dummyActivity classRisk
    }

    private String findExpectedJourneyName(DataExcelIndependentDTO dto) {
        // Will return null unless workingDayService is stubbed
        return null;
    }

    private AffiliationDependent invokeConvertDataAffiliationDependent(DataExcelDependentDTO dto, UserMain user) {
        try {
            Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
                    "convertDataAffiliationDependent", DataExcelDependentDTO.class, UserMain.class);
            method.setAccessible(true);
            return (AffiliationDependent) method.invoke(service, dto, user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("validActivityEconomicDependent - With AffiliateMercantile, economic activity found - Should return true")
    void validActivityEconomicDependent_withAffiliateMercantile_economicActivityFound_shouldReturnTrue()
            throws Exception {
        // Arrange
        String codeActivity = "513";

        // Create EconomicActivity
        EconomicActivity activity = new EconomicActivity();
        activity.setId(513L);
        activity.setClassRisk("5");
        activity.setCodeCIIU("1");
        activity.setAdditionalCode("3");

        // Create AffiliateActivityEconomic for the AffiliateMercantile
        AffiliateActivityEconomic affiliateActivity = new AffiliateActivityEconomic();
        affiliateActivity.setActivityEconomic(activity);

        // Create AffiliateMercantile with the activity
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setEconomicActivity(List.of(affiliateActivity));

        // Setup service's activities list
        service.allActivities = List.of(activity);

        // Act - Invoke the private method using reflection
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
                "validActivityEconomicDependent", String.class, Object.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(service, codeActivity, affiliateMercantile);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("validActivityEconomicDependent - With AffiliateMercantile, economic activity not in list - Should return false")
    void validActivityEconomicDependent_withAffiliateMercantile_economicActivityNotInList_shouldReturnFalse()
            throws Exception {
        // Arrange
        String codeActivity = "513";

        // Create EconomicActivity for the service's list
        EconomicActivity activity = new EconomicActivity();
        activity.setId(513L);
        activity.setClassRisk("5");
        activity.setCodeCIIU("1");
        activity.setAdditionalCode("3");

        // Create different EconomicActivity for the affiliation
        EconomicActivity differentActivity = new EconomicActivity();
        differentActivity.setId(400L);
        differentActivity.setClassRisk("4");
        differentActivity.setCodeCIIU("0");
        differentActivity.setAdditionalCode("0");

        // Create AffiliateActivityEconomic with the different activity
        AffiliateActivityEconomic affiliateActivity = new AffiliateActivityEconomic();
        affiliateActivity.setActivityEconomic(differentActivity);

        // Create AffiliateMercantile with the different activity
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setEconomicActivity(List.of(affiliateActivity));

        // Setup service's activities list
        service.allActivities = List.of(activity);

        // Act - Invoke the private method using reflection
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
                "validActivityEconomicDependent", String.class, Object.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(service, codeActivity, affiliateMercantile);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("validActivityEconomicDependent - With Affiliation, economic activity found - Should return true")
    void validActivityEconomicDependent_withAffiliation_economicActivityFound_shouldReturnTrue() throws Exception {
        // Arrange
        String codeActivity = "513";

        // Create EconomicActivity
        EconomicActivity activity = new EconomicActivity();
        activity.setId(513L);
        activity.setClassRisk("5");
        activity.setCodeCIIU("1");
        activity.setAdditionalCode("3");

        // Create AffiliateActivityEconomic for the Affiliation
        AffiliateActivityEconomic affiliateActivity = new AffiliateActivityEconomic();
        affiliateActivity.setActivityEconomic(activity);

        // Create Affiliation with the activity
        Affiliation affiliation = new Affiliation();
        affiliation.setEconomicActivity(List.of(affiliateActivity));

        // Setup service's activities list
        service.allActivities = List.of(activity);

        // Act - Invoke the private method using reflection
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
                "validActivityEconomicDependent", String.class, Object.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(service, codeActivity, affiliation);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("validActivityEconomicDependent - With Affiliation, economic activity not in list - Should return false")
    void validActivityEconomicDependent_withAffiliation_economicActivityNotInList_shouldReturnFalse() throws Exception {
        // Arrange
        String codeActivity = "513";

        // Create EconomicActivity for the service's list
        EconomicActivity activity = new EconomicActivity();
        activity.setId(513L);
        activity.setClassRisk("5");
        activity.setCodeCIIU("1");
        activity.setAdditionalCode("3");

        // Create different EconomicActivity for the affiliation
        EconomicActivity differentActivity = new EconomicActivity();
        differentActivity.setId(400L);
        differentActivity.setClassRisk("4");
        differentActivity.setCodeCIIU("0");
        differentActivity.setAdditionalCode("0");

        // Create AffiliateActivityEconomic with the different activity
        AffiliateActivityEconomic affiliateActivity = new AffiliateActivityEconomic();
        affiliateActivity.setActivityEconomic(differentActivity);

        // Create Affiliation with the different activity
        Affiliation affiliation = new Affiliation();
        affiliation.setEconomicActivity(List.of(affiliateActivity));

        // Setup service's activities list
        service.allActivities = List.of(activity);

        // Act - Invoke the private method using reflection
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
                "validActivityEconomicDependent", String.class, Object.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(service, codeActivity, affiliation);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("validActivityEconomicDependent - Economic activity not found - Should return false")
    void validActivityEconomicDependent_economicActivityNotFound_shouldReturnFalse() throws Exception {
        // Arrange
        String codeActivity = "513";

        // Create AffiliateMercantile
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setEconomicActivity(new ArrayList<>());

        // Setup service's activities list - empty to simulate activity not found
        service.allActivities = new ArrayList<>();

        // Act - Invoke the private method using reflection
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
                "validActivityEconomicDependent", String.class, Object.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(service, codeActivity, affiliateMercantile);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("validActivityEconomicDependent - Affiliation null - Should throw AffiliationError")
    void validActivityEconomicDependent_affiliationNull_shouldThrowAffiliationError() throws Exception {
        // Arrange
        String codeActivity = "513";

        // Create EconomicActivity
        EconomicActivity activity = new EconomicActivity();
        activity.setId(513L);
        activity.setClassRisk("5");
        activity.setCodeCIIU("1");
        activity.setAdditionalCode("3");

        // Setup service's activities list
        service.allActivities = List.of(activity);

        // Act & Assert - Invoke the private method using reflection
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
                "validActivityEconomicDependent", String.class, Object.class);
        method.setAccessible(true);

        // We need to handle the InvocationTargetException that wraps the
        // AffiliationError
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(service, codeActivity, null);
        });

        // Check that the cause is AffiliationError
        assertTrue(exception.getCause() instanceof AffiliationError);
    }

    @Test
    @DisplayName("validActivityEconomicDependent - Invalid affiliation type - Should throw AffiliationError")
    void validActivityEconomicDependent_invalidAffiliationType_shouldThrowAffiliationError() throws Exception {
        // Arrange
        String codeActivity = "513";

        // Create an object of an invalid type (not AffiliateMercantile or Affiliation)
        Object invalidObject = new Object();

        // Create EconomicActivity
        EconomicActivity activity = new EconomicActivity();
        activity.setId(513L);
        activity.setClassRisk("5");
        activity.setCodeCIIU("1");
        activity.setAdditionalCode("3");

        // Setup service's activities list
        service.allActivities = List.of(activity);

        // Act & Assert - Invoke the private method using reflection
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
                "validActivityEconomicDependent", String.class, Object.class);
        method.setAccessible(true);

        // We need to handle the InvocationTargetException that wraps the
        // AffiliationError
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(service, codeActivity, invalidObject);
        });

        // Check that the cause is AffiliationError
        assertTrue(exception.getCause() instanceof AffiliationError);
    }


    @Test
    @DisplayName("sendEmail - should send email when affiliation is AffiliateMercantile")
    void sendEmail_withAffiliateMercantile_shouldSendEmail() throws Exception {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("F123");
        affiliate.setCompany("Test Company");

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setEmail("test@example.com");

        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliateMercantile));
        when(domesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // Act
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod("sendEmail", Affiliate.class,
                MultipartFile.class);
        method.setAccessible(true);
        method.invoke(service, affiliate, mockFile);

        // Assert
        verify(sendEmails, times(1)).emailBulkLoad("Test Company", "test@example.com", mockFile);
    }

    @Test
    @DisplayName("sendEmail - should send email when affiliation is Affiliation")
    void sendEmail_withAffiliation_shouldSendEmail() throws Exception {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("F123");
        affiliate.setCompany("Test Company");

        com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation affiliation = new com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation();
        affiliation.setEmail("test@example.com");

        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(domesticServiceIndependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliation));

        // Act
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod("sendEmail", Affiliate.class,
                MultipartFile.class);
        method.setAccessible(true);
        method.invoke(service, affiliate, mockFile);

        // Assert
        verify(sendEmails, times(1)).emailBulkLoad("Test Company", "test@example.com", mockFile);
    }

    @Test
    @DisplayName("sendEmail - should not send email when no affiliation is found")
    void sendEmail_withNoAffiliation_shouldNotSendEmail() throws Exception {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("F123");
        affiliate.setCompany("Test Company");

        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(domesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // Act
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod("sendEmail", Affiliate.class,
                MultipartFile.class);
        method.setAccessible(true);
        method.invoke(service, affiliate, mockFile);

        // Assert
        verify(sendEmails, times(0)).emailBulkLoad(any(), any(), any());
    }

    @Test
    @DisplayName("sendEmail - should not send email when affiliation has no email")
    void sendEmail_withAffiliationNoEmail_shouldNotSendEmail() throws Exception {
        // Arrange
        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("F123");
        affiliate.setCompany("Test Company");

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setEmail(null); // No email

        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliateMercantile));
        when(domesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // Act
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod("sendEmail", Affiliate.class,
                MultipartFile.class);
        method.setAccessible(true);
        method.invoke(service, affiliate, mockFile);

        // Assert
        verify(sendEmails, times(0)).emailBulkLoad(any(), any(), any());
    }

}
