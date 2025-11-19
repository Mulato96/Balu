package com.gal.afiliaciones.application.service.affiliate.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

import com.gal.afiliaciones.application.service.notification.RegistryConnectInterviewWebService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.domain.model.Operator;
import com.gal.afiliaciones.domain.model.RegistryConnectInterviewWeb;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DataStatusAffiliationDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {FiledWebSocketServiceImpl.class})
@DisabledInAotMode
@ExtendWith(SpringExtension.class)
class FiledWebSocketServiceImplTest {
    @MockBean
    private AffiliateMercantileRepository affiliateMercantileRepository;

    @MockBean
    private AffiliateRepository affiliateRepository;

    @Autowired
    private FiledWebSocketServiceImpl filedWebSocketServiceImpl;

    @MockBean
    private IAffiliationEmployerDomesticServiceIndependentRepository
            iAffiliationEmployerDomesticServiceIndependentRepository;

    @MockBean
    private IUserPreRegisterRepository iUserPreRegisterRepository;

    @MockBean
    private RegistryConnectInterviewWebService registryConnectInterviewWebService;

    @MockBean
    private SimpMessagingTemplate simpMessagingTemplate;


    @BeforeEach
    void setUp() {
        simpMessagingTemplate = mock(SimpMessagingTemplate.class);
        affiliateRepository = mock(AffiliateRepository.class);
        affiliateMercantileRepository = mock(AffiliateMercantileRepository.class);
        iUserPreRegisterRepository = mock(IUserPreRegisterRepository.class);
        affiliateMercantileRepository = mock(AffiliateMercantileRepository.class);
        registryConnectInterviewWebService = mock(RegistryConnectInterviewWebService.class);
        iAffiliationEmployerDomesticServiceIndependentRepository = mock(IAffiliationEmployerDomesticServiceIndependentRepository.class);

        filedWebSocketServiceImpl = new FiledWebSocketServiceImpl(simpMessagingTemplate, affiliateRepository, affiliateMercantileRepository,
                iUserPreRegisterRepository, affiliateMercantileRepository, registryConnectInterviewWebService, iAffiliationEmployerDomesticServiceIndependentRepository);
    }

    @Test
    void changeStateAffiliation_shouldSendNotificationWithAffiliation() {
        String filedNumber = "file123";

        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber(filedNumber);
        affiliate.setDateAffiliateSuspend(LocalDateTime.now());

        Affiliation affiliation = new Affiliation();
        affiliation.setStageManagement("stage");
        affiliation.setFiledNumber(filedNumber);

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.empty());

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);

        filedWebSocketServiceImpl.changeStateAffiliation(filedNumber);

        verify(simpMessagingTemplate).convertAndSend(eq("/filed/" + filedNumber), captor.capture());

        Map<String, Object> sentData = captor.getValue();
        assertEquals("official", sentData.get("type"));
        assertEquals("changeAffiliation", sentData.get("typeNotification"));
        assertTrue(sentData.containsKey("dataStatusAffiliation"));
        assertTrue(sentData.get("dataStatusAffiliation") instanceof DataStatusAffiliationDTO);
    }

    @Test
    void changeStateAffiliation_shouldSendNotificationWithMercantileWhenAffiliationEmpty() {
        String filedNumber = "file123";

        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber(filedNumber);
        affiliate.setDateAffiliateSuspend(LocalDateTime.now());

        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setFiledNumber(filedNumber);

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.of(mercantile));

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);

        filedWebSocketServiceImpl.changeStateAffiliation(filedNumber);

        verify(simpMessagingTemplate).convertAndSend(eq("/filed/" + filedNumber), captor.capture());

        Map<String, Object> sentData = captor.getValue();
        assertEquals("official", sentData.get("type"));
        assertEquals("changeAffiliation", sentData.get("typeNotification"));
        assertTrue(sentData.containsKey("dataStatusAffiliation"));
        assertTrue(sentData.get("dataStatusAffiliation") instanceof DataStatusAffiliationDTO);
    }

    @Test
    void changeStateAffiliation_shouldNotSendNotificationWhenAffiliateNull() {
        String filedNumber = "file123";

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        filedWebSocketServiceImpl.changeStateAffiliation(filedNumber);

        verify(simpMessagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    void connectOfficial_shouldSaveRegistryAndSendNotificationWhenValidData() {
        String filedNumber = "file123";
        String id = "1";
        String state = "connected";

        Map<String, String> dataUser = new HashMap<>();
        dataUser.put("numberFiled", filedNumber);
        dataUser.put("id", id);

        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setFiledNumber(filedNumber);

        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mercantile));

        ArgumentCaptor<RegistryConnectInterviewWeb> registryCaptor = ArgumentCaptor.forClass(RegistryConnectInterviewWeb.class);
        ArgumentCaptor<Map<String, Object>> messageCaptor = ArgumentCaptor.forClass(Map.class);

        filedWebSocketServiceImpl.connectOfficial(dataUser, state);

        verify(registryConnectInterviewWebService).save(registryCaptor.capture());
        RegistryConnectInterviewWeb savedRegistry = registryCaptor.getValue();
        assertEquals(filedNumber, savedRegistry.getNumberFiled());
        assertEquals(Long.parseLong(id), savedRegistry.getIdUser());
        assertEquals(state, savedRegistry.getState());
        assertEquals("official", savedRegistry.getTypeUser());
        assertNotNull(savedRegistry.getDate());

        verify(simpMessagingTemplate).convertAndSend(eq("/filed/" + filedNumber), messageCaptor.capture());
        Map<String, Object> sentData = messageCaptor.getValue();
        assertEquals("official", sentData.get("type"));
        assertEquals(filedNumber, sentData.get("filedNumber"));
        assertEquals("officialConnected", sentData.get("typeNotification"));
    }

    @Test
    void connectOfficial_shouldThrowWhenDataUserIncomplete() {
        Map<String, String> dataUser = new HashMap<>();
        dataUser.put("numberFiled", "file123");
        // missing id

        AffiliationError ex = assertThrows(AffiliationError.class, () -> filedWebSocketServiceImpl.connectOfficial(dataUser, "connected"));
    }

    @Test
    void connectUser_shouldSaveRegistryAndSendNotificationWhenValidData() {
        String filedNumber = "file123";
        String id = "2";
        String state = "disconnected";

        Map<String, String> dataUser = new HashMap<>();
        dataUser.put("numberFiled", filedNumber);
        dataUser.put("id", id);

        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setFiledNumber(filedNumber);

        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mercantile));

        ArgumentCaptor<RegistryConnectInterviewWeb> registryCaptor = ArgumentCaptor.forClass(RegistryConnectInterviewWeb.class);
        ArgumentCaptor<Map<String, Object>> messageCaptor = ArgumentCaptor.forClass(Map.class);

        filedWebSocketServiceImpl.connectUser(dataUser, state);

        verify(registryConnectInterviewWebService).save(registryCaptor.capture());
        RegistryConnectInterviewWeb savedRegistry = registryCaptor.getValue();
        assertEquals(filedNumber, savedRegistry.getNumberFiled());
        assertEquals(Long.parseLong(id), savedRegistry.getIdUser());
        assertEquals(state, savedRegistry.getState());
        assertEquals("user", savedRegistry.getTypeUser());
        assertNotNull(savedRegistry.getDate());

        verify(simpMessagingTemplate).convertAndSend(eq("/filed/" + filedNumber), messageCaptor.capture());
        Map<String, Object> sentData = messageCaptor.getValue();
        assertEquals("user", sentData.get("type"));
        assertEquals(filedNumber, sentData.get("filedNumber"));
        assertEquals("userDisconnected", sentData.get("typeNotification"));
    }


    @Test
    void notificationByFiledNumber_shouldReturnDatesMap() {
        String filedNumber = "file123";

        RegistryConnectInterviewWeb r1 = new RegistryConnectInterviewWeb();
        r1.setDate(LocalDateTime.of(2023, 1, 1, 10, 0));
        r1.setTypeUser("user");
        r1.setState("connected");

        RegistryConnectInterviewWeb r2 = new RegistryConnectInterviewWeb();
        r2.setDate(LocalDateTime.of(2023, 1, 1, 11, 0));
        r2.setTypeUser("official");
        r2.setState("disconnected");

        RegistryConnectInterviewWeb r3 = new RegistryConnectInterviewWeb();
        r3.setDate(LocalDateTime.of(2023, 1, 1, 9, 0));
        r3.setTypeUser(null);
        r3.setState("connected");

        List<RegistryConnectInterviewWeb> list = Arrays.asList(r1, r2, r3);

        when(registryConnectInterviewWebService.findByFiledNumber(filedNumber)).thenReturn(list);

        Map<String, String> result = filedWebSocketServiceImpl.notificationByFiledNumber(filedNumber);

        assertNotNull(result);
        assertEquals(r3.getDate().toString(), result.get("connected"));
        assertEquals(r1.getDate().toString(), result.get("userconnected"));
        assertEquals(r2.getDate().toString(), result.get("officialdisconnected"));
        assertNull(result.get("userdisconnected"));
    }

    @Test
    void findDataStatusAffiliationDTO_shouldReturnDtoWhenBothFound() {
        String filedNumber = "file123";

        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setFiledNumber(filedNumber);
        mercantile.setStageManagement("stage1");

        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber(filedNumber);
        affiliate.setDateAffiliateSuspend(LocalDateTime.now());

        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mercantile));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        DataStatusAffiliationDTO dto = null;
        try {
            java.lang.reflect.Method method = FiledWebSocketServiceImpl.class.getDeclaredMethod("findDataStatusAffiliationDTO", String.class);
            method.setAccessible(true);
            dto = (DataStatusAffiliationDTO) method.invoke(filedWebSocketServiceImpl, filedNumber);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertNotNull(dto);
        assertEquals(filedNumber, dto.getFiledNumber());
        assertEquals(affiliate.getDateAffiliateSuspend(), dto.getDateAffiliateSuspend());
    }

    @Test
    void findDataStatusAffiliationDTO_shouldReturnNullWhenOneNotFound() {
        String filedNumber = "file123";

        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(new Affiliate()));

        DataStatusAffiliationDTO dto = null;
        try {
            java.lang.reflect.Method method = FiledWebSocketServiceImpl.class.getDeclaredMethod("findDataStatusAffiliationDTO", String.class);
            method.setAccessible(true);
            dto = (DataStatusAffiliationDTO) method.invoke(filedWebSocketServiceImpl, filedNumber);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertNull(dto);
    }

    /**
     * Test {@link FiledWebSocketServiceImpl#changeStateAffiliation(String)}.
     *
     * <p>Method under test: {@link FiledWebSocketServiceImpl#changeStateAffiliation(String)}
     */
    @Test
    @DisplayName("Test changeStateAffiliation(String)")
    
    void testChangeStateAffiliation() {
        // Arrange
        when(affiliateRepository.findOne(Mockito.<Specification<Affiliate>>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class, () -> filedWebSocketServiceImpl.changeStateAffiliation("42"));
        verify(affiliateRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link FiledWebSocketServiceImpl#changeStateAffiliation(String)}.
     *
     * <p>Method under test: {@link FiledWebSocketServiceImpl#changeStateAffiliation(String)}
     */
    @Test
    @DisplayName("Test changeStateAffiliation(String)")
    
    void testChangeStateAffiliation2() {
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
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findByFiledNumber(
                Mockito.<String>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class, () -> filedWebSocketServiceImpl.changeStateAffiliation("42"));
        verify(iAffiliationEmployerDomesticServiceIndependentRepository).findByFiledNumber("42");
        verify(affiliateRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link FiledWebSocketServiceImpl#changeStateAffiliation(String)}.
     *
     * <ul>
     *   <li>Then calls {@link AffiliateMercantileRepository#findByFiledNumber(String)}.
     * </ul>
     *
     * <p>Method under test: {@link FiledWebSocketServiceImpl#changeStateAffiliation(String)}
     */
    @Test
    @DisplayName("Test changeStateAffiliation(String); then calls findByFiledNumber(String)")
    
    void testChangeStateAffiliation_thenCallsFindByFiledNumber() {
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
        when(affiliateMercantileRepository.findByFiledNumber(Mockito.<String>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        Affiliation affiliation = new Affiliation();
        affiliation.setAddress("42 Main St");
        affiliation.setAddressContractDataStep2("42 Main St");
        affiliation.setAddressEmployer("42 Main St");
        affiliation.setAddressIndependentWorker("42 Main St");
        affiliation.setAddressWorkDataCenter("42 Main St");
        affiliation.setAge("Age");
        affiliation.setCityMunicipality(1L);
        affiliation.setCodeContributantSubtype("Code Contributant Subtype");
        affiliation.setCodeContributantType(1L);
        affiliation.setCodeContributorType("Code Contributor Type");
        affiliation.setCodeLegalNatureEmployer(1L);
        affiliation.setCodeMainEconomicActivity("Code Main Economic Activity");
        affiliation.setCompanyName("Company Name");
        affiliation.setContractDuration("Contract Duration");
        affiliation.setContractEndDate(LocalDate.of(1970, 1, 1));
        affiliation.setContractIbcValue(Constant.IBC_TOLERANCE);
        affiliation.setContractMonthlyValue(Constant.IBC_TOLERANCE);
        affiliation.setContractQuality("Contract Quality");
        affiliation.setContractStartDate(LocalDate.of(1970, 1, 1));
        affiliation.setContractTotalValue(Constant.IBC_TOLERANCE);
        affiliation.setContractType("Contract Type");
        affiliation.setCurrentARL("Current ARL");
        affiliation.setDateOfBirth(LocalDate.of(1970, 1, 1));
        affiliation.setDateOfBirthIndependentWorker(LocalDate.of(1970, 1, 1));
        affiliation.setDateRegularization(LocalDate.of(1970, 1, 1).atStartOfDay());
        affiliation.setDateRequest("2020-03-01");
        affiliation.setDepartment(1L);
        affiliation.setDepartmentEmployer(1L);
        affiliation.setDuration("Duration");
        affiliation.setDv(1);
        affiliation.setEconomicActivity(new ArrayList<>());
        affiliation.setEmail("jane.doe@example.org");
        affiliation.setEmailContractor("jane.doe@example.org");
        affiliation.setEndDate(LocalDate.of(1970, 1, 1));
        affiliation.setFiledNumber("42");
        affiliation.setFirstName("Jane");
        affiliation.setFirstNameContractor("Jane");
        affiliation.setFirstNameIndependentWorker("Jane");
        affiliation.setFirstNameSignatory("Jane");
        affiliation.setGender("Gender");
        affiliation.setHealthPromotingEntity(1L);
        affiliation.setIbcPercentage(Constant.IBC_TOLERANCE);
        affiliation.setId(1L);
        affiliation.setIdAffiliate(1L);
        affiliation.setIdCardinalPoint2(1L);
        affiliation.setIdCardinalPoint2ContractDataStep2(1L);
        affiliation.setIdCardinalPoint2Employer(1L);
        affiliation.setIdCardinalPoint2IndependentWorker(1L);
        affiliation.setIdCardinalPoint2WorkDataCenter(1L);
        affiliation.setIdCardinalPointMainStreet(1L);
        affiliation.setIdCardinalPointMainStreetContractDataStep2(1L);
        affiliation.setIdCardinalPointMainStreetEmployer(1L);
        affiliation.setIdCardinalPointMainStreetIndependentWorker(1L);
        affiliation.setIdCardinalPointMainStreetWorkDataCenter(1L);
        affiliation.setIdCityContractDataStep2(1L);
        affiliation.setIdCityIndependentWorker(1L);
        affiliation.setIdCityWorkDataCenter(1L);
        affiliation.setIdDepartmentContractDataStep2(1L);
        affiliation.setIdDepartmentIndependentWorker(1L);
        affiliation.setIdDepartmentWorkDataCenter(1L);
        affiliation.setIdEmployerSize(1L);
        affiliation.setIdFamilyMember(1L);
        affiliation.setIdFolderAlfresco("Id Folder Alfresco");
        affiliation.setIdHorizontalProperty1(1L);
        affiliation.setIdHorizontalProperty1ContractDataStep2(1L);
        affiliation.setIdHorizontalProperty1Employer(1L);
        affiliation.setIdHorizontalProperty1IndependentWorker(1L);
        affiliation.setIdHorizontalProperty1WorkDataCenter(1L);
        affiliation.setIdHorizontalProperty2(1L);
        affiliation.setIdHorizontalProperty2ContractDataStep2(1L);
        affiliation.setIdHorizontalProperty2Employer(1L);
        affiliation.setIdHorizontalProperty2IndependentWorker(1L);
        affiliation.setIdHorizontalProperty2WorkDataCenter(1L);
        affiliation.setIdHorizontalProperty3(1L);
        affiliation.setIdHorizontalProperty3ContractDataStep2(1L);
        affiliation.setIdHorizontalProperty3Employer(1L);
        affiliation.setIdHorizontalProperty3IndependentWorker(1L);
        affiliation.setIdHorizontalProperty3WorkDataCenter(1L);
        affiliation.setIdHorizontalProperty4(1L);
        affiliation.setIdHorizontalProperty4ContractDataStep2(1L);
        affiliation.setIdHorizontalProperty4Employer(1L);
        affiliation.setIdHorizontalProperty4IndependentWorker(1L);
        affiliation.setIdHorizontalProperty4WorkDataCenter(1L);
        affiliation.setIdLetter1MainStreet(1L);
        affiliation.setIdLetter1MainStreetContractDataStep2(1L);
        affiliation.setIdLetter1MainStreetEmployer(1L);
        affiliation.setIdLetter1MainStreetIndependentWorker(1L);
        affiliation.setIdLetter1MainStreetWorkDataCenter(1L);
        affiliation.setIdLetter2MainStreet(1L);
        affiliation.setIdLetter2MainStreetContractDataStep2(1L);
        affiliation.setIdLetter2MainStreetEmployer(1L);
        affiliation.setIdLetter2MainStreetIndependentWorker(1L);
        affiliation.setIdLetter2MainStreetWorkDataCenter(1L);
        affiliation.setIdLetterSecondStreet(1L);
        affiliation.setIdLetterSecondStreetContractDataStep2(1L);
        affiliation.setIdLetterSecondStreetEmployer(1L);
        affiliation.setIdLetterSecondStreetIndependentWorker(1L);
        affiliation.setIdLetterSecondStreetWorkDataCenter(1L);
        affiliation.setIdMainHeadquarter(1L);
        affiliation.setIdMainStreet(1L);
        affiliation.setIdMainStreetContractDataStep2(1L);
        affiliation.setIdMainStreetEmployer(1L);
        affiliation.setIdMainStreetIndependentWorker(1L);
        affiliation.setIdMainStreetWorkDataCenter(1L);
        affiliation.setIdNum1SecondStreet(1L);
        affiliation.setIdNum1SecondStreetContractDataStep2(1L);
        affiliation.setIdNum1SecondStreetEmployer(1L);
        affiliation.setIdNum1SecondStreetIndependentWorker(1L);
        affiliation.setIdNum1SecondStreetWorkDataCenter(1L);
        affiliation.setIdNum2SecondStreet(1L);
        affiliation.setIdNum2SecondStreetContractDataStep2(1L);
        affiliation.setIdNum2SecondStreetEmployer(1L);
        affiliation.setIdNum2SecondStreetIndependentWorker(1L);
        affiliation.setIdNum2SecondStreetWorkDataCenter(1L);
        affiliation.setIdNumHorizontalProperty1(1L);
        affiliation.setIdNumHorizontalProperty1ContractDataStep2(1L);
        affiliation.setIdNumHorizontalProperty1Employer(1L);
        affiliation.setIdNumHorizontalProperty1IndependentWorker(1L);
        affiliation.setIdNumHorizontalProperty1WorkDataCenter(1L);
        affiliation.setIdNumHorizontalProperty2(1L);
        affiliation.setIdNumHorizontalProperty2ContractDataStep2(1L);
        affiliation.setIdNumHorizontalProperty2Employer(1L);
        affiliation.setIdNumHorizontalProperty2IndependentWorker(1L);
        affiliation.setIdNumHorizontalProperty2WorkDataCenter(1L);
        affiliation.setIdNumHorizontalProperty3(1L);
        affiliation.setIdNumHorizontalProperty3ContractDataStep2(1L);
        affiliation.setIdNumHorizontalProperty3Employer(1L);
        affiliation.setIdNumHorizontalProperty3IndependentWorker(1L);
        affiliation.setIdNumHorizontalProperty3WorkDataCenter(1L);
        affiliation.setIdNumHorizontalProperty4(1L);
        affiliation.setIdNumHorizontalProperty4ContractDataStep2(1L);
        affiliation.setIdNumHorizontalProperty4Employer(1L);
        affiliation.setIdNumHorizontalProperty4IndependentWorker(1L);
        affiliation.setIdNumHorizontalProperty4WorkDataCenter(1L);
        affiliation.setIdNumberMainStreet(1L);
        affiliation.setIdNumberMainStreetContractDataStep2(1L);
        affiliation.setIdNumberMainStreetEmployer(1L);
        affiliation.setIdNumberMainStreetIndependentWorker(1L);
        affiliation.setIdNumberMainStreetWorkDataCenter(1L);
        affiliation.setIdProcedureType(1L);
        affiliation.setIdentificationDocumentNumber("42");
        affiliation.setIdentificationDocumentNumberContractor("42");
        affiliation.setIdentificationDocumentNumberContractorLegalRepresentative("42");
        affiliation.setIdentificationDocumentNumberSignatory("42");
        affiliation.setIdentificationDocumentType("Identification Document Type");
        affiliation.setIdentificationDocumentTypeContractor("Identification Document Type Contractor");
        affiliation.setIdentificationDocumentTypeLegalRepresentative(
                "Identification Document Type Legal Representative");
        affiliation.setIdentificationDocumentTypeSignatory("Identification Document Type Signatory");
        affiliation.setInitialNumberWorkers(10);
        affiliation.setIs723(true);
        affiliation.setIsBis(true);
        affiliation.setIsBisContractDataStep2(true);
        affiliation.setIsBisEmployer(true);
        affiliation.setIsBisIndependentWorker(true);
        affiliation.setIsBisWorkDataCenter(true);
        affiliation.setIsForeignPension(true);
        affiliation.setIsHiringButler(true);
        affiliation.setIsHiringDomesticService(true);
        affiliation.setIsHiringDriver(true);
        affiliation.setIsHiringNurse(true);
        affiliation.setIsRuralZone(true);
        affiliation.setIsRuralZoneEmployer(true);
        affiliation.setIsSameEmployerAddress(true);
        affiliation.setIsVip(true);
        affiliation.setJourneyEstablished("Journey Established");
        affiliation.setLegalRepFirstName("Jane");
        affiliation.setLegalRepSecondName("Legal Rep Second Name");
        affiliation.setLegalRepSecondSurname("Doe");
        affiliation.setLegalRepSurname("Doe");
        affiliation.setMunicipalityEmployer(1L);
        affiliation.setNameLegalNatureEmployer("Name Legal Nature Employer");
        affiliation.setNationality(1L);
        affiliation.setNationalityIndependentWorker(1L);
        affiliation.setNumButler(10);
        affiliation.setNumDomesticService(10);
        affiliation.setNumDriver(10);
        affiliation.setNumHeadquarters(10);
        affiliation.setNumNurse(10);
        affiliation.setNumWorkCenters(10);
        affiliation.setOccupation("Occupation");
        affiliation.setOccupationSignatory("Occupation Signatory");
        affiliation.setOtherGender("Other Gender");
        affiliation.setPensionFundAdministrator(1L);
        affiliation.setPersonType("Person Type");
        affiliation.setPhone1("6625550144");
        affiliation.setPhone1WorkDataCenter("6625550144");
        affiliation.setPhone2("6625550144");
        affiliation.setPhone2WorkDataCenter("6625550144");
        affiliation.setPrice(Constant.IBC_TOLERANCE);
        affiliation.setRealNumberWorkers(1L);
        affiliation.setRisk("Risk");
        affiliation.setSecondName("Second Name");
        affiliation.setSecondNameContractor("Second Name Contractor");
        affiliation.setSecondNameIndependentWorker("Second Name Independent Worker");
        affiliation.setSecondNameSignatory("Second Name Signatory");
        affiliation.setSecondSurname("Doe");
        affiliation.setSecondSurnameContractor("Doe");
        affiliation.setSecondSurnameIndependentWorker("Doe");
        affiliation.setSecondSurnameSignatory("Doe");
        affiliation.setSecondaryEmail("jane.doe@example.org");
        affiliation.setSecondaryPhone1("6625550144");
        affiliation.setSecondaryPhone2("6625550144");
        affiliation.setSpecialTaxIdentificationNumber("42");
        affiliation.setStageManagement("Stage Management");
        affiliation.setStartDate(LocalDate.of(1970, 1, 1));
        affiliation.setSurname("Doe");
        affiliation.setSurnameContractor("Doe");
        affiliation.setSurnameIndependentWorker("Doe");
        affiliation.setSurnameSignatory("Doe");
        affiliation.setTotalPayrollValue(Constant.IBC_TOLERANCE);
        affiliation.setTransportSupply(true);
        affiliation.setTypeAffiliation("Type Affiliation");
        Optional<Affiliation> ofResult2 = Optional.of(affiliation);
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findByFiledNumber(
                Mockito.<String>any()))
                .thenReturn(ofResult2);

        // Act and Assert
        assertThrows(
                AffiliationError.class, () -> filedWebSocketServiceImpl.changeStateAffiliation("42"));
        verify(iAffiliationEmployerDomesticServiceIndependentRepository).findByFiledNumber("42");
        verify(affiliateMercantileRepository).findByFiledNumber("42");
        verify(affiliateRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link FiledWebSocketServiceImpl#connectOfficial(Map, String)}.
     *
     * <ul>
     *   <li>Given {@code 42}.
     *   <li>When {@link HashMap#HashMap()} {@code numberFiled} is {@code 42}.
     *   <li>Then throw {@link AffiliationError}.
     * </ul>
     *
     * <p>Method under test: {@link FiledWebSocketServiceImpl#connectOfficial(Map, String)}
     */
    @Test
    @DisplayName(
            "Test connectOfficial(Map, String); given '42'; when HashMap() 'numberFiled' is '42'; then throw AffiliationError")
    
    void testConnectOfficial_given42_whenHashMapNumberFiledIs42_thenThrowAffiliationError() {
        // Arrange
        HashMap<String, String> dataUser = new HashMap<>();
        dataUser.put("numberFiled", "42");

        // Act and Assert
        assertThrows(
                AffiliationError.class, () -> filedWebSocketServiceImpl.connectOfficial(dataUser, "MD"));
    }

    /**
     * Test {@link FiledWebSocketServiceImpl#connectOfficial(Map, String)}.
     *
     * <ul>
     *   <li>Given {@link AffiliateMercantileRepository}.
     *   <li>When {@link HashMap#HashMap()}.
     * </ul>
     *
     * <p>Method under test: {@link FiledWebSocketServiceImpl#connectOfficial(Map, String)}
     */
    @Test
    @DisplayName(
            "Test connectOfficial(Map, String); given AffiliateMercantileRepository; when HashMap()")
    
    void testConnectOfficial_givenAffiliateMercantileRepository_whenHashMap() {
        // Arrange, Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> filedWebSocketServiceImpl.connectOfficial(new HashMap<>(), "MD"));
    }

    /**
     * Test {@link FiledWebSocketServiceImpl#connectOfficial(Map, String)}.
     *
     * <ul>
     *   <li>Given {@link AffiliateMercantileRepository}.
     *   <li>When {@code null}.
     * </ul>
     *
     * <p>Method under test: {@link FiledWebSocketServiceImpl#connectOfficial(Map, String)}
     */
    @Test
    @DisplayName(
            "Test connectOfficial(Map, String); given AffiliateMercantileRepository; when 'null'")
    
    void testConnectOfficial_givenAffiliateMercantileRepository_whenNull() {
        // Arrange, Act and Assert
        assertThrows(
                AffiliationError.class, () -> filedWebSocketServiceImpl.connectOfficial(null, "connected"));
    }

    /**
     * Test {@link FiledWebSocketServiceImpl#connectOfficial(Map, String)}.
     *
     * <ul>
     *   <li>Then calls {@link AffiliateMercantileRepository#findOne(Specification)}.
     * </ul>
     *
     * <p>Method under test: {@link FiledWebSocketServiceImpl#connectOfficial(Map, String)}
     */
    @Test
    @DisplayName("Test connectOfficial(Map, String); then calls findOne(Specification)")
    
    void testConnectOfficial_thenCallsFindOne() {
        // Arrange
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        HashMap<String, String> dataUser = new HashMap<>();
        dataUser.put("numberFiled", "Data User");
        dataUser.put("id", "Data User");

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () -> filedWebSocketServiceImpl.connectOfficial(dataUser, "connected"));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link FiledWebSocketServiceImpl#connectUser(Map, String)}.
     *
     * <ul>
     *   <li>Given {@code 42}.
     *   <li>When {@link HashMap#HashMap()} {@code numberFiled} is {@code 42}.
     *   <li>Then throw {@link AffiliationError}.
     * </ul>
     *
     * <p>Method under test: {@link FiledWebSocketServiceImpl#connectUser(Map, String)}
     */
    @Test
    @DisplayName(
            "Test connectUser(Map, String); given '42'; when HashMap() 'numberFiled' is '42'; then throw AffiliationError")
    
    void testConnectUser_given42_whenHashMapNumberFiledIs42_thenThrowAffiliationError() {
        // Arrange
        HashMap<String, String> dataUser = new HashMap<>();
        dataUser.put("numberFiled", "42");

        // Act and Assert
        assertThrows(
                AffiliationError.class, () -> filedWebSocketServiceImpl.connectUser(dataUser, "MD"));
    }

    /**
     * Test {@link FiledWebSocketServiceImpl#connectUser(Map, String)}.
     *
     * <ul>
     *   <li>Given {@link AffiliateMercantileRepository}.
     *   <li>When {@link HashMap#HashMap()}.
     * </ul>
     *
     * <p>Method under test: {@link FiledWebSocketServiceImpl#connectUser(Map, String)}
     */
    @Test
    @DisplayName("Test connectUser(Map, String); given AffiliateMercantileRepository; when HashMap()")
    
    void testConnectUser_givenAffiliateMercantileRepository_whenHashMap() {
        // Arrange, Act and Assert
        assertThrows(
                AffiliationError.class, () -> filedWebSocketServiceImpl.connectUser(new HashMap<>(), "MD"));
    }

    /**
     * Test {@link FiledWebSocketServiceImpl#connectUser(Map, String)}.
     *
     * <ul>
     *   <li>Given {@link AffiliateMercantileRepository}.
     *   <li>When {@code null}.
     * </ul>
     *
     * <p>Method under test: {@link FiledWebSocketServiceImpl#connectUser(Map, String)}
     */
    @Test
    @DisplayName("Test connectUser(Map, String); given AffiliateMercantileRepository; when 'null'")
    
    void testConnectUser_givenAffiliateMercantileRepository_whenNull() {
        // Arrange, Act and Assert
        assertThrows(
                AffiliationError.class, () -> filedWebSocketServiceImpl.connectUser(null, "connected"));
    }

    /**
     * Test {@link FiledWebSocketServiceImpl#connectUser(Map, String)}.
     *
     * <ul>
     *   <li>Then calls {@link AffiliateMercantileRepository#findOne(Specification)}.
     * </ul>
     *
     * <p>Method under test: {@link FiledWebSocketServiceImpl#connectUser(Map, String)}
     */
    @Test
    @DisplayName("Test connectUser(Map, String); then calls findOne(Specification)")
    
    void testConnectUser_thenCallsFindOne() {
        // Arrange
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        HashMap<String, String> dataUser = new HashMap<>();
        dataUser.put("numberFiled", "Data User");
        dataUser.put("id", "Data User");

        // Act and Assert
        assertThrows(
                AffiliationError.class, () -> filedWebSocketServiceImpl.connectUser(dataUser, "connected"));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link FiledWebSocketServiceImpl#reschedulingInterviewWeb(String, LocalDateTime)}.
     *
     * <p>Method under test: {@link FiledWebSocketServiceImpl#reschedulingInterviewWeb(String,
     * LocalDateTime)}
     */
    @Test
    @DisplayName("Test reschedulingInterviewWeb(String, LocalDateTime)")
    
    void testReschedulingInterviewWeb() {
        // Arrange
        when(affiliateRepository.findOne(Mockito.<Specification<Affiliate>>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

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
        Optional<AffiliateMercantile> ofResult = Optional.of(affiliateMercantile);
        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenReturn(ofResult);

        // Act and Assert
        assertThrows(
                AffiliationError.class,
                () ->
                        filedWebSocketServiceImpl.reschedulingInterviewWeb(
                                "42", LocalDate.of(1970, 1, 1).atStartOfDay()));
        verify(affiliateRepository).findOne(isA(Specification.class));
        verify(affiliateMercantileRepository).findOne(isA(Specification.class));
    }

    /**
     * Test {@link FiledWebSocketServiceImpl#notificationByFiledNumber(String)}.
     *
     * <ul>
     *   <li>Then return {@code MD} is {@code 1970-01-01T00:00}.
     * </ul>
     *
     * <p>Method under test: {@link FiledWebSocketServiceImpl#notificationByFiledNumber(String)}
     */
    @Test
    @DisplayName("Test notificationByFiledNumber(String); then return 'MD' is '1970-01-01T00:00'")
    
    void testNotificationByFiledNumber_thenReturnMdIs19700101t0000() {
        // Arrange
        RegistryConnectInterviewWeb registryConnectInterviewWeb = new RegistryConnectInterviewWeb();
        registryConnectInterviewWeb.setDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        registryConnectInterviewWeb.setId(1L);
        registryConnectInterviewWeb.setIdUser(1L);
        registryConnectInterviewWeb.setNumberFiled("42");
        registryConnectInterviewWeb.setState("MD");
        registryConnectInterviewWeb.setTypeUser(null);

        RegistryConnectInterviewWeb registryConnectInterviewWeb2 = new RegistryConnectInterviewWeb();
        registryConnectInterviewWeb2.setDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        registryConnectInterviewWeb2.setId(2L);
        registryConnectInterviewWeb2.setIdUser(2L);
        registryConnectInterviewWeb2.setNumberFiled("userconnected");
        registryConnectInterviewWeb2.setState("userconnected");
        registryConnectInterviewWeb2.setTypeUser("officialconnected");

        ArrayList<RegistryConnectInterviewWeb> registryConnectInterviewWebList = new ArrayList<>();
        registryConnectInterviewWebList.add(registryConnectInterviewWeb2);
        registryConnectInterviewWebList.add(registryConnectInterviewWeb);
        when(registryConnectInterviewWebService.findByFiledNumber(Mockito.<String>any()))
                .thenReturn(registryConnectInterviewWebList);

        // Act
        Map<String, String> actualNotificationByFiledNumberResult =
                filedWebSocketServiceImpl.notificationByFiledNumber("42");

        // Assert
        verify(registryConnectInterviewWebService).findByFiledNumber("42");
        assertEquals(6, actualNotificationByFiledNumberResult.size());
        assertEquals("1970-01-01T00:00", actualNotificationByFiledNumberResult.get("MD"));
        assertEquals(
                "1970-01-01T00:00",
                actualNotificationByFiledNumberResult.get("officialconnecteduserconnected"));
        assertNull(actualNotificationByFiledNumberResult.get("officialconnected"));
        assertNull(actualNotificationByFiledNumberResult.get("officialdisconnected"));
        assertNull(actualNotificationByFiledNumberResult.get("userconnected"));
        assertNull(actualNotificationByFiledNumberResult.get("userdisconnected"));
    }

    /**
     * Test {@link FiledWebSocketServiceImpl#notificationByFiledNumber(String)}.
     *
     * <ul>
     *   <li>Then return size is five.
     * </ul>
     *
     * <p>Method under test: {@link FiledWebSocketServiceImpl#notificationByFiledNumber(String)}
     */
    @Test
    @DisplayName("Test notificationByFiledNumber(String); then return size is five")
    
    void testNotificationByFiledNumber_thenReturnSizeIsFive() {
        // Arrange
        RegistryConnectInterviewWeb registryConnectInterviewWeb = new RegistryConnectInterviewWeb();
        registryConnectInterviewWeb.setDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        registryConnectInterviewWeb.setId(1L);
        registryConnectInterviewWeb.setIdUser(1L);
        registryConnectInterviewWeb.setNumberFiled("42");
        registryConnectInterviewWeb.setState("MD");
        registryConnectInterviewWeb.setTypeUser("userconnected");

        ArrayList<RegistryConnectInterviewWeb> registryConnectInterviewWebList = new ArrayList<>();
        registryConnectInterviewWebList.add(registryConnectInterviewWeb);
        when(registryConnectInterviewWebService.findByFiledNumber(Mockito.<String>any()))
                .thenReturn(registryConnectInterviewWebList);

        // Act
        Map<String, String> actualNotificationByFiledNumberResult =
                filedWebSocketServiceImpl.notificationByFiledNumber("42");

        // Assert
        verify(registryConnectInterviewWebService).findByFiledNumber("42");
        assertEquals(5, actualNotificationByFiledNumberResult.size());
        assertEquals("1970-01-01T00:00", actualNotificationByFiledNumberResult.get("userconnectedMD"));
        assertNull(actualNotificationByFiledNumberResult.get("officialconnected"));
        assertNull(actualNotificationByFiledNumberResult.get("officialdisconnected"));
        assertNull(actualNotificationByFiledNumberResult.get("userconnected"));
        assertNull(actualNotificationByFiledNumberResult.get("userdisconnected"));
    }

    /**
     * Test {@link FiledWebSocketServiceImpl#notificationByFiledNumber(String)}.
     *
     * <ul>
     *   <li>Then return size is four.
     * </ul>
     *
     * <p>Method under test: {@link FiledWebSocketServiceImpl#notificationByFiledNumber(String)}
     */
    @Test
    @DisplayName("Test notificationByFiledNumber(String); then return size is four")
    
    void testNotificationByFiledNumber_thenReturnSizeIsFour() {
        // Arrange
        when(registryConnectInterviewWebService.findByFiledNumber(Mockito.<String>any()))
                .thenReturn(new ArrayList<>());

        // Act
        Map<String, String> actualNotificationByFiledNumberResult =
                filedWebSocketServiceImpl.notificationByFiledNumber("42");

        // Assert
        verify(registryConnectInterviewWebService).findByFiledNumber("42");
        assertEquals(4, actualNotificationByFiledNumberResult.size());
        assertNull(actualNotificationByFiledNumberResult.get("officialconnected"));
        assertNull(actualNotificationByFiledNumberResult.get("officialdisconnected"));
        assertNull(actualNotificationByFiledNumberResult.get("userconnected"));
        assertNull(actualNotificationByFiledNumberResult.get("userdisconnected"));
    }

    /**
     * Test {@link FiledWebSocketServiceImpl#notificationByFiledNumber(String)}.
     *
     * <ul>
     *   <li>Then return size is six.
     * </ul>
     *
     * <p>Method under test: {@link FiledWebSocketServiceImpl#notificationByFiledNumber(String)}
     */
    @Test
    @DisplayName("Test notificationByFiledNumber(String); then return size is six")
    
    void testNotificationByFiledNumber_thenReturnSizeIsSix() {
        // Arrange
        RegistryConnectInterviewWeb registryConnectInterviewWeb = new RegistryConnectInterviewWeb();
        registryConnectInterviewWeb.setDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        registryConnectInterviewWeb.setId(1L);
        registryConnectInterviewWeb.setIdUser(1L);
        registryConnectInterviewWeb.setNumberFiled("42");
        registryConnectInterviewWeb.setState("MD");
        registryConnectInterviewWeb.setTypeUser("userconnected");

        RegistryConnectInterviewWeb registryConnectInterviewWeb2 = new RegistryConnectInterviewWeb();
        registryConnectInterviewWeb2.setDate(LocalDate.of(1970, 1, 1).atStartOfDay());
        registryConnectInterviewWeb2.setId(2L);
        registryConnectInterviewWeb2.setIdUser(2L);
        registryConnectInterviewWeb2.setNumberFiled("userconnected");
        registryConnectInterviewWeb2.setState("userconnected");
        registryConnectInterviewWeb2.setTypeUser("officialconnected");

        ArrayList<RegistryConnectInterviewWeb> registryConnectInterviewWebList = new ArrayList<>();
        registryConnectInterviewWebList.add(registryConnectInterviewWeb2);
        registryConnectInterviewWebList.add(registryConnectInterviewWeb);
        when(registryConnectInterviewWebService.findByFiledNumber(Mockito.<String>any()))
                .thenReturn(registryConnectInterviewWebList);

        // Act
        Map<String, String> actualNotificationByFiledNumberResult =
                filedWebSocketServiceImpl.notificationByFiledNumber("42");

        // Assert
        verify(registryConnectInterviewWebService).findByFiledNumber("42");
        assertEquals(6, actualNotificationByFiledNumberResult.size());
        assertEquals(
                "1970-01-01T00:00",
                actualNotificationByFiledNumberResult.get("officialconnecteduserconnected"));
        assertEquals("1970-01-01T00:00", actualNotificationByFiledNumberResult.get("userconnectedMD"));
        assertNull(actualNotificationByFiledNumberResult.get("officialconnected"));
        assertNull(actualNotificationByFiledNumberResult.get("officialdisconnected"));
        assertNull(actualNotificationByFiledNumberResult.get("userconnected"));
        assertNull(actualNotificationByFiledNumberResult.get("userdisconnected"));
    }

    /**
     * Test {@link FiledWebSocketServiceImpl#notificationByFiledNumber(String)}.
     *
     * <ul>
     *   <li>Then throw {@link AffiliationError}.
     * </ul>
     *
     * <p>Method under test: {@link FiledWebSocketServiceImpl#notificationByFiledNumber(String)}
     */
    @Test
    @DisplayName("Test notificationByFiledNumber(String); then throw AffiliationError")
    
    void testNotificationByFiledNumber_thenThrowAffiliationError() {
        // Arrange
        when(registryConnectInterviewWebService.findByFiledNumber(Mockito.<String>any()))
                .thenThrow(new AffiliationError("Not all who wander are lost"));

        // Act and Assert
        assertThrows(
                AffiliationError.class, () -> filedWebSocketServiceImpl.notificationByFiledNumber("42"));
        verify(registryConnectInterviewWebService).findByFiledNumber("42");
    }
}
