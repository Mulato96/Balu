package com.gal.afiliaciones.application.service.identificationlegalnature.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gal.afiliaciones.domain.model.IdentificationLegalNature;
import com.gal.afiliaciones.infrastructure.dao.repository.IdentificationLegalNatureRepository;
import com.gal.afiliaciones.infrastructure.dto.IdentificationLegalNatureDTO;



class IdentificationLegalNatureServiceImplTest {

    private IdentificationLegalNatureRepository identificationLegalNatureRepository;
    private IdentificationLegalNatureServiceImpl service;

    @BeforeEach
    void setUp() {
        identificationLegalNatureRepository = mock(IdentificationLegalNatureRepository.class);
        service = new IdentificationLegalNatureServiceImpl(identificationLegalNatureRepository);
    }

    @Test
    void testCreate_shouldSaveAndReturnDTOs() {
        IdentificationLegalNatureDTO dto1 = new IdentificationLegalNatureDTO();
        IdentificationLegalNatureDTO dto2 = new IdentificationLegalNatureDTO();
        // Optionally set fields on DTOs here

        IdentificationLegalNature entity1 = new IdentificationLegalNature();
        IdentificationLegalNature entity2 = new IdentificationLegalNature();
        // Optionally set fields on entities here

        List<IdentificationLegalNature> savedEntities = Arrays.asList(entity1, entity2);

        when(identificationLegalNatureRepository.saveAll(anyList())).thenReturn(savedEntities);

        List<IdentificationLegalNatureDTO> result = service.create(Arrays.asList(dto1, dto2));

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(identificationLegalNatureRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testFindByNit_whenPresent_shouldReturnTrue() {
        String nit = "12345";
        when(identificationLegalNatureRepository.findByNit(nit)).thenReturn(Optional.of(new IdentificationLegalNature()));

        boolean exists = service.findByNit(nit);

        assertTrue(exists);
        verify(identificationLegalNatureRepository, times(1)).findByNit(nit);
    }

    @Test
    void testFindByNit_whenNotPresent_shouldReturnFalse() {
        String nit = "67890";
        when(identificationLegalNatureRepository.findByNit(nit)).thenReturn(Optional.empty());

        boolean exists = service.findByNit(nit);

        assertFalse(exists);
        verify(identificationLegalNatureRepository, times(1)).findByNit(nit);
    }
}