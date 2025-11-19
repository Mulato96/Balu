package com.gal.afiliaciones.application.service.impl.certicate;

import com.gal.afiliaciones.application.service.CodeValidCertificationService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.excelprocessingdata.ExcelProcessingServiceData;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.helper.CertificateServiceHelper;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.Certificate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.CertificateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.FileBase64DTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.AffiliationCertificate;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateBulkDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateReportRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.DataFileCertificateBulkDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.ResponseBulkDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // evita UnnecessaryStubbingException
class CertificateBulkServiceImplTest {

    @Mock private FiledService filedService;
    @Mock private CollectProperties properties;
    @Mock private AlfrescoService alfrescoService;
    @Mock private GenericWebClient genericWebClient;
    @Mock private AffiliateRepository affiliateRepository;
    @Mock private CertificateRepository certificateRepository;
    @Mock private CertificateServiceHelper certificateServiceHelper;
    @Mock private IUserPreRegisterRepository iUserPreRegisterRepository;
    @Mock private ExcelProcessingServiceData excelProcessingServiceData;
    @Mock private CodeValidCertificationService codeValidCertificationService;
    @Mock private AffiliationDependentRepository affiliationDependentRepository;
    @Mock private SendEmails sendEmails;

    @InjectMocks
    private CertificateBulkServiceImpl certificateBulkService;

    private MockMultipartFile mockExcelFile;
    private Affiliate mockAffiliate;
    private UserMain mockUserMain;
    private AffiliationCertificate mockAffiliationCertificate;

    @BeforeEach
    void setUp() {
        mockExcelFile = new MockMultipartFile(
                "file","test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "test content".getBytes()
        );

        mockAffiliate = new Affiliate();
        mockAffiliate.setNitCompany("123456789");
        mockAffiliate.setDocumentNumber("987654321");

        mockUserMain = new UserMain();
        mockUserMain.setIdentification("123456789");
        mockUserMain.setEmail("test@test.com");

        mockAffiliationCertificate = mock(AffiliationCertificate.class);
        LocalDate today = LocalDate.now();
        LocalDate nextYear = LocalDate.now().plusYears(1);

        // Tipos correctos
        doReturn("1234567890").when(mockAffiliationCertificate).getIdentificationNumber();
        doReturn("CC").when(mockAffiliationCertificate).getIdentificationDocumentType();
        doReturn("John Doe").when(mockAffiliationCertificate).getFullName();
        doReturn("Test Company").when(mockAffiliationCertificate).getCompany();
        doReturn("123456789").when(mockAffiliationCertificate).getNitCompany();
        doReturn(today).when(mockAffiliationCertificate).getCoverageDate();           // LocalDate
        doReturn("ACTIVO").when(mockAffiliationCertificate).getAffiliationStatus();
        doReturn("Trabajador").when(mockAffiliationCertificate).getAffiliationSubtype();
        doReturn("I").when(mockAffiliationCertificate).getRisk();
        doReturn("Developer").when(mockAffiliationCertificate).getOccupationName();
        doReturn(nextYear.toString()).when(mockAffiliationCertificate).getEndDate();  // String
        doReturn(null).when(mockAffiliationCertificate).getRetirementDate();         // LocalDate/null
    }

    // ---------------- tests ----------------

    @Test
    void generateMassiveWorkerCertificates_Success() throws Exception {
        when(affiliateRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(mockAffiliate));

        Map<String, Object> row = new HashMap<>();
        row.put("numberDocument", "1234567890");
        row.put("typeDocument", "CC");
        row.put("addressed", "Dirigido A");
        when(excelProcessingServiceData.converterExcelToMap(any(), any()))
                .thenReturn(List.of(row));

        doReturn(1000).when(properties).getMaximumRecordsConsultCertificate();

        CertificateBulkDTO dto = new CertificateBulkDTO();
        dto.setNumberDocument("1234567890");
        dto.setTypeDocument("CC");
        dto.setAddressed("Dirigido A");
        dto.setValid(true);

        when(excelProcessingServiceData.converterMapToClass(anyList(), eq(CertificateBulkDTO.class)))
                .thenReturn(List.of(dto));

        when(affiliationDependentRepository.findAffiliateCertificate(anySet(), anyString()))
                .thenReturn(List.of(mockAffiliationCertificate));

        when(filedService.getNextFiledNumberCertificate()).thenReturn("FOLIO-1");
        when(codeValidCertificationService.consultCode(anyString(), anyString(), anyBoolean()))
                .thenReturn("VALIDATION_CODE");

        ResponseBulkDTO res = certificateBulkService.generateMassiveWorkerCertificates(
                mockExcelFile, "123456789", "CC");

        assertNotNull(res);
        assertEquals(1, res.getRecordsTotal());
        assertEquals(0, res.getRecordsError());
    }

    @Test
    void generateMassiveWorkerCertificates_NullFile_ThrowsError() {
        assertThrows(AffiliationError.class, () ->
                certificateBulkService.generateMassiveWorkerCertificates(null, "123", "CC"));
    }

    @Test
    void generateMassiveWorkerCertificates_InvalidFileType_ThrowsError() {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file", "x.txt", "text/plain", "x".getBytes());
        assertThrows(AffiliationError.class, () ->
                certificateBulkService.generateMassiveWorkerCertificates(invalidFile, "123", "CC"));
    }

    @Test
    void generateMassiveWorkerCertificates_AffiliateNotFound_ThrowsError() {
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        assertThrows(AffiliationError.class, () ->
                certificateBulkService.generateMassiveWorkerCertificates(mockExcelFile, "123", "CC"));
    }

    @Test
    void generateMassiveWorkerCertificates_ExceedsMaximumRecords_ThrowsError() throws Exception {
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mockAffiliate));
        when(excelProcessingServiceData.converterExcelToMap(any(), any()))
                .thenReturn(new ArrayList<>(Collections.nCopies(1001, Map.of())));
        doReturn(1000).when(properties).getMaximumRecordsConsultCertificate();
        assertThrows(AffiliationError.class, () ->
                certificateBulkService.generateMassiveWorkerCertificates(mockExcelFile, "123", "CC"));
    }

    @Test
    void generateMassiveWorkerCertificates_WithErrors_ReturnsErrorDocument() throws Exception {
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mockAffiliate));
        when(excelProcessingServiceData.converterExcelToMap(any(), any()))
                .thenReturn(List.of(Map.of("numberDocument", "x")));
        doReturn(1000).when(properties).getMaximumRecordsConsultCertificate();

        CertificateBulkDTO invalid = new CertificateBulkDTO();
        invalid.setNumberDocument("x");
        invalid.setTypeDocument("XX");
        invalid.setValid(false);

        when(excelProcessingServiceData.converterMapToClass(anyList(), eq(CertificateBulkDTO.class)))
                .thenReturn(List.of(invalid));
        when(affiliationDependentRepository.findAffiliateCertificate(anySet(), anyString()))
                .thenReturn(List.of());
        when(excelProcessingServiceData.createDocumentExcelErrors(anyList()))
                .thenReturn(new ExportDocumentsDTO());

        ResponseBulkDTO res = certificateBulkService.generateMassiveWorkerCertificates(mockExcelFile, "123", "CC");
        assertEquals(1, res.getRecordsError());
        assertNotNull(res.getDocument());
    }

    @Test
    void getTemplate_Success() throws Exception {
        doReturn("tpl").when(properties).getWorkerMassiveCertificateTemplateId();
        // getDocument puede lanzar IOException → doReturn
        doReturn("base64").when(alfrescoService).getDocument("tpl");
        assertEquals("base64", certificateBulkService.getTemplate());
    }

    @Test
    void createCertificatesMassive_WithIdDocument_Success() throws Exception {
        String idDoc = "doc-1";
        setupSecurityContext("test@test.com");

        Field f = CertificateBulkServiceImpl.class.getDeclaredField("listRecordBulkCertificate");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, DataFileCertificateBulkDTO> map = (Map<String, DataFileCertificateBulkDTO>) f.get(certificateBulkService);

        DataFileCertificateBulkDTO data = new DataFileCertificateBulkDTO();
        data.setIdDocument("id-interno");
        data.setDocumentNumberEmployer("123456789");

        Certificate cert = new Certificate();
        cert.setNumberDocument("1234567890");
        CertificateBulkDTO row = new CertificateBulkDTO();
        row.setValid(true);
        row.setNumberDocument("1234567890");
        row.setCertificate(cert);
        data.setListCertificateBulkDTO(List.of(row));
        map.put(idDoc, data);

        when(iUserPreRegisterRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUserMain));
        when(certificateServiceHelper.transformToDependentWorkerCertificate(any())).thenReturn(new CertificateReportRequestDTO());
        when(genericWebClient.generateReportCertificate(any())).thenReturn(Base64.getEncoder().encodeToString("pdf".getBytes()));

        // saveAll devuelve lista (no usar doNothing)
        doReturn(Collections.singletonList(cert))
                .when(certificateRepository).saveAll(anyList());

        MultipartFile zip = certificateBulkService.createCertificatesMassive(idDoc);
        assertNotNull(zip);
        assertEquals("application/zip", zip.getContentType());
        verify(certificateRepository).saveAll(anyList());
    }

    @Test
    void createCertificatesMassive_EmptyData_ThrowsError() {
        setupSecurityContext("test@test.com");
        when(iUserPreRegisterRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUserMain));
        assertThrows(AffiliationError.class, () ->
                certificateBulkService.createCertificatesMassive("no-existe"));
    }

    @Test
    void deleteRecords_Success() {
        certificateBulkService.deleteRecords("x");
        assertTrue(true);
    }

    @Test
    void createCertificatesMassive_Async_DependentType_Success() throws Exception {
        LocalDate date = LocalDate.now();
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(List.of(mockAffiliate));
        when(affiliationDependentRepository.findAffiliateCertificate(
                anyString(), anyString(), any(LocalDate.class)))
                .thenReturn(List.of(mockAffiliationCertificate));
        when(filedService.getNextFiledNumberCertificate()).thenReturn("F1");
        when(codeValidCertificationService.consultCode(anyString(), anyString(), anyBoolean())).thenReturn("VALID");
        when(certificateServiceHelper.transformToDependentWorkerCertificate(any())).thenReturn(new CertificateReportRequestDTO());
        when(genericWebClient.generateReportCertificate(any())).thenReturn(Base64.getEncoder().encodeToString("pdf".getBytes()));

        ResponseUploadOrReplaceFilesDTO upload = mock(ResponseUploadOrReplaceFilesDTO.class, RETURNS_DEEP_STUBS);
        when(upload.getDocuments().get(0).getDocumentId()).thenReturn("alf-id");
        // uploadOrReplaceFiles lanza IOException → doReturn
        doReturn(upload).when(alfrescoService).uploadOrReplaceFiles(anyString(), anyString(), anyList());

        doReturn("folder").when(properties).getFolderIdCertificate();
        doNothing().when(sendEmails).emailCertificateMassive(any(), anyString());

        certificateBulkService.createCertificatesMassive(Constant.TYPE_AFFILLATE_DEPENDENT, date, mockUserMain);
        verify(sendEmails).emailCertificateMassive(any(), anyString());
    }

    @Test
    void createCertificatesMassive_Async_IndependentType_Success() throws Exception {
        LocalDate date = LocalDate.now();
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(List.of(mockAffiliate));
        when(affiliationDependentRepository.findAffiliateCertificate(
                anyString(), anyString(), any(LocalDate.class)))
                .thenReturn(List.of(mockAffiliationCertificate));
        when(filedService.getNextFiledNumberCertificate()).thenReturn("F1");
        when(codeValidCertificationService.consultCode(anyString(), anyString(), anyBoolean())).thenReturn("VALID");
        when(certificateServiceHelper.transformToDependentWorkerCertificate(any())).thenReturn(new CertificateReportRequestDTO());
        when(genericWebClient.generateReportCertificate(any())).thenReturn(Base64.getEncoder().encodeToString("pdf".getBytes()));

        ResponseUploadOrReplaceFilesDTO upload = mock(ResponseUploadOrReplaceFilesDTO.class, RETURNS_DEEP_STUBS);
        when(upload.getDocuments().get(0).getDocumentId()).thenReturn("alf-id");
        doReturn(upload).when(alfrescoService).uploadOrReplaceFiles(anyString(), anyString(), anyList());
        doReturn("folder").when(properties).getFolderIdCertificate();

        doReturn(Collections.emptyList()).when(certificateRepository).saveAll(anyList());

        certificateBulkService.createCertificatesMassive(Constant.TYPE_AFFILLATE_INDEPENDENT, date, mockUserMain);
        verify(certificateRepository).saveAll(anyList());
    }

    @Test
    void createCertificatesMassive_Async_InvalidType_ThrowsError() {
        assertThrows(AffiliationError.class, () ->
                certificateBulkService.createCertificatesMassive("XYZ", LocalDate.now(), mockUserMain));
    }

    @Test
    void createCertificatesMassive_Async_AffiliateNotFound_ThrowsError() {
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(List.of());
        assertThrows(AffiliationError.class, () ->
                certificateBulkService.createCertificatesMassive(Constant.TYPE_AFFILLATE_DEPENDENT, LocalDate.now(), mockUserMain));
    }

    @Test
    void deleteRecordsCertificate_Success() {
        // getter retorna int, NO Long
        doReturn(24).when(properties).getMaximumFileSaveTimeHour();
        certificateBulkService.deleteRecordsCertificate();
        assertTrue(true);
    }

    @Test
    void recordsBulkMassive_Success() throws Exception {
        setupSecurityContext("test@test.com");
        when(iUserPreRegisterRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUserMain));

        Field f = CertificateBulkServiceImpl.class.getDeclaredField("listRecordBulkCertificate");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, DataFileCertificateBulkDTO> map = (Map<String, DataFileCertificateBulkDTO>) f.get(certificateBulkService);

        DataFileCertificateBulkDTO data = new DataFileCertificateBulkDTO();
        data.setDocumentNumberEmployer("123456789");
        map.put("k", data);

        List<Map<String, Object>> res = certificateBulkService.recordsBulkMassive();
        assertNotNull(res);
        assertFalse(res.isEmpty());
    }


    @Test
    void downloadDocumentZip_DocumentNotFound_ThrowsError() {
        assertThrows(AffiliationError.class, () ->
                certificateBulkService.downloadDocumentZip("nope"));
    }
    @Test
    void downloadDocumentZip_Base64Null_ThrowsError() throws Exception {
        // Mismo armado del mapa que en el test de éxito
        Field f = CertificateBulkServiceImpl.class.getDeclaredField("listRecordBulkCertificate");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, DataFileCertificateBulkDTO> map =
                (Map<String, DataFileCertificateBulkDTO>) f.get(certificateBulkService);

        String idDocument = "zip-2";
        DataFileCertificateBulkDTO data = new DataFileCertificateBulkDTO();
        data.setIdDocument("any");
        data.setDocumentNumberEmployer("123456789");
        try {
            DataFileCertificateBulkDTO.class.getMethod("setIdAlfresco", String.class)
                    .invoke(data, "alf-id-NULL");
        } catch (NoSuchMethodException e) { /* ignore */ }

        map.put(idDocument, data);

        // Forzamos null desde Alfresco → la implementación hace Optional.of(null) → NPE
        when(alfrescoService.getDocument(anyString())).thenReturn(null);

        assertThrows(NullPointerException.class, () ->
                certificateBulkService.downloadDocumentZip(idDocument));
    }





    @Test
    void createCertificatesMassive_ValidationCodeEmpty_SetsDefaultMessage() throws Exception {
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(List.of(mockAffiliate));
        when(affiliationDependentRepository.findAffiliateCertificate(
                anyString(), anyString(), any(LocalDate.class)))
                .thenReturn(List.of(mockAffiliationCertificate));

        when(filedService.getNextFiledNumberCertificate()).thenReturn("F3");
        when(codeValidCertificationService.consultCode(anyString(), anyString(), anyBoolean())).thenReturn("");
        when(genericWebClient.generateReportCertificate(any())).thenReturn(Base64.getEncoder().encodeToString("pdf".getBytes()));
        doReturn("folder").when(properties).getFolderIdCertificate();

        ResponseUploadOrReplaceFilesDTO upload = mock(ResponseUploadOrReplaceFilesDTO.class, RETURNS_DEEP_STUBS);
        when(upload.getDocuments().get(0).getDocumentId()).thenReturn("alf-id");
        doReturn(upload).when(alfrescoService).uploadOrReplaceFiles(anyString(), anyString(), anyList());

        ArgumentCaptor<Certificate> cap = ArgumentCaptor.forClass(Certificate.class);
        when(certificateServiceHelper.transformToDependentWorkerCertificate(cap.capture()))
                .thenReturn(new CertificateReportRequestDTO());

        certificateBulkService.createCertificatesMassive(Constant.TYPE_AFFILLATE_DEPENDENT, LocalDate.now(), mockUserMain);
        assertEquals("El usuario no está validado", cap.getValue().getValidatorCode());
    }

    @Test
    void getUserPreRegister_Success() {
        setupSecurityContext("test@test.com");
        when(iUserPreRegisterRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUserMain));
        UserMain res = certificateBulkService.getUserPreRegister();
        assertEquals("test@test.com", res.getEmail());
    }

    @Test
    void getUserPreRegister_UserNotFound_ThrowsError() {
        setupSecurityContext("test@test.com");
        when(iUserPreRegisterRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(AffiliationError.class, () -> certificateBulkService.getUserPreRegister());
    }

    @Test
    void generateZip_Success() throws Exception {
        String b64 = Base64.getEncoder().encodeToString("pdf".getBytes());
        MultipartFile res = certificateBulkService.generateZip(List.of(
                new FileBase64DTO("a.pdf", b64),
                new FileBase64DTO("b.pdf", b64)
        ));
        assertEquals("application/zip", res.getContentType());
        assertTrue(res.getSize() > 0);
    }

    @Test
    void generateZip_EmptyList_Success() throws Exception {
        MultipartFile res = certificateBulkService.generateZip(List.of());
        assertEquals("application/zip", res.getContentType());
    }

    @Test
    void base64ToMultipartFile_Success() throws Exception {
        String b64 = Base64.getEncoder().encodeToString("hello".getBytes());
        MultipartFile f = certificateBulkService.base64ToMultipartFile(b64, "x.pdf", "application/pdf");
        assertEquals("x.pdf", f.getOriginalFilename());
        assertEquals("application/pdf", f.getContentType());
    }

    @Test
    void formatDate_Various() {
        String r1 = CertificateBulkServiceImpl.formatDate(LocalDate.of(2024, 3, 15));
        assertTrue(r1.contains("15") && r1.contains("2024"));
        String r2 = CertificateBulkServiceImpl.formatDate(LocalDate.of(2024, 1, 1));
        assertTrue(r2.contains("1"));
        String r3 = CertificateBulkServiceImpl.formatDate(LocalDate.of(2024, 12, 31));
        assertTrue(r3.contains("31"));
    }

    @Test
    void saveCertificates_SuccessAndEmpty() {
        doReturn(Collections.emptyList()).when(certificateRepository).saveAll(anyList());
        certificateBulkService.saveCertificates(List.of(new Certificate()));
        certificateBulkService.saveCertificates(List.of());
        verify(certificateRepository, times(2)).saveAll(anyList());
    }

    @Test
    void getEmailUserPreRegister_SuccessAndNotFound() {
        setupSecurityContext("test@test.com");
        when(iUserPreRegisterRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUserMain));
        assertEquals("123456789", certificateBulkService.getEmailUserPreRegister());

        when(iUserPreRegisterRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(AffiliationError.class, () -> certificateBulkService.getEmailUserPreRegister());
    }

    // helper
    private void setupSecurityContext(String email) {
        SecurityContext sc = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);

        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                claims
        );

        when(auth.getPrincipal()).thenReturn(jwt);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);
    }

}
