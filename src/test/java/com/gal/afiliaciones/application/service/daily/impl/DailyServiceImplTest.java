package com.gal.afiliaciones.application.service.daily.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;

import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.infrastructure.dao.repository.DateInterviewWebRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dto.daily.DailyRoomsDTO;

import reactor.core.publisher.Mono;


class DailyServiceImplTest {

    @Mock
    private WebClient webClient;
    @Mock
    private CollectProperties properties;
    @Mock
    private DateInterviewWebRepository dateInterviewWebRepository;
    @Mock
    private IUserPreRegisterRepository iUserPreRegisterRepository;

    @InjectMocks
    private DailyServiceImpl dailyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Helper for mocking WebClient calls
    private <T> void mockWebClientPost(String url, T response, Class<T> clazz) {
        WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(url)).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(clazz)).thenReturn(Mono.just(response));
    }

    @Test
    void testCreateRoom_success() {
        String url = "http://test/daily/createRoom";
        DailyRoomsDTO expected = new DailyRoomsDTO();
        when(properties.getUrlTransversal()).thenReturn("http://test/");
        mockWebClientPost(url, expected, DailyRoomsDTO.class);

        DailyRoomsDTO result = dailyService.createRoom();
        assertNotNull(result);
    }

    @Test
    void testCreateTokenOfficial_userNotFound() {
        Long idOfficial = 2L;
        when(iUserPreRegisterRepository.findById(idOfficial)).thenReturn(Optional.empty());
        assertThrows(AffiliationError.class, () -> dailyService.createTokenOfficial(idOfficial));
    }

    @Test
    void testStartMeet_noInterviews() {
        Long idOfficial = 1L;
        when(dateInterviewWebRepository.findAll()).thenReturn(Collections.emptyList());
        assertThrows(AffiliationError.class, () -> dailyService.startMeet(idOfficial));
    }
}