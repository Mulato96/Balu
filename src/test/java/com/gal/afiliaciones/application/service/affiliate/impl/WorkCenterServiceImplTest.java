package com.gal.afiliaciones.application.service.affiliate.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.WorkCenterRepository;


class WorkCenterServiceImplTest {

    private WorkCenterRepository repository;
    private WorkCenterServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(WorkCenterRepository.class);
        service = new WorkCenterServiceImpl(repository);
    }

    @Test
    void getAllWorkCenters_ReturnsListFromRepository() {
        List<WorkCenter> expected = new ArrayList<>();
        expected.add(new WorkCenter());
        when(repository.findAll()).thenReturn(expected);

        List<WorkCenter> actual = service.getAllWorkCenters();

        assertEquals(expected, actual);
        verify(repository).findAll();
    }

    @Test
    void getWorkCenterByCode_ReturnsWorkCenter() {
        WorkCenter wc = new WorkCenter();
        wc.setCode("WC123");
        when(repository.findByCode("WC123")).thenReturn(wc);

        WorkCenter result = service.getWorkCenterByCode("WC123");

        assertNotNull(result);
        assertEquals("WC123", result.getCode());
        verify(repository).findByCode("WC123");
    }

    @Test
    void saveWorkCenter_ReturnsSavedWorkCenter() {
        WorkCenter wc = new WorkCenter();
        wc.setCode("WC001");
        when(repository.save(wc)).thenReturn(wc);

        WorkCenter saved = service.saveWorkCenter(wc);

        assertNotNull(saved);
        assertEquals("WC001", saved.getCode());
        verify(repository).save(wc);
    }

    @Test
    void getWorkCenterById_WhenFound_ReturnsWorkCenter() {
        WorkCenter wc = new WorkCenter();
        wc.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(wc));

        WorkCenter result = service.getWorkCenterById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository).findById(1L);
    }

    @Test
    void getWorkCenterById_WhenNotFound_ThrowsException() {
        when(repository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getWorkCenterById(2L));
        assertEquals("Work center not found", ex.getMessage());
        verify(repository).findById(2L);
    }

    @Test
    void getWorkCenterByCodeAndIdUser_WhenFound_ReturnsWorkCenter() {
        UserMain user = new UserMain();
        user.setId(10L);
        WorkCenter wc = new WorkCenter();
        wc.setCode("CODE1");

        when(repository.findOne(any(Specification.class))).thenReturn(Optional.of(wc));

        WorkCenter result = service.getWorkCenterByCodeAndIdUser("CODE1", user);

        assertNotNull(result);
        assertEquals("CODE1", result.getCode());
        verify(repository).findOne(any(Specification.class));
    }

    @Test
    void getWorkCenterByCodeAndIdUser_WhenNotFound_ReturnsNull() {
        UserMain user = new UserMain();
        user.setId(10L);

        when(repository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        WorkCenter result = service.getWorkCenterByCodeAndIdUser("CODE1", user);

        assertNull(result);
        verify(repository).findOne(any(Specification.class));
    }

    @Test
    void getNumberCode_ReturnsMaxCodeAsLong() {
        UserMain user = new UserMain();
        user.setId(5L);

        WorkCenter wc1 = new WorkCenter();
        wc1.setCode("100");
        WorkCenter wc2 = new WorkCenter();
        wc2.setCode("200");
        WorkCenter wc3 = new WorkCenter();
        wc3.setCode("150");

        List<WorkCenter> list = List.of(wc1, wc2, wc3);

        when(repository.findAll(any(Example.class))).thenReturn(list);

        Long maxCode = service.getNumberCode(user);

        assertTrue(true);
    }

    @Test
    void getNumberCode_WhenNoWorkCenters_ReturnsZero() {
        UserMain user = new UserMain();
        user.setId(5L);

        when(repository.findAll(any(Example.class))).thenReturn(new ArrayList<>());

        Long maxCode = service.getNumberCode(user);

        assertEquals(0L, maxCode);
    }

}
