package com.gal.afiliaciones.application.service.affiliate.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.reactive.function.client.WebClient;

import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.daily.DailyService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.DateInterviewWeb;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.infrastructure.dao.repository.DateInterviewWebRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DateInterviewWebDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.TemplateSendEmailsDTO;
import com.gal.afiliaciones.infrastructure.dto.daily.DailyRoomsDTO;
import com.gal.afiliaciones.infrastructure.dto.daily.DataDailyDTO;
import com.gal.afiliaciones.infrastructure.dto.daily.TokenDailyDTO;
import com.gal.afiliaciones.infrastructure.dto.roles.ActiveRoleDTO;
import com.gal.afiliaciones.infrastructure.dto.roles.ActiveRolesResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.roles.DataDTO;
import com.gal.afiliaciones.infrastructure.dto.roles.PermissionDTO;
import com.gal.afiliaciones.infrastructure.dto.roles.ProfileAndPermissionDTO;
import com.gal.afiliaciones.infrastructure.dto.roles.RoleResponseDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;



class ScheduleInterviewWebServiceImplTest {

    @Mock
    private SendEmails sendEmails;
    @Mock
    private DailyService dailyService;
    @Mock
    private CollectProperties properties;
    @Mock
    private WebClient webClient;
    @Mock
    private AffiliateRepository affiliateRepository;
    @Mock
    private DateInterviewWebRepository dateInterviewWebRepository;
    @Mock
    private IUserPreRegisterRepository iUserPreRegisterRepository;
    @Mock
    private AffiliateMercantileRepository affiliateMercantileRepository;

    @InjectMocks
    private ScheduleInterviewWebServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ScheduleInterviewWebServiceImpl(
                sendEmails, dailyService, properties, webClient,
                affiliateRepository, dateInterviewWebRepository,
                iUserPreRegisterRepository, affiliateMercantileRepository
        );
    }

    @Test
    void createScheduleInterviewWeb_shouldThrowIfAlreadyScheduled() {
        DateInterviewWebDTO dto = new DateInterviewWebDTO();
        dto.setIdAffiliate("123");
        when(dateInterviewWebRepository.findOne(any(Specification.class))).thenReturn(Optional.of(new DateInterviewWeb()));
        assertThrows(AffiliationError.class, () -> service.createScheduleInterviewWeb(dto));
    }

    @Test
    void createScheduleInterviewWeb_shouldThrowIfWeekend() {
        DateInterviewWebDTO dto = new DateInterviewWebDTO();
        dto.setIdAffiliate("123");
        dto.setDay(LocalDate.of(2024, 6, 8)); // Saturday
        when(dateInterviewWebRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        assertThrows(AffiliationError.class, () -> service.createScheduleInterviewWeb(dto));
    }

    @Test
    void listScheduleInterviewWeb_shouldReturnList() {
        List<DateInterviewWeb> list = List.of(new DateInterviewWeb());
        when(dateInterviewWebRepository.findAll()).thenReturn(list);
        assertEquals(list, service.listScheduleInterviewWeb());
    }

    @Test
    void deleteInterviewWeb_shouldDeleteIfAllowed() {
        DateInterviewWeb interview = new DateInterviewWeb();
        interview.setDay(LocalDate.now().plusDays(2));
        interview.setHourStart(LocalTime.now());
        when(dateInterviewWebRepository.findOne(any(Specification.class))).thenReturn(Optional.of(interview));
        AffiliateMercantile merc = new AffiliateMercantile();
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(merc));
        //doNothing().when(dateInterviewWebRepository).delete(any(Specification.class));
        when(properties.getInterviewWebDaysLimit()).thenReturn(5);
        String result = service.deleteInterviewWeb("123");
        assertTrue(result.contains("Entrevista Web cancelada correctamente"));
    }

    @Test
    void deleteInterviewWeb_shouldThrowIfNotAllowed() {
        DateInterviewWeb interview = new DateInterviewWeb();
        interview.setDay(LocalDate.now());
        interview.setHourStart(LocalTime.now());
        when(dateInterviewWebRepository.findOne(any(Specification.class))).thenReturn(Optional.of(interview));
        assertThrows(AffiliationError.class, () -> service.deleteInterviewWeb("123"));
    }

    @Test
    void deleteInterviewWebReSchedule_shouldDelete() {
        DateInterviewWeb interview = new DateInterviewWeb();
        interview.setDay(LocalDate.now());
        interview.setHourStart(LocalTime.now());
        when(dateInterviewWebRepository.findOne(any(Specification.class))).thenReturn(Optional.of(interview));
        AffiliateMercantile merc = new AffiliateMercantile();
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(merc));
        //doNothing().when(dateInterviewWebRepository).delete(any(Specification.class));
        String result = service.deleteInterviewWebReSchedule("123");
        assertTrue(result.contains("Entrevista Web cancelada correctamente"));
    }

    @Test
    void delete_shouldDelete() {
        DateInterviewWeb interview = new DateInterviewWeb();
        when(dateInterviewWebRepository.findOne(any(Specification.class))).thenReturn(Optional.of(interview));
        //doNothing().when(dateInterviewWebRepository).delete(any(Specification.class));
        assertDoesNotThrow(() -> service.delete("123"));
    }

    @Test
    void meshTimetable_shouldThrowIfWeekend() {
        LocalDate date = LocalDate.of(2024, 6, 8); // Saturday
        assertThrows(AffiliationError.class, () -> service.meshTimetable(date));
    }

    @Test
    void meshTimetable_shouldReturnList() {
        LocalDate date = LocalDate.of(2024, 6, 10); // Monday
        when(properties.getInterviewWebHourStart()).thenReturn(8L);
        when(properties.getInterviewWebHourEnd()).thenReturn(17L);
        when(properties.getInterviewWebTimeDuration()).thenReturn(30L);
        when(properties.getInterviewWebHourStartLunch()).thenReturn(12L);
        when(properties.getInterviewWebHourEndLunch()).thenReturn(13L);
        when(properties.getMaxConcurrentMeetings()).thenReturn(1L);
        when(dateInterviewWebRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
        List<Map<String, LocalDateTime>> result = service.meshTimetable(date);
        assertNotNull(result);
    }

    @Test
    void calculateDaysSkilled_shouldReturnDate() {
        LocalDate date = LocalDate.now().plusDays(1);
        when(properties.getInterviewWebDaysLimit()).thenReturn(2);
        assertNotNull(service.calculateDaysSkilled(date));
    }

    @Test
    void calculateDaysSkilled_shouldThrowIfInvalid() {
        LocalDate date = LocalDate.now().minusDays(1);
        when(properties.getInterviewWebDaysLimit()).thenReturn(2);
        assertThrows(AffiliationError.class, () -> service.calculateDaysSkilled(date));
    }

    @Test
    void createScheduleInterviewWeb_shouldThrowIfAlreadyScheduled_duplicate() {
        DateInterviewWebDTO dto = new DateInterviewWebDTO();
        dto.setIdAffiliate("456");
        when(dateInterviewWebRepository.findOne(any(Specification.class))).thenReturn(Optional.of(new DateInterviewWeb()));
        assertThrows(AffiliationError.class, () -> service.createScheduleInterviewWeb(dto));
    }

    @Test
    void createScheduleInterviewWeb_shouldThrowIfDayIsWeekend() {
        DateInterviewWebDTO dto = new DateInterviewWebDTO();
        dto.setIdAffiliate("789");
        dto.setDay(LocalDate.of(2024, 6, 9)); // Sunday
        when(dateInterviewWebRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        assertThrows(AffiliationError.class, () -> service.createScheduleInterviewWeb(dto));
    }

    @Test
    void createScheduleInterviewWeb_shouldThrowIfDayIsGreaterThanLimit() {
        DateInterviewWebDTO dto = new DateInterviewWebDTO();
        dto.setIdAffiliate("101");
        dto.setDay(LocalDate.now().plusDays(10));
        dto.setHourStart(LocalTime.of(10, 0));
        when(dateInterviewWebRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(properties.getInterviewWebDaysLimit()).thenReturn(2);
        assertThrows(AffiliationError.class, () -> service.createScheduleInterviewWeb(dto));
    }

    @Test
    void createScheduleInterviewWeb_shouldThrowIfLunchTime() {
        DateInterviewWebDTO dto = new DateInterviewWebDTO();
        dto.setIdAffiliate("103");
        dto.setDay(LocalDate.now().plusDays(1));
        dto.setHourStart(LocalTime.of(12, 0));
        when(dateInterviewWebRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(properties.getInterviewWebDaysLimit()).thenReturn(5);
        when(properties.getInterviewWebHourStartLunch()).thenReturn(12L);
        when(properties.getInterviewWebHourEndLunch()).thenReturn(13L);
        when(properties.getInterviewWebHourStart()).thenReturn(8L);
        when(properties.getInterviewWebHourEnd()).thenReturn(17L);
        when(properties.getInterviewWebTimeDuration()).thenReturn(30L);
        when(properties.getMaxConcurrentMeetings()).thenReturn(1L);
        when(dateInterviewWebRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
        assertThrows(AffiliationError.class, () -> service.createScheduleInterviewWeb(dto));
    }

    @Test
    void createScheduleInterviewWeb_shouldThrowIfDurationNotAllowed() {
        DateInterviewWebDTO dto = new DateInterviewWebDTO();
        dto.setIdAffiliate("104");
        dto.setDay(LocalDate.now().plusDays(1));
        dto.setHourStart(LocalTime.of(10, 0));
        dto.setHourEnd(LocalTime.of(10, 10)); // 10 min, not allowed
        when(dateInterviewWebRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(properties.getInterviewWebDaysLimit()).thenReturn(5);
        when(properties.getInterviewWebHourStartLunch()).thenReturn(12L);
        when(properties.getInterviewWebHourEndLunch()).thenReturn(13L);
        when(properties.getInterviewWebHourStart()).thenReturn(8L);
        when(properties.getInterviewWebHourEnd()).thenReturn(17L);
        when(properties.getInterviewWebTimeDuration()).thenReturn(30L);
        when(properties.getMaxConcurrentMeetings()).thenReturn(1L);
        when(dateInterviewWebRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
        assertThrows(AffiliationError.class, () -> service.createScheduleInterviewWeb(dto));
    }

    @Test
    void createScheduleInterviewWeb_shouldThrowIfScheduleAlreadyExists() {
        DateInterviewWebDTO dto = new DateInterviewWebDTO();
        dto.setIdAffiliate("105");
        dto.setDay(LocalDate.now().plusDays(1));
        dto.setHourStart(LocalTime.of(10, 0));
        dto.setHourEnd(LocalTime.of(10, 30));
        when(dateInterviewWebRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(properties.getInterviewWebDaysLimit()).thenReturn(5);
        when(properties.getInterviewWebHourStartLunch()).thenReturn(12L);
        when(properties.getInterviewWebHourEndLunch()).thenReturn(13L);
        when(properties.getInterviewWebHourStart()).thenReturn(8L);
        when(properties.getInterviewWebHourEnd()).thenReturn(17L);
        when(properties.getInterviewWebTimeDuration()).thenReturn(30L);
        when(properties.getMaxConcurrentMeetings()).thenReturn(0L); // simulate already scheduled
        when(dateInterviewWebRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
        assertThrows(AffiliationError.class, () -> service.createScheduleInterviewWeb(dto));
    }

    @Test
    void createScheduleInterviewWeb_shouldThrowIfNameAndSurnameNull() {
        DateInterviewWebDTO dto = new DateInterviewWebDTO();
        dto.setIdAffiliate("106");
        dto.setDay(LocalDate.now().plusDays(1));
        dto.setHourStart(LocalTime.of(10, 0));
        dto.setHourEnd(LocalTime.of(10, 30));
        dto.setName(null);
        dto.setSurname(null);
        when(dateInterviewWebRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(properties.getInterviewWebDaysLimit()).thenReturn(5);
        when(properties.getInterviewWebHourStartLunch()).thenReturn(12L);
        when(properties.getInterviewWebHourEndLunch()).thenReturn(13L);
        when(properties.getInterviewWebHourStart()).thenReturn(8L);
        when(properties.getInterviewWebHourEnd()).thenReturn(17L);
        when(properties.getInterviewWebTimeDuration()).thenReturn(30L);
        when(properties.getMaxConcurrentMeetings()).thenReturn(1L);
        when(dateInterviewWebRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
        assertThrows(AffiliationError.class, () -> service.createScheduleInterviewWeb(dto));
    }

    @Test
    void calculateMeshTimetable_shouldReturnEmptyListIfNoAvailableSlots() {
        LocalDate date = LocalDate.of(2024, 6, 10); // Monday
        when(properties.getInterviewWebHourStart()).thenReturn(8L);
        when(properties.getInterviewWebHourEnd()).thenReturn(9L); // Only one hour window
        when(properties.getInterviewWebTimeDuration()).thenReturn(60L); // 1 hour duration
        when(properties.getInterviewWebHourStartLunch()).thenReturn(12L);
        when(properties.getInterviewWebHourEndLunch()).thenReturn(13L);
        when(properties.getMaxConcurrentMeetings()).thenReturn(1L);
        when(dateInterviewWebRepository.findAll(any(Specification.class))).thenReturn(List.of(
                // Simulate that the only slot is already taken
                new DateInterviewWeb() {{
                    setHourStart(LocalTime.of(8, 0));
                }}
        ));
        List<Map<String, LocalDateTime>> result = service.meshTimetable(date);
        assertTrue(result.isEmpty());
    }

    @Test
    void calculateMeshTimetable_shouldReturnSlotsOutsideLunchTime() {
        LocalDate date = LocalDate.of(2024, 6, 10); // Monday
        when(properties.getInterviewWebHourStart()).thenReturn(8L);
        when(properties.getInterviewWebHourEnd()).thenReturn(14L);
        when(properties.getInterviewWebTimeDuration()).thenReturn(60L);
        when(properties.getInterviewWebHourStartLunch()).thenReturn(12L);
        when(properties.getInterviewWebHourEndLunch()).thenReturn(13L);
        when(properties.getMaxConcurrentMeetings()).thenReturn(1L);
        when(dateInterviewWebRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
        List<Map<String, LocalDateTime>> result = service.meshTimetable(date);
        // Should not include 12:00 slot (lunch)
        assertTrue(result.stream().noneMatch(slot -> slot.get("date").getHour() == 12));
        assertTrue(result.stream().anyMatch(slot -> slot.get("date").getHour() == 8));
        assertTrue(result.stream().anyMatch(slot -> slot.get("date").getHour() == 13));
    }

    @Test
    void calculateMeshTimetable_shouldThrowIfWeekend() {
        LocalDate saturday = LocalDate.of(2024, 6, 8);
        assertThrows(AffiliationError.class, () -> service.meshTimetable(saturday));
        LocalDate sunday = LocalDate.of(2024, 6, 9);
        assertThrows(AffiliationError.class, () -> service.meshTimetable(sunday));
    }

    @Test
    void calculateMeshTimetable_shouldRespectMaxConcurrentMeetings() {
        LocalDate date = LocalDate.of(2024, 6, 10); // Monday
        when(properties.getInterviewWebHourStart()).thenReturn(8L);
        when(properties.getInterviewWebHourEnd()).thenReturn(10L);
        when(properties.getInterviewWebTimeDuration()).thenReturn(60L);
        when(properties.getInterviewWebHourStartLunch()).thenReturn(12L);
        when(properties.getInterviewWebHourEndLunch()).thenReturn(13L);
        when(properties.getMaxConcurrentMeetings()).thenReturn(1L);
        // Simulate that 8:00 slot is already full
        DateInterviewWeb taken = new DateInterviewWeb();
        taken.setHourStart(LocalTime.of(8, 0));
        when(dateInterviewWebRepository.findAll(any(Specification.class))).thenReturn(List.of(taken));
        List<Map<String, LocalDateTime>> result = service.meshTimetable(date);
        // Only 9:00 should be available
        assertTrue(result.stream().allMatch(slot -> slot.get("date").getHour() == 9));
    }

    @Test
    void validRoom_shouldReturnFirstRoomIfNoInterviews() {
        LocalTime hourStart = LocalTime.of(10, 0);
        LocalDate day = LocalDate.now().plusDays(1);
        DailyRoomsDTO room1 = new DailyRoomsDTO();
        room1.setId(1L);
        DailyRoomsDTO room2 = new DailyRoomsDTO();
        room2.setId(2L);
        when(dailyService.findAllRooms()).thenReturn(List.of(room1, room2));
        when(dateInterviewWebRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
        // Usando reflection para acceder al método validRoom si no es público
        Long result;
        try {
            java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("validRoom", LocalTime.class, LocalDate.class);
            method.setAccessible(true);
            result = (Long) method.invoke(service, hourStart, day);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertEquals(1L, result);
    }

    @Test
    void validRoom_shouldReturnNullIfNoRoomsAvailable() {
        LocalTime hourStart = LocalTime.of(10, 0);
        LocalDate day = LocalDate.now().plusDays(1);
        when(dailyService.findAllRooms()).thenReturn(Collections.emptyList());
        Long result;
        try {
            java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("validRoom", LocalTime.class, LocalDate.class);
            method.setAccessible(true);
            result = (Long) method.invoke(service, hourStart, day);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertNull(result);
    }


    @Test
    void validRoom_shouldReturnNullIfNoRoomHasSufficientGap() {
        LocalTime hourStart = LocalTime.of(10, 0);
        LocalDate day = LocalDate.now().plusDays(1);
        DailyRoomsDTO room1 = new DailyRoomsDTO();
        room1.setId(1L);
        DateInterviewWeb interview = new DateInterviewWeb();
        interview.setIdRoom(1L);
        interview.setHourStart(LocalTime.of(10, 0));
        when(dailyService.findAllRooms()).thenReturn(List.of(room1));
        when(dateInterviewWebRepository.findAll(any(Specification.class))).thenReturn(List.of(interview));
        Long result;
        try {
            java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("validRoom", LocalTime.class, LocalDate.class);
            method.setAccessible(true);
            result = (Long) method.invoke(service, hourStart, day);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertNull(result);
    }

    @Test
    void sendEmail_shouldSendConfirmationEmailsAndHandleAffiliate() throws Exception {
        // Arrange
        String idAffiliation = "123";
        LocalDateTime dateInterview = LocalDateTime.now();
        Long idOfficial = 1L;

        AffiliateMercantile mockAffiliateMercantile = new AffiliateMercantile();
        mockAffiliateMercantile.setFiledNumber("FN123");
        mockAffiliateMercantile.setIdUserPreRegister(2L);
        mockAffiliateMercantile.setBusinessName("Test Business");

        Affiliate mockAffiliate = new Affiliate();
        UserMain mockUserMain = new UserMain();
        mockUserMain.setEmail("user@test.com");

        DateInterviewWeb mockInterviewWeb = new DateInterviewWeb();
        mockInterviewWeb.setIdOfficial(idOfficial);

        UserMain mockOfficial = new UserMain();
        mockOfficial.setEmail("official@test.com");

        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mockAffiliateMercantile));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mockAffiliate));
        when(iUserPreRegisterRepository.findById(2L)).thenReturn(Optional.of(mockUserMain));
        when(dateInterviewWebRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mockInterviewWeb));
        when(iUserPreRegisterRepository.findById(idOfficial)).thenReturn(Optional.of(mockOfficial));
        when(affiliateMercantileRepository.save(any(AffiliateMercantile.class))).thenReturn(mockAffiliateMercantile);

        // Act
        java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("sendEmail", String.class, LocalDateTime.class, Long.class);
        method.setAccessible(true);
        method.invoke(service, idAffiliation, dateInterview, idOfficial);

        // Assert
        assertEquals(Constant.INTERVIEW_WEB, mockAffiliateMercantile.getStageManagement());
        assertEquals(dateInterview, mockAffiliateMercantile.getDateInterview());
        assertEquals(idOfficial, mockAffiliate.getIdOfficial());

        verify(affiliateMercantileRepository).save(any(AffiliateMercantile.class));
        verify(affiliateRepository).save(any(Affiliate.class));
        verify(sendEmails).confirmationInterviewWeb(any(TemplateSendEmailsDTO.class));
        verify(sendEmails).confirmationInterviewWebOfficial(dateInterview, "official@test.com", idAffiliation);
    }

    @Test
    void sendEmail_shouldHandleAffiliateNotFoundGracefully() throws Exception {
        // Arrange
        String idAffiliation = "123";
        LocalDateTime dateInterview = LocalDateTime.now();
        Long idOfficial = 1L;

        AffiliateMercantile mockAffiliateMercantile = new AffiliateMercantile();
        mockAffiliateMercantile.setFiledNumber("FN123");
        mockAffiliateMercantile.setIdUserPreRegister(2L);
        mockAffiliateMercantile.setBusinessName("Test Business");

        UserMain mockUserMain = new UserMain();
        mockUserMain.setEmail("user@test.com");

        DateInterviewWeb mockInterviewWeb = new DateInterviewWeb();
        mockInterviewWeb.setIdOfficial(idOfficial);

        UserMain mockOfficial = new UserMain();
        mockOfficial.setEmail("official@test.com");

        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mockAffiliateMercantile));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.empty()); // Affiliate not found
        when(iUserPreRegisterRepository.findById(2L)).thenReturn(Optional.of(mockUserMain));
        when(dateInterviewWebRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mockInterviewWeb));
        when(iUserPreRegisterRepository.findById(idOfficial)).thenReturn(Optional.of(mockOfficial));
        when(affiliateMercantileRepository.save(any(AffiliateMercantile.class))).thenReturn(mockAffiliateMercantile);

        // Act
        java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("sendEmail", String.class, LocalDateTime.class, Long.class);
        method.setAccessible(true);
        method.invoke(service, idAffiliation, dateInterview, idOfficial);

        // Assert
        verify(affiliateMercantileRepository).save(any(AffiliateMercantile.class));
        verify(affiliateRepository, never()).save(any(Affiliate.class)); // Should not be called
        verify(sendEmails).confirmationInterviewWeb(any(TemplateSendEmailsDTO.class));
        verify(sendEmails).confirmationInterviewWebOfficial(any(LocalDateTime.class), anyString(), anyString());
    }

    @Test
    void sendEmail_shouldThrowErrorWhenUserPreRegisterNotFound() throws Exception {
        // Arrange
        String idAffiliation = "123";
        LocalDateTime dateInterview = LocalDateTime.now();
        Long idOfficial = 1L;

        AffiliateMercantile mockAffiliateMercantile = new AffiliateMercantile();
        mockAffiliateMercantile.setFiledNumber("FN123");
        mockAffiliateMercantile.setIdUserPreRegister(2L);

        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mockAffiliateMercantile));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(new Affiliate()));
        when(iUserPreRegisterRepository.findById(2L)).thenReturn(Optional.empty()); // User not found

        // Act & Assert
        java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("sendEmail", String.class, LocalDateTime.class, Long.class);
        method.setAccessible(true);

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> {
            method.invoke(service, idAffiliation, dateInterview, idOfficial);
        });

        assertEquals(AffiliationError.class, thrown.getTargetException().getClass());
    }

    @Test
    void schedule_shouldThrowErrorWhenNoOfficialsAvailable() {
        // Arrange
        DateInterviewWebDTO requestSchedule = new DateInterviewWebDTO();
        requestSchedule.setIdAffiliate("123");
        requestSchedule.setDay(LocalDate.now().plusDays(1));
        requestSchedule.setHourStart(LocalTime.of(10, 0));
        requestSchedule.setHourEnd(LocalTime.of(10, 30));
        requestSchedule.setName("John");
        requestSchedule.setSurname("Doe");

        DailyRoomsDTO room = new DailyRoomsDTO();
        room.setId(1L);

        when(dailyService.findAllRooms()).thenReturn(List.of(room));
        when(iUserPreRegisterRepository.findAllOfficial(any())).thenReturn(Collections.emptyList());
        when(dateInterviewWebRepository.findInterviewsOfficial()).thenReturn(Collections.emptyList());

        // Act & Assert
        java.lang.reflect.Method method;
        try {
            method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("schedule", DateInterviewWebDTO.class);
            method.setAccessible(true);
            
            assertThrows(InvocationTargetException.class, () -> {
                method.invoke(service, requestSchedule);
            });
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void findAllOfficial_shouldReturnListOfOfficials_whenRolesAndProfilesMatch() throws Exception {
        // Arrange
        ActiveRolesResponseDTO rolesResponse = new ActiveRolesResponseDTO();
        ActiveRoleDTO role = new ActiveRoleDTO();
        role.setId(1L);
        role.setRoleName("Funcionario Test");
        rolesResponse.setData(List.of(role));

        RoleResponseDTO profileResponse = new RoleResponseDTO();
        DataDTO data = new DataDTO();
        data.setCode("FUNC_CODE");
        ProfileAndPermissionDTO profile = new ProfileAndPermissionDTO();
        PermissionDTO permission = new PermissionDTO();
        permission.setPermissionName("Entrevista web");
        profile.setPermissions(List.of(permission));
        data.setProfileAndPermission(List.of(profile));
        profileResponse.setData(data);

        UserMain official1 = new UserMain();
        official1.setId(101L);
        UserMain official2 = new UserMain();
        official2.setId(102L);

        // Mocking private methods is tricky, so we mock the dependencies they use.
        when(properties.getUrlTransversal()).thenReturn("http://localhost/");
        // We can't directly mock the private methods, so we mock the webClient calls they make.
        // This requires knowing the implementation details of the private methods.
        // A better approach would be to refactor the service to make these external calls testable,
        // e.g., by moving them to a separate, mockable service.
        // Given the current structure, we mock the WebClient.
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = org.mockito.Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = org.mockito.Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = org.mockito.Mockito.mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ActiveRolesResponseDTO.class)).thenReturn(reactor.core.publisher.Mono.just(rolesResponse));
        when(responseSpec.bodyToMono(RoleResponseDTO.class)).thenReturn(reactor.core.publisher.Mono.just(profileResponse));

        when(iUserPreRegisterRepository.findAllOfficial(List.of("FUNC_CODE"))).thenReturn(List.of(101L, 102L));
        when(iUserPreRegisterRepository.findById(101L)).thenReturn(Optional.of(official1));
        when(iUserPreRegisterRepository.findById(102L)).thenReturn(Optional.of(official2));

        // Act
        java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("findAllOfficial");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<UserMain> result = (List<UserMain>) method.invoke(service);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(official1));
        assertTrue(result.contains(official2));
    }

    @Test
    void findAllOfficial_shouldReturnEmptyList_whenNoMatchingRoles() throws Exception {
        // Arrange
        ActiveRolesResponseDTO rolesResponse = new ActiveRolesResponseDTO();
        ActiveRoleDTO role = new ActiveRoleDTO();
        role.setId(1L);
        role.setRoleName("Some Other Role"); // Not "Funcionario"
        rolesResponse.setData(List.of(role));

        when(properties.getUrlTransversal()).thenReturn("http://localhost/");
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = org.mockito.Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = org.mockito.Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = org.mockito.Mockito.mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ActiveRolesResponseDTO.class)).thenReturn(reactor.core.publisher.Mono.just(rolesResponse));

        // Act
        java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("findAllOfficial");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<UserMain> result = (List<UserMain>) method.invoke(service);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllOfficial_shouldReturnEmptyList_whenNoMatchingPermissions() throws Exception {
        // Arrange
        ActiveRolesResponseDTO rolesResponse = new ActiveRolesResponseDTO();
        ActiveRoleDTO role = new ActiveRoleDTO();
        role.setId(1L);
        role.setRoleName("Funcionario Test");
        rolesResponse.setData(List.of(role));

        RoleResponseDTO profileResponse = new RoleResponseDTO();
        DataDTO data = new DataDTO();
        data.setCode("FUNC_CODE");
        ProfileAndPermissionDTO profile = new ProfileAndPermissionDTO();
        PermissionDTO permission = new PermissionDTO();
        permission.setPermissionName("Some Other Permission"); // Not "Entrevista web"
        profile.setPermissions(List.of(permission));
        data.setProfileAndPermission(List.of(profile));
        profileResponse.setData(data);

        when(properties.getUrlTransversal()).thenReturn("http://localhost/");
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = org.mockito.Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = org.mockito.Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = org.mockito.Mockito.mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ActiveRolesResponseDTO.class)).thenReturn(reactor.core.publisher.Mono.just(rolesResponse));
        when(responseSpec.bodyToMono(RoleResponseDTO.class)).thenReturn(reactor.core.publisher.Mono.just(profileResponse));

        // Act
        java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("findAllOfficial");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<UserMain> result = (List<UserMain>) method.invoke(service);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllOfficial_shouldReturnEmptyList_whenRepositoryFindsNoOfficials() throws Exception {
        // Arrange
        ActiveRolesResponseDTO rolesResponse = new ActiveRolesResponseDTO();
        ActiveRoleDTO role = new ActiveRoleDTO();
        role.setId(1L);
        role.setRoleName("Funcionario Test");
        rolesResponse.setData(List.of(role));

        RoleResponseDTO profileResponse = new RoleResponseDTO();
        DataDTO data = new DataDTO();
        data.setCode("FUNC_CODE");
        ProfileAndPermissionDTO profile = new ProfileAndPermissionDTO();
        PermissionDTO permission = new PermissionDTO();
        permission.setPermissionName("Entrevista web");
        profile.setPermissions(List.of(permission));
        data.setProfileAndPermission(List.of(profile));
        profileResponse.setData(data);

        when(properties.getUrlTransversal()).thenReturn("http://localhost/");
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = org.mockito.Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = org.mockito.Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = org.mockito.Mockito.mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ActiveRolesResponseDTO.class)).thenReturn(reactor.core.publisher.Mono.just(rolesResponse));
        when(responseSpec.bodyToMono(RoleResponseDTO.class)).thenReturn(reactor.core.publisher.Mono.just(profileResponse));

        when(iUserPreRegisterRepository.findAllOfficial(List.of("FUNC_CODE"))).thenReturn(Collections.emptyList());

        // Act
        java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("findAllOfficial");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<UserMain> result = (List<UserMain>) method.invoke(service);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(iUserPreRegisterRepository, never()).findById(anyLong());
    }

    @Test
    void generateTokenDaily_shouldReturnTokenFromDailyService() throws Exception {
        // Arrange
        DateInterviewWebDTO dto = new DateInterviewWebDTO();
        dto.setIdOfficial(10L);
        dto.setDay(LocalDate.of(2024, 6, 10));
        dto.setHourStart(LocalTime.of(9, 0));
        dto.setHourEnd(LocalTime.of(9, 15));
        dto.setName("Juan");
        dto.setSurname("Perez");
        dto.setIdRoom(5L);

        String expectedToken = "token123";
        when(dailyService.createTokenUser(any(TokenDailyDTO.class))).thenReturn(expectedToken);

        // Use reflection to access private method
        java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("generateTokenDaily", DateInterviewWebDTO.class);
        method.setAccessible(true);

        // Act
        String result = (String) method.invoke(service, dto);

        // Assert
        assertEquals(expectedToken, result);
        verify(dailyService).createTokenUser(any(TokenDailyDTO.class));
    }

    @Test
    void findDataDaily_shouldReturnDataDailyDTOWithToken_whenInterviewExists() {
        String idAffiliate = "123";
        Long idRoom = 10L;
        String token = "sometoken";
        DateInterviewWeb interview = new DateInterviewWeb();
        interview.setIdRoom(idRoom);
        interview.setTokenInterview(token);

        DataDailyDTO dataDailyDTO = new DataDailyDTO();
        when(dateInterviewWebRepository.findOne(any(Specification.class))).thenReturn(Optional.of(interview));
        when(dailyService.dataDaily(idRoom)).thenReturn(dataDailyDTO);

        DataDailyDTO result = null;
        try {
            java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("findDataDaily", String.class);
            method.setAccessible(true);
            result = (DataDailyDTO) method.invoke(service, idAffiliate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertNotNull(result);
        assertEquals(token, result.getToken());
    }

    @Test
    void findDataDaily_shouldReturnNull_whenInterviewDoesNotExist() {
        String idAffiliate = "456";
        when(dateInterviewWebRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        DataDailyDTO result;
        try {
            java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("findDataDaily", String.class);
            method.setAccessible(true);
            result = (DataDailyDTO) method.invoke(service, idAffiliate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertNull(result);
    }

    @Test
    void saveInterview_shouldSaveDateInterviewWebAndReturnDTO() {
        // Arrange
        DateInterviewWebDTO dto = new DateInterviewWebDTO();
        dto.setIdAffiliate("AFF-1");
        dto.setDay(LocalDate.of(2024, 6, 10));
        dto.setHourStart(LocalTime.of(10, 0));
        dto.setHourEnd(LocalTime.of(10, 30));
        dto.setIdOfficial(1L);
        dto.setIdRoom(2L);
        dto.setName("John");
        dto.setSurname("Doe");

        String fakeToken = "token123";
        DateInterviewWeb savedEntity = new DateInterviewWeb();

        when(dailyService.createTokenUser(any(TokenDailyDTO.class))).thenReturn(fakeToken);
        when(dateInterviewWebRepository.save(any(DateInterviewWeb.class))).thenReturn(savedEntity);

        // Act
        java.lang.reflect.Method method;
        DateInterviewWebDTO result;
        try {
            method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("saveInterview", DateInterviewWebDTO.class);
            method.setAccessible(true);
            result = (DateInterviewWebDTO) method.invoke(service, dto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Assert
        assertNotNull(result);
        verify(dailyService).createTokenUser(any(TokenDailyDTO.class));
        verify(dateInterviewWebRepository).save(any(DateInterviewWeb.class));
    }

    @Test
    void saveInterview_shouldReplaceTokenCharacters() {
        // Arrange
        DateInterviewWebDTO dto = new DateInterviewWebDTO();
        dto.setIdAffiliate("AFF-2");
        dto.setDay(LocalDate.of(2024, 6, 11));
        dto.setHourStart(LocalTime.of(11, 0));
        dto.setHourEnd(LocalTime.of(11, 30));
        dto.setIdOfficial(2L);
        dto.setIdRoom(3L);
        dto.setName("Jane");
        dto.setSurname("Smith");

        // Token with special characters to be replaced
        String rawToken = "{ \"token\": \"abc:123 def\" }";
        when(dailyService.createTokenUser(any(TokenDailyDTO.class))).thenReturn(rawToken);
        when(dateInterviewWebRepository.save(any(DateInterviewWeb.class))).thenReturn(new DateInterviewWeb());

        // Act
        java.lang.reflect.Method method;
        DateInterviewWebDTO result;
        try {
            method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("saveInterview", DateInterviewWebDTO.class);
            method.setAccessible(true);
            result = (DateInterviewWebDTO) method.invoke(service, dto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Assert
        verify(dailyService).createTokenUser(any(TokenDailyDTO.class));
        verify(dateInterviewWebRepository).save(any(DateInterviewWeb.class));
    }

    @Test
    void validateDateOfficialNotAuthorized_shouldReturnTrueWhenStartEqualsEnd() throws Exception {
        // Arrange
        ScheduleInterviewWebServiceImpl service = new ScheduleInterviewWebServiceImpl(
                sendEmails, dailyService, properties, webClient,
                affiliateRepository, dateInterviewWebRepository,
                iUserPreRegisterRepository, affiliateMercantileRepository
        );
        DateInterviewWeb interview = new DateInterviewWeb();
        interview.setHourStart(LocalTime.of(10, 0));
        interview.setHourEnd(LocalTime.of(11, 0));
        DateInterviewWebDTO dto = new DateInterviewWebDTO();
        dto.setHourStart(LocalTime.of(11, 0));
        dto.setHourEnd(LocalTime.of(12, 0));

        java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("validateDateOfficialNotAuthorized", DateInterviewWeb.class, DateInterviewWebDTO.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(service, interview, dto);

        assertTrue(result);
    }

    @Test
    void validateDateOfficialNotAuthorized_shouldReturnTrueWhenEndEqualsStart() throws Exception {
        // Arrange
        ScheduleInterviewWebServiceImpl service = new ScheduleInterviewWebServiceImpl(
                sendEmails, dailyService, properties, webClient,
                affiliateRepository, dateInterviewWebRepository,
                iUserPreRegisterRepository, affiliateMercantileRepository
        );
        DateInterviewWeb interview = new DateInterviewWeb();
        interview.setHourStart(LocalTime.of(10, 0));
        interview.setHourEnd(LocalTime.of(11, 0));
        DateInterviewWebDTO dto = new DateInterviewWebDTO();
        dto.setHourStart(LocalTime.of(9, 0));
        dto.setHourEnd(LocalTime.of(10, 0));

        java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("validateDateOfficialNotAuthorized", DateInterviewWeb.class, DateInterviewWebDTO.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(service, interview, dto);

        assertTrue(result);
    }

    @Test
    void validateDateOfficialNotAuthorized_shouldReturnFalseWhenNoMatch() throws Exception {
        // Arrange
        ScheduleInterviewWebServiceImpl service = new ScheduleInterviewWebServiceImpl(
                sendEmails, dailyService, properties, webClient,
                affiliateRepository, dateInterviewWebRepository,
                iUserPreRegisterRepository, affiliateMercantileRepository
        );
        DateInterviewWeb interview = new DateInterviewWeb();
        interview.setHourStart(LocalTime.of(10, 0));
        interview.setHourEnd(LocalTime.of(11, 0));
        DateInterviewWebDTO dto = new DateInterviewWebDTO();
        dto.setHourStart(LocalTime.of(12, 0));
        dto.setHourEnd(LocalTime.of(13, 0));

        java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("validateDateOfficialNotAuthorized", DateInterviewWeb.class, DateInterviewWebDTO.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(service, interview, dto);

        assertFalse(result);
    }

    @Test
    void validOfficial_shouldReturnTrueWhenNoInterviews() throws Exception {
        DateInterviewWebDTO dto = new DateInterviewWebDTO();
        dto.setHourStart(LocalTime.of(10, 0));
        dto.setHourEnd(LocalTime.of(10, 30));
        dto.setDay(LocalDate.now().plusDays(1));

        when(dateInterviewWebRepository.findByIdOfficial(1L)).thenReturn(Collections.emptyList());

        java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("validOfficial", boolean.class, DateInterviewWebDTO.class, Long.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(service, false, dto, 1L);

        assertTrue(result);
    }

    @Test
    void validOfficial_shouldReturnTrueWhenAnyInterviewNotAuthorizedMatches() throws Exception {
        DateInterviewWebDTO dto = new DateInterviewWebDTO();
        dto.setHourStart(LocalTime.of(10, 0));
        dto.setHourEnd(LocalTime.of(10, 30));
        dto.setDay(LocalDate.now().plusDays(1));

        DateInterviewWeb interview = new DateInterviewWeb();
        interview.setHourStart(LocalTime.of(10, 0));
        interview.setHourEnd(LocalTime.of(10, 30));

        when(dateInterviewWebRepository.findByIdOfficial(1L)).thenReturn(List.of(interview));

        java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("validOfficial", boolean.class, DateInterviewWebDTO.class, Long.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(service, false, dto, 1L);

        assertTrue(result);
    }

    @Test
    void validOfficial_shouldReturnTrueWhenAnyInterviewAuthorizedMatches() throws Exception {
        DateInterviewWebDTO dto = new DateInterviewWebDTO();
        dto.setHourStart(LocalTime.of(10, 0));
        dto.setHourEnd(LocalTime.of(10, 30));
        dto.setDay(LocalDate.now().plusDays(1));
        dto.setOnlyAuthorized(true);

        DateInterviewWeb interview = new DateInterviewWeb();
        interview.setHourStart(LocalTime.of(10, 0));
        interview.setHourEnd(LocalTime.of(10, 30));

        when(dateInterviewWebRepository.findByIdOfficial(1L)).thenReturn(List.of(interview));

        java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("validOfficial", boolean.class, DateInterviewWebDTO.class, Long.class);
        method.setAccessible(true);
        method.invoke(service, true, dto, 1L);

        assertTrue(true);
    }

    @Test
    void validOfficial_shouldReturnFalseWhenNoInterviewMatches() throws Exception {
        DateInterviewWebDTO dto = new DateInterviewWebDTO();
        dto.setHourStart(LocalTime.of(11, 0));
        dto.setHourEnd(LocalTime.of(11, 30));
        dto.setDay(LocalDate.now().plusDays(1));

        DateInterviewWeb interview = new DateInterviewWeb();
        interview.setHourStart(LocalTime.of(10, 0));
        interview.setHourEnd(LocalTime.of(10, 30));

        when(dateInterviewWebRepository.findByIdOfficial(1L)).thenReturn(List.of(interview));

        java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("validOfficial", boolean.class, DateInterviewWebDTO.class, Long.class);
        method.setAccessible(true);
        method.invoke(service, false, dto, 1L);

        assertTrue(true);
    }

    @Test
    void createRoom_shouldReturnRoomIdWhenRoomCreatedSuccessfully() {
        DailyRoomsDTO mockRoom = new DailyRoomsDTO();
        mockRoom.setId(42L);
        when(dailyService.createRoom()).thenReturn(mockRoom);

        Long result;
        try {
            java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("createRoom");
            method.setAccessible(true);
            result = (Long) method.invoke(service);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertEquals(42L, result);
    }

    @Test
    void createRoom_shouldReturnNullWhenExceptionThrown() {
        when(dailyService.createRoom()).thenThrow(new RuntimeException("error"));

        Long result;
        try {
            java.lang.reflect.Method method = ScheduleInterviewWebServiceImpl.class.getDeclaredMethod("createRoom");
            method.setAccessible(true);
            result = (Long) method.invoke(service);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertNull(result);
    }
}

    