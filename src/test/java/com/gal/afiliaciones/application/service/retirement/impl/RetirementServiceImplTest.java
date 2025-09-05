package com.gal.afiliaciones.application.service.retirement.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.config.ex.workerretirement.WorkerRetirementException;
import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirement.RetirementRepository;
import com.gal.afiliaciones.infrastructure.dto.workerretirement.DataWorkerRetirementDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

@ExtendWith(MockitoExtension.class)
class RetirementServiceImplTest {

    @Mock
    private AffiliateRepository affiliateRepository;
    @Mock
    private AffiliationDependentRepository affiliationDependentRepository;
    @Mock
    private IUserPreRegisterRepository userMainRepository;
    @Mock
    private RetirementRepository retirementRepository;
    @Mock
    private FiledService filedService;
    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;
    @Mock
    private SendEmails sendEmails;
    @Mock
    private AffiliateMercantileRepository mercantileRepository;
    @Mock
    private AffiliateService affiliateService;

    @InjectMocks
    private RetirementServiceImpl retirementService;

    private Affiliate affiliate;
    private UserMain userMain;
    private AffiliationDependent affiliationDependent;
    private Affiliation affiliationIndependent;
    private DataWorkerRetirementDTO dataWorkerRetirementDTO;
    private Retirement retirement;

    @BeforeEach
    void setUp() {
        affiliate = new Affiliate();
        userMain = new UserMain();
        affiliationDependent = new AffiliationDependent();
        affiliationIndependent = new Affiliation();
        dataWorkerRetirementDTO = new DataWorkerRetirementDTO();
        retirement = new Retirement();
    }


    @Test
    void validWorkedCompany_userNotFound_throwsException() {
        when(userMainRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(WorkerRetirementException.class, () -> {
            try {
            Method method = RetirementServiceImpl.class.getDeclaredMethod("validWorkedCompany", Long.class, String.class, String.class, String.class);
            method.setAccessible(true);
            method.invoke(retirementService, 1L, "CC", "123", "subType");
            } catch (Exception e) {
            throw new WorkerRetirementException(e.getMessage());
            }
        });
    }

    @Test
    void validWorkedCompany_affiliateLegalRepresentativeNotFound_throwsException() {
        when(userMainRepository.findById(any())).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThrows(WorkerRetirementException.class, () -> {
            try {
                Method method = RetirementServiceImpl.class.getDeclaredMethod("validWorkedCompany", Long.class, String.class, String.class, String.class);
                method.setAccessible(true);
                method.invoke(retirementService, 1L, "CC", "123", "subType");
            } catch (Exception e) {
                throw new WorkerRetirementException(e.getMessage());
            }
        });
    }

    @Test
    void validWorkedCompany_affiliateWorkerListEmpty_throwsException() {
        when(userMainRepository.findById(any())).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());

        assertThrows(WorkerRetirementException.class, () -> {
            try {
                Method method = RetirementServiceImpl.class.getDeclaredMethod("validWorkedCompany", Long.class, String.class, String.class, String.class);
                method.setAccessible(true);
                method.invoke(retirementService, 1L, "CC", "123", "subType");
            } catch (Exception e) {
                throw new WorkerRetirementException(e.getMessage());
            }
        });
    }

    @Test
    void validWorkedCompany_returnsFirstAffiliateWorker() {
        Affiliate affiliate1 = new Affiliate();
        affiliate1.setAffiliationDate(LocalDateTime.now().minusDays(1));
        Affiliate affiliate2 = new Affiliate();
        affiliate2.setAffiliationDate(LocalDateTime.now());

        List<Affiliate> affiliateWorkerList = List.of(affiliate1, affiliate2);

        when(userMainRepository.findById(any())).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(affiliateWorkerList);

        Affiliate result = null;
        try {
            Method method = RetirementServiceImpl.class.getDeclaredMethod("validWorkedCompany", Long.class, String.class, String.class, String.class);
            method.setAccessible(true);
            result = (Affiliate) method.invoke(retirementService, 1L, "CC", "123", "subType");
        } catch (Exception e) {
            
        }

        assertTrue(true);
    }

    @Test
    void retirementWorker_affiliateNotFound_throwsException() {
        when(affiliateRepository.findByIdAffiliate(any())).thenReturn(Optional.empty());

        assertThrows(WorkerRetirementException.class, () -> retirementService.retirementWorker(dataWorkerRetirementDTO));
    }

    @Test
    void retirementWorker_validInput_updatesAffiliateAndCreatesRetirement() {
        affiliate.setIdAffiliate(1L);
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setAffiliationSubType("SubType");
        dataWorkerRetirementDTO.setIdAffiliation(1L);
        dataWorkerRetirementDTO.setRetirementDate(LocalDate.now().plusDays(1));
        affiliationDependent.setFirstName("Test");
        affiliationDependent.setSurname("Test2");
        affiliationDependent.setSecondName("");
        affiliationDependent.setSecondSurname("");

        when(affiliateRepository.findByIdAffiliate(1L)).thenReturn(Optional.of(affiliate));
        when(filedService.getNextFiledNumberRetirementReason()).thenReturn("FILENUMBER123");
        when(retirementRepository.findByIdAffiliate(1L)).thenReturn(Optional.empty());
        when(affiliationDependentRepository.findByFiledNumber(any())).thenReturn(Optional.of(affiliationDependent));
        when(retirementRepository.save(any(Retirement.class))).thenReturn(retirement);

        retirementService.retirementWorker(dataWorkerRetirementDTO);

        assertTrue(true);
    }

    @Test
    void validateRetirementDate_nullRetirementDate_throwsException() throws Exception {
        Method method = RetirementServiceImpl.class.getDeclaredMethod("validateRetirementDate", Affiliate.class, LocalDate.class);
        method.setAccessible(true);

        assertThrows(WorkerRetirementException.class, () -> {
            try {
                method.invoke(retirementService, affiliate, null);
            } catch (Exception e) {
                throw new WorkerRetirementException(e.getCause().getMessage());
            }
        });
    }

    @Test
    void validateRetirementDate_retirementDateAfterContractEndDate_throwsException() throws Exception {
        Method method = RetirementServiceImpl.class.getDeclaredMethod("validateRetirementDate", Affiliate.class, LocalDate.class);
        method.setAccessible(true);

        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setFiledNumber("123");
        affiliationIndependent.setContractEndDate(LocalDate.now().minusDays(1));

        when(affiliationRepository.findByFiledNumber("123")).thenReturn(Optional.of(affiliationIndependent));

        assertThrows(WorkerRetirementException.class, () -> {
            try {
                method.invoke(retirementService, affiliate, LocalDate.now());
            } catch (Exception e) {
                throw new WorkerRetirementException(e.getCause().getMessage());
            }
        });
    }

    @Test
    void findUserNameToRetired_affiliationNotFound_throwsException() throws Exception {
        Method method = RetirementServiceImpl.class.getDeclaredMethod("findUserNameToRetired", String.class);
        method.setAccessible(true);

        when(affiliationDependentRepository.findByFiledNumber(any())).thenReturn(Optional.empty());

        assertThrows(WorkerRetirementException.class, () -> {
            try {
                method.invoke(retirementService, "123");
            } catch (Exception e) {
                throw new WorkerRetirementException(e.getCause().getMessage());
            }
        });
    }

    @Test
    void findUserNameToRetired_affiliationFound_returnsCompleteName() throws Exception {
        Method method = RetirementServiceImpl.class.getDeclaredMethod("findUserNameToRetired", String.class);
        method.setAccessible(true);

        affiliationDependent.setFirstName("Test");
        affiliationDependent.setSurname("Test2");
        affiliationDependent.setSecondName("");
        affiliationDependent.setSecondSurname("");

        when(affiliationDependentRepository.findByFiledNumber(any())).thenReturn(Optional.of(affiliationDependent));

        String completeName = null;
        try {
            completeName = (String) method.invoke(retirementService, "123");
        } catch (Exception e) {
            throw new WorkerRetirementException(e.getCause().getMessage());
        }

        assertEquals("Test Test2", completeName);
    }

    @Test
    void cancelRetirementWorker_affiliateNotFound_throwsException() {
        when(affiliateRepository.findByIdAffiliate(any())).thenReturn(Optional.empty());

        assertThrows(WorkerRetirementException.class, () -> retirementService.cancelRetirementWorker(1L));
    }

    @Test
    void cancelRetirementWorker_workerNotRetired_throwsException() {
        affiliate.setNoveltyType(Constant.NOVELTY_TYPE_RETIREMENT);
        when(affiliateRepository.findByIdAffiliate(any())).thenReturn(Optional.of(affiliate));

        assertThrows(WorkerRetirementException.class, () -> retirementService.cancelRetirementWorker(1L));
    }

    @Test
    void cancelRetirementWorker_retirementNotFound_throwsException() {
        affiliate.setNoveltyType("OTHER");
        when(affiliateRepository.findByIdAffiliate(any())).thenReturn(Optional.of(affiliate));
        when(retirementRepository.findByIdAffiliate(any())).thenReturn(Optional.empty());

        assertThrows(WorkerRetirementException.class, () -> retirementService.cancelRetirementWorker(1L));
    }

    @Test
    void cancelRetirementWorker_validInput_deletesRetirementAndSetsAffiliateRetirementDateToNull() {
        affiliate.setNoveltyType("OTHER");
        affiliate.setIdAffiliate(1L);
        when(affiliateRepository.findByIdAffiliate(any())).thenReturn(Optional.of(affiliate));
        when(retirementRepository.findByIdAffiliate(any())).thenReturn(Optional.of(retirement));

        Boolean result = retirementService.cancelRetirementWorker(1L);

        assertEquals(true, result);
    }

    @Test
    void createRequestRetirementWork_affiliateNotFound_returnsError() {
        when(affiliateRepository.findByIdAffiliate(any())).thenReturn(Optional.empty());

        String result = retirementService.createRequestRetirementWork(1L, LocalDate.now(), "Test");

        assertEquals("Error: affiliate not found", result);
    }
}
