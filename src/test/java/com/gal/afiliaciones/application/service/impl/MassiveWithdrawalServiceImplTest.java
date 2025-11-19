package com.gal.afiliaciones.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gal.afiliaciones.application.service.IRadicadoService;
import com.gal.afiliaciones.application.service.IValidationService;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.impl.certicate.InMemoryMultipartFile;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.HistoricoCarguesMasivos;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.HistoricoCarguesMasivosRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirement.RetirementRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirementreason.RetirementReasonRepository;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

@ContextConfiguration(classes = {MassiveWithdrawalServiceImpl.class})
@DisabledInAotMode
@ExtendWith(SpringExtension.class)
class MassiveWithdrawalServiceImplTest {
    @MockBean
    private AffiliateRepository affiliateRepository;

    @MockBean
    private AlfrescoService alfrescoService;

    @MockBean
    private CollectProperties collectProperties;

    @MockBean
    private HistoricoCarguesMasivosRepository historicoCarguesMasivosRepository;

    @MockBean
    private IRadicadoService iRadicadoService;

    @MockBean
    private IValidationService iValidationService;

    @Autowired
    private MassiveWithdrawalServiceImpl massiveWithdrawalServiceImpl;

    @MockBean
    private RetirementReasonRepository retirementReasonRepository;

    @MockBean
    private RetirementRepository retirementRepository;

    /**
     * Test {@link MassiveWithdrawalServiceImpl#downloadTemplate()}.
     *
     * <p>Method under test: {@link MassiveWithdrawalServiceImpl#downloadTemplate()}
     */
    @Test
    @DisplayName("Test downloadTemplate()")
    @Tag("MaintainedByDiffblue")
    void testDownloadTemplate() {
        // Arrange
        when(collectProperties.getIdTemplateRetirementMasivo()).thenThrow(new RuntimeException());

        // Act and Assert
        assertThrows(RuntimeException.class, () -> massiveWithdrawalServiceImpl.downloadTemplate());
        verify(collectProperties).getIdTemplateRetirementMasivo();
    }

    /**
     * Test {@link MassiveWithdrawalServiceImpl#downloadTemplate()}.
     *
     * <ul>
     *   <li>Given {@link AlfrescoService} {@link AlfrescoService#getDocument(String)} throw {@link
     *       RuntimeException#RuntimeException()}.
     * </ul>
     *
     * <p>Method under test: {@link MassiveWithdrawalServiceImpl#downloadTemplate()}
     */
    @Test
    @DisplayName(
            "Test downloadTemplate(); given AlfrescoService getDocument(String) throw RuntimeException()")
    @Tag("MaintainedByDiffblue")
    void testDownloadTemplate_givenAlfrescoServiceGetDocumentThrowRuntimeException() {
        // Arrange
        when(collectProperties.getIdTemplateRetirementMasivo())
                .thenReturn("Id Template Retirement Masivo");
        when(alfrescoService.getDocument(Mockito.<String>any())).thenThrow(new RuntimeException());

        // Act and Assert
        assertThrows(RuntimeException.class, () -> massiveWithdrawalServiceImpl.downloadTemplate());
        verify(alfrescoService).getDocument("Id Template Retirement Masivo");
        verify(collectProperties).getIdTemplateRetirementMasivo();
    }

    /**
     * Test {@link MassiveWithdrawalServiceImpl#downloadTemplate()}.
     *
     * <ul>
     *   <li>Then return {@code Document}.
     * </ul>
     *
     * <p>Method under test: {@link MassiveWithdrawalServiceImpl#downloadTemplate()}
     */
    @Test
    @DisplayName("Test downloadTemplate(); then return 'Document'")
    @Tag("MaintainedByDiffblue")
    void testDownloadTemplate_thenReturnDocument() {
        // Arrange
        when(collectProperties.getIdTemplateRetirementMasivo())
                .thenReturn("Id Template Retirement Masivo");
        when(alfrescoService.getDocument(Mockito.<String>any())).thenReturn("Document");

        // Act
        String actualDownloadTemplateResult = massiveWithdrawalServiceImpl.downloadTemplate();

        // Assert
        verify(alfrescoService).getDocument("Id Template Retirement Masivo");
        verify(collectProperties).getIdTemplateRetirementMasivo();
        assertEquals("Document", actualDownloadTemplateResult);
    }

    /**
     * Test {@link MassiveWithdrawalServiceImpl#uploadFile(MultipartFile, Long)}.
     *
     * <p>Method under test: {@link MassiveWithdrawalServiceImpl#uploadFile(MultipartFile, Long)}
     */
    @Test
    @DisplayName("Test uploadFile(MultipartFile, Long)")
    @Tag("MaintainedByDiffblue")
    void testUploadFile() throws UnsupportedEncodingException {
        // Arrange
        InMemoryMultipartFile file =
                new InMemoryMultipartFile("Name", "foo.txt", "text/plain", "AXAXAXAX".getBytes("UTF-8"));

        // Act and Assert
        assertThrows(RuntimeException.class, () -> massiveWithdrawalServiceImpl.uploadFile(file, 1L));
    }

    /**
     * Test {@link MassiveWithdrawalServiceImpl#uploadFile(MultipartFile, Long)}.
     *
     * <ul>
     *   <li>Given {@code true}.
     *   <li>Then calls {@link InMemoryMultipartFile#isEmpty()}.
     * </ul>
     *
     * <p>Method under test: {@link MassiveWithdrawalServiceImpl#uploadFile(MultipartFile, Long)}
     */
    @Test
    @DisplayName("Test uploadFile(MultipartFile, Long); given 'true'; then calls isEmpty()")
    @Tag("MaintainedByDiffblue")
    void testUploadFile_givenTrue_thenCallsIsEmpty() {
        // Arrange
        InMemoryMultipartFile file = mock(InMemoryMultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        // Act and Assert
        assertThrows(RuntimeException.class, () -> massiveWithdrawalServiceImpl.uploadFile(file, 1L));
        verify(file).isEmpty();
    }

    /**
     * Test {@link MassiveWithdrawalServiceImpl#getHistory(Long)}.
     *
     * <ul>
     *   <li>Then return Empty.
     * </ul>
     *
     * <p>Method under test: {@link MassiveWithdrawalServiceImpl#getHistory(Long)}
     */
    @Test
    @DisplayName("Test getHistory(Long); then return Empty")
    @Tag("MaintainedByDiffblue")
    void testGetHistory_thenReturnEmpty() {
        // Arrange
        when(historicoCarguesMasivosRepository.findByEmpleador_IdAffiliate(Mockito.<Long>any()))
                .thenReturn(new ArrayList<>());

        // Act
        List<HistoricoCarguesMasivos> actualHistory = massiveWithdrawalServiceImpl.getHistory(1L);

        // Assert
        verify(historicoCarguesMasivosRepository).findByEmpleador_IdAffiliate(1L);
        assertTrue(actualHistory.isEmpty());
    }

    /**
     * Test {@link MassiveWithdrawalServiceImpl#getHistory(Long)}.
     *
     * <ul>
     *   <li>Then throw {@link RuntimeException}.
     * </ul>
     *
     * <p>Method under test: {@link MassiveWithdrawalServiceImpl#getHistory(Long)}
     */
    @Test
    @DisplayName("Test getHistory(Long); then throw RuntimeException")
    @Tag("MaintainedByDiffblue")
    void testGetHistory_thenThrowRuntimeException() {
        // Arrange
        when(historicoCarguesMasivosRepository.findByEmpleador_IdAffiliate(Mockito.<Long>any()))
                .thenThrow(new RuntimeException());

        // Act and Assert
        assertThrows(RuntimeException.class, () -> massiveWithdrawalServiceImpl.getHistory(1L));
        verify(historicoCarguesMasivosRepository).findByEmpleador_IdAffiliate(1L);
    }
}
