package com.gal.afiliaciones.application.service.generalnovelty.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import com.gal.afiliaciones.domain.model.novelty.PermanentNovelty;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.wsconfecamaras.RequestExportDTO;
import java.time.LocalDate; 
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.web.reactive.function.client.WebClient;

import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.generalnovelty.GeneralNovelty;
import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.generalNovelty.GeneralNoveltyRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.PermanentNoveltyRepository;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.SaveGeneralNoveltyRequest;
import com.gal.afiliaciones.infrastructure.security.KeycloakTokenService;


class GeneralNoveltyServiceImplTest {

    private GeneralNoveltyRepository generalNoveltyRepository;
    private AffiliateRepository affiliateRepository;
    private GeneralNoveltyServiceImpl service;
    private PermanentNoveltyRepository permanentNoveltyRepository;
    private WebClient webClient;
    private KeycloakTokenService keycloakTokenService;
    private ExcelProcessingServiceData excelProcessingServiceData;

    @BeforeEach
    void setUp() {
        generalNoveltyRepository = mock(GeneralNoveltyRepository.class);
        affiliateRepository = mock(AffiliateRepository.class);
        permanentNoveltyRepository = mock(PermanentNoveltyRepository.class);
        webClient = mock(WebClient.class);
        keycloakTokenService = mock(KeycloakTokenService.class);
        excelProcessingServiceData = mock(ExcelProcessingServiceData.class);
        service = new GeneralNoveltyServiceImpl(generalNoveltyRepository, affiliateRepository,
                permanentNoveltyRepository, webClient, keycloakTokenService, excelProcessingServiceData);
    }

    @Test
    void saveGeneralNovelty_savesSuccessfully_whenAffiliateExistsAndFiledNumberIsUnique() {
        SaveGeneralNoveltyRequest request = mock(SaveGeneralNoveltyRequest.class);
        Affiliate affiliate = mock(Affiliate.class);

        when(request.getIdAffiliation()).thenReturn(1L);
        when(affiliateRepository.findByIdAffiliate(1L)).thenReturn(Optional.of(affiliate));
        when(affiliate.getRequestChannel()).thenReturn(2L);
        when(request.getFiledNumber()).thenReturn("F123");
        when(generalNoveltyRepository.findByFiledNumber("F123")).thenReturn(Optional.empty());
        when(request.getNoveltyType()).thenReturn("TYPE");
        when(request.getStatus()).thenReturn("STATUS");
        when(request.getObservation()).thenReturn("OBS");

        service.saveGeneralNovelty(request);

        ArgumentCaptor<GeneralNovelty> captor = ArgumentCaptor.forClass(GeneralNovelty.class);
        verify(generalNoveltyRepository).save(captor.capture());
        GeneralNovelty saved = captor.getValue();

        assertEquals(2L, saved.getRequestChannelId());
        assertEquals("F123", saved.getFiledNumber());
        assertEquals(LocalDate.now(), saved.getAffiliationDate());
        assertEquals("TYPE", saved.getNoveltyType());
        assertEquals("STATUS", saved.getStatus());
        assertEquals("OBS", saved.getObservation());
    }

    @Test
    void saveGeneralNovelty_throwsException_whenAffiliateNotFound() {
        SaveGeneralNoveltyRequest request = mock(SaveGeneralNoveltyRequest.class);
        when(request.getIdAffiliation()).thenReturn(1L);
        when(affiliateRepository.findByIdAffiliate(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.saveGeneralNovelty(request);
        });

        assertTrue(ex.getMessage().contains("No se encontró el afiliado"));
        verify(generalNoveltyRepository, never()).save(any());
    }

    @Test
    void saveGeneralNovelty_updatesWhenFiledNumberExists() {
        SaveGeneralNoveltyRequest request = mock(SaveGeneralNoveltyRequest.class);
        Affiliate affiliate = mock(Affiliate.class);

        when(request.getIdAffiliation()).thenReturn(1L);
        when(affiliateRepository.findByIdAffiliate(1L)).thenReturn(Optional.of(affiliate));
        when(affiliate.getRequestChannel()).thenReturn(2L);
        when(request.getFiledNumber()).thenReturn("F123");
        GeneralNovelty existing = mock(GeneralNovelty.class);
        when(generalNoveltyRepository.findByFiledNumber("F123")).thenReturn(Optional.of(existing));

        service.saveGeneralNovelty(request);

        // Debe actualizar el existente (guardar el mismo objeto existente)
        verify(generalNoveltyRepository).save(existing);
    }

    @Test
    void saveGeneralNovelty_throwsException_whenFiledNumberIsNullOrEmpty() {
        SaveGeneralNoveltyRequest request = mock(SaveGeneralNoveltyRequest.class);
        Affiliate affiliate = mock(Affiliate.class);

        when(request.getIdAffiliation()).thenReturn(1L);
        when(affiliateRepository.findByIdAffiliate(1L)).thenReturn(Optional.of(affiliate));
        when(affiliate.getRequestChannel()).thenReturn(2L);
        when(request.getFiledNumber()).thenReturn(null);
        when(request.getNoveltyType()).thenReturn("TYPE");
        when(request.getStatus()).thenReturn("STATUS");
        when(request.getObservation()).thenReturn("OBS");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            service.saveGeneralNovelty(request);
        });
        assertTrue(ex.getMessage().contains("vacío o nulo"));
        verify(generalNoveltyRepository, never()).save(any(GeneralNovelty.class));
    }

    @Test
    void exportNoveltiesByContributorDocument_exportsSuccessfully() {
        // Given
        String contributorIdentificationType = "CC";
        String contributorIdentification = "123456789";
        String exportType = "xlsx";

        List<PermanentNovelty> novelties = Arrays.asList(mock(PermanentNovelty.class));
        ExportDocumentsDTO expectedResponse = mock(ExportDocumentsDTO.class);

        when(permanentNoveltyRepository.findAllByContributorIdentificationTypeAndContributorIdentification(
                contributorIdentificationType, contributorIdentification))
                .thenReturn(novelties);

        when(excelProcessingServiceData.exportDataGrid(any(RequestExportDTO.class)))
                .thenReturn(expectedResponse);

        // When
        ExportDocumentsDTO result = service.exportNoveltiesByContributorDocument(
                contributorIdentificationType, contributorIdentification, exportType);

        // Then
        assertEquals(expectedResponse, result);
        verify(excelProcessingServiceData).exportDataGrid(any(RequestExportDTO.class));
    }
}