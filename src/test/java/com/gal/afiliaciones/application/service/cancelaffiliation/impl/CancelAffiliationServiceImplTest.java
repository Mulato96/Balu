package com.gal.afiliaciones.application.service.cancelaffiliation.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.application.service.generalnovelty.impl.GeneralNoveltyServiceImpl;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.cancelaffiliation.CancelAffiliationNotFoundException;
import com.gal.afiliaciones.config.ex.cancelaffiliation.DateCancelAffiliationException;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dto.cancelaffiliate.CancelAffiliateDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

class CancelAffiliationServiceImplTest {

    @Mock
    private AffiliateRepository affiliateRepository;
    @Mock
    private IUserPreRegisterRepository userMainRepository;
    @Mock
    private AffiliateMercantileRepository affiliateMercantileRepository;
    @Mock
    private AffiliationDependentRepository affiliationDependentRepository;
    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository domesticServiceIndependentRepository;
    @Mock
    private GeneralNoveltyServiceImpl generalNoveltyServiceImpl;

    @InjectMocks
    private CancelAffiliationServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void consultAffiliation_success() {
        String documentType = "CC";
        String documentNumber = "123";
        Long idUser = 1L;
        String subType = "SUB";
        String filedNumber = "F123";
        String nitCompany = "NIT123";
        String affiliationType = "TYPE1";

        AffiliationDependent dependent = new AffiliationDependent();
        dependent.setFiledNumber(filedNumber);
        dependent.setCoverageDate(LocalDate.now());

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(Boolean.FALSE);
        affiliate.setAffiliationStatus("ACTIVE");
        affiliate.setNitCompany(nitCompany);
        affiliate.setAffiliationType(affiliationType);

        UserMain user = new UserMain();
        user.setIdentification("123");

        when(affiliationDependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(dependent));
        when(affiliateRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliate));
        when(userMainRepository.findById(idUser)).thenReturn(Optional.of(user));
        when(affiliateRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliate));

        CancelAffiliateDTO dto = service.consultAffiliation(documentType, documentNumber, idUser, subType);

        assertNotNull(dto);
        assertEquals(affiliationType, dto.getContractType());
    }

    @Test
    void consultAffiliation_affiliationNotFound_dependent() {
        when(affiliationDependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.empty());

        assertThrows(CancelAffiliationNotFoundException.class,
                () -> service.consultAffiliation("CC", "123", 1L, "SUB"));
    }

    @Test
    void consultAffiliation_affiliationNotFound_affiliate() {
        AffiliationDependent dependent = new AffiliationDependent();
        dependent.setFiledNumber("F123");
        dependent.setCoverageDate(LocalDate.now());

        when(affiliationDependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(dependent));
        when(affiliateRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.empty());

        assertThrows(CancelAffiliationNotFoundException.class,
                () -> service.consultAffiliation("CC", "123", 1L, "SUB"));
    }

    @Test
    void consultAffiliation_affiliationAlreadyCancelled() {
        AffiliationDependent dependent = new AffiliationDependent();
        dependent.setFiledNumber("F123");
        dependent.setCoverageDate(LocalDate.now());

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(Boolean.TRUE);
        affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_INACTIVE);

        when(affiliationDependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(dependent));
        when(affiliateRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliate));

        assertThrows(CancelAffiliationNotFoundException.class,
                () -> service.consultAffiliation("CC", "123", 1L, "SUB"));
    }

    @Test
    void consultAffiliation_coverageDateTooOld() {
        AffiliationDependent dependent = new AffiliationDependent();
        dependent.setFiledNumber("F123");
        dependent.setCoverageDate(LocalDate.now().minusDays(2));

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(Boolean.FALSE);
        affiliate.setAffiliationStatus("ACTIVE");

        when(affiliationDependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(dependent));
        when(affiliateRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliate));

        assertThrows(DateCancelAffiliationException.class, () -> service.consultAffiliation("CC", "123", 1L, "SUB"));
    }

    @Test
    void consultAffiliation_invalidWorkedCompany() {
        AffiliationDependent dependent = new AffiliationDependent();
        dependent.setFiledNumber("F123");
        dependent.setCoverageDate(LocalDate.now());

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(Boolean.FALSE);
        affiliate.setAffiliationStatus("ACTIVE");
        affiliate.setNitCompany("NIT1");

        UserMain user = new UserMain();
        user.setIdentification("123");

        Affiliate affiliate2 = new Affiliate();
        affiliate2.setNitCompany("NIT2");

        when(affiliationDependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(dependent));
        when(affiliateRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliate))
                .thenReturn(Optional.of(affiliate2));
        when(userMainRepository.findById(anyLong())).thenReturn(Optional.of(user));

        assertThrows(DateCancelAffiliationException.class, () -> service.consultAffiliation("CC", "123", 1L, "SUB"));
    }

    @Test
    void updateStatusCanceledAffiliate_success() {
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(Boolean.FALSE);
        affiliate.setAffiliationStatus("ACTIVE");

        List<Affiliate> affiliates = Collections.singletonList(affiliate);
        Page<Affiliate> page = new PageImpl<>(affiliates);

        when(affiliateRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(affiliate);

        doNothing().when(generalNoveltyServiceImpl).saveGeneralNovelty(any());

        service.updateStatusCanceledAffiliate("123", "obs");

        assertTrue(affiliate.getAffiliationCancelled());
        assertEquals(Constant.AFFILIATION_STATUS_INACTIVE, affiliate.getAffiliationStatus());
        assertNotNull(affiliate.getDateAffiliateSuspend());
        assertEquals("obs", affiliate.getObservation());
    }

    @Test
    void updateStatusCanceledAffiliate_noAffiliateFound() {
        Page<Affiliate> page = new PageImpl<>(Collections.emptyList());
        when(affiliateRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        // Should not throw
        assertDoesNotThrow(() -> service.updateStatusCanceledAffiliate("123", "obs"));
    }

    @Test
    void validWorkedCompany_userNotFound() {
        when(userMainRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(AffiliationError.class, () -> {
            // Use reflection to call private method
            try {
                var method = CancelAffiliationServiceImpl.class.getDeclaredMethod("validWorkedCompany", Long.class,
                        String.class, String.class);
                method.setAccessible(true);
                method.invoke(service, 1L, "NIT", "SUB");
            } catch (Exception e) {
                throw e.getCause();
            }
        });
    }

    @Test
    void validWorkedCompany_affiliateNotFound() {
        UserMain user = new UserMain();
        user.setIdentification("123");
        when(userMainRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThrows(CancelAffiliationNotFoundException.class, () -> {
            try {
                var method = CancelAffiliationServiceImpl.class.getDeclaredMethod("validWorkedCompany", Long.class,
                        String.class, String.class);
                method.setAccessible(true);
                method.invoke(service, 1L, "NIT", "SUB");
            } catch (Exception e) {
                throw e.getCause();
            }
        });
    }

    @Test
    void validWorkedCompany_workerUnconnected() {
        UserMain user = new UserMain();
        user.setIdentification("123");
        Affiliate affiliate = new Affiliate();
        affiliate.setNitCompany("NIT2");

        when(userMainRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        assertThrows(DateCancelAffiliationException.class, () -> {
            try {
                var method = CancelAffiliationServiceImpl.class.getDeclaredMethod("validWorkedCompany", Long.class,
                        String.class, String.class);
                method.setAccessible(true);
                method.invoke(service, 1L, "NIT1", "SUB");
            } catch (Exception e) {
                throw e.getCause();
            }
        });
    }
}
