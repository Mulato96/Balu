package com.gal.afiliaciones.application.service.workingday.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.domain.model.WorkingDay;
import com.gal.afiliaciones.infrastructure.dao.repository.workingday.WorkingDayRepository;


class WorkingDayServiceImplTest {

    private WorkingDayRepository workingDayRepository;
    private WorkingDayServiceImpl workingDayService;

    @BeforeEach
    void setUp() {
        workingDayRepository = mock(WorkingDayRepository.class);
        workingDayService = new WorkingDayServiceImpl(workingDayRepository);
    }

    @Test
    void findByCode_shouldReturnWorkingDay_whenFound() {
        Long code = 123L;
        WorkingDay expectedWorkingDay = new WorkingDay();
        when(workingDayRepository.findOne(any(Specification.class))).thenReturn(Optional.of(expectedWorkingDay));

        WorkingDay result = workingDayService.findByCode(code);

        assertNotNull(result);
        assertEquals(expectedWorkingDay, result);

        ArgumentCaptor<Specification<WorkingDay>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(workingDayRepository).findOne(specCaptor.capture());
        Specification<WorkingDay> capturedSpec = specCaptor.getValue();
        assertNotNull(capturedSpec);
    }

    @Test
    void findByCode_shouldReturnNull_whenNotFound() {
        Long code = 456L;
        when(workingDayRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        WorkingDay result = workingDayService.findByCode(code);

        assertNull(result);
        verify(workingDayRepository).findOne(any(Specification.class));
    }

    @Test
    void findAll_shouldReturnListOfWorkingDays() {
        List<WorkingDay> expectedList = List.of(new WorkingDay(), new WorkingDay());
        when(workingDayRepository.findAll()).thenReturn(expectedList);

        List<WorkingDay> result = workingDayService.findAll();

        assertNotNull(result);
        assertEquals(expectedList.size(), result.size());
        assertEquals(expectedList, result);
        verify(workingDayRepository).findAll();
    }
}