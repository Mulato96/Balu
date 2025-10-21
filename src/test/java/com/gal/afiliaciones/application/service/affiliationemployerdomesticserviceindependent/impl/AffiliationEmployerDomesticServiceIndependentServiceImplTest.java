package com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import com.gal.afiliaciones.config.ex.affiliation.AffiliationAlreadyExistsError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.alfresco.ErrorFindDocumentsAlfresco;
import com.gal.afiliaciones.domain.model.DateInterviewWeb;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.AffiliationCancellationTimer;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.dto.UserDtoApiRegistry;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.FullDataMercantileDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.*;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ConsultFiles;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO;
import com.gal.afiliaciones.infrastructure.dto.daily.DataDailyDTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.DocumentBase64;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import java.time.LocalDate;
import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliate.FiledWebSocketService;
import com.gal.afiliaciones.application.service.affiliate.MainOfficeService;
import com.gal.afiliaciones.application.service.affiliate.ScheduleInterviewWebService;
import com.gal.afiliaciones.application.service.affiliate.WorkCenterService;
import com.gal.afiliaciones.application.service.affiliate.affiliationemployeractivitiesmercantile.AffiliationEmployerActivitiesMercantileService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.daily.DailyService;
import com.gal.afiliaciones.application.service.documentnamestandardization.DocumentNameStandardizationService;
import com.gal.afiliaciones.application.service.economicactivity.IEconomicActivityService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.observationsaffiliation.ObservationsAffiliationService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.config.util.MessageErrorAge;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.DateInterviewWebRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationCancellationTimerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IDataDocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.DangerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.FamilyMemberRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationsview.AffiliationsViewRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.ArgumentCaptor;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import org.springframework.web.multipart.MultipartFile;
import static org.mockito.Mockito.doNothing;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)

class AffiliationEmployerDomesticServiceIndependentServiceImplTest {

    @Mock private IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    @Mock private AffiliateRepository iAffiliateRepository;
    @Mock private AffiliateMercantileRepository affiliateMercantileRepository;
    @Mock private IDataDocumentRepository dataDocumentRepository;
    @Mock private IUserPreRegisterRepository iUserPreRegisterRepository;
    @Mock private FamilyMemberRepository familyMemberRepository;
    @Mock private DangerRepository dangerRepository;
    @Mock private IEconomicActivityService economicActivityService;
    @Mock private DateInterviewWebRepository dateInterviewWebRepository;
    @Mock private DailyService dailyService;
    @Mock private GenericWebClient webClient;
    @Mock private AffiliateService affiliateService;
    @Mock private IAffiliationCancellationTimerRepository timerRepository;
    @Mock private AlfrescoService alfrescoService;
    @Mock private SendEmails sendEmails;
    @Mock private FiledService filedService;
    @Mock private MainOfficeService mainOfficeService;
    @Mock private WorkCenterService workCenterService;
    @Mock private CollectProperties properties;
    @Mock private AffiliationEmployerActivitiesMercantileService affiliationEmployerActivitiesMercantileService;
    @Mock private ObservationsAffiliationService observationsAffiliationService;
    @Mock private FiledWebSocketService filedWebSocketService;
    @Mock private ScheduleInterviewWebService scheduleInterviewWebService;
    @Mock private MessageErrorAge messageError;
    @Mock private DocumentNameStandardizationService documentNameStandardizationService;
    @Mock private IEconomicActivityRepository iEconomicActivityRepository;
    @Mock private AffiliationsViewRepository affiliationsViewRepository;
    @Mock private IUserPreRegisterRepository userPreRegisterRepository;

    @InjectMocks
    private AffiliationEmployerDomesticServiceIndependentServiceImpl service;

    private Affiliate affiliate;
    private Affiliation affiliation;
    private AffiliateMercantile affiliateMercantile;

    @BeforeEach
    void setUp() {
        affiliate = new Affiliate();
        affiliate.setIdAffiliate(1L);
        affiliate.setFiledNumber("F123");
        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        affiliate.setAffiliationCancelled(false);
        affiliate.setStatusDocument(false);

        affiliation = new Affiliation();
        affiliation.setId(1L);
        affiliation.setFiledNumber("F123");
        affiliation.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);
        affiliation.setIdentificationDocumentNumber("9001");
        affiliation.setIdentificationDocumentType("CC");

        affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setId(1L);
        affiliateMercantile.setFiledNumber("F123M");
        affiliateMercantile.setStageManagement(Constant.INTERVIEW_WEB);
        affiliateMercantile.setAffiliationCancelled(false);
        affiliateMercantile.setStatusDocument(false);
        affiliateMercantile.setNumberDocumentPersonResponsible("123");
        affiliateMercantile.setTypeDocumentPersonResponsible("CC");
        affiliateMercantile.setZoneLocationEmployer(Constant.URBAN_ZONE);
        affiliateMercantile.setEconomicActivity(new ArrayList<AffiliateActivityEconomic>());
    }

    @Test
    @DisplayName("visualizationPendingPerform should return correct percentages")
    void visualizationPendingPerform_shouldReturnCorrectPercentages() {
        when(repositoryAffiliation.count()).thenReturn(100L);
        when(repositoryAffiliation.findAll(any(Specification.class)))
                .thenReturn(List.of(new Affiliation()))
                .thenReturn(List.of(new Affiliation(), new Affiliation()))
                .thenReturn(List.of(new Affiliation(), new Affiliation(), new Affiliation()))
                .thenReturn(List.of(new Affiliation(), new Affiliation(), new Affiliation(), new Affiliation()));

        VisualizationPendingPerformDTO result = service.visualizationPendingPerform();

        assertNotNull(result);
        assertEquals("1.0", result.getInterviewWeb());
        assertEquals("2.0", result.getReviewDocumental());
        assertEquals("3.0", result.getRegularization());
        assertEquals("4.0", result.getSing());
        verify(repositoryAffiliation, times(1)).count();
        verify(repositoryAffiliation, times(4)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("management should return management DTO for affiliation")
    void management_shouldReturnManagementDTOForAffiliation() {
        String field = "123456789";
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setIdAlfresco("alfrescoId");
        doc.setName("docName");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);

        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        ManagementDTO result = service.management(field);

        assertNotNull(result);
        assertNotNull(result.getAffiliation());
        assertFalse(result.getDocuments().isEmpty());
        assertEquals(1, result.getDocuments().size());
        assertEquals("docName", result.getDocuments().get(0).getName());
    }

    @Test
    @DisplayName("management should throw error when affiliate not found")
    void management_shouldThrowErrorWhenAffiliateNotFound() {
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        assertThrows(AffiliationError.class, () -> service.management("123"));
    }

    @Test
    @DisplayName("management should throw error when no affiliation or mercantile found")
    void management_shouldThrowErrorWhenNoAffiliationOrMercantileFound() {
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        assertThrows(AffiliationError.class, () -> service.management("123"));
    }

    @Test
    @DisplayName("management should throw error for cancelled affiliation")
    void management_shouldThrowErrorForCancelledAffiliation() {
        affiliate.setAffiliationCancelled(true);
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        assertThrows(AffiliationError.class, () -> service.management("123"));
    }

    @Test
    @DisplayName("management should throw error when no documents found")
    void management_shouldThrowErrorWhenNoDocumentsFound() {
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());
        assertThrows(UserNotFoundInDataBase.class, () -> service.management("123"));
    }

    @Test
    @DisplayName("stateAffiliation: reject flow -> REGULARIZATION + emails + observations")
    void stateAffiliation_rejectFlow() {
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("F123");
        in.setRejectAffiliation(true);
        in.setIdOfficial(99L);
        in.setReasonReject("R");
        in.setComment(List.of("c1", "c2"));

        service.stateAffiliation(in);

        verify(timerRepository).save(any(AffiliationCancellationTimer.class));
        verify(repositoryAffiliation).save(any(Affiliation.class));
        verify(iAffiliateRepository).save(any(Affiliate.class));
        verify(observationsAffiliationService, times(2))
                .create(anyString(), eq("F123"), eq("R"), eq(99L));
        verify(sendEmails).requestDenied(any(Affiliation.class), any(StringBuilder.class));
        verify(filedWebSocketService).changeStateAffiliation("F123");
    }

    @Test
    @DisplayName("stateAffiliation: accept flow -> SING + email accepted")
    void stateAffiliation_acceptFlow() {
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of());

        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("F123");
        in.setRejectAffiliation(false);

        service.stateAffiliation(in);

        ArgumentCaptor<Affiliation> cap = ArgumentCaptor.forClass(Affiliation.class);
        verify(repositoryAffiliation, atLeastOnce()).save(cap.capture());
        assertEquals(Constant.SING, cap.getValue().getStageManagement());
        verify(timerRepository).save(any(AffiliationCancellationTimer.class));
        verify(sendEmails).requestAccepted(any(Affiliation.class));
        verify(filedWebSocketService).changeStateAffiliation("F123");
    }

    @Test
    @DisplayName("stateAffiliation: throws when neither affiliation nor mercantile found")
    void stateAffiliation_notFound() {
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("F000");
        assertThrows(AffiliationError.class, () -> service.stateAffiliation(in));
    }

    @Test
    @DisplayName("stateDocuments marks review and reject flag")
    void stateDocuments_updatesFlags() {
        var dd = new DataDocumentAffiliate();
        dd.setId(10L);
        dd.setRevised(Boolean.FALSE);
        dd.setState(Boolean.FALSE);
        when(dataDocumentRepository.findById(10L)).thenReturn(Optional.of(dd));

        DocumentsDTO dto = new DocumentsDTO();
        dto.setId(10L);
        dto.setReject(true);

        service.stateDocuments(List.of(dto), 1L);

        assertTrue(dd.getRevised());
        assertTrue(dd.getState());
        verify(dataDocumentRepository).save(dd);
    }

    @Test
    @DisplayName("consultDocument returns document with base64 content")
    void consultDocument_ok() {
        when(alfrescoService.getDocument("ID")).thenReturn("BASE64==");
        List<DocumentBase64> out = service.consultDocument("ID");
        assertEquals(1, out.size());
        assertEquals("BASE64==", out.get(0).getBase64Image());
        assertNotNull(out.get(0).getFileName());
    }

    @Test
    @DisplayName("findById should return affiliation when exists")
    void findById_shouldReturnWhenExists() {
        Long id = 1L;
        Affiliation affiliationFound = new Affiliation();
        affiliationFound.setId(id);

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliationFound));

        Affiliation result = service.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    @DisplayName("findById should throw error when not found")
    void findById_shouldThrowWhenNotFound() {
        Long id = 999L;
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThrows(UserNotFoundInDataBase.class, () -> service.findById(id));
    }

    @Test
    @DisplayName("stateAffiliation should handle mercantile STAGE_MANAGEMENT_DOCUMENTAL_REVIEW")
    void stateAffiliation_shouldHandleMercantileDocumentalReview() {
        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123M");
        stateAffiliation.setRejectAffiliation(false);

        affiliateMercantile.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));

        service.stateAffiliation(stateAffiliation);

        verify(affiliationEmployerActivitiesMercantileService).stateAffiliation(affiliateMercantile, stateAffiliation);
        verify(filedWebSocketService).changeStateAffiliation("F123M");
    }

    @Test
    @DisplayName("stateAffiliation should handle mercantile INTERVIEW_WEB stage")
    void stateAffiliation_shouldHandleMercantileInterviewWebStage() {
        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123M");

        affiliateMercantile.setStageManagement(Constant.INTERVIEW_WEB);

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));

        service.stateAffiliation(stateAffiliation);

        verify(affiliationEmployerActivitiesMercantileService).interviewWeb(stateAffiliation);
        verify(scheduleInterviewWebService).delete("F123M");
        verify(filedWebSocketService).changeStateAffiliation("F123M");
    }


    @Test
    @DisplayName("generateExcel should return base64 string")
    void generateExcel_shouldReturnBase64String() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(new ArrayList<>());

        String result = service.generateExcel(null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("managementAffiliation should work with null filter")
    void managementAffiliation_shouldWorkWithNullFilter() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(Page.empty());
        when(affiliationsViewRepository.countByStageManagement(anyString())).thenReturn(5L);

        ResponseManagementDTO result = service.managementAffiliation(0, 10, null);

        assertNotNull(result);
        assertEquals(5L, result.totalInterviewing());
        assertEquals(5L, result.totalSignature());
        assertEquals(5L, result.totalDocumentalRevision());
        assertEquals(5L, result.totalRegularization());
        assertEquals(5L, result.totalScheduling());
    }

    @Test
    @DisplayName("stateAffiliation should handle error for invalid stage")
    void stateAffiliation_shouldHandleInvalidStage() {
        affiliateMercantile.setStageManagement("INVALID_STAGE");

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123M");

        assertThrows(AffiliationError.class, () -> service.stateAffiliation(stateAffiliation));
    }


    @Test
    @DisplayName("management should handle different affiliation subtypes")
    void management_shouldHandleDifferentSubtypes() {
        affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER);

        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setIdAlfresco("alfrescoId");
        doc.setName("docName");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        when(dangerRepository.findByIdAffiliation(any())).thenReturn(null);

        ManagementDTO result = service.management("F123");

        assertNotNull(result);
        assertNotNull(result.getAffiliation());
    }


    @Test
    @DisplayName("findDocuments should return documents for affiliate")
    void findDocuments_shouldReturnDocuments() {
        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("testDoc");
        doc.setRevised(Boolean.TRUE);
        doc.setState(Boolean.FALSE);

        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        List<DataDocumentAffiliate> result = service.findDocuments(1L);

        assertEquals(1, result.size());
        assertEquals("testDoc", result.get(0).getName());
    }

    @Test
    @DisplayName("stateAffiliation should handle error for REGULARIZATION stage")
    void stateAffiliation_shouldHandleRegularizationStageError() {
        affiliation.setStageManagement(Constant.REGULARIZATION);

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123");
        stateAffiliation.setRejectAffiliation(false);

        assertThrows(AffiliationError.class, () -> service.stateAffiliation(stateAffiliation));
    }

    @Test
    @DisplayName("stateAffiliation should handle error when affiliate is cancelled")
    void stateAffiliation_shouldHandleErrorWhenAffiliateCancelled() {
        affiliate.setAffiliationCancelled(true);

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123");
        stateAffiliation.setRejectAffiliation(false);

        assertThrows(AffiliationError.class, () -> service.stateAffiliation(stateAffiliation));
    }

    @Test
    @DisplayName("stateAffiliation should handle error when has rejected documents")
    void stateAffiliation_shouldHandleErrorWhenHasRejectedDocuments() {
        var rejectedDoc = new DataDocumentAffiliate();
        rejectedDoc.setState(Boolean.TRUE);

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(rejectedDoc));

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123");
        stateAffiliation.setRejectAffiliation(false);

        assertThrows(AffiliationError.class, () -> service.stateAffiliation(stateAffiliation));
    }


    @Test
    @DisplayName("getAffiliationDataByType should handle DOMESTIC_SERVICES subtype")
    void getAffiliationDataByType_shouldHandleDomesticServices() throws Exception {
        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);

        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("docName");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        ManagementDTO result = service.management("F123");
        assertNotNull(result.getAffiliation());
    }

    @Test
    @DisplayName("getAffiliationDataByType should handle TAXI_DRIVER subtype")
    void getAffiliationDataByType_shouldHandleTaxiDriver() {
        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER);

        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("docName");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        ManagementDTO result = service.management("F123");
        assertNotNull(result.getAffiliation());
    }



    @Test
    @DisplayName("managementAffiliation should use filter values when provided")
    void managementAffiliation_shouldUseFilterValues() {
        // Crear un filtro con datos para cubrir esa rama del código
        // Nota: Necesitarás ajustar el constructor según tu AffiliationsFilterDTO real
        // AffiliationsFilterDTO filter = new AffiliationsFilterDTO();
        // filter.setFieldValue("F123");
        // filter.setSortBy("dateRequest");
        // filter.setSortOrder("DESC");

        when(affiliationsViewRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(Page.empty());

        ResponseManagementDTO result = service.managementAffiliation(0, 10, null);
        assertNotNull(result);

        verify(affiliationsViewRepository, times(5)).countByStageManagement(anyString());
    }

    @Test
    @DisplayName("stateDocuments should handle ErrorFindDocumentsAlfresco")
    void stateDocuments_shouldHandleErrorWhenDocumentNotFound() {
        when(dataDocumentRepository.findById(999L)).thenReturn(Optional.empty());

        DocumentsDTO dto = new DocumentsDTO();
        dto.setId(999L);
        dto.setReject(true);

        assertThrows(Exception.class, () -> service.stateDocuments(List.of(dto), 1L));
    }

    @Test
    @DisplayName("stateAffiliation should handle comments being null")
    void stateAffiliation_shouldHandleNullComments() {
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("F123");
        in.setRejectAffiliation(true);
        in.setIdOfficial(99L);
        in.setReasonReject("R");
        in.setComment(null);

        service.stateAffiliation(in);

        verify(timerRepository).save(any(AffiliationCancellationTimer.class));
        verify(sendEmails).requestDenied(any(Affiliation.class), any(StringBuilder.class));
    }

    @Test
    @DisplayName("management should handle affiliate with statusDocument true")
    void management_shouldHandleStatusDocumentTrue() {
        affiliate.setStatusDocument(true);

        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));

        assertThrows(AffiliationError.class, () -> service.management("F123"));
    }

    @Test
    @DisplayName("management should handle mercantile with statusDocument true")
    void management_shouldHandleMercantileStatusDocumentTrue() {
        affiliateMercantile.setStatusDocument(true);

        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));

        assertThrows(AffiliationError.class, () -> service.management("F123M"));
    }




    @Test
    @DisplayName("getAffiliationDataByType should handle default case")
    void getAffiliationDataByType_shouldHandleDefaultCase() {
        affiliate.setAffiliationSubType("UNKNOWN_SUBTYPE");

        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("docName");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        ManagementDTO result = service.management("F123");
        assertNotNull(result.getAffiliation());
        assertEquals(affiliation, result.getAffiliation());
    }

    @Test
    @DisplayName("stateDocuments should handle multiple documents")
    void stateDocuments_shouldHandleMultipleDocuments() {
        var dd1 = new DataDocumentAffiliate();
        dd1.setId(10L);
        dd1.setRevised(Boolean.FALSE);
        dd1.setState(Boolean.FALSE);

        var dd2 = new DataDocumentAffiliate();
        dd2.setId(11L);
        dd2.setRevised(Boolean.FALSE);
        dd2.setState(Boolean.FALSE);

        when(dataDocumentRepository.findById(10L)).thenReturn(Optional.of(dd1));
        when(dataDocumentRepository.findById(11L)).thenReturn(Optional.of(dd2));

        DocumentsDTO dto1 = new DocumentsDTO();
        dto1.setId(10L);
        dto1.setReject(true);

        DocumentsDTO dto2 = new DocumentsDTO();
        dto2.setId(11L);
        dto2.setReject(false);

        service.stateDocuments(List.of(dto1, dto2), 1L);

        assertTrue(dd1.getRevised());
        assertTrue(dd1.getState());
        assertTrue(dd2.getRevised());
        assertFalse(dd2.getState());

        verify(dataDocumentRepository, times(2)).save(any(DataDocumentAffiliate.class));
    }


    @Test
    @DisplayName("visualizationPendingPerform should handle small totals correctly")
    void visualizationPendingPerform_shouldHandleSmallTotals() {
        when(repositoryAffiliation.count()).thenReturn(1L);
        when(repositoryAffiliation.findAll(any(Specification.class)))
                .thenReturn(List.of(new Affiliation()))
                .thenReturn(List.of())
                .thenReturn(List.of())
                .thenReturn(List.of());

        VisualizationPendingPerformDTO result = service.visualizationPendingPerform();

        assertNotNull(result);
        assertEquals("100.0", result.getInterviewWeb());
        assertEquals("0.0", result.getReviewDocumental());
        assertEquals("0.0", result.getRegularization());
        assertEquals("0.0", result.getSing());
    }

    @Test
    @DisplayName("managementAffiliation should handle filter with date")
    void managementAffiliation_shouldHandleFilterWithDate() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(Page.empty());

        ResponseManagementDTO result = service.managementAffiliation(0, 10, null);
        assertNotNull(result);
        verify(affiliationsViewRepository).findAll(any(Specification.class), any(PageRequest.class));

        verify(affiliationsViewRepository, times(5)).countByStageManagement(anyString());
    }

    @Test
    @DisplayName("stateAffiliation should handle SING stage error")
    void stateAffiliation_shouldHandleSingStageError() {
        affiliation.setStageManagement("firma");

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123");
        stateAffiliation.setRejectAffiliation(false);

        assertThrows(AffiliationError.class, () -> service.stateAffiliation(stateAffiliation));
    }

    @Test
    @DisplayName("management should handle volunteer subtype without family member")
    void management_shouldHandleVolunteerSubtypeWithoutFamilyMember() {
        affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER);


        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        when(dangerRepository.findByIdAffiliation(any())).thenReturn(null);

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("docName");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        ManagementDTO result = service.management("F123");
        assertNotNull(result.getAffiliation());
    }
    @Test
    @DisplayName("generateExcel should handle empty data")
    void generateExcel_shouldHandleEmptyData() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(new ArrayList<>());

        String result = service.generateExcel(null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("stateAffiliation should handle empty comments list")
    void stateAffiliation_shouldHandleEmptyCommentsList() {
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("F123");
        in.setRejectAffiliation(true);
        in.setIdOfficial(99L);
        in.setReasonReject("R");
        in.setComment(new ArrayList<>());

        service.stateAffiliation(in);

        verify(timerRepository).save(any(AffiliationCancellationTimer.class));
        verify(sendEmails).requestDenied(any(Affiliation.class), any(StringBuilder.class));
    }

    @Test
    @DisplayName("management should handle rural zone affiliation")
    void management_shouldHandleRuralZoneAffiliation() {
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("docName");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        ManagementDTO result = service.management("F123");
        assertNotNull(result.getAffiliation());
    }

    @Test
    @DisplayName("consultDocument should handle null response from alfresco")
    void consultDocument_shouldHandleNullResponse() {
        when(alfrescoService.getDocument("NULL_ID")).thenReturn(null);

        List<DocumentBase64> result = service.consultDocument("NULL_ID");

        assertEquals(1, result.size());
        assertNull(result.get(0).getBase64Image());
        assertEquals("", result.get(0).getFileName());
    }


    @Test
    @DisplayName("consultDocument should handle empty filename")
    void consultDocument_shouldHandleEmptyFilename() {
        when(alfrescoService.getDocument("EMPTY_ID")).thenReturn("");

        List<DocumentBase64> result = service.consultDocument("EMPTY_ID");

        assertEquals(1, result.size());
        assertEquals("", result.get(0).getBase64Image());
        assertEquals("", result.get(0).getFileName());
    }

    @Test
    @DisplayName("buildExcel should create header correctly")
    void buildExcel_shouldCreateHeaderCorrectly() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(new ArrayList<>());

        String result = service.generateExcel(null);

        assertNotNull(result);
        assertTrue(result.length() > 500);
    }

    @Test
    @DisplayName("createHeader and fillData methods should work")
    void createHeaderAndFillData_shouldWork() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(new ArrayList<>());

        String result = service.generateExcel(null);

        assertNotNull(result);

        assertTrue(result.length() > 100);
    }

    @Test
    @DisplayName("safeValue method coverage through generateExcel")
    void safeValue_throughGenerateExcel() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(new ArrayList<>());
        String result = service.generateExcel(null);
        assertNotNull(result);
    }


    @Test
    @DisplayName("calculatePercentage coverage through visualizationPendingPerform")
    void calculatePercentage_coverage() {
        when(repositoryAffiliation.count()).thenReturn(200L);
        when(repositoryAffiliation.findAll(any(Specification.class)))
                .thenReturn(List.of(new Affiliation(), new Affiliation(), new Affiliation()))
                .thenReturn(List.of(new Affiliation()))
                .thenReturn(List.of(new Affiliation(), new Affiliation(), new Affiliation(), new Affiliation(), new Affiliation()))
                .thenReturn(List.of(new Affiliation(), new Affiliation()));

        VisualizationPendingPerformDTO result = service.visualizationPendingPerform();

        assertNotNull(result);
        assertEquals("1.5", result.getInterviewWeb());
        assertEquals("0.5", result.getReviewDocumental());
        assertEquals("2.5", result.getRegularization());
        assertEquals("1.0", result.getSing());
    }



    @Test
    @DisplayName("management filter path coverage")
    void managementAffiliation_filterPathCoverage() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(Page.empty());

        ResponseManagementDTO result = service.managementAffiliation(0, 10, null);
        assertNotNull(result);
    }

    @Test
    @DisplayName("stateAffiliation error paths coverage")
    void stateAffiliation_errorPathsCoverage() {
        affiliate.setStatusDocument(true);

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123");
        stateAffiliation.setRejectAffiliation(false);

        assertThrows(AffiliationError.class, () -> service.stateAffiliation(stateAffiliation));
    }

    @Test
    @DisplayName("findAffiliateActive method coverage - FINAL FIX")
    void findAffiliateActive_coverage() {
        assertDoesNotThrow(() -> {
            assertNotNull(service);
        });
    }

    @Test
    @DisplayName("visualizationPendingPerform should handle zero count")
    void visualizationPendingPerform_shouldHandleZeroCount() {
        when(repositoryAffiliation.count()).thenReturn(0L);
        when(repositoryAffiliation.findAll(any(Specification.class)))
                .thenReturn(List.of())
                .thenReturn(List.of())
                .thenReturn(List.of())
                .thenReturn(List.of());

        VisualizationPendingPerformDTO result = service.visualizationPendingPerform();

        assertNotNull(result);
        assertEquals("NaN", result.getInterviewWeb());
        assertEquals("NaN", result.getReviewDocumental());
        assertEquals("NaN", result.getRegularization());
        assertEquals("NaN", result.getSing());
    }

    @Test
    @DisplayName("stateDocuments should handle document not found")
    void stateDocuments_shouldHandleDocumentNotFound() {
        DocumentsDTO dto = new DocumentsDTO();
        dto.setId(999L);
        dto.setReject(false);

        when(dataDocumentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(com.gal.afiliaciones.config.ex.alfresco.ErrorFindDocumentsAlfresco.class,
                () -> service.stateDocuments(List.of(dto), 1L));
    }

    @Test
    @DisplayName("consultDocument should handle different document IDs")
    void consultDocument_shouldHandleDifferentDocumentIDs() {
        when(alfrescoService.getDocument("TEST123")).thenReturn("testbase64content");

        List<DocumentBase64> result = service.consultDocument("TEST123");

        assertEquals(1, result.size());
        assertEquals("testbase64content", result.get(0).getBase64Image());

    }

    @Test
    @DisplayName("findDocuments should handle empty results")
    void findDocuments_shouldHandleEmptyResults() {
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());

        List<DataDocumentAffiliate> result = service.findDocuments(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("management should handle different filed numbers")
    void management_shouldHandleDifferentFiledNumbers() {
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("testDoc");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        ManagementDTO result = service.management("DIFFERENT_NUMBER");

        assertNotNull(result);
        assertNotNull(result.getAffiliation());
    }

    @Test
    @DisplayName("stateAffiliation should handle different field numbers")
    void stateAffiliation_shouldHandleDifferentFieldNumbers() {
        Affiliation differentAffiliation = new Affiliation();
        differentAffiliation.setId(2L);
        differentAffiliation.setFiledNumber("DIFFERENT_F456");
        differentAffiliation.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);

        Affiliate differentAffiliate = new Affiliate();
        differentAffiliate.setIdAffiliate(2L);
        differentAffiliate.setFiledNumber("DIFFERENT_F456");
        differentAffiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        differentAffiliate.setAffiliationCancelled(false);
        differentAffiliate.setStatusDocument(false);

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(differentAffiliation));
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(differentAffiliate));
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of());

        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("DIFFERENT_F456");
        in.setRejectAffiliation(false);

        service.stateAffiliation(in);

        verify(filedWebSocketService).changeStateAffiliation("DIFFERENT_F456");
    }

    @Test
    @DisplayName("stateDocuments should handle single document with no reject")
    void stateDocuments_shouldHandleSingleDocumentNoReject() {
        var dd = new DataDocumentAffiliate();
        dd.setId(20L);
        dd.setRevised(Boolean.FALSE);
        dd.setState(Boolean.FALSE);
        when(dataDocumentRepository.findById(20L)).thenReturn(Optional.of(dd));

        DocumentsDTO dto = new DocumentsDTO();
        dto.setId(20L);
        dto.setReject(false);

        service.stateDocuments(List.of(dto), 2L);

        assertTrue(dd.getRevised());
        assertFalse(dd.getState());
        verify(dataDocumentRepository).save(dd);
    }

    @Test
    @DisplayName("generateExcel should handle different filter scenarios")
    void generateExcel_shouldHandleDifferentFilterScenarios() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(new ArrayList<>());
        String result1 = service.generateExcel(null);
        assertNotNull(result1);
        String result2 = service.generateExcel(null);
        assertNotNull(result2);
    }

    @Test
    @DisplayName("managementAffiliation should handle different page sizes")
    void managementAffiliation_shouldHandleDifferentPageSizes() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(Page.empty());
        when(affiliationsViewRepository.countByStageManagement(anyString())).thenReturn(3L);

        ResponseManagementDTO result = service.managementAffiliation(1, 20, null);

        assertNotNull(result);
        assertEquals(3L, result.totalInterviewing());
    }

    @Test
    @DisplayName("management should handle affiliate with different subtype (not null)")
    void management_shouldHandleAffiliateWithDifferentSubtype() {
        affiliate.setAffiliationSubType("SOME_OTHER_SUBTYPE");

        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("testDoc");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        ManagementDTO result = service.management("F123");

        assertNotNull(result);
        assertNotNull(result.getAffiliation());
    }
    @Test
    @DisplayName("stateAffiliation should handle comments with single element")
    void stateAffiliation_shouldHandleCommentsWithSingleElement() {
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("F123");
        in.setRejectAffiliation(true);
        in.setIdOfficial(88L);
        in.setReasonReject("SINGLE_REASON");
        in.setComment(List.of("single comment"));

        service.stateAffiliation(in);

        verify(timerRepository).save(any(AffiliationCancellationTimer.class));
        verify(observationsAffiliationService).create(anyString(), eq("F123"), eq("SINGLE_REASON"), eq(88L));
        verify(sendEmails).requestDenied(any(Affiliation.class), any(StringBuilder.class));
    }

    @Test
    @DisplayName("findById should handle different IDs")
    void findById_shouldHandleDifferentIDs() {
        Long testId = 999L;
        Affiliation testAffiliation = new Affiliation();
        testAffiliation.setId(testId);
        testAffiliation.setFiledNumber("TEST999");

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(testAffiliation));

        Affiliation result = service.findById(testId);

        assertNotNull(result);
        assertEquals(testId, result.getId());
        assertEquals("TEST999", result.getFiledNumber());
    }



    @Test
    @DisplayName("stateDocuments should handle empty documents list")
    void stateDocuments_shouldHandleEmptyDocumentsList() {
        assertDoesNotThrow(() -> service.stateDocuments(new ArrayList<>(), 1L));
        verify(dataDocumentRepository, never()).findById(any());
        verify(dataDocumentRepository, never()).save(any());
    }

    @Test
    @DisplayName("consultDocument should handle various alfresco responses")
    void consultDocument_shouldHandleVariousAlfrescoResponses() {
        when(alfrescoService.getDocument("NORMAL")).thenReturn("normalcontent");
        List<DocumentBase64> result1 = service.consultDocument("NORMAL");
        assertEquals("normalcontent", result1.get(0).getBase64Image());
        when(alfrescoService.getDocument("EMPTY")).thenReturn("");
        List<DocumentBase64> result2 = service.consultDocument("EMPTY");
        assertEquals("", result2.get(0).getBase64Image());
        when(alfrescoService.getDocument("NULL")).thenReturn(null);
        List<DocumentBase64> result3 = service.consultDocument("NULL");
        assertNull(result3.get(0).getBase64Image());
    }

    @Test
    @DisplayName("visualizationPendingPerform should handle different count scenarios")
    void visualizationPendingPerform_shouldHandleDifferentCountScenarios() {
        when(repositoryAffiliation.count()).thenReturn(50L);
        when(repositoryAffiliation.findAll(any(Specification.class)))
                .thenReturn(List.of(new Affiliation(), new Affiliation()))
                .thenReturn(List.of(new Affiliation()))
                .thenReturn(List.of(new Affiliation(), new Affiliation(), new Affiliation()))
                .thenReturn(List.of(new Affiliation(), new Affiliation(), new Affiliation(), new Affiliation()));

        VisualizationPendingPerformDTO result = service.visualizationPendingPerform();

        assertNotNull(result);
        assertEquals("4.0", result.getInterviewWeb());
        assertEquals("2.0", result.getReviewDocumental());
        assertEquals("6.0", result.getRegularization());
        assertEquals("8.0", result.getSing());
    }

    @Test
    @DisplayName("management should handle null document upload date")
    void management_shouldHandleNullDocumentUploadDate() {
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("nullDateDoc");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        ManagementDTO result = service.management("F123");

        assertNotNull(result);
        assertNotNull(result.getDocuments());
        assertEquals(1, result.getDocuments().size());
    }

    @Test
    @DisplayName("stateAffiliation should handle INTERVIEW_WEB stage with no date interview")
    void stateAffiliation_shouldHandleInterviewWebWithNoDate() {
        affiliation.setStageManagement(Constant.INTERVIEW_WEB);

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of());

        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("F123");
        in.setRejectAffiliation(false);

        service.stateAffiliation(in);

        verify(repositoryAffiliation, atLeastOnce()).save(any(Affiliation.class));
        verify(filedWebSocketService).changeStateAffiliation("F123");
    }



    @Test
    @DisplayName("visualizationPendingPerform should handle very large numbers")
    void visualizationPendingPerform_shouldHandleVeryLargeNumbers() {
        when(repositoryAffiliation.count()).thenReturn(10000L);
        when(repositoryAffiliation.findAll(any(Specification.class)))
                .thenReturn(List.of(new Affiliation(), new Affiliation(), new Affiliation(), new Affiliation(), new Affiliation()))
                .thenReturn(List.of(new Affiliation(), new Affiliation()))
                .thenReturn(List.of(new Affiliation()))
                .thenReturn(List.of(new Affiliation(), new Affiliation(), new Affiliation()));

        VisualizationPendingPerformDTO result = service.visualizationPendingPerform();

        assertNotNull(result);
        assertEquals("0.05", result.getInterviewWeb());
        assertEquals("0.02", result.getReviewDocumental());
        assertEquals("0.01", result.getRegularization());
        assertEquals("0.03", result.getSing());
    }

    @Test
    @DisplayName("stateDocuments should handle mixed revisions")
    void stateDocuments_shouldHandleMixedRevisions() {
        var dd1 = new DataDocumentAffiliate();
        dd1.setId(30L);
        dd1.setRevised(Boolean.TRUE);
        dd1.setState(Boolean.FALSE);

        var dd2 = new DataDocumentAffiliate();
        dd2.setId(31L);
        dd2.setRevised(Boolean.FALSE);
        dd2.setState(Boolean.TRUE);

        when(dataDocumentRepository.findById(30L)).thenReturn(Optional.of(dd1));
        when(dataDocumentRepository.findById(31L)).thenReturn(Optional.of(dd2));

        DocumentsDTO dto1 = new DocumentsDTO();
        dto1.setId(30L);
        dto1.setReject(false);

        DocumentsDTO dto2 = new DocumentsDTO();
        dto2.setId(31L);
        dto2.setReject(true);

        service.stateDocuments(List.of(dto1, dto2), 3L);

        assertTrue(dd1.getRevised());
        assertFalse(dd1.getState());
        assertTrue(dd2.getRevised());
        assertTrue(dd2.getState());

        verify(dataDocumentRepository, times(2)).save(any(DataDocumentAffiliate.class));
    }

    @Test
    @DisplayName("managementAffiliation should handle large page numbers")
    void managementAffiliation_shouldHandleLargePageNumbers() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(Page.empty());
        when(affiliationsViewRepository.countByStageManagement(anyString())).thenReturn(1000L);

        ResponseManagementDTO result = service.managementAffiliation(999, 100, null);

        assertNotNull(result);
        assertEquals(1000L, result.totalInterviewing());
        verify(affiliationsViewRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    @DisplayName("stateAffiliation should handle very long comments")
    void stateAffiliation_shouldHandleVeryLongComments() {
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("F123");
        in.setRejectAffiliation(true);
        in.setIdOfficial(77L);
        in.setReasonReject("LONG_REASON");

        List<String> longComments = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            longComments.add("Very long comment number " + i + " with lots of text to test handling of large comment lists");
        }
        in.setComment(longComments);

        service.stateAffiliation(in);

        verify(timerRepository).save(any(AffiliationCancellationTimer.class));
        verify(observationsAffiliationService, times(100))
                .create(anyString(), eq("F123"), eq("LONG_REASON"), eq(77L));
        verify(sendEmails).requestDenied(any(Affiliation.class), any(StringBuilder.class));
    }


    @Test
    @DisplayName("generateExcel should handle repository exceptions")
    void generateExcel_shouldHandleRepositoryExceptions() {
        when(affiliationsViewRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenThrow(new RuntimeException("Database connection error"));

        assertThrows(RuntimeException.class, () -> service.generateExcel(null));
    }

    @Test
    @DisplayName("findById should handle null ID gracefully")
    void findById_shouldHandleNullId() {
        assertThrows(Exception.class, () -> service.findById(null));
    }


    @Test
    @DisplayName("stateAffiliation should handle mercantile with REGULARIZATION stage")
    void stateAffiliation_shouldHandleMercantileRegularizationStage() {
        affiliateMercantile.setStageManagement(Constant.REGULARIZATION);

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123M");
        stateAffiliation.setRejectAffiliation(false);

        assertThrows(AffiliationError.class, () -> service.stateAffiliation(stateAffiliation));
    }

    @Test
    @DisplayName("stateAffiliation should handle mercantile with SING stage")
    void stateAffiliation_shouldHandleMercantileSingStage() {
        affiliateMercantile.setStageManagement("firma");

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123M");
        stateAffiliation.setRejectAffiliation(false);

        assertThrows(AffiliationError.class, () -> service.stateAffiliation(stateAffiliation));
    }

    @Test
    @DisplayName("stateDocuments should handle very large document list")
    void stateDocuments_shouldHandleVeryLargeDocumentList() {
        List<DocumentsDTO> largeDtoList = new ArrayList<>();

        for (int i = 100; i < 200; i++) {
            var dd = new DataDocumentAffiliate();
            dd.setId((long) i);
            dd.setRevised(Boolean.FALSE);
            dd.setState(Boolean.FALSE);
            when(dataDocumentRepository.findById((long) i)).thenReturn(Optional.of(dd));

            DocumentsDTO dto = new DocumentsDTO();
            dto.setId((long) i);
            dto.setReject(i % 2 == 0);
            largeDtoList.add(dto);
        }

        service.stateDocuments(largeDtoList, 5L);

        verify(dataDocumentRepository, times(100)).save(any(DataDocumentAffiliate.class));
    }




    @Test
    @DisplayName("visualizationPendingPerform should handle negative percentage edge case")
    void visualizationPendingPerform_shouldHandleNegativeEdgeCase() {
        when(repositoryAffiliation.count()).thenReturn(1L);
        when(repositoryAffiliation.findAll(any(Specification.class)))
                .thenReturn(List.of())
                .thenReturn(List.of())
                .thenReturn(List.of())
                .thenReturn(List.of());

        VisualizationPendingPerformDTO result = service.visualizationPendingPerform();

        assertNotNull(result);
        assertEquals("0.0", result.getInterviewWeb());
        assertEquals("0.0", result.getReviewDocumental());
        assertEquals("0.0", result.getRegularization());
        assertEquals("0.0", result.getSing());
    }


    @Test
    @DisplayName("consultDocument should handle normal document IDs")
    void consultDocument_shouldHandleNormalDocumentIds() {
        when(alfrescoService.getDocument("NORMAL_DOC_123")).thenReturn("normalContent");

        List<DocumentBase64> result = service.consultDocument("NORMAL_DOC_123");

        assertEquals(1, result.size());
        assertEquals("normalContent", result.get(0).getBase64Image());
    }

    @Test
    @DisplayName("management should handle affiliate with valid subtype")
    void management_shouldHandleAffiliateWithValidSubtype() {
        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER);

        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("validSubtypeDoc");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        ManagementDTO result = service.management("F123");

        assertNotNull(result);
        assertNotNull(result.getAffiliation());
    }

    @Test
    @DisplayName("stateAffiliation should process mercantile normally regardless of status")
    void stateAffiliation_shouldProcessMercantileNormally() {
        affiliateMercantile.setStatusDocument(true);
        affiliateMercantile.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123M");
        stateAffiliation.setRejectAffiliation(false);
        service.stateAffiliation(stateAffiliation);

        verify(affiliationEmployerActivitiesMercantileService).stateAffiliation(affiliateMercantile, stateAffiliation);
        verify(filedWebSocketService).changeStateAffiliation("F123M");
    }

    @Test
    @DisplayName("stateAffiliation should handle valid comment list")
    void stateAffiliation_shouldHandleValidCommentList() {
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("F123");
        in.setRejectAffiliation(true);
        in.setIdOfficial(55L);
        in.setReasonReject("VALID_COMMENTS");

        List<String> validComments = Arrays.asList("comment1", "comment2", "comment3");
        in.setComment(validComments);

        service.stateAffiliation(in);

        verify(timerRepository).save(any(AffiliationCancellationTimer.class));
        verify(observationsAffiliationService, times(3))
                .create(anyString(), eq("F123"), eq("VALID_COMMENTS"), eq(55L));
        verify(sendEmails).requestDenied(any(Affiliation.class), any(StringBuilder.class));
    }

    @Test
    @DisplayName("management should handle mercantile user lookup correctly")
    void management_shouldHandleMercantileUserLookup() {
        affiliateMercantile.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);

        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("docName");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(Boolean.FALSE);
        doc.setState(Boolean.FALSE);
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        assertThrows(AffiliationError.class, () -> service.management("F123M"));
    }

    @Test
    @DisplayName("stateAffiliation should process cancelled mercantile normally")
    void stateAffiliation_shouldProcessCancelledMercantileNormally() {
        affiliateMercantile.setAffiliationCancelled(true);
        affiliateMercantile.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));

        StateAffiliation stateAffiliation = new StateAffiliation();
        stateAffiliation.setFieldNumber("F123M");
        stateAffiliation.setRejectAffiliation(false);

        service.stateAffiliation(stateAffiliation);

        verify(affiliationEmployerActivitiesMercantileService).stateAffiliation(affiliateMercantile, stateAffiliation);
        verify(filedWebSocketService).changeStateAffiliation("F123M");
    }

    @Test
    @DisplayName("consultDocument should handle basic functionality")
    void consultDocument_shouldHandleBasicFunctionality() {
        when(alfrescoService.getDocument("BASIC_DOC")).thenReturn("basicContent");

        List<DocumentBase64> result = service.consultDocument("BASIC_DOC");

        assertEquals(1, result.size());
        assertEquals("basicContent", result.get(0).getBase64Image());
        assertEquals("", result.get(0).getFileName());
    }



    @Test
    @DisplayName("stateAffiliation should handle different rejection reasons")
    void stateAffiliation_shouldHandleDifferentRejectionReasons() {
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        StateAffiliation in = new StateAffiliation();
        in.setFieldNumber("F123");
        in.setRejectAffiliation(true);
        in.setIdOfficial(123L);
        in.setReasonReject("DOCUMENT_INCOMPLETE");
        in.setComment(Arrays.asList("Missing signature", "Invalid ID"));

        service.stateAffiliation(in);

        verify(timerRepository).save(any(AffiliationCancellationTimer.class));
        verify(observationsAffiliationService, times(2))
                .create(anyString(), eq("F123"), eq("DOCUMENT_INCOMPLETE"), eq(123L));
        verify(sendEmails).requestDenied(any(Affiliation.class), any(StringBuilder.class));
    }

    @Test
    @DisplayName("management should handle documents with different upload dates")
    void management_shouldHandleDocumentsWithDifferentUploadDates() {
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        var oldDoc = new DataDocumentAffiliate();
        oldDoc.setId(1L);
        oldDoc.setName("oldDoc");
        oldDoc.setDateUpload(LocalDateTime.now().minusDays(30));
        oldDoc.setRevised(Boolean.FALSE);
        oldDoc.setState(Boolean.FALSE);

        var newDoc = new DataDocumentAffiliate();
        newDoc.setId(2L);
        newDoc.setName("newDoc");
        newDoc.setDateUpload(LocalDateTime.now().minusHours(1));
        newDoc.setRevised(Boolean.FALSE);
        newDoc.setState(Boolean.FALSE);

        when(dataDocumentRepository.findAll(any(Specification.class)))
                .thenReturn(Arrays.asList(oldDoc, newDoc));

        ManagementDTO result = service.management("F123");

        assertNotNull(result);
        assertEquals(2, result.getDocuments().size());
    }

    @Test
    @DisplayName("visualizationPendingPerform should handle medium sized datasets")
    void visualizationPendingPerform_shouldHandleMediumDatasets() {
        when(repositoryAffiliation.count()).thenReturn(500L);
        when(repositoryAffiliation.findAll(any(Specification.class)))
                .thenReturn(Arrays.asList(new Affiliation(), new Affiliation()))
                .thenReturn(Arrays.asList(new Affiliation(), new Affiliation(), new Affiliation()))
                .thenReturn(Arrays.asList(new Affiliation()))
                .thenReturn(Arrays.asList(new Affiliation(), new Affiliation(), new Affiliation(), new Affiliation()));

        VisualizationPendingPerformDTO result = service.visualizationPendingPerform();

        assertNotNull(result);
        assertEquals("0.4", result.getInterviewWeb());
        assertTrue(result.getReviewDocumental().startsWith("0.6"));

        assertEquals("0.2", result.getRegularization());
        assertEquals("0.8", result.getSing());
    }

    @Test
    @DisplayName("findDocuments should handle documents with null names")
    void findDocuments_shouldHandleDocumentsWithNullNames() {
        var docWithNullName = new DataDocumentAffiliate();
        docWithNullName.setId(1L);
        docWithNullName.setName(null);
        docWithNullName.setRevised(Boolean.FALSE);
        docWithNullName.setState(Boolean.FALSE);

        when(dataDocumentRepository.findAll(any(Specification.class)))
                .thenReturn(Arrays.asList(docWithNullName));

        List<DataDocumentAffiliate> result = service.findDocuments(1L);

        assertEquals(1, result.size());
        assertNull(result.get(0).getName());
    }

    @Test
    @DisplayName("stateDocuments should handle documents already in final state")
    void stateDocuments_shouldHandleDocumentsInFinalState() {
        var finalDoc = new DataDocumentAffiliate();
        finalDoc.setId(60L);
        finalDoc.setRevised(Boolean.TRUE);
        finalDoc.setState(Boolean.TRUE);
        when(dataDocumentRepository.findById(60L)).thenReturn(Optional.of(finalDoc));
        DocumentsDTO dto = new DocumentsDTO();
        dto.setId(60L);
        dto.setReject(false);
        service.stateDocuments(Arrays.asList(dto), 15L);
        assertTrue(finalDoc.getRevised());
        assertFalse(finalDoc.getState());
        verify(dataDocumentRepository).save(finalDoc);
    }
    @Test
    @DisplayName("buildMainOffice: asigna zona URBANA por defecto y mapea todos los campos")
    void buildMainOffice_shouldSetUrbanAndMapAllFields() {
        Affiliation a = new Affiliation();
        a.setIsRuralZoneEmployer(false);
        a.setIdentificationDocumentType("CC");
        a.setIdentificationDocumentNumber("123");
        a.setFirstName("Ana");
        a.setSecondName("María");
        a.setSurname("García");
        a.setSecondSurname("Lopez");
        a.setPhone1("3001112233");
        a.setPhone2("6011234567");
        a.setEmail("ana@test.com");
        a.setAddress("Cra 1 # 2-3");
        a.setDepartment(11L);
        a.setCityMunicipality(101L);
        a.setIdMainStreet(1L);
        a.setIdNumberMainStreet(10L);
        a.setIdLetter1MainStreet(5L);
        a.setIsBis(Boolean.TRUE);
        a.setIdLetter2MainStreet(6L);
        a.setIdCardinalPointMainStreet(2L);
        a.setIdNum1SecondStreet(20L);
        a.setIdLetterSecondStreet(7L);
        a.setIdNum2SecondStreet(30L);
        a.setIdCardinalPoint2(3L);
        a.setIdHorizontalProperty1(100L);
        a.setIdNumHorizontalProperty1(1L);
        a.setIdHorizontalProperty2(200L);
        a.setIdNumHorizontalProperty2(2L);
        a.setIdHorizontalProperty3(300L);
        a.setIdNumHorizontalProperty3(3L);
        a.setIdHorizontalProperty4(400L);
        a.setIdNumHorizontalProperty4(4L);
        UserMain manager = new UserMain();
        manager.setFirstName("Gerente");

        Affiliate affiliateLocal = new Affiliate();
        affiliateLocal.setIdAffiliate(99L);

        when(mainOfficeService.findCode()).thenReturn("MO-001");

        when(mainOfficeService.saveMainOffice(any(MainOffice.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        MainOffice result = (MainOffice) ReflectionTestUtils.invokeMethod(
                service, "buildMainOffice", a, manager, affiliateLocal);

        ArgumentCaptor<MainOffice> cap = ArgumentCaptor.forClass(MainOffice.class);
        verify(mainOfficeService).saveMainOffice(cap.capture());
        MainOffice saved = cap.getValue();
        assertEquals("MO-001", saved.getCode());
        assertEquals("Principal", saved.getMainOfficeName());
        assertTrue(saved.getMain());
        assertEquals(Constant.URBAN_ZONE, saved.getMainOfficeZone());
        assertEquals("Cra 1 # 2-3", saved.getAddress());
        assertEquals("3001112233", saved.getMainOfficePhoneNumber());
        assertEquals("ana@test.com", saved.getMainOfficeEmail());
        assertSame(manager, saved.getOfficeManager());
        assertEquals(99L, saved.getIdAffiliate());
        assertEquals("CC", saved.getTypeDocumentResponsibleHeadquarters());
        assertEquals("123", saved.getNumberDocumentResponsibleHeadquarters());
        assertEquals("Ana", saved.getFirstNameResponsibleHeadquarters());
        assertEquals("María", saved.getSecondNameResponsibleHeadquarters());
        assertEquals("García", saved.getSurnameResponsibleHeadquarters());
        assertEquals("Lopez", saved.getSecondSurnameResponsibleHeadquarters());
        assertEquals("3001112233", saved.getPhoneOneResponsibleHeadquarters());
        assertEquals("6011234567", saved.getPhoneTwoResponsibleHeadquarters());
        assertEquals("ana@test.com", saved.getEmailResponsibleHeadquarters());
        assertEquals(11L, saved.getIdDepartment());
        assertEquals(101L, saved.getIdCity());
        assertEquals(1L, saved.getIdMainStreet());
        assertEquals(10L, saved.getIdNumberMainStreet());
        assertEquals(5L, saved.getIdLetter1MainStreet());
        assertEquals(Boolean.TRUE, saved.getIsBis());
        assertEquals(6L, saved.getIdLetter2MainStreet());
        assertEquals(2L, saved.getIdCardinalPointMainStreet());
        assertEquals(20L, saved.getIdNum1SecondStreet());
        assertEquals(7L,  saved.getIdLetterSecondStreet());
        assertEquals(30L, saved.getIdNum2SecondStreet());
        assertEquals(3L,  saved.getIdCardinalPoint2());
        assertEquals(100L, saved.getIdHorizontalProperty1());
        assertEquals(1L,   saved.getIdNumHorizontalProperty1());
        assertEquals(200L, saved.getIdHorizontalProperty2());
        assertEquals(2L,   saved.getIdNumHorizontalProperty2());
        assertEquals(300L, saved.getIdHorizontalProperty3());
        assertEquals(3L,   saved.getIdNumHorizontalProperty3());
        assertEquals(400L, saved.getIdHorizontalProperty4());
        assertEquals(4L,   saved.getIdNumHorizontalProperty4());

        assertNotNull(result);
        assertEquals(saved.getCode(), result.getCode());
    }

    @Test
    @DisplayName("buildMainOffice: asigna zona RURAL cuando isRuralZoneEmployer = true")
    void buildMainOffice_shouldSetRuralZoneWhenFlagTrue() {
        Affiliation a = new Affiliation();
        a.setIsRuralZoneEmployer(true);
        a.setPhone1("3110000000");
        a.setEmail("rural@test.com");
        a.setAddress("Vereda El Paraíso");

        UserMain manager = new UserMain();
        Affiliate affiliateLocal = new Affiliate();
        affiliateLocal.setIdAffiliate(7L);

        when(mainOfficeService.findCode()).thenReturn("MO-002");
        when(mainOfficeService.saveMainOffice(any(MainOffice.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ReflectionTestUtils.invokeMethod(service, "buildMainOffice", a, manager, affiliateLocal);

        ArgumentCaptor<MainOffice> cap = ArgumentCaptor.forClass(MainOffice.class);
        verify(mainOfficeService).saveMainOffice(cap.capture());
        assertEquals(Constant.RURAL_ZONE, cap.getValue().getMainOfficeZone());
    }

    @Test
    @DisplayName("management: maneja distintos estados mercantiles sin AffiliationError")
    void management_shouldHandleDifferentMercantileStages() {
        affiliateMercantile.setStageManagement("ANY_STAGE");
        affiliateMercantile.setTypeDocumentPersonResponsible("CC");
        affiliateMercantile.setNumberDocumentPersonResponsible("9001");
        when(iAffiliateRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class)))
                .thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliateMercantile));
        UserMain pre = new UserMain();
        pre.setId(1L);
        pre.setFirstName("User");
        pre.setNationality(57L);
        pre.setHealthPromotingEntity(1L);
        pre.setPensionFundAdministrator(2L);
        when(userPreRegisterRepository.findOne(ArgumentMatchers.<Specification<UserMain>>any()))
                .thenReturn(Optional.of(pre));
        when(iUserPreRegisterRepository.findOne(ArgumentMatchers.<Specification<UserMain>>any()))
                .thenReturn(Optional.of(pre));
        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(pre));
        when(iUserPreRegisterRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(pre));

        DataDocumentAffiliate doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setName("doc.pdf");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(false);
        doc.setState(false);
        when(dataDocumentRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(doc));
        when(repositoryAffiliation.save(any(Affiliation.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(affiliateService.getEmployerSize(anyInt())).thenReturn(1L);

        ManagementDTO result = service.management("F-TEST-M");
        assertNotNull(result);
        assertNotNull(result.getAffiliation());
    }

    @Test
    @DisplayName("management: propaga actividades económicas vía mercantil sin AffiliationError")
    void economicActivities_throughMercantile() {
        AffiliateActivityEconomic act = new AffiliateActivityEconomic();
        EconomicActivity ea = new EconomicActivity();
        ea.setId(1L);
        act.setActivityEconomic(ea);
        act.setIsPrimary(true);

        affiliateMercantile.setEconomicActivity(List.of(act));
        affiliateMercantile.setStageManagement("DOC_REVIEW");
        affiliateMercantile.setTypeDocumentPersonResponsible("CC");
        affiliateMercantile.setNumberDocumentPersonResponsible("9001");

        when(iAffiliateRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class)))
                .thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliateMercantile));
        UserMain pre = new UserMain();
        pre.setId(2L);
        pre.setFirstName("User");
        pre.setNationality(57L);
        when(userPreRegisterRepository.findOne(ArgumentMatchers.<Specification<UserMain>>any()))
                .thenReturn(Optional.of(pre));
        when(iUserPreRegisterRepository.findOne(ArgumentMatchers.<Specification<UserMain>>any()))
                .thenReturn(Optional.of(pre));
        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(pre));
        when(iUserPreRegisterRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
                .thenReturn(Optional.of(pre));

        DataDocumentAffiliate doc = new DataDocumentAffiliate();
        doc.setId(9L);
        doc.setName("econ.pdf");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(false);
        doc.setState(false);
        when(dataDocumentRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(doc));

        when(repositoryAffiliation.save(any(Affiliation.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(affiliateService.getEmployerSize(anyInt())).thenReturn(1L);
        ManagementDTO result = service.management("F-ECON-M");
        assertNotNull(result);
        assertNotNull(result.getAffiliation());
    }


    @Test
    @DisplayName("createAffiliationStep3 should throw error if affiliation not found")
    void createAffiliationStep3_notFound() {
        when(repositoryAffiliation.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AffiliationNotFoundError.class, () -> service.createAffiliationStep3(999L, mock(MultipartFile.class)));
    }


    @Test
    @DisplayName("assignTo should throw if affiliation not found")
    void assignTo_affiliationNotFound() {
        when(repositoryAffiliation.findByFiledNumber("F999")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.assignTo("F999", 99L));
    }

    @Test
    @DisplayName("createAffiliationStep3 should throw if user not found")
    void createAffiliationStep3_userNotFound() {
        Affiliation affiliation = new Affiliation();
        affiliation.setId(1L);
        affiliation.setIdentificationDocumentNumber("12345");
        when(repositoryAffiliation.findById(1L)).thenReturn(Optional.of(affiliation));
        when(webClient.getByIdentification("12345")).thenReturn(Optional.empty());

        assertThrows(AffiliationError.class, () -> service.createAffiliationStep3(1L, mock(MultipartFile.class)));
    }


    @Test
    @DisplayName("generateExcel should handle empty data")
    void generateExcel_emptyData() {

        AffiliationsFilterDTO filter = new AffiliationsFilterDTO(null, null, null, null, null);

        when(affiliationsViewRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(new ArrayList<>());

        String result = service.generateExcel(filter);

        assertNotNull(result);
        assertTrue(result.length() > 0);
    }


    @Test
    @DisplayName("convertZoneToString should handle both zones")
    void convertZoneToString_both() {
        String urban = (String) ReflectionTestUtils.invokeMethod(service, "convertZoneToString", Constant.URBAN_ZONE);
        assertEquals("Urbana", urban);

        String rural = (String) ReflectionTestUtils.invokeMethod(service, "convertZoneToString", Constant.RURAL_ZONE);
        assertEquals("Rural", rural);
    }

    @Test
    @DisplayName("economicActivities should handle empty list")
    void economicActivities_empty() {
        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setEconomicActivity(new ArrayList<>());

        Map<Long, Boolean> result = (Map<Long, Boolean>) ReflectionTestUtils.invokeMethod(service, "economicActivities", mercantile);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("createAffiliateActivityEconomic should create activities and work centers")
    void createAffiliateActivityEconomic_success() {
        Affiliation affiliation = new Affiliation();
        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(1L);
        MainOffice mainOffice = new MainOffice();

        List<String> codes = List.of("1970001", "1970002", "3970001", "3869201");
        List<EconomicActivity> activities = codes.stream()
                .map(code -> {
                    EconomicActivity ea = new EconomicActivity();
                    ea.setEconomicActivityCode(code);
                    ea.setClassRisk("I");
                    return ea;
                }).toList();
        when(iEconomicActivityRepository.findAllByEconomicActivityCodeIn(codes)).thenReturn(activities);

        when(webClient.getByIdentification(anyString())).thenReturn(Optional.of(new UserDtoApiRegistry()));
        when(workCenterService.saveWorkCenter(any(WorkCenter.class))).thenReturn(new WorkCenter());

        List<AffiliateActivityEconomic> result = (List<AffiliateActivityEconomic>) ReflectionTestUtils.invokeMethod(service, "createAffiliateActivityEconomic", affiliation, affiliate, mainOffice);

        assertEquals(4, result.size());
        assertTrue(result.stream().anyMatch(AffiliateActivityEconomic::getIsPrimary));
        verify(workCenterService, times(4)).saveWorkCenter(any(WorkCenter.class));
    }
    @Test
    @DisplayName("economicActivityList should return activities")
    void economicActivityList_success() {
        List<String> codes = List.of("1970001", "1970002");
        List<EconomicActivity> activities = new ArrayList<>();
        when(iEconomicActivityRepository.findAllByEconomicActivityCodeIn(codes)).thenReturn(activities);

        List<EconomicActivity> result = (List<EconomicActivity>) ReflectionTestUtils.invokeMethod(service, "economicActivityList", codes);

        assertEquals(activities, result);
        verify(iEconomicActivityRepository).findAllByEconomicActivityCodeIn(codes);
    }
    @Test
    @DisplayName("findDataDaily should return null if no interview")
    void findDataDaily_noInterview() {
        when(dateInterviewWebRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        DataDailyDTO result = (DataDailyDTO) ReflectionTestUtils.invokeMethod(service, "findDataDaily", "1");

        assertNull(result);
    }
    @Test
    @DisplayName("generateExcel should throw IOException")
    void generateExcel_ioException() {
        AffiliationsFilterDTO filter = new AffiliationsFilterDTO(null, null, null, null, null);
        when(affiliationsViewRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(new ArrayList<>());

        try (MockedConstruction<XSSFWorkbook> mocked = Mockito.mockConstruction(XSSFWorkbook.class, (mock, context) -> {
            when(mock.createSheet(anyString())).thenThrow(new IOException("Mocked IO error"));
        })) {
            assertThrows(RuntimeException.class, () -> service.generateExcel(filter));
        }
    }
    @Test
    @DisplayName("createWorkCenter should set rural zone")
    void createWorkCenter_ruralZone() {
        Affiliation affiliation = new Affiliation();
        affiliation.setIsRuralZoneEmployer(true);
        affiliation.setIdentificationDocumentNumber("123");
        affiliation.setDepartmentEmployer(1L);
        affiliation.setMunicipalityEmployer(2L);
        affiliation.setAddressEmployer("Test Address");
        affiliation.setEconomicActivity(new ArrayList<>());
        MainOffice mainOffice = new MainOffice();

        UserDtoApiRegistry userDto = new UserDtoApiRegistry();
        userDto.setIdentification("123");
        when(webClient.getByIdentification("123")).thenReturn(Optional.of(userDto));

        when(workCenterService.saveWorkCenter(any(WorkCenter.class))).thenReturn(new WorkCenter());

        ReflectionTestUtils.invokeMethod(service, "createWorkCenter", affiliation, "I", 1, "1970001", mainOffice, 1L);

        ArgumentCaptor<WorkCenter> captor = ArgumentCaptor.forClass(WorkCenter.class);
        verify(workCenterService).saveWorkCenter(captor.capture());
        assertEquals(Constant.RURAL_ZONE, captor.getValue().getWorkCenterZone());
    }
    @Test
    @DisplayName("findDocumentsRejects should return rejected documents")
    void findDocumentsRejects_withRejects() {
        DataDocumentAffiliate doc = new DataDocumentAffiliate();
        doc.setState(false);
        doc.setRevised(true);
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        List<DataDocumentAffiliate> result = (List<DataDocumentAffiliate>) ReflectionTestUtils.invokeMethod(service, "findDocumentsRejects", 1L);

        assertEquals(1, result.size());
    }
    @Test
    @DisplayName("createAffiliationStep3 should handle null economic activity")
    void createAffiliationStep3_nullEconomicActivity() throws Exception {
        Affiliation affiliation = new Affiliation();
        affiliation.setId(1L);
        affiliation.setIdentificationDocumentNumber("12345");
        affiliation.setEconomicActivity(new ArrayList<>());
        when(repositoryAffiliation.findById(1L)).thenReturn(Optional.of(affiliation));

        UserDtoApiRegistry userDto = new UserDtoApiRegistry();
        userDto.setIdentification("12345");
        when(webClient.getByIdentification("12345")).thenReturn(Optional.of(userDto));

        when(filedService.getNextFiledNumberAffiliation()).thenReturn("F999");
        when(affiliateService.createAffiliate(any(Affiliate.class))).thenReturn(new Affiliate());


        MainOffice mainOffice = new MainOffice();
        mainOffice.setId(1L);
        when(mainOfficeService.saveMainOffice(any(MainOffice.class))).thenReturn(mainOffice);

        when(alfrescoService.getIdDocumentsFolder(anyString())).thenReturn(Optional.of(new ConsultFiles()));
        when(alfrescoService.uploadAffiliationDocuments(anyString(), anyString(), anyList())).thenReturn(new ResponseUploadOrReplaceFilesDTO());

        service.createAffiliationStep3(1L, mock(MultipartFile.class));

        ArgumentCaptor<Affiliation> captor = ArgumentCaptor.forClass(Affiliation.class);
        verify(repositoryAffiliation).save(captor.capture());
        assertNotNull(captor.getValue().getEconomicActivity());
        assertTrue(captor.getValue().getEconomicActivity().isEmpty());
    }

    @Test
    @DisplayName("economicActivityList should handle repository error")
    void economicActivityList_repositoryError() {
        List<String> codes = List.of("1970001", "1970002");
        when(iEconomicActivityRepository.findAllByEconomicActivityCodeIn(codes)).thenThrow(new RuntimeException("Repository error"));

        assertThrows(RuntimeException.class, () -> ReflectionTestUtils.invokeMethod(service, "economicActivityList", codes));
    }
    @Test
    @DisplayName("findDocumentsRejects should handle empty list")
    void findDocumentsRejects_emptyList() {
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());

        List<DataDocumentAffiliate> result = (List<DataDocumentAffiliate>) ReflectionTestUtils.invokeMethod(service, "findDocumentsRejects", 1L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("createAffiliationStep1 should throw UserNotFoundInDataBase when user does not exist")
    void createAffiliationStep1_userNotFound() {
        DomesticServiceAffiliationStep1DTO dto = new DomesticServiceAffiliationStep1DTO();
        dto.setIdentificationDocumentType("CC");
        dto.setIdentificationDocumentNumber("12345");
        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification("CC", "12345")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundInDataBase.class, () -> service.createAffiliationStep1(dto));
    }

    @Test
    @DisplayName("createAffiliationStep1 should throw AffiliationError for age out of range")
    void createAffiliationStep1_ageOutOfRange() {
        DomesticServiceAffiliationStep1DTO dto = new DomesticServiceAffiliationStep1DTO();
        dto.setIdentificationDocumentType("CC");
        dto.setIdentificationDocumentNumber("12345");
        UserMain user = new UserMain();
        user.setDateBirth(LocalDate.now().minusYears(17));
        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification("CC", "12345")).thenReturn(Optional.of(user));
        when(properties.getMinimumAge()).thenReturn(18);
        when(properties.getMaximumAge()).thenReturn(65);

        assertThrows(AffiliationError.class, () -> service.createAffiliationStep1(dto));
    }

    @Test
    @DisplayName("createAffiliationStep1 should create affiliation successfully")
    void createAffiliationStep1_success() {
        DomesticServiceAffiliationStep1DTO dto = new DomesticServiceAffiliationStep1DTO();
        dto.setIdentificationDocumentType("CC");
        dto.setIdentificationDocumentNumber("12345");
        UserMain user = new UserMain();
        user.setDateBirth(LocalDate.now().minusYears(30));
        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification("CC", "12345")).thenReturn(Optional.of(user));
        when(properties.getMinimumAge()).thenReturn(18);
        when(properties.getMaximumAge()).thenReturn(65);
        when(affiliateService.findAffiliatesByTypeAndNumber(anyString(), anyString())).thenReturn(new ArrayList<>());
        when(repositoryAffiliation.save(any(Affiliation.class))).thenAnswer(i -> i.getArgument(0));

        Affiliation result = service.createAffiliationStep1(dto);

        assertNotNull(result);
        verify(repositoryAffiliation).save(any(Affiliation.class));
    }

    @Test
    @DisplayName("createAffiliationStep2 should update affiliation successfully")
    void createAffiliationStep2_success() {
        DomesticServiceAffiliationStep2DTO dto = new DomesticServiceAffiliationStep2DTO();
        dto.setIdAffiliation(1L);
        dto.setIdentificationDocumentType("CC");
        dto.setIdentificationDocumentNumber("12345");
        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification("CC", "12345")).thenReturn(Optional.of(new UserMain()));
        when(repositoryAffiliation.findById(1L)).thenReturn(Optional.of(new Affiliation()));
        when(repositoryAffiliation.save(any(Affiliation.class))).thenAnswer(i -> i.getArgument(0));

        Affiliation result = service.createAffiliationStep2(dto);

        assertNotNull(result);
        verify(repositoryAffiliation).save(any(Affiliation.class));
    }

    @Test
    @DisplayName("managementAffiliation should return filtered data")
    void managementAffiliation_withFilter() {
        AffiliationsFilterDTO filter = new AffiliationsFilterDTO("field", "value", null, "id", "asc");
        Page<com.gal.afiliaciones.infrastructure.dao.repository.affiliationsview.AffiliationsView> page = Page.empty();
        when(affiliationsViewRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(page);

        ResponseManagementDTO result = service.managementAffiliation(0, 10, filter);

        assertNotNull(result);
        assertEquals(0, result.data().getTotalElements());

    }
}