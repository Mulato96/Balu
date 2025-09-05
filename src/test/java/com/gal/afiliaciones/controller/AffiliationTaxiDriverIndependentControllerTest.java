package com.gal.afiliaciones.controller;

import com.gal.afiliaciones.application.service.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.controller.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentController;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationIndependentTaxiDriverStep3DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentCreateDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentPreLoadDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentUpdateDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AffiliationTaxiDriverIndependentControllerTest {

    @Mock
    private AffiliationTaxiDriverIndependentService service;

    @InjectMocks
    private AffiliationTaxiDriverIndependentController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPreloadAffiliationDataSuccess() {
        AffiliationTaxiDriverIndependentPreLoadDTO preloadDTO = new AffiliationTaxiDriverIndependentPreLoadDTO();
        when(service.preloadAffiliationData(anyString(), anyString(), eq("Taxista"), anyString(), anyString(), anyLong())).thenReturn(preloadDTO);

        ResponseEntity<BodyResponseConfig<AffiliationTaxiDriverIndependentPreLoadDTO>> response = controller.preloadAffiliationData("NI", "600123123", "Taxista", "CC", "123456", 0L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testPreloadAffiliationDataNotFound() {
        when(service.preloadAffiliationData(anyString(), anyString(), eq("Taxista"), anyString(), anyString(), anyLong())).thenReturn( new AffiliationTaxiDriverIndependentPreLoadDTO());

        ResponseEntity<BodyResponseConfig<AffiliationTaxiDriverIndependentPreLoadDTO>> response = controller.preloadAffiliationData("NI", "600123123", "Taxista", "CC", "123456", 0L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testCreateAffiliationSuccess() {
        AffiliationTaxiDriverIndependentCreateDTO dto = new AffiliationTaxiDriverIndependentCreateDTO();

        ResponseEntity<Long> response = controller.createAffiliation(dto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(service, times(1)).createAffiliation(dto);
    }

    @Test
    void testUpdateAffiliationSuccess() {
        AffiliationTaxiDriverIndependentUpdateDTO dto = new AffiliationTaxiDriverIndependentUpdateDTO();

        ResponseEntity<Void> response = controller.updateAffiliation(dto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(service, times(1)).updateAffiliation(dto);
    }

    @Test
    void testUploadDocumentsSuccess() {
        AffiliationIndependentTaxiDriverStep3DTO dto = new AffiliationIndependentTaxiDriverStep3DTO();
        dto.setIdAffiliation(1L);
        dto.setRisk("1");
        dto.setOccupation("Test occupation");
        dto.setPrice(new BigDecimal(2000000));
        dto.setContractIbcValue(new BigDecimal(2000000).multiply(Constant.PERCENTAGE_40));

        Affiliation affiliation = new Affiliation();
        when(service.uploadDocuments(eq(dto), anyList())).thenReturn(affiliation);

        ResponseEntity<Affiliation> response = controller.uploadDocuments(dto, List.of(mock(MultipartFile.class)));

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(affiliation, response.getBody());
        verify(service, times(1)).uploadDocuments(eq(dto), anyList());
    }

    @Test
    void testUploadDocumentsThrowsAffiliationError() {
        AffiliationIndependentTaxiDriverStep3DTO dto = new AffiliationIndependentTaxiDriverStep3DTO();
        dto.setIdAffiliation(1L);
        dto.setRisk("1");
        dto.setOccupation("Test occupation");
        dto.setPrice(new BigDecimal(2000000));
        dto.setContractIbcValue(new BigDecimal(2000000).multiply(Constant.PERCENTAGE_40));

        when(service.uploadDocuments(eq(dto), anyList())).thenThrow(new AffiliationError("Error"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                controller.uploadDocuments(dto, List.of(mock(MultipartFile.class)))
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Error al subir los documentos para la afiliación", exception.getReason());
    }

}
