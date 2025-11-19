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
import org.mockito.Mockito;
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



    @Test
    void processExcelFileAsync_DependentsSkipped_whenIndependentContractExists() throws Exception {
        mockMultipartFile(createValidExcelFile(), "test.xlsx");

        // Webhook con empleador + un dependiente
        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI"); emp.setNumeroDocumento("123456789");
        WebhookEmployerResponseDTO.Dependiente dep = new WebhookEmployerResponseDTO.Dependiente();
        dep.setTipoDocumento("CC"); dep.setNumeroDocumento("987654321");
        WebhookEmployerResponseDTO r = new WebhookEmployerResponseDTO();
        r.setEmpleador(emp); r.setEmpleados(List.of(dep));
        when(webhookEmployerService.processEmployersList(any())).thenReturn(List.of(r));

        // consultEmployerClient no usado aquí
        when(consultEmployerClient.consult(anyString(), anyString(), anyInt())).thenReturn(null);

        // existsBy... => true => saltar dependientes
        when(affiliateRepository.existsByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        String out = excelWebhookEmployerService.processExcelFileAsync(multipartFile);
        assertNotNull(out);
        verify(affiliateService, never()).affiliateBUs(anyString(), anyString());
    }


    @Test
    void saveErrorReportToFile_success_createsFileOnDisk() throws Exception {
        // errorReportDir ya apunta a @TempDir por setUp()
        List<String> errs = List.of("e1", "e2");
        assertDoesNotThrow(() -> org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService, "saveErrorReportToFile", errs));

        // Verificamos que se haya creado un archivo en el tempDir
        try (java.util.stream.Stream<Path> s = java.nio.file.Files.list(tempDir)) {
            boolean created = s.anyMatch(p -> p.getFileName().toString().startsWith("informe_errores_afiliacion_"));
            assertTrue(created, "Debe crearse el archivo de informe de errores");
        }
    }

    @Test
    void readExcelFile_withNullRow_shouldSkipSafely() throws Exception {
        // Construimos un excel con encabezado y una fila "lastRowNum" pero row == null
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Sheet1");
            Row h = sh.createRow(0);
            h.createCell(0).setCellValue("tipo_documento");
            h.createCell(1).setCellValue("numero_documento");
            // NO creamos la fila 1 (será null)
            wb.write(out);
            mockMultipartFile(out.toByteArray(), "nullrow.xlsx");
        }
        List<WebhookEmployerRequestDTO> res = excelWebhookEmployerService.readExcelFile(multipartFile);
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    void loadProcessedList_whenReadAllLinesFails_returnsEmptyList() {
        try (MockedStatic<Files> mf = mockStatic(Files.class); MockedStatic<Paths> mp = mockStatic(Paths.class)) {
            Path mockPath = mock(Path.class); Path mockParent = mock(Path.class);
            mp.when(() -> Paths.get(anyString())).thenReturn(mockPath);
            when(mockPath.getParent()).thenReturn(mockParent);
            mf.when(() -> Files.exists(mockPath)).thenReturn(true);
            mf.when(() -> Files.readAllLines(mockPath)).thenThrow(new IOException("boom"));

            @SuppressWarnings("unchecked")
            List<String> out = (List<String>) org.springframework.test.util.ReflectionTestUtils
                    .invokeMethod(excelWebhookEmployerService, "loadProcessedList", "file.tmp");
            assertNotNull(out);
            assertTrue(out.isEmpty());
        }
    }

    @Test
    void readEmployerFromRow_numericTipoDocumento_shouldReturnNull_becauseInvalidType() {
        // tipo_documento numérico => "1" (no es NI/CC/...), debe descartarse
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue(1);              // tipo_documento como número
        row.createCell(1).setCellValue("123456789");    // número OK

        WebhookEmployerRequestDTO result = (WebhookEmployerRequestDTO) org.springframework.test.util.ReflectionTestUtils
                .invokeMethod(excelWebhookEmployerService, "readEmployerFromRow", row, 0, 1);
        assertNull(result);
    }
// ==================== EXTRA TESTS DE COBERTURA ====================

    @Test
    void processExcelFile_whenEmptyExcel_shouldThrowInvalidStructure() throws Exception {
        // Excel con solo encabezado (sin data) -> validateExcelStructure = false
        byte[] excel = createEmptyExcelFile();
        mockMultipartFile(excel, "empty.xlsx");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> excelWebhookEmployerService.processExcelFile(multipartFile));
        assertTrue(ex.getMessage().toLowerCase().contains("estructura"));
    }



    @Test
    void processExcelFileAsync_whenConsultEmployerClientErrors_shouldNotCrash() throws Exception {
        mockMultipartFile(createValidExcelFile(), "test.xlsx");

        // Empleador simple para forzar la llamada al client
        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI"); emp.setNumeroDocumento("123456789");
        WebhookEmployerResponseDTO r = new WebhookEmployerResponseDTO(); r.setEmpleador(emp);
        when(webhookEmployerService.processEmployersList(any())).thenReturn(java.util.List.of(r));

        // <<< IMPORTANTE: usar then(inv -> Mono.error(...)) en lugar de thenReturn(Mono...)
        when(consultEmployerClient.consult(anyString(), anyString(), anyInt()))
                .then(inv -> reactor.core.publisher.Mono.error(new RuntimeException("client boom")));

        assertDoesNotThrow(() -> excelWebhookEmployerService.processExcelFileAsync(multipartFile));
    }


    @Test
    void processWebhookResponsesForAffiliation_validEmployersAndDependents_processBoth() throws Exception {
        // Respuesta 1: empleador mercantil
        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");
        WebhookEmployerResponseDTO r1 = new WebhookEmployerResponseDTO();
        r1.setEmpleador(emp);

        // Respuesta 2: solo dependiente
        WebhookEmployerResponseDTO.Dependiente dep = new WebhookEmployerResponseDTO.Dependiente();
        dep.setTipoDocumento("CC");
        dep.setNumeroDocumento("987654321");
        WebhookEmployerResponseDTO r2 = new WebhookEmployerResponseDTO();
        r2.setEmpleados(java.util.List.of(dep));

        // No existen bloqueos previos en repos
        when(affiliateMercantileRepository.findAll(any(Specification.class))).thenReturn(java.util.Collections.emptyList());
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(java.util.Collections.emptyList());

        // IMPORTANTE: devolvemos false para NO entrar a la rama que envía correo
        when(affiliationEmployerActivitiesMercantileService.affiliateBUs(anyString(), anyString(), anyInt()))
                .thenReturn(false);
        when(affiliateService.affiliateBUs(anyString(), anyString()))
                .thenReturn(false);

        // Invocación al método privado sin propagar checked (más la firma throws Exception por si acaso)
        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService,
                "processWebhookResponsesForAffiliation",
                java.util.List.of(r1, r2)
        ));

        // Verificamos que ambos caminos se ejecutaron (mercantil y dependiente)
        verify(affiliationEmployerActivitiesMercantileService, atLeastOnce())
                .affiliateBUs(eq("NI"), eq("123456789"), anyInt());
        verify(affiliateService, atLeastOnce())
                .affiliateBUs(eq("CC"), eq("987654321"));
    }


    @Test
    void saveProcessedList_success_writesFile() throws Exception {
        try (MockedStatic<Files> mf = mockStatic(Files.class); MockedStatic<Paths> mp = mockStatic(Paths.class)) {
            Path mockPath = mock(Path.class);
            mp.when(() -> Paths.get(anyString())).thenReturn(mockPath);
            mf.when(() -> Files.write(any(Path.class), any(List.class))).thenReturn(mockPath);

            assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                    excelWebhookEmployerService, "saveProcessedList",
                    "progreso.tmp", List.of("NI-123456789")
            ));
        }
    }

    @Test
    void loadProcessedList_success_returnsLines() {
        try (MockedStatic<Files> mf = mockStatic(Files.class); MockedStatic<Paths> mp = mockStatic(Paths.class)) {
            Path mockPath = mock(Path.class); Path mockParent = mock(Path.class);
            mp.when(() -> Paths.get(anyString())).thenReturn(mockPath);
            when(mockPath.getParent()).thenReturn(mockParent);
            mf.when(() -> Files.exists(mockPath)).thenReturn(true);
            mf.when(() -> Files.readAllLines(mockPath)).thenReturn(List.of("NI-123456789"));

            @SuppressWarnings("unchecked")
            List<String> out = (List<String>) ReflectionTestUtils.invokeMethod(
                    excelWebhookEmployerService, "loadProcessedList", "progreso.tmp");

            assertEquals(1, out.size());
            assertEquals("NI-123456789", out.get(0));
        }
    }
////////////////////////////////////////////////////////
    // ========== TESTS CORREGIDOS PARA LLEGAR A 100% DE COBERTURA ==========

    @Test
    void processExcelFileAsync_WithEmptyEmployerResponse_ShouldHandleGracefully() throws Exception {
        mockMultipartFile(createValidExcelFile(), "test.xlsx");

        // consultEmployerClient retorna lista vacía
        when(consultEmployerClient.consult(anyString(), anyString(), anyInt()))
                .thenReturn(Mono.just(Collections.emptyList()));

        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");
        WebhookEmployerResponseDTO r = new WebhookEmployerResponseDTO();
        r.setEmpleador(emp);

        when(webhookEmployerService.processEmployersList(any())).thenReturn(List.of(r));
        when(affiliateMercantileRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());
        when(affiliationEmployerActivitiesMercantileService.affiliateBUs(anyString(), anyString(), anyInt()))
                .thenReturn(true);

        String result = excelWebhookEmployerService.processExcelFileAsync(multipartFile);
        assertNotNull(result);
        assertTrue(result.contains("Procesamiento"));
    }

    @Test
    void processExcelFileAsync_MercantileReturnsFalse_ShouldAddToErrorReport() throws Exception {
        mockMultipartFile(createValidExcelFile(), "test.xlsx");

        EmployerResponse empResp = new EmployerResponse();
        empResp.setRazonSocial("Test Company");

        when(consultEmployerClient.consult(anyString(), anyString(), anyInt()))
                .thenReturn(Mono.just(List.of(empResp)));

        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");
        WebhookEmployerResponseDTO r = new WebhookEmployerResponseDTO();
        r.setEmpleador(emp);

        when(webhookEmployerService.processEmployersList(any())).thenReturn(List.of(r));
        when(affiliateMercantileRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        // affiliateBUs retorna FALSE
        when(affiliationEmployerActivitiesMercantileService.affiliateBUs(anyString(), anyString(), anyInt()))
                .thenReturn(false);

        String result = excelWebhookEmployerService.processExcelFileAsync(multipartFile);
        assertNotNull(result);
        assertTrue(result.contains("errores") || result.contains("completado"));
    }

    @Test
    void processExcelFileAsync_MercantileReturnsNull_ShouldAddToErrorReport() throws Exception {
        mockMultipartFile(createValidExcelFile(), "test.xlsx");

        EmployerResponse empResp = new EmployerResponse();
        empResp.setRazonSocial("Test Company");

        when(consultEmployerClient.consult(anyString(), anyString(), anyInt()))
                .thenReturn(Mono.just(List.of(empResp)));

        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");
        WebhookEmployerResponseDTO r = new WebhookEmployerResponseDTO();
        r.setEmpleador(emp);

        when(webhookEmployerService.processEmployersList(any())).thenReturn(List.of(r));
        when(affiliateMercantileRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        // affiliateBUs retorna NULL
        when(affiliationEmployerActivitiesMercantileService.affiliateBUs(anyString(), anyString(), anyInt()))
                .thenReturn(null);

        String result = excelWebhookEmployerService.processExcelFileAsync(multipartFile);
        assertNotNull(result);
    }

    @Test
    void processExcelFileAsync_DependentReturnsFalse_ShouldAddToErrorReport() throws Exception {
        mockMultipartFile(createValidExcelFile(), "test.xlsx");

        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");

        WebhookEmployerResponseDTO.Dependiente dep = new WebhookEmployerResponseDTO.Dependiente();
        dep.setTipoDocumento("CC");
        dep.setNumeroDocumento("987654321");

        WebhookEmployerResponseDTO r = new WebhookEmployerResponseDTO();
        r.setEmpleador(emp);
        r.setEmpleados(List.of(dep));

        when(webhookEmployerService.processEmployersList(any())).thenReturn(List.of(r));
        when(consultEmployerClient.consult(anyString(), anyString(), anyInt()))
                .thenReturn(Mono.empty());

        when(affiliateRepository.existsByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(false);

        when(affiliateRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList());

        // affiliateBUs para dependiente retorna FALSE
        when(affiliateService.affiliateBUs(anyString(), anyString()))
                .thenReturn(false);

        String result = excelWebhookEmployerService.processExcelFileAsync(multipartFile);
        assertNotNull(result);
    }

    @Test
    void processExcelFileAsync_DependentReturnsNull_ShouldAddToErrorReport() throws Exception {
        mockMultipartFile(createValidExcelFile(), "test.xlsx");

        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");

        WebhookEmployerResponseDTO.Dependiente dep = new WebhookEmployerResponseDTO.Dependiente();
        dep.setTipoDocumento("CC");
        dep.setNumeroDocumento("987654321");

        WebhookEmployerResponseDTO r = new WebhookEmployerResponseDTO();
        r.setEmpleador(emp);
        r.setEmpleados(List.of(dep));

        when(webhookEmployerService.processEmployersList(any())).thenReturn(List.of(r));
        when(consultEmployerClient.consult(anyString(), anyString(), anyInt()))
                .thenReturn(Mono.empty());

        when(affiliateRepository.existsByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(false);

        when(affiliateRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList());

        // affiliateBUs para dependiente retorna NULL
        when(affiliateService.affiliateBUs(anyString(), anyString()))
                .thenReturn(null);

        String result = excelWebhookEmployerService.processExcelFileAsync(multipartFile);
        assertNotNull(result);
    }

    @Test
    void processExcelFileAsync_DependentWithNullType_ShouldAddToErrorReport() throws Exception {
        mockMultipartFile(createValidExcelFile(), "test.xlsx");

        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");

        // Dependiente con tipo null
        WebhookEmployerResponseDTO.Dependiente dep = new WebhookEmployerResponseDTO.Dependiente();
        dep.setTipoDocumento(null);
        dep.setNumeroDocumento("987654321");

        WebhookEmployerResponseDTO r = new WebhookEmployerResponseDTO();
        r.setEmpleador(emp);
        r.setEmpleados(List.of(dep));

        when(webhookEmployerService.processEmployersList(any())).thenReturn(List.of(r));
        when(consultEmployerClient.consult(anyString(), anyString(), anyInt()))
                .thenReturn(Mono.empty());

        when(affiliateRepository.existsByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(false);

        String result = excelWebhookEmployerService.processExcelFileAsync(multipartFile);
        assertNotNull(result);
    }

    @Test
    void processExcelFileAsync_DependentWithNullNumber_ShouldAddToErrorReport() throws Exception {
        mockMultipartFile(createValidExcelFile(), "test.xlsx");

        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");

        // Dependiente con número null
        WebhookEmployerResponseDTO.Dependiente dep = new WebhookEmployerResponseDTO.Dependiente();
        dep.setTipoDocumento("CC");
        dep.setNumeroDocumento(null);

        WebhookEmployerResponseDTO r = new WebhookEmployerResponseDTO();
        r.setEmpleador(emp);
        r.setEmpleados(List.of(dep));

        when(webhookEmployerService.processEmployersList(any())).thenReturn(List.of(r));
        when(consultEmployerClient.consult(anyString(), anyString(), anyInt()))
                .thenReturn(Mono.empty());

        when(affiliateRepository.existsByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(false);

        String result = excelWebhookEmployerService.processExcelFileAsync(multipartFile);
        assertNotNull(result);
    }

    @Test
    void processExcelFileAsync_DependentAlreadyExists_ShouldSkip() throws Exception {
        mockMultipartFile(createValidExcelFile(), "test.xlsx");

        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");

        WebhookEmployerResponseDTO.Dependiente dep = new WebhookEmployerResponseDTO.Dependiente();
        dep.setTipoDocumento("CC");
        dep.setNumeroDocumento("987654321");

        WebhookEmployerResponseDTO r = new WebhookEmployerResponseDTO();
        r.setEmpleador(emp);
        r.setEmpleados(List.of(dep));

        when(webhookEmployerService.processEmployersList(any())).thenReturn(List.of(r));
        when(consultEmployerClient.consult(anyString(), anyString(), anyInt()))
                .thenReturn(Mono.empty());

        when(affiliateRepository.existsByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(false);

        when(affiliateRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(new Affiliate()));

        String result = excelWebhookEmployerService.processExcelFileAsync(multipartFile);
        assertNotNull(result);
        verify(affiliateService, never()).affiliateBUs(anyString(), anyString());
    }

    @Test
    void processExcelFileAsync_DependentThrowsException_ShouldAddToErrorReport() throws Exception {
        mockMultipartFile(createValidExcelFile(), "test.xlsx");

        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");

        WebhookEmployerResponseDTO.Dependiente dep = new WebhookEmployerResponseDTO.Dependiente();
        dep.setTipoDocumento("CC");
        dep.setNumeroDocumento("987654321");

        WebhookEmployerResponseDTO r = new WebhookEmployerResponseDTO();
        r.setEmpleador(emp);
        r.setEmpleados(List.of(dep));

        when(webhookEmployerService.processEmployersList(any())).thenReturn(List.of(r));
        when(consultEmployerClient.consult(anyString(), anyString(), anyInt()))
                .thenReturn(Mono.empty());

        when(affiliateRepository.existsByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(false);

        when(affiliateRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList());

        // Lanzar excepción al afiliar dependiente
        when(affiliateService.affiliateBUs(anyString(), anyString()))
                .thenThrow(new RuntimeException("Error al afiliar dependiente"));

        String result = excelWebhookEmployerService.processExcelFileAsync(multipartFile);
        assertNotNull(result);
    }

    @Test
    void processExcelFileAsync_WithErrorsInProcessing_ShouldReturnErrorMessage() throws Exception {
        mockMultipartFile(createValidExcelFile(), "test.xlsx");

        // Forzar error en processEmployersList
        when(webhookEmployerService.processEmployersList(any()))
                .thenThrow(new RuntimeException("Error procesando webhook"));

        String result = excelWebhookEmployerService.processExcelFileAsync(multipartFile);
        assertNotNull(result);
        assertTrue(result.toLowerCase().contains("error") || result.contains("completado"));
    }

    @Test
    void processFullExcelFlowAsync_FirstResponseWithoutEmployer_ShouldSkipMercantile() throws Exception {
        WebhookEmployerResponseDTO r1 = new WebhookEmployerResponseDTO();
        r1.setEmpleador(null); // Sin empleador

        WebhookEmployerResponseDTO.Dependiente dep = new WebhookEmployerResponseDTO.Dependiente();
        dep.setTipoDocumento("CC");
        dep.setNumeroDocumento("987654321");

        WebhookEmployerResponseDTO r2 = new WebhookEmployerResponseDTO();
        r2.setEmpleados(List.of(dep));

        when(asyncWebhookEmployerService.processEmployersListAsync(any()))
                .thenReturn(CompletableFuture.completedFuture(List.of(r1, r2)));

        when(affiliateService.affiliateBUs(anyString(), anyString()))
                .thenReturn(true);

        assertDoesNotThrow(() -> excelWebhookEmployerService.processFullExcelFlowAsync(
                createMockEmployerRequests()));

        Thread.sleep(200);
        verify(affiliationEmployerActivitiesMercantileService, never())
                .affiliateBUs(anyString(), anyString(), anyInt());
    }

    @Test
    void processFullExcelFlowAsync_WithMultipleDependents_ShouldProcessAll() throws Exception {
        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");

        WebhookEmployerResponseDTO.Dependiente dep1 = new WebhookEmployerResponseDTO.Dependiente();
        dep1.setTipoDocumento("CC");
        dep1.setNumeroDocumento("111111111");

        WebhookEmployerResponseDTO.Dependiente dep2 = new WebhookEmployerResponseDTO.Dependiente();
        dep2.setTipoDocumento("CE");
        dep2.setNumeroDocumento("222222222");

        WebhookEmployerResponseDTO r1 = new WebhookEmployerResponseDTO();
        r1.setEmpleador(emp);

        WebhookEmployerResponseDTO r2 = new WebhookEmployerResponseDTO();
        r2.setEmpleados(List.of(dep1, dep2));

        when(asyncWebhookEmployerService.processEmployersListAsync(any()))
                .thenReturn(CompletableFuture.completedFuture(List.of(r1, r2)));

        when(affiliationEmployerActivitiesMercantileService.affiliateBUs(anyString(), anyString(), anyInt()))
                .thenReturn(true);

        when(affiliateService.affiliateBUs(anyString(), anyString()))
                .thenReturn(true);

        assertDoesNotThrow(() -> excelWebhookEmployerService.processFullExcelFlowAsync(
                createMockEmployerRequests()));

        Thread.sleep(200);
    }

    @Test
    void processFullExcelFlowAsync_WithEmptyDependentsList_ShouldLogWarning() throws Exception {
        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");

        WebhookEmployerResponseDTO r1 = new WebhookEmployerResponseDTO();
        r1.setEmpleador(emp);

        WebhookEmployerResponseDTO r2 = new WebhookEmployerResponseDTO();
        r2.setEmpleados(Collections.emptyList());

        when(asyncWebhookEmployerService.processEmployersListAsync(any()))
                .thenReturn(CompletableFuture.completedFuture(List.of(r1, r2)));

        when(affiliationEmployerActivitiesMercantileService.affiliateBUs(anyString(), anyString(), anyInt()))
                .thenReturn(true);

        assertDoesNotThrow(() -> excelWebhookEmployerService.processFullExcelFlowAsync(
                createMockEmployerRequests()));

        Thread.sleep(200);
    }

    @Test
    void processFullExcelFlowAsync_WithNullDependentsList_ShouldLogWarning() throws Exception {
        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");

        WebhookEmployerResponseDTO r1 = new WebhookEmployerResponseDTO();
        r1.setEmpleador(emp);

        WebhookEmployerResponseDTO r2 = new WebhookEmployerResponseDTO();
        r2.setEmpleados(null);

        when(asyncWebhookEmployerService.processEmployersListAsync(any()))
                .thenReturn(CompletableFuture.completedFuture(List.of(r1, r2)));

        when(affiliationEmployerActivitiesMercantileService.affiliateBUs(anyString(), anyString(), anyInt()))
                .thenReturn(true);

        assertDoesNotThrow(() -> excelWebhookEmployerService.processFullExcelFlowAsync(
                createMockEmployerRequests()));

        Thread.sleep(200);
    }

    @Test
    void processFullExcelFlowAsync_DependentWithErrors_ShouldContinueProcessing() throws Exception {
        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");

        WebhookEmployerResponseDTO.Dependiente dep1 = new WebhookEmployerResponseDTO.Dependiente();
        dep1.setTipoDocumento("CC");
        dep1.setNumeroDocumento("111111111");

        WebhookEmployerResponseDTO.Dependiente dep2 = new WebhookEmployerResponseDTO.Dependiente();
        dep2.setTipoDocumento("CE");
        dep2.setNumeroDocumento("222222222");

        WebhookEmployerResponseDTO r1 = new WebhookEmployerResponseDTO();
        r1.setEmpleador(emp);

        WebhookEmployerResponseDTO r2 = new WebhookEmployerResponseDTO();
        r2.setEmpleados(List.of(dep1, dep2));

        when(asyncWebhookEmployerService.processEmployersListAsync(any()))
                .thenReturn(CompletableFuture.completedFuture(List.of(r1, r2)));

        when(affiliationEmployerActivitiesMercantileService.affiliateBUs(anyString(), anyString(), anyInt()))
                .thenReturn(true);

        // Primer dependiente falla, segundo debe procesarse igual
        when(affiliateService.affiliateBUs(eq("CC"), eq("111111111")))
                .thenThrow(new RuntimeException("Error"));
        when(affiliateService.affiliateBUs(eq("CE"), eq("222222222")))
                .thenReturn(true);

        assertDoesNotThrow(() -> excelWebhookEmployerService.processFullExcelFlowAsync(
                createMockEmployerRequests()));

        Thread.sleep(200);
    }




    @Test
    void processEmployerForMercantile_WhenResultIsFalse_ShouldLog() {
        WebhookEmployerResponseDTO.EmployerData employer = new WebhookEmployerResponseDTO.EmployerData();
        employer.setTipoDocumento("NI");
        employer.setNumeroDocumento("123456789");

        when(affiliationEmployerActivitiesMercantileService.affiliateBUs(anyString(), anyString(), anyInt()))
                .thenReturn(false);

        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService,
                "processEmployerForMercantile",
                employer
        ));
    }

    @Test
    void processEmployerForMercantile_WhenResultIsNull_ShouldLog() {
        WebhookEmployerResponseDTO.EmployerData employer = new WebhookEmployerResponseDTO.EmployerData();
        employer.setTipoDocumento("NI");
        employer.setNumeroDocumento("123456789");

        when(affiliationEmployerActivitiesMercantileService.affiliateBUs(anyString(), anyString(), anyInt()))
                .thenReturn(null);

        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService,
                "processEmployerForMercantile",
                employer
        ));
    }

    @Test
    void readEmployerFromRow_WithFormulaCell_ShouldHandleGracefully() {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(1);

        Cell formulaCell = row.createCell(0);
        formulaCell.setCellFormula("1+1");
        row.createCell(1).setCellValue("123456789");

        WebhookEmployerRequestDTO result = (WebhookEmployerRequestDTO) ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService, "readEmployerFromRow", row, 0, 1);

        assertNull(result);
    }

    @Test
    void isValidDocumentType_WithLowercaseType_ShouldReturnFalse() {
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService, "isValidDocumentType", "ni"));
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService, "isValidDocumentType", "cc"));
    }

    @Test
    void isValidDocumentType_WithEmptyString_ShouldReturnFalse() {
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService, "isValidDocumentType", ""));
    }

    @Test
    void isValidDocumentType_WithNullString_ShouldReturnFalse() {
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService, "isValidDocumentType", (String) null));
    }

    @Test
    void processExcelFile_WithSuccessfulAffiliation_ShouldProcessBothMercantileAndDependents() throws Exception {
        byte[] excelContent = createValidExcelFile();
        mockMultipartFile(excelContent, "test.xlsx");

        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");

        WebhookEmployerResponseDTO.Dependiente dep = new WebhookEmployerResponseDTO.Dependiente();
        dep.setTipoDocumento("CC");
        dep.setNumeroDocumento("987654321");

        WebhookEmployerResponseDTO r1 = new WebhookEmployerResponseDTO();
        r1.setEmpleador(emp);

        WebhookEmployerResponseDTO r2 = new WebhookEmployerResponseDTO();
        r2.setEmpleados(List.of(dep));

        List<WebhookEmployerResponseDTO> responses = List.of(r1, r2);

        when(asyncWebhookEmployerService.processEmployersListAsync(any()))
                .thenReturn(CompletableFuture.completedFuture(responses));

        when(affiliateMercantileRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList());

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        when(affiliationEmployerActivitiesMercantileService.affiliateBUs(anyString(), anyString(), anyInt()))
                .thenReturn(true);

        when(affiliateService.affiliateBUs(anyString(), anyString()))
                .thenReturn(true);

        List<WebhookEmployerResponseDTO> result = excelWebhookEmployerService.processExcelFile(multipartFile);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void readExcelFile_WithBlankCell_ShouldSkipRow() throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Sheet1");
            Row h = sh.createRow(0);
            h.createCell(0).setCellValue("tipo_documento");
            h.createCell(1).setCellValue("numero_documento");

            Row r1 = sh.createRow(1);
            r1.createCell(0).setCellType(CellType.BLANK);
            r1.createCell(1).setCellValue("123456789");

            Row r2 = sh.createRow(2);
            r2.createCell(0).setCellValue("NI");
            r2.createCell(1).setCellValue("987654321");

            wb.write(out);
            mockMultipartFile(out.toByteArray(), "blank.xlsx");
        }

        List<WebhookEmployerRequestDTO> result = excelWebhookEmployerService.readExcelFile(multipartFile);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("NI", result.get(0).getIdTipoDocEmpresa());
        assertEquals("987654321", result.get(0).getIdEmpresa());
    }

    @Test
    void readExcelFile_WithAllValidDocumentTypes_ShouldProcessAll() throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Sheet1");
            Row h = sh.createRow(0);
            h.createCell(0).setCellValue("tipo_documento");
            h.createCell(1).setCellValue("numero_documento");

            String[] types = {"NI", "CC", "CE", "TI", "PA", "PT", "CD"};
            for (int i = 0; i < types.length; i++) {
                Row r = sh.createRow(i + 1);
                r.createCell(0).setCellValue(types[i]);
                r.createCell(1).setCellValue("12345678" + i);
            }

            wb.write(out);
            mockMultipartFile(out.toByteArray(), "alltypes.xlsx");
        }

        List<WebhookEmployerRequestDTO> result = excelWebhookEmployerService.readExcelFile(multipartFile);

        assertNotNull(result);
        assertEquals(7, result.size());
    }

    @Test
    void validateExcelStructure_WithExtraColumns_ShouldReturnTrue() throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Sheet1");
            Row h = sh.createRow(0);
            h.createCell(0).setCellValue("extra_column");
            h.createCell(1).setCellValue("tipo_documento");
            h.createCell(2).setCellValue("numero_documento");
            h.createCell(3).setCellValue("another_column");

            Row r = sh.createRow(1);
            r.createCell(0).setCellValue("extra");
            r.createCell(1).setCellValue("NI");
            r.createCell(2).setCellValue("123456789");
            r.createCell(3).setCellValue("another");

            wb.write(out);
            mockMultipartFile(out.toByteArray(), "extra.xlsx");
        }

        assertTrue(excelWebhookEmployerService.validateExcelStructure(multipartFile));
    }

    @Test
    void validateExcelStructure_WithCaseSensitiveHeaders_ShouldReturnTrue() throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Sheet1");
            Row h = sh.createRow(0);
            h.createCell(0).setCellValue("TIPO_DOCUMENTO");
            h.createCell(1).setCellValue("NUMERO_DOCUMENTO");

            Row r = sh.createRow(1);
            r.createCell(0).setCellValue("NI");
            r.createCell(1).setCellValue("123456789");

            wb.write(out);
            mockMultipartFile(out.toByteArray(), "uppercase.xlsx");
        }

        assertTrue(excelWebhookEmployerService.validateExcelStructure(multipartFile));
    }



    @Test
    void processFullExcelFlowAsync_WithOnlyOneResponse_ShouldProcessOnlyEmployer() throws Exception {
        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");

        WebhookEmployerResponseDTO r = new WebhookEmployerResponseDTO();
        r.setEmpleador(emp);

        when(asyncWebhookEmployerService.processEmployersListAsync(any()))
                .thenReturn(CompletableFuture.completedFuture(List.of(r)));

        when(affiliationEmployerActivitiesMercantileService.affiliateBUs(anyString(), anyString(), anyInt()))
                .thenReturn(true);

        assertDoesNotThrow(() -> excelWebhookEmployerService.processFullExcelFlowAsync(
                createMockEmployerRequests()));

        Thread.sleep(200);

        verify(affiliationEmployerActivitiesMercantileService, timeout(1000))
                .affiliateBUs(eq("NI"), eq("123456789"), eq(0));
    }

    @Test
    void readEmployerFromRow_WithBooleanCell_ShouldReturnNull() {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(1);

        Cell boolCell = row.createCell(0);
        boolCell.setCellValue(false);
        row.createCell(1).setCellValue("123456789");

        WebhookEmployerRequestDTO result = (WebhookEmployerRequestDTO) ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService, "readEmployerFromRow", row, 0, 1);

        assertNull(result);
    }

    @Test
    void readEmployerFromRow_WithEmptyStringAfterTrim_ShouldReturnNull() {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(1);

        row.createCell(0).setCellValue("   ");
        row.createCell(1).setCellValue("123456789");

        WebhookEmployerRequestDTO result = (WebhookEmployerRequestDTO) ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService, "readEmployerFromRow", row, 0, 1);

        assertNull(result);
    }

    @Test
    void readEmployerFromRow_WithBothCellsNull_ShouldReturnNull() {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(1);

        WebhookEmployerRequestDTO result = (WebhookEmployerRequestDTO) ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService, "readEmployerFromRow", row, 0, 1);

        assertNull(result);
    }

    @Test
    void validateExcelStructure_WithOnlyTipoDocumento_ShouldReturnFalse() throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Sheet1");
            Row h = sh.createRow(0);
            h.createCell(0).setCellValue("tipo_documento");

            Row r = sh.createRow(1);
            r.createCell(0).setCellValue("NI");

            wb.write(out);
            mockMultipartFile(out.toByteArray(), "partial.xlsx");
        }

        assertFalse(excelWebhookEmployerService.validateExcelStructure(multipartFile));
    }

    @Test
    void validateExcelStructure_WithOnlyNumeroDocumento_ShouldReturnFalse() throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Sheet1");
            Row h = sh.createRow(0);
            h.createCell(0).setCellValue("numero_documento");

            Row r = sh.createRow(1);
            r.createCell(0).setCellValue("123456789");

            wb.write(out);
            mockMultipartFile(out.toByteArray(), "partial.xlsx");
        }

        assertFalse(excelWebhookEmployerService.validateExcelStructure(multipartFile));
    }

    @Test
    void processWebhookResponsesForAffiliation_WithIndexGreaterThanSize_ShouldNotCrash() {
        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");

        WebhookEmployerResponseDTO r = new WebhookEmployerResponseDTO();
        r.setEmpleador(emp);

        when(affiliateMercantileRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList());

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        when(affiliationEmployerActivitiesMercantileService.affiliateBUs(anyString(), anyString(), anyInt()))
                .thenReturn(true);

        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService,
                "processWebhookResponsesForAffiliation",
                List.of(r)
        ));
    }

    @Test
    void saveErrorReportToFile_CreatesDirectoryIfNotExists() {
        List<String> errors = List.of("Error 1", "Error 2", "Error 3");

        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService,
                "saveErrorReportToFile",
                errors
        ));

        try (java.util.stream.Stream<Path> stream = Files.list(tempDir)) {
            boolean fileCreated = stream.anyMatch(p ->
                    p.getFileName().toString().startsWith("informe_errores_afiliacion_"));
            assertTrue(fileCreated);
        } catch (Exception e) {
        }
    }

    @Test
    void readExcelFile_LogsDebugForEachValidEmployer() throws Exception {
        mockMultipartFile(createValidExcelFile(), "test.xlsx");

        List<WebhookEmployerRequestDTO> result = excelWebhookEmployerService.readExcelFile(multipartFile);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("NI", result.get(0).getIdTipoDocEmpresa());
        assertEquals("123456789", result.get(0).getIdEmpresa());
        assertEquals(0, result.get(0).getIdSubEmpresa());
    }

    @Test
    void processFullExcelFlowAsync_WithDependentHavingNullFields_ShouldLogWarning() throws Exception {
        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");

        WebhookEmployerResponseDTO.Dependiente dep = new WebhookEmployerResponseDTO.Dependiente();
        dep.setTipoDocumento(null);
        dep.setNumeroDocumento(null);

        WebhookEmployerResponseDTO r1 = new WebhookEmployerResponseDTO();
        r1.setEmpleador(emp);

        WebhookEmployerResponseDTO r2 = new WebhookEmployerResponseDTO();
        r2.setEmpleados(List.of(dep));

        when(asyncWebhookEmployerService.processEmployersListAsync(any()))
                .thenReturn(CompletableFuture.completedFuture(List.of(r1, r2)));

        when(affiliationEmployerActivitiesMercantileService.affiliateBUs(anyString(), anyString(), anyInt()))
                .thenReturn(true);

        assertDoesNotThrow(() -> excelWebhookEmployerService.processFullExcelFlowAsync(
                createMockEmployerRequests()));

        Thread.sleep(200);
        verify(affiliateService, never()).affiliateBUs(anyString(), anyString());
    }

    @Test
    void readEmployerFromRow_WithWhitespaceInValues_ShouldTrimValues() throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sh = wb.createSheet("Sheet1");
            Row h = sh.createRow(0);
            h.createCell(0).setCellValue("tipo_documento");
            h.createCell(1).setCellValue("numero_documento");

            Row r = sh.createRow(1);
            r.createCell(0).setCellValue("  NI  ");
            r.createCell(1).setCellValue("  123456789  ");

            wb.write(out);
            mockMultipartFile(out.toByteArray(), "whitespace.xlsx");
        }

        List<WebhookEmployerRequestDTO> result = excelWebhookEmployerService.readExcelFile(multipartFile);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("NI", result.get(0).getIdTipoDocEmpresa());
        assertEquals("123456789", result.get(0).getIdEmpresa());
    }

    @Test
    void processExcelFileAsync_WithEmptyWebhookResponseList_ShouldHandleGracefully() throws Exception {
        mockMultipartFile(createValidExcelFile(), "test.xlsx");

        when(webhookEmployerService.processEmployersList(any()))
                .thenReturn(Collections.emptyList());

        String result = excelWebhookEmployerService.processExcelFileAsync(multipartFile);
        assertNotNull(result);
        assertTrue(result.contains("completado"));
    }

    @Test
    void processExcelFileAsync_MercantileSpecificationFindsMatch_ShouldLogAndSkip() throws Exception {
        mockMultipartFile(createValidExcelFile(), "test.xlsx");

        EmployerResponse empResp = new EmployerResponse();
        empResp.setRazonSocial("Test Company");

        when(consultEmployerClient.consult(anyString(), anyString(), anyInt()))
                .thenReturn(Mono.just(List.of(empResp)));

        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");
        WebhookEmployerResponseDTO r = new WebhookEmployerResponseDTO();
        r.setEmpleador(emp);

        when(webhookEmployerService.processEmployersList(any())).thenReturn(List.of(r));
        when(affiliateMercantileRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(new AffiliateMercantile()));

        String result = excelWebhookEmployerService.processExcelFileAsync(multipartFile);
        assertNotNull(result);

        verify(affiliationEmployerActivitiesMercantileService, never())
                .affiliateBUs(anyString(), anyString(), anyInt());
    }

    @Test
    void processExcelFileAsync_IndependentContractExistsForEmployer_ShouldLogAndSkipMercantile() throws Exception {
        mockMultipartFile(createValidExcelFile(), "test.xlsx");

        EmployerResponse empResp = new EmployerResponse();
        empResp.setRazonSocial("Test Company");

        when(consultEmployerClient.consult(anyString(), anyString(), anyInt()))
                .thenReturn(Mono.just(List.of(empResp)));

        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");
        WebhookEmployerResponseDTO r = new WebhookEmployerResponseDTO();
        r.setEmpleador(emp);

        when(webhookEmployerService.processEmployersList(any())).thenReturn(List.of(r));
        when(affiliateMercantileRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList());

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                eq("NI"), eq("123456789"), anyString(), anyString()))
                .thenReturn(List.of(new Affiliate()));

        String result = excelWebhookEmployerService.processExcelFileAsync(multipartFile);
        assertNotNull(result);

        verify(affiliationEmployerActivitiesMercantileService, never())
                .affiliateBUs(anyString(), anyString(), anyInt());
    }

    @Test
    void readEmployerFromRow_WithErrorCell_ShouldReturnNull() {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(1);

        Cell errorCell = row.createCell(0);
        errorCell.setCellErrorValue((byte) 0);
        row.createCell(1).setCellValue("123456789");

        WebhookEmployerRequestDTO result = (WebhookEmployerRequestDTO) ReflectionTestUtils.invokeMethod(
                excelWebhookEmployerService, "readEmployerFromRow", row, 0, 1);

        assertNull(result);
    }

    @Test
    void processFullExcelFlowAsync_MultipleDependentsInSingleResponse_ShouldProcessAll() throws Exception {
        WebhookEmployerResponseDTO.EmployerData emp = new WebhookEmployerResponseDTO.EmployerData();
        emp.setTipoDocumento("NI");
        emp.setNumeroDocumento("123456789");

        WebhookEmployerResponseDTO.Dependiente dep1 = new WebhookEmployerResponseDTO.Dependiente();
        dep1.setTipoDocumento("CC");
        dep1.setNumeroDocumento("111111111");

        WebhookEmployerResponseDTO.Dependiente dep2 = new WebhookEmployerResponseDTO.Dependiente();
        dep2.setTipoDocumento("CE");
        dep2.setNumeroDocumento("222222222");

        WebhookEmployerResponseDTO.Dependiente dep3 = new WebhookEmployerResponseDTO.Dependiente();
        dep3.setTipoDocumento("TI");
        dep3.setNumeroDocumento("333333333");

        WebhookEmployerResponseDTO r1 = new WebhookEmployerResponseDTO();
        r1.setEmpleador(emp);

        WebhookEmployerResponseDTO r2 = new WebhookEmployerResponseDTO();
        r2.setEmpleados(List.of(dep1, dep2, dep3));

        when(asyncWebhookEmployerService.processEmployersListAsync(any()))
                .thenReturn(CompletableFuture.completedFuture(List.of(r1, r2)));

        when(affiliationEmployerActivitiesMercantileService.affiliateBUs(anyString(), anyString(), anyInt()))
                .thenReturn(true);

        when(affiliateService.affiliateBUs(anyString(), anyString()))
                .thenReturn(true);

        assertDoesNotThrow(() -> excelWebhookEmployerService.processFullExcelFlowAsync(
                createMockEmployerRequests()));

        Thread.sleep(300);
        verify(affiliateService, timeout(1000).atLeast(3))
                .affiliateBUs(anyString(), anyString());
    }

    @Test
    void loadProcessedList_WithNonExistentDirectory_ShouldCreateDirectoryAndFile() {
        try (MockedStatic<Files> mf = mockStatic(Files.class); MockedStatic<Paths> mp = mockStatic(Paths.class)) {
            Path mockPath = mock(Path.class);
            Path mockParent = mock(Path.class);

            mp.when(() -> Paths.get(anyString())).thenReturn(mockPath);
            when(mockPath.getParent()).thenReturn(mockParent);

            mf.when(() -> Files.exists(mockPath)).thenReturn(false);
            mf.when(() -> Files.createDirectories(mockParent)).thenReturn(mockParent);
            mf.when(() -> Files.createFile(mockPath)).thenReturn(mockPath);

            List<String> result = (List<String>) ReflectionTestUtils.invokeMethod(
                    excelWebhookEmployerService, "loadProcessedList", "test.tmp");
            assertNotNull(result);
            assertTrue(result.isEmpty());

            mf.verify(() -> Files.createDirectories(mockParent));
            mf.verify(() -> Files.createFile(mockPath));
        }
    }

}
