package com.gal.afiliaciones.application.service.billing.impl;

import static com.gal.afiliaciones.infrastructure.utils.Constant.AFFILIATION_STATUS_ACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.BillDetail;
import com.gal.afiliaciones.domain.model.Billing;
import com.gal.afiliaciones.domain.model.Policy;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.billing.BillDetailHistoryRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.billing.BillingHistoryRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.BillDetailRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.BillingRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;

class BillingServiceImplTest {

    @Mock
    private BillingRepository billingRepository;
    @Mock
    private BillingHistoryRepository billingHistoryRepository;
    @Mock
    private AffiliateRepository affiliationRepository;
    @Mock
    private PolicyRepository policyRepository;
    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository iAffiliationEmployerDomesticServiceIndependentRepository;
    @Mock
    private GenericWebClient webClient;
    @Mock
    private AffiliationDependentRepository dependentRepository;
    @Mock
    private BillDetailRepository billDetailRepository;
    @Mock
    private BillDetailHistoryRepository billDetailHistoryRepository;
    @Mock
    private CollectProperties collectProperties;

    @InjectMocks
    private BillingServiceImpl billingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateBilling_movesCurrentBillsAndDetailsAndSavesNewBills() {
        Policy policy = new Policy();
        policy.setId(1L);
        // Prepare current bills and bill details
        Billing billing1 = Billing.builder()
                .policy(policy)
                .branch("branch1")
                .insuranceBranch("insBranch1")
                .billingEffectiveDateFrom(LocalDate.now().minusDays(10))
                .billingEffectiveDateTo(LocalDate.now())
                .contributorType("CC")
                .contributorId("123")
                .salary(BigDecimal.valueOf(1000))
                .riskRate(BigDecimal.valueOf(2))
                .billingDays(30)
                .billingAmount(BigDecimal.valueOf(20))
                .paymentPeriod("202306")
                .build();
        List<Billing> currentBills = List.of(billing1);

        BillDetail billDetail1 = BillDetail.builder()
                .policy(policy)
                .identificationType("CC")
                .identificationNumber("123")
                .billingAmount(BigDecimal.valueOf(20))
                .build();
        List<BillDetail> currentBillDetails = List.of(billDetail1);

        when(billingRepository.findAll()).thenReturn(currentBills);
        when(billDetailRepository.findAll()).thenReturn(currentBillDetails);
        when(collectProperties.getCutSettlementOne()).thenReturn(13);

        // Prepare active affiliates
        Affiliate affiliate = Affiliate.builder()
                .idAffiliate(1L)
                .filedNumber("filed123")
                .documentType("CC")
                .documentNumber("123")
                .build();
        List<Affiliate> activeAffiliates = List.of(affiliate);
        when(affiliationRepository.findAllByAffiliationStatusAndFiledNumberIsNotNull(AFFILIATION_STATUS_ACTIVE)).thenReturn(activeAffiliates);

        // Prepare policies for affiliate
        Policy policy2 = new Policy();
        policy2.setId(10L);
        policy2.setCode("POL123");
        policy2.setEffectiveDateFrom(LocalDate.now().minusMonths(1));
        policy2.setEffectiveDateTo(LocalDate.now().plusMonths(1));
        policy2.setIdAffiliate(1L);
        when(policyRepository.findByIdAffiliate(1L)).thenReturn(List.of(policy2));
        when(policyRepository.findByCode("POL123")).thenReturn(List.of(policy2));

        // Prepare affiliation and dependent
        Affiliation affiliation = new Affiliation();
        affiliation.setContractMonthlyValue(BigDecimal.valueOf(1000));
        affiliation.setPrice(BigDecimal.valueOf(2));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findByFiledNumber("filed123")).thenReturn(Optional.of(affiliation));

        AffiliationDependent dependent = new AffiliationDependent();
        dependent.setSalary(BigDecimal.valueOf(500));
        dependent.setPriceRisk(BigDecimal.valueOf(1));
        dependent.setIdentificationDocumentType("CC");
        dependent.setIdentificationDocumentNumber("456");
        when(dependentRepository.findByFiledNumber("filed123")).thenReturn(Optional.of(dependent));

        // Mock dependentRepository.findOne for calculateBillingAmountDependent
        when(affiliationRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(dependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(dependent));

        // Mock webClient for salary minimum
        SalaryDTO salaryDTO = new SalaryDTO();
        salaryDTO.setValue(1000L);
        when(webClient.getSmlmvByYear(anyInt())).thenReturn(salaryDTO);
        
        // Mock consecutive methods
        when(billingRepository.findConsecutiveEmployer()).thenReturn(Optional.of(1L));
        when(billingRepository.findConsecutiveWorker()).thenReturn(Optional.of(1L));

        // Run method
        billingService.generateBilling();

        // Verify consecutive methods were called
        verify(billingRepository).findConsecutiveEmployer();
        verify(billingRepository).findConsecutiveWorker();

        // Verify new bills saved
        verify(billingRepository).saveAll(anyList());
    }

    @Test
    void testGenerateBill_returnsNullWhenNoPolicies() {
        Affiliate affiliate = Affiliate.builder()
                .idAffiliate(1L)
                .filedNumber("filed123")
                .build();
        when(policyRepository.findByIdAffiliate(1L)).thenReturn(Collections.emptyList());

        Billing result = null;
        try {
            java.lang.reflect.Method method = BillingServiceImpl.class.getDeclaredMethod("generateBill", Affiliate.class);
            method.setAccessible(true);
            result = (Billing) method.invoke(billingService, affiliate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNull(result);
    }

    @Test
    void testGenerateBill_returnsNullWhenNoAffiliationNorDependent() {
        Affiliate affiliate = Affiliate.builder()
                .idAffiliate(1L)
                .filedNumber("filed123")
                .build();
        Policy policy = new Policy();
        policy.setId(10L);
        policy.setCode("POL123");
        policy.setEffectiveDateFrom(LocalDate.now());
        policy.setEffectiveDateTo(LocalDate.now().plusMonths(30));
        policy.setIdAffiliate(1L);
        when(policyRepository.findByIdAffiliate(1L)).thenReturn(List.of(policy));
        when(iAffiliationEmployerDomesticServiceIndependentRepository.findByFiledNumber("filed123")).thenReturn(Optional.empty());
        when(dependentRepository.findByFiledNumber("filed123")).thenReturn(Optional.empty());

        Billing result = null;
        try {
            java.lang.reflect.Method method = BillingServiceImpl.class.getDeclaredMethod("generateBill", Affiliate.class);
            method.setAccessible(true);
            result = (Billing) method.invoke(billingService, affiliate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNull(result);
    }

    @Test
    void testCalculateBillingAmount_correctCalculation() {
        BigDecimal salary = BigDecimal.valueOf(2000);
        BigDecimal riskRate = BigDecimal.valueOf(3);
        BigDecimal expected = salary.multiply(riskRate).divide(BigDecimal.valueOf(100)).setScale(2, BigDecimal.ROUND_HALF_UP);

        BigDecimal result = null;
        try {
            java.lang.reflect.Method method = BillingServiceImpl.class.getDeclaredMethod("calculateBillingAmount", BigDecimal.class, BigDecimal.class);
            method.setAccessible(true);
            result = (BigDecimal) method.invoke(billingService, salary, riskRate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(expected, result);
    }

    @Test
    void testAjustarSalario_belowMinimum_returnsMinimum() {
        SalaryDTO salaryDTO = new SalaryDTO();
        salaryDTO.setValue(1000L);
        when(webClient.getSmlmvByYear(anyInt())).thenReturn(salaryDTO);
        BigDecimal salary = BigDecimal.valueOf(500);
        BigDecimal adjusted = null;
        try {
            java.lang.reflect.Method method = BillingServiceImpl.class.getDeclaredMethod("ajustarSalario", BigDecimal.class);
            method.setAccessible(true);
            adjusted = (BigDecimal) method.invoke(billingService, salary);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(BigDecimal.valueOf(1000), adjusted);
    }

    @Test
    void testAjustarSalario_aboveMaximum_returnsMaximum() {
        SalaryDTO salaryDTO = new SalaryDTO();
        salaryDTO.setValue(1000L);
        when(webClient.getSmlmvByYear(anyInt())).thenReturn(salaryDTO);
        BigDecimal salary = BigDecimal.valueOf(30000);
        BigDecimal expectedMax = BigDecimal.valueOf(1000).multiply(BigDecimal.valueOf(25));
        BigDecimal adjusted = null;
        try {
            java.lang.reflect.Method method = BillingServiceImpl.class.getDeclaredMethod("ajustarSalario", BigDecimal.class);
            method.setAccessible(true);
            adjusted = (BigDecimal) method.invoke(billingService, salary);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(expectedMax, adjusted);
    }

    @Test
    void testAjustarSalario_withinRange_returnsSame() {
        SalaryDTO salaryDTO = new SalaryDTO();
        salaryDTO.setValue(1000L);
        when(webClient.getSmlmvByYear(anyInt())).thenReturn(salaryDTO);
        BigDecimal salary = BigDecimal.valueOf(1500);
        BigDecimal adjusted = null;
        try {
            java.lang.reflect.Method method = BillingServiceImpl.class.getDeclaredMethod("ajustarSalario", BigDecimal.class);
            method.setAccessible(true);
            adjusted = (BigDecimal) method.invoke(billingService, salary);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(salary, adjusted);
    }

    @Test
    void testGetAllBills_callsRepository() {
        List<Billing> bills = List.of(Billing.builder().build());
        when(billingRepository.findAll()).thenReturn(bills);
        List<Billing> result = billingService.getAllBills();
        assertSame(bills, result);
        verify(billingRepository).findAll();
    }

    @Test
    void testGetBillsByContributor_callsRepository() {
        String contributorId = "123";
        List<Billing> bills = List.of(Billing.builder().build());
        when(billingRepository.findByContributorId(contributorId)).thenReturn(bills);
        List<Billing> result = billingService.getBillsByContributor(contributorId);
        assertSame(bills, result);
        verify(billingRepository).findByContributorId(contributorId);
    }

    @Test
    void testGetBillsByDates_callsRepository() {
        LocalDate from = LocalDate.now().minusDays(10);
        LocalDate to = LocalDate.now();
        List<Billing> bills = List.of(Billing.builder().build());
        when(billingRepository.findByBillingEffectiveDateFromLessThanEqualAndBillingEffectiveDateToGreaterThanEqual(from, to)).thenReturn(bills);
        List<Billing> result = billingService.getBillsByDates(from, to);
        assertSame(bills, result);
        verify(billingRepository).findByBillingEffectiveDateFromLessThanEqualAndBillingEffectiveDateToGreaterThanEqual(from, to);
    }

    @Test
    void testGetBillById_found() {
        Billing bill = Billing.builder().build();
        when(billingRepository.findById(1L)).thenReturn(Optional.of(bill));
        Billing result = billingService.getBillById(1L);
        assertSame(bill, result);
    }

    @Test
    void testGetBillById_notFound() {
        when(billingRepository.findById(1L)).thenReturn(Optional.empty());
        Billing result = billingService.getBillById(1L);
        assertNull(result);
    }

    @Test
    void testCalculateBillingAmountDependent_withDependents() {
        Policy policy = new Policy();
        policy.setId(10L);
        policy.setCode("POL123");
        policy.setIdAffiliate(1L);
        Affiliate affiliate = Affiliate.builder()
                .idAffiliate(1L)
                .filedNumber("filed123")
                .build();
        AffiliationDependent dependent = new AffiliationDependent();
        dependent.setSalary(BigDecimal.valueOf(1000));
        dependent.setPriceRisk(BigDecimal.valueOf(2));
        dependent.setIdentificationDocumentType("CC");
        dependent.setIdentificationDocumentNumber("123");

        when(policyRepository.findByCode("POL123")).thenReturn(List.of(policy));
        when(affiliationRepository.findByIdAffiliate(1L)).thenReturn(Optional.of(affiliate));
        when(dependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(dependent));
        SalaryDTO salaryDTO = new SalaryDTO();
        salaryDTO.setValue(1000L);
        when(webClient.getSmlmvByYear(anyInt())).thenReturn(salaryDTO);

        Map<Integer, BigDecimal> result = null;
        try {
            java.lang.reflect.Method method = BillingServiceImpl.class.getDeclaredMethod("calculateBillingAmountDependent", String.class);
            method.setAccessible(true);
            result = (Map<Integer, BigDecimal>) method.invoke(billingService, "POL123");
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(result);
        assertTrue(result.keySet().stream().findFirst().isPresent());
        assertTrue(result.values().stream().findFirst().get().compareTo(BigDecimal.ZERO) > 0);

        verify(billDetailRepository).save(any(BillDetail.class));
    }

    @Test
    void testCalculateBillingAmountDependent_noDependents() {
        Policy policy = new Policy();
        policy.setId(10L);
        policy.setCode("POL123");
        policy.setIdAffiliate(1L);
        Affiliate affiliate = Affiliate.builder()
                .idAffiliate(1L)
                .filedNumber("filed123")
                .build();

        when(policyRepository.findByCode("POL123")).thenReturn(List.of(policy));
        when(affiliationRepository.findByIdAffiliate(1L)).thenReturn(Optional.of(affiliate));
        when(dependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        SalaryDTO salaryDTO = new SalaryDTO();
        salaryDTO.setValue(1000L);
        when(webClient.getSmlmvByYear(anyInt())).thenReturn(salaryDTO);

        Map<Integer, BigDecimal> result = null;
        try {
            java.lang.reflect.Method method = BillingServiceImpl.class.getDeclaredMethod("calculateBillingAmountDependent", String.class);
            method.setAccessible(true);
            result = (Map<Integer, BigDecimal>) method.invoke(billingService, "POL123");
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(result);
        assertEquals(0, result.keySet().stream().findFirst().orElse(-1));
        assertEquals(BigDecimal.ZERO.setScale(2), result.values().stream().findFirst().orElse(BigDecimal.ONE).setScale(2));
    }
}