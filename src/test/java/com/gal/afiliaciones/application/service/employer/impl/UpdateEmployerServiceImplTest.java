package com.gal.afiliaciones.application.service.employer.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.application.service.KeycloakService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.retirementreason.impl.RetirementReasonServiceImpl;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.RequestUpdateDataBasicDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.RequestUpdateLegalRepresentativeDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.UpdateEmployerDataBasicDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.UpdateLegalRepresentativeDataDTO;
import com.gal.afiliaciones.infrastructure.dto.retirementreason.RegisteredAffiliationsDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.DataEmailUpdateEmployerDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

@ExtendWith(MockitoExtension.class)
class UpdateEmployerServiceImplTest {

    @Mock
    private RetirementReasonServiceImpl retirementReasonService;

    @Mock
    private AffiliateMercantileRepository affiliateMercantileRepository;

    @Mock
    private AffiliateRepository affiliateRepository;

    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository domesticRepository;

    @Mock
    private IEconomicActivityRepository economicActivityRepository;

    @Mock
    private IUserPreRegisterRepository userPreRegisterRepository;

    @Mock
    private SendEmails sendEmails;

    @Mock
    private KeycloakService keycloakService;

    @InjectMocks
    private UpdateEmployerServiceImpl updateEmployerService;

    private RequestUpdateDataBasicDTO requestUpdateDataBasicDTO;
    private RequestUpdateLegalRepresentativeDTO requestUpdateLegalRepresentativeDTO;
    private Affiliate affiliate;
    private AffiliateMercantile affiliateMercantile;
    private Affiliation affiliation;
    private UserMain userMain;

    @BeforeEach
    void setUp() {
        // Setup for common test data
        requestUpdateDataBasicDTO = new RequestUpdateDataBasicDTO();
        requestUpdateDataBasicDTO.setDocumentTypeEmployer("CC");
        requestUpdateDataBasicDTO.setDocumentNumberEmployer("123456789");
        requestUpdateDataBasicDTO.setAddressEmployer(new AddressDTO());
        requestUpdateDataBasicDTO.setPhone1Employer("1234567890");
        requestUpdateDataBasicDTO.setPhone2Employer("0987654321");
        requestUpdateDataBasicDTO.setEmailEmployer("test@example.com");
        
        requestUpdateLegalRepresentativeDTO = new RequestUpdateLegalRepresentativeDTO();
        requestUpdateLegalRepresentativeDTO.setTypeDocumentPersonResponsible("CC");
        requestUpdateLegalRepresentativeDTO.setNumberDocumentPersonResponsible("123456789");
        requestUpdateLegalRepresentativeDTO.setFirstName("John");
        requestUpdateLegalRepresentativeDTO.setSurname("Doe");
        requestUpdateLegalRepresentativeDTO.setEmail("new@example.com");
        requestUpdateLegalRepresentativeDTO.setEps(1L);
        requestUpdateLegalRepresentativeDTO.setAfp(1L);
        
        affiliate = new Affiliate();
        affiliate.setFiledNumber("FILE123");
        
        affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setFiledNumber("FILE123");
        affiliateMercantile.setBusinessName("Test Company");
        affiliateMercantile.setEmail("test@example.com");
        
        affiliation = new Affiliation();
        affiliation.setFirstName("John");
        affiliation.setSurname("Doe");
        affiliation.setEmail("test@example.com");
        
        userMain = new UserMain();
        userMain.setId(1L);
        userMain.setEmail("test@example.com");
    }

    @Test
    void updateEmployerDataBasic_DomesticServices_Success() {
        // Arrange
        requestUpdateDataBasicDTO.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(domesticRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(userMain));
        
        // Act
        Boolean result = updateEmployerService.updateEmployerDataBasic(requestUpdateDataBasicDTO);
        
        // Assert
        assertTrue(result);
        verify(domesticRepository).save(any(Affiliation.class));
        verify(userPreRegisterRepository).save(any(UserMain.class));
        verify(sendEmails).emailUpdateEmployer(any(DataEmailUpdateEmployerDTO.class));
    }

    @Test
    void updateEmployerDataBasic_MercantileEmployer_Success() {
        // Arrange
        requestUpdateDataBasicDTO.setAffiliationSubType(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);
        
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));
        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(userMain));
        
        // Act
        Boolean result = updateEmployerService.updateEmployerDataBasic(requestUpdateDataBasicDTO);
        
        // Assert
        assertTrue(result);
        verify(affiliateMercantileRepository).save(any(AffiliateMercantile.class));
        verify(userPreRegisterRepository).save(any(UserMain.class));
        verify(sendEmails).emailUpdateEmployer(any(DataEmailUpdateEmployerDTO.class));
    }

    @Test
    void searchEmployerDataBasic_DomesticServices_Success() {
        // Arrange
        String documentType = "CC";
        String documentNumber = "123456789";
        String affiliationSubType = Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES;
        
        List<RegisteredAffiliationsDTO> economicActivities = new ArrayList<>();
        economicActivities.add(RegisteredAffiliationsDTO.builder().build());
        
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(domesticRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(retirementReasonService.findEconomicActivitiesDomestic(any(Affiliation.class)))
            .thenReturn(economicActivities);
        
        // Act
        UpdateEmployerDataBasicDTO result = updateEmployerService.searchEmployerDataBasic(
            documentType, documentNumber, affiliationSubType);
        
        // Assert
        assertNotNull(result);
        assertEquals(affiliation.getEmail(), result.getEmailEmployer());
        verify(retirementReasonService).findEconomicActivitiesDomestic(any(Affiliation.class));
    }

    @Test
    void searchEmployerDataBasic_MercantileEmployer_Success() {
        // Arrange
        String documentType = "CC";
        String documentNumber = "123456789";
        String affiliationSubType = Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE;
        
        List<AffiliateMercantile> mercantileList = Collections.singletonList(affiliateMercantile);
        
        when(affiliateMercantileRepository.findAll(any(Specification.class))).thenReturn(mercantileList);
        when(affiliateMercantileRepository.findByFiledNumber(anyString())).thenReturn(Optional.of(affiliateMercantile));
        
        AffiliateActivityEconomic activityEconomic = mock(AffiliateActivityEconomic.class);
        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setId(1L);
        economicActivity.setClassRisk("1");
        economicActivity.setCodeCIIU("1234");
        economicActivity.setAdditionalCode("56");
        economicActivity.setDescription("Test Activity");
        
        when(activityEconomic.getActivityEconomic()).thenReturn(economicActivity);
        when(activityEconomic.getIsPrimary()).thenReturn(true);
        
        List<AffiliateActivityEconomic> activities = Collections.singletonList(activityEconomic);
        affiliateMercantile.setEconomicActivity(activities);
        
        List<EconomicActivity> economicActivities = Collections.singletonList(economicActivity);
        when(economicActivityRepository.findEconomicActivities(anyList())).thenReturn(economicActivities);
        
        // Act
        UpdateEmployerDataBasicDTO result = updateEmployerService.searchEmployerDataBasic(
            documentType, documentNumber, affiliationSubType);
        
        // Assert
        assertNotNull(result);
        assertEquals(affiliateMercantile.getEmailContactCompany(), result.getEmailEmployer());
    }

    @Test
    void searchLegalRepresentativeData_DomesticServices_Success() {
        // Arrange
        String documentType = "CC";
        String documentNumber = "123456789";
        String affiliationSubType = Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES;
        
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(domesticRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        
        // Act
        UpdateLegalRepresentativeDataDTO result = updateEmployerService.searchLegalRepresentativeData(
            documentType, documentNumber, affiliationSubType);
        
        // Assert
        assertNotNull(result);
        assertEquals(documentType, result.getTypeDocumentPersonResponsible());
        assertEquals(documentNumber, result.getNumberDocumentPersonResponsible());
    }

    @Test
    void searchLegalRepresentativeData_MercantileEmployer_Success() {
        // Arrange
        String documentType = "CC";
        String documentNumber = "123456789";
        String affiliationSubType = Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE;
        
        List<AffiliateMercantile> mercantileList = Collections.singletonList(affiliateMercantile);
        
        when(affiliateMercantileRepository.findAll(any(Specification.class))).thenReturn(mercantileList);
        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
            .thenReturn(Optional.of(userMain));
        
        // Act
        UpdateLegalRepresentativeDataDTO result = updateEmployerService.searchLegalRepresentativeData(
            documentType, documentNumber, affiliationSubType);
        
        // Assert
        assertNotNull(result);
        assertEquals(documentType, result.getTypeDocumentPersonResponsible());
        assertEquals(documentNumber, result.getNumberDocumentPersonResponsible());
    }

}