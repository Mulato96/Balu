package com.gal.afiliaciones.application.service.occupation.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gal.afiliaciones.domain.model.Occupation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;

class OccupationServiceImplTest {

    private GenericWebClient webClient;
    private OccupationServiceImpl occupationService;

    @BeforeEach
    void setUp() {
        webClient = mock(GenericWebClient.class);
        occupationService = new OccupationServiceImpl(webClient);
    }

    @Test
    void findOccupationsProvisionService_shouldReturnFilteredOccupations() {
        Occupation occ1 = new Occupation();
        occ1.setCodeOccupation("1234");
        Occupation occ2 = new Occupation();
        occ2.setCodeOccupation("7111"); // excluded
        Occupation occ3 = new Occupation();
        occ3.setCodeOccupation("5678");

        List<Occupation> allOccupations = Arrays.asList(occ1, occ2, occ3);

        when(webClient.getAllOccupations()).thenReturn(allOccupations);

        List<Occupation> result = occupationService.findOccupationsProvisionService();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(o -> o.getCodeOccupation().equals("1234")));
        assertTrue(result.stream().anyMatch(o -> o.getCodeOccupation().equals("5678")));
        assertFalse(result.stream().anyMatch(o -> o.getCodeOccupation().equals("7111")));
    }

    @Test
    void findOccupationsProvisionService_shouldReturnEmptyListWhenNoOccupations() {
        when(webClient.getAllOccupations()).thenReturn(Collections.emptyList());

        List<Occupation> result = occupationService.findOccupationsProvisionService();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findOccupationsProvisionService_shouldReturnAllWhenNoneExcluded() {
        Occupation occ1 = new Occupation();
        occ1.setCodeOccupation("2000");
        Occupation occ2 = new Occupation();
        occ2.setCodeOccupation("3000");

        List<Occupation> allOccupations = Arrays.asList(occ1, occ2);

        when(webClient.getAllOccupations()).thenReturn(allOccupations);

        List<Occupation> result = occupationService.findOccupationsProvisionService();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(o -> o.getCodeOccupation().equals("2000")));
        assertTrue(result.stream().anyMatch(o -> o.getCodeOccupation().equals("3000")));
    }
}
