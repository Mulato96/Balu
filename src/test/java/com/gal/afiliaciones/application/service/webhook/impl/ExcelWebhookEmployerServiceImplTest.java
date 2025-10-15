package com.gal.afiliaciones.application.service.webhook.impl;

import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliate.affiliationemployeractivitiesmercantile.AffiliationEmployerActivitiesMercantileService;
import com.gal.afiliaciones.application.service.webhook.AsyncWebhookEmployerService;
import com.gal.afiliaciones.application.service.webhook.WebhookEmployerService;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.infrastructure.client.generic.employer.ConsultEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.EmployerResponse;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerResponseDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExcelWebhookEmployerServiceImplTest {

    @Mock private AsyncWebhookEmployerService asyncWebhookEmployerService;
    @Mock private WebhookEmployerService webhookEmployerService;
    @Mock private AffiliationEmployerActivitiesMercantileService affiliationEmployerActivitiesMercantileService;
    @Mock private AffiliateService affiliateService;
    @Mock private AffiliateMercantileRepository affiliateMercantileRepository;
    @Mock private AffiliateRepository affiliateRepository;
    @Mock private ConsultEmployerClient consultEmployerClient;
    @Mock private MultipartFile multipartFile;

    @InjectMocks
    private ExcelWebhookEmployerServiceImpl excelWebhookEmployerService;

    @TempDir Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(excelWebhookEmployerService, "errorReportDir", tempDir.toString());
    }

    private void mockMultipartFile(byte[] content, String filename) throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn(filename);
        when(multipartFile.getInputStream()).thenAnswer(inv -> new ByteArrayInputStream(content));
    }

    @Test
    void processExcelFile_Success() throws Exception {
        byte[] excelContent = createValidExcelFile();
        mockMultipartFile(excelContent, "test.xlsx");

        List<WebhookEmployerResponseDTO> expectedResponses = createMockWebhookResponses();
        when(asyncWebhookEmployerService.processEmployersListAsync(any()))
                .thenReturn(CompletableFuture.completedFuture(expectedResponses));

        List<WebhookEmployerResponseDTO> result = excelWebhookEmployerService.processExcelFile(multipartFile);

        assertNotNull(result);
        assertEquals(expectedResponses.size(), result.size());
        verify(asyncWebhookEmployerService).processEmployersListAsync(any());
    }

    @Test
    void processExcelFile_InvalidStructure() throws Exception {
        mockMultipartFile(createInvalidExcelFile(), "invalid.xlsx");
        RuntimeException ex = assertThrows(RuntimeException.class, () -> excelWebhookEmployerService.processExcelFile(multipartFile));
        assertTrue(ex.getMessage().contains("Estructura del archivo Excel no es válida"));
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void processExcelFile_ProcessingError() throws Exception {
        mockMultipartFile(createValidExcelFile(), "test.xlsx");
        when(asyncWebhookEmployerService.processEmployersListAsync(any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Processing error")));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> excelWebhookEmployerService.processExcelFile(multipartFile));
        assertTrue(ex.getMessage().contains("Error procesando archivo Excel"));
    }

    private void setupFileMocks(MockedStatic<Files> mockedFiles, MockedStatic<Paths> mockedPaths) {
        Path mockPath = mock(Path.class);
        Path mockParent = mock(Path.class);
        mockedPaths.when(() -> Paths.get(anyString())).thenReturn(mockPath);
        when(mockPath.getParent()).thenReturn(mockParent);
        mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(true);
        mockedFiles.when(() -> Files.readAllLines(any(Path.class))).thenReturn(Collections.emptyList());
        mockedFiles.when(() -> Files.write(any(Path.class), any(List.class))).thenReturn(mockPath);
        mockedFiles.when(() -> Files.createDirectories(any(Path.class))).thenReturn(mockParent);
        mockedFiles.when(() -> Files.createFile(any(Path.class))).thenReturn(mockPath);
    }

    @Test
    void processExcelFileAsync_InvalidStructure() throws Exception {
        mockMultipartFile(createInvalidExcelFile(), "invalid.xlsx");
        RuntimeException ex = assertThrows(RuntimeException.class, () -> excelWebhookEmployerService.processExcelFileAsync(multipartFile));
        assertTrue(ex.getMessage().contains("Estructura del archivo Excel no es válida"));
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void processFullExcelFlowAsync_Success() throws Exception {
        var employers = createMockEmployerRequests();
        var webhookResponses = createMockWebhookResponses();

        when(asyncWebhookEmployerService.processEmployersListAsync(any()))
                .thenReturn(CompletableFuture.completedFuture(webhookResponses));
        when(affiliationEmployerActivitiesMercantileService.affiliateBUs(anyString(), anyString(), anyInt())).thenReturn(true);
        when(affiliateService.affiliateBUs(anyString(), anyString())).thenReturn(true);

        assertDoesNotThrow(() -> excelWebhookEmployerService.processFullExcelFlowAsync(employers));
        Thread.sleep(100);
        verify(asyncWebhookEmployerService).processEmployersListAsync(employers);
    }

    @Test
    void processFullExcelFlowAsync_EmptyWebhookResponse() throws Exception {
        var employers = createMockEmployerRequests();
        when(asyncWebhookEmployerService.processEmployersListAsync(any()))
                .thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));
        assertDoesNotThrow(() -> excelWebhookEmployerService.processFullExcelFlowAsync(employers));
        verify(asyncWebhookEmployerService).processEmployersListAsync(employers);
    }

    @Test
    void processFullExcelFlowAsync_ProcessingError() throws Exception {
        var employers = createMockEmployerRequests();
        when(asyncWebhookEmployerService.processEmployersListAsync(any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Processing error")));
        assertDoesNotThrow(() -> excelWebhookEmployerService.processFullExcelFlowAsync(employers));
        verify(asyncWebhookEmployerService).processEmployersListAsync(employers);
    }

    @Test
    void readExcelFile_Success() throws Exception {
        mockMultipartFile(createValidExcelFile(), "test.xlsx");
        var result = excelWebhookEmployerService.readExcelFile(multipartFile);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("NI", result.get(0).getIdTipoDocEmpresa());
        assertEquals("123456789", result.get(0).getIdEmpresa());
    }

    @Test
    void readExcelFile_MissingColumns() throws Exception {
        mockMultipartFile(createExcelWithMissingColumns(), "invalid.xlsx");
        var ex = assertThrows(IllegalArgumentException.class, () -> excelWebhookEmployerService.readExcelFile(multipartFile));
        assertTrue(ex.getMessage().contains("No se encontraron las columnas requeridas"));
    }

    @Test
    void readExcelFile_InvalidDocumentType() throws Exception {
        mockMultipartFile(createExcelWithInvalidDocumentType(), "invalid.xlsx");
        var result = excelWebhookEmployerService.readExcelFile(multipartFile);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void readExcelFile_NumericCells() throws Exception {
        mockMultipartFile(createExcelWithValidNumericValues(), "numeric.xlsx");
        var result = excelWebhookEmployerService.readExcelFile(multipartFile);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("NI", result.get(0).getIdTipoDocEmpresa());
        assertEquals("123456789", result.get(0).getIdEmpresa());
    }

    @Test
    void readExcelFile_IOException() throws Exception {
        when(multipartFile.getOriginalFilename()).thenReturn("test.xlsx");
        when(multipartFile.getInputStream()).thenThrow(new IOException("IO Error"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> excelWebhookEmployerService.readExcelFile(multipartFile));
        assertTrue(ex.getMessage().contains("Error leyendo archivo Excel"));
    }

    @Test
    void validateExcelStructure_Valid() throws Exception {
        mockMultipartFile(createValidExcelFile(), "test.xlsx");
        assertTrue(excelWebhookEmployerService.validateExcelStructure(multipartFile));
    }

    @Test
    void validateExcelStructure_Invalid() throws Exception {
        mockMultipartFile(createInvalidExcelFile(), "invalid.xlsx");
        assertFalse(excelWebhookEmployerService.validateExcelStructure(multipartFile));
    }

    @Test
    void validateExcelStructure_NoData() throws Exception {
        mockMultipartFile(createEmptyExcelFile(), "empty.xlsx");
        assertFalse(excelWebhookEmployerService.validateExcelStructure(multipartFile));
    }

    @Test
    void validateExcelStructure_IOException() throws Exception {
        when(multipartFile.getOriginalFilename()).thenReturn("test.xlsx");
        when(multipartFile.getInputStream()).thenThrow(new IOException("IO Error"));
        assertFalse(excelWebhookEmployerService.validateExcelStructure(multipartFile));
    }

    private byte[] createValidExcelFile() throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Sheet1");
            Row h = sh.createRow(0);
            h.createCell(0).setCellValue("tipo_documento");
            h.createCell(1).setCellValue("numero_documento");
            Row r = sh.createRow(1);
            r.createCell(0).setCellValue("NI");
            r.createCell(1).setCellValue("123456789");
            wb.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createInvalidExcelFile() throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Sheet1");
            Row h = sh.createRow(0);
            h.createCell(0).setCellValue("invalid_column");
            h.createCell(1).setCellValue("another_invalid");
            Row r = sh.createRow(1);
            r.createCell(0).setCellValue("value1");
            r.createCell(1).setCellValue("value2");
            wb.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createEmptyExcelFile() throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Sheet1");
            Row h = sh.createRow(0);
            h.createCell(0).setCellValue("tipo_documento");
            h.createCell(1).setCellValue("numero_documento");
            wb.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createExcelWithMissingColumns() throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Sheet1");
            Row h = sh.createRow(0);
            h.createCell(0).setCellValue("tipo_documento");
            h.createCell(1).setCellValue("other_column");
            Row r = sh.createRow(1);
            r.createCell(0).setCellValue("NI");
            r.createCell(1).setCellValue("some_value");
            wb.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createExcelWithInvalidDocumentType() throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Sheet1");
            Row h = sh.createRow(0);
            h.createCell(0).setCellValue("tipo_documento");
            h.createCell(1).setCellValue("numero_documento");
            Row r = sh.createRow(1);
            r.createCell(0).setCellValue("INVALID");
            r.createCell(1).setCellValue("123456789");
            wb.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createExcelWithValidNumericValues() throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Sheet1");
            Row h = sh.createRow(0);
            h.createCell(0).setCellValue("tipo_documento");
            h.createCell(1).setCellValue("numero_documento");
            Row r = sh.createRow(1);
            r.createCell(0).setCellValue("NI");
            r.createCell(1).setCellValue(123456789L);
            wb.write(out);
            return out.toByteArray();
        }
    }

    private List<WebhookEmployerRequestDTO> createMockEmployerRequests() {
        return Arrays.asList(
                WebhookEmployerRequestDTO.builder()
                        .idTipoDocEmpresa("NI")
                        .idEmpresa("123456789")
                        .idSubEmpresa(0)
                        .build()
        );
    }

    private List<WebhookEmployerResponseDTO> createMockWebhookResponses() {
        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");

        WebhookEmployerResponseDTO.Dependiente dep = new WebhookEmployerResponseDTO.Dependiente();
        dep.setTipoDocumento("CC");
        dep.setNumeroDocumento("987654321");

        WebhookEmployerResponseDTO r1 = new WebhookEmployerResponseDTO();
        r1.setEmpleador(emp);
        r1.setEmpleados(Collections.emptyList());

        WebhookEmployerResponseDTO r2 = new WebhookEmployerResponseDTO();
        r2.setEmpleados(Arrays.asList(dep));

        return Arrays.asList(r1, r2);
    }

    private EmployerResponse createMockEmployerResponse() {
        EmployerResponse response = new EmployerResponse();
        response.setRazonSocial("Test Company");
        return response;
    }

    @Test
    void processFullExcelFlowAsync_EmployerAlreadyExists_SkipMercantileAffiliation() throws Exception {
        var webhookResponses = createMockWebhookResponses();
        when(asyncWebhookEmployerService.processEmployersListAsync(any()))
                .thenReturn(CompletableFuture.completedFuture(webhookResponses));

        when(affiliateMercantileRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(new AffiliateMercantile()));

        assertDoesNotThrow(() -> excelWebhookEmployerService.processFullExcelFlowAsync(createMockEmployerRequests()));

        verify(affiliationEmployerActivitiesMercantileService, timeout(1000).times(1))
                .affiliateBUs(eq("NI"), eq("123456789"), eq(0));
    }

    @Test
    void processFullExcelFlowAsync_IndependentContractExists_SkipMercantileAffiliation() throws Exception {
        var webhookResponses = createMockWebhookResponses();
        when(asyncWebhookEmployerService.processEmployersListAsync(any()))
                .thenReturn(CompletableFuture.completedFuture(webhookResponses));

        when(affiliateMercantileRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Collections.singletonList(new Affiliate()));

        assertDoesNotThrow(() -> excelWebhookEmployerService.processFullExcelFlowAsync(createMockEmployerRequests()));
        verify(affiliationEmployerActivitiesMercantileService, never()).affiliateBUs(anyString(), anyString(), anyInt());
    }

    @Test
    void processFullExcelFlowAsync_MercantileThrowsException_IsCaught() throws Exception {
        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI"); emp.setNumeroDocumento("123456789");
        WebhookEmployerResponseDTO r = new WebhookEmployerResponseDTO(); r.setEmpleador(emp);

        when(asyncWebhookEmployerService.processEmployersListAsync(any()))
                .thenReturn(CompletableFuture.completedFuture(Collections.singletonList(r)));
        when(affiliateMercantileRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        when(affiliationEmployerActivitiesMercantileService.affiliateBUs(anyString(), anyString(), anyInt()))
                .thenThrow(new RuntimeException("boom"));

        try (MockedStatic<Files> mf = mockStatic(Files.class); MockedStatic<Paths> mp = mockStatic(Paths.class)) {
            setupFileMocks(mf, mp);
            assertDoesNotThrow(() -> excelWebhookEmployerService.processFullExcelFlowAsync(createMockEmployerRequests()));
        }
    }

    @Test
    void processExcelFile_WithEmptyResults_ShouldNotProcessAffiliation() throws Exception {
        byte[] excelContent = createValidExcelFile();
        mockMultipartFile(excelContent, "test.xlsx");

        when(asyncWebhookEmployerService.processEmployersListAsync(any()))
                .thenReturn(CompletableFuture.completedFuture(Collections.emptyList()));

        List<WebhookEmployerResponseDTO> result = excelWebhookEmployerService.processExcelFile(multipartFile);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(asyncWebhookEmployerService).processEmployersListAsync(any());
    }

    @Test
    void processExcelFileAsync_Success() throws Exception {
        byte[] excelContent = createValidExcelFile();
        mockMultipartFile(excelContent, "test.xlsx");

        when(webhookEmployerService.processEmployersList(any())).thenReturn(Collections.emptyList());
        when(consultEmployerClient.consult(anyString(), anyString(), anyInt())).thenReturn(null);

        String result = excelWebhookEmployerService.processExcelFileAsync(multipartFile);

        assertNotNull(result);
        assertTrue(result.contains("Procesamiento asíncrono completado"));
    }

    @Test
    void readEmployerFromRow_WithNullCells_ShouldReturnNull() {
        Row row = mock(Row.class);
        when(row.getCell(anyInt())).thenReturn(null);

        var result = ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService,
                "readEmployerFromRow",
                row, 0, 1
        );

        assertNull(result);
    }

    @Test
    void readEmployerFromRow_WithEmptyValues_ShouldReturnNull() {
        Row row = mock(Row.class);
        Cell cell = mock(Cell.class);

        when(row.getCell(anyInt())).thenReturn(cell);
        when(cell.getCellType()).thenReturn(CellType.STRING);
        when(cell.getStringCellValue()).thenReturn("");

        var result = ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService,
                "readEmployerFromRow",
                row, 0, 1
        );

        assertNull(result);
    }

    @Test
    void isValidDocumentType_ValidTypes_ShouldReturnTrue() {
        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService, "isValidDocumentType", "NI"));
        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService, "isValidDocumentType", "CC"));
        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService, "isValidDocumentType", "CE"));
    }

    @Test
    void processWebhookResponsesForAffiliation_WithNullResponses_ShouldNotProcess() {
        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService,
                "processWebhookResponsesForAffiliation",
                (List<WebhookEmployerResponseDTO>) null
        ));
    }

    @Test
    void processWebhookResponsesForAffiliation_WithEmptyResponses_ShouldNotProcess() {
        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService,
                "processWebhookResponsesForAffiliation",
                Collections.emptyList()
        ));
    }

    @Test
    void processEmployerForMercantile_WithException_ShouldLogError() {
        WebhookEmployerResponseDTO.EmployerData employer = new WebhookEmployerResponseDTO.EmployerData();
        employer.setTipoDocumento("NI");
        employer.setNumeroDocumento("123456789");

        when(affiliationEmployerActivitiesMercantileService.affiliateBUs(anyString(), anyString(), anyInt()))
                .thenThrow(new RuntimeException("Test error"));

        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService,
                "processEmployerForMercantile",
                employer
        ));
    }

    @Test
    void loadProcessedList_FileNotExists_ShouldCreateNewList() throws Exception {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class);
             MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            Path mockPath = mock(Path.class);
            Path mockParent = mock(Path.class);

            mockedPaths.when(() -> Paths.get(anyString())).thenReturn(mockPath);
            when(mockPath.getParent()).thenReturn(mockParent);
            mockedFiles.when(() -> Files.exists(mockPath)).thenReturn(false);
            mockedFiles.when(() -> Files.createDirectories(mockParent)).thenReturn(mockParent);
            mockedFiles.when(() -> Files.createFile(mockPath)).thenReturn(mockPath);

            List<String> result = (List<String>) ReflectionTestUtils.invokeMethod(
                    excelWebhookEmployerService, "loadProcessedList", "test.txt");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void saveProcessedList_WithException_ShouldLogError() {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class);
             MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {

            Path mockPath = mock(Path.class);
            mockedPaths.when(() -> Paths.get(anyString())).thenReturn(mockPath);
            mockedFiles.when(() -> Files.write(any(Path.class), any(List.class)))
                    .thenThrow(new IOException("Test error"));

            assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                    excelWebhookEmployerService,
                    "saveProcessedList",
                    "test.txt", Arrays.asList("item1", "item2")
            ));
        }
    }

    @Test
    void saveErrorReportToFile_WithException_ShouldLogError() {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.createDirectories(any(Path.class)))
                    .thenThrow(new IOException("Test error"));

            List<String> errorReport = Arrays.asList("Error 1", "Error 2");

            assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                    excelWebhookEmployerService,
                    "saveErrorReportToFile",
                    errorReport
            ));
        }
    }

    @Test
    void processExcelFileAsync_WithIOException_ShouldThrowRuntimeException() throws Exception {
        when(multipartFile.getOriginalFilename()).thenReturn("test.xlsx");
        when(multipartFile.getInputStream()).thenThrow(new IOException("IO Error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> excelWebhookEmployerService.processExcelFileAsync(multipartFile));

        assertTrue(ex.getMessage().contains("Error iniciando procesamiento asíncrono de Excel"));
    }

    @Test
    void processWebhookResponsesForAffiliation_WithEmployerNull_ShouldSkip() {
        WebhookEmployerResponseDTO response = new WebhookEmployerResponseDTO();
        response.setEmpleador(null);

        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService,
                "processWebhookResponsesForAffiliation",
                Collections.singletonList(response)
        ));
    }

    @Test
    void isValidDocumentType_AllTypes() {
        List<String> validTypes = Arrays.asList("NI", "CC", "CE", "TI", "PA", "PT", "CD");
        String invalidType = "XX";

        for (String type : validTypes) {
            assertTrue((Boolean) ReflectionTestUtils.invokeMethod(
                            excelWebhookEmployerService, "isValidDocumentType", type),
                    "Document type " + type + " should be valid");
        }
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(
                        excelWebhookEmployerService, "isValidDocumentType", invalidType),
                "Document type " + invalidType + " should be invalid");
    }

    @Test
    void readEmployerFromRow_UnexpectedCellType() {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue(true);
        row.createCell(1).setCellValue("123456789");

        WebhookEmployerRequestDTO result = (WebhookEmployerRequestDTO) ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService, "readEmployerFromRow", row, 0, 1);

        assertNull(result);
    }
}
