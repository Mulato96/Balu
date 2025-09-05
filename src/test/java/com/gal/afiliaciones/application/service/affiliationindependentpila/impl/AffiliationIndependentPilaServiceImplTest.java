package com.gal.afiliaciones.application.service.affiliationindependentpila.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.policy.PolicyService;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyIndependentRequestDTO;



class AffiliationIndependentPilaServiceImplTest {

    private IUserPreRegisterRepository userPreRegisterRepository;
    private IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    private FiledService filedService;
    private AffiliateRepository affiliateRepository;
    private PolicyService policyService;

    private AffiliationIndependentPilaServiceImpl service;

    @BeforeEach
    void setUp() {
        userPreRegisterRepository = mock(IUserPreRegisterRepository.class);
        repositoryAffiliation = mock(IAffiliationEmployerDomesticServiceIndependentRepository.class);
        filedService = mock(FiledService.class);
        affiliateRepository = mock(AffiliateRepository.class);
        policyService = mock(PolicyService.class);

        service = new AffiliationIndependentPilaServiceImpl(
                userPreRegisterRepository,
                repositoryAffiliation,
                filedService,
                affiliateRepository,
                policyService
        );
    }

    @Test
    void createAffiliationProvisionServicePila_success() {
        NoveltyIndependentRequestDTO dto = new NoveltyIndependentRequestDTO();
        dto.setIdentificationDocumentType("CC");
        dto.setIdentificationDocumentNumber("123456");
        dto.setFirstName("John");
        dto.setSecondName("M");
        dto.setSurname("Doe");
        dto.setSecondSurname("Smith");
        dto.setAddress("Address");
        dto.setDepartment(1L);
        dto.setCityMunicipality(2L);
        dto.setStartDate(LocalDate.of(2023,1,1));
        dto.setEndDate(LocalDate.of(2023,12,31));
        dto.setDuration("12");
        dto.setContractMonthlyValue(BigDecimal.valueOf(1000));
        dto.setContributorTypeCode("CTC");
        dto.setContributantTypeCode(1);
        dto.setContributantSubtypeCode(1);

        UserMain userMain = new UserMain();
        userMain.setId(10L);
        userMain.setDateBirth(LocalDate.of(1990,1,1));
        userMain.setNationality(1L);
        userMain.setSex("M");
        userMain.setOtherSex(null);
        userMain.setPhoneNumber2("123456789");

        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification(any(), any())).thenReturn(Optional.of(userMain));
        when(userPreRegisterRepository.findOne(Mockito.<Specification<UserMain>>any())).thenReturn(Optional.of(userMain));
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("F123");
        when(affiliateRepository.save(any())).thenAnswer(i -> {
            Affiliate a = i.getArgument(0);
            a.setIdAffiliate(100L);
            return a;
        });

        Long result = service.createAffiliationProvisionServicePila(dto);

        assertNotNull(result);
        assertEquals(100L, result);

        verify(policyService, times(2)).createPolicy(any(), any(), any(), any(), any(), any(), any());
        verify(repositoryAffiliation).save(any());
    }

    @Test
    void mapperDtoToEntity_userNotFound_throwsUserNotFoundInDataBase() {
        NoveltyIndependentRequestDTO dto = new NoveltyIndependentRequestDTO();
        dto.setIdentificationDocumentType("CC");
        dto.setIdentificationDocumentNumber("000");

        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification(any(), any())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundInDataBase.class, () -> {
            service.createAffiliationProvisionServicePila(dto);
        });
    }

    @Test
    void createAffiliationTaxiDriverPila_success() {
        NoveltyIndependentRequestDTO dto = new NoveltyIndependentRequestDTO();
        dto.setIdentificationDocumentType("CC");
        dto.setIdentificationDocumentNumber("123");
        dto.setStartDate(LocalDate.of(2023,1,1));
        dto.setEndDate(LocalDate.of(2023,12,31));
        dto.setDuration("12");
        dto.setContractMonthlyValue(BigDecimal.valueOf(1000));
        dto.setContributorTypeCode("CTC");
        dto.setContributantTypeCode(1);
        dto.setContributantSubtypeCode(1);

        UserMain userMain = new UserMain();
        userMain.setId(20L);
        userMain.setDateBirth(LocalDate.of(1980,1,1));
        userMain.setNationality(1L);
        userMain.setSex("F");
        userMain.setOtherSex(null);
        userMain.setPhoneNumber2("987654321");

        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification(any(), any())).thenReturn(Optional.of(userMain));
        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(userMain));
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("F456");
        when(affiliateRepository.save(any())).thenAnswer(i -> {
            Affiliate a = i.getArgument(0);
            a.setIdAffiliate(200L);
            return a;
        });

        Long result = service.createAffiliationTaxiDriverPila(dto);

        assertNotNull(result);
        assertEquals(200L, result);
        verify(policyService, times(2)).createPolicy(any(), any(), any(), any(), any(), any(), any());
        verify(repositoryAffiliation).save(any());
    }

    @Test
    void createAffiliationCouncillorPila_success() {
        NoveltyIndependentRequestDTO dto = new NoveltyIndependentRequestDTO();
        dto.setIdentificationDocumentType("CC");
        dto.setIdentificationDocumentNumber("321");
        dto.setStartDate(LocalDate.of(2023,2,1));
        dto.setEndDate(LocalDate.of(2023,11,30));
        dto.setDuration("10");
        dto.setContractMonthlyValue(BigDecimal.valueOf(2000));
        dto.setContributorTypeCode("CTC");
        dto.setContributantTypeCode(2);
        dto.setContributantSubtypeCode(2);

        UserMain userMain = new UserMain();
        userMain.setId(30L);
        userMain.setDateBirth(LocalDate.of(1975,5,5));
        userMain.setNationality(1L);
        userMain.setSex("M");
        userMain.setOtherSex(null);
        userMain.setPhoneNumber2("5555555");

        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification(any(), any())).thenReturn(Optional.of(userMain));
        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(userMain));
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("F789");
        when(affiliateRepository.save(any())).thenAnswer(i -> {
            Affiliate a = i.getArgument(0);
            a.setIdAffiliate(300L);
            return a;
        });

        Long result = service.createAffiliationCouncillorPila(dto);

        assertNotNull(result);
        assertEquals(300L, result);
        verify(policyService, times(2)).createPolicy(any(), any(), any(), any(), any(), any(), any());
        verify(repositoryAffiliation).save(any());
    }

}
