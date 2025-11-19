package com.gal.afiliaciones.application.service.affiliate.bulkloadingdependentindependent.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import com.gal.afiliaciones.application.service.affiliate.bulkloadingdependentindependent.BulkLoadingHelp;
import com.gal.afiliaciones.application.service.affiliate.recordloadbulk.RecordLoadBulkService;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.application.service.impl.certicate.InMemoryMultipartFile;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.config.util.MessageErrorAge;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.Operator;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.SmlmvRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MainOfficeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dto.afpeps.FundAfpDTO;
import com.gal.afiliaciones.infrastructure.dto.afpeps.FundEpsDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.DataExcelDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.DataExcelIndependentDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.validation.BulkMessageService;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

@ContextConfiguration(classes = {BulkLoadingDependentIndependentServiceImpl.class})
@DisabledInAotMode
@ExtendWith(SpringExtension.class)
class BulkLoadingDependentIndependentServiceImplTest {
    @MockBean
    private AffiliateMercantileRepository affiliateMercantileRepository;

    @MockBean
    private AffiliateRepository affiliateRepository;

    @MockBean
    private AffiliationDependentRepository affiliationDependentRepository;

    @MockBean
    private AlfrescoService alfrescoService;

    @Autowired
    private BulkLoadingDependentIndependentServiceImpl bulkLoadingDependentIndependentServiceImpl;

    @MockBean
    private BulkLoadingHelp bulkLoadingHelp;

    @MockBean
    private BulkMessageService bulkMessageService;

    @MockBean
    private CollectProperties collectProperties;

    @MockBean
    private DepartmentRepository departmentRepository;

    @MockBean
    private EconomicActivity economicActivity;

    @MockBean
    private ExcelProcessingServiceData excelProcessingServiceData;

    @MockBean
    private FundAfpDTO fundAfpDTO;

    @MockBean
    private FundEpsDTO fundEpsDTO;

    @MockBean
    private GenericWebClient genericWebClient;

    @MockBean
    private IAffiliationEmployerDomesticServiceIndependentRepository
            iAffiliationEmployerDomesticServiceIndependentRepository;

    @MockBean
    private IEconomicActivityRepository iEconomicActivityRepository;

    @MockBean
    private IUserPreRegisterRepository iUserPreRegisterRepository;

    @MockBean
    private LinkedHashMap<String, Object> linkedHashMap;

    @Autowired
    private List<FundEpsDTO> list;

    @Autowired
    private List<FundAfpDTO> list2;

    @Autowired
    private List<LinkedHashMap<String, Object>> list3;

    @Autowired
    private List<Municipality> list4;

    @Autowired
    private List<EconomicActivity> list5;

    @Autowired
    private List<MainOffice> list6;

    @MockBean
    private MainOffice mainOffice;

    @MockBean
    private MainOfficeRepository mainOfficeRepository;

    @MockBean
    private MessageErrorAge messageErrorAge;

    @MockBean
    private Municipality municipality;

    @MockBean
    private MunicipalityRepository municipalityRepository;

    @MockBean
    private RecordLoadBulkService recordLoadBulkService;

    @MockBean
    private SmlmvRepository smlmvRepository;

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

        bulkLoadingDependentIndependentServiceImpl.allActivities = new ArrayList<>();
        bulkLoadingDependentIndependentServiceImpl.allMunicipality = new ArrayList<>();
    }

    @Test
    @DisplayName("dataFile - Should throw error for wrong bonding type")
    void dataFile_shouldThrowErrorForWrongBondingType() {
        AffiliationError exception = assertThrows(AffiliationError.class, () -> {
            bulkLoadingDependentIndependentServiceImpl.dataFile(mockFile, "WRONG_TYPE", 1L, 123L);
        });
        assertTrue(true);
    }

    @Test
    @DisplayName("dataFile - Should throw error when user not found")
    void dataFile_shouldThrowErrorWhenUserNotFound() {
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.empty());
        AffiliationError exception = assertThrows(AffiliationError.class, () -> {
            bulkLoadingDependentIndependentServiceImpl.dataFile(mockFile, Constant.TYPE_AFFILLATE_DEPENDENT, 1L, 123L);
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
            bulkLoadingDependentIndependentServiceImpl.dataFile(mockFile, Constant.TYPE_AFFILLATE_DEPENDENT, 1L, 123L);
        });
        assertTrue(true);
    }

    @Test
    @DisplayName("dataFileWithNumber - Should throw error when affiliate not found")
    void dataFileWithNumber_shouldThrowErrorWhenAffiliateNotFound() {
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        AffiliationError exception = assertThrows(AffiliationError.class, () -> {
            bulkLoadingDependentIndependentServiceImpl.dataFileWithNumber(mockFile, "CC", "123", "CC", 1L);
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
            bulkLoadingDependentIndependentServiceImpl.dataFileWithNumber(mockFile, "CC", "123", "CC", 1L);
        });
        assertTrue(true);
    }

    @Test
    @DisplayName("getTemplateByBondingType - Should return independent template id")
    void getTemplateByBondingType_shouldReturnIndependentTemplateId() {
        when(collectProperties.getIdTemplateIndependent()).thenReturn("id_independent");
        when(alfrescoService.getDocument("id_independent")).thenReturn("base64_independent_doc");

        String result = bulkLoadingDependentIndependentServiceImpl.getTemplateByBondingType("INDEPENDIENTE");

        assertEquals("base64_independent_doc", result);
        verify(collectProperties, times(1)).getIdTemplateIndependent();
        verify(alfrescoService, times(1)).getDocument("id_independent");
    }

    @Test
    @DisplayName("getTemplateByBondingType - Should return dependent template id")
    void getTemplateByBondingType_shouldReturnDependentTemplateId() {
        when(collectProperties.getIdTemplateDependent()).thenReturn("id_dependent");
        when(alfrescoService.getDocument("id_dependent")).thenReturn("base64_dependent_doc");

        String result = bulkLoadingDependentIndependentServiceImpl.getTemplateByBondingType("DEPENDIENTE");

        assertEquals("base64_dependent_doc", result);
        verify(collectProperties, times(1)).getIdTemplateDependent();
        verify(alfrescoService, times(1)).getDocument("id_dependent");
    }

    @Test
    @DisplayName("getTemplateByBondingType - Should throw error for empty bonding type")
    void getTemplateByBondingType_shouldThrowErrorForEmptyBondingType() {
        AffiliationError exception = assertThrows(AffiliationError.class, () -> {
            bulkLoadingDependentIndependentServiceImpl.getTemplateByBondingType("");
        });
        assertTrue(true);
    }

    @Test
    @DisplayName("downloadTemplateGuide - Should return guide document")
    void downloadTemplateGuide_shouldReturnGuideDocument() {
        when(collectProperties.getIdTemplateGuide()).thenReturn("id_guide");
        when(alfrescoService.getDocument("id_guide")).thenReturn("base64_guide_doc");

        String result = bulkLoadingDependentIndependentServiceImpl.downloadTemplateGuide();

        assertEquals("base64_guide_doc", result);
        verify(collectProperties, times(1)).getIdTemplateGuide();
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

        String result = bulkLoadingDependentIndependentServiceImpl.consultAffiliation("CC", "123");

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
            bulkLoadingDependentIndependentServiceImpl.consultAffiliation("CC", "123");
        });
        assertTrue(true);
    }

    @Test
    @DisplayName("consultAffiliation - Should throw error if mercantile affiliation not found")
    void consultAffiliation_shouldThrowErrorIfMercantileNotFound() {
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        AffiliationError exception = assertThrows(AffiliationError.class, () -> {
            bulkLoadingDependentIndependentServiceImpl.consultAffiliation("CC", "123");
        });
        assertTrue(true);
    }

    private AffiliationDependent invokeConvertDataAffiliationIndependent(DataExcelIndependentDTO dto, UserMain user) {
        try {
            Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
                    "convertDataAffiliationIndependent", DataExcelIndependentDTO.class, UserMain.class);
            method.setAccessible(true);
            return (AffiliationDependent) method.invoke(bulkLoadingDependentIndependentServiceImpl, dto, user);
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
            return (AffiliationDependent) method.invoke(bulkLoadingDependentIndependentServiceImpl, dto, user);
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
        bulkLoadingDependentIndependentServiceImpl.allActivities = List.of(activity);

        // Act - Invoke the private method using reflection
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
                "validActivityEconomicDependent", String.class, Object.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(bulkLoadingDependentIndependentServiceImpl, codeActivity, affiliateMercantile);

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
        bulkLoadingDependentIndependentServiceImpl.allActivities = List.of(activity);

        // Act - Invoke the private method using reflection
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
                "validActivityEconomicDependent", String.class, Object.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(bulkLoadingDependentIndependentServiceImpl, codeActivity, affiliateMercantile);

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
        bulkLoadingDependentIndependentServiceImpl.allActivities = List.of(activity);

        // Act - Invoke the private method using reflection
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
                "validActivityEconomicDependent", String.class, Object.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(bulkLoadingDependentIndependentServiceImpl, codeActivity, affiliation);

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
        bulkLoadingDependentIndependentServiceImpl.allActivities = List.of(activity);

        // Act - Invoke the private method using reflection
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
                "validActivityEconomicDependent", String.class, Object.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(bulkLoadingDependentIndependentServiceImpl, codeActivity, affiliation);

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
        bulkLoadingDependentIndependentServiceImpl.allActivities = new ArrayList<>();

        // Act - Invoke the private method using reflection
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
                "validActivityEconomicDependent", String.class, Object.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(bulkLoadingDependentIndependentServiceImpl, codeActivity, affiliateMercantile);

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
        bulkLoadingDependentIndependentServiceImpl.allActivities = List.of(activity);

        // Act & Assert - Invoke the private method using reflection
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
                "validActivityEconomicDependent", String.class, Object.class);
        method.setAccessible(true);

        // We need to handle the InvocationTargetException that wraps the
        // AffiliationError
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(bulkLoadingDependentIndependentServiceImpl, codeActivity, null);
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
        bulkLoadingDependentIndependentServiceImpl.allActivities = List.of(activity);

        // Act & Assert - Invoke the private method using reflection
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
                "validActivityEconomicDependent", String.class, Object.class);
        method.setAccessible(true);

        // We need to handle the InvocationTargetException that wraps the
        // AffiliationError
        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(bulkLoadingDependentIndependentServiceImpl, codeActivity, invalidObject);
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
        assertThrows(AffiliationError.class, () -> bulkLoadingDependentIndependentServiceImpl.consultAffiliation("CC", "999"));
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

        assertTrue((Boolean)m.invoke(bulkLoadingDependentIndependentServiceImpl, adult, "CC"));
        assertFalse((Boolean)m.invoke(bulkLoadingDependentIndependentServiceImpl, minor, "CC"));
        assertTrue((Boolean)m.invoke(bulkLoadingDependentIndependentServiceImpl, okOther, "TI"));   // distinto de CC
        assertFalse((Boolean)m.invoke(bulkLoadingDependentIndependentServiceImpl, tooOld, "TI"));   // antes de 1900
    }

    @Test
    void formatDate_validAndInvalid() throws Exception {
        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("formatDate", String.class);
        m.setAccessible(true);
        assertNotNull(m.invoke(bulkLoadingDependentIndependentServiceImpl, "2025/01/15"));
        assertNotNull(m.invoke(bulkLoadingDependentIndependentServiceImpl, "15/01/2025"));
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

        assertTrue((Boolean)m.invoke(bulkLoadingDependentIndependentServiceImpl, ok));
        assertFalse((Boolean)m.invoke(bulkLoadingDependentIndependentServiceImpl, before));
        assertFalse((Boolean)m.invoke(bulkLoadingDependentIndependentServiceImpl, after));
    }

    @Test
    void validSalary_bounds() throws Exception {
        // Crear SalaryDTO directamente sin usar el repositorio
        SalaryDTO smlv = new SalaryDTO();
        smlv.setValue(1300000L);
        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("validSalary", String.class, SalaryDTO.class);
        m.setAccessible(true);

        long min = smlv.getValue();
        long ok = min + 1;
        assertTrue((Boolean)m.invoke(bulkLoadingDependentIndependentServiceImpl, String.valueOf(min), smlv));
        assertTrue((Boolean)m.invoke(bulkLoadingDependentIndependentServiceImpl, String.valueOf(ok), smlv));
        assertFalse((Boolean)m.invoke(bulkLoadingDependentIndependentServiceImpl, String.valueOf(min - 1), smlv));
        assertFalse((Boolean)m.invoke(bulkLoadingDependentIndependentServiceImpl, "no-num", smlv));
    }
    @Test
    void validAge_usesMinMax() throws Exception {
        when(collectProperties.getMinimumAge()).thenReturn(18);
        when(collectProperties.getMaximumAge()).thenReturn(60);

        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("validAge", String.class);
        m.setAccessible(true);
        String fmt = "yyyy/MM/dd";
        String ok = LocalDate.now().minusYears(30).format(DateTimeFormatter.ofPattern(fmt));
        String tooYoung = LocalDate.now().minusYears(16).format(DateTimeFormatter.ofPattern(fmt));
        String tooOld = LocalDate.now().minusYears(70).format(DateTimeFormatter.ofPattern(fmt));
        String bad = "01-01-2000";

        assertTrue((Boolean)m.invoke(bulkLoadingDependentIndependentServiceImpl, ok));
        assertFalse((Boolean)m.invoke(bulkLoadingDependentIndependentServiceImpl, tooYoung));
        assertFalse((Boolean)m.invoke(bulkLoadingDependentIndependentServiceImpl, tooOld));
        assertFalse((Boolean)m.invoke(bulkLoadingDependentIndependentServiceImpl, bad));
    }



    @Test
    void validActivityEconomicIndependent_codeOnly() throws Exception {
        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("validActivityEconomicIndependent", String.class);
        m.setAccessible(true);

        EconomicActivity e4 = new EconomicActivity();
        e4.setId(1L);
        e4.setClassRisk("4");
        e4.setCodeCIIU("01");
        e4.setAdditionalCode("0");

        EconomicActivity e3 = new EconomicActivity();
        e3.setId(2L);
        e3.setClassRisk("3");
        e3.setCodeCIIU("02");
        e3.setAdditionalCode("0");

        bulkLoadingDependentIndependentServiceImpl.allActivities = List.of(e4, e3);

        String code4 = e4.getClassRisk()+e4.getCodeCIIU()+e4.getAdditionalCode(); // "4010"
        String code3 = e3.getClassRisk()+e3.getCodeCIIU()+e3.getAdditionalCode(); // "3020"

        assertTrue((Boolean)m.invoke(bulkLoadingDependentIndependentServiceImpl, code4));
        assertTrue((Boolean)m.invoke(bulkLoadingDependentIndependentServiceImpl, code3));
        assertFalse((Boolean)m.invoke(bulkLoadingDependentIndependentServiceImpl, "NOEXISTE"));
    }

    @Test
    void validActivityEconomicIndependent_withAffiliateMercantile_returnsHighestRiskCode() throws Exception {
        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("validActivityEconomicIndependent", String.class, Object.class);
        m.setAccessible(true);

        EconomicActivity aLow = new EconomicActivity(); aLow.setId(10L); aLow.setClassRisk("3"); aLow.setCodeCIIU("10"); aLow.setAdditionalCode("0"); aLow.setEconomicActivityCode("A-LOW");
        EconomicActivity aHigh = new EconomicActivity(); aHigh.setId(20L); aHigh.setClassRisk("5"); aHigh.setCodeCIIU("20"); aHigh.setAdditionalCode("0"); aHigh.setEconomicActivityCode("A-HIGH");

        bulkLoadingDependentIndependentServiceImpl.allActivities = List.of(aLow, aHigh);

        AffiliateActivityEconomic ae = new AffiliateActivityEconomic(); ae.setActivityEconomic(aLow);
        AffiliateMercantile merc = new AffiliateMercantile(); merc.setEconomicActivity(List.of(ae));

        String inputCode = aHigh.getClassRisk()+aHigh.getCodeCIIU()+aHigh.getAdditionalCode();
        String result = (String)m.invoke(bulkLoadingDependentIndependentServiceImpl, inputCode, merc);
        assertEquals("A-HIGH", result);
    }

    @Test
    void findActivityEconomic_foundAndNotFound() throws Exception {
        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("findActivityEconomic", String.class);
        m.setAccessible(true);

        EconomicActivity act = new EconomicActivity();
        act.setId(99L); act.setClassRisk("4"); act.setCodeCIIU("77"); act.setAdditionalCode("1");
        bulkLoadingDependentIndependentServiceImpl.allActivities = List.of(act);

        String code = act.getClassRisk()+act.getCodeCIIU()+act.getAdditionalCode();
        Object dto = m.invoke(bulkLoadingDependentIndependentServiceImpl, code);
        assertNotNull(dto);

        assertNull(m.invoke(bulkLoadingDependentIndependentServiceImpl, "NOPE"));
    }

    @Test
    void findAllById_picksHighestRisk() throws Exception {
        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("findAllById", List.class);
        m.setAccessible(true);

        EconomicActivity a1 = new EconomicActivity(); a1.setId(1L); a1.setClassRisk("2"); a1.setEconomicActivityCode("R2");
        EconomicActivity a2 = new EconomicActivity(); a2.setId(2L); a2.setClassRisk("5"); a2.setEconomicActivityCode("R5");
        bulkLoadingDependentIndependentServiceImpl.allActivities = List.of(a1, a2);

        String code = (String) m.invoke(bulkLoadingDependentIndependentServiceImpl, List.of(1L, 2L));
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
        assertSame(am, m.invoke(bulkLoadingDependentIndependentServiceImpl, "F123"));

        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.empty());
        Affiliation af = new Affiliation();
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(af));
        assertSame(af, m.invoke(bulkLoadingDependentIndependentServiceImpl, "F123"));

        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.empty());
        assertNull(m.invoke(bulkLoadingDependentIndependentServiceImpl, "F123"));
    }

    @Test
    void validLetter_returnsLetterIfAnyFalse() throws Exception {
        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("validLetter", List.class, String.class);
        m.setAccessible(true);
        assertNull(m.invoke(bulkLoadingDependentIndependentServiceImpl, List.of(true, true), "X"));
        assertEquals("X", m.invoke(bulkLoadingDependentIndependentServiceImpl, List.of(true, false, true), "X"));
    }


    @Test
    void validDatesCoverageContract_writesMessageOnIssues() throws Exception {
        Method m = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("validDatesCoverageContract", String.class, String.class, StringBuilder.class);
        m.setAccessible(true);

        StringBuilder bad1 = new StringBuilder();
        String coverageBeforeContract = LocalDate.now().plusDays(5).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String contractAfterCoverage = LocalDate.now().plusDays(10).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        m.invoke(bulkLoadingDependentIndependentServiceImpl, coverageBeforeContract, contractAfterCoverage, bad1);
        assertTrue(bad1.toString().contains("cobertura") || bad1.toString().contains("COBERTURA"));

        StringBuilder bad2 = new StringBuilder();
        m.invoke(bulkLoadingDependentIndependentServiceImpl, "NO/FECHA", "NO/FECHA", bad2);
        assertTrue(bad2.toString().contains("cobertura") || bad2.toString().contains("COBERTURA") ||
                bad2.toString().contains("formato") || bad2.toString().contains("FORMATO"));
    }

    /**
     * Test {@link BulkLoadingDependentIndependentServiceImpl#dataFile(MultipartFile, String, Long,
     * Long)}.
     *
     * <p>Method under test: {@link BulkLoadingDependentIndependentServiceImpl#dataFile(MultipartFile,
     * String, Long, Long)}
     */
    @Test
    @DisplayName("Test dataFile(MultipartFile, String, Long, Long)")
    @Tag("MaintainedByDiffblue")
    void testDataFile() throws UnsupportedEncodingException {
        // Arrange
        when(iUserPreRegisterRepository.findById(Mockito.<Long>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        InMemoryMultipartFile file =
                new InMemoryMultipartFile(
                        "Name", "foo.txt", "application/vnd.ms-excel", "AXAXAXAX".getBytes("UTF-8"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        bulkLoadingDependentIndependentServiceImpl.dataFile(
                                file, "Trabajador Independiente", 1L, 1L));
        verify(iUserPreRegisterRepository).findById(1L);
    }

    /**
     * Test {@link BulkLoadingDependentIndependentServiceImpl#dataFile(MultipartFile, String, Long,
     * Long)}.
     *
     * <ul>
     *   <li>Then calls {@link AffiliateRepository#findByIdAffiliate(Long)}.
     * </ul>
     *
     * <p>Method under test: {@link BulkLoadingDependentIndependentServiceImpl#dataFile(MultipartFile,
     * String, Long, Long)}
     */
    @Test
    @DisplayName(
            "Test dataFile(MultipartFile, String, Long, Long); then calls findByIdAffiliate(Long)")
    @Tag("MaintainedByDiffblue")
    void testDataFile_thenCallsFindByIdAffiliate() throws UnsupportedEncodingException {
        // Arrange
        when(affiliateRepository.findByIdAffiliate(Mockito.<Long>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

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

        UserMain userMain = new UserMain();
        userMain.setAcceptNotification(true);
        userMain.setAddress("42 Main St");
        userMain.setAge(1);
        userMain.setArea(1L);
        userMain.setAssignedPassword(true);
        userMain.setCodeOtp("Code Otp");
        userMain.setCompanyName("Company Name");
        userMain.setCreateDate(null);
        userMain.setCreatedAtTemporalPassword(LocalDate.of(1970, 1, 1));
        userMain.setDateBirth(LocalDate.of(1970, 1, 1));
        userMain.setEmail("jane.doe@example.org");
        userMain.setEmployerUpdateTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        userMain.setFinancialOperator(financialOperator);
        userMain.setFirstName("Jane");
        userMain.setGenerateAttempts(1);
        userMain.setGenerateOutTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        userMain.setHealthPromotingEntity(1L);
        userMain.setId(1L);
        userMain.setIdCardinalPoint2(1L);
        userMain.setIdCardinalPointMainStreet(1L);
        userMain.setIdCity(1L);
        userMain.setIdDepartment(1L);
        userMain.setIdHorizontalProperty1(1L);
        userMain.setIdHorizontalProperty2(1L);
        userMain.setIdHorizontalProperty3(1L);
        userMain.setIdHorizontalProperty4(1L);
        userMain.setIdLetter1MainStreet(1L);
        userMain.setIdLetter2MainStreet(1L);
        userMain.setIdLetterSecondStreet(1L);
        userMain.setIdMainStreet(1L);
        userMain.setIdNum1SecondStreet(1L);
        userMain.setIdNum2SecondStreet(1L);
        userMain.setIdNumHorizontalProperty1(1L);
        userMain.setIdNumHorizontalProperty2(1L);
        userMain.setIdNumHorizontalProperty3(1L);
        userMain.setIdNumHorizontalProperty4(1L);
        userMain.setIdNumberMainStreet(1L);
        userMain.setIdentification("Identification");
        userMain.setIdentificationType("Identification Type");
        userMain.setInactiveByPendingAffiliation(true);
        userMain.setInfoOperator(InfoOperator);
        userMain.setIsBis(true);
        userMain.setIsImport(true);
        userMain.setIsInArrearsStatus(true);
        userMain.setIsPasswordExpired(true);
        userMain.setIsTemporalPassword(true);
        userMain.setLastAffiliationAttempt(LocalDate.of(1970, 1, 1).atStartOfDay());
        userMain.setLastPasswordUpdate(LocalDate.of(1970, 1, 1).atStartOfDay());
        userMain.setLastUpdate(LocalDate.of(1970, 1, 1).atStartOfDay());
        userMain.setLevelAuthorization("JaneDoe");
        userMain.setLockoutTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        userMain.setLoginAttempts(1);
        userMain.setNationality(1L);
        userMain.setOffice(1);
        userMain.setOtherSex("Other Sex");
        userMain.setPensionFundAdministrator(1L);
        userMain.setPhoneNumber("6625550144");
        userMain.setPhoneNumber2("6625550144");
        userMain.setPin("Pin");
        userMain.setPosition(1);
        userMain.setProfile("Profile");
        userMain.setRoles(new ArrayList<>());
        userMain.setSecondName("Second Name");
        userMain.setSecondSurname("Doe");
        userMain.setSex("Sex");
        userMain.setStatus(1L);
        userMain.setStatusActive(true);
        userMain.setStatusInactiveSince(LocalDate.of(1970, 1, 1).atStartOfDay());
        userMain.setStatusPreRegister(true);
        userMain.setStatusStartAfiiliate(true);
        userMain.setSurname("Doe");
        userMain.setUserName("janedoe");
        userMain.setUserType(1L);
        userMain.setValidAttempts(1);
        userMain.setValidOutTime(LocalDate.of(1970, 1, 1).atStartOfDay());
        userMain.setVerificationDigit(1);
        Optional<UserMain> ofResult = Optional.of(userMain);
        when(iUserPreRegisterRepository.findById(Mockito.<Long>any())).thenReturn(ofResult);
        InMemoryMultipartFile file =
                new InMemoryMultipartFile(
                        "Name", "foo.txt", "application/vnd.ms-excel", "AXAXAXAX".getBytes("UTF-8"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        bulkLoadingDependentIndependentServiceImpl.dataFile(
                                file, "Trabajador Independiente", 1L, 1L));
        verify(affiliateRepository).findByIdAffiliate(1L);
        verify(iUserPreRegisterRepository).findById(1L);
    }

    /**
     * Test {@link BulkLoadingDependentIndependentServiceImpl#dataFile(MultipartFile, String, Long,
     * Long)}.
     *
     * <ul>
     *   <li>When {@code Trabajador Dependiente}.
     * </ul>
     *
     * <p>Method under test: {@link BulkLoadingDependentIndependentServiceImpl#dataFile(MultipartFile,
     * String, Long, Long)}
     */
    @Test
    @DisplayName("Test dataFile(MultipartFile, String, Long, Long); when 'Trabajador Dependiente'")
    @Tag("MaintainedByDiffblue")
    void testDataFile_whenTrabajadorDependiente() throws UnsupportedEncodingException {
        // Arrange
        when(iUserPreRegisterRepository.findById(Mockito.<Long>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        InMemoryMultipartFile file =
                new InMemoryMultipartFile(
                        "Name", "foo.txt", "application/vnd.ms-excel", "AXAXAXAX".getBytes("UTF-8"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        bulkLoadingDependentIndependentServiceImpl.dataFile(
                                file, "Trabajador Dependiente", 1L, 1L));
        verify(iUserPreRegisterRepository).findById(1L);
    }

    /**
     * Test {@link BulkLoadingDependentIndependentServiceImpl#dataFileWithNumber(MultipartFile,
     * String, String, String, Long)}.
     *
     * <p>Method under test: {@link
     * BulkLoadingDependentIndependentServiceImpl#dataFileWithNumber(MultipartFile, String, String,
     * String, Long)}
     */
    @Test
    @DisplayName("Test dataFileWithNumber(MultipartFile, String, String, String, Long)")
    @Tag("MaintainedByDiffblue")
    void testDataFileWithNumber() throws UnsupportedEncodingException {
        // Arrange
        when(affiliateRepository.findOne(Mockito.<Specification<Affiliate>>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        InMemoryMultipartFile file =
                new InMemoryMultipartFile("Name", "foo.txt", "text/plain", "AXAXAXAX".getBytes("UTF-8"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        bulkLoadingDependentIndependentServiceImpl.dataFileWithNumber(
                                file, "Type", "42", "Type Document", 1L));
        verify(affiliateRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link BulkLoadingDependentIndependentServiceImpl#dataFileWithNumber(MultipartFile,
     * String, String, String, Long)}.
     *
     * <p>Method under test: {@link
     * BulkLoadingDependentIndependentServiceImpl#dataFileWithNumber(MultipartFile, String, String,
     * String, Long)}
     */
    @Test
    @DisplayName("Test dataFileWithNumber(MultipartFile, String, String, String, Long)")
    @Tag("MaintainedByDiffblue")
    void testDataFileWithNumber2() throws UnsupportedEncodingException {
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
        when(affiliateRepository.findOne(Mockito.<Specification<Affiliate>>any())).thenReturn(ofResult);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        InMemoryMultipartFile file =
                new InMemoryMultipartFile("Name", "foo.txt", "text/plain", "AXAXAXAX".getBytes("UTF-8"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        bulkLoadingDependentIndependentServiceImpl.dataFileWithNumber(
                                file, "Type", "42", "Type Document", 1L));
        verify(affiliateRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link BulkLoadingDependentIndependentServiceImpl#dataFileWithNumber(MultipartFile,
     * String, String, String, Long)}.
     *
     * <ul>
     *   <li>Given {@link AffiliateMercantile#AffiliateMercantile()} Address is {@code 42 Main St}.
     * </ul>
     *
     * <p>Method under test: {@link
     * BulkLoadingDependentIndependentServiceImpl#dataFileWithNumber(MultipartFile, String, String,
     * String, Long)}
     */
    @Test
    @DisplayName(
            "Test dataFileWithNumber(MultipartFile, String, String, String, Long); given AffiliateMercantile() Address is '42 Main St'")
    @Tag("MaintainedByDiffblue")
    void testDataFileWithNumber_givenAffiliateMercantileAddressIs42MainSt()
            throws UnsupportedEncodingException {
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
        when(affiliateRepository.findOne(Mockito.<Specification<Affiliate>>any())).thenReturn(ofResult);

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult2 = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenReturn(ofResult2);
        InMemoryMultipartFile file =
                new InMemoryMultipartFile("Name", "foo.txt", "text/plain", "AXAXAXAX".getBytes("UTF-8"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        bulkLoadingDependentIndependentServiceImpl.dataFileWithNumber(
                                file, "Type", "42", "Type Document", 1L));
        verify(affiliateRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link BulkLoadingDependentIndependentServiceImpl#getTemplateByBondingType(String)}.
     *
     * <p>Method under test: {@link
     * BulkLoadingDependentIndependentServiceImpl#getTemplateByBondingType(String)}
     */
    @Test
    @DisplayName("Test getTemplateByBondingType(String)")
    @Tag("MaintainedByDiffblue")
    void testGetTemplateByBondingType() {
        // Arrange
        when(collectProperties.getIdTemplateDependent())
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> bulkLoadingDependentIndependentServiceImpl.getTemplateByBondingType("DEPENDIENTE"));
        verify(collectProperties).getIdTemplateDependent();
    }

    /**
     * Test {@link BulkLoadingDependentIndependentServiceImpl#getTemplateByBondingType(String)}.
     *
     * <p>Method under test: {@link
     * BulkLoadingDependentIndependentServiceImpl#getTemplateByBondingType(String)}
     */
    @Test
    @DisplayName("Test getTemplateByBondingType(String)")
    @Tag("MaintainedByDiffblue")
    void testGetTemplateByBondingType2() {
        // Arrange
        when(alfrescoService.getDocument(Mockito.<String>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> bulkLoadingDependentIndependentServiceImpl.getTemplateByBondingType("Bonding Type"));
        verify(alfrescoService).getDocument("");
    }

    /**
     * Test {@link BulkLoadingDependentIndependentServiceImpl#getTemplateByBondingType(String)}.
     *
     * <p>Method under test: {@link
     * BulkLoadingDependentIndependentServiceImpl#getTemplateByBondingType(String)}
     */
    @Test
    @DisplayName("Test getTemplateByBondingType(String)")
    @Tag("MaintainedByDiffblue")
    void testGetTemplateByBondingType3() {
        // Arrange
        when(collectProperties.getIdTemplateIndependent()).thenReturn("Id Template Independent");
        when(alfrescoService.getDocument(Mockito.<String>any())).thenReturn("Document");

        // Act
        String actualTemplateByBondingType =
                bulkLoadingDependentIndependentServiceImpl.getTemplateByBondingType("INDEPENDIENTE");

        // Assert
        verify(alfrescoService).getDocument("Id Template Independent");
        verify(collectProperties).getIdTemplateIndependent();
        assertEquals("Document", actualTemplateByBondingType);
    }

    /**
     * Test {@link BulkLoadingDependentIndependentServiceImpl#getTemplateByBondingType(String)}.
     *
     * <p>Method under test: {@link
     * BulkLoadingDependentIndependentServiceImpl#getTemplateByBondingType(String)}
     */
    @Test
    @DisplayName("Test getTemplateByBondingType(String)")
    @Tag("MaintainedByDiffblue")
    void testGetTemplateByBondingType4() {
        // Arrange
        when(collectProperties.getIdTemplateIndependent())
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> bulkLoadingDependentIndependentServiceImpl.getTemplateByBondingType("INDEPENDIENTE"));
        verify(collectProperties).getIdTemplateIndependent();
    }

    /**
     * Test {@link BulkLoadingDependentIndependentServiceImpl#getTemplateByBondingType(String)}.
     *
     * <p>Method under test: {@link
     * BulkLoadingDependentIndependentServiceImpl#getTemplateByBondingType(String)}
     */
    @Test
    @DisplayName("Test getTemplateByBondingType(String)")
    @Tag("MaintainedByDiffblue")
    void testGetTemplateByBondingType5() {
        // Arrange
        when(collectProperties.getIdTemplateDependent()).thenReturn("Id Template Dependent");
        when(alfrescoService.getDocument(Mockito.<String>any())).thenReturn("Document");

        // Act
        String actualTemplateByBondingType =
                bulkLoadingDependentIndependentServiceImpl.getTemplateByBondingType("DEPENDIENTE");

        // Assert
        verify(alfrescoService).getDocument("Id Template Dependent");
        verify(collectProperties).getIdTemplateDependent();
        assertEquals("Document", actualTemplateByBondingType);
    }

    /**
     * Test {@link BulkLoadingDependentIndependentServiceImpl#getTemplateByBondingType(String)}.
     *
     * <ul>
     *   <li>Given {@link CollectProperties}.
     *   <li>When empty string.
     * </ul>
     *
     * <p>Method under test: {@link
     * BulkLoadingDependentIndependentServiceImpl#getTemplateByBondingType(String)}
     */
    @Test
    @DisplayName("Test getTemplateByBondingType(String); given CollectProperties; when empty string")
    @Tag("MaintainedByDiffblue")
    void testGetTemplateByBondingType_givenCollectProperties_whenEmptyString() {
        // Arrange, Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> bulkLoadingDependentIndependentServiceImpl.getTemplateByBondingType(""));
    }

    /**
     * Test {@link BulkLoadingDependentIndependentServiceImpl#getTemplateByBondingType(String)}.
     *
     * <ul>
     *   <li>When {@code Bonding Type}.
     *   <li>Then return {@code Document}.
     * </ul>
     *
     * <p>Method under test: {@link
     * BulkLoadingDependentIndependentServiceImpl#getTemplateByBondingType(String)}
     */
    @Test
    @DisplayName("Test getTemplateByBondingType(String); when 'Bonding Type'; then return 'Document'")
    @Tag("MaintainedByDiffblue")
    void testGetTemplateByBondingType_whenBondingType_thenReturnDocument() {
        // Arrange
        when(alfrescoService.getDocument(Mockito.<String>any())).thenReturn("Document");

        // Act
        String actualTemplateByBondingType =
                bulkLoadingDependentIndependentServiceImpl.getTemplateByBondingType("Bonding Type");

        // Assert
        verify(alfrescoService).getDocument("");
        assertEquals("Document", actualTemplateByBondingType);
    }

    /**
     * Test {@link BulkLoadingDependentIndependentServiceImpl#downloadTemplateGuide()}.
     *
     * <p>Method under test: {@link
     * BulkLoadingDependentIndependentServiceImpl#downloadTemplateGuide()}
     */
    @Test
    @DisplayName("Test downloadTemplateGuide()")
    @Tag("MaintainedByDiffblue")
    void testDownloadTemplateGuide() {
        // Arrange
        when(collectProperties.getIdTemplateGuide()).thenReturn("1234");
        when(alfrescoService.getDocument(Mockito.<String>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> bulkLoadingDependentIndependentServiceImpl.downloadTemplateGuide());
        verify(alfrescoService).getDocument("1234");
        verify(collectProperties).getIdTemplateGuide();
    }

    /**
     * Test {@link BulkLoadingDependentIndependentServiceImpl#downloadTemplateGuide()}.
     *
     * <p>Method under test: {@link
     * BulkLoadingDependentIndependentServiceImpl#downloadTemplateGuide()}
     */
    @Test
    @DisplayName("Test downloadTemplateGuide()")
    @Tag("MaintainedByDiffblue")
    void testDownloadTemplateGuide2() {
        // Arrange
        when(collectProperties.getIdTemplateGuide())
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> bulkLoadingDependentIndependentServiceImpl.downloadTemplateGuide());
        verify(collectProperties).getIdTemplateGuide();
    }

    /**
     * Test {@link BulkLoadingDependentIndependentServiceImpl#downloadTemplateGuide()}.
     *
     * <ul>
     *   <li>Then return {@code Document}.
     * </ul>
     *
     * <p>Method under test: {@link
     * BulkLoadingDependentIndependentServiceImpl#downloadTemplateGuide()}
     */
    @Test
    @DisplayName("Test downloadTemplateGuide(); then return 'Document'")
    @Tag("MaintainedByDiffblue")
    void testDownloadTemplateGuide_thenReturnDocument() {
        // Arrange
        when(collectProperties.getIdTemplateGuide()).thenReturn("1234");
        when(alfrescoService.getDocument(Mockito.<String>any())).thenReturn("Document");

        // Act
        String actualDownloadTemplateGuideResult =
                bulkLoadingDependentIndependentServiceImpl.downloadTemplateGuide();

        // Assert
        verify(alfrescoService).getDocument("1234");
        verify(collectProperties).getIdTemplateGuide();
        assertEquals("Document", actualDownloadTemplateGuideResult);
    }

    /**
     * Test {@link BulkLoadingDependentIndependentServiceImpl#consultAffiliation(String, String)}.
     *
     * <p>Method under test: {@link
     * BulkLoadingDependentIndependentServiceImpl#consultAffiliation(String, String)}
     */
    @Test
    @DisplayName("Test consultAffiliation(String, String)")
    @Tag("MaintainedByDiffblue")
    void testConsultAffiliation() {
        // Arrange
        when(affiliateRepository.findOne(Mockito.<Specification<Affiliate>>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> bulkLoadingDependentIndependentServiceImpl.consultAffiliation("Type", "42"));
        verify(affiliateRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link BulkLoadingDependentIndependentServiceImpl#consultAffiliation(String, String)}.
     *
     * <p>Method under test: {@link
     * BulkLoadingDependentIndependentServiceImpl#consultAffiliation(String, String)}
     */
    @Test
    @DisplayName("Test consultAffiliation(String, String)")
    @Tag("MaintainedByDiffblue")
    void testConsultAffiliation2() {
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
        when(affiliateRepository.findOne(Mockito.<Specification<Affiliate>>any())).thenReturn(ofResult);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> bulkLoadingDependentIndependentServiceImpl.consultAffiliation("Type", "42"));
        verify(affiliateRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link BulkLoadingDependentIndependentServiceImpl#consultAffiliation(String, String)}.
     *
     * <ul>
     *   <li>Given {@link AffiliateMercantile#AffiliateMercantile()} Address is {@code 42 Main St}.
     * </ul>
     *
     * <p>Method under test: {@link
     * BulkLoadingDependentIndependentServiceImpl#consultAffiliation(String, String)}
     */
    @Test
    @DisplayName(
            "Test consultAffiliation(String, String); given AffiliateMercantile() Address is '42 Main St'")
    @Tag("MaintainedByDiffblue")
    void testConsultAffiliation_givenAffiliateMercantileAddressIs42MainSt() {
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
        when(affiliateRepository.findOne(Mockito.<Specification<Affiliate>>any())).thenReturn(ofResult);

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setAddress("42 Main St");
        affiliateMercantile.setAddressContactCompany("42 Main St");
        affiliateMercantile.setAddressIsEqualsContactCompany(true);
        affiliateMercantile.setAddressLegalRepresentative("42 Main St");
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setAffiliationStatus("Affiliation Status");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setArl("Arl");
        affiliateMercantile.setBusinessName("Business Name");
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setCityMunicipalityContactCompany(1L);
        affiliateMercantile.setCodeContributorType("Code Contributor Type");
        affiliateMercantile.setDateCreateAffiliate(LocalDate.of(1970, 1, 1));
        affiliateMercantile.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliateMercantile.setDateRequest("2020-03-01");
        affiliateMercantile.setDecentralizedConsecutive(1L);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setDepartmentContactCompany(1L);
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setEconomicActivity(new ArrayList<>());
        affiliateMercantile.setEmail("jane.doe@example.org");
        affiliateMercantile.setEmailContactCompany("jane.doe@example.org");
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setFiledNumber("42");
        affiliateMercantile.setId(1L);
        affiliateMercantile.setIdAffiliate(1L);
        affiliateMercantile.setIdCardinalPoint2(1L);
        affiliateMercantile.setIdCardinalPoint2ContactCompany(1L);
        affiliateMercantile.setIdCardinalPoint2LegalRepresentative(1L);
        affiliateMercantile.setIdCardinalPointMainStreet(1L);
        affiliateMercantile.setIdCardinalPointMainStreetContactCompany(1L);
        affiliateMercantile.setIdCardinalPointMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdCity(1L);
        affiliateMercantile.setIdCityContactCompany(1L);
        affiliateMercantile.setIdCityLegalRepresentative(1L);
        affiliateMercantile.setIdDepartment(1L);
        affiliateMercantile.setIdDepartmentContactCompany(1L);
        affiliateMercantile.setIdDepartmentLegalRepresentative(1L);
        affiliateMercantile.setIdEmployerSize(1L);
        affiliateMercantile.setIdFolderAlfresco("Id Folder Alfresco");
        affiliateMercantile.setIdHorizontalProperty1(1L);
        affiliateMercantile.setIdHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty2(1L);
        affiliateMercantile.setIdHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty3(1L);
        affiliateMercantile.setIdHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdHorizontalProperty4(1L);
        affiliateMercantile.setIdHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdLetter1MainStreet(1L);
        affiliateMercantile.setIdLetter1MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter1MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetter2MainStreet(1L);
        affiliateMercantile.setIdLetter2MainStreetContactCompany(1L);
        affiliateMercantile.setIdLetter2MainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdLetterSecondStreet(1L);
        affiliateMercantile.setIdLetterSecondStreetContactCompany(1L);
        affiliateMercantile.setIdLetterSecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdMainHeadquarter(1L);
        affiliateMercantile.setIdMainStreet(1L);
        affiliateMercantile.setIdMainStreetContactCompany(1L);
        affiliateMercantile.setIdMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum1SecondStreet(1L);
        affiliateMercantile.setIdNum1SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum1SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNum2SecondStreet(1L);
        affiliateMercantile.setIdNum2SecondStreetContactCompany(1L);
        affiliateMercantile.setIdNum2SecondStreetLegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty1(1L);
        affiliateMercantile.setIdNumHorizontalProperty1ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty1LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty2(1L);
        affiliateMercantile.setIdNumHorizontalProperty2ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty2LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty3(1L);
        affiliateMercantile.setIdNumHorizontalProperty3ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty3LegalRepresentative(1L);
        affiliateMercantile.setIdNumHorizontalProperty4(1L);
        affiliateMercantile.setIdNumHorizontalProperty4ContactCompany(1L);
        affiliateMercantile.setIdNumHorizontalProperty4LegalRepresentative(1L);
        affiliateMercantile.setIdNumberMainStreet(1L);
        affiliateMercantile.setIdNumberMainStreetContactCompany(1L);
        affiliateMercantile.setIdNumberMainStreetLegalRepresentative(1L);
        affiliateMercantile.setIdProcedureType(1L);
        affiliateMercantile.setIdSubTypeEmployer(1L);
        affiliateMercantile.setIdTypeEmployer(1L);
        affiliateMercantile.setIdUserPreRegister(1L);
        affiliateMercantile.setIsBis(true);
        affiliateMercantile.setIsBisContactCompany(true);
        affiliateMercantile.setIsBisLegalRepresentative(true);
        affiliateMercantile.setIsVip(true);
        affiliateMercantile.setLegalStatus("Legal Status");
        affiliateMercantile.setNumberDocumentPersonResponsible("42");
        affiliateMercantile.setNumberIdentification("42");
        affiliateMercantile.setNumberWorkers(1L);
        affiliateMercantile.setPhoneOne("6625550144");
        affiliateMercantile.setPhoneOneContactCompany("6625550144");
        affiliateMercantile.setPhoneOneLegalRepresentative("6625550144");
        affiliateMercantile.setPhoneTwo("6625550144");
        affiliateMercantile.setPhoneTwoContactCompany("6625550144");
        affiliateMercantile.setPhoneTwoLegalRepresentative("6625550144");
        affiliateMercantile.setRealNumberWorkers(1L);
        affiliateMercantile.setStageManagement("Stage Management");
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setSubTypeAffiliation("Sub Type Affiliation");
        affiliateMercantile.setTypeAffiliation("Type Affiliation");
        affiliateMercantile.setTypeDocumentIdentification("Type Document Identification");
        affiliateMercantile.setTypeDocumentPersonResponsible("Type Document Person Responsible");
        affiliateMercantile.setTypePerson("Type Person");
        affiliateMercantile.setZoneLocationEmployer("Zone Location Employer");
        Optional<AffiliateMercantile> ofResult2 = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> bulkLoadingDependentIndependentServiceImpl.consultAffiliation("Type", "42"));
        verify(affiliateRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
    }
}
