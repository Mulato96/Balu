package com.gal.afiliaciones.application.service.contract.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.impl.AffiliationEmployerDomesticServiceIndependentServiceImpl;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dto.contract.ContractEmployerResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.contract.ContractFilterDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

class ContractServiceImplTest {

    @Mock
    private AffiliateRepository affiliateRepository;
    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    @Mock
    private AffiliationEmployerDomesticServiceIndependentServiceImpl affiliationService;

    @InjectMocks
    private ContractServiceImpl contractService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findContractsByEmployer_returnsEmptyList_whenNoAffiliatesFound() {
        ContractFilterDTO filters = new ContractFilterDTO();
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        List<ContractEmployerResponseDTO> result = contractService.findContractsByEmployer(filters);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findContractsByEmployer_returnsContractsList_whenAffiliatesAndAffiliationsExist() {
        ContractFilterDTO filters = new ContractFilterDTO();
        Affiliate affiliate = new Affiliate();
        affiliate.setCompany("TestCompany");
        affiliate.setFiledNumber("123");
        affiliate.setAffiliationStatus("ACTIVE");
        affiliate.setAffiliationSubType("TYPE1");
        affiliate.setIdAffiliate(1L);
        affiliate.setAffiliationType("TYPE_A");

        List<Affiliate> affiliates = List.of(affiliate);

        Affiliation affiliation = new Affiliation();
        affiliation.setStageManagement("STAGE1");
        affiliation.setContractStartDate(LocalDate.of(2023, 1, 1));
        affiliation.setContractEndDate(LocalDate.of(2023, 12, 31));

        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(affiliates);
        when(repositoryAffiliation.findByFiledNumber("123")).thenReturn(Optional.of(affiliation));

        List<ContractEmployerResponseDTO> result = contractService.findContractsByEmployer(filters);

        assertEquals(1, result.size());
        ContractEmployerResponseDTO dto = result.get(0);
        assertEquals("TestCompany", dto.getCompany());
        assertEquals("STAGE1", dto.getStageManagement());
        assertEquals("2023-01-01", dto.getStartContractDate());
        assertEquals("2023-12-31", dto.getEndContractDate());
        assertEquals("ACTIVE", dto.getStatus());
        assertEquals("123", dto.getFiledNumber());
        assertEquals("TYPE1", dto.getBondingType());
        assertEquals(1L, dto.getIdAffiliate());
        assertEquals("TYPE_A", dto.getAffiliationType());
    }

    @Test
    void findContractsByEmployer_filtersByUpdateRequiredTrue() {
        ContractFilterDTO filters = new ContractFilterDTO();
        filters.setUpdateRequired(true);

        Affiliate affiliate = new Affiliate();
        affiliate.setCompany("TestCompany");
        affiliate.setFiledNumber("123");
        affiliate.setAffiliationStatus("ACTIVE");
        affiliate.setAffiliationSubType("TYPE1");
        affiliate.setIdAffiliate(1L);
        affiliate.setAffiliationType("TYPE_A");

        Affiliation affiliation = new Affiliation();
        affiliation.setStageManagement(Constant.PENDING_COMPLETE_FORM);

        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(List.of(affiliate));
        when(repositoryAffiliation.findByFiledNumber("123")).thenReturn(Optional.of(affiliation));

        List<ContractEmployerResponseDTO> result = contractService.findContractsByEmployer(filters);

        assertEquals(1, result.size());
        assertEquals(Constant.PENDING_COMPLETE_FORM, result.get(0).getStageManagement());
    }

    @Test
    void findContractsByEmployer_filtersByUpdateRequiredFalse() {
        ContractFilterDTO filters = new ContractFilterDTO();
        filters.setUpdateRequired(false);

        Affiliate affiliate = new Affiliate();
        affiliate.setCompany("TestCompany");
        affiliate.setFiledNumber("123");
        affiliate.setAffiliationStatus("ACTIVE");
        affiliate.setAffiliationSubType("TYPE1");
        affiliate.setIdAffiliate(1L);
        affiliate.setAffiliationType("TYPE_A");

        Affiliation affiliation = new Affiliation();
        affiliation.setStageManagement("OTHER_STAGE");

        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(List.of(affiliate));
        when(repositoryAffiliation.findByFiledNumber("123")).thenReturn(Optional.of(affiliation));

        List<ContractEmployerResponseDTO> result = contractService.findContractsByEmployer(filters);

        assertEquals(1, result.size());
        assertEquals("OTHER_STAGE", result.get(0).getStageManagement());
    }

    @Test
    void getStep1Pila_returnsAffiliationData_whenAffiliateAndAffiliationExist() {
        String filedNumber = "123";
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationSubType("TYPE1");

        Affiliation affiliation = new Affiliation();

        when(affiliateRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliationService.getAffiliationDataByType("TYPE1", affiliation)).thenReturn("DATA");

        Object result = contractService.getStep1Pila(filedNumber);

        assertEquals("DATA", result);
    }

    @Test
    void getStep1Pila_returnsNewAffiliation_whenNoAffiliationFound() {
        String filedNumber = "123";
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationSubType("TYPE1");

        when(affiliateRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());

        Object result = contractService.getStep1Pila(filedNumber);

        assertTrue(result instanceof Affiliation);
    }

    @Test
    void getStep1Pila_throwsException_whenAffiliateNotFound() {
        String filedNumber = "not_found";
        when(affiliateRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.empty());

        assertThrows(AffiliateNotFound.class, () -> contractService.getStep1Pila(filedNumber));
    }
}
