package com.gal.afiliaciones.application.service.independentcontractor.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gal.afiliaciones.domain.model.independentcontractor.ContractQuality;
import com.gal.afiliaciones.domain.model.independentcontractor.ContractType;
import com.gal.afiliaciones.domain.model.independentcontractor.IndependentContractor;
import com.gal.afiliaciones.infrastructure.dao.repository.independentcontractor.IndependentContractorDao;
import com.gal.afiliaciones.infrastructure.dto.independentcontractor.IndependentContractorDTO;



class IndependentContractorServiceImplTest {

    private IndependentContractorDao independentContractorDao;
    private IndependentContractorServiceImpl service;

    @BeforeEach
    void setUp() {
        independentContractorDao = mock(IndependentContractorDao.class);
        service = new IndependentContractorServiceImpl(independentContractorDao);
    }

    @Test
    void testFindById_Found() {
        IndependentContractor contractor = new IndependentContractor();
        contractor.setId(1L);
        contractor.setDescription("Test Desc");

        when(independentContractorDao.findById(1L)).thenReturn(Optional.of(contractor));

        Optional<IndependentContractorDTO> result = service.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Test Desc", result.get().getDescription());
    }

    @Test
    void testFindById_NotFound() {
        when(independentContractorDao.findById(2L)).thenReturn(Optional.empty());

        Optional<IndependentContractorDTO> result = service.findById(2L);

        assertFalse(result.isPresent());
    }

    @Test
    void testFindAll() {
        IndependentContractor contractor1 = new IndependentContractor();
        contractor1.setId(1L);
        contractor1.setDescription("Desc1");

        IndependentContractor contractor2 = new IndependentContractor();
        contractor2.setId(2L);
        contractor2.setDescription("Desc2");

        when(independentContractorDao.findAll()).thenReturn(Arrays.asList(contractor1, contractor2));

        List<IndependentContractorDTO> result = service.findAll();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Desc1", result.get(0).getDescription());
        assertEquals(2L, result.get(1).getId());
        assertEquals("Desc2", result.get(1).getDescription());
    }

    @Test
    void testFindAllContractQuality() {
        ContractQuality quality1 = new ContractQuality();
        ContractQuality quality2 = new ContractQuality();

        when(independentContractorDao.findAllContractQuality()).thenReturn(Arrays.asList(quality1, quality2));

        List<ContractQuality> result = service.findAllContractQuality();

        assertEquals(2, result.size());
        assertSame(quality1, result.get(0));
        assertSame(quality2, result.get(1));
    }

    @Test
    void testFindAllContractType() {
        ContractType type1 = new ContractType();
        ContractType type2 = new ContractType();

        when(independentContractorDao.findAllContractType()).thenReturn(Arrays.asList(type1, type2));

        List<ContractType> result = service.findAllContractType();

        assertEquals(2, result.size());
        assertSame(type1, result.get(0));
        assertSame(type2, result.get(1));
    }
}