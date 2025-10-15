package com.gal.afiliaciones.application.service.consultationform.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Collections;

import com.gal.afiliaciones.infrastructure.dao.repository.IDataDocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.domesticservicesaffiliation.DomesticServicesAffiliationRepositoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.AffiliationEmployerDomesticServiceIndependentService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.Occupation;
import com.gal.afiliaciones.domain.model.Policy;
import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.conciliationbilling.BillingCollectionConciliationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.BillingRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirement.RetirementRepository;
import com.gal.afiliaciones.infrastructure.dto.consultationform.DocumentsOfAffiliationDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.InfoConsultDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker.HistoryAffiliationsWithdrawalsHistoryDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker.HistoryJobRelatedDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker.UpdatesWorkerHistoryDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ConsultationFormServiceImplTest {

    @Mock
    private AffiliateRepository affiliateRepository;

    @Mock
    private IUserPreRegisterRepository userPreRegisterRepository;

    @Mock
    private AffiliateMercantileRepository affiliateMercantileRepository;

    @Mock
    private AffiliationDependentRepository affiliationDependentRepository;

    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;

    @Mock
    private AffiliationEmployerDomesticServiceIndependentService affiliationEmployerDomesticServiceIndependentService;

    @Mock
    private GenericWebClient genericWebClient;

    @Mock
    private BillingRepository billingRepository;

    @Mock
    private BillingCollectionConciliationRepository billingCollectionConciliationRepository;

    @Mock
    private RetirementRepository retirementRepository;

    @Mock
    private OccupationRepository occupationRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private DomesticServicesAffiliationRepositoryRepository affiliationEmployerDomesticServiceIndependent;

    @Mock
    private IDataDocumentRepository dataDocumentRepository;

    @InjectMocks
    private ConsultationFormServiceImpl consultationFormService;

    private Affiliate testAffiliate;
    private List<Affiliate> affiliateList;
    private AffiliationDependent affiliationDependent;
    private Affiliation affiliation;
    private AffiliateMercantile affiliateMercantile;
    private UserMain userMain;
    private Retirement retirement;

    @BeforeEach
    void setUp() {
        // Set up test data
        testAffiliate = new Affiliate();
        testAffiliate.setIdAffiliate(1L);
        testAffiliate.setDocumentType("CC");
        testAffiliate.setDocumentNumber("123456789");
        testAffiliate.setAffiliationType("DEPENDENT");
        testAffiliate.setAffiliationSubType("EMPLOYEE");
        testAffiliate.setAffiliationStatus("ACTIVE");
        testAffiliate.setAffiliationDate(new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        testAffiliate.setCoverageStartDate(LocalDate.now());
        testAffiliate.setCompany("Cesar S.A");
        testAffiliate.setNitCompany("901538337");
        testAffiliate.setFiledNumber("FILE-123");
        testAffiliate.setNoveltyType("Afiliación");

        affiliateList = List.of(testAffiliate);

        // Setup AffiliationDependent
        affiliationDependent = new AffiliationDependent();
        affiliationDependent.setIdentificationDocumentType("CC");
        affiliationDependent.setIdentificationDocumentNumber("123456789");
        affiliationDependent.setFirstName("John");
        affiliationDependent.setSecondName("Doe");
        affiliationDependent.setSurname("Smith");
        affiliationDependent.setSecondSurname("Jones");
        affiliationDependent.setDateOfBirth(LocalDate.of(1990, 1, 1));
        affiliationDependent.setGender("M");
        affiliationDependent.setIdDepartment(1L);
        affiliationDependent.setIdCity(1L);
        affiliationDependent.setAddress("123 Main St");
        affiliationDependent.setHealthPromotingEntity(1L);
        affiliationDependent.setPensionFundAdministrator(1L);
        affiliationDependent.setPhone1("123456789");
        affiliationDependent.setPhone2("987654321");
        affiliationDependent.setEmail("john.doe@example.com");
        affiliationDependent.setStartDate(LocalDate.now());
        affiliationDependent.setRisk(1);
        affiliationDependent.setPriceRisk(BigDecimal.valueOf(2.5));
        affiliationDependent.setIdOccupation(1L);

        // Setup Affiliation
        affiliation = new Affiliation();
        affiliation.setIdentificationDocumentType("CC");
        affiliation.setIdentificationDocumentNumber("123456789");
        affiliation.setFirstName("John");
        affiliation.setSecondName("Doe");
        affiliation.setSurname("Smith");
        affiliation.setSecondSurname("Jones");
        affiliation.setDateOfBirth(LocalDate.of(1990, 1, 1));
        affiliation.setGender("M");
        affiliation.setDepartment(1L);
        affiliation.setCityMunicipality(1L);
        affiliation.setAddress("123 Main St");
        affiliation.setHealthPromotingEntity(1L);
        affiliation.setPensionFundAdministrator(1L);
        affiliation.setPhone1("123456789");
        affiliation.setPhone2("987654321");
        affiliation.setEmail("john.doe@example.com");
        affiliation.setStartDate(LocalDate.now());
        affiliation.setOccupation("Developer");
        affiliation.setRisk("1");
        affiliation.setPrice(BigDecimal.valueOf(2.5));
        affiliation.setDv(1);
        affiliation.setLegalRepFirstName("Legal");
        affiliation.setLegalRepSecondName("Rep");
        affiliation.setLegalRepSurname("Last");
        affiliation.setLegalRepSecondSurname("Name");
        affiliation.setNameLegalNatureEmployer("SAS");
        
        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setEconomicActivityCode("1234");
        
        AffiliateActivityEconomic affiliateActivityEconomic = new AffiliateActivityEconomic();
        affiliateActivityEconomic.setActivityEconomic(economicActivity);
        affiliateActivityEconomic.setIsPrimary(true);
        
        List<AffiliateActivityEconomic> activitySet = new ArrayList<>();
        activitySet.add(affiliateActivityEconomic);
        affiliation.setEconomicActivity(activitySet);

        // Setup AffiliateMercantile
        affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setNumberIdentification("987654321");
        affiliateMercantile.setDigitVerificationDV(1);
        affiliateMercantile.setDepartment(1L);
        affiliateMercantile.setCityMunicipality(1L);
        affiliateMercantile.setAddress("123 Main St");
        affiliateMercantile.setPhoneOne("123456789");
        affiliateMercantile.setPhoneTwo("987654321");
        affiliateMercantile.setEmail("company@example.com");
        affiliateMercantile.setTypeDocumentPersonResponsible("CC");
        affiliateMercantile.setNumberDocumentPersonResponsible("123456789");
        affiliateMercantile.setLegalStatus("SAS");
        affiliateMercantile.setEconomicActivity(activitySet);

        // Setup UserMain
        userMain = new UserMain();
        userMain.setFirstName("Legal");
        userMain.setSecondName("Rep");
        userMain.setSurname("Last");
        userMain.setSecondSurname("Name");
        
        // Setup Retirement
        retirement = new Retirement();
        retirement.setIdAffiliate(1L);
        retirement.setRetirementDate(LocalDate.now());
        retirement.setFiledNumber("RET-123");
        
        // Setup Occupation
        Occupation occupation = new Occupation();
        occupation.setNameOccupation("Developer");
    }

    @Test
    void testGetInfo_independentAffiliate_returnsWorkerInfoDTO() {
        // Arrange
        String typeId = "CC";
        String idNumber = "123456";
        String affiliationType = Constant.EMPLOYEE;

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(typeId, idNumber))
                .thenReturn(List.of(affiliate));

        UserMain user = new UserMain();
        user.setIdentificationType(typeId);
        user.setIdentification(idNumber);
        user.setStatusActive(true);
        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));

        // Act
        InfoConsultDTO result = consultationFormService.getInfo(typeId, idNumber, affiliationType, null);

        // Assert
        assertNotNull(result);
        assertEquals(typeId, ((com.gal.afiliaciones.infrastructure.dto.consultationform.WorkerBasicInfoDTO) result).getDocumentType());
    }

    @Test
    void testGetInfo_affiliateNotFound_throwsAffiliationNotFoundError() {
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "123"))
                .thenReturn(List.of());

        assertThrows(AffiliationNotFoundError.class, () -> {
            consultationFormService.getInfo("CC", "123", Constant.EMPLOYEE, null);
        });
    }

    @Test
    void testGetInfo_independentAffiliate_userNotFound_throwsUserNotFoundInDataBase() {
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "456"))
                .thenReturn(List.of(affiliate));

        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThrows(UserNotFoundInDataBase.class, () -> {
            consultationFormService.getInfo("CC", "456", Constant.EMPLOYEE, null);
        });
    }
        
    @Test
    void getHistoryJobRelated_WhenIndependentAffiliate_ShouldReturnHistoryJobRelatedDTO() {
        // Arrange
        String filedNumber = "FILE-123";
        testAffiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        
        when(affiliateRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.of(testAffiliate));
        when(affiliationRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.of(affiliation));
        when(billingRepository.findByContributorId(testAffiliate.getDocumentNumber())).thenReturn(Collections.emptyList());
        
        // Act
        HistoryJobRelatedDTO result = consultationFormService.getHistoryJobRelated(filedNumber);
        
        // Assert
        assertNotNull(result);
        assertEquals(testAffiliate.getAffiliationStatus(), result.getAffiliationStatus());
        assertEquals(testAffiliate.getAffiliationSubType(), result.getTypeOfLinkage());
        assertEquals(Integer.parseInt(affiliation.getRisk()), result.getRiskLevel());
        assertEquals(affiliation.getPrice(), result.getRate());
        
        // Verify
        verify(affiliateRepository).findByFiledNumber(filedNumber);
        verify(affiliationRepository).findByFiledNumber(filedNumber);
        verify(billingRepository).findByContributorId(testAffiliate.getDocumentNumber());
    }
    
    @Test
    void getUpdatesWorkerHistory_ShouldReturnUpdatesWorkerHistoryDTO() {
        // Arrange
        String filedNumber = "FILE-123";
        Policy policy = new Policy();
        policy.setId(1L);
        List<Policy> policyList = List.of(policy);
        
        when(affiliateRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.of(testAffiliate));
        when(policyRepository.findByIdAffiliate(testAffiliate.getIdAffiliate())).thenReturn(policyList);
        when(billingRepository.findByPolicy_Id(policy.getId())).thenReturn(Optional.empty());
        
        // Act
        UpdatesWorkerHistoryDTO result = consultationFormService.getUpdatesWorkerHistory(filedNumber);
        
        // Assert
        assertNotNull(result);
        assertEquals("Portal", result.getChannel());
        assertEquals(testAffiliate.getNoveltyType(), result.getNoveltyType());
        assertEquals("No", result.getRetirementNovelty());
        assertEquals(testAffiliate.getFiledNumber(), result.getRecordNumber());
        
        // Verify
        verify(affiliateRepository).findByFiledNumber(filedNumber);
        verify(policyRepository).findByIdAffiliate(testAffiliate.getIdAffiliate());
        verify(billingRepository).findByPolicy_Id(policy.getId());
    }
    
    @Test
    void getDocumentAffiliationWorker_WhenIndependentAffiliate_ShouldReturnDocumentsOfAffiliationDTO() {
        // Arrange
        String filedNumber = "FILE-123";
        testAffiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        List<DataDocumentAffiliate> documents = new ArrayList<>();
        DataDocumentAffiliate document = new DataDocumentAffiliate();
        document.setIdAlfresco("alfresco123");
        documents.add(document);
        
        when(affiliateRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.of(testAffiliate));
        when(affiliationEmployerDomesticServiceIndependentService.findDocuments(testAffiliate.getIdAffiliate())).thenReturn(documents);
        when(affiliationRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.of(affiliation));
        when(genericWebClient.getFileBase64(anyString())).thenReturn(Mono.just("base64data"));
        
        // Act
        DocumentsOfAffiliationDTO result = consultationFormService.getDocumentAffiliationWorker(filedNumber);
        
        // Assert
        assertNotNull(result);
        assertEquals(affiliation.getIdentificationDocumentType(), result.getDocumentType());
        assertEquals(affiliation.getIdentificationDocumentNumber(), result.getDocumentNumber());
        assertEquals(affiliation.getFirstName(), result.getFirstName());
        assertFalse(result.getDocumentIds().isEmpty());
        
        // Verify
        verify(affiliateRepository).findByFiledNumber(filedNumber);
        verify(affiliationEmployerDomesticServiceIndependentService).findDocuments(testAffiliate.getIdAffiliate());
        verify(affiliationRepository).findByFiledNumber(filedNumber);
        verify(genericWebClient).getFileBase64(anyString());
    }
    
    @Test
    void getAffiliationWithdrawalsHistory_WhenIndependentAffiliate_ShouldReturnHistoryDTO() {
        // Arrange
        String filedNumber = "FILE-123";
        testAffiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        
        when(affiliateRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.of(testAffiliate));
        when(affiliationRepository.findByFiledNumber(filedNumber)).thenReturn(Optional.of(affiliation));
        
        // Act
        HistoryAffiliationsWithdrawalsHistoryDTO result = consultationFormService.getAffiliationWithdrawalsHistory(filedNumber);
        
        // Assert
        assertNotNull(result);
        assertEquals("Portal", result.getChannel());
        assertEquals(affiliation.getHealthPromotingEntity(), result.getEps());
        assertEquals(affiliation.getOccupation(), result.getOcupation());
        assertEquals(affiliation.getAddress(), result.getAddress());
        
        // Verify
        verify(affiliateRepository).findByFiledNumber(filedNumber);
        verify(affiliationRepository).findByFiledNumber(filedNumber);
    }
}