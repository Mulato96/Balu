
package com.gal.afiliaciones.application.service.generalnovelty.impl;

import com.gal.afiliaciones.config.ex.generalnovelty.GeneralNoveltyException;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.RequestChannel;
import com.gal.afiliaciones.domain.model.generalnovelty.GeneralNovelty;
import com.gal.afiliaciones.domain.model.novelty.NoveltyStatusCausal;
import com.gal.afiliaciones.domain.model.novelty.PermanentNovelty;
import com.gal.afiliaciones.domain.model.novelty.NoveltyStatus;
import com.gal.afiliaciones.domain.model.novelty.TypeOfUpdate;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.generalNovelty.GeneralNoveltyRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.PermanentNoveltyRepository;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.GeneralNoveltyDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.ExportNoveltyDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.ExportWorkerNoveltyDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.NoveltyContributorResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.PaymentsContributorsRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.PaymentsContributorsResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.SaveGeneralNoveltyRequest;
import com.gal.afiliaciones.infrastructure.security.KeycloakTokenService;
import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.wsconfecamaras.RequestExportDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GeneralNoveltyServiceImpl Tests")
class GeneralNoveltyServiceImplTest {

    @Mock
    private GeneralNoveltyRepository generalNoveltyRepository;

    @Mock
    private AffiliateRepository affiliateRepository;

    @Mock
    private PermanentNoveltyRepository permanentNoveltyRepository;

    @Mock
    private WebClient webClient;

    @Mock
    private KeycloakTokenService keycloakTokenService;

    @Mock
    private ExcelProcessingServiceData excelProcessingServiceData;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private GeneralNoveltyServiceImpl generalNoveltyService;

    private Affiliate affiliate;
    private GeneralNovelty generalNovelty;
    private PermanentNovelty permanentNovelty;
    private SaveGeneralNoveltyRequest saveRequest;
    private RequestChannel requestChannel;
    private TypeOfUpdate noveltyType;
    private NoveltyStatus noveltyStatus;
    private NoveltyStatusCausal causal;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(generalNoveltyService, "recaudoBaseUrl", "http://localhost:8080/");

        // Setup Affiliate
        affiliate = new Affiliate();
        affiliate.setIdAffiliate(1L);
        affiliate.setRequestChannel(100L);

        // Setup RequestChannel
        requestChannel = new RequestChannel();
        requestChannel.setId(100L);
        requestChannel.setName("Canal Web");

        // Setup NoveltyType
        noveltyType = new TypeOfUpdate();
        noveltyType.setId(1L);
        noveltyType.setDescription("Retiro");

        // Setup NoveltyStatus
        noveltyStatus = new NoveltyStatus();
        noveltyStatus.setId(1L);
        noveltyStatus.setStatus("Aprobado");

        // Setup Causal
        causal = new NoveltyStatusCausal();
        causal.setId(1L);
        causal.setCausal("Renuncia voluntaria");

        // Setup GeneralNovelty
        generalNovelty = GeneralNovelty.builder()
                .id(1L)
                .requestChannelId(100L)
                .filedNumber("RAD-12345")
                .affiliationDate(LocalDate.now())
                .noveltyType("Retiro")
                .status("Aprobado")
                .observation("Observación de prueba")
                .idAffiliate(1L)
                .build();
        generalNovelty.setRequestChannel(requestChannel);

        // Setup PermanentNovelty
        permanentNovelty = new PermanentNovelty();
        permanentNovelty.setId(1L);
        permanentNovelty.setChannel(requestChannel);
        permanentNovelty.setFiledNumber("RAD-67890");
        permanentNovelty.setRegistryDate(LocalDateTime.now());
        permanentNovelty.setNoveltyType(noveltyType);
        permanentNovelty.setStatus(noveltyStatus);
        permanentNovelty.setCausal(causal);
        permanentNovelty.setIdAffiliate(1L);
        permanentNovelty.setContributorIdentificationType("CC");
        permanentNovelty.setContributorIdentification("123456789");
        permanentNovelty.setContributantIdentificationType("CC");
        permanentNovelty.setContributantIdentification("987654321");
        permanentNovelty.setContributantFirstName("Juan");
        permanentNovelty.setContributantSecondName("Carlos");
        permanentNovelty.setContributantSurname("Pérez");
        permanentNovelty.setContributantSecondSurname("García");

        // Setup SaveGeneralNoveltyRequest
        saveRequest = new SaveGeneralNoveltyRequest();
        saveRequest.setIdAffiliation(1L);
        saveRequest.setFiledNumber("RAD-12345");
        saveRequest.setNoveltyType("Retiro");
        saveRequest.setStatus("Aprobado");
        saveRequest.setObservation("Observación de prueba");
    }

    // ==================== saveGeneralNovelty Tests ====================

    @Test
    @DisplayName("Should save new general novelty successfully")
    void testSaveGeneralNovelty_Success_NewNovelty() {
        // Arrange
        when(affiliateRepository.findByIdAffiliate(1L)).thenReturn(Optional.of(affiliate));
        when(generalNoveltyRepository.findByFiledNumber("RAD-12345")).thenReturn(Optional.empty());
        when(generalNoveltyRepository.save(any(GeneralNovelty.class))).thenReturn(generalNovelty);

        // Act
        generalNoveltyService.saveGeneralNovelty(saveRequest);

        // Assert
        verify(affiliateRepository).findByIdAffiliate(1L);
        verify(generalNoveltyRepository).findByFiledNumber("RAD-12345");
        verify(generalNoveltyRepository).save(any(GeneralNovelty.class));
    }

    @Test
    @DisplayName("Should update existing general novelty")
    void testSaveGeneralNovelty_Success_UpdateExisting() {
        // Arrange
        GeneralNovelty existingNovelty = GeneralNovelty.builder()
                .id(1L)
                .requestChannelId(99L)
                .filedNumber("RAD-12345")
                .affiliationDate(LocalDate.now().minusDays(1))
                .noveltyType("Otro")
                .status("Pendiente")
                .observation("Observación antigua")
                .idAffiliate(1L)
                .build();

        when(affiliateRepository.findByIdAffiliate(1L)).thenReturn(Optional.of(affiliate));
        when(generalNoveltyRepository.findByFiledNumber("RAD-12345")).thenReturn(Optional.of(existingNovelty));
        when(generalNoveltyRepository.save(any(GeneralNovelty.class))).thenReturn(existingNovelty);

        // Act
        generalNoveltyService.saveGeneralNovelty(saveRequest);

        // Assert
        verify(generalNoveltyRepository).save(argThat(novelty ->
                novelty.getRequestChannelId().equals(100L) &&
                        novelty.getNoveltyType().equals("Retiro") &&
                        novelty.getStatus().equals("Aprobado")
        ));
    }

    @Test
    @DisplayName("Should throw exception when affiliate not found")
    void testSaveGeneralNovelty_AffiliateNotFound() {
        // Arrange
        when(affiliateRepository.findByIdAffiliate(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> generalNoveltyService.saveGeneralNovelty(saveRequest));

        assertEquals("No se encontró el afiliado con id: 1", exception.getMessage());
        verify(generalNoveltyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when filed number is null")
    void testSaveGeneralNovelty_FiledNumberNull() {
        // Arrange
        saveRequest.setFiledNumber(null);
        when(affiliateRepository.findByIdAffiliate(1L)).thenReturn(Optional.of(affiliate));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> generalNoveltyService.saveGeneralNovelty(saveRequest));

        assertEquals("El radicado no puede ser vacío o nulo", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when filed number is empty")
    void testSaveGeneralNovelty_FiledNumberEmpty() {
        // Arrange
        saveRequest.setFiledNumber("   ");
        when(affiliateRepository.findByIdAffiliate(1L)).thenReturn(Optional.of(affiliate));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> generalNoveltyService.saveGeneralNovelty(saveRequest));

        assertEquals("El radicado no puede ser vacío o nulo", exception.getMessage());
    }

    // ==================== getGeneralNoveltiesByAffiliate Tests ====================

    @Test
    @DisplayName("Should get general novelties by affiliate successfully")
    void testGetGeneralNoveltiesByAffiliate_Success() {
        // Arrange
        when(generalNoveltyRepository.findAllByIdAffiliate(1L))
                .thenReturn(Collections.singletonList(generalNovelty));
        when(permanentNoveltyRepository.findAllByIdAffiliate(1L))
                .thenReturn(Collections.singletonList(permanentNovelty));

        // Act
        List<GeneralNoveltyDTO> result = generalNoveltyService.getGeneralNoveltiesByAffiliate(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(generalNoveltyRepository).findAllByIdAffiliate(1L);
        verify(permanentNoveltyRepository).findAllByIdAffiliate(1L);
    }

    @Test
    @DisplayName("Should return empty list when no novelties found")
    void testGetGeneralNoveltiesByAffiliate_EmptyList() {
        // Arrange
        when(generalNoveltyRepository.findAllByIdAffiliate(1L)).thenReturn(Collections.emptyList());
        when(permanentNoveltyRepository.findAllByIdAffiliate(1L)).thenReturn(Collections.emptyList());

        // Act
        List<GeneralNoveltyDTO> result = generalNoveltyService.getGeneralNoveltiesByAffiliate(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should throw exception when affiliate ID is null")
    void testGetGeneralNoveltiesByAffiliate_NullId() {
        // Act & Assert
        assertThrows(GeneralNoveltyException.class,
                () -> generalNoveltyService.getGeneralNoveltiesByAffiliate(null));
    }

    @Test
    @DisplayName("Should throw exception when affiliate ID is zero")
    void testGetGeneralNoveltiesByAffiliate_ZeroId() {
        // Act & Assert
        assertThrows(GeneralNoveltyException.class,
                () -> generalNoveltyService.getGeneralNoveltiesByAffiliate(0L));
    }

    @Test
    @DisplayName("Should throw exception when affiliate ID is negative")
    void testGetGeneralNoveltiesByAffiliate_NegativeId() {
        // Act & Assert
        assertThrows(GeneralNoveltyException.class,
                () -> generalNoveltyService.getGeneralNoveltiesByAffiliate(-1L));
    }

    @Test
    @DisplayName("Should map general novelty with null request channel")
    void testGetGeneralNoveltiesByAffiliate_NullRequestChannel() {
        // Arrange
        generalNovelty.setRequestChannel(null);
        when(generalNoveltyRepository.findAllByIdAffiliate(1L))
                .thenReturn(Collections.singletonList(generalNovelty));
        when(permanentNoveltyRepository.findAllByIdAffiliate(1L)).thenReturn(Collections.emptyList());

        // Act
        List<GeneralNoveltyDTO> result = generalNoveltyService.getGeneralNoveltiesByAffiliate(1L);

        // Assert
        assertEquals(1, result.size());
        assertNull(result.get(0).getRequestChannelName());
    }

    @Test
    @DisplayName("Should map permanent novelty with null fields")
    void testGetGeneralNoveltiesByAffiliate_PermanentNoveltyNullFields() {
        // Arrange
        permanentNovelty.setChannel(null);
        permanentNovelty.setRegistryDate(null);
        permanentNovelty.setNoveltyType(null);
        permanentNovelty.setStatus(null);
        permanentNovelty.setCausal(null);

        when(generalNoveltyRepository.findAllByIdAffiliate(1L)).thenReturn(Collections.emptyList());
        when(permanentNoveltyRepository.findAllByIdAffiliate(1L))
                .thenReturn(Collections.singletonList(permanentNovelty));

        // Act
        List<GeneralNoveltyDTO> result = generalNoveltyService.getGeneralNoveltiesByAffiliate(1L);

        // Assert
        assertEquals(1, result.size());
        assertNull(result.get(0).getRequestChannelId());
        assertNull(result.get(0).getRequestChannelName());
        assertNull(result.get(0).getAffiliationDate());
        assertNull(result.get(0).getNoveltyType());
        assertNull(result.get(0).getStatus());
        assertNull(result.get(0).getObservation());
    }

    // ==================== getPaymentsContributorsByFilter Tests ====================

    @Test
    @DisplayName("Should get payments contributors successfully")
    void testGetPaymentsContributorsByFilter_Success() {
        // Arrange
        PaymentsContributorsRequestDTO request = new PaymentsContributorsRequestDTO();
        PaymentsContributorsResponseDTO expectedResponse = new PaymentsContributorsResponseDTO();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(keycloakTokenService.getAccessToken()).thenReturn("test-token");
        when(requestBodySpec.header(eq("Authorization"), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Mono.class), eq(PaymentsContributorsRequestDTO.class)))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(PaymentsContributorsResponseDTO.class))
                .thenReturn(Mono.just(expectedResponse));

        // Act
        PaymentsContributorsResponseDTO result = generalNoveltyService.getPaymentsContributorsByFilter(request);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }

    @Test
    @DisplayName("Should handle generic exception")
    void testGetPaymentsContributorsByFilter_GenericException() {
        // Arrange
        PaymentsContributorsRequestDTO request = new PaymentsContributorsRequestDTO();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(keycloakTokenService.getAccessToken()).thenReturn("test-token");
        when(requestBodySpec.header(eq("Authorization"), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Mono.class), eq(PaymentsContributorsRequestDTO.class)))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(PaymentsContributorsResponseDTO.class))
                .thenReturn(Mono.error(new RuntimeException("Generic error")));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> generalNoveltyService.getPaymentsContributorsByFilter(request));

        assertTrue(exception.getMessage().contains("Error al consultar pagos de cotizantes"));
    }

    // ==================== getGeneralNoveltiesByContributorDocument Tests ====================

    @Test
    @DisplayName("Should get novelties by contributor document successfully")
    void testGetGeneralNoveltiesByContributorDocument_Success() {
        // Arrange
        when(permanentNoveltyRepository.findAllByContributorIdentificationTypeAndContributorIdentification(
                "CC", "123456789")).thenReturn(Collections.singletonList(permanentNovelty));

        // Act
        List<NoveltyContributorResponseDTO> result = generalNoveltyService
                .getGeneralNoveltiesByContributorDocument("CC", "123456789");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Canal Web", result.get(0).getChannelName());
        assertEquals("CC 987654321", result.get(0).getContributantIdentification());
        assertTrue(result.get(0).getContributantFullName().contains("Juan"));
    }

    @Test
    @DisplayName("Should throw exception when no novelties found for contributor")
    void testGetGeneralNoveltiesByContributorDocument_NotFound() {
        // Arrange
        when(permanentNoveltyRepository.findAllByContributorIdentificationTypeAndContributorIdentification(
                "CC", "123456789")).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(GeneralNoveltyException.class,
                () -> generalNoveltyService.getGeneralNoveltiesByContributorDocument("CC", "123456789"));
    }

    @Test
    @DisplayName("Should handle null values in contributor full name")
    void testGetGeneralNoveltiesByContributorDocument_NullNames() {
        // Arrange
        permanentNovelty.setContributantFirstName(null);
        permanentNovelty.setContributantSecondName(null);
        permanentNovelty.setContributantSurname(null);
        permanentNovelty.setContributantSecondSurname(null);

        when(permanentNoveltyRepository.findAllByContributorIdentificationTypeAndContributorIdentification(
                "CC", "123456789")).thenReturn(Collections.singletonList(permanentNovelty));

        // Act
        List<NoveltyContributorResponseDTO> result = generalNoveltyService
                .getGeneralNoveltiesByContributorDocument("CC", "123456789");

        // Assert
        assertEquals(1, result.size());
        assertEquals("", result.get(0).getContributantFullName());
    }

    @Test
    @DisplayName("Should handle null channel in contributor novelty")
    void testGetGeneralNoveltiesByContributorDocument_NullChannel() {
        // Arrange
        permanentNovelty.setChannel(null);
        when(permanentNoveltyRepository.findAllByContributorIdentificationTypeAndContributorIdentification(
                "CC", "123456789")).thenReturn(Collections.singletonList(permanentNovelty));

        // Act
        List<NoveltyContributorResponseDTO> result = generalNoveltyService
                .getGeneralNoveltiesByContributorDocument("CC", "123456789");

        // Assert
        assertNull(result.get(0).getChannelName());
    }

    @Test
    @DisplayName("Should handle null novelty type, status and causal")
    void testGetGeneralNoveltiesByContributorDocument_NullFields() {
        // Arrange
        permanentNovelty.setNoveltyType(null);
        permanentNovelty.setStatus(null);
        permanentNovelty.setCausal(null);

        when(permanentNoveltyRepository.findAllByContributorIdentificationTypeAndContributorIdentification(
                "CC", "123456789")).thenReturn(Collections.singletonList(permanentNovelty));

        // Act
        List<NoveltyContributorResponseDTO> result = generalNoveltyService
                .getGeneralNoveltiesByContributorDocument("CC", "123456789");

        // Assert
        assertNull(result.get(0).getNoveltyType());
        assertNull(result.get(0).getStatus());
        assertNull(result.get(0).getCausal());
    }

    // ==================== exportNoveltiesByContributorDocument Tests ====================

    @Test
    @DisplayName("Should export novelties by contributor document successfully")
    void testExportNoveltiesByContributorDocument_Success() {
        // Arrange
        ExportDocumentsDTO expectedExport = new ExportDocumentsDTO();
        expectedExport.setArchivo("base64content");

        when(permanentNoveltyRepository.findAllByContributorIdentificationTypeAndContributorIdentification(
                "CC", "123456789")).thenReturn(Collections.singletonList(permanentNovelty));
        when(excelProcessingServiceData.exportDataGrid(any(RequestExportDTO.class)))
                .thenReturn(expectedExport);

        // Act
        ExportDocumentsDTO result = generalNoveltyService
                .exportNoveltiesByContributorDocument("CC", "123456789", "xlsx");

        // Assert
        assertNotNull(result);
        assertEquals("base64content", result.getArchivo());
        verify(excelProcessingServiceData).exportDataGrid(any(RequestExportDTO.class));
    }

    @Test
    @DisplayName("Should export with correct file prefix")
    void testExportNoveltiesByContributorDocument_CorrectPrefix() {
        // Arrange
        ExportDocumentsDTO expectedExport = new ExportDocumentsDTO();
        ArgumentCaptor<RequestExportDTO> captor = ArgumentCaptor.forClass(RequestExportDTO.class);

        when(permanentNoveltyRepository.findAllByContributorIdentificationTypeAndContributorIdentification(
                "CC", "123456789")).thenReturn(Collections.singletonList(permanentNovelty));
        when(excelProcessingServiceData.exportDataGrid(any(RequestExportDTO.class)))
                .thenReturn(expectedExport);

        // Act
        generalNoveltyService.exportNoveltiesByContributorDocument("CC", "123456789", "pdf");

        // Assert
        verify(excelProcessingServiceData).exportDataGrid(captor.capture());
        assertEquals("novedades_CC_123456789", captor.getValue().getPrefixNameFile());
        assertEquals("pdf", captor.getValue().getFormat());
    }

    @Test
    @DisplayName("Should format dates correctly in export")
    void testExportNoveltiesByContributorDocument_DateFormat() {
        // Arrange
        ExportDocumentsDTO expectedExport = new ExportDocumentsDTO();
        ArgumentCaptor<RequestExportDTO> captor = ArgumentCaptor.forClass(RequestExportDTO.class);

        when(permanentNoveltyRepository.findAllByContributorIdentificationTypeAndContributorIdentification(
                "CC", "123456789")).thenReturn(Collections.singletonList(permanentNovelty));
        when(excelProcessingServiceData.exportDataGrid(any(RequestExportDTO.class)))
                .thenReturn(expectedExport);

        // Act
        generalNoveltyService.exportNoveltiesByContributorDocument("CC", "123456789", "xlsx");

        // Assert
        verify(excelProcessingServiceData).exportDataGrid(captor.capture());
        List<ExportNoveltyDTO> exportData = (List<ExportNoveltyDTO>) captor.getValue().getData();
        assertNotNull(exportData.get(0).getFechaRecibido());
        assertTrue(exportData.get(0).getFechaRecibido().matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    @DisplayName("Should handle null registry date in export")
    void testExportNoveltiesByContributorDocument_NullDate() {
        // Arrange
        permanentNovelty.setRegistryDate(null);
        ExportDocumentsDTO expectedExport = new ExportDocumentsDTO();
        ArgumentCaptor<RequestExportDTO> captor = ArgumentCaptor.forClass(RequestExportDTO.class);

        when(permanentNoveltyRepository.findAllByContributorIdentificationTypeAndContributorIdentification(
                "CC", "123456789")).thenReturn(Collections.singletonList(permanentNovelty));
        when(excelProcessingServiceData.exportDataGrid(any(RequestExportDTO.class)))
                .thenReturn(expectedExport);

        // Act
        generalNoveltyService.exportNoveltiesByContributorDocument("CC", "123456789", "xlsx");

        // Assert
        verify(excelProcessingServiceData).exportDataGrid(captor.capture());
        List<ExportNoveltyDTO> exportData = (List<ExportNoveltyDTO>) captor.getValue().getData();
        assertEquals("", exportData.get(0).getFechaRecibido());
    }

    // ==================== exportNoveltiesByWorkerByIdAffiliate Tests ====================

    @Test
    @DisplayName("Should export worker novelties by affiliate ID successfully")
    void testExportNoveltiesByWorkerByIdAffiliate_Success() {
        // Arrange
        ExportDocumentsDTO expectedExport = new ExportDocumentsDTO();
        expectedExport.setArchivo("base64content");

        when(generalNoveltyRepository.findAllByIdAffiliate(1L))
                .thenReturn(Collections.singletonList(generalNovelty));
        when(permanentNoveltyRepository.findAllByIdAffiliate(1L))
                .thenReturn(Collections.singletonList(permanentNovelty));
        when(excelProcessingServiceData.exportDataGrid(any(RequestExportDTO.class)))
                .thenReturn(expectedExport);

        // Act
        ExportDocumentsDTO result = generalNoveltyService.exportNoveltiesByWorkerByIdAffiliate(1L, "xlsx");

        // Assert
        assertNotNull(result);
        assertEquals("base64content", result.getArchivo());
        verify(excelProcessingServiceData).exportDataGrid(any(RequestExportDTO.class));
    }
}