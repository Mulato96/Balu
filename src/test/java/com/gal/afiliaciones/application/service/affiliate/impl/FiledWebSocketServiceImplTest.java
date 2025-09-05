package com.gal.afiliaciones.application.service.affiliate.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.gal.afiliaciones.application.service.notification.RegistryConnectInterviewWebService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.domain.model.RegistryConnectInterviewWeb;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DataStatusAffiliationDTO;


class FiledWebSocketServiceImplTest {

    private SimpMessagingTemplate messagingTemplate;
    private AffiliateRepository affiliateRepository;
    private AffiliateMercantileRepository mercantileRepository;
    private IUserPreRegisterRepository iUserPreRegisterRepository;
    private AffiliateMercantileRepository affiliateMercantileRepository;
    private RegistryConnectInterviewWebService registryConnectInterviewWebService;
    private IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;

    private FiledWebSocketServiceImpl service;

    @BeforeEach
    void setUp() {
        messagingTemplate = mock(SimpMessagingTemplate.class);
        affiliateRepository = mock(AffiliateRepository.class);
        mercantileRepository = mock(AffiliateMercantileRepository.class);
        iUserPreRegisterRepository = mock(IUserPreRegisterRepository.class);
        affiliateMercantileRepository = mock(AffiliateMercantileRepository.class);
        registryConnectInterviewWebService = mock(RegistryConnectInterviewWebService.class);
        repositoryAffiliation = mock(IAffiliationEmployerDomesticServiceIndependentRepository.class);

        service = new FiledWebSocketServiceImpl(messagingTemplate, affiliateRepository, mercantileRepository,
                iUserPreRegisterRepository, affiliateMercantileRepository, registryConnectInterviewWebService, repositoryAffiliation);
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
        when(repositoryAffiliation.findByFiledNumber(filedNumber)).thenReturn(Optional.of(affiliation));
        when(mercantileRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.empty());

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);

        service.changeStateAffiliation(filedNumber);

        verify(messagingTemplate).convertAndSend(eq("/filed/" + filedNumber), captor.capture());

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
        when(repositoryAffiliation.findByFiledNumber(filedNumber)).thenReturn(Optional.empty());
        when(mercantileRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.of(mercantile));

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);

        service.changeStateAffiliation(filedNumber);

        verify(messagingTemplate).convertAndSend(eq("/filed/" + filedNumber), captor.capture());

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

        service.changeStateAffiliation(filedNumber);

        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
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

        service.connectOfficial(dataUser, state);

        verify(registryConnectInterviewWebService).save(registryCaptor.capture());
        RegistryConnectInterviewWeb savedRegistry = registryCaptor.getValue();
        assertEquals(filedNumber, savedRegistry.getNumberFiled());
        assertEquals(Long.parseLong(id), savedRegistry.getIdUser());
        assertEquals(state, savedRegistry.getState());
        assertEquals("official", savedRegistry.getTypeUser());
        assertNotNull(savedRegistry.getDate());

        verify(messagingTemplate).convertAndSend(eq("/filed/" + filedNumber), messageCaptor.capture());
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

        AffiliationError ex = assertThrows(AffiliationError.class, () -> service.connectOfficial(dataUser, "connected"));
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

        service.connectUser(dataUser, state);

        verify(registryConnectInterviewWebService).save(registryCaptor.capture());
        RegistryConnectInterviewWeb savedRegistry = registryCaptor.getValue();
        assertEquals(filedNumber, savedRegistry.getNumberFiled());
        assertEquals(Long.parseLong(id), savedRegistry.getIdUser());
        assertEquals(state, savedRegistry.getState());
        assertEquals("user", savedRegistry.getTypeUser());
        assertNotNull(savedRegistry.getDate());

        verify(messagingTemplate).convertAndSend(eq("/filed/" + filedNumber), messageCaptor.capture());
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

        Map<String, String> result = service.notificationByFiledNumber(filedNumber);

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
            dto = (DataStatusAffiliationDTO) method.invoke(service, filedNumber);
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
            dto = (DataStatusAffiliationDTO) method.invoke(service, filedNumber);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertNull(dto);
    }
}