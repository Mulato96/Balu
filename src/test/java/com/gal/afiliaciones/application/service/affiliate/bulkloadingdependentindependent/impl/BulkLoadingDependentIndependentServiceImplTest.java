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

import com.gal.afiliaciones.domain.model.*;
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
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.AffiliationDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.DependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.ResponseServiceDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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




    // @Test
    // @DisplayName("validGeneral - Should process independent file with no errors and send email")
    // void validGeneral_shouldProcessIndependentFileWithNoErrorsAndSendEmail() throws IOException {
    //     MultipartFile file = mock(MultipartFile.class);
    //     Affiliate affiliate = new Affiliate();
    //     affiliate.setFiledNumber("F123");
    //     affiliate.setNitCompany("987654321");
    //     affiliate.setDocumentNumber("123456789");
    //     affiliate.setDocumentType("CC");

    //     // Setup mocks for file
    //     when(file.getOriginalFilename()).thenReturn("test.xlsx");

        // Setup mocks for dependencies
        // when(genericWebClient.getSmlmvByYear(anyInt())).thenReturn(salaryDTO);
        // when(excelProcessingServiceData.converterExcelToMap(any(), any()))
        //         .thenReturn(List.of(new LinkedHashMap<>()));
        // when(excelProcessingServiceData.converterMapToClass(any(), eq(DataExcelIndependentDTO.class)))
        //         .thenReturn(List.of(new DataExcelIndependentDTO() {
        //             {
        //                 setIdRecord(1);
        //                 setCoverageDate(LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_STRING)));
        //                 setIdentificationDocumentType("CC");
        //                 setIdentificationDocumentNumber("1234567");
        //                 setFirstName("John");
        //                 setSurname("Doe");
        //                 setDateOfBirth("01/01/1990");
        //                 setGender("M");
        //                 setHealthPromotingEntity("EPS1");
        //                 setPensionFundAdministrator("AFP1");
        //                 setIdDepartment("1");
        //                 setIdCity("CITY1");
        //                 setPhone1("3001234567");
        //                 setEmail("test@example.com");
        //                 setIdOccupation("OCC1");
        //                 setContractType("Type");
        //                 setTransportSupply("true");
        //                 setStartDate(LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_STRING)));
        //                 setEndDate(LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_STRING)));
        //                 setContractTotalValue("1000");
        //                 setCodeActivityContract("ContractCode");
        //                 setCodeActivityEmployer("4AB");
        //                 setEmployerDocumentTypeCodeContractor("CC");
        //                 setEmployerDocumentNumber("123");
        //             }
        //         }));
        // when(excelProcessingServiceData.findDataDuplicate(any(), any(), any())).thenReturn(List.of());
        // when(excelProcessingServiceData.createDocumentExcelErrors(any())).thenReturn(new ExportDocumentsDTO());
        // when(excelProcessingServiceData.findByEps(any())).thenReturn(List.of());
        // when(excelProcessingServiceData.findByAfp(any())).thenReturn(List.of());
        // when(excelProcessingServiceData.findByPensionOrEpsOrArl(any())).thenReturn(List.of());
        // when(municipalityRepository.findAll()).thenReturn(List.of());
        // when(iEconomicActivityRepository.findAll()).thenReturn(List.of());


    //     when(recordLoadBulkService.save(any())).thenReturn(new RecordLoadBulk() {
    //         {
    //             setId(1L);
    //         }
    //     });
    //     when(dependentRepository.findByIdentificationDocumentNumberIn(any())).thenReturn(List.of());

    //     // Call private method via reflection
    //     try {
    //         Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
    //                 "validGeneral", MultipartFile.class, String.class, Long.class, Affiliate.class);
    //         method.setAccessible(true);
    //         ResponseServiceDTO response = (ResponseServiceDTO) method.invoke(service, file,
    //                 Constant.TYPE_AFFILLATE_INDEPENDENT, 1L, affiliate);

    //         assertEquals("1", response.getTotalRecord());
    //         assertNotNull(response.getDocument());
    //     } catch (Exception e) {
    //         fail("Exception thrown: " + e.getMessage());
    //     }
    // }

    // @Test
    // @DisplayName("validGeneral - Should process dependent file with errors and create error document")
    // void validGeneral_shouldProcessDependentFileWithErrorsAndCreateErrorDocument() throws IOException {
    //     MultipartFile file = mock(MultipartFile.class);
    //     Affiliate affiliate = new Affiliate();
    //     affiliate.setFiledNumber("F123");
    //     affiliate.setNitCompany("987654321");
    //     affiliate.setDocumentNumber("123456789");
    //     affiliate.setDocumentType("CC");

    //     // Setup mocks for file
    //     when(file.getOriginalFilename()).thenReturn("test.xlsx");

        // Setup mocks for dependencies
        // when(genericWebClient.getSmlmvByYear(anyInt())).thenReturn(salaryDTO);
        // when(excelProcessingServiceData.converterExcelToMap(any(), any()))
        //         .thenReturn(List.of(new LinkedHashMap<>()));
        // when(excelProcessingServiceData.converterMapToClass(any(), eq(DataExcelDependentDTO.class)))
        //         .thenReturn(List.of(new DataExcelDependentDTO() {
        //             {
        //                 setIdRecord(1);
        //                 setCoverageDate("01/01/2099"); // Invalid
        //                 setIdentificationDocumentType("XX"); // Invalid
        //                 setIdentificationDocumentNumber("1"); // Invalid
        //                 setFirstName("John123"); // Invalid
        //                 setSurname("Doe$"); // Invalid
        //                 setDateOfBirth("01/01/2020"); // Invalid age for CC
        //                 setGender("X"); // Invalid
        //                 setHealthPromotingEntity("EPS_INVALID"); // Invalid
        //                 setPensionFundAdministrator("AFP_INVALID"); // Invalid
        //                 setIdDepartment("INVALID"); // Invalid
        //                 setIdCity("CITY_INVALID"); // Invalid
        //                 setPhone1("12345"); // Invalid
        //                 setIdWorkModality("5"); // Invalid
        //                 setSalary("100"); // Invalid
        //                 setIdOccupation(null); // Invalid (isRequested)
        //                 setEconomicActivityCode("INVALID"); // Invalid
        //                 setEmployerDocumentTypeCodeContractor("XX"); // Invalid
        //             }
        //         }));
        // when(excelProcessingServiceData.findDataDuplicate(any(), any(), any())).thenReturn(List.of());
        // when(excelProcessingServiceData.createDocumentExcelErrors(any())).thenReturn(new ExportDocumentsDTO());
        // when(excelProcessingServiceData.findByEps(any())).thenReturn(List.of());
        // when(excelProcessingServiceData.findByAfp(any())).thenReturn(List.of());
        // when(excelProcessingServiceData.findByPensionOrEpsOrArl(any())).thenReturn(List.of());
        // when(municipalityRepository.findAll()).thenReturn(List.of());
        // when(iEconomicActivityRepository.findAll()).thenReturn(List.of());
        // when(recordLoadBulkService.save(any())).thenReturn(new RecordLoadBulk() {
        //     {
        //         setId(1L);
        //     }
        // });
        // when(dependentRepository.findByIdentificationDocumentNumberIn(any())).thenReturn(List.of());

    //     // Call private method via reflection
    //     try {
    //         Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
    //                 "validGeneral", MultipartFile.class, String.class, Long.class, Affiliate.class);
    //         method.setAccessible(true);
    //         ResponseServiceDTO response = (ResponseServiceDTO) method.invoke(service, file,
    //                 Constant.TYPE_AFFILLATE_DEPENDENT, 1L, affiliate);

    //         assertEquals("1", response.getTotalRecord());
    //         assertEquals("1", response.getRecordError());
    //         assertEquals("0", response.getRecordSuccessful());
    //         assertNotNull(response.getDocument());
    //     } catch (Exception e) {
    //         fail("Exception thrown: " + e.getMessage());
    //     }
    // }


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

//    @Test
//    @DisplayName("assignPolicy - Should create policy for dependent when employer and policy exist")
//    void assignPolicy_shouldCreatePolicyForDependent_whenEmployerAndPolicyExist() throws Exception {
//        // Arrange
//        Long idAffiliate = 1L;
//        String nitEmployer = "900123456";
//        String identificationTypeDependent = "CC";
//        String identificationNumberDependent = "10203040";
//        Long idPolicyType = Constant.ID_EMPLOYER_POLICY;
//        String nameCompany = "Test Company";
//
//        Affiliate employerAffiliate = new Affiliate();
//        employerAffiliate.setIdAffiliate(2L);
//
//        Policy employerPolicy = new Policy();
//        employerPolicy.setIdPolicyType(idPolicyType);
//        employerPolicy.setCode("POL123");
//
//        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(employerAffiliate));
//        when(policyRepository.findByIdAffiliate(employerAffiliate.getIdAffiliate()))
//                .thenReturn(List.of(employerPolicy));
//
//        // Act
//        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod("assignPolicy", Long.class,
//                String.class, String.class, String.class, Long.class, String.class);
//        method.setAccessible(true);
//        method.invoke(service, idAffiliate, nitEmployer, identificationTypeDependent, identificationNumberDependent,
//                idPolicyType, nameCompany);
//
//        // Assert
//        verify(policyService, times(1)).createPolicyDependent(
//                eq(identificationTypeDependent),
//                eq(identificationNumberDependent),
//                any(LocalDate.class),
//                eq(idAffiliate),
//                eq(employerPolicy.getCode()),
//                eq(nameCompany));
//    }
@Test
void consultAffiliation_shouldThrowWhenAffiliateNotFound() {
    when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
    assertThrows(AffiliationError.class, () -> service.consultAffiliation("CC", "999"));
}

    @Test
    void validDateBirtDate_variants() throws Exception {
        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("validDateBirtDate", String.class, String.class);
        m.setAccessible(true);

        String fmt = "yyyy/MM/dd";
        String adult = LocalDate.now().minusYears(20).format(DateTimeFormatter.ofPattern(fmt));
        String minor = LocalDate.now().minusYears(16).format(DateTimeFormatter.ofPattern(fmt));
        String okOther = LocalDate.now().minusYears(5).format(DateTimeFormatter.ofPattern(fmt));
        String tooOld = "1899/12/31";

        assertTrue((Boolean)m.invoke(service, adult, "CC"));
        assertFalse((Boolean)m.invoke(service, minor, "CC"));
        assertTrue((Boolean)m.invoke(service, okOther, "TI"));   // distinto de CC
        assertFalse((Boolean)m.invoke(service, tooOld, "TI"));   // antes de 1900
    }

    @Test
    void formatDate_validAndInvalid() throws Exception {
        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("formatDate", String.class);
        m.setAccessible(true);
        assertNotNull(m.invoke(service, "2025/01/15"));
        assertNull(m.invoke(service, "15/01/2025"));
    }

    @Test
    void basicValidators_shouldCoverNameEmailPhone() throws Exception {
        Method vName = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("validName", String.class, int.class);
        Method vMail = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("validEmail", String.class);
        Method vPhone = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("validNumberPhone", String.class);
        vName.setAccessible(true);
        vMail.setAccessible(true);
        vPhone.setAccessible(true);

        assertTrue((Boolean)vName.invoke(service, "José Pérez", 50));
        assertFalse((Boolean)vName.invoke(service, "John123", 50));

        assertTrue((Boolean)vMail.invoke(service, "user@mail.com"));
        assertFalse((Boolean)vMail.invoke(service, "bad@@mail"));

        assertTrue((Boolean)vPhone.invoke(service, "300"));
        assertFalse((Boolean)vPhone.invoke(service, "999"));
    }

    @Test
    void validDateStartCoverage_window() throws Exception {
        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("validDateStartCoverage", String.class);
        m.setAccessible(true);
        String fmt = "yyyy/MM/dd";
        String ok = LocalDate.now().plusDays(5).format(DateTimeFormatter.ofPattern(fmt));
        String before = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern(fmt));
        String after = LocalDate.now().plusMonths(2).format(DateTimeFormatter.ofPattern(fmt));

        assertTrue((Boolean)m.invoke(service, ok));
        assertFalse((Boolean)m.invoke(service, before));
        assertFalse((Boolean)m.invoke(service, after));
    }

    @Test
    void validSalary_bounds() throws Exception {
        when(genericWebClient.getSmlmvByYear(any(Integer.class))).thenReturn(salaryDTO);
        Method salary = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod("salary");
        salary.setAccessible(true);
        SalaryDTO smlv = (SalaryDTO) salary.invoke(service);

        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("validSalary", String.class, com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO.class);
        m.setAccessible(true);

        long min = smlv.getValue();
        long ok = min + 1;
        long maxFail = smlv.getValue() * 25;

        assertTrue((Boolean)m.invoke(service, String.valueOf(min), smlv));
        assertTrue((Boolean)m.invoke(service, String.valueOf(ok), smlv));
        assertFalse((Boolean)m.invoke(service, String.valueOf(min - 1), smlv));
        assertFalse((Boolean)m.invoke(service, String.valueOf(maxFail), smlv));
        assertFalse((Boolean)m.invoke(service, "no-num", smlv));
    }

    @Test
    void validAge_usesMinMax() throws Exception {
        when(properties.getMinimumAge()).thenReturn(18);
        when(properties.getMaximumAge()).thenReturn(60);

        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("validAge", String.class);
        m.setAccessible(true);
        String fmt = "yyyy/MM/dd";
        String ok = LocalDate.now().minusYears(30).format(DateTimeFormatter.ofPattern(fmt));
        String tooYoung = LocalDate.now().minusYears(16).format(DateTimeFormatter.ofPattern(fmt));
        String tooOld = LocalDate.now().minusYears(70).format(DateTimeFormatter.ofPattern(fmt));
        String bad = "01-01-2000";

        assertTrue((Boolean)m.invoke(service, ok));
        assertFalse((Boolean)m.invoke(service, tooYoung));
        assertFalse((Boolean)m.invoke(service, tooOld));
        assertFalse((Boolean)m.invoke(service, bad));
    }

    @Test
    void departmentAndMunicipality_checks() throws Exception {
        Method vDep = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("validDepartment", String.class);
        Method vMun = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("validMunicipality", String.class);
        vDep.setAccessible(true);
        vMun.setAccessible(true);

        Department dep = new Department(); dep.setIdDepartment(Integer.valueOf(1));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dep));
        when(departmentRepository.findById(2L)).thenReturn(Optional.empty());

        Municipality m1 = new Municipality(); m1.setIdMunicipality(100L);
        Municipality m2 = new Municipality(); m2.setIdMunicipality(200L);
        service.allMunicipality = List.of(m1, m2);

        assertTrue((Boolean)vDep.invoke(service, "1"));
        assertFalse((Boolean)vDep.invoke(service, "2"));
        assertTrue((Boolean)vMun.invoke(service, "100"));
        assertFalse((Boolean)vMun.invoke(service, "999"));
        // entrada inválida
        assertFalse((Boolean)vDep.invoke(service, "abc"));
        assertFalse((Boolean)vMun.invoke(service, "abc"));
    }

    @Test
    void validActivityEconomicIndependent_codeOnly() throws Exception {
        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("validActivityEconomicIndependent", String.class);
        m.setAccessible(true);

        EconomicActivity e4 = new EconomicActivity(); e4.setId(1L); e4.setClassRisk("4");
        e4.setCodeCIIU("01"); e4.setAdditionalCode("0");
        EconomicActivity e3 = new EconomicActivity(); e3.setId(2L); e3.setClassRisk("3");
        e3.setCodeCIIU("02"); e3.setAdditionalCode("0");
        service.allActivities = List.of(e4, e3);

        String code4 = e4.getClassRisk()+e4.getCodeCIIU()+e4.getAdditionalCode(); // "4010"
        String code3 = e3.getClassRisk()+e3.getCodeCIIU()+e3.getAdditionalCode();

        assertTrue((Boolean)m.invoke(service, code4));
        assertFalse((Boolean)m.invoke(service, code3));
        assertFalse((Boolean)m.invoke(service, "NOEXISTE"));
    }

    @Test
    void validActivityEconomicIndependent_withAffiliateMercantile_returnsHighestRiskCode() throws Exception {
        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("validActivityEconomicIndependent", String.class, Object.class);
        m.setAccessible(true);

        EconomicActivity aLow = new EconomicActivity(); aLow.setId(10L); aLow.setClassRisk("3"); aLow.setCodeCIIU("10"); aLow.setAdditionalCode("0"); aLow.setEconomicActivityCode("A-LOW");
        EconomicActivity aHigh = new EconomicActivity(); aHigh.setId(20L); aHigh.setClassRisk("5"); aHigh.setCodeCIIU("20"); aHigh.setAdditionalCode("0"); aHigh.setEconomicActivityCode("A-HIGH");

        service.allActivities = List.of(aLow, aHigh);

        AffiliateActivityEconomic ae = new AffiliateActivityEconomic(); ae.setActivityEconomic(aLow);
        AffiliateMercantile merc = new AffiliateMercantile(); merc.setEconomicActivity(List.of(ae));

        String inputCode = aHigh.getClassRisk()+aHigh.getCodeCIIU()+aHigh.getAdditionalCode();
        String result = (String)m.invoke(service, inputCode, merc);
        assertEquals("A-HIGH", result);
    }

    @Test
    void findActivityEconomic_foundAndNotFound() throws Exception {
        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("findActivityEconomic", String.class);
        m.setAccessible(true);

        EconomicActivity act = new EconomicActivity();
        act.setId(99L); act.setClassRisk("4"); act.setCodeCIIU("77"); act.setAdditionalCode("1");
        service.allActivities = List.of(act);

        String code = act.getClassRisk()+act.getCodeCIIU()+act.getAdditionalCode();
        Object dto = m.invoke(service, code);
        assertNotNull(dto);

        assertNull(m.invoke(service, "NOPE"));
    }

    @Test
    void findAllById_picksHighestRisk() throws Exception {
        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("findAllById", List.class);
        m.setAccessible(true);

        EconomicActivity a1 = new EconomicActivity(); a1.setId(1L); a1.setClassRisk("2"); a1.setEconomicActivityCode("R2");
        EconomicActivity a2 = new EconomicActivity(); a2.setId(2L); a2.setClassRisk("5"); a2.setEconomicActivityCode("R5");
        service.allActivities = List.of(a1, a2);

        String code = (String) m.invoke(service, List.of(1L, 2L));
        assertEquals("R5", code);
    }

    @Test
    void findAffiliation_prefersMercantileElseDomesticElseNull() throws Exception {
        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("findAffiliation", String.class);
        m.setAccessible(true);

        AffiliateMercantile am = new AffiliateMercantile();
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(am));
        assertSame(am, m.invoke(service, "F123"));

        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.empty());
        Affiliation af = new Affiliation();
        when(domesticServiceIndependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(af));
        assertSame(af, m.invoke(service, "F123"));

        when(domesticServiceIndependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.empty());
        assertNull(m.invoke(service, "F123"));
    }

    @Test
    void validLetter_returnsLetterIfAnyFalse() throws Exception {
        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("validLetter", List.class, String.class);
        m.setAccessible(true);
        assertNull(m.invoke(service, List.of(true, true), "X"));
        assertEquals("X", m.invoke(service, List.of(true, false, true), "X"));
    }

    @Test
    void validDocumentEmployer_appendsErrorOnMismatch() throws Exception {
        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("validDocumentEmployer",
                        com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.DataExcelDependentDTO.class,
                        com.gal.afiliaciones.domain.model.affiliate.Affiliate.class,
                        StringBuilder.class);
        m.setAccessible(true);

        DataExcelDependentDTO dto = new DataExcelDependentDTO();
        dto.setIdentificationDocumentNumberContractor("X");
        dto.setEmployerDocumentTypeCodeContractor("TI");

        Affiliate aff = new Affiliate();
        aff.setDocumentNumber("Y");
        aff.setDocumentType("CC");

        StringBuilder sb = new StringBuilder();
        m.invoke(service, dto, aff, sb);
        assertTrue(sb.toString().contains("Número de documento empleador"));
    }

    @Test
    void validAddress_appendsErrorOnInvalidChars() throws Exception {
        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("validAddress", String.class, StringBuilder.class);
        m.setAccessible(true);

        StringBuilder ok = new StringBuilder();
        m.invoke(service, "CALLE#10-20", ok);
        assertEquals("", ok.toString());

        StringBuilder bad = new StringBuilder();
        m.invoke(service, "CALLE 10-20*", bad);
        assertTrue(bad.toString().contains("Dirección"));
    }

    @Test
    void validDatesCoverageContract_writesMessageOnIssues() throws Exception {
        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("validDatesCoverageContract", String.class, String.class, StringBuilder.class);
        m.setAccessible(true);

        StringBuilder bad1 = new StringBuilder();
        m.invoke(service, LocalDate.now().toString(), LocalDate.now().plusDays(1).toString(), bad1);
        assertTrue(bad1.toString().contains("cobertura"));

        StringBuilder bad2 = new StringBuilder();
        m.invoke(service, "NO/FECHA", "NO/FECHA", bad2);
        assertTrue(bad2.toString().contains("cobertura"));
    }
}
