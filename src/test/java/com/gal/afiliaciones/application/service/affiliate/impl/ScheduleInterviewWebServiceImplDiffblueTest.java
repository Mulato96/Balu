package com.gal.afiliaciones.application.service.affiliate.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.daily.DailyService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.DateInterviewWeb;
import com.gal.afiliaciones.infrastructure.dao.repository.DateInterviewWebRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DateInterviewWebDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
@TestPropertySource(locations = "classpath:application.properties")
@ContextConfiguration(classes = {ScheduleInterviewWebServiceImpl.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class ScheduleInterviewWebServiceImplDiffblueTest {
    @MockBean
    private DateInterviewWebDTO dateInterviewWebDTO;

    @MockBean
    private SendEmails sendEmails;

    @MockBean
    private DailyService dailyService;

    @MockBean
    private CollectProperties properties;

    @MockBean
    private WebClient webClient;

    @MockBean
    private AffiliateRepository affiliateRepository;

    @MockBean
    private DateInterviewWebRepository dateInterviewWebRepository;

    @MockBean
    private IUserPreRegisterRepository iUserPreRegisterRepository;

    @MockBean
    private AffiliateMercantileRepository affiliateMercantileRepository;

    @Autowired
    private Map<String, DateInterviewWebDTO> map;

    @Autowired
    private ScheduleInterviewWebServiceImpl scheduleInterviewWebServiceImpl;

    /**
     * Method under test:
     * {@link ScheduleInterviewWebServiceImpl#createScheduleInterviewWeb(DateInterviewWebDTO)}
     */
    @Test
    void testCreateScheduleInterviewWeb() {
        // Arrange
        when(dateInterviewWebDTO.getHourStart()).thenReturn(LocalTime.MIDNIGHT);
        when(dateInterviewWebDTO.getHourEnd()).thenReturn(LocalTime.MIDNIGHT);
        when(dateInterviewWebDTO.getDay()).thenReturn(LocalDate.of(1970, 1, 1));
        when(dateInterviewWebDTO.getIdAffiliate()).thenReturn("Id Affiliate");

        // Act and Assert
        assertThrows(AffiliationError.class,
                () -> scheduleInterviewWebServiceImpl.createScheduleInterviewWeb(dateInterviewWebDTO));
    }

    /**
     * Method under test:
     * {@link ScheduleInterviewWebServiceImpl#createScheduleInterviewWeb(DateInterviewWebDTO)}
     */
    @Test
    void testCreateScheduleInterviewWeb2() {
        // Arrange
        when(dateInterviewWebDTO.getHourStart()).thenThrow(new AffiliationError("Not all who wander are lost"));
        when(dateInterviewWebDTO.getHourEnd()).thenReturn(LocalTime.MIDNIGHT);
        when(dateInterviewWebDTO.getDay()).thenReturn(LocalDate.of(1970, 1, 1));
        when(dateInterviewWebDTO.getIdAffiliate()).thenReturn("Id Affiliate");

        // Act and Assert
        assertThrows(AffiliationError.class,
                () -> scheduleInterviewWebServiceImpl.createScheduleInterviewWeb(dateInterviewWebDTO));
    }

    /**
     * Method under test:
     * {@link ScheduleInterviewWebServiceImpl#createScheduleInterviewWeb(DateInterviewWebDTO)}
     */
    @Test
    void testCreateScheduleInterviewWeb3() {
        // Arrange
        when(dateInterviewWebDTO.getHourStart()).thenReturn(LocalTime.MIDNIGHT);
        when(dateInterviewWebDTO.getHourEnd()).thenReturn(null);
        when(dateInterviewWebDTO.getDay()).thenReturn(null);
        when(dateInterviewWebDTO.getIdAffiliate()).thenReturn(null);
        when(dateInterviewWebDTO.getDay()).thenReturn(LocalDate.of(1970, 1, 1));
        when(dateInterviewWebDTO.getHourEnd()).thenReturn(LocalTime.MIDNIGHT);
        when(dateInterviewWebDTO.getHourStart()).thenReturn(LocalTime.MIDNIGHT);

        // Act and Assert
        assertThrows(AffiliationError.class,
                () -> scheduleInterviewWebServiceImpl.createScheduleInterviewWeb(dateInterviewWebDTO));
    }

    /**
     * Method under test:
     * {@link ScheduleInterviewWebServiceImpl#createScheduleInterviewWeb(DateInterviewWebDTO)}
     */
    @Test
    void testCreateScheduleInterviewWeb4() {
        // Arrange
        when(dateInterviewWebDTO.getHourStart()).thenReturn(LocalTime.MIDNIGHT);
        when(dateInterviewWebDTO.getHourEnd()).thenReturn(null);
        when(dateInterviewWebDTO.getDay()).thenReturn(null);
        when(dateInterviewWebDTO.getIdAffiliate()).thenReturn("foo");
        when(dateInterviewWebDTO.getDay()).thenThrow(new AffiliationError("Not all who wander are lost"));
        when(dateInterviewWebDTO.getHourEnd()).thenReturn(LocalTime.MIDNIGHT);
        when(dateInterviewWebDTO.getHourStart()).thenReturn(LocalTime.MIDNIGHT);

        // Act and Assert
        assertThrows(AffiliationError.class,
                () -> scheduleInterviewWebServiceImpl.createScheduleInterviewWeb(dateInterviewWebDTO));
        verify(dateInterviewWebDTO, atLeast(1)).getDay();
    }

    /**
     * Method under test:
     * {@link ScheduleInterviewWebServiceImpl#listScheduleInterviewWeb()}
     */
    @Test
    void testListScheduleInterviewWeb() {
        // Arrange and Act
        List<DateInterviewWeb> actualListScheduleInterviewWebResult = scheduleInterviewWebServiceImpl
                .listScheduleInterviewWeb();

        // Assert
        assertEquals(0, actualListScheduleInterviewWebResult.size());

    }

}
