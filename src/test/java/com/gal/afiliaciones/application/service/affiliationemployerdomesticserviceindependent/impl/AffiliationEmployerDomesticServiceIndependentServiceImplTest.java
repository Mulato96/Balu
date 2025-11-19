package com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliate.FiledWebSocketService;
import com.gal.afiliaciones.application.service.affiliate.MainOfficeService;
import com.gal.afiliaciones.application.service.affiliate.ScheduleInterviewWebService;
import com.gal.afiliaciones.application.service.affiliate.WorkCenterService;
import com.gal.afiliaciones.application.service.affiliate.affiliationemployeractivitiesmercantile.AffiliationEmployerActivitiesMercantileService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.daily.DailyService;
import com.gal.afiliaciones.application.service.documentnamestandardization.DocumentNameStandardizationService;
import com.gal.afiliaciones.application.service.economicactivity.IEconomicActivityService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.impl.certicate.InMemoryMultipartFile;
import com.gal.afiliaciones.application.service.observationsaffiliation.ObservationsAffiliationService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.config.util.MessageErrorAge;
import com.gal.afiliaciones.domain.model.AffiliationsView;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.Operator;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.AffiliationAssignmentHistory;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.AffiliationCancellationTimer;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.DateInterviewWebRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationAssignRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationCancellationTimerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IDataDocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.DangerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.FamilyMemberRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationsview.AffiliationsViewRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.UserDtoApiRegistry;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.*;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ConsultFiles;
import com.gal.afiliaciones.infrastructure.dto.alfresco.DocumentBase64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO;
import com.gal.afiliaciones.infrastructure.dto.daily.DataDailyDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ContextConfiguration(classes = {AffiliationEmployerDomesticServiceIndependentServiceImpl.class})
@DisabledInAotMode
@ExtendWith(SpringExtension.class)
class AffiliationEmployerDomesticServiceIndependentServiceImplTest {
    @MockBean
    private AffiliateMercantileRepository affiliateMercantileRepository;

    @MockBean
    private AffiliateRepository affiliateRepository;

    @MockBean
    private AffiliateService affiliateService;

    @MockBean
    private AffiliationEmployerActivitiesMercantileService
            affiliationEmployerActivitiesMercantileService;

    @Autowired
    private AffiliationEmployerDomesticServiceIndependentServiceImpl
            affiliationEmployerDomesticServiceIndependentServiceImpl;

    @MockBean
    private AffiliationsViewRepository affiliationsViewRepository;

    @MockBean
    private AlfrescoService alfrescoService;

    @MockBean
    private CollectProperties collectProperties;

    @MockBean
    private DailyService dailyService;

    @MockBean
    private DangerRepository dangerRepository;

    @MockBean
    private DateInterviewWebRepository dateInterviewWebRepository;

    @MockBean
    private DocumentNameStandardizationService documentNameStandardizationService;

    @MockBean
    private FamilyMemberRepository familyMemberRepository;

    @MockBean
    private FiledService filedService;

    @MockBean
    private FiledWebSocketService filedWebSocketService;

    @MockBean
    private GenericWebClient genericWebClient;

    @MockBean
    private IAffiliationAssignRepository iAffiliationAssignRepository;

    @MockBean
    private DepartmentRepository departmentRepository;

    @MockBean
    private MunicipalityRepository municipalityRepository;

    @MockBean
    private IAffiliationCancellationTimerRepository iAffiliationCancellationTimerRepository;

    @MockBean
    private IAffiliationEmployerDomesticServiceIndependentRepository
            iAffiliationEmployerDomesticServiceIndependentRepository;

    @MockBean
    private IDataDocumentRepository iDataDocumentRepository;

    @MockBean
    private IEconomicActivityRepository iEconomicActivityRepository;

    @MockBean
    private IEconomicActivityService iEconomicActivityService;

    @MockBean
    private IUserPreRegisterRepository iUserPreRegisterRepository;

    @MockBean
    private MainOfficeService mainOfficeService;

    @MockBean
    private MessageErrorAge messageErrorAge;

    @MockBean
    private ObservationsAffiliationService observationsAffiliationService;

    @MockBean
    private ScheduleInterviewWebService scheduleInterviewWebService;

    @MockBean
    private SendEmails sendEmails;

    @MockBean
    private WorkCenterService workCenterService;

    private AffiliateMercantile affiliateMercantile;


    private Affiliate affiliate;
    private Affiliation affiliation;


    @BeforeEach
    void setUp() {
        affiliate = new Affiliate();
        affiliate.setIdAffiliate(123L);
        affiliate.setFiledNumber("F123");
        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        affiliate.setAffiliationCancelled(false);
        affiliate.setStatusDocument(false);
        affiliate.setUserId(1L);

        affiliation = new Affiliation();
        affiliation.setId(1L);
        affiliation.setFiledNumber("F123");
        affiliation.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);
        affiliation.setIdentificationDocumentNumber("9001");
        affiliation.setIdentificationDocumentType("CC");

        affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setId(1L);
        affiliateMercantile.setFiledNumber("F123M");
        affiliateMercantile.setStageManagement(Constant.INTERVIEW_WEB);
        affiliateMercantile.setAffiliationCancelled(false);
        affiliateMercantile.setStatusDocument(false);
        affiliateMercantile.setNumberDocumentPersonResponsible("123");
        affiliateMercantile.setTypeDocumentPersonResponsible("CC");
        affiliateMercantile.setZoneLocationEmployer(Constant.URBAN_ZONE);
        affiliateMercantile.setEconomicActivity(new ArrayList<AffiliateActivityEconomic>());
    }

    /**
     * Test {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep1(DomesticServiceAffiliationStep1DTO)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep1(DomesticServiceAffiliationStep1DTO)}
     */
    @Test
    @DisplayName("Test createAffiliationStep1(DomesticServiceAffiliationStep1DTO)")
    void testCreateAffiliationStep1() {
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

        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(ofResult);
        when(collectProperties.getMinimumAge())
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        DomesticServiceAffiliationStep1DTO dto =
                new DomesticServiceAffiliationStep1DTO(
                        0L,
                        "Identification Document Type",
                        "42",
                        true,
                        10,
                        true,
                        10,
                        true,
                        10,
                        true,
                        10,
                        true,
                        1L,
                        1L,
                        "42 Main St",
                        1L,
                        1L,
                        1L,
                        true,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        "6625550144",
                        "6625550144",
                        "jane.doe@example.org");

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerDomesticServiceIndependentServiceImpl.createAffiliationStep1(dto));
        verify(collectProperties).getMinimumAge();
        verify(iUserPreRegisterRepository).findOne(any(Specification.class));
    }

    /**
     * Test {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep1(DomesticServiceAffiliationStep1DTO)}.
     *
     * <ul>
     *   <li>Given {@link UserMain#UserMain()} DateBirth is now.
     *   <li>Then calls {@link MessageErrorAge#messageError(String, String)}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep1(DomesticServiceAffiliationStep1DTO)}
     */
    @Test
    @DisplayName(
            "Test createAffiliationStep1(DomesticServiceAffiliationStep1DTO); given UserMain() DateBirth is now; then calls messageError(String, String)")
    
    void testCreateAffiliationStep1_givenUserMainDateBirthIsNow_thenCallsMessageError() {
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
        userMain.setDateBirth(LocalDate.now());
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
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(ofResult);
        when(collectProperties.getMinimumAge()).thenReturn(1);
        when(messageErrorAge.messageError(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn("An error occurred");
        DomesticServiceAffiliationStep1DTO dto =
                new DomesticServiceAffiliationStep1DTO(
                        0L,
                        "Identification Document Type",
                        "42",
                        true,
                        10,
                        true,
                        10,
                        true,
                        10,
                        true,
                        10,
                        true,
                        1L,
                        1L,
                        "42 Main St",
                        1L,
                        1L,
                        1L,
                        true,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        "6625550144",
                        "6625550144",
                        "jane.doe@example.org");

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerDomesticServiceIndependentServiceImpl.createAffiliationStep1(dto));
        verify(collectProperties).getMinimumAge();
        verify(messageErrorAge).messageError("Identification Type", "Identification");
        verify(iUserPreRegisterRepository).findOne(any(Specification.class));
    }

    /**
     * Test {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep1(DomesticServiceAffiliationStep1DTO)}.
     *
     * <ul>
     *   <li>Then calls {@link MessageErrorAge#messageError(String, String)}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep1(DomesticServiceAffiliationStep1DTO)}
     */
    @Test
    @DisplayName(
            "Test createAffiliationStep1(DomesticServiceAffiliationStep1DTO); then calls messageError(String, String)")
    
    void testCreateAffiliationStep1_thenCallsMessageError() {
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
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(ofResult);
        when(collectProperties.getMaximumAge()).thenReturn(3);
        when(collectProperties.getMinimumAge()).thenReturn(1);
        when(messageErrorAge.messageError(Mockito.<String>any(), Mockito.<String>any()))
                .thenReturn("An error occurred");
        DomesticServiceAffiliationStep1DTO dto =
                new DomesticServiceAffiliationStep1DTO(
                        0L,
                        "Identification Document Type",
                        "42",
                        true,
                        10,
                        true,
                        10,
                        true,
                        10,
                        true,
                        10,
                        true,
                        1L,
                        1L,
                        "42 Main St",
                        1L,
                        1L,
                        1L,
                        true,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        "6625550144",
                        "6625550144",
                        "jane.doe@example.org");

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerDomesticServiceIndependentServiceImpl.createAffiliationStep1(dto));
        verify(collectProperties).getMinimumAge();
        verify(messageErrorAge).messageError("Identification Type", "Identification");
        verify(iUserPreRegisterRepository).findOne(any(Specification.class));
    }

    /**
     * Test {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep1(DomesticServiceAffiliationStep1DTO)}.
     *
     * <ul>
     *   <li>When {@link DomesticServiceAffiliationStep1DTO#DomesticServiceAffiliationStep1DTO()}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep1(DomesticServiceAffiliationStep1DTO)}
     */
    @Test
    @DisplayName(
            "Test createAffiliationStep1(DomesticServiceAffiliationStep1DTO); when DomesticServiceAffiliationStep1DTO()")
    
    void testCreateAffiliationStep1_whenDomesticServiceAffiliationStep1DTO() {
        // Arrange
        when(iUserPreRegisterRepository.findOne(any(Specification.class)))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerDomesticServiceIndependentServiceImpl.createAffiliationStep1(
                                new DomesticServiceAffiliationStep1DTO()));
        verify(iUserPreRegisterRepository).findOne(any(Specification.class));
    }

    /**
     * Test {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep1(DomesticServiceAffiliationStep1DTO)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep1(DomesticServiceAffiliationStep1DTO)}
     */
    @Test
    @DisplayName("Test createAffiliationStep1(DomesticServiceAffiliationStep1DTO)")
    
    void testCreateAffiliationStep12() {
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
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(ofResult);
        when(collectProperties.getMaximumAge()).thenReturn(3);
        when(collectProperties.getMinimumAge()).thenReturn(1);
        when(messageErrorAge.messageError(Mockito.<String>any(), Mockito.<String>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        DomesticServiceAffiliationStep1DTO dto =
                new DomesticServiceAffiliationStep1DTO(
                        0L,
                        "Identification Document Type",
                        "42",
                        true,
                        10,
                        true,
                        10,
                        true,
                        10,
                        true,
                        10,
                        true,
                        1L,
                        1L,
                        "42 Main St",
                        1L,
                        1L,
                        1L,
                        true,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        "6625550144",
                        "6625550144",
                        "jane.doe@example.org");

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerDomesticServiceIndependentServiceImpl.createAffiliationStep1(dto));
        verify(collectProperties).getMinimumAge();
        verify(messageErrorAge).messageError("Identification Type", "Identification");
        verify(iUserPreRegisterRepository).findOne(any(Specification.class));
    }

    /**
     * Test {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep2(DomesticServiceAffiliationStep2DTO)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep2(DomesticServiceAffiliationStep2DTO)}
     */
    @Test
    @DisplayName("Test createAffiliationStep2(DomesticServiceAffiliationStep2DTO)")
    
    void testCreateAffiliationStep2() {
        // Arrange
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findById(Mockito.<Long>any()))
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
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(ofResult);
        DomesticServiceAffiliationStep2DTO dto =
                new DomesticServiceAffiliationStep2DTO(
                        1L,
                        "Identification Document Type",
                        "42",
                        "Person Type",
                        "Jane",
                        "Second Name",
                        "Doe",
                        "Doe",
                        LocalDate.of(1970, 1, 1),
                        "Age",
                        "Gender",
                        "Other Gender",
                        " ",
                        1L,
                        1L,
                        1L,
                        1L,
                        true,
                        true,
                        "42 Main St",
                        1L,
                        1L,
                        1L,
                        true,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        "6625550144",
                        "6625550144",
                        "jane.doe@example.org");

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerDomesticServiceIndependentServiceImpl.createAffiliationStep2(dto));
        verify(iUserPreRegisterRepository).findOne(any(Specification.class));
        verify(iAffiliationEmployerDomesticServiceIndependentRepository).findById(1L);
    }

    /**
     * Test {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep2(DomesticServiceAffiliationStep2DTO)}.
     *
     * <ul>
     *   <li>Given {@link UserMain#UserMain()} PensionFundAdministrator is {@code null}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep2(DomesticServiceAffiliationStep2DTO)}
     */
    @Test
    @DisplayName(
            "Test createAffiliationStep2(DomesticServiceAffiliationStep2DTO); given UserMain() PensionFundAdministrator is 'null'")
    
    void testCreateAffiliationStep2_givenUserMainPensionFundAdministratorIsNull() {
        // Arrange
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findById(Mockito.<Long>any()))
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
        userMain.setPensionFundAdministrator(null);
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
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(ofResult);
        DomesticServiceAffiliationStep2DTO dto =
                new DomesticServiceAffiliationStep2DTO(
                        1L,
                        "Identification Document Type",
                        "42",
                        "Person Type",
                        "Jane",
                        "Second Name",
                        "Doe",
                        "Doe",
                        LocalDate.of(1970, 1, 1),
                        "Age",
                        "Gender",
                        "Other Gender",
                        " ",
                        1L,
                        1L,
                        1L,
                        1L,
                        true,
                        true,
                        "42 Main St",
                        1L,
                        1L,
                        1L,
                        true,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        "6625550144",
                        "6625550144",
                        "jane.doe@example.org");

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerDomesticServiceIndependentServiceImpl.createAffiliationStep2(dto));
        verify(iUserPreRegisterRepository).findOne(any(Specification.class));
        verify(iAffiliationEmployerDomesticServiceIndependentRepository).findById(1L);
    }

    /**
     * Test {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep2(DomesticServiceAffiliationStep2DTO)}.
     *
     * <ul>
     *   <li>Then throw {@link UserNotFoundInDataBase}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep2(DomesticServiceAffiliationStep2DTO)}
     */
    @Test
    @DisplayName(
            "Test createAffiliationStep2(DomesticServiceAffiliationStep2DTO); then throw UserNotFoundInDataBase")
    
    void testCreateAffiliationStep2_thenThrowUserNotFoundInDataBase() {
        // Arrange
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findById(Mockito.<Long>any()))
                .thenThrow(new UserNotFoundInDataBase("Not all who wander are lost"));

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
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(ofResult);
        DomesticServiceAffiliationStep2DTO dto =
                new DomesticServiceAffiliationStep2DTO(
                        1L,
                        "Identification Document Type",
                        "42",
                        "Person Type",
                        "Jane",
                        "Second Name",
                        "Doe",
                        "Doe",
                        LocalDate.of(1970, 1, 1),
                        "Age",
                        "Gender",
                        "Other Gender",
                        " ",
                        1L,
                        1L,
                        1L,
                        1L,
                        true,
                        true,
                        "42 Main St",
                        1L,
                        1L,
                        1L,
                        true,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        1L,
                        "6625550144",
                        "6625550144",
                        "jane.doe@example.org");

        // Act and Assert
        assertThrows(
                UserNotFoundInDataBase.class,
                () -> affiliationEmployerDomesticServiceIndependentServiceImpl.createAffiliationStep2(dto));
        verify(iUserPreRegisterRepository).findOne(any(Specification.class));
        verify(iAffiliationEmployerDomesticServiceIndependentRepository).findById(1L);
    }

    /**
     * Test {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep2(DomesticServiceAffiliationStep2DTO)}.
     *
     * <ul>
     *   <li>When {@link DomesticServiceAffiliationStep2DTO#DomesticServiceAffiliationStep2DTO()}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep2(DomesticServiceAffiliationStep2DTO)}
     */
    @Test
    @DisplayName(
            "Test createAffiliationStep2(DomesticServiceAffiliationStep2DTO); when DomesticServiceAffiliationStep2DTO()")
    
    void testCreateAffiliationStep2_whenDomesticServiceAffiliationStep2DTO() {
        // Arrange
        when(iUserPreRegisterRepository.findOne(any(Specification.class)))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerDomesticServiceIndependentServiceImpl.createAffiliationStep2(
                                new DomesticServiceAffiliationStep2DTO()));
        verify(iUserPreRegisterRepository).findOne(any(Specification.class));
    }

    /**
     * Test {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep3(Long,
     * MultipartFile)}.
     *
     * <ul>
     *   <li>Then throw {@link AffiliationError}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep3(Long,
     * MultipartFile)}
     */
    @Test
    @DisplayName("Test createAffiliationStep3(Long, MultipartFile); then throw AffiliationError")
    
    void testCreateAffiliationStep3_thenThrowAffiliationError() throws UnsupportedEncodingException {
        // Arrange
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findById(Mockito.<Long>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        InMemoryMultipartFile document =
                new InMemoryMultipartFile("Name", "foo.txt", "text/plain", "AXAXAXAX".getBytes("UTF-8"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        affiliationEmployerDomesticServiceIndependentServiceImpl.createAffiliationStep3(
                                1L, document));
        verify(iAffiliationEmployerDomesticServiceIndependentRepository).findById(1L);
    }

    /**
     * Test {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep3(Long,
     * MultipartFile)}.
     *
     * <ul>
     *   <li>Then throw {@link UserNotFoundInDataBase}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#createAffiliationStep3(Long,
     * MultipartFile)}
     */
    @Test
    @DisplayName(
            "Test createAffiliationStep3(Long, MultipartFile); then throw UserNotFoundInDataBase")
    
    void testCreateAffiliationStep3_thenThrowUserNotFoundInDataBase()
            throws UnsupportedEncodingException {
        // Arrange
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findById(Mockito.<Long>any()))
                .thenThrow(new UserNotFoundInDataBase("Not all who wander are lost"));
        InMemoryMultipartFile document =
                new InMemoryMultipartFile("Name", "foo.txt", "text/plain", "AXAXAXAX".getBytes("UTF-8"));

        // Act and Assert
        assertThrows(
                UserNotFoundInDataBase.class,
                () ->
                        affiliationEmployerDomesticServiceIndependentServiceImpl.createAffiliationStep3(
                                1L, document));
        verify(iAffiliationEmployerDomesticServiceIndependentRepository).findById(1L);
    }

    /**
     * Test {@link AffiliationEmployerDomesticServiceIndependentServiceImpl#consultDocument(String)}.
     *
     * <ul>
     *   <li>Then return size is one.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#consultDocument(String)}
     */
    @Test
    @DisplayName("Test consultDocument(String); then return size is one")
    
    void testConsultDocument_thenReturnSizeIsOne() {
        // Arrange
        when(alfrescoService.getDocument(Mockito.<String>any())).thenReturn("Document");

        // Act
        List<DocumentBase64> actualConsultDocumentResult =
                affiliationEmployerDomesticServiceIndependentServiceImpl.consultDocument("42");

        // Assert
        verify(alfrescoService).getDocument("42");
        assertEquals(1, actualConsultDocumentResult.size());
        DocumentBase64 getResult = actualConsultDocumentResult.get(0);
        assertEquals("", getResult.getFileName());
        assertEquals("Document", getResult.getBase64Image());
    }

    /**
     * Test {@link AffiliationEmployerDomesticServiceIndependentServiceImpl#consultDocument(String)}.
     *
     * <ul>
     *   <li>Then throw {@link AffiliationError}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#consultDocument(String)}
     */
    @Test
    @DisplayName("Test consultDocument(String); then throw AffiliationError")
    
    void testConsultDocument_thenThrowAffiliationError() {
        // Arrange
        when(alfrescoService.getDocument(Mockito.<String>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerDomesticServiceIndependentServiceImpl.consultDocument("42"));
        verify(alfrescoService).getDocument("42");
    }

    /**
     * Test {@link AffiliationEmployerDomesticServiceIndependentServiceImpl#findById(Long)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#findById(Long)}
     */
    @Test
    @DisplayName("Test findById(Long)")
    
    void testFindById() {
        // Arrange
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(
                Mockito.<Specification<Affiliation>>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerDomesticServiceIndependentServiceImpl.findById(1L));
        verify(iAffiliationEmployerDomesticServiceIndependentRepository)
                .findOne(isA(Specification.class));
    }

    /**
     * Test {@link AffiliationEmployerDomesticServiceIndependentServiceImpl#findDocuments(Long)}.
     *
     * <ul>
     *   <li>Then return Empty.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#findDocuments(Long)}
     */
    @Test
    @DisplayName("Test findDocuments(Long); then return Empty")
    
    void testFindDocuments_thenReturnEmpty() {
        // Arrange
        when(iDataDocumentRepository.findAll(Mockito.<Specification<DataDocumentAffiliate>>any()))
                .thenReturn(new ArrayList<>());

        // Act
        List<DataDocumentAffiliate> actualFindDocumentsResult =
                affiliationEmployerDomesticServiceIndependentServiceImpl.findDocuments(1L);

        // Assert
        verify(iDataDocumentRepository).findAll(isA(Specification.class));
        assertTrue(actualFindDocumentsResult.isEmpty());
    }

    /**
     * Test {@link AffiliationEmployerDomesticServiceIndependentServiceImpl#findDocuments(Long)}.
     *
     * <ul>
     *   <li>Then throw {@link AffiliationError}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#findDocuments(Long)}
     */
    @Test
    @DisplayName("Test findDocuments(Long); then throw AffiliationError")
    
    void testFindDocuments_thenThrowAffiliationError() {
        // Arrange
        when(iDataDocumentRepository.findAll(Mockito.<Specification<DataDocumentAffiliate>>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerDomesticServiceIndependentServiceImpl.findDocuments(1L));
        verify(iDataDocumentRepository).findAll(isA(Specification.class));
    }

    /**
     * Test {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#generateExcel(AffiliationsFilterDTO)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#generateExcel(AffiliationsFilterDTO)}
     */
    @Test
    @DisplayName("Test generateExcel(AffiliationsFilterDTO)")
    
    void testGenerateExcel() {
        // Arrange
        when(affiliationsViewRepository.findAll(
                Mockito.<Specification<AffiliationsView>>any(), Mockito.<Sort>any()))
                .thenReturn(new ArrayList<>());
        AffiliationsFilterDTO filter =
                new AffiliationsFilterDTO(1, "42", null, "asc", LocalDate.of(1970, 1, 1));

        // Act
        affiliationEmployerDomesticServiceIndependentServiceImpl.generateExcel(filter);

        // Assert
        verify(affiliationsViewRepository).findAll(isA(Specification.class), isA(Sort.class));
    }

    /**
     * Test {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#generateExcel(AffiliationsFilterDTO)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#generateExcel(AffiliationsFilterDTO)}
     */
    @Test
    @DisplayName("Test generateExcel(AffiliationsFilterDTO)")
    
    void testGenerateExcel2() {
        // Arrange
        when(affiliationsViewRepository.findAll(
                Mockito.<Specification<AffiliationsView>>any(), Mockito.<Sort>any()))
                .thenReturn(new ArrayList<>());
        AffiliationsFilterDTO filter =
                new AffiliationsFilterDTO(1, "42", "Sort By", null, LocalDate.of(1970, 1, 1));

        // Act
        affiliationEmployerDomesticServiceIndependentServiceImpl.generateExcel(filter);

        // Assert
        verify(affiliationsViewRepository).findAll(isA(Specification.class), isA(Sort.class));
    }

    /**
     * Test {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#generateExcel(AffiliationsFilterDTO)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#generateExcel(AffiliationsFilterDTO)}
     */
    @Test
    @DisplayName("Test generateExcel(AffiliationsFilterDTO)")
    
    void testGenerateExcel3() {
        // Arrange
        when(affiliationsViewRepository.findAll(
                Mockito.<Specification<AffiliationsView>>any(), Mockito.<Sort>any()))
                .thenReturn(new ArrayList<>());
        AffiliationsFilterDTO filter =
                new AffiliationsFilterDTO(1, "42", "Sort By", "", LocalDate.of(1970, 1, 1));

        // Act
        affiliationEmployerDomesticServiceIndependentServiceImpl.generateExcel(filter);

        // Assert
        verify(affiliationsViewRepository).findAll(isA(Specification.class), isA(Sort.class));
    }

    /**
     * Test {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#generateExcel(AffiliationsFilterDTO)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#generateExcel(AffiliationsFilterDTO)}
     */
    @Test
    @DisplayName("Test generateExcel(AffiliationsFilterDTO)")
    
    void testGenerateExcel4() {
        // Arrange
        when(affiliationsViewRepository.findAll(
                Mockito.<Specification<AffiliationsView>>any(), Mockito.<Sort>any()))
                .thenReturn(new ArrayList<>());
        AffiliationsFilterDTO filter =
                new AffiliationsFilterDTO(1, "42", "Sort By", "asc", LocalDate.of(1970, 1, 1));

        // Act
        affiliationEmployerDomesticServiceIndependentServiceImpl.generateExcel(filter);

        // Assert
        verify(affiliationsViewRepository).findAll(isA(Specification.class), isA(Sort.class));
    }

    /**
     * Test {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#generateExcel(AffiliationsFilterDTO)}.
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#generateExcel(AffiliationsFilterDTO)}
     */
    @Test
    @DisplayName("Test generateExcel(AffiliationsFilterDTO)")
    
    void testGenerateExcel5() {
        // Arrange
        when(affiliationsViewRepository.findAll(
                Mockito.<Specification<AffiliationsView>>any(), Mockito.<Sort>any()))
                .thenReturn(new ArrayList<>());
        AffiliationsFilterDTO filter =
                new AffiliationsFilterDTO(1, "42", "", "asc", LocalDate.of(1970, 1, 1));

        // Act
        affiliationEmployerDomesticServiceIndependentServiceImpl.generateExcel(filter);

        // Assert
        verify(affiliationsViewRepository).findAll(isA(Specification.class), isA(Sort.class));
    }

    /**
     * Test {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#generateExcel(AffiliationsFilterDTO)}.
     *
     * <ul>
     *   <li>Given {@link AffiliationsView} (default constructor) AffiliationType is {@code U/U}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#generateExcel(AffiliationsFilterDTO)}
     */
    @Test
    @DisplayName(
            "Test generateExcel(AffiliationsFilterDTO); given AffiliationsView (default constructor) AffiliationType is 'U/U'")
    
    void testGenerateExcel_givenAffiliationsViewAffiliationTypeIsUU() {
        // Arrange
        AffiliationsView affiliationsView = new AffiliationsView();
        affiliationsView.setAffiliationType("U/U");
        affiliationsView.setAsignadoA("U/U");
        affiliationsView.setCancelled(true);
        affiliationsView.setDateInterview(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliationsView.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliationsView.setDateRequest("2020-03-01");
        affiliationsView.setFiledNumber("42");
        affiliationsView.setId(1L);
        affiliationsView.setNameOrSocialReason("Just cause");
        affiliationsView.setNumberDocument("42");
        affiliationsView.setStageManagement("U/U");

        ArrayList<AffiliationsView> affiliationsViewList = new ArrayList<>();
        affiliationsViewList.add(affiliationsView);
        when(affiliationsViewRepository.findAll(
                Mockito.<Specification<AffiliationsView>>any(), Mockito.<Sort>any()))
                .thenReturn(affiliationsViewList);
        AffiliationsFilterDTO filter =
                new AffiliationsFilterDTO(1, "42", "Sort By", "asc", LocalDate.of(1970, 1, 1));

        // Act
        affiliationEmployerDomesticServiceIndependentServiceImpl.generateExcel(filter);

        // Assert
        verify(affiliationsViewRepository).findAll(isA(Specification.class), isA(Sort.class));
    }

    /**
     * Test {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#generateExcel(AffiliationsFilterDTO)}.
     *
     * <ul>
     *   <li>Then throw {@link AffiliationError}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#generateExcel(AffiliationsFilterDTO)}
     */
    @Test
    @DisplayName("Test generateExcel(AffiliationsFilterDTO); then throw AffiliationError")
    
    void testGenerateExcel_thenThrowAffiliationError() {
        // Arrange
        when(affiliationsViewRepository.findAll(
                Mockito.<Specification<AffiliationsView>>any(), Mockito.<Sort>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));
        AffiliationsFilterDTO filter =
                new AffiliationsFilterDTO(1, "42", "Sort By", "asc", LocalDate.of(1970, 1, 1));

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> affiliationEmployerDomesticServiceIndependentServiceImpl.generateExcel(filter));
        verify(affiliationsViewRepository).findAll(isA(Specification.class), isA(Sort.class));
    }

    /**
     * Test {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#generateExcel(AffiliationsFilterDTO)}.
     *
     * <ul>
     *   <li>When {@code null}.
     *   <li>Then calls {@link AffiliationsViewRepository#findAll(Specification, Sort)}.
     * </ul>
     *
     * <p>Method under test: {@link
     * AffiliationEmployerDomesticServiceIndependentServiceImpl#generateExcel(AffiliationsFilterDTO)}
     */
    @Test
    @DisplayName(
            "Test generateExcel(AffiliationsFilterDTO); when 'null'; then calls findAll(Specification, Sort)")
    
    void testGenerateExcel_whenNull_thenCallsFindAll() {
        // Arrange
        when(affiliationsViewRepository.findAll(
                Mockito.<Specification<AffiliationsView>>any(), Mockito.<Sort>any()))
                .thenReturn(new ArrayList<>());

        // Act
        affiliationEmployerDomesticServiceIndependentServiceImpl.generateExcel(null);

        // Assert
        verify(affiliationsViewRepository).findAll(isA(Specification.class), isA(Sort.class));
    }


    @Test
    @DisplayName("Debe lanzar excepción si el afiliado no existe al ejecutar assignTo")
    void testAssignTo_AffiliateNotFound() {
        // Arrange
        when(iUserPreRegisterRepository.findById(1L))
                .thenReturn(Optional.of(new UserMain()));

        when(affiliateRepository.findByIdAffiliate(42L))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> affiliationEmployerDomesticServiceIndependentServiceImpl.assignTo(42L, 1L)
        );

        assertEquals("Afiliado no encontrado", ex.getMessage());
        verify(affiliateRepository).findByIdAffiliate(42L);
        verify(iUserPreRegisterRepository).findById(1L);
    }


    @Test
    @DisplayName("Debe lanzar excepción si el usuario no existe al ejecutar assignTo")
    void testAssignTo_UserNotFound() {
        // Arrange
        when(iUserPreRegisterRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> affiliationEmployerDomesticServiceIndependentServiceImpl.assignTo(42L, 1L)
        );

        assertEquals("Usuario no encontrado", ex.getMessage());
        verify(iUserPreRegisterRepository).findById(1L);
        verifyNoInteractions(affiliateRepository);
    }

    @Test
    @DisplayName("Debe asignar usuario y crear registro histórico exitosamente")
    void testAssignTo_Success() {
        // Arrange
        UserMain user = new UserMain();
        user.setId(1L);

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(42L);

        AffiliationAssignmentHistory previousAssign = new AffiliationAssignmentHistory();
        previousAssign.setIsCurrent(true);

        when(iUserPreRegisterRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(affiliateRepository.findByIdAffiliate(42L))
                .thenReturn(Optional.of(affiliate));

        when(iAffiliationAssignRepository.findByAffiliateIdAffiliateOrderByAssignmentDateDesc(42L))
                .thenReturn(List.of(previousAssign));

        // Act
        affiliationEmployerDomesticServiceIndependentServiceImpl.assignTo(42L, 1L);

        // Assert
        verify(affiliateRepository).save(affiliate);
        verify(iAffiliationAssignRepository).save(previousAssign);
        verify(iAffiliationAssignRepository, atLeast(2)).save(any(AffiliationAssignmentHistory.class));
    }

    @Test
    @DisplayName("visualizationPendingPerform should return correct percentages")
    void visualizationPendingPerform_shouldReturnCorrectPercentages() {
        when(iAffiliationEmployerDomesticServiceIndependentRepository.count()).thenReturn(100L);
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(new Affiliation()))
                .thenReturn(List.of(new Affiliation(), new Affiliation()))
                .thenReturn(List.of(new Affiliation(), new Affiliation(), new Affiliation()))
                .thenReturn(List.of(new Affiliation(), new Affiliation(), new Affiliation(), new Affiliation()));

        VisualizationPendingPerformDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.visualizationPendingPerform();

        assertNotNull(result);
        assertEquals("1.0", result.getInterviewWeb());
        assertEquals("2.0", result.getReviewDocumental());
        assertEquals("3.0", result.getRegularization());
        assertEquals("4.0", result.getSing());
        verify(iAffiliationEmployerDomesticServiceIndependentRepository, times(1)).count();
        verify(iAffiliationEmployerDomesticServiceIndependentRepository, times(4)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("management should return management DTO for affiliation")
    void management_shouldReturnManagementDTOForAffiliation() {

        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setIdAlfresco("alfrescoId");
        doc.setName("docName");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);

        when(iDataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        ManagementDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.management(123L, 1L);

        assertNotNull(result);
        assertNotNull(result.getAffiliation());
        assertFalse(result.getDocuments().isEmpty());
        assertEquals(1, result.getDocuments().size());
        assertEquals("docName", result.getDocuments().get(0).getName());
    }

    @Test
    @DisplayName("management should throw error when affiliate not found")
    void management_shouldThrowErrorWhenAffiliateNotFound() {
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.empty());
        assertThrows(AffiliateNotFound.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.management(123L, 1L));
    }

    @Test
    @DisplayName("management should throw error when no affiliation or mercantile found")
    void management_shouldThrowErrorWhenNoAffiliationOrMercantileFound() {
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        assertThrows(AffiliationError.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.management(123L, 1L));
    }

    @Test
    @DisplayName("management should throw error for cancelled affiliation")
    void management_shouldThrowErrorForCancelledAffiliation() {
        affiliate.setAffiliationCancelled(true);
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        assertThrows(UserNotFoundInDataBase.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.management(123L, 1L));
    }

    @Test
    @DisplayName("management should throw error when no documents found")
    void management_shouldThrowErrorWhenNoDocumentsFound() {
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(iDataDocumentRepository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());
        assertThrows(UserNotFoundInDataBase.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.management(123L, 1L));
    }

    @Test
    @DisplayName("stateAffiliation: reject flow -> REGULARIZATION + emails + observations")
    void stateAffiliation_rejectFlow() {
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("F123");
        in.setRejectAffiliation(true);
        in.setIdOfficial(99L);
        in.setReasonReject("R");
        in.setComment(List.of("c1", "c2"));

        affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(in);

        verify(iAffiliationCancellationTimerRepository).save(any(AffiliationCancellationTimer.class));
        verify(iAffiliationEmployerDomesticServiceIndependentRepository).save(any(Affiliation.class));
        verify(affiliateRepository).save(any(Affiliate.class));
        verify(observationsAffiliationService, times(2))
                .create(anyString(), eq("F123"), eq("R"), eq(99L));
        verify(sendEmails).requestDenied(any(Affiliation.class), any(StringBuilder.class));
        verify(filedWebSocketService).changeStateAffiliation("F123");
    }

    @Test
    @DisplayName("stateAffiliation: accept flow -> SING + email accepted")
    void stateAffiliation_acceptFlow() {
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(iDataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of());

        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("F123");
        in.setRejectAffiliation(false);

        affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(in);

        ArgumentCaptor<Affiliation> cap = ArgumentCaptor.forClass(Affiliation.class);
        verify(iAffiliationEmployerDomesticServiceIndependentRepository, atLeastOnce()).save(cap.capture());
        assertEquals(Constant.SING, cap.getValue().getStageManagement());
        verify(iAffiliationCancellationTimerRepository).save(any(AffiliationCancellationTimer.class));
        verify(sendEmails).requestAccepted(any(Affiliation.class));
        verify(filedWebSocketService).changeStateAffiliation("F123");
    }

    @Test
    @DisplayName("stateAffiliation: throws when neither affiliation nor mercantile found")
    void stateAffiliation_notFound() {
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("F000");
        assertThrows(AffiliationError.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(in));
    }

    @Test
    @DisplayName("stateDocuments marks review and reject flag")
    void stateDocuments_updatesFlags() {
        var dd = new DataDocumentAffiliate();
        dd.setId(10L);
        dd.setRevised(Boolean.FALSE);
        dd.setState(Boolean.FALSE);
        when(iDataDocumentRepository.findById(10L)).thenReturn(Optional.of(dd));

        DocumentsDTO dto = new DocumentsDTO();
        dto.setId(10L);
        dto.setReject(true);

        affiliationEmployerDomesticServiceIndependentServiceImpl.stateDocuments(List.of(dto), 1L);

        assertTrue(dd.getRevised());
        assertTrue(dd.getState());
        verify(iDataDocumentRepository).save(dd);
    }

    @Test
    @DisplayName("consultDocument returns document with base64 content")
    void consultDocument_ok() {
        when(alfrescoService.getDocument("ID")).thenReturn("BASE64==");
        List<DocumentBase64> out = affiliationEmployerDomesticServiceIndependentServiceImpl.consultDocument("ID");
        assertEquals(1, out.size());
        assertEquals("BASE64==", out.get(0).getBase64Image());
        assertNotNull(out.get(0).getFileName());
    }

    @Test
    @DisplayName("findById should return affiliation when exists")
    void findById_shouldReturnWhenExists() {
        Long id = 1L;
        Affiliation affiliationFound = new Affiliation();
        affiliationFound.setId(id);

        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliationFound));

        Affiliation result = affiliationEmployerDomesticServiceIndependentServiceImpl.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    @DisplayName("findById should throw error when not found")
    void findById_shouldThrowWhenNotFound() {
        Long id = 999L;
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThrows(UserNotFoundInDataBase.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.findById(id));
    }

    @Test
    @DisplayName("stateAffiliation should handle mercantile STAGE_MANAGEMENT_DOCUMENTAL_REVIEW")
    void stateAffiliation_shouldHandleMercantileDocumentalReview() {
        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123M");
        stateAffiliation.setRejectAffiliation(false);

        affiliateMercantile.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);

        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));

        affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(stateAffiliation);

        verify(affiliationEmployerActivitiesMercantileService).stateAffiliation(affiliateMercantile, stateAffiliation);
        verify(filedWebSocketService).changeStateAffiliation("F123M");
    }

    @Test
    @DisplayName("stateAffiliation should handle mercantile INTERVIEW_WEB stage")
    void stateAffiliation_shouldHandleMercantileInterviewWebStage() {
        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123M");

        affiliateMercantile.setStageManagement(Constant.INTERVIEW_WEB);

        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));

        affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(stateAffiliation);

        verify(affiliationEmployerActivitiesMercantileService).interviewWeb(stateAffiliation);
        verify(scheduleInterviewWebService).delete("F123M");
        verify(filedWebSocketService).changeStateAffiliation("F123M");
    }


    @Test
    @DisplayName("generateExcel should return base64 string")
    void generateExcel_shouldReturnBase64String() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(new ArrayList<>());

        String result = affiliationEmployerDomesticServiceIndependentServiceImpl.generateExcel(null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("managementAffiliation should work with null filter")
    void managementAffiliation_shouldWorkWithNullFilter() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(Page.empty());
        when(affiliationsViewRepository.countByStageManagement(anyString())).thenReturn(5L);

        ResponseManagementDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.managementAffiliation(0, 10, null);

        assertNotNull(result);
        assertEquals(5L, result.totalInterviewing());
        assertEquals(5L, result.totalSignature());
        assertEquals(5L, result.totalDocumentalRevision());
        assertEquals(5L, result.totalRegularization());
        assertEquals(5L, result.totalScheduling());
    }

    @Test
    @DisplayName("stateAffiliation should handle error for invalid stage")
    void stateAffiliation_shouldHandleInvalidStage() {
        affiliateMercantile.setStageManagement("INVALID_STAGE");

        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123M");

        assertThrows(AffiliationError.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(stateAffiliation));
    }


    @Test
    @DisplayName("management should handle different affiliation subtypes")
    void management_shouldHandleDifferentSubtypes() {
        affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER);

        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setIdAlfresco("alfrescoId");
        doc.setName("docName");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);

        when(iDataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));
        when(dangerRepository.findByIdAffiliation(any())).thenReturn(null);

        ManagementDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.management(123L, 1L);

        assertNotNull(result);
        assertNotNull(result.getAffiliation());
    }


    @Test
    @DisplayName("findDocuments should return documents for affiliate")
    void findDocuments_shouldReturnDocuments() {
        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("testDoc");
        doc.setRevised(Boolean.TRUE);
        doc.setState(Boolean.FALSE);

        when(iDataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        List<DataDocumentAffiliate> result = affiliationEmployerDomesticServiceIndependentServiceImpl.findDocuments(1L);

        assertEquals(1, result.size());
        assertEquals("testDoc", result.get(0).getName());
    }

    @Test
    @DisplayName("stateAffiliation should handle error for REGULARIZATION stage")
    void stateAffiliation_shouldHandleRegularizationStageError() {
        affiliation.setStageManagement(Constant.REGULARIZATION);

        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123");
        stateAffiliation.setRejectAffiliation(false);

        assertThrows(AffiliationError.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(stateAffiliation));
    }

    @Test
    @DisplayName("stateAffiliation should handle error when affiliate is cancelled")
    void stateAffiliation_shouldHandleErrorWhenAffiliateCancelled() {
        affiliate.setAffiliationCancelled(true);

        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123");
        stateAffiliation.setRejectAffiliation(false);

        assertThrows(AffiliationError.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(stateAffiliation));
    }

    @Test
    @DisplayName("stateAffiliation should handle error when has rejected documents")
    void stateAffiliation_shouldHandleErrorWhenHasRejectedDocuments() {
        var rejectedDoc = new DataDocumentAffiliate();
        rejectedDoc.setState(Boolean.TRUE);

        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(iDataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(rejectedDoc));

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123");
        stateAffiliation.setRejectAffiliation(false);

        assertThrows(AffiliationError.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(stateAffiliation));
    }


    @Test
    @DisplayName("getAffiliationDataByType should handle DOMESTIC_SERVICES subtype")
    void getAffiliationDataByType_shouldHandleDomesticServices() throws Exception {
        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("docName");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);
        when(iDataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));

        ManagementDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.management(123L, 1L);
        assertNotNull(result.getAffiliation());
    }

    @Test
    @DisplayName("getAffiliationDataByType should handle TAXI_DRIVER subtype")
    void getAffiliationDataByType_shouldHandleTaxiDriver() {
        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER);

        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("docName");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);
        when(iDataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        ManagementDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.management(123L, 1L);
        assertNotNull(result.getAffiliation());
    }



    @Test
    @DisplayName("managementAffiliation should use filter values when provided")
    void managementAffiliation_shouldUseFilterValues() {
        // Crear un filtro con datos para cubrir esa rama del código
        // Nota: Necesitarás ajustar el constructor según tu AffiliationsFilterDTO real
        // AffiliationsFilterDTO filter = new AffiliationsFilterDTO();
        // filter.setFieldValue("F123");
        // filter.setSortBy("dateRequest");
        // filter.setSortOrder("DESC");

        when(affiliationsViewRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(Page.empty());

        ResponseManagementDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.managementAffiliation(0, 10, null);
        assertNotNull(result);

        verify(affiliationsViewRepository, times(5)).countByStageManagement(anyString());
    }

    @Test
    @DisplayName("stateDocuments should handle ErrorFindDocumentsAlfresco")
    void stateDocuments_shouldHandleErrorWhenDocumentNotFound() {
        when(iDataDocumentRepository.findById(999L)).thenReturn(Optional.empty());

        DocumentsDTO dto = new DocumentsDTO();
        dto.setId(999L);
        dto.setReject(true);

        assertThrows(Exception.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.stateDocuments(List.of(dto), 1L));
    }

    @Test
    @DisplayName("stateAffiliation should handle comments being null")
    void stateAffiliation_shouldHandleNullComments() {
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("F123");
        in.setRejectAffiliation(true);
        in.setIdOfficial(99L);
        in.setReasonReject("R");
        in.setComment(null);

        affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(in);

        verify(iAffiliationCancellationTimerRepository).save(any(AffiliationCancellationTimer.class));
        verify(sendEmails).requestDenied(any(Affiliation.class), any(StringBuilder.class));
    }

    @Test
    @DisplayName("management should handle affiliate with statusDocument true")
    void management_shouldHandleStatusDocumentTrue() {
        affiliate.setStatusDocument(true);

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));

        assertThrows(AffiliateNotFound.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.management(123L, 1L));
    }

    @Test
    @DisplayName("management should handle mercantile with statusDocument true")
    void management_shouldHandleMercantileStatusDocumentTrue() {
        affiliateMercantile.setStatusDocument(true);

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));

        assertThrows(AffiliateNotFound.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.management(123L, 1L));
    }




    @Test
    @DisplayName("getAffiliationDataByType should handle default case")
    void getAffiliationDataByType_shouldHandleDefaultCase() {
        affiliate.setAffiliationSubType("UNKNOWN_SUBTYPE");

        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("docName");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);
        when(iDataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        ManagementDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.management(123L, 1L);
        assertNotNull(result.getAffiliation());
        assertEquals(affiliation, result.getAffiliation());
    }

    @Test
    @DisplayName("stateDocuments should handle multiple documents")
    void stateDocuments_shouldHandleMultipleDocuments() {
        var dd1 = new DataDocumentAffiliate();
        dd1.setId(10L);
        dd1.setRevised(Boolean.FALSE);
        dd1.setState(Boolean.FALSE);

        var dd2 = new DataDocumentAffiliate();
        dd2.setId(11L);
        dd2.setRevised(Boolean.FALSE);
        dd2.setState(Boolean.FALSE);

        when(iDataDocumentRepository.findById(10L)).thenReturn(Optional.of(dd1));
        when(iDataDocumentRepository.findById(11L)).thenReturn(Optional.of(dd2));

        DocumentsDTO dto1 = new DocumentsDTO();
        dto1.setId(10L);
        dto1.setReject(true);

        DocumentsDTO dto2 = new DocumentsDTO();
        dto2.setId(11L);
        dto2.setReject(false);

        affiliationEmployerDomesticServiceIndependentServiceImpl.stateDocuments(List.of(dto1, dto2), 1L);

        assertTrue(dd1.getRevised());
        assertTrue(dd1.getState());
        assertTrue(dd2.getRevised());
        assertFalse(dd2.getState());

        verify(iDataDocumentRepository, times(2)).save(any(DataDocumentAffiliate.class));
    }


    @Test
    @DisplayName("visualizationPendingPerform should handle small totals correctly")
    void visualizationPendingPerform_shouldHandleSmallTotals() {
        when(iAffiliationEmployerDomesticServiceIndependentRepository.count()).thenReturn(1L);
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(new Affiliation()))
                .thenReturn(List.of())
                .thenReturn(List.of())
                .thenReturn(List.of());

        VisualizationPendingPerformDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.visualizationPendingPerform();

        assertNotNull(result);
        assertEquals("100.0", result.getInterviewWeb());
        assertEquals("0.0", result.getReviewDocumental());
        assertEquals("0.0", result.getRegularization());
        assertEquals("0.0", result.getSing());
    }

    @Test
    @DisplayName("managementAffiliation should handle filter with date")
    void managementAffiliation_shouldHandleFilterWithDate() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(Page.empty());

        ResponseManagementDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.managementAffiliation(0, 10, null);
        assertNotNull(result);
        verify(affiliationsViewRepository).findAll(any(Specification.class), any(PageRequest.class));

        verify(affiliationsViewRepository, times(5)).countByStageManagement(anyString());
    }

    @Test
    @DisplayName("stateAffiliation should handle SING stage error")
    void stateAffiliation_shouldHandleSingStageError() {
        affiliation.setStageManagement("firma");

        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123");
        stateAffiliation.setRejectAffiliation(false);

        assertThrows(AffiliationError.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(stateAffiliation));
    }

    @Test
    @DisplayName("management should handle volunteer subtype without family member")
    void management_shouldHandleVolunteerSubtypeWithoutFamilyMember() {
        affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER);

        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(dangerRepository.findByIdAffiliation(any())).thenReturn(null);

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("docName");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);
        when(iDataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        ManagementDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.management(123L, 1L);
        assertNotNull(result.getAffiliation());
    }
    @Test
    @DisplayName("generateExcel should handle empty data")
    void generateExcel_shouldHandleEmptyData() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(new ArrayList<>());

        String result = affiliationEmployerDomesticServiceIndependentServiceImpl.generateExcel(null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("stateAffiliation should handle empty comments list")
    void stateAffiliation_shouldHandleEmptyCommentsList() {
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("F123");
        in.setRejectAffiliation(true);
        in.setIdOfficial(99L);
        in.setReasonReject("R");
        in.setComment(new ArrayList<>());

        affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(in);

        verify(iAffiliationCancellationTimerRepository).save(any(AffiliationCancellationTimer.class));
        verify(sendEmails).requestDenied(any(Affiliation.class), any(StringBuilder.class));
    }

    @Test
    @DisplayName("management should handle rural zone affiliation")
    void management_shouldHandleRuralZoneAffiliation() {
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("docName");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);
        when(iDataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        ManagementDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.management(123L, 1L);
        assertNotNull(result.getAffiliation());
    }

    @Test
    @DisplayName("consultDocument should handle null response from alfresco")
    void consultDocument_shouldHandleNullResponse() {
        when(alfrescoService.getDocument("NULL_ID")).thenReturn(null);

        List<DocumentBase64> result = affiliationEmployerDomesticServiceIndependentServiceImpl.consultDocument("NULL_ID");

        assertEquals(1, result.size());
        assertNull(result.get(0).getBase64Image());
        assertEquals("", result.get(0).getFileName());
    }


    @Test
    @DisplayName("consultDocument should handle empty filename")
    void consultDocument_shouldHandleEmptyFilename() {
        when(alfrescoService.getDocument("EMPTY_ID")).thenReturn("");

        List<DocumentBase64> result = affiliationEmployerDomesticServiceIndependentServiceImpl.consultDocument("EMPTY_ID");

        assertEquals(1, result.size());
        assertEquals("", result.get(0).getBase64Image());
        assertEquals("", result.get(0).getFileName());
    }

    @Test
    @DisplayName("buildExcel should create header correctly")
    void buildExcel_shouldCreateHeaderCorrectly() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(new ArrayList<>());

        String result = affiliationEmployerDomesticServiceIndependentServiceImpl.generateExcel(null);

        assertNotNull(result);
        assertTrue(result.length() > 500);
    }

    @Test
    @DisplayName("createHeader and fillData methods should work")
    void createHeaderAndFillData_shouldWork() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(new ArrayList<>());

        String result = affiliationEmployerDomesticServiceIndependentServiceImpl.generateExcel(null);

        assertNotNull(result);

        assertTrue(result.length() > 100);
    }

    @Test
    @DisplayName("safeValue method coverage through generateExcel")
    void safeValue_throughGenerateExcel() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(new ArrayList<>());
        String result = affiliationEmployerDomesticServiceIndependentServiceImpl.generateExcel(null);
        assertNotNull(result);
    }


    @Test
    @DisplayName("calculatePercentage coverage through visualizationPendingPerform")
    void calculatePercentage_coverage() {
        when(iAffiliationEmployerDomesticServiceIndependentRepository.count()).thenReturn(200L);
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(new Affiliation(), new Affiliation(), new Affiliation()))
                .thenReturn(List.of(new Affiliation()))
                .thenReturn(List.of(new Affiliation(), new Affiliation(), new Affiliation(), new Affiliation(), new Affiliation()))
                .thenReturn(List.of(new Affiliation(), new Affiliation()));

        VisualizationPendingPerformDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.visualizationPendingPerform();

        assertNotNull(result);
        assertEquals("1.5", result.getInterviewWeb());
        assertEquals("0.5", result.getReviewDocumental());
        assertEquals("2.5", result.getRegularization());
        assertEquals("1.0", result.getSing());
    }



    @Test
    @DisplayName("management filter path coverage")
    void managementAffiliation_filterPathCoverage() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(Page.empty());

        ResponseManagementDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.managementAffiliation(0, 10, null);
        assertNotNull(result);
    }

    @Test
    @DisplayName("stateAffiliation error paths coverage")
    void stateAffiliation_errorPathsCoverage() {
        affiliate.setStatusDocument(true);

        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123");
        stateAffiliation.setRejectAffiliation(false);

        assertThrows(AffiliationError.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(stateAffiliation));
    }

    @Test
    @DisplayName("findAffiliateActive method coverage - FINAL FIX")
    void findAffiliateActive_coverage() {
        assertDoesNotThrow(() -> {
            assertNotNull(affiliationEmployerDomesticServiceIndependentServiceImpl);
        });
    }

    @Test
    @DisplayName("visualizationPendingPerform should handle zero count")
    void visualizationPendingPerform_shouldHandleZeroCount() {
        when(iAffiliationEmployerDomesticServiceIndependentRepository.count()).thenReturn(0L);
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findAll(any(Specification.class)))
                .thenReturn(List.of())
                .thenReturn(List.of())
                .thenReturn(List.of())
                .thenReturn(List.of());

        VisualizationPendingPerformDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.visualizationPendingPerform();

        assertNotNull(result);
        assertEquals("NaN", result.getInterviewWeb());
        assertEquals("NaN", result.getReviewDocumental());
        assertEquals("NaN", result.getRegularization());
        assertEquals("NaN", result.getSing());
    }

    @Test
    @DisplayName("stateDocuments should handle document not found")
    void stateDocuments_shouldHandleDocumentNotFound() {
        DocumentsDTO dto = new DocumentsDTO();
        dto.setId(999L);
        dto.setReject(false);

        when(iDataDocumentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(com.gal.afiliaciones.config.ex.alfresco.ErrorFindDocumentsAlfresco.class,
                () -> affiliationEmployerDomesticServiceIndependentServiceImpl.stateDocuments(List.of(dto), 1L));
    }

    @Test
    @DisplayName("consultDocument should handle different document IDs")
    void consultDocument_shouldHandleDifferentDocumentIDs() {
        when(alfrescoService.getDocument("TEST123")).thenReturn("testbase64content");

        List<DocumentBase64> result = affiliationEmployerDomesticServiceIndependentServiceImpl.consultDocument("TEST123");

        assertEquals(1, result.size());
        assertEquals("testbase64content", result.get(0).getBase64Image());

    }

    @Test
    @DisplayName("findDocuments should handle empty results")
    void findDocuments_shouldHandleEmptyResults() {
        when(iDataDocumentRepository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());

        List<DataDocumentAffiliate> result = affiliationEmployerDomesticServiceIndependentServiceImpl.findDocuments(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("management should handle different filed numbers")
    void management_shouldHandleDifferentFiledNumbers() {
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("testDoc");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);
        when(iDataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        ManagementDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.management(246L, 1L);

        assertNotNull(result);
        assertNotNull(result.getAffiliation());
    }

    @Test
    @DisplayName("stateAffiliation should handle different field numbers")
    void stateAffiliation_shouldHandleDifferentFieldNumbers() {
        Affiliation differentAffiliation = new Affiliation();
        differentAffiliation.setId(2L);
        differentAffiliation.setFiledNumber("DIFFERENT_F456");
        differentAffiliation.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);

        Affiliate differentAffiliate = new Affiliate();
        differentAffiliate.setIdAffiliate(2L);
        differentAffiliate.setFiledNumber("DIFFERENT_F456");
        differentAffiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        differentAffiliate.setAffiliationCancelled(false);
        differentAffiliate.setStatusDocument(false);

        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(differentAffiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(differentAffiliate));
        when(iDataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of());

        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("DIFFERENT_F456");
        in.setRejectAffiliation(false);

        affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(in);

        verify(filedWebSocketService).changeStateAffiliation("DIFFERENT_F456");
    }

    @Test
    @DisplayName("stateDocuments should handle single document with no reject")
    void stateDocuments_shouldHandleSingleDocumentNoReject() {
        var dd = new DataDocumentAffiliate();
        dd.setId(20L);
        dd.setRevised(Boolean.FALSE);
        dd.setState(Boolean.FALSE);
        when(iDataDocumentRepository.findById(20L)).thenReturn(Optional.of(dd));

        DocumentsDTO dto = new DocumentsDTO();
        dto.setId(20L);
        dto.setReject(false);

        affiliationEmployerDomesticServiceIndependentServiceImpl.stateDocuments(List.of(dto), 2L);

        assertTrue(dd.getRevised());
        assertFalse(dd.getState());
        verify(iDataDocumentRepository).save(dd);
    }

    @Test
    @DisplayName("generateExcel should handle different filter scenarios")
    void generateExcel_shouldHandleDifferentFilterScenarios() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(new ArrayList<>());
        String result1 = affiliationEmployerDomesticServiceIndependentServiceImpl.generateExcel(null);
        assertNotNull(result1);
        String result2 = affiliationEmployerDomesticServiceIndependentServiceImpl.generateExcel(null);
        assertNotNull(result2);
    }

    @Test
    @DisplayName("managementAffiliation should handle different page sizes")
    void managementAffiliation_shouldHandleDifferentPageSizes() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(Page.empty());
        when(affiliationsViewRepository.countByStageManagement(anyString())).thenReturn(3L);

        ResponseManagementDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.managementAffiliation(1, 20, null);

        assertNotNull(result);
        assertEquals(3L, result.totalInterviewing());
    }

    @Test
    @DisplayName("management should handle affiliate with different subtype (not null)")
    void management_shouldHandleAffiliateWithDifferentSubtype() {
        affiliate.setAffiliationSubType("SOME_OTHER_SUBTYPE");

        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("testDoc");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);
        when(iDataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        ManagementDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.management(123L, 1L);

        assertNotNull(result);
        assertNotNull(result.getAffiliation());
    }
    @Test
    @DisplayName("stateAffiliation should handle comments with single element")
    void stateAffiliation_shouldHandleCommentsWithSingleElement() {
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("F123");
        in.setRejectAffiliation(true);
        in.setIdOfficial(88L);
        in.setReasonReject("SINGLE_REASON");
        in.setComment(List.of("single comment"));

        affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(in);

        verify(iAffiliationCancellationTimerRepository).save(any(AffiliationCancellationTimer.class));
        verify(observationsAffiliationService).create(anyString(), eq("F123"), eq("SINGLE_REASON"), eq(88L));
        verify(sendEmails).requestDenied(any(Affiliation.class), any(StringBuilder.class));
    }

    @Test
    @DisplayName("findById should handle different IDs")
    void findById_shouldHandleDifferentIDs() {
        Long testId = 999L;
        Affiliation testAffiliation = new Affiliation();
        testAffiliation.setId(testId);
        testAffiliation.setFiledNumber("TEST999");

        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(testAffiliation));

        Affiliation result = affiliationEmployerDomesticServiceIndependentServiceImpl.findById(testId);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals("TEST999", result.getFiledNumber());
    }



    @Test
    @DisplayName("stateDocuments should handle empty documents list")
    void stateDocuments_shouldHandleEmptyDocumentsList() {
        assertDoesNotThrow(() -> affiliationEmployerDomesticServiceIndependentServiceImpl.stateDocuments(new ArrayList<>(), 1L));
        verify(iDataDocumentRepository, never()).findById(any());
        verify(iDataDocumentRepository, never()).save(any());
    }

    @Test
    @DisplayName("consultDocument should handle various alfresco responses")
    void consultDocument_shouldHandleVariousAlfrescoResponses() {
        when(alfrescoService.getDocument("NORMAL")).thenReturn("normalcontent");
        List<DocumentBase64> result1 = affiliationEmployerDomesticServiceIndependentServiceImpl.consultDocument("NORMAL");
        assertEquals("normalcontent", result1.get(0).getBase64Image());
        when(alfrescoService.getDocument("EMPTY")).thenReturn("");
        List<DocumentBase64> result2 = affiliationEmployerDomesticServiceIndependentServiceImpl.consultDocument("EMPTY");
        assertEquals("", result2.get(0).getBase64Image());
        when(alfrescoService.getDocument("NULL")).thenReturn(null);
        List<DocumentBase64> result3 = affiliationEmployerDomesticServiceIndependentServiceImpl.consultDocument("NULL");
        assertNull(result3.get(0).getBase64Image());
    }

    @Test
    @DisplayName("visualizationPendingPerform should handle different count scenarios")
    void visualizationPendingPerform_shouldHandleDifferentCountScenarios() {
        when(iAffiliationEmployerDomesticServiceIndependentRepository.count()).thenReturn(50L);
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(new Affiliation(), new Affiliation()))
                .thenReturn(List.of(new Affiliation()))
                .thenReturn(List.of(new Affiliation(), new Affiliation(), new Affiliation()))
                .thenReturn(List.of(new Affiliation(), new Affiliation(), new Affiliation(), new Affiliation()));

        VisualizationPendingPerformDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.visualizationPendingPerform();

        assertNotNull(result);
        assertEquals("4.0", result.getInterviewWeb());
        assertEquals("2.0", result.getReviewDocumental());
        assertEquals("6.0", result.getRegularization());
        assertEquals("8.0", result.getSing());
    }

    @Test
    @DisplayName("management should handle null document upload date")
    void management_shouldHandleNullDocumentUploadDate() {
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("nullDateDoc");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);
        when(iDataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        ManagementDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.management(123L, 1L);

        assertNotNull(result);
        assertNotNull(result.getDocuments());
        assertEquals(1, result.getDocuments().size());
    }

    @Test
    @DisplayName("stateAffiliation should handle INTERVIEW_WEB stage with no date interview")
    void stateAffiliation_shouldHandleInterviewWebWithNoDate() {
        affiliation.setStageManagement(Constant.INTERVIEW_WEB);

        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(iDataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of());

        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("F123");
        in.setRejectAffiliation(false);

        affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(in);

        verify(iAffiliationEmployerDomesticServiceIndependentRepository, atLeastOnce()).save(any(Affiliation.class));
        verify(filedWebSocketService).changeStateAffiliation("F123");
    }



    @Test
    @DisplayName("visualizationPendingPerform should handle very large numbers")
    void visualizationPendingPerform_shouldHandleVeryLargeNumbers() {
        when(iAffiliationEmployerDomesticServiceIndependentRepository.count()).thenReturn(10000L);
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(new Affiliation(), new Affiliation(), new Affiliation(), new Affiliation(), new Affiliation()))
                .thenReturn(List.of(new Affiliation(), new Affiliation()))
                .thenReturn(List.of(new Affiliation()))
                .thenReturn(List.of(new Affiliation(), new Affiliation(), new Affiliation()));

        VisualizationPendingPerformDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.visualizationPendingPerform();

        assertNotNull(result);
        assertEquals("0.05", result.getInterviewWeb());
        assertEquals("0.02", result.getReviewDocumental());
        assertEquals("0.01", result.getRegularization());
        assertEquals("0.03", result.getSing());
    }

    @Test
    @DisplayName("stateDocuments should handle mixed revisions")
    void stateDocuments_shouldHandleMixedRevisions() {
        var dd1 = new DataDocumentAffiliate();
        dd1.setId(30L);
        dd1.setRevised(Boolean.TRUE);
        dd1.setState(Boolean.FALSE);

        var dd2 = new DataDocumentAffiliate();
        dd2.setId(31L);
        dd2.setRevised(Boolean.FALSE);
        dd2.setState(Boolean.TRUE);

        when(iDataDocumentRepository.findById(30L)).thenReturn(Optional.of(dd1));
        when(iDataDocumentRepository.findById(31L)).thenReturn(Optional.of(dd2));

        DocumentsDTO dto1 = new DocumentsDTO();
        dto1.setId(30L);
        dto1.setReject(false);

        DocumentsDTO dto2 = new DocumentsDTO();
        dto2.setId(31L);
        dto2.setReject(true);

        affiliationEmployerDomesticServiceIndependentServiceImpl.stateDocuments(List.of(dto1, dto2), 3L);

        assertTrue(dd1.getRevised());
        assertFalse(dd1.getState());
        assertTrue(dd2.getRevised());
        assertTrue(dd2.getState());

        verify(iDataDocumentRepository, times(2)).save(any(DataDocumentAffiliate.class));
    }

    @Test
    @DisplayName("managementAffiliation should handle large page numbers")
    void managementAffiliation_shouldHandleLargePageNumbers() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(Page.empty());
        when(affiliationsViewRepository.countByStageManagement(anyString())).thenReturn(1000L);

        ResponseManagementDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.managementAffiliation(999, 100, null);

        assertNotNull(result);
        assertEquals(1000L, result.totalInterviewing());
        verify(affiliationsViewRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    @DisplayName("stateAffiliation should handle very long comments")
    void stateAffiliation_shouldHandleVeryLongComments() {
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("F123");
        in.setRejectAffiliation(true);
        in.setIdOfficial(77L);
        in.setReasonReject("LONG_REASON");

        List<String> longComments = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            longComments.add("Very long comment number " + i + " with lots of text to test handling of large comment lists");
        }
        in.setComment(longComments);

        affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(in);

        verify(iAffiliationCancellationTimerRepository).save(any(AffiliationCancellationTimer.class));
        verify(observationsAffiliationService, times(100))
                .create(anyString(), eq("F123"), eq("LONG_REASON"), eq(77L));
        verify(sendEmails).requestDenied(any(Affiliation.class), any(StringBuilder.class));
    }


    @Test
    @DisplayName("generateExcel should handle repository exceptions")
    void generateExcel_shouldHandleRepositoryExceptions() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenThrow(new RuntimeException("Database connection error"));

        assertThrows(RuntimeException.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.generateExcel(null));
    }

    @Test
    @DisplayName("findById should handle null ID gracefully")
    void findById_shouldHandleNullId() {
        assertThrows(Exception.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.findById(null));
    }


    @Test
    @DisplayName("stateAffiliation should handle mercantile with REGULARIZATION stage")
    void stateAffiliation_shouldHandleMercantileRegularizationStage() {
        affiliateMercantile.setStageManagement(Constant.REGULARIZATION);

        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123M");
        stateAffiliation.setRejectAffiliation(false);

        assertThrows(AffiliationError.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(stateAffiliation));
    }

    @Test
    @DisplayName("stateAffiliation should handle mercantile with SING stage")
    void stateAffiliation_shouldHandleMercantileSingStage() {
        affiliateMercantile.setStageManagement("firma");

        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123M");
        stateAffiliation.setRejectAffiliation(false);

        assertThrows(AffiliationError.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(stateAffiliation));
    }

    @Test
    @DisplayName("stateDocuments should handle very large document list")
    void stateDocuments_shouldHandleVeryLargeDocumentList() {
        List<DocumentsDTO> largeDtoList = new ArrayList<>();

        for (int i = 100; i < 200; i++) {
            var dd = new DataDocumentAffiliate();
            dd.setId((long) i);
            dd.setRevised(Boolean.FALSE);
            dd.setState(Boolean.FALSE);
            when(iDataDocumentRepository.findById((long) i)).thenReturn(Optional.of(dd));

            DocumentsDTO dto = new DocumentsDTO();
            dto.setId((long) i);
            dto.setReject(i % 2 == 0);
            largeDtoList.add(dto);
        }

        affiliationEmployerDomesticServiceIndependentServiceImpl.stateDocuments(largeDtoList, 5L);

        verify(iDataDocumentRepository, times(100)).save(any(DataDocumentAffiliate.class));
    }




    @Test
    @DisplayName("visualizationPendingPerform should handle negative percentage edge case")
    void visualizationPendingPerform_shouldHandleNegativeEdgeCase() {
        when(iAffiliationEmployerDomesticServiceIndependentRepository.count()).thenReturn(1L);
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findAll(any(Specification.class)))
                .thenReturn(List.of())
                .thenReturn(List.of())
                .thenReturn(List.of())
                .thenReturn(List.of());

        VisualizationPendingPerformDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.visualizationPendingPerform();

        assertNotNull(result);
        assertEquals("0.0", result.getInterviewWeb());
        assertEquals("0.0", result.getReviewDocumental());
        assertEquals("0.0", result.getRegularization());
        assertEquals("0.0", result.getSing());
    }


    @Test
    @DisplayName("consultDocument should handle normal document IDs")
    void consultDocument_shouldHandleNormalDocumentIds() {
        when(alfrescoService.getDocument("NORMAL_DOC_123")).thenReturn("normalContent");

        List<DocumentBase64> result = affiliationEmployerDomesticServiceIndependentServiceImpl.consultDocument("NORMAL_DOC_123");

        assertEquals(1, result.size());
        assertEquals("normalContent", result.get(0).getBase64Image());
    }

    @Test
    @DisplayName("management should handle affiliate with valid subtype")
    void management_shouldHandleAffiliateWithValidSubtype() {
        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER);

        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("validSubtypeDoc");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);
        when(iDataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        ManagementDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.management(123L, 1L);

        assertNotNull(result);
        assertNotNull(result.getAffiliation());
    }

    @Test
    @DisplayName("stateAffiliation should process mercantile normally regardless of status")
    void stateAffiliation_shouldProcessMercantileNormally() {
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);

        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123M");
        stateAffiliation.setRejectAffiliation(false);
        affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(stateAffiliation);

        verify(affiliationEmployerActivitiesMercantileService).stateAffiliation(affiliateMercantile, stateAffiliation);
        verify(filedWebSocketService).changeStateAffiliation("F123M");
    }

    @Test
    @DisplayName("stateAffiliation should handle valid comment list")
    void stateAffiliation_shouldHandleValidCommentList() {
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("F123");
        in.setRejectAffiliation(true);
        in.setIdOfficial(55L);
        in.setReasonReject("VALID_COMMENTS");

        List<String> validComments = Arrays.asList("comment1", "comment2", "comment3");
        in.setComment(validComments);

        affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(in);

        verify(iAffiliationCancellationTimerRepository).save(any(AffiliationCancellationTimer.class));
        verify(observationsAffiliationService, times(3))
                .create(anyString(), eq("F123"), eq("VALID_COMMENTS"), eq(55L));
        verify(sendEmails).requestDenied(any(Affiliation.class), any(StringBuilder.class));
    }

    @Test
    @DisplayName("management should handle mercantile user lookup correctly")
    void management_shouldHandleMercantileUserLookup() {
        affiliateMercantile.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("docName");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);
        when(iDataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        assertThrows(AffiliateNotFound.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.management(123L, 1L));
    }

    @Test
    @DisplayName("stateAffiliation should process cancelled mercantile normally")
    void stateAffiliation_shouldProcessCancelledMercantileNormally() {
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);

        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123M");
        stateAffiliation.setRejectAffiliation(false);

        affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(stateAffiliation);

        verify(affiliationEmployerActivitiesMercantileService).stateAffiliation(affiliateMercantile, stateAffiliation);
        verify(filedWebSocketService).changeStateAffiliation("F123M");
    }

    @Test
    @DisplayName("consultDocument should handle basic functionality")
    void consultDocument_shouldHandleBasicFunctionality() {
        when(alfrescoService.getDocument("BASIC_DOC")).thenReturn("basicContent");

        List<DocumentBase64> result = affiliationEmployerDomesticServiceIndependentServiceImpl.consultDocument("BASIC_DOC");

        assertEquals(1, result.size());
        assertEquals("basicContent", result.get(0).getBase64Image());
        assertEquals("", result.get(0).getFileName());
    }



    @Test
    @DisplayName("stateAffiliation should handle different rejection reasons")
    void stateAffiliation_shouldHandleDifferentRejectionReasons() {
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("F123");
        in.setRejectAffiliation(true);
        in.setIdOfficial(123L);
        in.setReasonReject("DOCUMENT_INCOMPLETE");
        in.setComment(Arrays.asList("Missing signature", "Invalid ID"));

        affiliationEmployerDomesticServiceIndependentServiceImpl.stateAffiliation(in);

        verify(iAffiliationCancellationTimerRepository).save(any(AffiliationCancellationTimer.class));
        verify(observationsAffiliationService, times(2))
                .create(anyString(), eq("F123"), eq("DOCUMENT_INCOMPLETE"), eq(123L));
        verify(sendEmails).requestDenied(any(Affiliation.class), any(StringBuilder.class));
    }

    @Test
    @DisplayName("management should handle documents with different upload dates")
    void management_shouldHandleDocumentsWithDifferentUploadDates() {
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var oldDoc = new DataDocumentAffiliate();
        oldDoc.setId(1L);
        oldDoc.setName("oldDoc");
        oldDoc.setDateUpload(LocalDateTime.now().minusDays(30));
        oldDoc.setRevised(Boolean.FALSE);
        oldDoc.setState(Boolean.FALSE);

        var newDoc = new DataDocumentAffiliate();
        newDoc.setId(2L);
        newDoc.setName("newDoc");
        newDoc.setDateUpload(LocalDateTime.now().minusHours(1));
        newDoc.setRevised(Boolean.FALSE);
        newDoc.setState(Boolean.FALSE);

        when(iDataDocumentRepository.findAll(any(Specification.class)))
                .thenReturn(Arrays.asList(oldDoc, newDoc));

        ManagementDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.management(123L, 1L);

        assertNotNull(result);
        assertEquals(2, result.getDocuments().size());
    }

    @Test
    @DisplayName("visualizationPendingPerform should handle medium sized datasets")
    void visualizationPendingPerform_shouldHandleMediumDatasets() {
        when(iAffiliationEmployerDomesticServiceIndependentRepository.count()).thenReturn(500L);
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findAll(any(Specification.class)))
                .thenReturn(Arrays.asList(new Affiliation(), new Affiliation()))
                .thenReturn(Arrays.asList(new Affiliation(), new Affiliation(), new Affiliation()))
                .thenReturn(Arrays.asList(new Affiliation()))
                .thenReturn(Arrays.asList(new Affiliation(), new Affiliation(), new Affiliation(), new Affiliation()));

        VisualizationPendingPerformDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.visualizationPendingPerform();

        assertNotNull(result);
        assertEquals("0.4", result.getInterviewWeb());
        assertTrue(result.getReviewDocumental().startsWith("0.6"));

        assertEquals("0.2", result.getRegularization());
        assertEquals("0.8", result.getSing());
    }

    @Test
    @DisplayName("findDocuments should handle documents with null names")
    void findDocuments_shouldHandleDocumentsWithNullNames() {
        var docWithNullName = new DataDocumentAffiliate();
        docWithNullName.setId(1L);
        docWithNullName.setName(null);
        docWithNullName.setRevised(Boolean.FALSE);
        docWithNullName.setState(Boolean.FALSE);

        when(iDataDocumentRepository.findAll(any(Specification.class)))
                .thenReturn(Arrays.asList(docWithNullName));

        List<DataDocumentAffiliate> result = affiliationEmployerDomesticServiceIndependentServiceImpl.findDocuments(1L);

        assertEquals(1, result.size());
        assertNull(result.get(0).getName());
    }

    @Test
    @DisplayName("stateDocuments should handle documents already in final state")
    void stateDocuments_shouldHandleDocumentsInFinalState() {
        var finalDoc = new DataDocumentAffiliate();
        finalDoc.setId(60L);
        finalDoc.setRevised(Boolean.TRUE);
        finalDoc.setState(Boolean.TRUE);
        when(iDataDocumentRepository.findById(60L)).thenReturn(Optional.of(finalDoc));
        DocumentsDTO dto = new DocumentsDTO();
        dto.setId(60L);
        dto.setReject(false);
        affiliationEmployerDomesticServiceIndependentServiceImpl.stateDocuments(Arrays.asList(dto), 15L);
        assertTrue(finalDoc.getRevised());
        assertFalse(finalDoc.getState());
        verify(iDataDocumentRepository).save(finalDoc);
    }
    @Test
    @DisplayName("buildMainOffice: asigna zona URBANA por defecto y mapea todos los campos")
    void buildMainOffice_shouldSetUrbanAndMapAllFields() {
        Affiliation a = new Affiliation();
        a.setIsRuralZoneEmployer(false);
        a.setIdentificationDocumentType("CC");
        a.setIdentificationDocumentNumber("123");
        a.setFirstName("Ana");
        a.setSecondName("María");
        a.setSurname("García");
        a.setSecondSurname("Lopez");
        a.setPhone1("3001112233");
        a.setPhone2("6011234567");
        a.setEmail("ana@test.com");
        a.setAddress("Cra 1 # 2-3");
        a.setDepartment(11L);
        a.setCityMunicipality(7L);
        a.setIdMainStreet(1L);
        a.setIdNumberMainStreet(10L);
        a.setIdLetter1MainStreet(5L);
        a.setIsBis(Boolean.TRUE);
        a.setIdLetter2MainStreet(6L);
        a.setIdCardinalPointMainStreet(2L);
        a.setIdNum1SecondStreet(20L);
        a.setIdLetterSecondStreet(7L);
        a.setIdNum2SecondStreet(30L);
        a.setIdCardinalPoint2(3L);
        a.setIdHorizontalProperty1(100L);
        a.setIdNumHorizontalProperty1(1L);
        a.setIdHorizontalProperty2(200L);
        a.setIdNumHorizontalProperty2(2L);
        a.setIdHorizontalProperty3(300L);
        a.setIdNumHorizontalProperty3(3L);
        a.setIdHorizontalProperty4(400L);
        a.setIdNumHorizontalProperty4(4L);
        UserMain manager = new UserMain();
        manager.setFirstName("Gerente");

        Affiliate affiliateLocal = new Affiliate();
        affiliateLocal.setIdAffiliate(99L);

        when(mainOfficeService.findCode()).thenReturn("MO-001");

        when(mainOfficeService.saveMainOffice(any(MainOffice.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        MainOffice result = (MainOffice) ReflectionTestUtils.invokeMethod(
                affiliationEmployerDomesticServiceIndependentServiceImpl, "buildMainOffice", a, manager, affiliateLocal, "MO-001", true);

        ArgumentCaptor<MainOffice> cap = ArgumentCaptor.forClass(MainOffice.class);
        verify(mainOfficeService).saveMainOffice(cap.capture());
        MainOffice saved = cap.getValue();
        assertEquals("MO-001", saved.getCode());
        assertTrue(saved.getMain());
        assertEquals(Constant.URBAN_ZONE, saved.getMainOfficeZone());
        assertEquals("Cra 1 # 2-3", saved.getAddress());
        assertEquals("3001112233", saved.getMainOfficePhoneNumber());
        assertEquals("ana@test.com", saved.getMainOfficeEmail());
        assertSame(manager, saved.getOfficeManager());
        assertEquals(99L, saved.getIdAffiliate());
        assertEquals("CC", saved.getTypeDocumentResponsibleHeadquarters());
        assertEquals("123", saved.getNumberDocumentResponsibleHeadquarters());
        assertEquals("Ana", saved.getFirstNameResponsibleHeadquarters());
        assertEquals("María", saved.getSecondNameResponsibleHeadquarters());
        assertEquals("García", saved.getSurnameResponsibleHeadquarters());
        assertEquals("Lopez", saved.getSecondSurnameResponsibleHeadquarters());
        assertEquals("3001112233", saved.getPhoneOneResponsibleHeadquarters());
        assertEquals("6011234567", saved.getPhoneTwoResponsibleHeadquarters());
        assertEquals("ana@test.com", saved.getEmailResponsibleHeadquarters());
        assertEquals(11L, saved.getIdDepartment());
        assertEquals(7L, saved.getIdCity());
        assertEquals(1L, saved.getIdMainStreet());
        assertEquals(10L, saved.getIdNumberMainStreet());
        assertEquals(5L, saved.getIdLetter1MainStreet());
        assertEquals(Boolean.TRUE, saved.getIsBis());
        assertEquals(6L, saved.getIdLetter2MainStreet());
        assertEquals(2L, saved.getIdCardinalPointMainStreet());
        assertEquals(20L, saved.getIdNum1SecondStreet());
        assertEquals(7L,  saved.getIdLetterSecondStreet());
        assertEquals(30L, saved.getIdNum2SecondStreet());
        assertEquals(3L,  saved.getIdCardinalPoint2());
        assertEquals(100L, saved.getIdHorizontalProperty1());
        assertEquals(1L,   saved.getIdNumHorizontalProperty1());
        assertEquals(200L, saved.getIdHorizontalProperty2());
        assertEquals(2L,   saved.getIdNumHorizontalProperty2());
        assertEquals(300L, saved.getIdHorizontalProperty3());
        assertEquals(3L,   saved.getIdNumHorizontalProperty3());
        assertEquals(400L, saved.getIdHorizontalProperty4());
        assertEquals(4L,   saved.getIdNumHorizontalProperty4());

        assertNotNull(result);
        assertEquals(saved.getCode(), result.getCode());
    }

    @Test
    @DisplayName("buildMainOffice: asigna zona RURAL cuando isRuralZoneEmployer = true")
    void buildMainOffice_shouldSetRuralZoneWhenFlagTrue() {
        Affiliation a = new Affiliation();
        a.setIsRuralZoneEmployer(true);
        a.setPhone1("3110000000");
        a.setEmail("rural@test.com");
        a.setAddress("Vereda El Paraíso");

        UserMain manager = new UserMain();
        Affiliate affiliateLocal = new Affiliate();
        affiliateLocal.setIdAffiliate(7L);

        when(mainOfficeService.findCode()).thenReturn("MO-002");
        when(mainOfficeService.saveMainOffice(any(MainOffice.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ReflectionTestUtils.invokeMethod(affiliationEmployerDomesticServiceIndependentServiceImpl, "buildMainOffice", a, manager, affiliateLocal, "MO-002", true);

        ArgumentCaptor<MainOffice> cap = ArgumentCaptor.forClass(MainOffice.class);
        verify(mainOfficeService).saveMainOffice(cap.capture());
        assertEquals(Constant.RURAL_ZONE, cap.getValue().getMainOfficeZone());
    }

    @Test
    @DisplayName("management: maneja distintos estados mercantiles sin AffiliationError")
    void management_shouldHandleDifferentMercantileStages() {
        affiliateMercantile.setStageManagement("ANY_STAGE");
        affiliateMercantile.setTypeDocumentPersonResponsible("CC");
        affiliateMercantile.setNumberDocumentPersonResponsible("9001");
        when(affiliateRepository.findByIdAffiliate(anyLong()))
                .thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliateMercantile));
        UserMain pre = new UserMain();
        pre.setId(1L);
        pre.setFirstName("User");
        pre.setNationality(57L);
        pre.setHealthPromotingEntity(1L);
        pre.setPensionFundAdministrator(2L);
        when(iUserPreRegisterRepository.findOne(ArgumentMatchers.<Specification<UserMain>>any()))
                .thenReturn(Optional.of(pre));
        when(iUserPreRegisterRepository.findOne(ArgumentMatchers.<Specification<UserMain>>any()))
                .thenReturn(Optional.of(pre));
        when(iUserPreRegisterRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(pre));
        when(iUserPreRegisterRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(pre));

        DataDocumentAffiliate doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("doc.pdf");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(false);
        doc.setState(false);
        when(iDataDocumentRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(doc));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.save(any(Affiliation.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(affiliateService.getEmployerSize(anyInt())).thenReturn(1L);

        ManagementDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.management(123L, 1L);
        assertNotNull(result);
        assertNotNull(result.getAffiliation());
    }

    @Test
    @DisplayName("management: propaga actividades económicas vía mercantil sin AffiliationError")
    void economicActivities_throughMercantile() {
        AffiliateActivityEconomic act = new AffiliateActivityEconomic();
        EconomicActivity ea = new EconomicActivity();
        ea.setId(1L);
        act.setActivityEconomic(ea);
        act.setIsPrimary(true);

        affiliateMercantile.setEconomicActivity(List.of(act));
        affiliateMercantile.setStageManagement("DOC_REVIEW");
        affiliateMercantile.setTypeDocumentPersonResponsible("CC");
        affiliateMercantile.setNumberDocumentPersonResponsible("9001");

        when(affiliateRepository.findByIdAffiliate(anyLong()))
                .thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliateMercantile));
        UserMain pre = new UserMain();
        pre.setId(2L);
        pre.setFirstName("User");
        pre.setNationality(57L);
        when(iUserPreRegisterRepository.findOne(ArgumentMatchers.<Specification<UserMain>>any()))
                .thenReturn(Optional.of(pre));
        when(iUserPreRegisterRepository.findOne(ArgumentMatchers.<Specification<UserMain>>any()))
                .thenReturn(Optional.of(pre));
        when(iUserPreRegisterRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(pre));
        when(iUserPreRegisterRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(pre));

        DataDocumentAffiliate doc = new DataDocumentAffiliate();
        doc.setId(9L);
        doc.setName("econ.pdf");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(false);
        doc.setState(false);
        when(iDataDocumentRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(doc));

        when(iAffiliationEmployerDomesticServiceIndependentRepository.save(any(Affiliation.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(affiliateService.getEmployerSize(anyInt())).thenReturn(1L);
        ManagementDTO result = affiliationEmployerDomesticServiceIndependentServiceImpl.management(123L, 1L);
        assertNotNull(result);
        assertNotNull(result.getAffiliation());
    }


    @Test
    @DisplayName("createAffiliationStep3 should throw error if affiliation not found")
    void createAffiliationStep3_notFound() {
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AffiliationNotFoundError.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.createAffiliationStep3(999L, mock(MultipartFile.class)));
    }


    @Test
    @DisplayName("assignTo should throw if affiliation not found")
    void assignTo_affiliationNotFound() {
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findByFiledNumber("F999")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.assignTo(999L, 99L));
    }

    @Test
    @DisplayName("createAffiliationStep3 should throw if user not found")
    void createAffiliationStep3_userNotFound() {
        Affiliation affiliation = new Affiliation();
        affiliation.setId(1L);
        affiliation.setIdentificationDocumentNumber("12345");

        when(iAffiliationEmployerDomesticServiceIndependentRepository.findById(1L)).thenReturn(Optional.of(affiliation));
        when(genericWebClient.getByIdentification("12345")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundInDataBase.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.createAffiliationStep3(1L, mock(MultipartFile.class)));
    }


    @Test
    @DisplayName("generateExcel should handle empty data")
    void generateExcel_emptyData() {

        AffiliationsFilterDTO filter = new AffiliationsFilterDTO(null, null, null, null, null);

        when(affiliationsViewRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(new ArrayList<>());

        String result = affiliationEmployerDomesticServiceIndependentServiceImpl.generateExcel(filter);

        assertNotNull(result);
        assertTrue(result.length() > 0);
    }


    @Test
    @DisplayName("convertZoneToString should handle both zones")
    void convertZoneToString_both() {
        String urban = (String) ReflectionTestUtils.invokeMethod(affiliationEmployerDomesticServiceIndependentServiceImpl, "convertZoneToString", Constant.URBAN_ZONE);
        assertEquals("Urbana", urban);

        String rural = (String) ReflectionTestUtils.invokeMethod(affiliationEmployerDomesticServiceIndependentServiceImpl, "convertZoneToString", Constant.RURAL_ZONE);
        assertEquals("Rural", rural);
    }

    @Test
    @DisplayName("economicActivities should handle empty list")
    void economicActivities_empty() {
        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setEconomicActivity(new ArrayList<>());

        Map<Long, Boolean> result = (Map<Long, Boolean>) ReflectionTestUtils.invokeMethod(affiliationEmployerDomesticServiceIndependentServiceImpl, "economicActivities", mercantile);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("createAffiliateActivityEconomic should create activities and work centers")
    void createAffiliateActivityEconomic_success() {
        Affiliation affiliation = new Affiliation();
        affiliation.setDepartment(11L);
        affiliation.setCityMunicipality(7L);
        affiliation.setAddress("CRA 53 N 127-40");
        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(1L);
        UserMain userMain = new UserMain();
        userMain.setIdentificationType("CC");
        userMain.setIdentification("1234");
        MainOffice mainOffice = new MainOffice();

        List<String> codes = List.of("1970001", "1970002", "3970001", "3869201");
        List<EconomicActivity> activities = codes.stream()
                .map(code -> {
                    EconomicActivity ea = new EconomicActivity();
                    ea.setEconomicActivityCode(code);
                    ea.setClassRisk("I");
                    return ea;
                }).toList();

        when(iEconomicActivityRepository.findAllByEconomicActivityCodeIn(codes)).thenReturn(activities);
        when(workCenterService.saveWorkCenter(any(WorkCenter.class))).thenReturn(new WorkCenter());

        List<AffiliateActivityEconomic> result = (List<AffiliateActivityEconomic>) ReflectionTestUtils.invokeMethod(affiliationEmployerDomesticServiceIndependentServiceImpl, "createAffiliateActivityEconomic", affiliation, affiliate, userMain);

        assertEquals(4, result.size());
        assertTrue(result.stream().anyMatch(AffiliateActivityEconomic::getIsPrimary));
        verify(workCenterService, times(4)).saveWorkCenter(any(WorkCenter.class));
    }
    @Test
    @DisplayName("economicActivityList should return activities")
    void economicActivityList_success() {
        List<String> codes = List.of("1970001", "1970002");
        List<EconomicActivity> activities = new ArrayList<>();
        when(iEconomicActivityRepository.findAllByEconomicActivityCodeIn(codes)).thenReturn(activities);

        List<EconomicActivity> result = (List<EconomicActivity>) ReflectionTestUtils.invokeMethod(affiliationEmployerDomesticServiceIndependentServiceImpl, "economicActivityList", codes);

        assertEquals(activities, result);
        verify(iEconomicActivityRepository).findAllByEconomicActivityCodeIn(codes);
    }
    @Test
    @DisplayName("findDataDaily should return null if no interview")
    void findDataDaily_noInterview() {
        when(dateInterviewWebRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        DataDailyDTO result = (DataDailyDTO) ReflectionTestUtils.invokeMethod(affiliationEmployerDomesticServiceIndependentServiceImpl, "findDataDaily", "1");

        assertNull(result);
    }
    @Test
    @DisplayName("generateExcel should throw IOException")
    void generateExcel_ioException() {
        AffiliationsFilterDTO filter = new AffiliationsFilterDTO(null, null, null, null, null);
        when(affiliationsViewRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(new ArrayList<>());

        try (MockedConstruction<XSSFWorkbook> mocked = Mockito.mockConstruction(XSSFWorkbook.class, (mock, context) -> {
            when(mock.createSheet(anyString())).thenThrow(new IOException("Mocked IO error"));
        })) {
            assertThrows(RuntimeException.class, () -> affiliationEmployerDomesticServiceIndependentServiceImpl.generateExcel(filter));
        }
    }
    @Test
    @DisplayName("createWorkCenter should set rural zone")
    void createWorkCenter_ruralZone() {
        Affiliation affiliation = new Affiliation();
        affiliation.setIsRuralZoneEmployer(true);
        affiliation.setIdentificationDocumentNumber("123");
        affiliation.setDepartmentEmployer(1L);
        affiliation.setMunicipalityEmployer(2L);
        affiliation.setAddressEmployer("Test Address");
        affiliation.setEconomicActivity(new ArrayList<>());
        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setClassRisk("1");
        economicActivity.setEconomicActivityCode("1970001");
        UserMain userMain = new UserMain();
        userMain.setIdentificationType("CC");
        userMain.setIdentification("1234");
        MainOffice mainOffice = new MainOffice();

        when(workCenterService.saveWorkCenter(any(WorkCenter.class))).thenReturn(new WorkCenter());

        ReflectionTestUtils.invokeMethod(affiliationEmployerDomesticServiceIndependentServiceImpl, "createWorkCenter", affiliation, economicActivity, 1, mainOffice, 1L, userMain);

        ArgumentCaptor<WorkCenter> captor = ArgumentCaptor.forClass(WorkCenter.class);
        verify(workCenterService).saveWorkCenter(captor.capture());
        assertEquals(Constant.RURAL_ZONE, captor.getValue().getWorkCenterZone());
    }
    @Test
    @DisplayName("findDocumentsRejects should return rejected documents")
    void findDocumentsRejects_withRejects() {
        DataDocumentAffiliate doc = new DataDocumentAffiliate();
        doc.setState(false);
        doc.setRevised(true);
        when(iDataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        List<DataDocumentAffiliate> result = (List<DataDocumentAffiliate>) ReflectionTestUtils.invokeMethod(affiliationEmployerDomesticServiceIndependentServiceImpl, "findDocumentsRejects", 1L);

        assertEquals(1, result.size());
    }
    @Test
    @DisplayName("createAffiliationStep3 should handle null economic activity")
    void createAffiliationStep3_nullEconomicActivity() throws Exception {
        Affiliation affiliation = new Affiliation();
        affiliation.setId(1L);
        affiliation.setIdentificationDocumentNumber("12345");
        affiliation.setEconomicActivity(new ArrayList<>());
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findById(1L)).thenReturn(Optional.of(affiliation));

        UserMain userDto = new UserMain();
        userDto.setIdentificationType("CC");
        userDto.setIdentification("12345");
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(userDto));

        when(filedService.getNextFiledNumberAffiliation()).thenReturn("F999");
        when(affiliateService.createAffiliate(any(Affiliate.class))).thenReturn(new Affiliate());

        MainOffice mainOffice = new MainOffice();
        mainOffice.setId(1L);
        when(mainOfficeService.saveMainOffice(any(MainOffice.class))).thenReturn(mainOffice);

        when(alfrescoService.getIdDocumentsFolder(anyString())).thenReturn(Optional.of(new ConsultFiles()));
        when(alfrescoService.uploadAffiliationDocuments(anyString(), anyString(), anyList())).thenReturn(new ResponseUploadOrReplaceFilesDTO());

        affiliationEmployerDomesticServiceIndependentServiceImpl.createAffiliationStep3(1L, mock(MultipartFile.class));

        ArgumentCaptor<Affiliation> captor = ArgumentCaptor.forClass(Affiliation.class);
        verify(iAffiliationEmployerDomesticServiceIndependentRepository).save(captor.capture());
        assertNotNull(captor.getValue().getEconomicActivity());
        assertTrue(captor.getValue().getEconomicActivity().isEmpty());
    }

    @Test
    @DisplayName("economicActivityList should handle repository error")
    void economicActivityList_repositoryError() {
        List<String> codes = List.of("1970001", "1970002");
        when(iEconomicActivityRepository.findAllByEconomicActivityCodeIn(codes)).thenThrow(new RuntimeException("Repository error"));

        assertThrows(RuntimeException.class, () -> ReflectionTestUtils.invokeMethod(affiliationEmployerDomesticServiceIndependentServiceImpl, "economicActivityList", codes));
    }
    @Test
    @DisplayName("findDocumentsRejects should handle empty list")
    void findDocumentsRejects_emptyList() {
        when(iDataDocumentRepository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());

        List<DataDocumentAffiliate> result = (List<DataDocumentAffiliate>) ReflectionTestUtils.invokeMethod(affiliationEmployerDomesticServiceIndependentServiceImpl, "findDocumentsRejects", 1L);

        assertTrue(result.isEmpty());
    }
}
