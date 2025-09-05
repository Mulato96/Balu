package com.gal.afiliaciones.application.service.consultationform.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.AffiliationEmployerDomesticServiceIndependentService;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.domain.model.Department;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.HistoryOptions;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.Policy;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.HistoryOptionsRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MainOfficeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.contractextension.ContractExtensionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.dto.consultationform.AffiliationInformationDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.HeadquartersAffiliationDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.PolicyDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.ViewingAssociatedDocumentsDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

@ExtendWith(MockitoExtension.class)
class ConsultEmployerInfoImplTest {

    @Mock
    private AffiliateRepository affiliateRepository;
    @Mock
    private IUserPreRegisterRepository userPreRegisterRepository;
    @Mock
    private AffiliateMercantileRepository affiliateMercantileRepository;
    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;
    @Mock
    private IEconomicActivityRepository economicActivityRepository;
    @Mock
    private GenericWebClient genericWebClient;
    @Mock
    private HistoryOptionsRepository historyOptionsRepository;
    @Mock
    private MainOfficeRepository mainOfficeRepository;
    @Mock
    private PolicyRepository policyRepository;
    @Mock
    private AffiliationEmployerDomesticServiceIndependentService affiliationEmployerDomesticServiceIndependentService;
    @Mock
    private ContractExtensionRepository contractExtensionRepository;
    @Mock
    private AffiliationDependentRepository affiliationDependentRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private MunicipalityRepository municipalityRepository;

    @InjectMocks
    private ConsultEmployerInfoImpl consultEmployerInfo;

    private Affiliate affiliate;
    private Policy policy;
    private AffiliateMercantile affiliateMercantile;
    private Affiliation affiliation;
    private UserMain userMain;
    private EconomicActivity economicActivity;
    private AffiliateActivityEconomic affiliateActivityEconomic;

    @BeforeEach
    void setUp() {
        affiliate = new Affiliate();
        affiliate.setIdAffiliate(1L);
        affiliate.setFiledNumber("123");
        affiliate.setAffiliationType("TYPE");
        affiliate.setNoveltyType("NOVELTY");
        affiliate.setAffiliationDate(LocalDateTime.now());
        affiliate.setNitCompany("123456");
        affiliate.setUserId(1L);
        affiliate.setAffiliationSubType("SUBTYPE");

        policy = new Policy();
        policy.setCode("POLICY123");
        policy.setEffectiveDateFrom(LocalDate.now());
        policy.setEffectiveDateTo(LocalDate.now().plusYears(1));

        economicActivity = new EconomicActivity();
        economicActivity.setId(1L);
        economicActivity.setClassRisk("1");
        economicActivity.setCodeCIIU("1234");
        economicActivity.setAdditionalCode("56");
        economicActivity.setDescription("Test Economic Activity");

        affiliateActivityEconomic = new AffiliateActivityEconomic();
        affiliateActivityEconomic.setActivityEconomic(economicActivity);
        affiliateActivityEconomic.setIsPrimary(true);
        
        affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setId(1L);
        affiliateMercantile.setFiledNumber("123");
        affiliateMercantile.setTypeDocumentIdentification("CC");
        affiliateMercantile.setNumberIdentification("123456789");
        affiliateMercantile.setBusinessName("Test Company");
        affiliateMercantile.setTypeAffiliation("EMPLOYER");
        affiliateMercantile.setAfp(1L);
        affiliateMercantile.setEps(1L);
        affiliateMercantile.setDateCreateAffiliate(LocalDate.now());
        affiliateMercantile.setTypeDocumentPersonResponsible("CC");
        affiliateMercantile.setNumberDocumentPersonResponsible("987654321");
        affiliateMercantile.setEconomicActivity(List.of(affiliateActivityEconomic));

        affiliation = new Affiliation();
        affiliation.setId(1L);
        affiliation.setFiledNumber("123");
        affiliation.setIdentificationDocumentType("CC");
        affiliation.setIdentificationDocumentNumber("123456789");
        affiliation.setFirstName("John");
        affiliation.setSecondName("Doe");
        affiliation.setSurname("Smith");
        affiliation.setSecondSurname("Johnson");
        affiliation.setDateOfBirth(LocalDate.of(1990, 1, 1));
        affiliation.setGender("M");
        affiliation.setNationality(1L);
        affiliation.setPensionFundAdministrator(1L);
        affiliation.setHealthPromotingEntity(1L);
        affiliation.setCompanyName("Test Company");
        affiliation.setTypeAffiliation("EMPLOYER");
        affiliation.setCodeMainEconomicActivity("1");

        userMain = new UserMain();
        userMain.setFirstName("John");
        userMain.setSecondName("Doe");
        userMain.setSurname("Smith");
        userMain.setSecondSurname("Johnson");
        userMain.setDateBirth(LocalDate.of(1990, 1, 1));
        userMain.setSex("M");
        userMain.setNationality(1L);
    }

    @Test
    void getPolicyInfo_ShouldReturnPolicyDtoList() {
        // Arrange
        when(affiliateRepository.findByFiledNumber(anyString())).thenReturn(Optional.of(affiliate));
        when(policyRepository.findByIdAffiliate(anyLong())).thenReturn(List.of(policy));

        // Act
        List<PolicyDTO> result = consultEmployerInfo.getPolicyInfo("123");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        PolicyDTO policyDTO = result.get(0);
        assertEquals(policy.getCode(), policyDTO.getPolicyNumber());
        assertEquals(affiliate.getAffiliationType(), policyDTO.getBonding());
        assertEquals(policy.getEffectiveDateFrom(), policyDTO.getValidityFrom());
        assertEquals(policy.getEffectiveDateTo(), policyDTO.getValidityTo());
        
        // Verify
        verify(affiliateRepository).findByFiledNumber("123");
        verify(policyRepository).findByIdAffiliate(1L);
    }

    @Test
    void getPolicyInfo_WhenAffiliateNotFound_ShouldThrowException() {
        // Arrange
        when(affiliateRepository.findByFiledNumber(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AffiliateNotFoundException.class, 
            () -> consultEmployerInfo.getPolicyInfo("123"));
        
        // Verify
        verify(affiliateRepository).findByFiledNumber("123");
        verifyNoInteractions(policyRepository);
    }

    @Test
    void getHistoryOptions_ShouldReturnAllHistoryOptions() {
        // Arrange
        List<HistoryOptions> expectedOptions = List.of(new HistoryOptions());
        when(historyOptionsRepository.findAll()).thenReturn(expectedOptions);

        // Act
        List<HistoryOptions> result = consultEmployerInfo.getHistoryOptions();

        // Assert
        assertSame(expectedOptions, result);
        
        // Verify
        verify(historyOptionsRepository).findAll();
    }

    @Test
    void getAffiliationInfoEmployeer_ForMercantileType_ShouldReturnDTO() {
        // Arrange
        affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);
        
        when(affiliateRepository.findByFiledNumber(anyString())).thenReturn(Optional.of(affiliate));
        when(affiliateMercantileRepository.findByFiledNumber(anyString())).thenReturn(Optional.of(affiliateMercantile));
        when(userPreRegisterRepository.findByIdentificationTypeAndIdentification(anyString(), anyString()))
            .thenReturn(Optional.of(userMain));

        // Act
        AffiliationInformationDTO result = consultEmployerInfo.getAffiliationInfoEmployeer("123");

        // Assert
        assertNotNull(result);
        assertEquals(affiliateMercantile.getTypeDocumentIdentification(), result.getDocumentType());
        assertEquals(affiliateMercantile.getNumberIdentification(), result.getDocumentNumber());
        assertEquals(userMain.getFirstName(), result.getFirstName());
        assertEquals(affiliateMercantile.getBusinessName(), result.getCompanyName());

        // Verify
        verify(affiliateRepository).findByFiledNumber("123");
        verify(affiliateMercantileRepository).findByFiledNumber("123");
        verify(userPreRegisterRepository).findByIdentificationTypeAndIdentification(
            affiliateMercantile.getTypeDocumentPersonResponsible(),
            affiliateMercantile.getNumberDocumentPersonResponsible());
    }

    @Test
    void getAffiliationInfoEmployeer_ForNonMercantileType_ShouldReturnDTO() {
        // Arrange
        affiliate.setAffiliationSubType("OTHER_TYPE");
        
        when(affiliateRepository.findByFiledNumber(anyString())).thenReturn(Optional.of(affiliate));
        when(affiliationRepository.findByFiledNumber(anyString())).thenReturn(Optional.of(affiliation));
        when(economicActivityRepository.findById(anyLong())).thenReturn(Optional.of(economicActivity));

        // Act
        AffiliationInformationDTO result = consultEmployerInfo.getAffiliationInfoEmployeer("123");

        // Assert
        assertNotNull(result);
        assertEquals(affiliation.getIdentificationDocumentType(), result.getDocumentType());
        assertEquals(affiliation.getIdentificationDocumentNumber(), result.getDocumentNumber());
        assertEquals(affiliation.getFirstName(), result.getFirstName());
        assertEquals(affiliation.getCompanyName(), result.getCompanyName());

        // Verify
        verify(affiliateRepository).findByFiledNumber("123");
        verify(affiliationRepository).findByFiledNumber("123");
        verify(economicActivityRepository).findById(Long.valueOf(affiliation.getCodeMainEconomicActivity()));
    }

    @Test
    void getHeadquarters_ShouldReturnHeadquartersList() {
        // Arrange
        MainOffice mainOffice = new MainOffice();
        mainOffice.setCode("OFFICE1");
        mainOffice.setMainOfficeName("Headquarters 1");
        mainOffice.setIdDepartment(1L);
        mainOffice.setIdCity(1L);
        mainOffice.setAddress("123 Main St");

        Department department = new Department();
        department.setDepartmentName("Department 1");

        Municipality municipality = new Municipality();
        municipality.setMunicipalityName("Municipality 1");

        when(affiliateRepository.findByFiledNumber(anyString())).thenReturn(Optional.of(affiliate));
        when(mainOfficeRepository.findByOfficeManager_Id(anyLong())).thenReturn(List.of(mainOffice));
        when(departmentRepository.findById(anyLong())).thenReturn(Optional.of(department));
        when(municipalityRepository.findById(anyLong())).thenReturn(Optional.of(municipality));

        // Act
        List<HeadquartersAffiliationDTO> result = consultEmployerInfo.getHeadquarters("123");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        HeadquartersAffiliationDTO dto = result.get(0);
        assertEquals(mainOffice.getMainOfficeName(), dto.getBranch());
        assertEquals(mainOffice.getCode(), dto.getBranchId());
        assertEquals(department.getDepartmentName(), dto.getDepartment());
        assertEquals(municipality.getMunicipalityName(), dto.getCityOrMunicipality());
        assertEquals(mainOffice.getAddress(), dto.getFullAddress());

        // Verify
        verify(affiliateRepository).findByFiledNumber("123");
        verify(mainOfficeRepository).findByOfficeManager_Id(affiliate.getUserId());
        verify(departmentRepository).findById(mainOffice.getIdDepartment());
        verify(municipalityRepository).findById(mainOffice.getIdCity());
    }

    @Test
    void getOldestDocumentDate_ShouldReturnOldestDate() {
        // Arrange
        ViewingAssociatedDocumentsDTO doc1 = new ViewingAssociatedDocumentsDTO();
        doc1.setCreatedDate("2022-05-15");
        
        ViewingAssociatedDocumentsDTO doc2 = new ViewingAssociatedDocumentsDTO();
        doc2.setCreatedDate("2021-03-10");
        
        ViewingAssociatedDocumentsDTO doc3 = new ViewingAssociatedDocumentsDTO();
        doc3.setCreatedDate("2023-01-20");
        
        List<ViewingAssociatedDocumentsDTO> documents = List.of(doc1, doc2, doc3);

        // Act
        LocalDate result = consultEmployerInfo.getOldestDocumentDate(documents);

        // Assert
        assertNotNull(result);
        assertEquals(LocalDate.parse("2021-03-10"), result);
    }

    @Test
    void getOldestDocumentDate_WithNullOrEmptyDocuments_ShouldReturnNull() {
        // Act & Assert
        assertNull(consultEmployerInfo.getOldestDocumentDate(null));
        assertNull(consultEmployerInfo.getOldestDocumentDate(List.of()));
        
        // With empty dates
        ViewingAssociatedDocumentsDTO doc = new ViewingAssociatedDocumentsDTO();
        doc.setCreatedDate("");
        assertNull(consultEmployerInfo.getOldestDocumentDate(List.of(doc)));
    }
}