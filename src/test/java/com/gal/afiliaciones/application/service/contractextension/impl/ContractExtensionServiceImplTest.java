package com.gal.afiliaciones.application.service.contractextension.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.gal.afiliaciones.application.service.CertificateService;
import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.generalnovelty.impl.GeneralNoveltyServiceImpl;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.domain.model.ContractExtension;
import com.gal.afiliaciones.domain.model.Policy;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ICardRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.CertificateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.contractextension.ContractExtensionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.dto.contractextension.ContractExtensionInfoDTO;
import com.gal.afiliaciones.infrastructure.dto.contractextension.ContractExtensionRequest;


class ContractExtensionServiceImplTest {

    @Mock
    private AffiliateRepository affiliateRepository;
    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    @Mock
    private PolicyRepository policyRepository;
    @Mock
    private ICardRepository cardRepository;
    @Mock
    private CertificateRepository certificateRepository;
    @Mock
    private GenerateCardAffiliatedService cardAffiliatedService;
    @Mock
    private CertificateService certificateService;
    @Mock
    private ContractExtensionRepository contractExtensionRepository;

    @Mock
    private GeneralNoveltyServiceImpl generalNoveltyServiceImpl;


    @InjectMocks
    private ContractExtensionServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ContractExtensionServiceImpl(
                affiliateRepository,
                repositoryAffiliation,
                policyRepository,
                cardRepository,
                certificateRepository,
                cardAffiliatedService,
                certificateService,
                contractExtensionRepository,
                generalNoveltyServiceImpl
        );
    }

    @Test
    void getInfoContract_shouldReturnInfo_whenAffiliateAndAffiliationExist() {
        String filedNumber = "123";
        Affiliate affiliate = mock(Affiliate.class);
        Affiliation affiliation = mock(Affiliation.class);

        when(affiliateRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.of(affiliate));
        when(affiliate.getFiledNumber()).thenReturn(filedNumber);
        when(repositoryAffiliation.findByFiledNumber(filedNumber)).thenReturn(Optional.of(affiliation));
        when(affiliation.getContractStartDate()).thenReturn(LocalDate.now().minusMonths(1));
        when(affiliation.getContractEndDate()).thenReturn(LocalDate.now().plusMonths(1));
        when(affiliation.getContractType()).thenReturn("type");
        when(affiliation.getContractQuality()).thenReturn("quality");
        when(affiliation.getTransportSupply()).thenReturn(true);
        when(affiliation.getContractDuration()).thenReturn("30");
        when(affiliation.getJourneyEstablished()).thenReturn("journey");
        when(affiliation.getContractMonthlyValue()).thenReturn(new BigDecimal(1000.0));
        when(affiliation.getContractTotalValue()).thenReturn(new BigDecimal(12000.0));
        when(affiliation.getContractIbcValue()).thenReturn(new BigDecimal(1100.0));

        ContractExtensionInfoDTO dto = service.getInfoContract(filedNumber);

        assertNotNull(dto);
        assertEquals("type", dto.getContractType());
        assertEquals("quality", dto.getContractQuality());
        assertEquals(Boolean.TRUE, dto.getContractTrasnport());
        assertEquals("30", dto.getContractDuration());
        assertEquals("journey", dto.getJourneyEstablishment());
        assertEquals(new BigDecimal(1000.0), dto.getContractMonthlyValue());
        assertEquals(new BigDecimal(12000.0), dto.getContractTotalValue());
        assertEquals(new BigDecimal(1100.0), dto.getContractIBC());
    }

    @Test
    void getInfoContract_shouldThrow_whenAffiliateNotFound() {
        String filedNumber = "notfound";
        when(affiliateRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.empty());
        assertThrows(AffiliateNotFoundException.class, () -> service.getInfoContract(filedNumber));
    }

    @Test
    void getInfoContract_shouldReturnNull_whenAffiliationNotFound() {
        String filedNumber = "123";
        Affiliate affiliate = mock(Affiliate.class);
        when(affiliateRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.of(affiliate));
        when(affiliate.getFiledNumber()).thenReturn(filedNumber);
        when(repositoryAffiliation.findByFiledNumber(filedNumber)).thenReturn(Optional.empty());
        assertNull(service.getInfoContract(filedNumber));
    }

    @Test
    void saveExtensionContract_shouldUpdateAndReturnSuccessMessage() {
        String filedNumber = "123";
        ContractExtensionRequest request = mock(ContractExtensionRequest.class);
        Affiliate affiliate = mock(Affiliate.class);
        Affiliation affiliation = mock(Affiliation.class);

        LocalDate currentEndDate = LocalDate.now();
        LocalDate newEndDate = currentEndDate.plusDays(1);

        when(request.getFiledNumber()).thenReturn(filedNumber);
        when(affiliateRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.of(affiliate));
        when(affiliate.getFiledNumber()).thenReturn(filedNumber);
        when(repositoryAffiliation.findByFiledNumber(filedNumber)).thenReturn(Optional.of(affiliation));
        when(affiliation.getContractEndDate()).thenReturn(currentEndDate);
        when(request.getContractEndDate()).thenReturn(newEndDate);
        when(affiliation.getRisk()).thenReturn("1");

        // For update methods
        when(affiliate.getIdAffiliate()).thenReturn(1L);
        when(affiliation.getId()).thenReturn(2L);
        when(affiliate.getCompany()).thenReturn("company");

        // For policy update
        var policy = mock(Policy.class);
        when(policyRepository.findByIdAffiliate(anyLong())).thenReturn(List.of(policy));

        // For card/certificate deletion
        when(cardRepository.findByFiledNumberAndCompany(anyString(), anyString())).thenReturn(Optional.empty());
        when(certificateRepository.findByFiledNumberAndCompany(anyString(), anyString())).thenReturn(Optional.empty());

        String result = service.saveExtensionContract(request);

        assertEquals("Modificación exitosa, tu modificación de fecha fin de contrato se ha registrado correctamente.", result);

        verify(repositoryAffiliation).save(affiliation);
        verify(affiliateRepository).save(affiliate);
        verify(policyRepository).save(policy);
        verify(cardAffiliatedService).createCardWithoutOtp(filedNumber);
        verify(contractExtensionRepository).save(any(ContractExtension.class));
    }

    @Test
    void saveExtensionContract_shouldThrow_whenAffiliateNotFound() {
        ContractExtensionRequest request = mock(ContractExtensionRequest.class);
        when(request.getFiledNumber()).thenReturn("notfound");
        when(affiliateRepository.findByFiledNumber("notfound")).thenReturn(Optional.empty());
        assertThrows(AffiliateNotFoundException.class, () -> service.saveExtensionContract(request));
    }

    @Test
    void saveExtensionContract_shouldThrow_whenAffiliationNotFound() {
        ContractExtensionRequest request = mock(ContractExtensionRequest.class);
        Affiliate affiliate = mock(Affiliate.class);
        when(request.getFiledNumber()).thenReturn("123");
        when(affiliateRepository.findByFiledNumber("123")).thenReturn(Optional.of(affiliate));
        when(affiliate.getFiledNumber()).thenReturn("123");
        when(repositoryAffiliation.findByFiledNumber("123")).thenReturn(Optional.empty());
        assertThrows(AffiliateNotFoundException.class, () -> service.saveExtensionContract(request));
    }

    @Test
    void validateAffiliationRisk_shouldReturnTrueForValidRisk() {
        Affiliation affiliation = mock(Affiliation.class);
        when(affiliation.getRisk()).thenReturn("1");
        boolean result = (boolean) ReflectionTestUtils.invokeMethod(service, "validateAffiliationRisk", affiliation);
        assertTrue(result);
    }

    @Test
    void validateAffiliationRisk_shouldReturnFalseForInvalidRisk() {
        Affiliation affiliation = mock(Affiliation.class);
        when(affiliation.getRisk()).thenReturn("5");
        boolean result = (boolean) ReflectionTestUtils.invokeMethod(service, "validateAffiliationRisk", affiliation);
        assertFalse(result);
    }

    @Test
    void validateNewEndDate_shouldReturnErrorMessages() {
        LocalDate now = LocalDate.now();
        String resultEqual = (String) ReflectionTestUtils.invokeMethod(service, "validateNewEndDate", now, now);
        assertEquals("La fecha de fin del contrato no puede ser igual a la fecha actual.", resultEqual);

        String resultBefore = (String) ReflectionTestUtils.invokeMethod(service, "validateNewEndDate", now, now.minusDays(1));
        assertEquals("La nueva fecha de fin del contrato debe ser al menos un día posterior a la fecha actual.", resultBefore);

        String resultAfter6Months = (String) ReflectionTestUtils.invokeMethod(service, "validateNewEndDate", now, now.plusMonths(7));
        assertEquals("La nueva fecha de fin del contrato no puede ser más de seis meses posterior a la fecha actual.", resultAfter6Months);

        String resultValid = (String) ReflectionTestUtils.invokeMethod(service, "validateNewEndDate", now, now.plusDays(1));
        assertEquals("", resultValid);
    }
}