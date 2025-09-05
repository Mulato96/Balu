package com.gal.afiliaciones.application.service.observationsaffiliation.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.domain.model.ObservationsAffiliation;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.observationsaffiliation.ObservationsAffiliationRepository;
import com.gal.afiliaciones.infrastructure.dto.observationsaffiliation.ObservationAffiliationDTO;

class ObservationsAffiliationServiceImplTest {

    @Mock
    private IUserPreRegisterRepository iUserPreRegisterRepository;
    @Mock
    private ObservationsAffiliationRepository observationsAffiliationRepository;

    @InjectMocks
    private ObservationsAffiliationServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_shouldReturnDTO_whenValidInput() {
        String observation = "Valid observation";
        String filedNumber = "12345";
        String reasonReject = "Reason";
        Long idOfficial = 1L;

        ObservationsAffiliation saved = new ObservationsAffiliation();
        saved.setObservations(observation);
        saved.setFiledNumber(filedNumber);
        saved.setDate(LocalDateTime.now());
        saved.setReasonReject(reasonReject);
        saved.setIdOfficial(idOfficial);

        UserMain user = new UserMain();
        user.setFirstName("John");
        user.setSurname("Doe");

        when(observationsAffiliationRepository.save(any())).thenReturn(saved);
        when(iUserPreRegisterRepository.findById(idOfficial)).thenReturn(Optional.of(user));

        ObservationAffiliationDTO dto = service.create(observation, filedNumber, reasonReject, idOfficial);

        assertEquals(observation, dto.getObservations());
        assertEquals(filedNumber, dto.getFiledNumber());
        assertEquals("John Doe", dto.getNameOfficial());
        assertEquals("Rechazo", dto.getState());
        assertEquals(reasonReject, dto.getReasonReject());
        assertNotNull(dto.getDate());
    }

    @Test
    void create_shouldThrowAffiliationError_whenObservationTooLong() {
        String longObservation = "a".repeat(300);
        assertThrows(AffiliationError.class, () ->
                service.create(longObservation, "123", "reason", 1L));
    }


    @Test
    void findByFiledNumber_shouldReturnListOfDTOs() {
        String filedNumber = "12345";
        Long idOfficial = 2L;

        ObservationsAffiliation obs = new ObservationsAffiliation();
        obs.setObservations("Obs1");
        obs.setFiledNumber(filedNumber);
        obs.setDate(LocalDateTime.now());
        obs.setReasonReject("Reason1");
        obs.setIdOfficial(idOfficial);

        UserMain user = new UserMain();
        user.setFirstName("Jane");
        user.setSurname("Smith");

        when(observationsAffiliationRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(obs));
        when(iUserPreRegisterRepository.findById(idOfficial)).thenReturn(Optional.of(user));

        List<ObservationAffiliationDTO> result = service.findByFiledNumber(filedNumber);

        assertEquals(1, result.size());
        ObservationAffiliationDTO dto = result.get(0);
        assertEquals("Obs1", dto.getObservations());
        assertEquals(filedNumber, dto.getFiledNumber());
        assertEquals("Jane Smith", dto.getNameOfficial());
        assertEquals("Rechazo", dto.getState());
        assertEquals("Reason1", dto.getReasonReject());
        assertNotNull(dto.getDate());
    }

    @Test
    void findByFiledNumber_shouldReturnEmptyList_whenNoObservations() {
        when(observationsAffiliationRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList());
        List<ObservationAffiliationDTO> result = service.findByFiledNumber("no-filed");
        assertTrue(result.isEmpty());
    }
}