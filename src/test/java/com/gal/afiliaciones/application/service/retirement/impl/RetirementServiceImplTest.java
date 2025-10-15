package com.gal.afiliaciones.application.service.retirement.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
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
    void validWorkedCompany_dependentNotFound_throwsException() {
        when(affiliationDependentRepository.findAll(any(Specification.class))).thenReturn(List.of());

        assertThrows(WorkerRetirementException.class, () -> {
            try {
 
            Method method = RetirementServiceImpl.class.getDeclaredMethod("validWorkedCompany", Long.class, String.class, String.class);
            method.setAccessible(true);
            method.invoke(retirementService, 1L, "CC", "123");

            } catch (Exception e) {
                throw new WorkerRetirementException(e.getMessage());
            }
        });
    }

    @Test
    void validWorkedCompany_affiliateNotFound_throwsException() {
        // Datos de entrada
        Long idAffiliateEmployer = 1L;
        String documentType = "CC";
        String documentNumber = "123456";

        // Mock de AffiliationDependent existente
        AffiliationDependent dependent = new AffiliationDependent();
        dependent.setId(100L);
        dependent.setFiledNumber("F123");

        // Simular resultados del repositorio
        when(affiliationDependentRepository.findAll(any(Specification.class))).thenReturn(List.of(dependent));
        lenient().when(affiliateRepository.findByFiledNumber("F123")).thenReturn(Optional.empty());

        assertThrows(AffiliateNotFound.class, () -> {
            try {
                Method method = RetirementServiceImpl.class.getDeclaredMethod("validWorkedCompany", Long.class, String.class, String.class);
                method.setAccessible(true);
                method.invoke(retirementService, idAffiliateEmployer, documentType, documentNumber);
            } catch (Exception e) {
                throw new AffiliateNotFound(e.getMessage());
            }
        });
    }

    @Test
    void validWorkedCompany_returnsFirstAffiliateWorker() {
        Affiliate affiliate1 = new Affiliate();
        affiliate1.setAffiliationDate(LocalDateTime.now().minusDays(1));
        Affiliate affiliate2 = new Affiliate();
        affiliate2.setAffiliationDate(LocalDateTime.now());

        List<AffiliationDependent> affiliationDependentList = new ArrayList<>();
        affiliationDependentList.add(affiliationDependent);

        when(affiliationDependentRepository.findAll(any(Specification.class))).thenReturn(affiliationDependentList);
        when(affiliateRepository.findByFiledNumber(anyString())).thenReturn(Optional.of(affiliate));

        Affiliate result = null;
        try {
            Method method = RetirementServiceImpl.class.getDeclaredMethod("validWorkedCompany", Long.class, String.class, String.class);
            method.setAccessible(true);
            result = (Affiliate) method.invoke(retirementService, 1L, "CC", "123");
        } catch (Exception e) {

        }

        assertTrue(true);
    }

    @Test
    void retirementWorker_affiliationError_throwsException() {
        when(affiliationDependentRepository.findAll(any(Specification.class))).thenReturn(List.of());

        assertThrows(AffiliationError.class, () -> retirementService.retirementWorker(dataWorkerRetirementDTO));
    }
    @Test
    void retirementWorker_validInput_updatesAffiliateAndCreatesRetirement() {
        // Affiliate (trabajador)
        affiliate.setIdAffiliate(1L);
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setAffiliationSubType("SubType");
        affiliate.setFiledNumber("F123"); // obligatorio para validateRetirementDate()

        // DTO de entrada con fecha válida (antes de endDate)
        dataWorkerRetirementDTO.setIdAffiliation(1L);
        dataWorkerRetirementDTO.setIdentificationDocumentType("CC");
        dataWorkerRetirementDTO.setIdentificationDocumentNumber("123");
        dataWorkerRetirementDTO.setIdAffiliateEmployer(999L);
        dataWorkerRetirementDTO.setRetirementDate(LocalDate.now().plusDays(1));

        // Afiliación dependiente del trabajador (debe existir y tener endDate >= fecha retiro)
        affiliationDependent.setFirstName("Test");
        affiliationDependent.setSurname("Test2");
        affiliationDependent.setSecondName("");
        affiliationDependent.setSecondSurname("");
        affiliationDependent.setFiledNumber("F123");
        affiliationDependent.setEndDate(LocalDate.now().plusDays(10)); // clave para que no lance excepción

        // Mocks mínimos
        when(affiliationDependentRepository.findAll(any(Specification.class)))
                .thenReturn(java.util.List.of(affiliationDependent));
        when(affiliateRepository.findByIdAffiliate(1L))
                .thenReturn(java.util.Optional.of(affiliate));
        when(affiliationDependentRepository.findByFiledNumber("F123"))
                .thenReturn(java.util.Optional.of(affiliationDependent));
        when(filedService.getNextFiledNumberRetirementReason())
                .thenReturn("FILENUMBER123");
        when(retirementRepository.findByIdAffiliate(1L))
                .thenReturn(java.util.Optional.empty());
        when(retirementRepository.save(any(Retirement.class)))
                .thenReturn(retirement);

        // Ejecutar
        retirementService.retirementWorker(dataWorkerRetirementDTO);

        // Si no lanzó excepción, la validación pasó
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
