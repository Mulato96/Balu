package com.gal.afiliaciones.application.service.affiliate.impl;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gal.afiliaciones.application.service.CertificateBulkService;
import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliate.ScheduleInterviewWebService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.generalnovelty.impl.GeneralNoveltyServiceImpl;
import com.gal.afiliaciones.application.service.novelty.NoveltyRuafService;

import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.AffiliationCancellationTimer;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationCancellationTimerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.retirement.RetirementRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirementreason.RetirementReasonRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirementreason.RetirementReasonWorkerRepository;

import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = { ScheduledTimersAffiliationsServiceImpl.class })
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class ScheduledTimersAffiliationsServiceImplDiffblueTest {

    @MockBean
    private AffiliateMercantileRepository affiliateMercantileRepository;

    @MockBean
    private AffiliateRepository iAffiliateRepository;

    @MockBean
    private IAffiliationCancellationTimerRepository iAffiliationCancellationTimerRepository;

    @MockBean
    private IAffiliationEmployerDomesticServiceIndependentRepository iAffiliationEmployerDomesticServiceIndependentRepository;

    @MockBean
    private SendEmails sendEmails;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @MockBean
    private ScheduleInterviewWebService scheduleInterviewWebService;

    @MockBean
    private RetirementRepository retirementRepository;

    @MockBean
    private IUserPreRegisterRepository userPreRegisterRepository;

    @MockBean
    private CollectProperties properties;

    @MockBean
    private AffiliateService affiliateService;

    @MockBean
    private ArlInformationDao arlInformationDao;

    @MockBean
    private NoveltyRuafService noveltyRuafService;

    @MockBean
    private AffiliationDependentRepository affiliationDependentRepository;

    @MockBean
    private GeneralNoveltyServiceImpl generalNoveltyService;

    @MockBean
    private RetirementReasonWorkerRepository retirementReasonWorkerRepository;

    @MockBean
    private RetirementReasonRepository retirementReasonRepository;

    @Autowired
    private ScheduledTimersAffiliationsServiceImpl scheduledTimersAffiliationsServiceImpl;

    @MockBean
    private CertificateBulkService certificateService;


    @Test
    void testTimers() {
        // Arrange
        when(iAffiliationCancellationTimerRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        scheduledTimersAffiliationsServiceImpl.timers();

        // Assert
        verify(iAffiliationCancellationTimerRepository).findAll();
    }

    @Test
    void testTimers2() {
        // Arrange
        AffiliationCancellationTimer timer = new AffiliationCancellationTimer();
        timer.setDateStart(LocalDate.of(1970, 1, 1).atStartOfDay());
        timer.setId(1L);
        timer.setNumberDocument("42");
        timer.setType('A');
        timer.setTypeDocument("Type Document");

        ArrayList<AffiliationCancellationTimer> timers = new ArrayList<>();
        timers.add(timer);

        when(iAffiliationCancellationTimerRepository.findAll()).thenReturn(timers);

        // Act
        scheduledTimersAffiliationsServiceImpl.timers();

        // Assert
        verify(iAffiliationCancellationTimerRepository).findAll();
    }
}
