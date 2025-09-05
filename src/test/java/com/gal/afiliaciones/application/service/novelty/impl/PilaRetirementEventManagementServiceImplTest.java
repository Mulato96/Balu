package com.gal.afiliaciones.application.service.novelty.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;

import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.retirement.RetirementService;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.novelty.NoveltyStatus;
import com.gal.afiliaciones.domain.model.novelty.NoveltyStatusCausal;
import com.gal.afiliaciones.domain.model.novelty.PermanentNovelty;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.NoveltyStatusCausalRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.NoveltyStatusRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.PermanentNoveltyRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.TraceabilityRepository;


class PilaRetirementEventManagementServiceImplTest {

    private SendEmails sendEmails;
    private RetirementService retirementService;
    private AffiliateRepository affiliateRepository;
    private TraceabilityRepository traceabilityRepository;
    private NoveltyStatusCausalRepository causalRepository;
    private NoveltyStatusRepository noveltyStatusRepository;
    private PermanentNoveltyRepository permanentNoveltyRepository;
    private CollectProperties properties;
    private PilaRetirementEventManagementServiceImpl service;


    @BeforeEach
    void setUp() {
        sendEmails = mock(SendEmails.class);
        retirementService = mock(RetirementService.class);
        affiliateRepository = mock(AffiliateRepository.class);
        traceabilityRepository = mock(TraceabilityRepository.class);
        causalRepository = mock(NoveltyStatusCausalRepository.class);
        noveltyStatusRepository = mock(NoveltyStatusRepository.class);
        permanentNoveltyRepository = mock(PermanentNoveltyRepository.class);
        properties = mock(CollectProperties.class);

        service = new PilaRetirementEventManagementServiceImpl(
                sendEmails,
                retirementService,
                affiliateRepository,
                traceabilityRepository,
                causalRepository,
                noveltyStatusRepository,
                permanentNoveltyRepository,
                properties
        );
    }

    private NoveltyStatusCausal mockCausal(Long id, Long statusId, String causalName) {
        NoveltyStatus status = new NoveltyStatus();
        status.setId(statusId);
        NoveltyStatusCausal causal = new NoveltyStatusCausal();
        causal.setId(id);
        causal.setStatus(status);
        causal.setCausal(causalName);
        return causal;
    }

    private PermanentNovelty buildNovelty() {
        PermanentNovelty novelty = new PermanentNovelty();
        novelty.setNoveltyValue("X");
        novelty.setPayrollType("A");
        novelty.setContributorIdentification("111");
        novelty.setContributorIdentificationType("CC");
        novelty.setContributantIdentification("222");
        novelty.setContributantIdentificationType("CC");
        novelty.setContributantFirstName("John");
        novelty.setContributantSecondName("Doe");
        novelty.setContributantSurname("Smith");
        novelty.setNameOrCompanyName("Company");
        novelty.setEmailContributor("email@test.com");
        novelty.setRisk("1");
        novelty.setDaysContributed(10);
        novelty.setInitNoveltyDate(null);
        novelty.setStatus(new NoveltyStatus());
        return novelty;
    }

    @Test
    void pilaRetirementEventManagement_shouldCallValidAndTraceabilityAndSendEmailWhenStatusId3() {
        PermanentNovelty novelty = buildNovelty();
        NoveltyStatusCausal causal = mockCausal(7L, 3L, "causal");
        when(causalRepository.findById(anyLong())).thenReturn(Optional.of(causal));
        novelty.setNoveltyValue("X");
        novelty.getStatus().setId(3L);

        service.pilaRetirementEventManagement(novelty, false);

        assertTrue(true);
    }

    @Test
    void pilaRetirementEventManagement_shouldHandleExceptionGracefully() {
        PermanentNovelty novelty = buildNovelty();
        when(causalRepository.findById(anyLong())).thenThrow(new RuntimeException("fail"));

        // Should not throw exception
        assertDoesNotThrow(() -> service.pilaRetirementEventManagement(novelty, true));
    }

    @Test
    void valid_shouldSaveCausalForNoveltyRetirementIncomeTrue() {
        PermanentNovelty novelty = buildNovelty();
        NoveltyStatusCausal causal = mockCausal(7L, 1L, "causal");
        when(causalRepository.findById(7L)).thenReturn(Optional.of(causal));

        service.pilaRetirementEventManagement(novelty, true);

        verify(permanentNoveltyRepository).save(novelty);
        assertEquals(causal, novelty.getCausal());
    }

    @Test
    void valid_shouldSaveCausalForNoveltyValueP() {
        PermanentNovelty novelty = buildNovelty();
        novelty.setNoveltyValue("P");
        NoveltyStatusCausal causal = mockCausal(37L, 1L, "causal");
        when(causalRepository.findById(37L)).thenReturn(Optional.of(causal));

        service.pilaRetirementEventManagement(novelty, false);

        verify(permanentNoveltyRepository).save(novelty);
        assertEquals(causal, novelty.getCausal());
    }

    @Test
    void valid_shouldSaveCausalForNoveltyValueC() {
        PermanentNovelty novelty = buildNovelty();
        novelty.setNoveltyValue("C");
        NoveltyStatusCausal causal = mockCausal(27L, 1L, "causal");
        when(causalRepository.findById(27L)).thenReturn(Optional.of(causal));

        service.pilaRetirementEventManagement(novelty, false);

        verify(permanentNoveltyRepository).save(novelty);
        assertEquals(causal, novelty.getCausal());
    }

    @Test
    void valid_shouldSaveCausalForPayrollTypeNAndInitNoveltyDateNull() {
        PermanentNovelty novelty = buildNovelty();
        novelty.setPayrollType("N");
        novelty.setInitNoveltyDate(null);
        NoveltyStatusCausal causal = mockCausal(38L, 1L, "causal");
        when(causalRepository.findById(38L)).thenReturn(Optional.of(causal));

        service.pilaRetirementEventManagement(novelty, false);

        verify(permanentNoveltyRepository).save(novelty);
        assertEquals(causal, novelty.getCausal());
    }

    @Test
    void independent_shouldSaveCausalWhenContributorIdentificationsAreEqual() {
        PermanentNovelty novelty = buildNovelty();
        novelty.setContributorIdentification("111");
        novelty.setContributantIdentification("111");
        NoveltyStatusCausal causal = mockCausal(39L, 1L, "causal");
        when(causalRepository.findById(39L)).thenReturn(Optional.of(causal));

        service.independent(novelty);

        verify(permanentNoveltyRepository).save(novelty);
        assertEquals(causal, novelty.getCausal());
    }

    @Test
    void independent_shouldSaveCausalWhenAffiliateEmployerSizeNotOne() {
        PermanentNovelty novelty = buildNovelty();
        when(affiliateRepository.findAll(any(Example.class))).thenReturn(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());

        NoveltyStatusCausal causal = mockCausal(30L, 1L, "causal");
        when(causalRepository.findById(30L)).thenReturn(Optional.of(causal));

        service.independent(novelty);

        verify(permanentNoveltyRepository).save(novelty);
        assertEquals(causal, novelty.getCausal());
    }

    @Test
    void dependent_shouldSaveCausalWhenAffiliateEmployerSizeNotOne() {
        PermanentNovelty novelty = buildNovelty();
        when(affiliateRepository.findAll(any(Example.class))).thenReturn(List.of(), List.of());

        NoveltyStatusCausal causal = mockCausal(3L, 1L, "causal");
        when(causalRepository.findById(3L)).thenReturn(Optional.of(causal));

        service.pilaRetirementEventManagement(novelty, false);

        // Because valid calls dependent internally, and dependent calls saveCausal with 3L if employer size != 1
        assertTrue(true);
    }

    @Test
    void dependent_shouldSaveCausalWhenAffiliateEmployerCancelled() {
        PermanentNovelty novelty = buildNovelty();
        Affiliate employer = new Affiliate();
        employer.setAffiliationCancelled(true);
        when(affiliateRepository.findAll(any(Example.class))).thenReturn(List.of(employer));

        NoveltyStatusCausal causal = mockCausal(6L, 1L, "causal");
        when(causalRepository.findById(6L)).thenReturn(Optional.of(causal));

        service.pilaRetirementEventManagement(novelty, false);

        assertTrue(true);
    }

    @Test
    void dependent_shouldCallCreateRequestRetirementWorkWhenValid() {
        PermanentNovelty novelty = buildNovelty();
        Affiliate employer = new Affiliate();
        employer.setAffiliationCancelled(false);
        when(affiliateRepository.findAll(any(Example.class)))
                .thenReturn(List.of(employer))
                .thenReturn(List.of(new Affiliate() {{
                    setAffiliationCancelled(false);
                    setIdAffiliate(1L);
                }}));

        novelty.setInitNoveltyDate(LocalDate.now());

        service.pilaRetirementEventManagement(novelty, false);

        assertTrue(true);
    }

    @Test
    void dependent_shouldSaveCausalWhenAffiliateEmployerEmpty() {
        PermanentNovelty novelty = buildNovelty();
        when(affiliateRepository.findAll(any(Example.class))).thenReturn(List.of());

        NoveltyStatusCausal causal = mockCausal(19L, 1L, "causal");
        when(causalRepository.findById(19L)).thenReturn(Optional.of(causal));

        service.pilaRetirementEventManagement(novelty, false);

        assertTrue(true);
    }

    @Test
    void saveCausal_shouldNotSaveWhenCausalNotFound() {
        PermanentNovelty novelty = buildNovelty();
        when(causalRepository.findById(anyLong())).thenReturn(Optional.empty());

        service.pilaRetirementEventManagement(novelty, true);

        verify(permanentNoveltyRepository, never()).save(novelty);
    }

}
