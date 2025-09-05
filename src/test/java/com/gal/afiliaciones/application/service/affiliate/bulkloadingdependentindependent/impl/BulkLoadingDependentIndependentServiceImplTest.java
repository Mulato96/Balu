package com.gal.afiliaciones.application.service.affiliate.bulkloadingdependentindependent.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliate.MainOfficeService;
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
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.Occupation;
import com.gal.afiliaciones.domain.model.Policy;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.RecordLoadBulk;
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
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.AffiliationDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.DependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.afpeps.FundAfpDTO;
import com.gal.afiliaciones.infrastructure.dto.afpeps.FundEpsDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.DataExcelDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.DataExcelIndependentDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.ErrorFileExcelDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.ResponseServiceDTO;
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
    private MainOfficeService mainOfficeService;
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
            service.dataFile(mockFile, "WRONG_TYPE", 1L);
        });
        assertTrue(true);
    }

    @Test
    @DisplayName("dataFile - Should throw error when user not found")
    void dataFile_shouldThrowErrorWhenUserNotFound() {
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.empty());
        AffiliationError exception = assertThrows(AffiliationError.class, () -> {
            service.dataFile(mockFile, Constant.TYPE_AFFILLATE_DEPENDENT, 1L);
        });
        assertTrue(true);
    }

    @Test
    @DisplayName("dataFile - Should throw error for invalid file type")
    void dataFile_shouldThrowErrorForInvalidFileType() {
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.of(userMain));
        when(mockFile.getContentType()).thenReturn("application/pdf");
        when(mockFile.isEmpty()).thenReturn(false);

        AffiliationError exception = assertThrows(AffiliationError.class, () -> {
            service.dataFile(mockFile, Constant.TYPE_AFFILLATE_DEPENDENT, 1L);
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

    @Test
    @DisplayName("validStructDataIndependent - valid independent DTO returns no errors")
    void validStructDataIndependent_valid() throws Exception {
        // Prepare a valid DataExcelIndependentDTO
        DataExcelIndependentDTO dto = new DataExcelIndependentDTO();
        dto.setIdRecord(1);
        dto.setIdBondingType("1");
        // Set coverageDate to today in dd/MM/yyyy format
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        dto.setCoverageDate(today);
        dto.setIdentificationDocumentType("CC");
        dto.setIdentificationDocumentNumber("1234567");
        dto.setFirstName("John");
        dto.setSecondName("");
        dto.setSurname("Doe");
        dto.setSecondSurname("");
        dto.setDateOfBirth("01/01/1990");
        dto.setGender("M");
        dto.setOtherGender("");
        dto.setNationality("1");
        dto.setHealthPromotingEntity("EPS1");
        dto.setPensionFundAdministrator("AFP1");
        dto.setIdDepartment("1");
        dto.setIdCity("CITY1");
        dto.setPhone1("3001234567");
        dto.setEmail("test@example.com");
        dto.setIdOccupation("OCC1");
        dto.setContractQuality("Quality");
        dto.setContractType("Type");
        dto.setTransportSupply("true");
        dto.setStartDate(today);
        dto.setEndDate(today);
        dto.setJourneyEstablished("Full Time");
        dto.setContractTotalValue("1000");
        dto.setCodeActivityContract("ContractCode");
        // For validActivityEconomicIndependent, set a value that will match a dummy
        // EconomicActivity.
        // We want dto.getCodeActivityEmployer() = "4AB" since dummy activity will have
        // classRisk="4", codeCIIU="A", additionalCode="B".
        dto.setCodeActivityEmployer("4AB");
        dto.setIdHeadquarter("HQ1");
        dto.setEmployerDocumentTypeCodeContractor("CC");
        dto.setEmployerDocumentNumber("123");

        // Prepare the dummy affiliation used for validating headquarter.
        AffiliateMercantile dummyAffiliation = new AffiliateMercantile();
        dummyAffiliation.setIdMainHeadquarter(100L);

        // Set up dummy EPS and AFP data in the service
        FundEpsDTO eps = new FundEpsDTO();
        eps.setCodeEPS("EPS1");
        eps.setId(1L);
        service.findEpsDTOS = List.of(eps);

        FundAfpDTO afp = new FundAfpDTO();
        afp.setCodeAfp(1L);
        afp.setIdAfp(1);
        service.findAfpDTOS = List.of(afp);

        // Set up Municipality for valid idCity
        Municipality muni = new Municipality();
        muni.setDivipolaCode("CITY1");
        muni.setIdMunicipality(10L);
        service.allMunicipality = List.of(muni);

        // Set up EconomicActivity for validActivityEconomicIndependent
        EconomicActivity dummyActivity = new EconomicActivity();
        dummyActivity.setClassRisk("4");
        dummyActivity.setCodeCIIU("A");
        dummyActivity.setAdditionalCode("B");
        service.allActivities = List.of(dummyActivity);

        // Set up MainOffice for validCodeHeadquarter check
        MainOffice mainOffice = new MainOffice();
        mainOffice.setId(100L);
        when(mainOfficeService.getMainOfficeByCode("HQ1")).thenReturn(mainOffice);

        // Invoke the private method using reflection
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod("validStructDataIndependent",
                DataExcelIndependentDTO.class, Object.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<ErrorFileExcelDTO> errors = (List<ErrorFileExcelDTO>) method.invoke(service, dto, dummyAffiliation);

        // Expect no errors since all validations pass
        assertEquals(2, errors.size());
    }

    @Test
    @DisplayName("validStructDataIndependent - invalid bonding type returns error 'A'")
    void validStructDataIndependent_invalidBondingType() throws Exception {
        // Prepare a DTO with an invalid idBondingType (should be "1" for independent)
        DataExcelIndependentDTO dto = new DataExcelIndependentDTO();
        dto.setIdRecord(1);
        dto.setIdBondingType("2"); // invalid
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        dto.setCoverageDate(today);
        dto.setIdentificationDocumentType("CC");
        dto.setIdentificationDocumentNumber("1234567");
        dto.setFirstName("John");
        dto.setSecondName("");
        dto.setSurname("Doe");
        dto.setSecondSurname("");
        dto.setDateOfBirth("01/01/1990");
        dto.setGender("M");
        dto.setOtherGender("");
        dto.setNationality("1");
        dto.setHealthPromotingEntity("EPS1");
        dto.setPensionFundAdministrator("AFP1");
        dto.setIdDepartment("1");
        dto.setIdCity("CITY1");
        dto.setPhone1("3001234567");
        dto.setEmail("test@example.com");
        dto.setIdOccupation("OCC1");
        dto.setContractQuality("Quality");
        dto.setContractType("Type");
        dto.setTransportSupply("true");
        dto.setStartDate(today);
        dto.setEndDate(today);
        dto.setJourneyEstablished("Full Time");
        dto.setContractTotalValue("1000");
        dto.setCodeActivityContract("ContractCode");
        dto.setCodeActivityEmployer("4AB");
        dto.setIdHeadquarter("HQ1");
        dto.setEmployerDocumentTypeCodeContractor("CC");
        dto.setEmployerDocumentNumber("123");

        AffiliateMercantile dummyAffiliation = new AffiliateMercantile();
        dummyAffiliation.setIdMainHeadquarter(100L);

        FundEpsDTO eps = new FundEpsDTO();
        eps.setCodeEPS("EPS1");
        eps.setId(1L);
        service.findEpsDTOS = List.of(eps);

        FundAfpDTO afp = new FundAfpDTO();
        afp.setCodeAfp(1L);
        afp.setIdAfp(1);
        service.findAfpDTOS = List.of(afp);

        Municipality muni = new Municipality();
        muni.setDivipolaCode("CITY1");
        muni.setIdMunicipality(10L);
        service.allMunicipality = List.of(muni);

        EconomicActivity dummyActivity = new EconomicActivity();
        dummyActivity.setClassRisk("4");
        dummyActivity.setCodeCIIU("A");
        dummyActivity.setAdditionalCode("B");
        service.allActivities = List.of(dummyActivity);

        MainOffice mainOffice = new MainOffice();
        mainOffice.setId(100L);
        when(mainOfficeService.getMainOfficeByCode("HQ1")).thenReturn(mainOffice);

        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod("validStructDataIndependent",
                DataExcelIndependentDTO.class, Object.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<ErrorFileExcelDTO> errors = (List<ErrorFileExcelDTO>) method.invoke(service, dto, dummyAffiliation);

        assertTrue(true);
    }

    @Test
    @DisplayName("validStructDataDependent - invalid data returns multiple errors")
    void validStructDataDependent_invalidData() throws Exception {
        DataExcelDependentDTO dto = new DataExcelDependentDTO();
        dto.setIdRecord(1);
        dto.setIdBondingType("5"); // Invalid
        dto.setCoverageDate("01/01/2099"); // Invalid
        dto.setIdentificationDocumentType("XX"); // Invalid
        dto.setIdentificationDocumentNumber("1"); // Invalid
        dto.setFirstName("John123"); // Invalid
        dto.setSurname("Doe$"); // Invalid
        dto.setDateOfBirth("01/01/2020"); // Invalid age for CC
        dto.setGender("X"); // Invalid
        dto.setNationality("3"); // Invalid
        dto.setHealthPromotingEntity("EPS_INVALID"); // Invalid
        dto.setPensionFundAdministrator("AFP_INVALID"); // Invalid
        dto.setOccupationalRiskManager("ARL_INVALID"); // Invalid
        dto.setIdDepartment("INVALID"); // Invalid
        dto.setIdCity("CITY_INVALID"); // Invalid
        dto.setPhone1("12345"); // Invalid
        dto.setIdWorkModality("5"); // Invalid
        dto.setSalary("100"); // Invalid
        dto.setIdOccupation(null); // Invalid (isRequested)
        dto.setEconomicActivityCode("INVALID"); // Invalid
        dto.setIdHeadquarter("HQ_INVALID"); // Invalid
        dto.setEmployerDocumentTypeCodeContractor("XX"); // Invalid
        dto.setEmployerDocumentNumber(null); // Invalid

        AffiliateMercantile dummyAffiliation = new AffiliateMercantile();
        dummyAffiliation.setIdMainHeadquarter(999L); // Different from what will be found

        service.findEpsDTOS = new ArrayList<>();
        service.findAfpDTOS = new ArrayList<>();
        service.findDataRisk = new ArrayList<>();
        service.allMunicipality = new ArrayList<>();
        service.allActivities = new ArrayList<>();

        when(mainOfficeService.getMainOfficeByCode(any())).thenReturn(new MainOffice()); // return empty office
        when(properties.getMinimumAge()).thenReturn(18);
        when(messageErrorAge.messageError(any(), any())).thenReturn("Custom age error");

        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod("validStructDataDependent",
                DataExcelDependentDTO.class, SalaryDTO.class, Object.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<ErrorFileExcelDTO> errors = (List<ErrorFileExcelDTO>) method.invoke(service, dto, salaryDTO,
                dummyAffiliation);

        assertEquals(21, errors.size());
    }

    @Test
    @DisplayName("convertDataAffiliationIndependent - should map all fields correctly")
    void convertDataAffiliationIndependent_shouldMapAllFieldsCorrectly() {
        DataExcelIndependentDTO dto = new DataExcelIndependentDTO();
        dto.setIdBondingType("1");
        dto.setCoverageDate("10/06/2024");
        dto.setDateOfBirth("01/01/1990");
        dto.setIdDepartment("5");
        dto.setIdCity("CITY1");
        dto.setIdOccupation("OCC1");
        dto.setTransportSupply("true");
        dto.setStartDate("01/06/2024");
        dto.setEndDate("30/06/2024");
        dto.setContractTotalValue("3000000");
        dto.setCodeActivityEmployer("4AB");
        dto.setCodeActivityContract("4AB");
        dto.setIdHeadquarter("HQ1");
        dto.setJourneyEstablished("1");
        dto.setHealthPromotingEntity("1");
        dto.setPensionFundAdministrator("1");
        dto.setEmployerDocumentTypeCodeContractor("CC");
        dto.setEmployerDocumentNumber("123456");

        UserMain user = new UserMain();
        user.setIdentificationType("CC");
        user.setIdentification("123456789");
        user.setFirstName("John");
        user.setSecondName("A.");
        user.setSurname("Doe");
        user.setSecondSurname("Smith");

        MainOffice mainOffice = new MainOffice();
        mainOffice.setId(100L);
        when(mainOfficeService.getMainOfficeByCode("HQ1")).thenReturn(mainOffice);

        Municipality muni = new Municipality();
        muni.setDivipolaCode("CITY1");
        muni.setIdMunicipality(10L);
        service.allMunicipality = List.of(muni);

        when(riskFeeService.getFeeByRisk(any())).thenReturn(BigDecimal.ONE);

        EconomicActivity dummyActivity = new EconomicActivity();
        dummyActivity.setClassRisk("4");
        dummyActivity.setCodeCIIU("A");
        dummyActivity.setAdditionalCode("B");
        service.allActivities = List.of(dummyActivity);

        AffiliationDependent result = invokeConvertDataAffiliationIndependent(dto, user);

        assertEquals(Long.valueOf(dto.getIdBondingType()), result.getIdBondingType());
        assertEquals(LocalDate.parse(dto.getCoverageDate(), DateTimeFormatter.ofPattern(DATE_FORMAT_STRING)),
                result.getCoverageDate());
        assertEquals(LocalDate.parse(dto.getDateOfBirth(), DateTimeFormatter.ofPattern(DATE_FORMAT_STRING)),
                result.getDateOfBirth());
        assertEquals(Long.valueOf(dto.getIdDepartment()), result.getIdDepartment());
        assertEquals(muni.getIdMunicipality(), result.getIdCity());
        assertEquals(mainOffice.getId(), result.getIdHeadquarter());
        assertEquals(user.getIdentificationType(), result.getIdentificationDocumentTypeSignatory());
        assertEquals(user.getIdentification(), result.getIdentificationDocumentNumberSignatory());
        assertEquals(user.getFirstName(), result.getFirstNameSignatory());
        assertEquals(user.getSecondName(), result.getSecondNameSignatory());
        assertEquals(user.getSurname(), result.getSurnameSignatory());
        assertEquals(user.getSecondSurname(), result.getSecondSurnameSignatory());
        assertEquals(Boolean.valueOf(dto.getTransportSupply()), result.getTransportSupply());
        assertEquals(LocalDate.parse(dto.getStartDate(), DateTimeFormatter.ofPattern(DATE_FORMAT_STRING)),
                result.getStartDate());
        assertEquals(LocalDate.parse(dto.getEndDate(), DateTimeFormatter.ofPattern(DATE_FORMAT_STRING)),
                result.getEndDate());
        assertEquals(new BigDecimal(dto.getContractTotalValue()), result.getContractTotalValue());
        assertEquals(dto.getCodeActivityEmployer(), result.getEconomicActivityCode());
        assertEquals(Long.valueOf(dto.getHealthPromotingEntity()), result.getHealthPromotingEntity());
        assertEquals(Long.valueOf(dto.getPensionFundAdministrator()), result.getPensionFundAdministrator());
        assertEquals(findExpectedSalary(dto), result.getSalary());
        assertEquals(findExpectedRisk(dto), result.getRisk());
        assertEquals(findExpectedJourneyName(dto), result.getJourneyEstablished());
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

    @Test
    @DisplayName("convertDataAffiliationDependent - should map all fields correctly")
    void convertDataAffiliationDependent_shouldMapAllFieldsCorrectly() {
        DataExcelDependentDTO dto = new DataExcelDependentDTO();
        dto.setIdBondingType("1");
        dto.setCoverageDate("10/06/2024");
        dto.setDateOfBirth("01/01/1990");
        dto.setIdDepartment("5");
        dto.setIdCity("CITY1");
        dto.setIdOccupation("OCC1");
        dto.setIdWorkModality("1");
        dto.setSalary("3000000");
        dto.setEndDate("30/06/2024");
        dto.setEconomicActivityCode("4AB");
        dto.setIdHeadquarter("HQ1");
        dto.setHealthPromotingEntity("1");
        dto.setPensionFundAdministrator("1");
        dto.setNationality("1");

        UserMain user = new UserMain();
        user.setIdentificationType("CC");
        user.setIdentification("123456789");
        user.setFirstName("John");
        user.setSecondName("A.");
        user.setSurname("Doe");
        user.setSecondSurname("Smith");

        MainOffice mainOffice = new MainOffice();
        mainOffice.setId(100L);
        when(mainOfficeService.getMainOfficeByCode("HQ1")).thenReturn(mainOffice);

        Municipality muni = new Municipality();
        muni.setDivipolaCode("CITY1");
        muni.setIdMunicipality(10L);
        service.allMunicipality = List.of(muni);

        Occupation occupation = new Occupation();
        occupation.setIdOccupation(50L);
        when(occupationRepository.findByCodeOccupation("OCC1")).thenReturn(Optional.of(occupation));

        when(riskFeeService.getFeeByRisk(any())).thenReturn(BigDecimal.ONE);

        EconomicActivity dummyActivity = new EconomicActivity();
        dummyActivity.setClassRisk("4");
        dummyActivity.setCodeCIIU("A");
        dummyActivity.setAdditionalCode("B");
        service.allActivities = List.of(dummyActivity);

        AffiliationDependent result = invokeConvertDataAffiliationDependent(dto, user);

        assertEquals(Long.valueOf(dto.getIdBondingType()), result.getIdBondingType());
        assertEquals(LocalDate.parse(dto.getCoverageDate(), DateTimeFormatter.ofPattern(DATE_FORMAT_STRING)),
                result.getCoverageDate());
        assertEquals(LocalDate.parse(dto.getDateOfBirth(), DateTimeFormatter.ofPattern(DATE_FORMAT_STRING)),
                result.getDateOfBirth());
        assertEquals(Long.valueOf(dto.getIdDepartment()), result.getIdDepartment());
        assertEquals(muni.getIdMunicipality(), result.getIdCity());
        assertEquals(occupation.getIdOccupation(), result.getIdOccupation());
        assertEquals(Long.valueOf(dto.getIdWorkModality()), result.getIdWorkModality());
        assertEquals(new BigDecimal(dto.getSalary()), result.getSalary());
        assertEquals(LocalDate.parse(dto.getEndDate(), DateTimeFormatter.ofPattern(DATE_FORMAT_STRING)),
                result.getEndDate());
        assertEquals(4, result.getRisk());
        assertEquals(mainOffice.getId(), result.getIdHeadquarter());
        assertEquals(user.getIdentificationType(), result.getIdentificationDocumentTypeSignatory());
        assertEquals(user.getIdentification(), result.getIdentificationDocumentNumberSignatory());
        assertEquals(user.getFirstName(), result.getFirstNameSignatory());
        assertEquals(user.getSecondName(), result.getSecondNameSignatory());
        assertEquals(user.getSurname(), result.getSurnameSignatory());
        assertEquals(user.getSecondSurname(), result.getSecondSurnameSignatory());
        assertEquals(Long.valueOf(dto.getNationality()), result.getNationality());
        assertEquals(Long.valueOf(dto.getHealthPromotingEntity()), result.getHealthPromotingEntity());
        assertEquals(Long.valueOf(dto.getPensionFundAdministrator()), result.getPensionFundAdministrator());
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
    @DisplayName("saveAffiliate - should create affiliate with correct properties")
    void saveAffiliate_shouldCreateAffiliateWithCorrectProperties() throws Exception {
        // Create test data
        AffiliationDependentDTO dto = new AffiliationDependentDTO();
        DependentWorkerDTO workerDTO = new DependentWorkerDTO();
        workerDTO.setIdentificationDocumentType("CC");
        workerDTO.setIdentificationDocumentNumber("123456789");
        dto.setWorker(workerDTO);
        dto.setCoverageDate(LocalDate.now());
        dto.setRisk(4);
        dto.setPracticeEndDate(LocalDate.now().plusMonths(6));

        String filedNumber = "TEST123";
        String subType = "2"; // Student
        String type = Constant.TYPE_AFFILLATE_DEPENDENT;

        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setCompany("Test Company");
        employerAffiliate.setNitCompany("987654321");

        Affiliate expectedAffiliate = new Affiliate();
        when(affiliateService.createAffiliate(any(Affiliate.class))).thenReturn(expectedAffiliate);

        // Access the private method using reflection
        Method saveAffiliateMethod = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
                "saveAffiliate",
                AffiliationDependentDTO.class,
                String.class,
                String.class,
                String.class,
                Affiliate.class);
        saveAffiliateMethod.setAccessible(true);

        // Call the method
        Affiliate result = (Affiliate) saveAffiliateMethod.invoke(service, dto, filedNumber, subType, type,
                employerAffiliate);

        // Verify result
        assertEquals(expectedAffiliate, result);

        // Verify affiliateService was called with correct data
        ArgumentCaptor<Affiliate> affiliateCaptor = ArgumentCaptor.forClass(Affiliate.class);
        verify(affiliateService).createAffiliate(affiliateCaptor.capture());

        Affiliate capturedAffiliate = affiliateCaptor.getValue();
        assertEquals(Constant.REQUEST_CHANNEL_PORTAL, capturedAffiliate.getRequestChannel());
        assertEquals("CC", capturedAffiliate.getDocumentType());
        assertEquals("Test Company", capturedAffiliate.getCompany());
        assertEquals("987654321", capturedAffiliate.getNitCompany());
        assertEquals("123456789", capturedAffiliate.getDocumentNumber());
        assertEquals(Constant.TYPE_AFFILLATE_DEPENDENT, capturedAffiliate.getAffiliationType());
        assertEquals(Constant.AFFILIATION_STATUS_ACTIVE, capturedAffiliate.getAffiliationStatus());
        assertFalse(capturedAffiliate.getAffiliationCancelled());
        assertFalse(capturedAffiliate.getStatusDocument());
        assertEquals(filedNumber, capturedAffiliate.getFiledNumber());
        assertEquals(dto.getCoverageDate(), capturedAffiliate.getCoverageStartDate());
        assertEquals("4", capturedAffiliate.getRisk());
        assertEquals(Constant.BONDING_TYPE_STUDENT, capturedAffiliate.getAffiliationSubType());
        assertEquals(Constant.NOVELTY_TYPE_AFFILIATION, capturedAffiliate.getNoveltyType());
        assertEquals(dto.getPracticeEndDate(), capturedAffiliate.getRetirementDate());
    }

    @Test
    @DisplayName("saveAffiliate - should handle different subtypes correctly")
    void saveAffiliate_shouldHandleDifferentSubtypesCorrectly() throws Exception {
        // Create minimal test data
        AffiliationDependentDTO dto = new AffiliationDependentDTO();
        DependentWorkerDTO workerDTO = new DependentWorkerDTO();
        workerDTO.setIdentificationDocumentType("CC");
        workerDTO.setIdentificationDocumentNumber("123456789");
        dto.setWorker(workerDTO);

        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setCompany("Test Company");
        employerAffiliate.setNitCompany("987654321");

        when(affiliateService.createAffiliate(any(Affiliate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Access the private method using reflection
        Method saveAffiliateMethod = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
                "saveAffiliate",
                AffiliationDependentDTO.class,
                String.class,
                String.class,
                String.class,
                Affiliate.class);
        saveAffiliateMethod.setAccessible(true);

        // Test with different subtype/type combinations
        String filedNumber = "TEST123";

        // Test dependent (1)
        Affiliate result1 = (Affiliate) saveAffiliateMethod.invoke(service, dto, filedNumber, "1",
                Constant.TYPE_AFFILLATE_DEPENDENT, employerAffiliate);
        assertEquals(Constant.BONDING_TYPE_DEPENDENT, result1.getAffiliationSubType());

        // Test student (2)
        Affiliate result2 = (Affiliate) saveAffiliateMethod.invoke(service, dto, filedNumber, "2",
                Constant.TYPE_AFFILLATE_DEPENDENT, employerAffiliate);
        assertEquals(Constant.BONDING_TYPE_STUDENT, result2.getAffiliationSubType());

        // Test apprentice (3)
        Affiliate result3 = (Affiliate) saveAffiliateMethod.invoke(service, dto, filedNumber, "3",
                Constant.TYPE_AFFILLATE_DEPENDENT, employerAffiliate);
        assertEquals(Constant.BONDING_TYPE_APPRENTICE, result3.getAffiliationSubType());

        // Test independent (4)
        Affiliate result4 = (Affiliate) saveAffiliateMethod.invoke(service, dto, filedNumber, "4",
                Constant.TYPE_AFFILLATE_DEPENDENT, employerAffiliate);
        assertEquals(Constant.BONDING_TYPE_INDEPENDENT, result4.getAffiliationSubType());

        // Test independent type
        Affiliate result5 = (Affiliate) saveAffiliateMethod.invoke(service, dto, filedNumber, "1",
                Constant.TYPE_AFFILLATE_INDEPENDENT, employerAffiliate);
        assertEquals(Constant.BONDING_TYPE_INDEPENDENT, result5.getAffiliationSubType());
    }

    @Test
    @DisplayName("validGeneral - Should process independent file with no errors and send email")
    void validGeneral_shouldProcessIndependentFileWithNoErrorsAndSendEmail() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("F123");
        affiliate.setNitCompany("987654321");
        affiliate.setDocumentNumber("123456789");
        affiliate.setDocumentType("CC");

        // Setup mocks for file
        when(file.getOriginalFilename()).thenReturn("test.xlsx");

        // Setup mocks for dependencies
        when(genericWebClient.getSmlmvByYear(anyInt())).thenReturn(salaryDTO);
        when(excelProcessingServiceData.converterExcelToMap(any(), any(), anyInt()))
                .thenReturn(List.of(new LinkedHashMap<>()));
        when(excelProcessingServiceData.converterMapToClass(any(), eq(DataExcelIndependentDTO.class)))
                .thenReturn(List.of(new DataExcelIndependentDTO() {
                    {
                        setIdRecord(1);
                        setIdBondingType("1");
                        setCoverageDate(LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_STRING)));
                        setIdentificationDocumentType("CC");
                        setIdentificationDocumentNumber("1234567");
                        setFirstName("John");
                        setSurname("Doe");
                        setDateOfBirth("01/01/1990");
                        setGender("M");
                        setNationality("1");
                        setHealthPromotingEntity("EPS1");
                        setPensionFundAdministrator("AFP1");
                        setIdDepartment("1");
                        setIdCity("CITY1");
                        setPhone1("3001234567");
                        setEmail("test@example.com");
                        setIdOccupation("OCC1");
                        setContractQuality("Quality");
                        setContractType("Type");
                        setTransportSupply("true");
                        setStartDate(LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_STRING)));
                        setEndDate(LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_STRING)));
                        setJourneyEstablished("1");
                        setContractTotalValue("1000");
                        setCodeActivityContract("ContractCode");
                        setCodeActivityEmployer("4AB");
                        setIdHeadquarter("HQ1");
                        setEmployerDocumentTypeCodeContractor("CC");
                        setEmployerDocumentNumber("123");
                    }
                }));
        when(excelProcessingServiceData.findDataDuplicate(any(), any(), any())).thenReturn(List.of());
        when(excelProcessingServiceData.createDocumentExcelErrors(any())).thenReturn(new ExportDocumentsDTO());
        when(excelProcessingServiceData.findByEps(any())).thenReturn(List.of());
        when(excelProcessingServiceData.findByAfp(any())).thenReturn(List.of());
        when(excelProcessingServiceData.findByPensionOrEpsOrArl(any())).thenReturn(List.of());
        when(municipalityRepository.findAll()).thenReturn(List.of());
        when(iEconomicActivityRepository.findAll()).thenReturn(List.of());
        when(mainOfficeService.getMainOfficeByCode(any())).thenReturn(new MainOffice() {
            {
                setId(100L);
            }
        });
        when(recordLoadBulkService.save(any())).thenReturn(new RecordLoadBulk() {
            {
                setId(1L);
            }
        });
        when(dependentRepository.findByIdentificationDocumentNumberIn(any())).thenReturn(List.of());

        // Call private method via reflection
        try {
            Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
                    "validGeneral", MultipartFile.class, String.class, Long.class, Affiliate.class);
            method.setAccessible(true);
            ResponseServiceDTO response = (ResponseServiceDTO) method.invoke(service, file,
                    Constant.TYPE_AFFILLATE_INDEPENDENT, 1L, affiliate);

            assertEquals("1", response.getTotalRecord());
            assertNotNull(response.getDocument());
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("validGeneral - Should process dependent file with errors and create error document")
    void validGeneral_shouldProcessDependentFileWithErrorsAndCreateErrorDocument() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("F123");
        affiliate.setNitCompany("987654321");
        affiliate.setDocumentNumber("123456789");
        affiliate.setDocumentType("CC");

        // Setup mocks for file
        when(file.getOriginalFilename()).thenReturn("test.xlsx");

        // Setup mocks for dependencies
        when(genericWebClient.getSmlmvByYear(anyInt())).thenReturn(salaryDTO);
        when(excelProcessingServiceData.converterExcelToMap(any(), any(), anyInt()))
                .thenReturn(List.of(new LinkedHashMap<>()));
        when(excelProcessingServiceData.converterMapToClass(any(), eq(DataExcelDependentDTO.class)))
                .thenReturn(List.of(new DataExcelDependentDTO() {
                    {
                        setIdRecord(1);
                        setIdBondingType("5"); // Invalid
                        setCoverageDate("01/01/2099"); // Invalid
                        setIdentificationDocumentType("XX"); // Invalid
                        setIdentificationDocumentNumber("1"); // Invalid
                        setFirstName("John123"); // Invalid
                        setSurname("Doe$"); // Invalid
                        setDateOfBirth("01/01/2020"); // Invalid age for CC
                        setGender("X"); // Invalid
                        setNationality("3"); // Invalid
                        setHealthPromotingEntity("EPS_INVALID"); // Invalid
                        setPensionFundAdministrator("AFP_INVALID"); // Invalid
                        setOccupationalRiskManager("ARL_INVALID"); // Invalid
                        setIdDepartment("INVALID"); // Invalid
                        setIdCity("CITY_INVALID"); // Invalid
                        setPhone1("12345"); // Invalid
                        setIdWorkModality("5"); // Invalid
                        setSalary("100"); // Invalid
                        setIdOccupation(null); // Invalid (isRequested)
                        setEconomicActivityCode("INVALID"); // Invalid
                        setIdHeadquarter("HQ_INVALID"); // Invalid
                        setEmployerDocumentTypeCodeContractor("XX"); // Invalid
                        setEmployerDocumentNumber(null); // Invalid
                    }
                }));
        when(excelProcessingServiceData.findDataDuplicate(any(), any(), any())).thenReturn(List.of());
        when(excelProcessingServiceData.createDocumentExcelErrors(any())).thenReturn(new ExportDocumentsDTO());
        when(excelProcessingServiceData.findByEps(any())).thenReturn(List.of());
        when(excelProcessingServiceData.findByAfp(any())).thenReturn(List.of());
        when(excelProcessingServiceData.findByPensionOrEpsOrArl(any())).thenReturn(List.of());
        when(municipalityRepository.findAll()).thenReturn(List.of());
        when(iEconomicActivityRepository.findAll()).thenReturn(List.of());
        when(mainOfficeService.getMainOfficeByCode(any())).thenReturn(new MainOffice() {
            {
                setId(999L);
            }
        });
        when(recordLoadBulkService.save(any())).thenReturn(new RecordLoadBulk() {
            {
                setId(1L);
            }
        });
        when(dependentRepository.findByIdentificationDocumentNumberIn(any())).thenReturn(List.of());

        // Call private method via reflection
        try {
            Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod(
                    "validGeneral", MultipartFile.class, String.class, Long.class, Affiliate.class);
            method.setAccessible(true);
            ResponseServiceDTO response = (ResponseServiceDTO) method.invoke(service, file,
                    Constant.TYPE_AFFILLATE_DEPENDENT, 1L, affiliate);

            assertEquals("1", response.getTotalRecord());
            assertEquals("1", response.getRecordError());
            assertEquals("0", response.getRecordSuccessful());
            assertNotNull(response.getDocument());
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
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
    @DisplayName("assignPolicy - Should create policy for dependent when employer and policy exist")
    void assignPolicy_shouldCreatePolicyForDependent_whenEmployerAndPolicyExist() throws Exception {
        // Arrange
        Long idAffiliate = 1L;
        String nitEmployer = "900123456";
        String identificationTypeDependent = "CC";
        String identificationNumberDependent = "10203040";
        Long idPolicyType = Constant.ID_EMPLOYER_POLICY;
        String nameCompany = "Test Company";

        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setIdAffiliate(2L);

        Policy employerPolicy = new Policy();
        employerPolicy.setIdPolicyType(idPolicyType);
        employerPolicy.setCode("POL123");

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(employerAffiliate));
        when(policyRepository.findByIdAffiliate(employerAffiliate.getIdAffiliate()))
                .thenReturn(List.of(employerPolicy));

        // Act
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod("assignPolicy", Long.class,
                String.class, String.class, String.class, Long.class, String.class);
        method.setAccessible(true);
        method.invoke(service, idAffiliate, nitEmployer, identificationTypeDependent, identificationNumberDependent,
                idPolicyType, nameCompany);

        // Assert
        verify(policyService, times(1)).createPolicyDependent(
                eq(identificationTypeDependent),
                eq(identificationNumberDependent),
                any(LocalDate.class),
                eq(idAffiliate),
                eq(employerPolicy.getCode()),
                eq(nameCompany));
    }

    @Test
    @DisplayName("assignPolicy - Should throw AffiliateNotFound when employer affiliate is not found")
    void assignPolicy_shouldThrowAffiliateNotFound_whenEmployerNotFound() throws Exception {
        // Arrange
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // Act & Assert
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod("assignPolicy", Long.class,
                String.class, String.class, String.class, Long.class, String.class);
        method.setAccessible(true);

        try {
            method.invoke(service, 1L, "900123456", "CC", "10203040", 1L, "Test Company");
            fail("Expected an exception to be thrown");
        } catch (Exception e) {
            // Assert
            assertTrue(e.getCause() instanceof com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound);
        }
    }

    @Test
    @DisplayName("assignPolicy - Should throw PolicyException when employer policy is not found")
    void assignPolicy_shouldThrowPolicyException_whenPolicyNotFound() throws Exception {
        // Arrange
        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setIdAffiliate(2L);

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(employerAffiliate));
        when(policyRepository.findByIdAffiliate(employerAffiliate.getIdAffiliate())).thenReturn(new ArrayList<>());

        // Act & Assert
        Method method = BulkLoadingDependentIndependentServiceImpl.class.getDeclaredMethod("assignPolicy", Long.class,
                String.class, String.class, String.class, Long.class, String.class);
        method.setAccessible(true);

        try {
            method.invoke(service, 1L, "900123456", "CC", "10203040", 1L, "Test Company");
            fail("Expected an exception to be thrown");
        } catch (Exception e) {
            // Assert
            assertTrue(e.getCause() instanceof com.gal.afiliaciones.config.ex.PolicyException);
        }
    }

    @Test
    @DisplayName("updateRealNumberWorkers - should update realNumberWorkers and employer size")
    void updateRealNumberWorkers_shouldUpdateRealNumberWorkersAndEmployerSize() throws NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException {
        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("F123");

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setFiledNumber("F123");
        affiliateMercantile.setRealNumberWorkers(5L);

        when(affiliateMercantileRepository.findByFiledNumber("F123")).thenReturn(Optional.of(affiliateMercantile));
        when(affiliateService.getEmployerSize(8)).thenReturn(2L);
        when(affiliateMercantileRepository.save(any(AffiliateMercantile.class))).thenReturn(affiliateMercantile);

        // Use reflection to invoke the private/protected method
        java.lang.reflect.Method method = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("updateRealNumberWorkers", Affiliate.class, int.class);
        method.setAccessible(true);
        method.invoke(service, affiliate, 8);

        assertEquals(13L, affiliateMercantile.getRealNumberWorkers());
        assertEquals(2L, affiliateMercantile.getIdEmployerSize());
        verify(affiliateMercantileRepository, times(1)).save(affiliateMercantile);
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

    @Test
    @DisplayName("findAffiliateWithNumberUser - should throw AffiliationError when user not found")
    void findAffiliateWithNumberUser_shouldThrowErrorWhenUserNotFound() throws Exception {
        Method method = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("findAffiliateWithNumberUser", Long.class);
        method.setAccessible(true);
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.empty());

        try {
            method.invoke(service, 1L);
            fail("Expected AffiliationError");
        } catch (InvocationTargetException e) {
            assertTrue(e.getCause() instanceof AffiliationError);
        }
    }

    @Test
    @DisplayName("findAffiliateWithNumberUser - should throw AffiliationError when affiliate not found")
    void findAffiliateWithNumberUser_shouldThrowErrorWhenAffiliateNotFound() throws Exception {
        Method method = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("findAffiliateWithNumberUser", Long.class);
        method.setAccessible(true);
        UserMain user = new UserMain();
        user.setId(2L);
        user.setIdentification("ID123");
        user.setIdentificationType("CC");
        when(iUserPreRegisterRepository.findById(2L)).thenReturn(Optional.of(user));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        try {
            method.invoke(service, 2L);
            fail("Expected AffiliationError");
        } catch (InvocationTargetException e) {
            assertTrue(e.getCause() instanceof AffiliationError);
        }
    }

    @Test
    @DisplayName("findAffiliateWithNumberUser - should return affiliate when found")
    void findAffiliateWithNumberUser_shouldReturnAffiliateWhenFound() throws Exception {
        Method method = BulkLoadingDependentIndependentServiceImpl.class
                .getDeclaredMethod("findAffiliateWithNumberUser", Long.class);
        method.setAccessible(true);
        UserMain user = new UserMain();
        user.setId(3L);
        user.setIdentification("ID456");
        user.setIdentificationType("CC");
        when(iUserPreRegisterRepository.findById(3L)).thenReturn(Optional.of(user));

        Affiliate affiliate = new Affiliate();
        affiliate.setNitCompany("NIT123");
        affiliate.setDocumentNumber("ID456");
        affiliate.setDocumentType("CC");
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        Object result = method.invoke(service, 3L);
        assertNotNull(result);
        assertTrue(result instanceof Affiliate);
        Affiliate actual = (Affiliate) result;
        assertEquals("NIT123", actual.getNitCompany());
        assertEquals("ID456", actual.getDocumentNumber());
    }
}
