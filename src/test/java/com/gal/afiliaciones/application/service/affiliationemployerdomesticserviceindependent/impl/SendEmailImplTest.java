package com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import com.gal.afiliaciones.application.service.CertificateService;
import com.gal.afiliaciones.application.service.affiliate.MercantileFormService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.DomesticServiceIndependentServiceReportService;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.economicactivity.IEconomicActivityService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.individualindependentaffiliation.IndividualIndependentAffiliationService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.Department;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.FundPension;
import com.gal.afiliaciones.domain.model.Gender;
import com.gal.afiliaciones.domain.model.Health;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.Danger;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.GenderRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.DangerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.afp.FundPensionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.eps.HealthPromotingEntityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.form.ApplicationFormDao;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DataEmployerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.TemplateSendEmailsDTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ConsultFiles;
import com.gal.afiliaciones.infrastructure.dto.alfresco.Entries;
import com.gal.afiliaciones.infrastructure.dto.alfresco.Entry;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.OccupationDecree1563DTO;
import com.gal.afiliaciones.infrastructure.dto.individualindependentaffiliation.IndividualIndependentAffiliationDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.DataEmailApplyDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.DataEmailNotApplyDTO;
import com.gal.afiliaciones.infrastructure.dto.otp.email.EmailDataDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.DataEmailUpdateEmployerDTO;
import com.gal.afiliaciones.infrastructure.utils.EmailService;

import jakarta.mail.MessagingException;

class SendEmailImplTest {

    @Mock
    private EmailService emailService;
    @Mock
    private CertificateService certificateService;
    @Mock
    private GenericWebClient webClient;
    @Mock
    private ArlInformationDao arlInformationDao;
    @Mock
    private IndividualIndependentAffiliationService formIndependentService;
    @Mock
    private DangerRepository dangerRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private MunicipalityRepository municipalityRepository;
    @Mock
    private IEconomicActivityRepository economicActivityRepository;
    @Mock
    private GenderRepository genderRepository;
    @Mock
    private IEconomicActivityService economicActivityService;
    @Mock
    private ApplicationFormDao applicationFormDao;
    @Mock
    private MercantileFormService mercantileFormService;
    @Mock
    private CollectProperties properties;
    @Mock
    private AffiliateRepository affiliateRepository;
    @Mock
    private DomesticServiceIndependentServiceReportService domesticServiceIndependentServiceReportService;
    @Mock
    private FiledService filedService;
    @Mock
    private HealthPromotingEntityRepository healthRepository;
    @Mock
    private FundPensionRepository pensionRepository;

    @InjectMocks
    private SendEmailImpl sendEmailImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ArlInformation arlInfo = new ArlInformation();
        arlInfo.setId(1L);
        arlInfo.setName("ARL Test");
        arlInfo.setEmail("test@arl.com");
        arlInfo.setNit("123456");
        when(arlInformationDao.findAllArlInformation()).thenReturn(
                Collections.singletonList(arlInfo));
        when(properties.getLinkLogin()).thenReturn("http://login.url");
        when(properties.getNodeFirmas()).thenReturn("nodeFirmas");
        when(properties.getCustomerServiceUrl()).thenReturn("http://customer.service");
    }

    @Test
    void testRequestDenied() throws MessagingException, IOException {
        Affiliation affiliation = mock(Affiliation.class);
        when(affiliation.getIdentificationDocumentNumber()).thenReturn("123");
        when(affiliation.getFirstName()).thenReturn("John");
        when(affiliation.getSurname()).thenReturn("Doe");
        when(affiliation.getSecondName()).thenReturn(null);
        when(affiliation.getSecondSurname()).thenReturn(null);
        when(affiliation.getIdentificationDocumentType()).thenReturn("CC");
        when(affiliation.getFiledNumber()).thenReturn("F123");
        when(affiliation.getEmail()).thenReturn("john@doe.com");

        StringBuilder observation = new StringBuilder("Some observation");

        sendEmailImpl.requestDenied(affiliation, observation);

        verify(emailService).sendSimpleMessage(any(EmailDataDTO.class), anyString());
    }

    @Test
    void testRequestDeniedDocumentsMercantile() throws MessagingException, IOException {
        TemplateSendEmailsDTO dto = mock(TemplateSendEmailsDTO.class);
        when(dto.getIdentification()).thenReturn("123");
        when(dto.getFirstName()).thenReturn("Jane");
        when(dto.getSurname()).thenReturn("Smith");
        when(dto.getSecondName()).thenReturn(null);
        when(dto.getSecondSurname()).thenReturn(null);
        when(dto.getIdentificationType()).thenReturn("CC");
        when(dto.getFieldNumber()).thenReturn("F456");
        when(dto.getEmail()).thenReturn("jane@smith.com");

        StringBuilder observation = new StringBuilder("Obs");

        sendEmailImpl.requestDeniedDocumentsMercantile(dto, observation);

        verify(emailService).sendSimpleMessage(any(EmailDataDTO.class), anyString());
    }

    @Test
    void testConfirmationInterviewWeb() throws MessagingException, IOException {
        TemplateSendEmailsDTO dto = mock(TemplateSendEmailsDTO.class);
        when(dto.getFirstName()).thenReturn("Jane");
        when(dto.getSurname()).thenReturn("Smith");
        when(dto.getSecondName()).thenReturn(null);
        when(dto.getSecondSurname()).thenReturn(null);
        when(dto.getIdentificationType()).thenReturn("CC");
        when(dto.getIdentification()).thenReturn("123");
        when(dto.getFieldNumber()).thenReturn("F456");
        when(dto.getEmail()).thenReturn("jane@smith.com");
        when(dto.getDateInterview()).thenReturn(LocalDateTime.now());

        sendEmailImpl.confirmationInterviewWeb(dto);

        verify(emailService).sendSimpleMessage(any(EmailDataDTO.class), anyString());
    }

    @Test
    void testInterviewWebApproved() throws MessagingException, IOException {
        TemplateSendEmailsDTO dto = mock(TemplateSendEmailsDTO.class);
        when(dto.getFirstName()).thenReturn("Jane");
        when(dto.getSurname()).thenReturn("Smith");
        when(dto.getSecondName()).thenReturn(null);
        when(dto.getSecondSurname()).thenReturn(null);
        when(dto.getIdentificationType()).thenReturn("CC");
        when(dto.getIdentification()).thenReturn("123");
        when(dto.getFieldNumber()).thenReturn("F456");
        when(dto.getEmail()).thenReturn("jane@smith.com");

        sendEmailImpl.interviewWebApproved(dto);

        verify(emailService).sendSimpleMessage(any(EmailDataDTO.class), anyString());
    }

    @Test
    void testEmailUpdateEmployer() throws MessagingException, IOException {
        DataEmailUpdateEmployerDTO dataEmail = mock(DataEmailUpdateEmployerDTO.class);
        when(dataEmail.getNameEmployer()).thenReturn("Employer");
        when(dataEmail.getSectionUpdated()).thenReturn("Section");
        when(dataEmail.getEmailEmployer()).thenReturn("employer@company.com");

        sendEmailImpl.emailUpdateEmployer(dataEmail);

        verify(emailService).sendSimpleMessage(any(EmailDataDTO.class), anyString());
    }

    @Test
    void testEmailUpdateMassiveWorkers() throws MessagingException, IOException {
        DataEmailUpdateEmployerDTO dataEmail = mock(DataEmailUpdateEmployerDTO.class);
        when(dataEmail.getNameEmployer()).thenReturn("Employer");
        when(dataEmail.getEmailEmployer()).thenReturn("employer@company.com");

        MultipartFile file = mock(MultipartFile.class);

        sendEmailImpl.emailUpdateMassiveWorkers(file, dataEmail);

        verify(emailService).sendManyFilesMessage(any(EmailDataDTO.class), anyString());
    }

    @Test
    void testEmailBulkLoad() throws MessagingException, IOException {
        MultipartFile file = mock(MultipartFile.class);

        sendEmailImpl.emailBulkLoad("Company", "company@email.com", file);

        verify(emailService).sendManyFilesMessage(any(EmailDataDTO.class), anyString());
    }

    @Test
    void testSendEmailHeadquarters() throws MessagingException, IOException {
        Map<String, Object> data = new HashMap<>();
        sendEmailImpl.sendEmailHeadquarters(data, "hq@email.com");
        verify(emailService).sendSimpleMessage(any(EmailDataDTO.class), anyString());
    }

    @Test
    void testEmailWelcomeRegister() throws MessagingException, IOException {
        UserMain user = mock(UserMain.class);
        when(user.getFirstName()).thenReturn("User");
        when(user.getSurname()).thenReturn("Main");
        when(user.getIdentificationType()).thenReturn("CC");
        when(user.getIdentification()).thenReturn("123");
        when(user.getEmail()).thenReturn("user@main.com");

        sendEmailImpl.emailWelcomeRegister(user);

        verify(emailService).sendSimpleMessage(any(EmailDataDTO.class), anyString());
    }

    @Test
    void testEmailApplyPILA() throws MessagingException, IOException {
        DataEmailApplyDTO dataEmail = mock(DataEmailApplyDTO.class);
        when(dataEmail.getNovelty()).thenReturn("Novedad");
        when(dataEmail.getFiledNumber()).thenReturn("F123");
        when(dataEmail.getCompleteName()).thenReturn("John Doe");
        when(dataEmail.getPayrollNumber()).thenReturn("P123");
        when(dataEmail.getEmailTo()).thenReturn("to@email.com");

        sendEmailImpl.emailApplyPILA(dataEmail);

        verify(emailService).sendSimpleMessage(any(EmailDataDTO.class), anyString());
    }

    @Test
    void testEmailNotApplyPILA() throws MessagingException, IOException {
        DataEmailNotApplyDTO dataEmail = mock(DataEmailNotApplyDTO.class);
        when(dataEmail.getNovelty()).thenReturn("Novedad");
        when(dataEmail.getFiledNumber()).thenReturn("F123");
        when(dataEmail.getCompleteName()).thenReturn("John Doe");
        when(dataEmail.getPayrollNumber()).thenReturn("P123");
        when(dataEmail.getCausal()).thenReturn("Causal");
        when(dataEmail.getEmailTo()).thenReturn("to@email.com");

        sendEmailImpl.emailNotApplyPILA(dataEmail);

        verify(emailService).sendSimpleMessage(any(EmailDataDTO.class), anyString());
    }

    @Test
    void testEmailNotRetirementPILA() throws MessagingException, IOException {
        Map<String, Object> data = new HashMap<>();
        sendEmailImpl.emailNotRetirementPILA(data, "to@email.com");
        // If emailService.sendSimpleMessage is not called, verify no interactions
        org.mockito.Mockito.verifyNoInteractions(emailService);
    }

    @Test
    void testCompleteInformationProvisionServicesOrCouncillor_withValidAffiliationWith7CharEconomicActivityCode()
            throws Exception {
        Affiliation affiliation = mock(Affiliation.class);
        IndividualIndependentAffiliationDTO dto = new IndividualIndependentAffiliationDTO();

        // Setup for 7-char code
        when(affiliation.getAddressIndependentWorker()).thenReturn("Address1");
        when(affiliation.getIdDepartmentIndependentWorker()).thenReturn(1L);
        when(affiliation.getIdCityIndependentWorker()).thenReturn(2L);
        when(affiliation.getContractType()).thenReturn("ContractType");
        when(affiliation.getContractQuality()).thenReturn("ContractQuality");
        when(affiliation.getTransportSupply()).thenReturn(true);
        when(affiliation.getStartDate()).thenReturn(LocalDate.of(2024, 6, 1));
        when(affiliation.getEndDate()).thenReturn(LocalDate.of(2024, 7, 1));
        when(affiliation.getDuration()).thenReturn("12.00");
        when(affiliation.getJourneyEstablished()).thenReturn("Full");
        when(affiliation.getContractTotalValue()).thenReturn(BigDecimal.valueOf(1000L));
        when(affiliation.getContractMonthlyValue()).thenReturn(BigDecimal.valueOf(100L));
        when(affiliation.getContractIbcValue()).thenReturn(BigDecimal.valueOf(50L));
        when(affiliation.getCodeMainEconomicActivity()).thenReturn("1ABCD12");
        when(affiliation.getOccupation()).thenReturn("Occupation");
        when(affiliation.getAddressContractDataStep2()).thenReturn("Address2");
        when(affiliation.getIdDepartmentWorkDataCenter()).thenReturn(3L);
        when(affiliation.getIdCityWorkDataCenter()).thenReturn(4L);
        when(affiliation.getCompanyName()).thenReturn("Company");
        when(affiliation.getIdentificationDocumentTypeContractor()).thenReturn("NIT");
        when(affiliation.getIdentificationDocumentNumberContractor()).thenReturn("900123456");
        when(affiliation.getDv()).thenReturn(5);
        when(affiliation.getEmailContractor()).thenReturn("contractor@email.com");
        when(affiliation.getPhone1WorkDataCenter()).thenReturn("123456789");
        when(affiliation.getFirstNameSignatory()).thenReturn("SigFirst");
        when(affiliation.getSecondNameSignatory()).thenReturn("SigSecond");
        when(affiliation.getSurnameSignatory()).thenReturn("SigSurname");
        when(affiliation.getSecondSurnameSignatory()).thenReturn("SigSecondSurname");
        when(affiliation.getIdentificationDocumentTypeSignatory()).thenReturn("CC");
        when(affiliation.getIdentificationDocumentNumberSignatory()).thenReturn("112233");
        // EconomicActivityService mock
        EconomicActivity activity = new EconomicActivity();
        activity.setDescription("Desc");
        activity.setClassRisk("1");
        activity.setCodeCIIU("ABCD");
        activity.setAdditionalCode("12");
        when(economicActivityService.getEconomicActivityByRiskCodeCIIUCodeAdditional("1", "ABCD", "12"))
                .thenReturn(activity);

        Department dep1 = new Department();
        dep1.setDepartmentName("Dep1");
        when(departmentRepository.findById(1L)).thenReturn(java.util.Optional.of(dep1));
        Department dep3 = new Department();
        dep3.setDepartmentName("Dep3");
        when(departmentRepository.findById(3L)).thenReturn(java.util.Optional.of(dep3));

        Municipality mun2 = new Municipality();
        mun2.setMunicipalityName("Mun2");
        when(municipalityRepository.findById(2L)).thenReturn(java.util.Optional.of(mun2));
        Municipality mun4 = new Municipality();
        mun4.setMunicipalityName("Mun4");
        when(municipalityRepository.findById(4L)).thenReturn(java.util.Optional.of(mun4));

        // Call method
        // Usando reflection para invocar el método no visible
        java.lang.reflect.Method method = SendEmailImpl.class.getDeclaredMethod(
                "completeInformationProvisionServicesOrCouncillor",
                IndividualIndependentAffiliationDTO.class,
                Affiliation.class);
        method.setAccessible(true);
        method.invoke(sendEmailImpl, dto, affiliation);

        // Assertions
        assertEquals("Address1", dto.getAddressGI());
        assertEquals("Dep1", dto.getDepartmentGI());
        assertEquals("Mun2", dto.getCityOrDistrictGI());
        assertEquals("ContractType", dto.getContractTypeACI());
        assertEquals("ContractQuality", dto.getContractQualityACI());
        assertTrue(dto.getTransportSupplyACI());
        assertEquals("2024-06-01", dto.getContractStartDateACI());
        assertEquals("2024-07-01", dto.getContractEndDateACI());
        assertEquals("12", dto.getNumberOfMonthsACI());
        assertEquals("Full", dto.getEstablishedWorkShiftACI());
        assertEquals("1000", dto.getTotalContractValueACI());
        assertEquals("100", dto.getMonthlyContractValueACI());
        assertEquals("50", dto.getBaseContributionIncomeACI());
        assertEquals("Desc", dto.getActivityCarriedACI());
        assertEquals("1ABCD12", dto.getEconomicActivityCodeACI());
        assertEquals("Occupation", dto.getJobPositionACI());
        assertFalse(dto.getTaxiDriverACI());
        assertEquals("Address2", dto.getAddressACI());
        assertEquals("Dep3", dto.getDepartmentACI());
        assertEquals("Mun4", dto.getCityOrDistrictACI());
        assertEquals("Company", dto.getFullNameOrBusinessNameCI());
        assertEquals("NIT", dto.getIdentificationDocumentTypeCI());
        assertEquals("900123456", dto.getIdentificationDocumentNumberCI());
        assertEquals("5", dto.getDvCI());
        assertEquals("1ABCD12", dto.getEconomicActivityCodeCI());
        assertEquals("Dep3", dto.getDepartmentCI());
        assertEquals("Mun4", dto.getCityOrDistrictCI());
        assertEquals("123456789", dto.getMobileOrLandlineCI());
        assertEquals("contractor@email.com", dto.getEmailCI());
        assertEquals("SigFirst SigSecond SigSurname SigSecondSurname", dto.getFullNameOrBusinessNameICS());
        assertEquals("CC", dto.getIdentificationDocumentTypeICS());
        assertEquals("112233", dto.getIdentificationDocumentNumberICS());
        assertEquals("1ABCD12", dto.getEconomicActivityCodeARL());
    }

    @Test
    void testCompleteInformationProvisionServicesOrCouncillor_withValidAffiliationWithNon7CharEconomicActivityCode()
            throws NoSuchMethodException {
        Affiliation affiliation = mock(Affiliation.class);
        IndividualIndependentAffiliationDTO dto = new IndividualIndependentAffiliationDTO();

        // Setup for non-7-char code
        when(affiliation.getAddressIndependentWorker()).thenReturn("AddressX");
        when(affiliation.getIdDepartmentIndependentWorker()).thenReturn(10L);
        when(affiliation.getIdCityIndependentWorker()).thenReturn(20L);
        when(affiliation.getContractType()).thenReturn("TypeX");
        when(affiliation.getContractQuality()).thenReturn("QualityX");
        when(affiliation.getTransportSupply()).thenReturn(false);
        when(affiliation.getStartDate()).thenReturn(LocalDate.of(2024, 1, 1));
        when(affiliation.getEndDate()).thenReturn(LocalDate.of(2024, 2, 1));
        when(affiliation.getDuration()).thenReturn("6.00");
        when(affiliation.getJourneyEstablished()).thenReturn("Partial");
        when(affiliation.getContractTotalValue()).thenReturn(BigDecimal.valueOf(2000L));
        when(affiliation.getContractMonthlyValue()).thenReturn(BigDecimal.valueOf(200L));
        when(affiliation.getContractIbcValue()).thenReturn(BigDecimal.valueOf(150L));
        when(affiliation.getCodeMainEconomicActivity()).thenReturn("12345");
        when(affiliation.getOccupation()).thenReturn("OccX");
        when(affiliation.getAddressContractDataStep2()).thenReturn("AddrY");
        when(affiliation.getIdDepartmentWorkDataCenter()).thenReturn(30L);
        when(affiliation.getIdCityWorkDataCenter()).thenReturn(40L);
        when(affiliation.getCompanyName()).thenReturn("CompX");
        when(affiliation.getIdentificationDocumentTypeContractor()).thenReturn("NITX");
        when(affiliation.getIdentificationDocumentNumberContractor()).thenReturn("800123456");
        when(affiliation.getDv()).thenReturn(8);
        when(affiliation.getEmailContractor()).thenReturn("contx@email.com");
        when(affiliation.getPhone1WorkDataCenter()).thenReturn("987654321");
        when(affiliation.getFirstNameSignatory()).thenReturn("FSign");
        when(affiliation.getSecondNameSignatory()).thenReturn("SSign");
        when(affiliation.getSurnameSignatory()).thenReturn("SurSign");
        when(affiliation.getSecondSurnameSignatory()).thenReturn("SSurSign");
        when(affiliation.getIdentificationDocumentTypeSignatory()).thenReturn("TI");
        when(affiliation.getIdentificationDocumentNumberSignatory()).thenReturn("445566");
        // EconomicActivityRepository mock
        EconomicActivity activity = new EconomicActivity();
        activity.setDescription("DescX");
        activity.setClassRisk("2");
        activity.setCodeCIIU("CDE");
        activity.setAdditionalCode("34");
        when(economicActivityRepository.findById(12345L)).thenReturn(java.util.Optional.of(activity));

        Department dep10 = new Department();
        dep10.setDepartmentName("Dep10");
        when(departmentRepository.findById(10L)).thenReturn(java.util.Optional.of(dep10));
        Department dep30 = new Department();
        dep30.setDepartmentName("Dep30");
        when(departmentRepository.findById(30L)).thenReturn(java.util.Optional.of(dep30));

        Municipality mun20 = new Municipality();
        mun20.setMunicipalityName("Mun20");
        when(municipalityRepository.findById(20L)).thenReturn(java.util.Optional.of(mun20));
        Municipality mun40 = new Municipality();
        mun40.setMunicipalityName("Mun40");
        when(municipalityRepository.findById(40L)).thenReturn(java.util.Optional.of(mun40));

        // Call method
        // Usando reflection para invocar el método no visible
        java.lang.reflect.Method method = SendEmailImpl.class.getDeclaredMethod(
                "completeInformationProvisionServicesOrCouncillor",
                IndividualIndependentAffiliationDTO.class,
                Affiliation.class);
        method.setAccessible(true);
        try {
            method.invoke(sendEmailImpl, dto, affiliation);
        } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        // Assertions
        assertEquals("AddressX", dto.getAddressGI());
        assertEquals("Dep10", dto.getDepartmentGI());
        assertEquals("Mun20", dto.getCityOrDistrictGI());
        assertEquals("TypeX", dto.getContractTypeACI());
        assertEquals("QualityX", dto.getContractQualityACI());
        assertFalse(dto.getTransportSupplyACI());
        assertEquals("2024-01-01", dto.getContractStartDateACI());
        assertEquals("2024-02-01", dto.getContractEndDateACI());
        assertEquals("6", dto.getNumberOfMonthsACI());
        assertEquals("Partial", dto.getEstablishedWorkShiftACI());
        assertEquals("2000", dto.getTotalContractValueACI());
        assertEquals("200", dto.getMonthlyContractValueACI());
        assertEquals("150", dto.getBaseContributionIncomeACI());
        assertEquals("DescX", dto.getActivityCarriedACI());
        assertEquals("2CDE34", dto.getEconomicActivityCodeACI());
        assertEquals("OccX", dto.getJobPositionACI());
        assertFalse(dto.getTaxiDriverACI());
        assertEquals("AddrY", dto.getAddressACI());
        assertEquals("Dep30", dto.getDepartmentACI());
        assertEquals("Mun40", dto.getCityOrDistrictACI());
        assertEquals("CompX", dto.getFullNameOrBusinessNameCI());
        assertEquals("NITX", dto.getIdentificationDocumentTypeCI());
        assertEquals("800123456", dto.getIdentificationDocumentNumberCI());
        assertEquals("8", dto.getDvCI());
        assertEquals("2CDE34", dto.getEconomicActivityCodeCI());
        assertEquals("Dep30", dto.getDepartmentCI());
        assertEquals("Mun40", dto.getCityOrDistrictCI());
        assertEquals("987654321", dto.getMobileOrLandlineCI());
        assertEquals("contx@email.com", dto.getEmailCI());
        assertEquals("FSign SSign SurSign SSurSign", dto.getFullNameOrBusinessNameICS());
        assertEquals("TI", dto.getIdentificationDocumentTypeICS());
        assertEquals("445566", dto.getIdentificationDocumentNumberICS());
        assertEquals("2CDE34", dto.getEconomicActivityCodeARL());
    }

    @Test
    void testCompleteInformationTaxiDriver_withValidAffiliation() throws Exception {
        Affiliation affiliation = mock(Affiliation.class);
        IndividualIndependentAffiliationDTO dto = new IndividualIndependentAffiliationDTO();

        when(affiliation.getContractStartDate()).thenReturn(LocalDate.of(2024, 5, 1));
        when(affiliation.getContractEndDate()).thenReturn(LocalDate.of(2024, 6, 1));
        when(affiliation.getContractDuration()).thenReturn("12.00");
        when(affiliation.getContractTotalValue()).thenReturn(BigDecimal.valueOf(5000L));
        when(affiliation.getContractMonthlyValue()).thenReturn(BigDecimal.valueOf(500L));
        when(affiliation.getContractIbcValue()).thenReturn(BigDecimal.valueOf(300L));
        when(affiliation.getOccupation()).thenReturn("Taxi Driver");
        when(affiliation.getCodeMainEconomicActivity()).thenReturn("1ABCD12");
        when(affiliation.getAddressWorkDataCenter()).thenReturn("Taxi Address");
        when(affiliation.getIdDepartmentWorkDataCenter()).thenReturn(11L);
        when(affiliation.getIdCityWorkDataCenter()).thenReturn(22L);
        when(affiliation.getCompanyName()).thenReturn("Taxi Company");
        when(affiliation.getIdentificationDocumentTypeContractor()).thenReturn("NIT");
        when(affiliation.getIdentificationDocumentNumberContractor()).thenReturn("900987654");
        when(affiliation.getDv()).thenReturn(7);
        when(affiliation.getEmailContractor()).thenReturn("taxi@company.com");
        when(affiliation.getPhone1WorkDataCenter()).thenReturn("321654987");

        Department dep = new Department();
        dep.setDepartmentName("TaxiDept");
        when(departmentRepository.findById(11L)).thenReturn(java.util.Optional.of(dep));
        Municipality mun = new Municipality();
        mun.setMunicipalityName("TaxiCity");
        when(municipalityRepository.findById(22L)).thenReturn(java.util.Optional.of(mun));

        // Usando reflection para invocar el método no visible
        java.lang.reflect.Method method = SendEmailImpl.class.getDeclaredMethod(
                "completeInformationTaxiDriver",
                IndividualIndependentAffiliationDTO.class,
                Affiliation.class,
                String.class);
        method.setAccessible(true);
        method.invoke(sendEmailImpl, dto, affiliation, "12");

        assertEquals("Civil", dto.getContractTypeACI());
        assertEquals("Privado", dto.getContractQualityACI());
        assertFalse(dto.getTransportSupplyACI());
        assertEquals("2024-05-01", dto.getContractStartDateACI());
        assertEquals("2024-06-01", dto.getContractEndDateACI());
        assertEquals("12", dto.getNumberOfMonthsACI());
        assertEquals("5000", dto.getTotalContractValueACI());
        assertEquals("500", dto.getMonthlyContractValueACI());
        assertEquals("300", dto.getBaseContributionIncomeACI());
        assertEquals("Taxi Driver", dto.getJobPositionACI());
        assertEquals("1ABCD12", dto.getEconomicActivityCodeACI());
        assertTrue(dto.getTaxiDriverACI());
        assertEquals("Taxi Address", dto.getAddressACI());
        assertEquals("Taxidept", dto.getDepartmentACI());
        assertEquals("Taxicity", dto.getCityOrDistrictACI());
        assertEquals("Taxi Company", dto.getFullNameOrBusinessNameCI());
        assertEquals("NIT", dto.getIdentificationDocumentTypeCI());
        assertEquals("900987654", dto.getIdentificationDocumentNumberCI());
        assertEquals("Taxi Address", dto.getAddressCI());
        assertEquals("Taxidept", dto.getDepartmentCI());
        assertEquals("Taxicity", dto.getCityOrDistrictCI());
        assertEquals("7", dto.getDvCI());
        assertEquals("taxi@company.com", dto.getEmailCI());
        assertEquals("321654987", dto.getMobileOrLandlineCI());
    }

    @Test
    void testCompleteInformationTaxiDriver_withNullDepartmentAndCity() throws Exception {
        Affiliation affiliation = mock(Affiliation.class);
        IndividualIndependentAffiliationDTO dto = new IndividualIndependentAffiliationDTO();

        when(affiliation.getContractStartDate()).thenReturn(LocalDate.of(2024, 3, 1));
        when(affiliation.getContractEndDate()).thenReturn(LocalDate.of(2024, 4, 1));
        when(affiliation.getContractDuration()).thenReturn("8.00");
        when(affiliation.getContractTotalValue()).thenReturn(BigDecimal.valueOf(8000L));
        when(affiliation.getContractMonthlyValue()).thenReturn(BigDecimal.valueOf(800L));
        when(affiliation.getContractIbcValue()).thenReturn(BigDecimal.valueOf(400L));
        when(affiliation.getOccupation()).thenReturn("Taxi Driver");
        when(affiliation.getCodeMainEconomicActivity()).thenReturn("1ABCD12");
        when(affiliation.getAddressWorkDataCenter()).thenReturn("Taxi Address 2");
        when(affiliation.getIdDepartmentWorkDataCenter()).thenReturn(null);
        when(affiliation.getIdCityWorkDataCenter()).thenReturn(null);
        when(affiliation.getCompanyName()).thenReturn("Taxi Company 2");
        when(affiliation.getIdentificationDocumentTypeContractor()).thenReturn("NIT");
        when(affiliation.getIdentificationDocumentNumberContractor()).thenReturn("900123789");
        when(affiliation.getDv()).thenReturn(9);
        when(affiliation.getEmailContractor()).thenReturn("taxi2@company.com");
        when(affiliation.getPhone1WorkDataCenter()).thenReturn("987654321");

        java.lang.reflect.Method method = SendEmailImpl.class.getDeclaredMethod(
                "completeInformationTaxiDriver",
                IndividualIndependentAffiliationDTO.class,
                Affiliation.class,
                String.class);
        method.setAccessible(true);
        method.invoke(sendEmailImpl, dto, affiliation, "8");

        assertEquals("Civil", dto.getContractTypeACI());
        assertEquals("Privado", dto.getContractQualityACI());
        assertFalse(dto.getTransportSupplyACI());
        assertEquals("2024-03-01", dto.getContractStartDateACI());
        assertEquals("2024-04-01", dto.getContractEndDateACI());
        assertEquals("8", dto.getNumberOfMonthsACI());
        assertEquals("8000", dto.getTotalContractValueACI());
        assertEquals("800", dto.getMonthlyContractValueACI());
        assertEquals("400", dto.getBaseContributionIncomeACI());
        assertEquals("Taxi Driver", dto.getJobPositionACI());
        assertEquals("1ABCD12", dto.getEconomicActivityCodeACI());
        assertTrue(dto.getTaxiDriverACI());
        assertEquals("Taxi Address 2", dto.getAddressACI());
        assertNull(dto.getDepartmentACI());
        assertNull(dto.getCityOrDistrictACI());
        assertEquals("Taxi Company 2", dto.getFullNameOrBusinessNameCI());
        assertEquals("NIT", dto.getIdentificationDocumentTypeCI());
        assertEquals("900123789", dto.getIdentificationDocumentNumberCI());
        assertEquals("Taxi Address 2", dto.getAddressCI());
        assertNull(dto.getDepartmentCI());
        assertNull(dto.getCityOrDistrictCI());
        assertEquals("9", dto.getDvCI());
        assertEquals("taxi2@company.com", dto.getEmailCI());
        assertEquals("987654321", dto.getMobileOrLandlineCI());
    }

    @Test
    void testCompleteInformationVolunteer_withValidAffiliation() throws Exception {
        Affiliation affiliation = mock(Affiliation.class);
        IndividualIndependentAffiliationDTO dto = new IndividualIndependentAffiliationDTO();

        // Datos básicos del afiliado voluntario
        when(affiliation.getContractEndDate()).thenReturn(LocalDate.of(2024, 8, 1));
        when(affiliation.getContractIbcValue()).thenReturn(BigDecimal.valueOf(1234L));
        when(affiliation.getOccupation()).thenReturn("Bombero");
        when(affiliation.getAddressEmployer()).thenReturn("Calle 123");
        when(affiliation.getDepartmentEmployer()).thenReturn(99L);
        when(affiliation.getMunicipalityEmployer()).thenReturn(88L);
        when(affiliation.getSecondaryPhone1()).thenReturn("5551234");
        when(affiliation.getEmail()).thenReturn("voluntario@email.com");
        when(affiliation.getId()).thenReturn(77L);

        // Mock de entidades relacionadas
        Department dep = new Department();
        dep.setDepartmentName("Antioquia");
        when(departmentRepository.findById(99L)).thenReturn(java.util.Optional.of(dep));
        Municipality mun = new Municipality();
        mun.setMunicipalityName("Medellin");
        when(municipalityRepository.findById(88L)).thenReturn(java.util.Optional.of(mun));

        // Mock para dangers
        Danger danger = new Danger();
        when(dangerRepository.findByIdAffiliation(77L)).thenReturn(danger);

        // Mock para ocupación voluntaria
        List<OccupationDecree1563DTO> occupations = new ArrayList<>();
        OccupationDecree1563DTO occ = new OccupationDecree1563DTO();
        occ.setOccupation("Bombero");
        occ.setCode(123L);
        occupations.add(occ);
        BodyResponseConfig<List<OccupationDecree1563DTO>> body = new BodyResponseConfig<>();
        body.setData(occupations);
        when(webClient.getOccupationsByVolunteer()).thenReturn(body);

        // Usando reflection para invocar el método no visible
        java.lang.reflect.Method method = SendEmailImpl.class.getDeclaredMethod(
                "completeInformationVolunteer",
                IndividualIndependentAffiliationDTO.class,
                Affiliation.class);
        method.setAccessible(true);
        method.invoke(sendEmailImpl, dto, affiliation);

        // Asserts
        assertEquals("Civil", dto.getContractTypeACI());
        assertEquals("Privado", dto.getContractQualityACI());
        assertFalse(dto.getTransportSupplyACI());
        assertEquals(LocalDate.now().toString(), dto.getContractStartDateACI());
        assertEquals("2024-08-01", dto.getContractEndDateACI());
        assertEquals("N/A", dto.getNumberOfMonthsACI());
        assertEquals("N/A", dto.getTotalContractValueACI());
        assertEquals("1234", dto.getMonthlyContractValueACI());
        assertEquals("1234", dto.getBaseContributionIncomeACI());
        assertEquals("N/A", dto.getActivityCarriedACI());
        assertEquals("N/A", dto.getEconomicActivityCodeACI());
        assertEquals("Bombero", dto.getJobPositionACI());
        assertFalse(dto.getTaxiDriverACI());
        assertEquals("Calle 123", dto.getAddressACI());
        assertEquals("Antioquia", dto.getDepartmentACI());
        assertEquals("Medellin", dto.getCityOrDistrictACI());
        assertEquals("NIT", dto.getIdentificationDocumentTypeCI());
        assertEquals("N/A", dto.getEconomicActivityCodeCI());
        assertEquals("Calle 123", dto.getAddressCI());
        assertEquals("Antioquia", dto.getDepartmentCI());
        assertEquals("Medellin", dto.getCityOrDistrictCI());
        assertEquals("5551234", dto.getMobileOrLandlineCI());
        assertEquals("voluntario@email.com", dto.getEmailCI());
        assertEquals("N/A", dto.getFullNameOrBusinessNameICS());
        assertEquals("N/A", dto.getIdentificationDocumentTypeICS());
        assertEquals("N/A", dto.getIdentificationDocumentNumberICS());
        assertEquals("123", dto.getEconomicActivityCodeARL());
        assertNotNull(dto.getAffiliationIndependentVolunteerStep2DTO());
    }

    @Test
    void testWelcomeMercantile_handlesExceptionGracefully() throws MessagingException, IOException {
        TemplateSendEmailsDTO dto = mock(TemplateSendEmailsDTO.class);
        when(dto.getFirstName()).thenReturn("Jane");
        when(dto.getSurname()).thenReturn("Smith");
        when(dto.getBusinessName()).thenReturn("BizName");
        when(dto.getIdentificationType()).thenReturn("CC");
        when(dto.getIdentification()).thenReturn("123456");
        when(dto.getTypeAffiliation()).thenReturn("TYPE");
        when(dto.getId()).thenReturn(99L);
        when(dto.getEmail()).thenReturn("jane@smith.com");
        when(dto.getFieldNumber()).thenReturn("F789");

        // Mock ARL info
        ArlInformation arlInfo = new ArlInformation();
        arlInfo.setId(1L);
        arlInfo.setName("ARL Test");
        arlInfo.setEmail("test@arl.com");
        arlInfo.setNit("123456");
        when(arlInformationDao.findAllArlInformation()).thenReturn(Collections.singletonList(arlInfo));
        when(properties.getLinkLogin()).thenReturn("http://login.url");

        // Simulate exception in certificate generation
        when(certificateService.createAndGenerateCertificate(any()))
                .thenThrow(new RuntimeException("Certificate error"));

        // Should not throw, just log error
        sendEmailImpl.welcomeMercantile(dto);

        // Should not send email if exception occurs before sending
        verify(emailService, org.mockito.Mockito.never()).sendManyFilesMessage(any(), any());
    }

    /*@Test
    void testFindNodeIdSignature_returnsId_whenSignatureExists() {
        // Arrange
        String identificationNumber = "123456789";
        String nodeFirmas = "nodeFirmas";
        String signatureNodeId = "signatureNodeId";

        when(webClient.folderExistsByName(nodeFirmas, identificationNumber)).thenReturn(Optional.of(signatureNodeId));

        // Act
        String result = null;
        try {
            java.lang.reflect.Method method = SendEmailImpl.class.getDeclaredMethod("findNodeIdSignature",
                    String.class);
            method.setAccessible(true);
            result = (String) method.invoke(sendEmailImpl, identificationNumber);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Assert
        assertEquals(signatureNodeId, result);
    }*/

    @Test
    void testFindNodeIdSignature_returnsNull_whenNoUserFolderFound() {
        String identificationNumber = "notfound";
        when(properties.getNodeFirmas()).thenReturn("nodeFirmas");
        when(webClient.folderExistsByName("nodeFirmas", identificationNumber)).thenReturn(Optional.empty());

        String result = null;
        try {
            java.lang.reflect.Method method = SendEmailImpl.class.getDeclaredMethod("findNodeIdSignature",
                    String.class);
            method.setAccessible(true);
            result = (String) method.invoke(sendEmailImpl, identificationNumber);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertNull(result);
    }

    /*@Test
    void testFindNodeIdSignature_returnsNull_whenNoUserEntryMatches() {
        String identificationNumber = "noMatch";
        String nodeFirmas = "nodeFirmas";

        when(webClient.folderExistsByName(nodeFirmas, identificationNumber)).thenReturn(Optional.of(nodeFirmas));
        when(properties.getNodeFirmas()).thenReturn(nodeFirmas);

        String result = null;
        try {
            java.lang.reflect.Method method = SendEmailImpl.class.getDeclaredMethod("findNodeIdSignature",
                    String.class);
            method.setAccessible(true);
            result = (String) method.invoke(sendEmailImpl, identificationNumber);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertNull(result);
    }*/

    @Test
    void testReminderInterviewWeb_sendsEmailWithCorrectData() throws Exception {
        TemplateSendEmailsDTO dto = mock(TemplateSendEmailsDTO.class);
        when(dto.getFirstName()).thenReturn("Ana");
        when(dto.getSurname()).thenReturn("Gomez");
        when(dto.getSecondName()).thenReturn("Maria");
        when(dto.getSecondSurname()).thenReturn("Perez");
        when(dto.getIdentificationType()).thenReturn("CC");
        when(dto.getIdentification()).thenReturn("123456");
        when(dto.getFieldNumber()).thenReturn("F789");
        when(dto.getEmail()).thenReturn("ana@gomez.com");
        LocalDateTime interviewDate = LocalDateTime.of(2024, 6, 10, 15, 30);
        when(dto.getDateInterview()).thenReturn(interviewDate);

        sendEmailImpl.reminderInterviewWeb(dto);

        verify(emailService).sendSimpleMessage(any(EmailDataDTO.class), anyString());
    }

    @Test
    void testReminderInterviewWeb_nullSecondNames() throws Exception {
        TemplateSendEmailsDTO dto = mock(TemplateSendEmailsDTO.class);
        when(dto.getFirstName()).thenReturn("Carlos");
        when(dto.getSurname()).thenReturn("Lopez");
        when(dto.getSecondName()).thenReturn(null);
        when(dto.getSecondSurname()).thenReturn(null);
        when(dto.getIdentificationType()).thenReturn("TI");
        when(dto.getIdentification()).thenReturn("654321");
        when(dto.getFieldNumber()).thenReturn("F321");
        when(dto.getEmail()).thenReturn("carlos@lopez.com");
        LocalDateTime interviewDate = LocalDateTime.of(2024, 7, 5, 10, 0);
        when(dto.getDateInterview()).thenReturn(interviewDate);

        sendEmailImpl.reminderInterviewWeb(dto);

        verify(emailService).sendSimpleMessage(any(EmailDataDTO.class), anyString());
    }

    @Test
    void testGenerateRequestCertificateDependent_withBondingType1() throws Exception {
        AffiliationDependent affiliation = mock(AffiliationDependent.class);
        when(affiliation.getIdentificationDocumentType()).thenReturn("CC");
        when(affiliation.getIdentificationDocumentNumber()).thenReturn("123456");
        Long idAffiliate = 10L;
        Long idBondingType = 1L;

        // Use reflection to access private method
        java.lang.reflect.Method method = SendEmailImpl.class.getDeclaredMethod(
                "generateRequestCertificateDependent",
                AffiliationDependent.class, Long.class, Long.class);
        method.setAccessible(true);
        Object result = method.invoke(sendEmailImpl, affiliation, idAffiliate, idBondingType);

        assertNotNull(result);
        assertTrue(result instanceof com.gal.afiliaciones.domain.model.affiliate.FindAffiliateReqDTO);
        com.gal.afiliaciones.domain.model.affiliate.FindAffiliateReqDTO dto = (com.gal.afiliaciones.domain.model.affiliate.FindAffiliateReqDTO) result;
        assertEquals(idAffiliate.intValue(), dto.getIdAffiliate());
        assertEquals("CC", dto.getDocumentType());
        assertEquals("123456", dto.getDocumentNumber());
        assertEquals("Dependiente", dto.getAffiliationType());
    }

    @Test
    void testGenerateRequestCertificateDependent_withBondingType2() throws Exception {
        AffiliationDependent affiliation = mock(AffiliationDependent.class);
        when(affiliation.getIdentificationDocumentType()).thenReturn("TI");
        when(affiliation.getIdentificationDocumentNumber()).thenReturn("654321");
        Long idAffiliate = 20L;
        Long idBondingType = 2L;

        java.lang.reflect.Method method = SendEmailImpl.class.getDeclaredMethod(
                "generateRequestCertificateDependent",
                AffiliationDependent.class, Long.class, Long.class);
        method.setAccessible(true);
        Object result = method.invoke(sendEmailImpl, affiliation, idAffiliate, idBondingType);

        assertNotNull(result);
        com.gal.afiliaciones.domain.model.affiliate.FindAffiliateReqDTO dto = (com.gal.afiliaciones.domain.model.affiliate.FindAffiliateReqDTO) result;
        assertEquals(idAffiliate.intValue(), dto.getIdAffiliate());
        assertEquals("TI", dto.getDocumentType());
        assertEquals("654321", dto.getDocumentNumber());
        assertEquals("Estudiante Decreto 055 de 2015", dto.getAffiliationType());
    }

    @Test
    void testGenerateRequestCertificateDependent_withBondingType3() throws Exception {
        AffiliationDependent affiliation = mock(AffiliationDependent.class);
        when(affiliation.getIdentificationDocumentType()).thenReturn("CE");
        when(affiliation.getIdentificationDocumentNumber()).thenReturn("789012");
        Long idAffiliate = 30L;
        Long idBondingType = 3L;

        java.lang.reflect.Method method = SendEmailImpl.class.getDeclaredMethod(
                "generateRequestCertificateDependent",
                AffiliationDependent.class, Long.class, Long.class);
        method.setAccessible(true);
        Object result = method.invoke(sendEmailImpl, affiliation, idAffiliate, idBondingType);

        assertNotNull(result);
        com.gal.afiliaciones.domain.model.affiliate.FindAffiliateReqDTO dto = (com.gal.afiliaciones.domain.model.affiliate.FindAffiliateReqDTO) result;
        assertEquals(idAffiliate.intValue(), dto.getIdAffiliate());
        assertEquals("CE", dto.getDocumentType());
        assertEquals("789012", dto.getDocumentNumber());
    }

    @Test
    void testGenerateRequestCertificateDependent_withBondingType4() throws Exception {
        AffiliationDependent affiliation = mock(AffiliationDependent.class);
        when(affiliation.getIdentificationDocumentType()).thenReturn("PA");
        when(affiliation.getIdentificationDocumentNumber()).thenReturn("345678");
        Long idAffiliate = 40L;
        Long idBondingType = 4L;

        java.lang.reflect.Method method = SendEmailImpl.class.getDeclaredMethod(
                "generateRequestCertificateDependent",
                AffiliationDependent.class, Long.class, Long.class);
        method.setAccessible(true);
        Object result = method.invoke(sendEmailImpl, affiliation, idAffiliate, idBondingType);

        assertNotNull(result);
        com.gal.afiliaciones.domain.model.affiliate.FindAffiliateReqDTO dto = (com.gal.afiliaciones.domain.model.affiliate.FindAffiliateReqDTO) result;
        assertEquals(idAffiliate.intValue(), dto.getIdAffiliate());
        assertEquals("PA", dto.getDocumentType());
        assertEquals("345678", dto.getDocumentNumber());
        assertEquals("Independiente", dto.getAffiliationType());
    }

    @Test
    void testGenerateRequestCertificateDependent_withUnknownBondingType() throws Exception {
        AffiliationDependent affiliation = mock(AffiliationDependent.class);
        when(affiliation.getIdentificationDocumentType()).thenReturn("RC");
        when(affiliation.getIdentificationDocumentNumber()).thenReturn("999999");
        Long idAffiliate = 50L;
        Long idBondingType = 99L;

        java.lang.reflect.Method method = SendEmailImpl.class.getDeclaredMethod(
                "generateRequestCertificateDependent",
                AffiliationDependent.class, Long.class, Long.class);
        method.setAccessible(true);
        Object result = method.invoke(sendEmailImpl, affiliation, idAffiliate, idBondingType);

        assertNotNull(result);
        com.gal.afiliaciones.domain.model.affiliate.FindAffiliateReqDTO dto = (com.gal.afiliaciones.domain.model.affiliate.FindAffiliateReqDTO) result;
        assertEquals(idAffiliate.intValue(), dto.getIdAffiliate());
        assertEquals("RC", dto.getDocumentType());
        assertEquals("999999", dto.getDocumentNumber());
        // Should not set affiliationType for unknown idBondingType
        assertNull(dto.getAffiliationType());
    }

    @Test
    void testConfirmationInterviewWebOfficial_sendsEmailWithCorrectData() throws Exception {
        LocalDateTime dateInterview = LocalDateTime.of(2024, 6, 10, 15, 30);
        String email = "official@email.com";
        String filedNumber = "F789";

        // Mock ARL info
        ArlInformation arlInfo = new ArlInformation();
        arlInfo.setId(2L);
        arlInfo.setName("ARL Oficial");
        arlInfo.setEmail("arl@oficial.com");
        arlInfo.setNit("654321");
        when(arlInformationDao.findAllArlInformation()).thenReturn(Collections.singletonList(arlInfo));
        when(properties.getLinkLogin()).thenReturn("http://login.url");

        sendEmailImpl.confirmationInterviewWebOfficial(dateInterview, email, filedNumber);

        ArgumentCaptor<EmailDataDTO> captor = ArgumentCaptor.forClass(EmailDataDTO.class);
        verify(emailService).sendSimpleMessage(captor.capture(), anyString());
        EmailDataDTO dto = captor.getValue();

        assertEquals(email, dto.getDestinatario());
        assertEquals(com.gal.afiliaciones.infrastructure.utils.Constant.REMINDER_INTERVIEW_WEB_MERCANTILE_OFFICIAL,
                dto.getPlantilla());
        Map<String, Object> data = dto.getDatos();
        assertEquals("ARL Oficial", data.get("nameArl"));
        assertEquals("http://login.url", data.get("link"));
        assertTrue(data.get("date").toString().contains("2024"));
        assertTrue(data.get(com.gal.afiliaciones.infrastructure.utils.Constant.EMAIL_SUBJECT_NAME).toString()
                .contains(filedNumber));
    }

    @Test
    void testConfirmationInterviewWebOfficial_handlesNullArlInformationGracefully() {
        LocalDateTime dateInterview = LocalDateTime.of(2024, 6, 10, 15, 30);
        String email = "official@email.com";
        String filedNumber = "F789";

        when(arlInformationDao.findAllArlInformation()).thenReturn(Collections.emptyList());
        when(properties.getLinkLogin()).thenReturn("http://login.url");

        // Should throw IndexOutOfBoundsException due to empty ARL list
        org.junit.jupiter.api.Assertions.assertThrows(IndexOutOfBoundsException.class, () -> {
            sendEmailImpl.confirmationInterviewWebOfficial(dateInterview, email, filedNumber);
        });
    }

    @Test
    void testDataWorkerRetirement_returnsExpectedMap() throws Exception {
        // Arrange
        Retirement retirement = mock(Retirement.class);
        when(retirement.getRetirementDate()).thenReturn(LocalDate.of(2024, 6, 15));
        when(retirement.getIdentificationDocumentType()).thenReturn("CC");
        when(retirement.getIdentificationDocumentNumber()).thenReturn("123456789");
        when(retirement.getFiledNumber()).thenReturn("F999");
        when(retirement.getCompleteName()).thenReturn("John Doe");

        // Use reflection to access private method
        java.lang.reflect.Method method = SendEmailImpl.class.getDeclaredMethod(
                "dataWorkerRetirement", Retirement.class, String.class);
        method.setAccessible(true);

        ArlInformation arlInfo = new ArlInformation();
        arlInfo.setName("ARL Test");
        arlInfo.setPhoneNumber("123-456-7890");
        when(arlInformationDao.findAllArlInformation()).thenReturn(Collections.singletonList(arlInfo));

        // Act
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) method.invoke(sendEmailImpl, retirement, "Employer Name");

        // Assert
        assertNotNull(result);
        assertEquals("Employer Name", result.get("user"));
        assertEquals("CC", result.get("identificationType"));
        assertEquals("123456789", result.get("identificationNumber"));
        assertEquals("F999", result.get("filedNumber"));
        assertEquals("John Doe", result.get("completeName"));
        assertEquals("ARL Test", result.get("nameARL"));
        assertEquals("123-456-7890", result.get("phoneARL"));
    }

    @Test
    void testDataWorkerRetirement_withNullFields() throws Exception {
        // Arrange
        Retirement retirement = mock(Retirement.class);
        when(retirement.getRetirementDate()).thenReturn(LocalDate.of(2024, 1, 1));
        when(retirement.getIdentificationDocumentType()).thenReturn(null);
        when(retirement.getIdentificationDocumentNumber()).thenReturn(null);
        when(retirement.getFiledNumber()).thenReturn(null);
        when(retirement.getCompleteName()).thenReturn(null);

        java.lang.reflect.Method method = SendEmailImpl.class.getDeclaredMethod(
                "dataWorkerRetirement", Retirement.class, String.class);
        method.setAccessible(true);

        ArlInformation arlInfo = new ArlInformation();
        arlInfo.setName("ARL Null");
        arlInfo.setPhoneNumber(null);
        when(arlInformationDao.findAllArlInformation()).thenReturn(Collections.singletonList(arlInfo));

        // Act
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) method.invoke(sendEmailImpl, retirement, null);

        // Assert
        assertNotNull(result);
        assertEquals("01/01/2024", result.get("date"));
        assertNull(result.get("user"));
        assertNull(result.get("identificationType"));
        assertNull(result.get("identificationNumber"));
        assertNull(result.get("filedNumber"));
        assertNull(result.get("completeName"));
        assertEquals("ARL Null", result.get("nameARL"));
        assertNull(result.get("phoneARL"));
    }

    @Test
    void testDataAffiliatedDependent_AllFieldsPresent() throws Exception {
        // Arrange
        AffiliationDependent affiliation = mock(AffiliationDependent.class);
        when(affiliation.getIdentificationDocumentType()).thenReturn("CC");
        when(affiliation.getIdentificationDocumentNumber()).thenReturn("123456");
        when(affiliation.getFiledNumber()).thenReturn("F789");
        when(affiliation.getFirstName()).thenReturn("Ana");
        when(affiliation.getSecondName()).thenReturn("Maria");
        when(affiliation.getSurname()).thenReturn("Perez");
        when(affiliation.getSecondSurname()).thenReturn("Gomez");

        // Use reflection to access private method
        java.lang.reflect.Method method = SendEmailImpl.class.getDeclaredMethod(
                "dataAffiliatedDependent", AffiliationDependent.class);
        method.setAccessible(true);

        // Act
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) method.invoke(sendEmailImpl, affiliation);

        // Assert
        assertEquals("Ana", data.get("firstName"));
        assertEquals("Maria", data.get("secondName"));
        assertEquals("Perez", data.get("surname"));
        assertEquals("Gomez", data.get("secondSurname"));
        assertTrue(data.containsKey("nameARL")); // Constant.NAME_ARL_LABEL
        assertNotNull(data.get("nameARL"));
    }

    @Test
    void testDataAffiliatedDependent_NullSecondNameAndSecondSurname() throws Exception {
        // Arrange
        AffiliationDependent affiliation = mock(AffiliationDependent.class);
        when(affiliation.getIdentificationDocumentType()).thenReturn("TI");
        when(affiliation.getIdentificationDocumentNumber()).thenReturn("654321");
        when(affiliation.getFiledNumber()).thenReturn("F321");
        when(affiliation.getFirstName()).thenReturn("Luis");
        when(affiliation.getSecondName()).thenReturn(null);
        when(affiliation.getSurname()).thenReturn("Rodriguez");
        when(affiliation.getSecondSurname()).thenReturn(null);

        // Use reflection to access private method
        java.lang.reflect.Method method = SendEmailImpl.class.getDeclaredMethod(
                "dataAffiliatedDependent", AffiliationDependent.class);
        method.setAccessible(true);

        // Act
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) method.invoke(sendEmailImpl, affiliation);

        // Assert
        assertEquals("Luis", data.get("firstName"));
        assertEquals("", data.get("secondName"));
        assertEquals("Rodriguez", data.get("surname"));
        assertEquals("", data.get("secondSurname"));
        assertTrue(data.containsKey("nameARL")); // Constant.NAME_ARL_LABEL
    }

    @Test
    void testDataUpdateAffiliatedDependent_withAllNamesPresent() {
        AffiliationDependent affiliation = mock(AffiliationDependent.class);
        DataEmployerDTO dataEmployerDTO = mock(DataEmployerDTO.class);

        when(affiliation.getFirstName()).thenReturn("Ana");
        when(affiliation.getSecondName()).thenReturn("Maria");
        when(affiliation.getSurname()).thenReturn("Perez");
        when(affiliation.getSecondSurname()).thenReturn("Lopez");
        when(affiliation.getIdentificationDocumentType()).thenReturn("CC");
        when(affiliation.getIdentificationDocumentNumber()).thenReturn("123456");
        when(affiliation.getFiledNumber()).thenReturn("F789");
        when(dataEmployerDTO.getCompleteNameOrCompanyName()).thenReturn("Empresa S.A.");

        // AffiliateRepository mock for findOne
        Affiliate affiliate = mock(Affiliate.class);
        when(affiliate.getAffiliationSubType()).thenReturn("Subtipo");
        when(affiliate.getAffiliationStatus()).thenReturn("Activo");
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        Map<String, Object> result = invokeDataUpdateAffiliatedDependent(affiliation, dataEmployerDTO);

        assertEquals("Empresa S.A.", result.get("user"));
        assertTrue(result.get("completeName").toString().contains("Ana Maria Perez Lopez"));
        assertEquals("CC", result.get("identificationType"));
        assertEquals("123456", result.get("identificationNumber"));
        assertEquals("Subtipo", result.get("vinculationType"));
        assertNotNull(result.get("date"));
        assertNotNull(result.get("emailARL"));
        assertNotNull(result.get("nameARL"));
    }

    @Test
    void testDataUpdateAffiliatedDependent_withEmptySecondNames() {
        AffiliationDependent affiliation = mock(AffiliationDependent.class);
        DataEmployerDTO dataEmployerDTO = mock(DataEmployerDTO.class);

        when(affiliation.getFirstName()).thenReturn("Luis");
        when(affiliation.getSecondName()).thenReturn("");
        when(affiliation.getSurname()).thenReturn("Gomez");
        when(affiliation.getSecondSurname()).thenReturn("");
        when(affiliation.getIdentificationDocumentType()).thenReturn("TI");
        when(affiliation.getIdentificationDocumentNumber()).thenReturn("654321");
        when(affiliation.getFiledNumber()).thenReturn("F321");
        when(dataEmployerDTO.getCompleteNameOrCompanyName()).thenReturn("Compañía XYZ");

        Affiliate affiliate = mock(Affiliate.class);
        when(affiliate.getAffiliationSubType()).thenReturn("OtroSubtipo");
        when(affiliate.getAffiliationStatus()).thenReturn("Inactivo");
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        Map<String, Object> result = invokeDataUpdateAffiliatedDependent(affiliation, dataEmployerDTO);

        assertEquals("Compañía XYZ", result.get("user"));
        assertEquals("Luis Gomez", result.get("completeName"));
        assertEquals("TI", result.get("identificationType"));
        assertEquals("654321", result.get("identificationNumber"));
        assertEquals("OtroSubtipo", result.get("vinculationType"));
        assertNotNull(result.get("date"));
        assertNotNull(result.get("emailARL"));
        assertNotNull(result.get("nameARL"));
    }

    @Test
    void testDataUpdateAffiliatedDependent_affiliateNotFoundThrowsException() {
        AffiliationDependent affiliation = mock(AffiliationDependent.class);
        DataEmployerDTO dataEmployerDTO = mock(DataEmployerDTO.class);

        when(affiliation.getFirstName()).thenReturn("Carlos");
        when(affiliation.getSecondName()).thenReturn("");
        when(affiliation.getSurname()).thenReturn("Ramirez");
        when(affiliation.getSecondSurname()).thenReturn("");
        when(affiliation.getIdentificationDocumentType()).thenReturn("CC");
        when(affiliation.getIdentificationDocumentNumber()).thenReturn("111222");
        when(affiliation.getFiledNumber()).thenReturn("F000");
        when(dataEmployerDTO.getCompleteNameOrCompanyName()).thenReturn("Empresa ABC");

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThrows(com.gal.afiliaciones.config.ex.affiliation.AffiliationError.class, () -> {
            invokeDataUpdateAffiliatedDependent(affiliation, dataEmployerDTO);
        });
    }
    @Test
    void testTransformToIndependentFormData_Volunteer() {
        Affiliation affiliation = mock(Affiliation.class);
        when(affiliation.getFiledNumber()).thenReturn("F001");
        when(affiliation.getIdentificationDocumentType()).thenReturn("CC");
        when(affiliation.getIdentificationDocumentNumber()).thenReturn("123456");
        when(affiliation.getFirstName()).thenReturn("Ana");
        when(affiliation.getSecondName()).thenReturn("Maria");
        when(affiliation.getSurname()).thenReturn("Perez");
        when(affiliation.getSecondSurname()).thenReturn("Lopez");
        when(affiliation.getDateOfBirth()).thenReturn(LocalDate.of(1990, 1, 1));
        when(affiliation.getGender()).thenReturn("F");
        when(affiliation.getNationality()).thenReturn(1L);
        when(affiliation.getHealthPromotingEntity()).thenReturn(1L);
        when(affiliation.getPensionFundAdministrator()).thenReturn(2L);
        when(affiliation.getAddress()).thenReturn("Calle 1");
        when(affiliation.getDepartment()).thenReturn(10L);
        when(affiliation.getCityMunicipality()).thenReturn(20L);
        when(affiliation.getPhone1()).thenReturn("3001234567");
        when(affiliation.getEmail()).thenReturn("ana@correo.com");
        when(affiliation.getRisk()).thenReturn("1");
        when(affiliation.getPrice()).thenReturn(new java.math.BigDecimal("123.45"));
        when(affiliation.getContractDuration()).thenReturn("12.00");
        when(affiliation.getContractEndDate()).thenReturn(LocalDate.of(2024, 12, 31));
        when(affiliation.getContractIbcValue()).thenReturn(new java.math.BigDecimal("500"));
        when(affiliation.getOccupation()).thenReturn("Voluntario");
        when(affiliation.getAddressEmployer()).thenReturn("DirEmp");
        when(affiliation.getDepartmentEmployer()).thenReturn(11L);
        when(affiliation.getMunicipalityEmployer()).thenReturn(22L);
        when(affiliation.getSecondaryPhone1()).thenReturn("3012345678");
        when(affiliation.getId()).thenReturn(99L);

        // Mocks para dependencias
        Gender gender = new Gender();
        gender.setDescription("Femenino");
        when(genderRepository.findByGenderType("F")).thenReturn(Optional.of(gender));
        Health health = new Health();
        health.setNameEPS("EPS Salud");
        when(healthRepository.findById(1L)).thenReturn(Optional.of(health));
        FundPension fund = new FundPension();
        fund.setNameAfp("AFP Pension");
        when(pensionRepository.findById(2L)).thenReturn(Optional.of(fund));
        Department dep = new Department();
        dep.setDepartmentName("Antioquia");
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(dep));
        when(departmentRepository.findById(11L)).thenReturn(Optional.of(dep));
        Municipality mun = new Municipality();
        mun.setMunicipalityName("Medellin");
        when(municipalityRepository.findById(20L)).thenReturn(Optional.of(mun));
        when(municipalityRepository.findById(22L)).thenReturn(Optional.of(mun));
        Danger danger = new Danger();
        when(dangerRepository.findByIdAffiliation(99L)).thenReturn(danger);
        // AlfrescoService
        when(webClient.folderExistsByName(anyString(), anyString())).thenReturn(Optional.empty());
        // WebClient
        when(webClient.getFileBase64(anyString())).thenReturn(reactor.core.publisher.Mono.just("firmaBase64"));
        // OccupationDecree1563DTO
        List<com.gal.afiliaciones.infrastructure.dto.economicactivity.OccupationDecree1563DTO> occList = new ArrayList<>();
        com.gal.afiliaciones.infrastructure.dto.economicactivity.OccupationDecree1563DTO occ = new com.gal.afiliaciones.infrastructure.dto.economicactivity.OccupationDecree1563DTO();
        occ.setOccupation("Voluntario");
        occ.setCode(123L);
        occList.add(occ);
        BodyResponseConfig<List<com.gal.afiliaciones.infrastructure.dto.economicactivity.OccupationDecree1563DTO>> body = new BodyResponseConfig<>();
        body.setData(occList);
        when(webClient.getOccupationsByVolunteer()).thenReturn(body);

        // filedService
        when(filedService.getNextFiledNumberForm()).thenReturn("CONSEC001");
        // applicationFormDao
        //doNothing().when(applicationFormDao).saveFormRegistry(any());

        IndividualIndependentAffiliationDTO dto = (IndividualIndependentAffiliationDTO)
                org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                        sendEmailImpl, "transformToIndependentFormData", affiliation, "SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER");

        assertNotNull(dto);
        assertEquals("F001", dto.getFiledNumber());
        assertEquals("Ana Maria Perez Lopez", dto.getFullNameOrBusinessNameGI());
        assertEquals("Femenino", dto.getGenderGI());
        assertEquals("EPS Salud", dto.getCurrentHealthInsuranceGI());
        assertEquals("AFP Pension", dto.getCurrentPensionFundGI());
        assertEquals("Calle 1", dto.getAddressGI());
        assertEquals("Antioquia", dto.getDepartmentGI());
        assertEquals("Medellin", dto.getCityOrDistrictGI());
        assertEquals("3001234567", dto.getMobileOrLandlineGI());
        assertEquals("ana@correo.com", dto.getEmailGI());
        assertEquals("1", dto.getRiskClassARL());
        assertEquals("123.45", dto.getFeeARL());
    }

    @Test
    void testTransformToIndependentFormData_TaxiDriver() {
        Affiliation affiliation = mock(Affiliation.class);
        when(affiliation.getFiledNumber()).thenReturn("F002");
        when(affiliation.getIdentificationDocumentType()).thenReturn("CC");
        when(affiliation.getIdentificationDocumentNumber()).thenReturn("654321");
        when(affiliation.getFirstName()).thenReturn("Luis");
        when(affiliation.getSecondName()).thenReturn("Carlos");
        when(affiliation.getSurname()).thenReturn("Gomez");
        when(affiliation.getSecondSurname()).thenReturn("Ruiz");
        when(affiliation.getDateOfBirth()).thenReturn(LocalDate.of(1985, 5, 5));
        when(affiliation.getGender()).thenReturn("M");
        when(affiliation.getNationality()).thenReturn(1L);
        when(affiliation.getHealthPromotingEntity()).thenReturn(3L);
        when(affiliation.getPensionFundAdministrator()).thenReturn(4L);
        when(affiliation.getAddress()).thenReturn("Calle 2");
        when(affiliation.getDepartment()).thenReturn(12L);
        when(affiliation.getCityMunicipality()).thenReturn(24L);
        when(affiliation.getPhone1()).thenReturn("3007654321");
        when(affiliation.getEmail()).thenReturn("luis@correo.com");
        when(affiliation.getRisk()).thenReturn("2");
        when(affiliation.getPrice()).thenReturn(new java.math.BigDecimal("321.00"));
        when(affiliation.getContractDuration()).thenReturn("24.00");
        when(affiliation.getContractStartDate()).thenReturn(LocalDate.of(2024, 1, 1));
        when(affiliation.getContractEndDate()).thenReturn(LocalDate.of(2024, 12, 31));
        when(affiliation.getContractTotalValue()).thenReturn(new java.math.BigDecimal("10000"));
        when(affiliation.getContractMonthlyValue()).thenReturn(new java.math.BigDecimal("1000"));
        when(affiliation.getContractIbcValue()).thenReturn(new java.math.BigDecimal("800"));
        when(affiliation.getOccupation()).thenReturn("Conductor");
        when(affiliation.getAddressWorkDataCenter()).thenReturn("DirTaxi");
        when(affiliation.getIdDepartmentWorkDataCenter()).thenReturn(13L);
        when(affiliation.getIdCityWorkDataCenter()).thenReturn(26L);
        when(affiliation.getCompanyName()).thenReturn("Taxi S.A.");
        when(affiliation.getIdentificationDocumentTypeContractor()).thenReturn("NIT");
        when(affiliation.getIdentificationDocumentNumberContractor()).thenReturn("900000001");
        when(affiliation.getDv()).thenReturn(9);
        when(affiliation.getEmailContractor()).thenReturn("taxi@empresa.com");
        when(affiliation.getPhone1WorkDataCenter()).thenReturn("3123456789");

        Gender gender = new Gender();
        gender.setDescription("Masculino");
        when(genderRepository.findByGenderType("M")).thenReturn(Optional.of(gender));
        Health health = new Health();
        health.setNameEPS("EPS Taxi");
        when(healthRepository.findById(3L)).thenReturn(Optional.of(health));
        FundPension fund = new FundPension();
        fund.setNameAfp("AFP Taxi");
        when(pensionRepository.findById(4L)).thenReturn(Optional.of(fund));
        Department dep = new Department();
        dep.setDepartmentName("Cundinamarca");
        when(departmentRepository.findById(12L)).thenReturn(Optional.of(dep));
        Department depTaxi = new Department();
        depTaxi.setDepartmentName("Bogota");
        when(departmentRepository.findById(13L)).thenReturn(Optional.of(depTaxi));
        Municipality mun = new Municipality();
        mun.setMunicipalityName("Soacha");
        when(municipalityRepository.findById(24L)).thenReturn(Optional.of(mun));
        Municipality munTaxi = new Municipality();
        munTaxi.setMunicipalityName("Bogota");
        when(municipalityRepository.findById(26L)).thenReturn(Optional.of(munTaxi));
        Danger danger = new Danger();
        when(dangerRepository.findByIdAffiliation(anyLong())).thenReturn(danger);
        when(webClient.folderExistsByName(anyString(), anyString())).thenReturn(Optional.empty());
        when(webClient.getFileBase64(anyString())).thenReturn(reactor.core.publisher.Mono.just("firmaTaxi"));
        when(filedService.getNextFiledNumberForm()).thenReturn("CONSEC002");
        //doNothing().when(applicationFormDao).saveFormRegistry(any());

        IndividualIndependentAffiliationDTO dto = (IndividualIndependentAffiliationDTO)
                org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                        sendEmailImpl, "transformToIndependentFormData", affiliation, "AFILIATION_SUBTYPE_TAXI_DRIVER");

        assertNotNull(dto);
        assertEquals("F002", dto.getFiledNumber());
        assertEquals("Luis Carlos Gomez Ruiz", dto.getFullNameOrBusinessNameGI());
        assertEquals("Masculino", dto.getGenderGI());
        assertEquals("EPS Taxi", dto.getCurrentHealthInsuranceGI());
        assertEquals("AFP Taxi", dto.getCurrentPensionFundGI());
        assertEquals("Calle 2", dto.getAddressGI());
        assertEquals("Cundinamarca", dto.getDepartmentGI());
        assertEquals("Soacha", dto.getCityOrDistrictGI());
        assertEquals("3007654321", dto.getMobileOrLandlineGI());
        assertEquals("luis@correo.com", dto.getEmailGI());
        assertEquals("2", dto.getRiskClassARL());
        assertEquals("321.00", dto.getFeeARL());
    }

    @Test
    void testTransformToIndependentFormData_ProvisionServices() {
        Affiliation affiliation = mock(Affiliation.class);
        when(affiliation.getFiledNumber()).thenReturn("F003");
        when(affiliation.getIdentificationDocumentType()).thenReturn("CC");
        when(affiliation.getIdentificationDocumentNumber()).thenReturn("111222");
        when(affiliation.getFirstName()).thenReturn("Pedro");
        when(affiliation.getSecondName()).thenReturn("Jose");
        when(affiliation.getSurname()).thenReturn("Martinez");
        when(affiliation.getSecondSurname()).thenReturn("Gonzalez");
        when(affiliation.getDateOfBirth()).thenReturn(LocalDate.of(1995, 3, 3));
        when(affiliation.getGender()).thenReturn("M");
        when(affiliation.getNationality()).thenReturn(1L);
        when(affiliation.getHealthPromotingEntity()).thenReturn(5L);
        when(affiliation.getPensionFundAdministrator()).thenReturn(6L);
        when(affiliation.getAddress()).thenReturn("Calle 3");
        when(affiliation.getDepartment()).thenReturn(14L);
        when(affiliation.getCityMunicipality()).thenReturn(28L);
        when(affiliation.getPhone1()).thenReturn("3012345678");
        when(affiliation.getEmail()).thenReturn("pedro@correo.com");
        when(affiliation.getRisk()).thenReturn("3");
        when(affiliation.getPrice()).thenReturn(new java.math.BigDecimal("456.78"));
        when(affiliation.getContractDuration()).thenReturn("6.00");
        when(affiliation.getAddressIndependentWorker()).thenReturn("DirInd");
        when(affiliation.getIdDepartmentIndependentWorker()).thenReturn(15L);
        when(affiliation.getIdCityIndependentWorker()).thenReturn(30L);
        when(affiliation.getContractType()).thenReturn("Prestacion");
        when(affiliation.getContractQuality()).thenReturn("Calidad");
        when(affiliation.getTransportSupply()).thenReturn(true);
        when(affiliation.getStartDate()).thenReturn(LocalDate.of(2024, 2, 1));
        when(affiliation.getEndDate()).thenReturn(LocalDate.of(2024, 8, 1));
        when(affiliation.getDuration()).thenReturn("6.00");
        when(affiliation.getJourneyEstablished()).thenReturn("Jornada");
        when(affiliation.getContractTotalValue()).thenReturn(new java.math.BigDecimal("6000"));
        when(affiliation.getContractMonthlyValue()).thenReturn(new java.math.BigDecimal("1000"));
        when(affiliation.getContractIbcValue()).thenReturn(new java.math.BigDecimal("800"));
        when(affiliation.getCodeMainEconomicActivity()).thenReturn("3ABCD12");
        when(affiliation.getOccupation()).thenReturn("Consultor");
        when(affiliation.getAddressContractDataStep2()).thenReturn("DirContrato");
        when(affiliation.getIdDepartmentWorkDataCenter()).thenReturn(16L);
        when(affiliation.getIdCityWorkDataCenter()).thenReturn(32L);
        when(affiliation.getCompanyName()).thenReturn("Empresa S.A.");
        when(affiliation.getIdentificationDocumentTypeContractor()).thenReturn("NIT");
        when(affiliation.getIdentificationDocumentNumberContractor()).thenReturn("900000002");
        when(affiliation.getDv()).thenReturn(8);
        when(affiliation.getEmailContractor()).thenReturn("empresa@correo.com");
        when(affiliation.getPhone1WorkDataCenter()).thenReturn("3131313131");
        when(affiliation.getFirstNameSignatory()).thenReturn("Firma");
        when(affiliation.getSecondNameSignatory()).thenReturn("Sign");
        when(affiliation.getSurnameSignatory()).thenReturn("Ature");
        when(affiliation.getSecondSurnameSignatory()).thenReturn("Test");
        when(affiliation.getIdentificationDocumentTypeSignatory()).thenReturn("CC");
        when(affiliation.getIdentificationDocumentNumberSignatory()).thenReturn("123123");

        Gender gender = new Gender();
        gender.setDescription("Masculino");
        when(genderRepository.findByGenderType("M")).thenReturn(Optional.of(gender));
        Health health = new Health();
        health.setNameEPS("EPS Consultor");
        when(healthRepository.findById(5L)).thenReturn(Optional.of(health));
        FundPension fund = new FundPension();
        fund.setNameAfp("AFP Consultor");
        when(pensionRepository.findById(6L)).thenReturn(Optional.of(fund));
        Department depInd = new Department();
        depInd.setDepartmentName("Santander");
        when(departmentRepository.findById(14L)).thenReturn(Optional.of(depInd));
        Department depIndWorker = new Department();
        depIndWorker.setDepartmentName("Boyaca");
        when(departmentRepository.findById(15L)).thenReturn(Optional.of(depIndWorker));
        Department depWork = new Department();
        depWork.setDepartmentName("Cesar");
        when(departmentRepository.findById(16L)).thenReturn(Optional.of(depWork));
        Municipality mun = new Municipality();
        mun.setMunicipalityName("Bucaramanga");
        when(municipalityRepository.findById(28L)).thenReturn(Optional.of(mun));
        Municipality munInd = new Municipality();
        munInd.setMunicipalityName("Tunja");
        when(municipalityRepository.findById(30L)).thenReturn(Optional.of(munInd));
        Municipality munWork = new Municipality();
        munWork.setMunicipalityName("Valledupar");
        when(municipalityRepository.findById(32L)).thenReturn(Optional.of(munWork));
        Danger danger = new Danger();
        when(dangerRepository.findByIdAffiliation(anyLong())).thenReturn(danger);
        when(webClient.folderExistsByName(anyString(), anyString())).thenReturn(Optional.empty());
        when(webClient.getFileBase64(anyString())).thenReturn(reactor.core.publisher.Mono.just("firmaConsultor"));
        when(filedService.getNextFiledNumberForm()).thenReturn("CONSEC003");
        //doNothing().when(applicationFormDao).saveFormRegistry(any());
        EconomicActivity activity = new EconomicActivity();
        activity.setDescription("Consultoria");
        activity.setClassRisk("3");
        activity.setCodeCIIU("ABCD");
        activity.setAdditionalCode("12");
        when(economicActivityService.getEconomicActivityByRiskCodeCIIUCodeAdditional("3", "ABCD", "12")).thenReturn(activity);

        IndividualIndependentAffiliationDTO dto = (IndividualIndependentAffiliationDTO)
                org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                        sendEmailImpl, "transformToIndependentFormData", affiliation, "SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES");

        assertNotNull(dto);
        assertEquals("F003", dto.getFiledNumber());
        assertEquals("Pedro Jose Martinez Gonzalez", dto.getFullNameOrBusinessNameGI());
        assertEquals("Masculino", dto.getGenderGI());
        assertEquals("EPS Consultor", dto.getCurrentHealthInsuranceGI());
        assertEquals("AFP Consultor", dto.getCurrentPensionFundGI());
        assertEquals("Calle 3", dto.getAddressGI());
        assertEquals("Santander", dto.getDepartmentGI());
        assertEquals("Bucaramanga", dto.getCityOrDistrictGI());
        assertEquals("3012345678", dto.getMobileOrLandlineGI());
        assertEquals("pedro@correo.com", dto.getEmailGI());
        assertEquals("3", dto.getRiskClassARL());
        assertEquals("456.78", dto.getFeeARL());
    }
    // Helper to invoke private method dataUpdateAffiliatedDependent
    @SuppressWarnings("unchecked")
    private Map<String, Object> invokeDataUpdateAffiliatedDependent(AffiliationDependent affiliation,
            DataEmployerDTO dataEmployerDTO) {
        try {
            java.lang.reflect.Method method = SendEmailImpl.class.getDeclaredMethod(
                    "dataUpdateAffiliatedDependent",
                    AffiliationDependent.class,
                    DataEmployerDTO.class);
            method.setAccessible(true);
            return (Map<String, Object>) method.invoke(sendEmailImpl, affiliation, dataEmployerDTO);
        } catch (Exception e) {
            if (e.getCause() instanceof RuntimeException)
                throw (RuntimeException) e.getCause();
            throw new RuntimeException(e);
        }
    }

}
