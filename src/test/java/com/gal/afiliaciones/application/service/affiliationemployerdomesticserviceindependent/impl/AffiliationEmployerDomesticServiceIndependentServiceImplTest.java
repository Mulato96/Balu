package com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

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
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
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
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.ManagementDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.VisualizationPendingPerformDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;


@ExtendWith(MockitoExtension.class)
class AffiliationEmployerDomesticServiceIndependentServiceImplTest {

    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    @Mock
    private AffiliateRepository iAffiliateRepository;
    @Mock
    private AffiliateMercantileRepository affiliateMercantileRepository;
    @Mock
    private IDataDocumentRepository dataDocumentRepository;
    @Mock
    private IUserPreRegisterRepository iUserPreRegisterRepository;
    @Mock
    private FamilyMemberRepository familyMemberRepository;
    @Mock
    private DangerRepository dangerRepository;
    @Mock
    private IEconomicActivityService economicActivityService;
    @Mock
    private DateInterviewWebRepository dateInterviewWebRepository;
    @Mock
    private DailyService dailyService;
    @Mock
    private GenericWebClient webClient;
    @Mock
    private AffiliateService affiliateService;
    @Mock
    private IAffiliationCancellationTimerRepository timerRepository;
    @Mock
    private AlfrescoService alfrescoService;
    @Mock
    private SendEmails sendEmails;
    @Mock
    private FiledService filedService;
    @Mock
    private MainOfficeService mainOfficeService;
    @Mock
    private WorkCenterService workCenterService;
    @Mock
    private CollectProperties properties;
    @Mock
    private AffiliationEmployerActivitiesMercantileService affiliationEmployerActivitiesMercantileService;
    @Mock
    private ObservationsAffiliationService observationsAffiliationService;
    @Mock
    private FiledWebSocketService filedWebSocketService;
    @Mock
    private ScheduleInterviewWebService scheduleInterviewWebService;
    @Mock
    private MessageErrorAge messageError;
    @Mock
    private DocumentNameStandardizationService documentNameStandardizationService;
    @Mock
    private IEconomicActivityRepository iEconomicActivityRepository;
    @Mock
    private AffiliationsViewRepository affiliationsViewRepository;

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

        affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setId(1L);
        affiliateMercantile.setFiledNumber("F123M");
        affiliateMercantile.setStageManagement(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);
        affiliateMercantile.setAffiliationCancelled(false);
        affiliateMercantile.setStatusDocument(false);
    }

    @Test
    @DisplayName("visualizationPendingPerform should return correct percentages")
    void visualizationPendingPerform_shouldReturnCorrectPercentages() {
        // Given
        when(repositoryAffiliation.count()).thenReturn(100L);
        when(repositoryAffiliation.findAll(any(Specification.class)))
                .thenReturn(List.of(new Affiliation())) // 1%
                .thenReturn(List.of(new Affiliation(), new Affiliation())) // 2%
                .thenReturn(List.of(new Affiliation(), new Affiliation(), new Affiliation())) // 3%
                .thenReturn(List.of(new Affiliation(), new Affiliation(), new Affiliation(), new Affiliation())); // 4%

        // When
        VisualizationPendingPerformDTO result = service.visualizationPendingPerform();

        // Then
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
        // Given
        String field = "123456789";
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        DataDocumentAffiliate doc = new DataDocumentAffiliate();
        doc.setId(1L);
        doc.setIdAlfresco("alfrescoId");
        doc.setName("docName");
        doc.setDateUpload(LocalDateTime.now());
        doc.setRevised(false);
        doc.setState(false);
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(List.of(doc));

        // When
        ManagementDTO result = service.management(field);

        // Then
        assertNotNull(result);
        assertNotNull(result.getAffiliation());
        assertFalse(result.getDocuments().isEmpty());
        assertEquals(1, result.getDocuments().size());
        assertEquals("docName", result.getDocuments().get(0).getName());
    }

    @Test
    @DisplayName("management should throw error when affiliate not found")
    void management_shouldThrowErrorWhenAffiliateNotFound() {
        // Given
        String field = "123456789";
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AffiliationError.class, () -> service.management(field));
    }

    @Test
    @DisplayName("management should throw error when no affiliation or mercantile found")
    void management_shouldThrowErrorWhenNoAffiliationOrMercantileFound() {
        // Given
        String field = "123456789";
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AffiliationError.class, () -> service.management(field));
    }

    @Test
    @DisplayName("management should throw error for cancelled affiliation")
    void management_shouldThrowErrorForCancelledAffiliation() {
        // Given
        String field = "123456789";
        affiliate.setAffiliationCancelled(true);
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(AffiliationError.class, () -> service.management(field));
    }

    @Test
    @DisplayName("management should throw error when no documents found")
    void management_shouldThrowErrorWhenNoDocumentsFound() {
        // Given
        String field = "123456789";
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(dataDocumentRepository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());

        // When & Then
        assertThrows(UserNotFoundInDataBase.class, () -> service.management(field));
    }
}
