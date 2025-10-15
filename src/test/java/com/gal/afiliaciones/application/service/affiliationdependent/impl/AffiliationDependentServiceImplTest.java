package com.gal.afiliaciones.application.service.affiliationdependent.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.LocalDate;
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

import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.policy.PolicyService;
import com.gal.afiliaciones.application.service.risk.RiskFeeService;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationAlreadyExistsError;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.config.util.MessageErrorAge;
import com.gal.afiliaciones.domain.model.BondingTypeDependent;
import com.gal.afiliaciones.domain.model.WorkModality;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.dependent.BondingTypeDependentDao;
import com.gal.afiliaciones.infrastructure.dao.repository.dependent.WorkModalityDao;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.AffiliationDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.DependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.validatecontributorelationship.ValidateContributorRequest;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.service.RegistraduriaUnifiedService;

@ExtendWith(MockitoExtension.class)
class AffiliationDependentServiceImplTest {

    @Mock
    private BondingTypeDependentDao bondingTypeDependentDao;
    @Mock
    private IUserPreRegisterRepository iUserPreRegisterRepository;
    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    @Mock
    private AffiliationDependentRepository dependentRepository;
    @Mock
    private AffiliateRepository affiliateRepository;
    @Mock
    private WorkModalityDao workModalityDao;
    @Mock
    private GenericWebClient webClient;
    @Mock
    private AffiliateService affiliateService;
    @Mock
    private FiledService filedService;
    @Mock
    private RiskFeeService riskFeeService;
    @Mock
    private PolicyRepository policyRepository;
    @Mock
    private PolicyService policyService;
    @Mock
    private GenerateCardAffiliatedService cardAffiliatedService;
    @Mock
    private SendEmails sendEmails;
    @Mock
    private CollectProperties properties;
    @Mock
    private MessageErrorAge messageError;
    
    @Mock
    private RegistraduriaUnifiedService registraduriaUnifiedService;

    @InjectMocks
    private AffiliationDependentServiceImpl affiliationDependentService;

    private List<BondingTypeDependent> bondingTypeDependents;
    private List<WorkModality> workModalities;
    private ValidateContributorRequest request;
    private AffiliationDependentDTO affiliationDependentDTO;
    private List<Affiliate> affiliates;

    @BeforeEach
    void setUp() {
        // Initialize test data
        bondingTypeDependents = new ArrayList<>();
        BondingTypeDependent bondingTypeDependent = new BondingTypeDependent();
        bondingTypeDependent.setId(1L);
        bondingTypeDependents.add(bondingTypeDependent);

        workModalities = new ArrayList<>();
        WorkModality workModality = new WorkModality();
        workModality.setId(1L);
        workModalities.add(workModality);

        request = new ValidateContributorRequest();
        request.setEmployerIdentificationType("CC");
        request.setEmployerIdentificationNumber("123456789");
        request.setEmployeeIdentificationType("CC");
        request.setEmployeeIdentificationNumber("987654321");

        affiliationDependentDTO = new AffiliationDependentDTO();
        affiliationDependentDTO.setIdAffiliation(0L);
        affiliationDependentDTO.setIdBondingType(1L);
        affiliationDependentDTO.setIdentificationTypeEmployer("CC");
        affiliationDependentDTO.setIdentificationNumberEmployer("123456789");
        affiliationDependentDTO.setIdAffiliateEmployer(123L);
        affiliationDependentDTO.setCoverageDate(LocalDate.now());
        affiliationDependentDTO.setFromPila(false);
        
        DependentWorkerDTO workerDTO = new DependentWorkerDTO();
        workerDTO.setIdentificationDocumentType("CC");
        workerDTO.setIdentificationDocumentNumber("987654321");
        workerDTO.setDateOfBirth(LocalDate.of(1990, 1, 1));
        workerDTO.setOccupationalRiskManager(Constant.CODE_ARL);
        AddressDTO addressDTO = new AddressDTO();
        workerDTO.setAddress(addressDTO);
        affiliationDependentDTO.setWorker(workerDTO);
        affiliationDependentDTO.setEconomicActivityCode("1234567");

        affiliates = new ArrayList<>();
        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(1L);
        affiliate.setDocumentType("CC");
        affiliate.setDocumentNumber("123456789");
        affiliate.setNitCompany("123456789");
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        affiliate.setCompany("Test Company");
        affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);
        affiliates.add(affiliate);
    }

    @Test
    void findAll_ShouldReturnAllBondingTypes() {
        // Arrange
        when(bondingTypeDependentDao.findAll()).thenReturn(bondingTypeDependents);
        
        // Act
        List<BondingTypeDependent> result = affiliationDependentService.findAll();
        
        // Assert
        assertEquals(bondingTypeDependents, result);
        verify(bondingTypeDependentDao).findAll();
    }

    @Test
    void findAlllWorkModalities_ShouldReturnAllWorkModalities() {
        // Arrange
        when(workModalityDao.findAll()).thenReturn(workModalities);
        
        // Act
        List<WorkModality> result = affiliationDependentService.findAlllWorkModalities();
        
        // Assert
        assertEquals(workModalities, result);
        verify(workModalityDao).findAll();
    }

    @Test
    void calculateAge_WithValidDate_ShouldReturnCorrectAge() throws Exception {
        // Arrange
        LocalDate birthDate = LocalDate.now().minusYears(30);
        
        // Use reflection to access the private method
        Method calculateAgeMethod = AffiliationDependentServiceImpl.class
            .getDeclaredMethod("calculateAge", LocalDate.class);
        calculateAgeMethod.setAccessible(true);
        
        // Act
        Integer age = (Integer) calculateAgeMethod.invoke(affiliationDependentService, birthDate);
        
        // Assert
        assertEquals(30, age);
    }

    @Test
    void calculateAge_WithNullDate_ShouldReturnZero() throws Exception {
        // Use reflection to access the private method
        Method calculateAgeMethod = AffiliationDependentServiceImpl.class
            .getDeclaredMethod("calculateAge", LocalDate.class);
        calculateAgeMethod.setAccessible(true);
        
        // Act
        Integer age = (Integer) calculateAgeMethod.invoke(affiliationDependentService, (LocalDate) null);
        
        // Assert
        assertEquals(0, age);
    }

    @Test
    void preloadUserNotExists_WhenUserNotFound_ShouldReturnBasicInfo() {
        // Arrange
        when(repositoryAffiliation.findAll(any(Specification.class))).thenReturn(new ArrayList<>());
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        
        // Act
        DependentWorkerDTO result = affiliationDependentService.preloadUserNotExists(request);
        
        // Assert
        assertEquals(request.getEmployeeIdentificationType(), result.getIdentificationDocumentType());
        assertEquals(request.getEmployeeIdentificationNumber(), result.getIdentificationDocumentNumber());
        assertFalse(result.getUserFromRegistry());
        verify(repositoryAffiliation).findAll(any(Specification.class));
        verify(iUserPreRegisterRepository).findOne(any(Specification.class));
    }


    @Test
    void capitalize_WithValidString_ShouldReturnCapitalizedString() throws Exception {

        Method capitalizeMethod = AffiliationDependentServiceImpl.class
            .getDeclaredMethod("capitalize", String.class);
        capitalizeMethod.setAccessible(true);
        
        // Act
        String result = (String) capitalizeMethod.invoke(affiliationDependentService, (String) "test");
        
        // Assert
        assertEquals("Test", result);
    }

    @Test
    void capitalize_WithNullString_ShouldReturnEmptyString() throws Exception{

        Method capitalizeMethod = AffiliationDependentServiceImpl.class
            .getDeclaredMethod("capitalize", String.class);
        capitalizeMethod.setAccessible(true);
        
        // Act
        String result = (String) capitalizeMethod.invoke(affiliationDependentService, (String) null);
        
        // Assert
        assertEquals("", result);
    }

    @Test
    void capitalize_WithEmptyString_ShouldReturnEmptyString() throws Exception {

        Method capitalizeMethod = AffiliationDependentServiceImpl.class
            .getDeclaredMethod("capitalize", String.class);
        capitalizeMethod.setAccessible(true);
        
        // Act
        String result = (String) capitalizeMethod.invoke(affiliationDependentService, (String) "");
        
        // Assert
        assertEquals("", result);
    }
}