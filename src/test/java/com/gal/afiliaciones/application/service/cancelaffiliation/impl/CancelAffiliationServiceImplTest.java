package com.gal.afiliaciones.application.service.cancelaffiliation.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
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
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dto.cancelaffiliate.CancelAffiliateDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

class CancelAffiliationServiceImplTest {

    @Mock
    private AffiliateRepository affiliateRepository;
    @Mock
    private IUserPreRegisterRepository userMainRepository;
    @Mock
    private AffiliationDependentRepository affiliationDependentRepository;
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
        Long idAffiliateEmployer = 1L;
        String filedNumber = "F123";
        String nitCompany = "NIT123";
        String affiliationType = "TYPE1";

        List<AffiliationDependent> affiliationDependentList = new ArrayList<>();

        AffiliationDependent dependent = new AffiliationDependent();
        dependent.setFiledNumber(filedNumber);
        dependent.setCoverageDate(LocalDate.now());

        affiliationDependentList.add(dependent);

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(Boolean.FALSE);
        affiliate.setAffiliationStatus("ACTIVE");
        affiliate.setNitCompany(nitCompany);
        affiliate.setAffiliationType(affiliationType);

        Affiliate affiliateEmployer = new Affiliate();
        affiliateEmployer.setIdAffiliate(idAffiliateEmployer);
        affiliateEmployer.setUserId(idUser);
        affiliateEmployer.setNitCompany("123456");
        affiliateEmployer.setCompany("NAME");
        affiliateEmployer.setAffiliationSubType(subType);

        UserMain user = new UserMain();
        user.setIdentification("123");

        when(affiliateRepository.findByIdAffiliate(anyLong()))
                .thenReturn(Optional.of(affiliateEmployer));
        when(affiliationDependentRepository.findAll(any(Specification.class)))
                .thenReturn(affiliationDependentList);
        when(affiliateRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliate));
        when(affiliateRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliate));
        when(userMainRepository.findById(idUser)).thenReturn(Optional.of(user));
        when(affiliateRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliate));

        CancelAffiliateDTO dto = service.consultAffiliation(documentType, documentNumber, idAffiliateEmployer);

        assertNotNull(dto);
        assertEquals(affiliationType, dto.getContractType());
    }

    @Test
    void consultAffiliation_affiliationNotFound_dependent() {
        when(affiliationDependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.empty());

        assertThrows(CancelAffiliationNotFoundException.class,
                () -> service.consultAffiliation("CC", "123", 1L));
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
                () -> service.consultAffiliation("CC", "123", 1L));
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
                () -> service.consultAffiliation("CC", "123", 1L));
    }

    @Test
    void consultAffiliation_coverageDateTooOld() {
        AffiliationDependent dependent = new AffiliationDependent();
        dependent.setFiledNumber("F123");
        dependent.setCoverageDate(LocalDate.now().minusDays(2));

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(Boolean.FALSE);
        affiliate.setAffiliationStatus("ACTIVE");

        Affiliate affiliateEmployer = new Affiliate();
        affiliateEmployer.setIdAffiliate(123L);
        affiliateEmployer.setUserId(1L);
        affiliateEmployer.setNitCompany("123456");
        affiliateEmployer.setCompany("NAME");
        affiliateEmployer.setAffiliationSubType(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);

        when(affiliateRepository.findByIdAffiliate(anyLong()))
                .thenReturn(Optional.of(affiliateEmployer));
        when(affiliationDependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(dependent));
        when(affiliateRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliate));

        assertThrows(CancelAffiliationNotFoundException.class, () -> service.consultAffiliation("CC", "123", 123L));
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

        assertThrows(CancelAffiliationNotFoundException.class, () -> service.consultAffiliation("CC", "123", 1L));
    }

    @Test
    void updateStatusCanceledAffiliate_success() {
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationCancelled(Boolean.FALSE);
        affiliate.setAffiliationStatus("ACTIVE");

        when(affiliateRepository.findByFiledNumber(anyString())).thenReturn(Optional.of(affiliate));
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
        when(affiliateRepository.findByFiledNumber(anyString())).thenReturn(Optional.empty());

        // Should not throw
        assertThrows(AffiliateNotFound.class, () -> service.updateStatusCanceledAffiliate("123", "obs"));
    }

}
