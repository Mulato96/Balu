package com.gal.afiliaciones.application.service.affiliate.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.gal.afiliaciones.application.service.CertificateBulkService;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliate.ScheduleInterviewWebService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.generalnovelty.impl.GeneralNoveltyServiceImpl;
import com.gal.afiliaciones.application.service.novelty.NoveltyRuafService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.DateInterviewWeb;
import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.RetirementReason;
import com.gal.afiliaciones.domain.model.affiliate.RetirementReasonWorker;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationCancellationTimerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.retirement.RetirementRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirementreason.RetirementReasonRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirementreason.RetirementReasonWorkerRepository;
import com.gal.afiliaciones.infrastructure.dto.affiliate.TemplateSendEmailsDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.SaveGeneralNoveltyRequest;
import com.gal.afiliaciones.infrastructure.dto.noveltyruaf.DataContributorDTO;
import com.gal.afiliaciones.infrastructure.dto.workerretirement.DataWorkerRetirementDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

class ScheduledTimersAffiliationsServiceImplTest {

    private SendEmails sendEmails;
    private SimpMessagingTemplate messagingTemplate;
    private AffiliateRepository iAffiliateRepository;
    private ScheduleInterviewWebService scheduleInterviewWebService;
    private IAffiliationCancellationTimerRepository timerRepository;
    private AffiliateMercantileRepository affiliateMercantileRepository;
    private IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    private RetirementRepository retirementRepository;
    private IUserPreRegisterRepository userPreRegisterRepository;
    private CollectProperties properties;
    private AffiliateService affiliateService;
    private GeneralNoveltyServiceImpl generalNoveltyService;

    private ArlInformationDao arlInformationDao;
    private NoveltyRuafService noveltyRuafService;
    private AffiliationDependentRepository affiliationDependentRepository;

    private ScheduledTimersAffiliationsServiceImpl service;

    private RetirementReasonWorkerRepository retirementReasonWorkerRepository;

    private RetirementReasonRepository retirementReasonRepository;

    private CertificateBulkService certificateService;

    @BeforeEach
    void setup() {
        sendEmails = mock(SendEmails.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        iAffiliateRepository = mock(AffiliateRepository.class);
        scheduleInterviewWebService = mock(ScheduleInterviewWebService.class);
        timerRepository = mock(IAffiliationCancellationTimerRepository.class);
        affiliateMercantileRepository = mock(AffiliateMercantileRepository.class);
        repositoryAffiliation = mock(IAffiliationEmployerDomesticServiceIndependentRepository.class);
        retirementRepository = mock(RetirementRepository.class);
        userPreRegisterRepository = mock(IUserPreRegisterRepository.class);
        properties = mock(CollectProperties.class);
        affiliateService = mock(AffiliateService.class);
        arlInformationDao = mock(ArlInformationDao.class);
        noveltyRuafService = mock(NoveltyRuafService.class);
        affiliationDependentRepository = mock(AffiliationDependentRepository.class);
        generalNoveltyService = mock(GeneralNoveltyServiceImpl.class);
        retirementReasonWorkerRepository = mock(RetirementReasonWorkerRepository.class);
        retirementReasonRepository = mock(RetirementReasonRepository.class);

        service = new ScheduledTimersAffiliationsServiceImpl(
                sendEmails,
                messagingTemplate,
                iAffiliateRepository,
                scheduleInterviewWebService,
                timerRepository,
                affiliateMercantileRepository,
                repositoryAffiliation,
                retirementRepository,
                userPreRegisterRepository,
                properties,
                affiliateService,
                arlInformationDao,
                noveltyRuafService,
                affiliationDependentRepository,
                generalNoveltyService,
                retirementReasonWorkerRepository,
                retirementReasonRepository,
                certificateService);
    }

    @Test
    void testSendNotifications_sendsMessagesAndEmails() {
        DateInterviewWeb interview = new DateInterviewWeb();
        interview.setDay(LocalDate.now());
        interview.setHourStart(LocalTime.now().plusMinutes(10));
        interview.setTokenInterview("token123");
        interview.setIdAffiliate("affiliateId");

        when(scheduleInterviewWebService.listScheduleInterviewWeb()).thenReturn(List.of(interview));

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setNumberIdentification("numId");
        affiliateMercantile.setTypeDocumentIdentification("typeId");
        affiliateMercantile.setFiledNumber("fileNum");

        Affiliate affiliate = new Affiliate();
        affiliate.setDocumentNumber("numId");

        UserMain userMain = new UserMain();

        when(affiliateMercantileRepository.findOne(Mockito.<Specification<AffiliateMercantile>>any()))
                .thenReturn(Optional.of(affiliateMercantile));
        when(iAffiliateRepository.findOne(Mockito.<Specification<Affiliate>>any())).thenReturn(Optional.of(affiliate));
        when(userPreRegisterRepository.findOne(Mockito.<Specification<UserMain>>any()))
                .thenReturn(Optional.of(userMain));

        doNothing().when(sendEmails).reminderInterviewWeb(any());

        service.sendNotifications();

        verify(messagingTemplate).convertAndSend(startsWith("/notificationInterviewWeb/"),
                contains("Tienes una reunion"));
        verify(sendEmails).reminderInterviewWeb(any(TemplateSendEmailsDTO.class));
    }

    @Test
    void testSendNotifications_affiliationNotFound_throwsAffiliationError() {
        DateInterviewWeb interview = new DateInterviewWeb();
        interview.setDay(LocalDate.now());
        interview.setHourStart(LocalTime.now().plusMinutes(10));
        interview.setTokenInterview("token123");
        interview.setIdAffiliate("affiliateId");

        when(scheduleInterviewWebService.listScheduleInterviewWeb()).thenReturn(List.of(interview));

        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.sendNotifications());
        verify(sendEmails, never()).reminderInterviewWeb(any());
    }

    @Test
    void testSendNotifications_userNotFound_throwsAffiliationError() {
        DateInterviewWeb interview = new DateInterviewWeb();
        interview.setDay(LocalDate.now());
        interview.setHourStart(LocalTime.now().plusMinutes(10));
        interview.setTokenInterview("token123");
        interview.setIdAffiliate("affiliateId");

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setNumberIdentification("numId");
        affiliateMercantile.setTypeDocumentIdentification("typeId");
        affiliateMercantile.setFiledNumber("fileNum");

        Affiliate affiliate = new Affiliate();
        affiliate.setDocumentNumber("numId");

        when(scheduleInterviewWebService.listScheduleInterviewWeb()).thenReturn(List.of(interview));
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliateMercantile));
        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.sendNotifications());
        verify(sendEmails, never()).reminderInterviewWeb(any());
    }

    @Test
    void testExpireTimeRegularizationAffiliation_suspendsAffiliationsAndMercantiles() {
        Affiliation affiliation = new Affiliation();
        affiliation.setFiledNumber("fileNum");
        List<Affiliation> affiliations = List.of(affiliation);

        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setFiledNumber("fileNum");
        List<AffiliateMercantile> mercantiles = List.of(mercantile);

        Affiliate affiliateToSuspend = new Affiliate();

        when(properties.getLimitUploadDocumentsRegularization()).thenReturn(1L);
        when(repositoryAffiliation.findAll(any(Specification.class))).thenReturn(affiliations);
        when(affiliateMercantileRepository.findAll(any(Specification.class))).thenReturn(mercantiles);
        when(iAffiliateRepository.findByFiledNumber("fileNum")).thenReturn(Optional.of(affiliateToSuspend));

        service.expireTimeRegularizationAffiliation();

        assertTrue(affiliateToSuspend.getAffiliationCancelled());
        verify(iAffiliateRepository, times(2)).save(affiliateToSuspend);
        verify(repositoryAffiliation).save(affiliation);
        verify(affiliateMercantileRepository).save(mercantile);
    }

    @Test
    void testSendNotification_returnsTrueForCorrectTimeWindow() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateNow = now.plusMinutes(10);

        java.lang.reflect.Method method = ScheduledTimersAffiliationsServiceImpl.class
                .getDeclaredMethod("sendNotification", LocalDateTime.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(service, dateNow);

        assertTrue(result);
    }

    @Test
    void testSendNotification_returnsFalseForOutsideTimeWindow() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateNow = now.plusMinutes(5);

        java.lang.reflect.Method method = ScheduledTimersAffiliationsServiceImpl.class
                .getDeclaredMethod("sendNotification", LocalDateTime.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(service, dateNow);

        assertFalse(result);
    }

    @Test
    void testDeleteRequestAffiliation_deletesOldAffiliationsAndMercantiles() {
        Affiliation affiliation = new Affiliation();
        affiliation.setDateRequest(LocalDateTime.now().minusDays(8).toString());

        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setDateCreateAffiliate(LocalDate.now().minusDays(8));

        when(repositoryAffiliation.findAll(any(Specification.class))).thenReturn(List.of(affiliation));
        when(affiliateMercantileRepository.findAll(any(Specification.class))).thenReturn(List.of(mercantile));

        service.deleteRequestAffiliation();

        verify(repositoryAffiliation).delete(affiliation);
        verify(affiliateMercantileRepository).delete(mercantile);
    }

    @Test
    void testUpdateRealNumberWorkers_forMercantileAffiliate() throws Exception {
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setFiledNumber("fileNumDependent");
        affiliate.setNitCompany("123");

        Affiliate affiliateMercantile = new Affiliate();
        affiliateMercantile.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        affiliateMercantile.setAffiliationSubType(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);
        affiliateMercantile.setFiledNumber("fileNumMercantile");
        affiliateMercantile.setDocumentNumber("123");

        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setRealNumberWorkers(10L);
        mercantile.setFiledNumber("fileNumMercantile");
        mercantile.setNumberIdentification("123");

        when(iAffiliateRepository.findAll(Mockito.<Specification<Affiliate>>any())).thenReturn(List.of(affiliateMercantile));
        when(affiliateMercantileRepository.findByFiledNumber("fileNumMercantile")).thenReturn(Optional.of(mercantile));
        when(affiliateService.getEmployerSize(9)).thenReturn(2L);

        java.lang.reflect.Method method = ScheduledTimersAffiliationsServiceImpl.class
                .getDeclaredMethod("updateRealNumberWorkers", Affiliate.class);
        method.setAccessible(true);
        method.invoke(service, affiliateMercantile);

        assertEquals(9L, mercantile.getRealNumberWorkers());
        verify(affiliateMercantileRepository).save(mercantile);
    }

    @Test
    void testUpdateRealNumberWorkers_forDomesticAffiliate() throws Exception {
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setFiledNumber("fileNumDependent");
        affiliate.setNitCompany("456");

        Affiliate affiliateDomestic = new Affiliate();
        affiliateDomestic.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        affiliateDomestic.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        affiliateDomestic.setFiledNumber("fileNumDomestic");
        affiliateDomestic.setDocumentNumber("456");

        Affiliation domesticAffiliation = new Affiliation();
        domesticAffiliation.setRealNumberWorkers(5L);
        domesticAffiliation.setFiledNumber("fileNumDomestic");
        domesticAffiliation.setIdentificationDocumentNumber("456");

        when(iAffiliateRepository.findAll(Mockito.<Specification<Affiliate>>any())).thenReturn(List.of(affiliateDomestic));
        when(repositoryAffiliation.findByFiledNumber("fileNumDomestic")).thenReturn(Optional.of(domesticAffiliation));
        when(affiliateService.getEmployerSize(4)).thenReturn(1L);

        java.lang.reflect.Method method = ScheduledTimersAffiliationsServiceImpl.class
                .getDeclaredMethod("updateRealNumberWorkers", Affiliate.class);
        method.setAccessible(true);
        method.invoke(service, affiliate);

        assertEquals(4L, domesticAffiliation.getRealNumberWorkers());
        verify(repositoryAffiliation).save(domesticAffiliation);
    }

    @Test
    void testUpdateRealNumberWorkers_forMercantileAffiliate_withNullWorkers() throws Exception {
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setFiledNumber("fileNumDependent");
        affiliate.setNitCompany("123");

        Affiliate affiliateMercantile = new Affiliate();
        affiliateMercantile.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        affiliateMercantile.setAffiliationSubType(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);
        affiliateMercantile.setFiledNumber("fileNumMercantile");
        affiliateMercantile.setDocumentNumber("123");

        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setRealNumberWorkers(null);
        mercantile.setFiledNumber("fileNumMercantile");
        mercantile.setNumberIdentification("123");

        when(iAffiliateRepository.findAll(Mockito.<Specification<Affiliate>>any())).thenReturn(List.of(affiliateMercantile));
        when(affiliateMercantileRepository.findByFiledNumber("fileNumMercantile")).thenReturn(Optional.of(mercantile));
        when(affiliateService.getEmployerSize(0)).thenReturn(1L);

        java.lang.reflect.Method method = ScheduledTimersAffiliationsServiceImpl.class
                .getDeclaredMethod("updateRealNumberWorkers", Affiliate.class);
        method.setAccessible(true);
        method.invoke(service, affiliate);

        assertEquals(0L, mercantile.getRealNumberWorkers());
        verify(affiliateMercantileRepository).save(mercantile);
    }

    @Test
    void testUpdateRealNumberWorkers_forDomesticAffiliate_withNullWorkers() throws Exception {
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setFiledNumber("fileNumDependent");
        affiliate.setNitCompany("456");

        Affiliate affiliateDomestic = new Affiliate();
        affiliateDomestic.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        affiliateDomestic.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        affiliateDomestic.setFiledNumber("fileNumDomestic");
        affiliateDomestic.setDocumentNumber("456");

        Affiliation domesticAffiliation = new Affiliation();
        domesticAffiliation.setRealNumberWorkers(null);
        domesticAffiliation.setFiledNumber("fileNumDomestic");
        domesticAffiliation.setIdentificationDocumentNumber("456");

        when(iAffiliateRepository.findAll(Mockito.<Specification<Affiliate>>any())).thenReturn(List.of(affiliateDomestic));
        when(repositoryAffiliation.findByFiledNumber("fileNumDomestic")).thenReturn(Optional.of(domesticAffiliation));
        when(affiliateService.getEmployerSize(0)).thenReturn(1L);

        java.lang.reflect.Method method = ScheduledTimersAffiliationsServiceImpl.class
                .getDeclaredMethod("updateRealNumberWorkers", Affiliate.class);
        method.setAccessible(true);
        method.invoke(service, affiliate);

        assertEquals(0L, domesticAffiliation.getRealNumberWorkers());
        verify(repositoryAffiliation).save(domesticAffiliation);
    }

    @Test
    void testSaveNoveltyRuaf_forDomesticEmployer() throws Exception {
        DataWorkerRetirementDTO data = new DataWorkerRetirementDTO();
        data.setIdentificationDocumentType("CC");
        data.setIdentificationDocumentNumber("12345");
        data.setFirstName("John");
        data.setSurname("Doe");
        data.setIdRetirementReason(1L);

        String nitEmployer = "98765";
        String filedNumber = "file123";

        com.gal.afiliaciones.domain.model.ArlInformation arlInfo = new com.gal.afiliaciones.domain.model.ArlInformation();
        arlInfo.setCode("ARL01");
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInfo));

        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        employerAffiliate.setFiledNumber(filedNumber);
        when(iAffiliateRepository.findAll(Mockito.<Specification<Affiliate>>any()))
                .thenReturn(List.of(employerAffiliate));

        Affiliation domesticAffiliation = new Affiliation();
        domesticAffiliation.setIdentificationDocumentType("NIT");
        when(repositoryAffiliation.findByFiledNumber(filedNumber)).thenReturn(Optional.of(domesticAffiliation));

        // doNothing().when(noveltyRuafService).createNovelty(any());

        java.lang.reflect.Method method = ScheduledTimersAffiliationsServiceImpl.class
                .getDeclaredMethod("saveNoveltyRuaf", DataWorkerRetirementDTO.class, Affiliate.class);
        method.setAccessible(true);
        method.invoke(service, data, employerAffiliate);

        verify(noveltyRuafService).createNovelty(any());
    }

    @Test
    void testSaveNoveltyRuaf_forMercantileEmployer() throws Exception {
        DataWorkerRetirementDTO data = new DataWorkerRetirementDTO();
        data.setIdentificationDocumentType("CC");
        data.setIdentificationDocumentNumber("54321");
        data.setFirstName("Jane");
        data.setSurname("Smith");
        data.setIdRetirementReason(2L); // Causal for death

        String nitEmployer = "112233";
        String filedNumber = "file456";

        com.gal.afiliaciones.domain.model.ArlInformation arlInfo = new com.gal.afiliaciones.domain.model.ArlInformation();
        arlInfo.setCode("ARL02");
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInfo));

        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setAffiliationSubType(Constant.TYPE_AFFILLATE_EMPLOYER);
        employerAffiliate.setFiledNumber(filedNumber);
        when(iAffiliateRepository.findAll(Mockito.<Specification<Affiliate>>any()))
                .thenReturn(List.of(employerAffiliate));

        AffiliateMercantile mercantileAffiliation = new AffiliateMercantile();
        mercantileAffiliation.setTypeDocumentIdentification("NIT");
        mercantileAffiliation.setDigitVerificationDV(5);
        when(affiliateMercantileRepository.findByFiledNumber(filedNumber))
                .thenReturn(Optional.of(mercantileAffiliation));

        // doNothing().when(noveltyRuafService).createNovelty(any());

        java.lang.reflect.Method method = ScheduledTimersAffiliationsServiceImpl.class
                .getDeclaredMethod("saveNoveltyRuaf", DataWorkerRetirementDTO.class, Affiliate.class);
        method.setAccessible(true);
        method.invoke(service, data, employerAffiliate);

        verify(noveltyRuafService).createNovelty(any());
    }

    @Test
    void testFindDataContributor_forDomesticEmployer() throws Exception {
        String nitEmployer = "123456789";
        String filedNumber = "fileDomestic";

        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        employerAffiliate.setFiledNumber(filedNumber);

        Affiliation domesticAffiliation = new Affiliation();
        domesticAffiliation.setIdentificationDocumentType("CC");

        when(iAffiliateRepository.findAll(Mockito.<Specification<Affiliate>>any()))
                .thenReturn(List.of(employerAffiliate));
        when(repositoryAffiliation.findByFiledNumber(filedNumber)).thenReturn(Optional.of(domesticAffiliation));

        java.lang.reflect.Method method = ScheduledTimersAffiliationsServiceImpl.class
                .getDeclaredMethod("findDataContributor", String.class);
        method.setAccessible(true);
        DataContributorDTO result = (DataContributorDTO) method.invoke(service, nitEmployer);

        assertEquals("CC", result.getIdentificationType());
        assertEquals(nitEmployer, result.getIdentificationNumber());
        verify(affiliateMercantileRepository, never()).findByFiledNumber(any());
    }

    @Test
    void testFindDataContributor_forMercantileEmployer() throws Exception {
        String nitEmployer = "987654321";
        String filedNumber = "fileMercantile";

        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setAffiliationSubType(Constant.TYPE_AFFILLATE_EMPLOYER);
        employerAffiliate.setFiledNumber(filedNumber);

        AffiliateMercantile mercantileAffiliation = new AffiliateMercantile();
        mercantileAffiliation.setTypeDocumentIdentification("NIT");
        mercantileAffiliation.setDigitVerificationDV(5);

        when(iAffiliateRepository.findAll(Mockito.<Specification<Affiliate>>any()))
                .thenReturn(List.of(employerAffiliate));
        when(affiliateMercantileRepository.findByFiledNumber(filedNumber))
                .thenReturn(Optional.of(mercantileAffiliation));

        java.lang.reflect.Method method = ScheduledTimersAffiliationsServiceImpl.class
                .getDeclaredMethod("findDataContributor", String.class);
        method.setAccessible(true);
        DataContributorDTO result = (DataContributorDTO) method.invoke(service, nitEmployer);

        assertEquals("NIT", result.getIdentificationType());
        assertEquals(5, result.getDv());
        assertEquals(nitEmployer, result.getIdentificationNumber());
        verify(repositoryAffiliation, never()).findByFiledNumber(any());
    }

    @Test
    void testDeleteAffiliationMercantile_affiliateFound() throws Exception {
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setId(1L);

        Affiliate affiliate = new Affiliate();

        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        java.lang.reflect.Method method = ScheduledTimersAffiliationsServiceImpl.class
                .getDeclaredMethod("deleteAffiliationMercantile", AffiliateMercantile.class);
        method.setAccessible(true);
        method.invoke(service, affiliateMercantile);

        assertTrue(affiliateMercantile.getAffiliationCancelled());
        verify(affiliateMercantileRepository).save(affiliateMercantile);

        assertTrue(affiliate.getAffiliationCancelled());
        verify(iAffiliateRepository).save(affiliate);
    }

    @Test
    void testDeleteAffiliationMercantile_affiliateNotFound() throws Exception {
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setId(1L);

        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        java.lang.reflect.Method method = ScheduledTimersAffiliationsServiceImpl.class
                .getDeclaredMethod("deleteAffiliationMercantile", AffiliateMercantile.class);
        method.setAccessible(true);
        method.invoke(service, affiliateMercantile);

        assertTrue(affiliateMercantile.getAffiliationCancelled());
        verify(affiliateMercantileRepository).save(affiliateMercantile);
        verify(iAffiliateRepository, never()).save(any(Affiliate.class));
    }

    @Test
    void testDeleteAffiliation_affiliateFound() throws Exception {
        Affiliation affiliation = new Affiliation();
        affiliation.setId(1L);

        Affiliate affiliateToCancel = new Affiliate();

        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateToCancel));

        java.lang.reflect.Method method = ScheduledTimersAffiliationsServiceImpl.class
                .getDeclaredMethod("deleteAffiliation", Affiliation.class);
        method.setAccessible(true);
        method.invoke(service, affiliation);

        verify(repositoryAffiliation).save(affiliation);
        assertTrue(affiliateToCancel.getAffiliationCancelled());
        verify(iAffiliateRepository).save(affiliateToCancel);
    }

    @Test
    void testDeleteAffiliation_affiliateNotFound() throws Exception {
        Affiliation affiliation = new Affiliation();
        affiliation.setId(1L);

        when(iAffiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        java.lang.reflect.Method method = ScheduledTimersAffiliationsServiceImpl.class
                .getDeclaredMethod("deleteAffiliation", Affiliation.class);
        method.setAccessible(true);
        method.invoke(service, affiliation);

        verify(repositoryAffiliation).save(affiliation);
        verify(iAffiliateRepository, never()).save(any(Affiliate.class));
    }

    @Test
    void testUpdateAffiliationIndependent_whenAffiliationIsIndependent() throws Exception {
        String filedNumber = "independent123";
        Affiliation affiliation = new Affiliation();
        affiliation.setFiledNumber(filedNumber);
        affiliation.setTypeAffiliation(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliation.setIdentificationDocumentType("CC");
        affiliation.setIdentificationDocumentNumber("102030");
        affiliation.setFirstName("Test");
        affiliation.setSurname("User");

        when(repositoryAffiliation.findByFiledNumber(filedNumber)).thenReturn(Optional.of(affiliation));

        java.lang.reflect.Method method = ScheduledTimersAffiliationsServiceImpl.class
                .getDeclaredMethod("updateAffiliationIndependent", String.class);
        method.setAccessible(true);

        DataWorkerRetirementDTO result = (DataWorkerRetirementDTO) method.invoke(service, filedNumber);

        verify(repositoryAffiliation).save(affiliation);
        assertEquals(Constant.NOVELTY_TYPE_RETIREMENT, affiliation.getStageManagement());
        assertEquals("CC", result.getIdentificationDocumentType());
        assertEquals("102030", result.getIdentificationDocumentNumber());
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getSurname());
    }

    @Test
    void testUpdateAffiliationIndependent_whenAffiliationIsNotIndependent() throws Exception {
        String filedNumber = "domestic456";
        Affiliation affiliation = new Affiliation();
        affiliation.setFiledNumber(filedNumber);
        affiliation.setTypeAffiliation(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);

        when(repositoryAffiliation.findByFiledNumber(filedNumber)).thenReturn(Optional.of(affiliation));

        java.lang.reflect.Method method = ScheduledTimersAffiliationsServiceImpl.class
                .getDeclaredMethod("updateAffiliationIndependent", String.class);
        method.setAccessible(true);

        DataWorkerRetirementDTO result = (DataWorkerRetirementDTO) method.invoke(service, filedNumber);

        verify(repositoryAffiliation).save(affiliation);
        assertEquals(Constant.NOVELTY_TYPE_RETIREMENT, affiliation.getStageManagement());
        // The DTO should be empty as mapperIndependentData is not called for this type
        assertEquals(null, result.getIdentificationDocumentNumber());
        assertEquals(null, result.getFirstName());
    }

    @Test
    void testUpdateAffiliationIndependent_throwsAffiliationNotFoundError() throws Exception {
        String filedNumber = "notFound789";
        when(repositoryAffiliation.findByFiledNumber(filedNumber)).thenReturn(Optional.empty());

        java.lang.reflect.Method method = ScheduledTimersAffiliationsServiceImpl.class
                .getDeclaredMethod("updateAffiliationIndependent", String.class);
        method.setAccessible(true);

        Exception exception = assertThrows(Exception.class, () -> {
            try {
                method.invoke(service, filedNumber);
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw (Exception) e.getTargetException();
            }
        });

        assertTrue(exception instanceof com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError);
        verify(repositoryAffiliation, never()).save(any(Affiliation.class));
    }

    @Test
    void testRetirement_forMercantileEmployee() {
        Retirement retirement = new Retirement();
        retirement.setIdAffiliate(1L);
        retirement.setRetirementDate(LocalDate.now());
        retirement.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        retirement.setIdRetirementReason(1L);

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(1L);
        affiliate.setFiledNumber("fileMercantile");
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        affiliate.setNitCompany("12345");

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();

        when(retirementRepository.findAll()).thenReturn(List.of(retirement));
        when(iAffiliateRepository.findByIdAffiliate(1L)).thenReturn(Optional.of(affiliate));
        when(iAffiliateRepository.save(any(Affiliate.class))).thenReturn(affiliate);
        when(affiliateMercantileRepository.findByFiledNumber("fileMercantile"))
                .thenReturn(Optional.of(affiliateMercantile));
        when(arlInformationDao.findAllArlInformation())
                .thenReturn(List.of(new com.gal.afiliaciones.domain.model.ArlInformation()));
        when(iAffiliateRepository.findAll(any(Specification.class))).thenReturn(List.of());

        RetirementReasonWorker reason = new RetirementReasonWorker();
        reason.setReason("Motivo de retiro");
        when(retirementReasonWorkerRepository.findById(1L)).thenReturn(Optional.of(reason));

        RetirementReason retirementReason = new RetirementReason();
        retirementReason.setReason("Motivo de retiro empleador");
        when(retirementReasonRepository.findById(1L)).thenReturn(Optional.of(retirementReason));

        // 👇 Mock obligatorio
        doNothing().when(generalNoveltyService).saveGeneralNovelty(any(SaveGeneralNoveltyRequest.class));

        service.retirement();

        verify(iAffiliateRepository).save(affiliate);
        assertTrue(affiliate.getAffiliationCancelled());

        verify(affiliateMercantileRepository).save(affiliateMercantile);
        assertEquals(Constant.NOVELTY_TYPE_RETIREMENT, affiliateMercantile.getStageManagement());
        assertEquals(Constant.AFFILIATION_STATUS_INACTIVE, affiliateMercantile.getAffiliationStatus());
    }

    private Affiliate updateRealNumberWorkers(Affiliate affiliateWorker) {
        Specification<Affiliate> spcEmployer = AffiliateSpecification.findByNitEmployer(affiliateWorker.getNitCompany());
        List<Affiliate> affiliateEmployer = iAffiliateRepository.findAll(spcEmployer);
        if(!affiliateEmployer.isEmpty()) {
            Affiliate affiliate = affiliateEmployer.get(0);
            if (affiliate.getAffiliationSubType().equals(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE)) {
                AffiliateMercantile affiliationMercantile = affiliateMercantileRepository
                        .findByFiledNumber(affiliate.getFiledNumber()).orElse(null);
                if (affiliationMercantile != null) {
                    Long realNumWorkers = affiliationMercantile.getRealNumberWorkers() != null
                            ? affiliationMercantile.getRealNumberWorkers() - 1L
                            : 0L;
                    affiliationMercantile.setRealNumberWorkers(realNumWorkers);
                    affiliationMercantile.setIdEmployerSize(affiliateService.getEmployerSize(realNumWorkers.intValue()));
                    affiliateMercantileRepository.save(affiliationMercantile);
                }
            } else {
                Affiliation affiliationDom = repositoryAffiliation.findByFiledNumber(affiliate.getFiledNumber())
                        .orElseThrow(() -> new com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError(
                                com.gal.afiliaciones.config.ex.Error.Type.AFFILIATION_NOT_FOUND));

                Long realNumWorkers = affiliationDom.getRealNumberWorkers() != null
                        ? affiliationDom.getRealNumberWorkers() - 1L
                        : 0L;
                affiliationDom.setRealNumberWorkers(realNumWorkers);
                affiliationDom.setIdEmployerSize(affiliateService.getEmployerSize(realNumWorkers.intValue()));
                repositoryAffiliation.save(affiliationDom);
            }
        }
        return affiliateWorker;
    }

    @Test
    void testRetirement_noRetirementsForToday() {
        Retirement retirement = new Retirement();
        retirement.setRetirementDate(LocalDate.now().minusDays(1));

        when(retirementRepository.findAll()).thenReturn(List.of(retirement));

        service.retirement();

        verify(iAffiliateRepository, never()).findByIdAffiliate(any());
        verify(iAffiliateRepository, never()).save(any());
        verify(noveltyRuafService, never()).createNovelty(any());
    }

    @Test
    void testUpdateRealNumberWorkers_forDomesticAffiliate_withCorrectSubType() throws Exception {
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setFiledNumber("fileNumDependent");
        affiliate.setNitCompany("456");

        Affiliate affiliateDomestic = new Affiliate();
        affiliateDomestic.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        affiliateDomestic.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        affiliateDomestic.setFiledNumber("fileNumDomestic");
        affiliateDomestic.setDocumentNumber("456");

        Affiliation domesticAffiliation = new Affiliation();
        domesticAffiliation.setRealNumberWorkers(5L);
        domesticAffiliation.setFiledNumber("fileNumDomestic");
        domesticAffiliation.setIdentificationDocumentNumber("456");

        when(iAffiliateRepository.findAll(Mockito.<Specification<Affiliate>>any())).thenReturn(List.of(affiliateDomestic));
        when(repositoryAffiliation.findByFiledNumber("fileNumDomestic")).thenReturn(Optional.of(domesticAffiliation));
        when(affiliateService.getEmployerSize(4)).thenReturn(1L);

        java.lang.reflect.Method method = ScheduledTimersAffiliationsServiceImpl.class
                .getDeclaredMethod("updateRealNumberWorkers", Affiliate.class);
        method.setAccessible(true);
        method.invoke(service, affiliate);

        assertEquals(4L, domesticAffiliation.getRealNumberWorkers());
        assertEquals(1L, domesticAffiliation.getIdEmployerSize());
        verify(repositoryAffiliation).save(domesticAffiliation);
        verify(affiliateMercantileRepository, never()).findByFiledNumber(any());
    }

    @Test
    void testUpdateRealNumberWorkers_forDomesticAffiliate_withNullWorkers_withCorrectSubType() throws Exception {
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setFiledNumber("fileNumDependent");
        affiliate.setNitCompany("456");

        Affiliate affiliateDomestic = new Affiliate();
        affiliateDomestic.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        affiliateDomestic.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        affiliateDomestic.setFiledNumber("fileNumDomestic");
        affiliateDomestic.setDocumentNumber("456");

        Affiliation domesticAffiliation = new Affiliation();
        domesticAffiliation.setRealNumberWorkers(null);
        domesticAffiliation.setFiledNumber("fileNumDomestic");
        domesticAffiliation.setIdentificationDocumentNumber("456");

        when(iAffiliateRepository.findAll(Mockito.<Specification<Affiliate>>any())).thenReturn(List.of(affiliateDomestic));
        when(repositoryAffiliation.findByFiledNumber("fileNumDomestic")).thenReturn(Optional.of(domesticAffiliation));
        when(affiliateService.getEmployerSize(0)).thenReturn(1L);

        java.lang.reflect.Method method = ScheduledTimersAffiliationsServiceImpl.class
                .getDeclaredMethod("updateRealNumberWorkers", Affiliate.class);
        method.setAccessible(true);
        method.invoke(service, affiliate);

        assertEquals(0L, domesticAffiliation.getRealNumberWorkers());
        assertEquals(1L, domesticAffiliation.getIdEmployerSize());
        verify(repositoryAffiliation).save(domesticAffiliation);
    }

    @Test
    void testUpdateRealNumberWorkers_forMercantileAffiliate_notFound() throws Exception {
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setFiledNumber("fileNumDependent");
        affiliate.setNitCompany("123");

        Affiliate affiliateMercantile = new Affiliate();
        affiliateMercantile.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        affiliateMercantile.setAffiliationSubType(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);
        affiliateMercantile.setFiledNumber("fileNumMercantileNotFound");
        affiliateMercantile.setDocumentNumber("123");

        when(iAffiliateRepository.findAll(Mockito.<Specification<Affiliate>>any())).thenReturn(List.of(affiliateMercantile));
        when(affiliateMercantileRepository.findByFiledNumber("fileNumMercantileNotFound")).thenReturn(Optional.empty());

        java.lang.reflect.Method method = ScheduledTimersAffiliationsServiceImpl.class
                .getDeclaredMethod("updateRealNumberWorkers", Affiliate.class);
        method.setAccessible(true);
        method.invoke(service, affiliate);

        verify(affiliateMercantileRepository, never()).save(any());
    }

    @Test
    void testUpdateRealNumberWorkers_forDomesticAffiliate_notFound_throwsException() throws Exception {
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setFiledNumber("fileNumDependent");
        affiliate.setNitCompany("456");

        Affiliate affiliateDomestic = new Affiliate();
        affiliateDomestic.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        affiliateDomestic.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        affiliateDomestic.setFiledNumber("fileNumDomesticNotFound");
        affiliateDomestic.setDocumentNumber("456");

        when(iAffiliateRepository.findAll(Mockito.<Specification<Affiliate>>any())).thenReturn(List.of(affiliateDomestic));
        when(repositoryAffiliation.findByFiledNumber("fileNumDomesticNotFound")).thenReturn(Optional.empty());

        java.lang.reflect.Method method = ScheduledTimersAffiliationsServiceImpl.class
                .getDeclaredMethod("updateRealNumberWorkers", Affiliate.class);
        method.setAccessible(true);

        Exception exception = assertThrows(Exception.class, () -> {
            method.invoke(service, affiliate);
        });

        assertTrue(exception.getCause() instanceof AffiliationNotFoundError);
        verify(repositoryAffiliation, never()).save(any());
    }

}
