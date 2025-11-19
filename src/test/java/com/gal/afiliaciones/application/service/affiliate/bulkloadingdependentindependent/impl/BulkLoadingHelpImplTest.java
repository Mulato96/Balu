package com.gal.afiliaciones.application.service.affiliate.bulkloadingdependentindependent.impl;


import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliate.recordloadbulk.RecordLoadBulkService;
import com.gal.afiliaciones.application.service.affiliationdependent.impl.AffiliationDependentServiceImpl;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.policy.PolicyService;
import com.gal.afiliaciones.application.service.risk.RiskFeeService;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.Policy;
import com.gal.afiliaciones.domain.model.Smlmv;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.SmlmvRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.DataExcelDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.bulkloadingdependentindependent.DataExcelIndependentDTO;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class BulkLoadingHelpImplTest {

    @Mock FiledService filedService;
    @Mock RiskFeeService riskFeeService;
    @Mock PolicyService policyService;
    @Mock AffiliateService affiliateService;
    @Mock PolicyRepository policyRepository;
    @Mock AffiliateRepository affiliateRepository;
    @Mock OccupationRepository occupationRepository;
    @Mock MunicipalityRepository municipalityRepository;
    @Mock AffiliationDependentServiceImpl dependentService;
    @Mock AffiliationDependentRepository dependentRepository;
    @Mock GenerateCardAffiliatedService cardAffiliatedService;
    @Mock IUserPreRegisterRepository iUserPreRegisterRepository;
    @Mock IEconomicActivityRepository iEconomicActivityRepository;
    @Mock AffiliateMercantileRepository affiliateMercantileRepository;
    @Mock RecordLoadBulkService recordLoadBulkService;
    @Mock GenericWebClient genericWebClient;
    @Mock SmlmvRepository smlmvRepository;

    @InjectMocks BulkLoadingHelpImpl service;

    @Test
    void affiliateDataDependent(){

        List<DataExcelDependentDTO> listDependent = listDependent();
        List<Municipality> allMunicipality = allMunicipality();
        List<EconomicActivity> allActivities = allActivities();
        List<Policy> listPolicy = listPolicy();
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        Affiliate affiliate1 = new Affiliate();
        Affiliate affiliation = new Affiliate();
        AffiliationDependent affiliationDependent = new AffiliationDependent();
        UserMain userMain = new UserMain();
        Smlmv smlmv =  new Smlmv();

        affiliate1.setIdAffiliate(1L);
        affiliate1.setFiledNumber("123");

        when(municipalityRepository.findAll())
                .thenReturn(allMunicipality);
        when(iEconomicActivityRepository.findAll())
                .thenReturn(allActivities);
        when(iUserPreRegisterRepository.findOne((Specification<UserMain>) any()))
                .thenReturn(Optional.of(userMain));
        when( smlmvRepository.findByValidDate(LocalDateTime.now()))
                .thenReturn(Optional.of(smlmv));
        when(riskFeeService.getFeeByRisk("1"))
                .thenReturn(BigDecimal.valueOf(1));
        when(affiliateService.createAffiliate(any()))
                .thenReturn(affiliation);
        when(dependentRepository.save(any()))
                .thenReturn(affiliationDependent);
        when(affiliateRepository.findById(1L))
                .thenReturn(Optional.of(affiliate1));
        when(policyRepository.findByIdAffiliate(1L))
                .thenReturn(listPolicy);
        when(affiliateMercantileRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliateMercantile));

        service.affiliateData(listDependent, null, "Trabajador Dependiente", affiliate1, 1L, 1L);
        verify(dependentRepository, times(1)).save(any());
    }

    @Test
    void affiliateDataDependent_(){

        List<DataExcelDependentDTO> listDependent = listDependent()
                .stream()
                .peek(e -> e.setIdOccupation("1078"))
                .toList();
        List<Municipality> allMunicipality = allMunicipality();
        List<EconomicActivity> allActivities = allActivities();
        List<Policy> listPolicy = listPolicy();
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        Affiliate affiliate1 = new Affiliate();
        Affiliate affiliation = new Affiliate();
        AffiliationDependent affiliationDependent = new AffiliationDependent();
        UserMain userMain = new UserMain();
        Smlmv smlmv =  new Smlmv();

        affiliate1.setIdAffiliate(1L);
        affiliate1.setFiledNumber("123");

        when(municipalityRepository.findAll())
                .thenReturn(allMunicipality);
        when(iEconomicActivityRepository.findAll())
                .thenReturn(allActivities);
        when(iUserPreRegisterRepository.findOne((Specification<UserMain>) any()))
                .thenReturn(Optional.of(userMain));
        when( smlmvRepository.findByValidDate(LocalDateTime.now()))
                .thenReturn(Optional.of(smlmv));
        when(riskFeeService.getFeeByRisk("1"))
                .thenReturn(BigDecimal.valueOf(1));
        when(affiliateService.createAffiliate(any()))
                .thenReturn(affiliation);
        when(dependentRepository.save(any()))
                .thenReturn(affiliationDependent);
        when(affiliateRepository.findById(1L))
                .thenReturn(Optional.of(affiliate1));
        when(policyRepository.findByIdAffiliate(1L))
                .thenReturn(listPolicy);
        when(affiliateMercantileRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliateMercantile));

        service.affiliateData(listDependent, null, "Trabajador Dependiente", affiliate1, 1L, 1L);
        verify(dependentRepository, times(1)).save(any());
    }

    @Test
    void affiliateDataDependent__(){

        List<DataExcelDependentDTO> listDependent = listDependent()
                .stream()
                .peek(e -> e.setIdOccupation("107"))
                .toList();
        List<Municipality> allMunicipality = allMunicipality();
        List<EconomicActivity> allActivities = allActivities();
        List<Policy> listPolicy = listPolicy();
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        Affiliate affiliate1 = new Affiliate();
        Affiliate affiliation = new Affiliate();
        AffiliationDependent affiliationDependent = new AffiliationDependent();
        UserMain userMain = new UserMain();
        Smlmv smlmv =  new Smlmv();

        affiliate1.setIdAffiliate(1L);
        affiliate1.setFiledNumber("123");

        when(municipalityRepository.findAll())
                .thenReturn(allMunicipality);
        when(iEconomicActivityRepository.findAll())
                .thenReturn(allActivities);
        when(iUserPreRegisterRepository.findOne((Specification<UserMain>) any()))
                .thenReturn(Optional.of(userMain));
        when( smlmvRepository.findByValidDate(LocalDateTime.now()))
                .thenReturn(Optional.of(smlmv));
        when(riskFeeService.getFeeByRisk("1"))
                .thenReturn(BigDecimal.valueOf(1));
        when(affiliateService.createAffiliate(any()))
                .thenReturn(affiliation);
        when(dependentRepository.save(any()))
                .thenReturn(affiliationDependent);
        when(affiliateRepository.findById(1L))
                .thenReturn(Optional.of(affiliate1));
        when(policyRepository.findByIdAffiliate(1L))
                .thenReturn(listPolicy);
        when(affiliateMercantileRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliateMercantile));

        service.affiliateData(listDependent, null, "Trabajador Dependiente", affiliate1, 1L, 1L);
        verify(dependentRepository, times(1)).save(any());
    }

    @Test
    void affiliateDataDependent_Exeption(){

        List<DataExcelDependentDTO> listDependent = listDependent()
                .stream()
                .peek(e -> e.setIdOccupation("107"))
                .toList();
        List<Municipality> allMunicipality = allMunicipality();
        List<EconomicActivity> allActivities = allActivities();
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        Affiliate affiliate1 = new Affiliate();
        Affiliate affiliation = new Affiliate();
        List<Policy> policy = new ArrayList<>();
        AffiliationDependent affiliationDependent = new AffiliationDependent();
        UserMain userMain = new UserMain();
        Smlmv smlmv =  new Smlmv();

        affiliate1.setIdAffiliate(1L);
        affiliate1.setFiledNumber("123");

        when(municipalityRepository.findAll())
                .thenReturn(allMunicipality);
        when(iEconomicActivityRepository.findAll())
                .thenReturn(allActivities);
        when(iUserPreRegisterRepository.findOne((Specification<UserMain>) any()))
                .thenReturn(Optional.of(userMain));
        when( smlmvRepository.findByValidDate(LocalDateTime.now()))
                .thenReturn(Optional.of(smlmv));
        when(riskFeeService.getFeeByRisk("1"))
                .thenReturn(BigDecimal.valueOf(1));
        when(affiliateService.createAffiliate(any()))
                .thenReturn(affiliation);
        when(dependentRepository.save(any()))
                .thenReturn(affiliationDependent);
        when(affiliateRepository.findById(1L))
                .thenReturn(Optional.of(affiliate1));
        when(policyRepository.findByIdAffiliate(1L))
                .thenReturn(policy);
        when(affiliateMercantileRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliateMercantile));
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.affiliateData(listDependent, null, "Trabajador Dependiente", affiliate1, 1L, 1L)
        );

        assertNotNull(ex.getMessage());
    }

    @Test
    void affiliateDataDependent_Exeption_(){

        List<DataExcelDependentDTO> listDependent = listDependent()
                .stream()
                .peek(e -> e.setIdOccupation("107"))
                .toList();
        List<Municipality> allMunicipality = allMunicipality();
        List<EconomicActivity> allActivities = null;
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        Affiliate affiliate1 = new Affiliate();
        Affiliate affiliation = new Affiliate();
        List<Policy> policy = new ArrayList<>();
        AffiliationDependent affiliationDependent = new AffiliationDependent();
        UserMain userMain = new UserMain();
        Smlmv smlmv =  new Smlmv();

        affiliate1.setIdAffiliate(1L);
        affiliate1.setFiledNumber("123");

        when(municipalityRepository.findAll())
                .thenReturn(allMunicipality);
        when(iEconomicActivityRepository.findAll())
                .thenReturn(allActivities);
        when(iUserPreRegisterRepository.findOne((Specification<UserMain>) any()))
                .thenReturn(Optional.of(userMain));
        when( smlmvRepository.findByValidDate(LocalDateTime.now()))
                .thenReturn(Optional.of(smlmv));
        when(riskFeeService.getFeeByRisk("1"))
                .thenReturn(BigDecimal.valueOf(1));
        when(affiliateService.createAffiliate(any()))
                .thenReturn(affiliation);
        when(dependentRepository.save(any()))
                .thenReturn(affiliationDependent);
        when(affiliateRepository.findById(1L))
                .thenReturn(Optional.of(affiliate1));
        when(policyRepository.findByIdAffiliate(1L))
                .thenReturn(policy);
        when(affiliateMercantileRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliateMercantile));
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.affiliateData(listDependent, null, "Trabajador Dependiente", affiliate1, 1L, 1L)
        );

        assertNotNull(ex.getMessage());
    }

    @Test
    void affiliateDataDependent_Exeption__(){

        List<DataExcelDependentDTO> listDependent = listDependent()
                .stream()
                .peek(e -> {
                    e.setIdOccupation("107");
                    e.setIdentificationDocumentType("CE");
                })
                .toList();
        List<Municipality> allMunicipality = null;
        List<EconomicActivity> allActivities = allActivities();
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        Affiliate affiliate1 = new Affiliate();
        Affiliate affiliation = new Affiliate();
        List<Policy> policy = new ArrayList<>();
        AffiliationDependent affiliationDependent = new AffiliationDependent();
        UserMain userMain = new UserMain();
        Smlmv smlmv =  new Smlmv();

        affiliate1.setIdAffiliate(1L);
        affiliate1.setFiledNumber("123");

        when(municipalityRepository.findAll())
                .thenReturn(allMunicipality);
        when(iEconomicActivityRepository.findAll())
                .thenReturn(allActivities);
        when(iUserPreRegisterRepository.findOne((Specification<UserMain>) any()))
                .thenReturn(Optional.of(userMain));
        when( smlmvRepository.findByValidDate(LocalDateTime.now()))
                .thenReturn(Optional.of(smlmv));
        when(riskFeeService.getFeeByRisk("1"))
                .thenReturn(BigDecimal.valueOf(1));
        when(affiliateService.createAffiliate(any()))
                .thenReturn(affiliation);
        when(dependentRepository.save(any()))
                .thenReturn(affiliationDependent);
        when(affiliateRepository.findById(1L))
                .thenReturn(Optional.of(affiliate1));
        when(policyRepository.findByIdAffiliate(1L))
                .thenReturn(policy);
        when(affiliateMercantileRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliateMercantile));
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.affiliateData(listDependent, null, "Trabajador Dependiente", affiliate1, 1L, 1L)
        );

        assertNotNull(ex.getMessage());
    }


    @Test
    void affiliateDataIndependent(){

        List<DataExcelIndependentDTO> listDependent = listIndependent();
        List<Municipality> allMunicipality = allMunicipality();
        List<EconomicActivity> allActivities = allActivities();
        List<Policy> listPolicy = listPolicy();
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        Affiliate affiliate1 = new Affiliate();
        Affiliate affiliation = new Affiliate();
        AffiliationDependent affiliationDependent = new AffiliationDependent();
        UserMain userMain = new UserMain();
        Smlmv smlmv =  new Smlmv();

        affiliate1.setIdAffiliate(1L);
        affiliate1.setFiledNumber("123");

        when(municipalityRepository.findAll())
                .thenReturn(allMunicipality);
        when(iEconomicActivityRepository.findAll())
                .thenReturn(allActivities);
        when(iUserPreRegisterRepository.findOne((Specification<UserMain>) any()))
                .thenReturn(Optional.of(userMain));
        when( smlmvRepository.findByValidDate(LocalDateTime.now()))
                .thenReturn(Optional.of(smlmv));
        when(riskFeeService.getFeeByRisk("1"))
                .thenReturn(BigDecimal.valueOf(1));
        when(affiliateService.createAffiliate(any()))
                .thenReturn(affiliation);
        when(dependentRepository.save(any()))
                .thenReturn(affiliationDependent);
        when(affiliateRepository.findById(1L))
                .thenReturn(Optional.of(affiliate1));
        when(policyRepository.findByIdAffiliate(1L))
                .thenReturn(listPolicy);
        when(affiliateMercantileRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliateMercantile));

        service.affiliateData(null, listDependent, "Trabajador Independiente", affiliate1, 1L, 1L);
        verify(dependentRepository, times(1)).save(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {1000, 250000, 1500000})
    void affiliateDataIndependent_(Integer smlmvValue){

        List<DataExcelIndependentDTO> listDependent = listIndependent();
        List<Municipality> allMunicipality = allMunicipality();
        List<EconomicActivity> allActivities = allActivities();
        List<Policy> listPolicy = listPolicy();
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        Affiliate affiliate1 = new Affiliate();
        Affiliate affiliation = new Affiliate();
        AffiliationDependent affiliationDependent = new AffiliationDependent();
        UserMain userMain = new UserMain();
        Smlmv smlmv =  new Smlmv();
        smlmv.setValor(smlmvValue);

        affiliate1.setIdAffiliate(1L);
        affiliate1.setFiledNumber("123");

        when(municipalityRepository.findAll())
                .thenReturn(allMunicipality);
        when(iEconomicActivityRepository.findAll())
                .thenReturn(allActivities);
        when(iUserPreRegisterRepository.findOne((Specification<UserMain>) any()))
                .thenReturn(Optional.of(userMain));
        when( smlmvRepository.findMostRecent())
                .thenReturn(Optional.of(smlmv));
        when(riskFeeService.getFeeByRisk("1"))
                .thenReturn(BigDecimal.valueOf(1));
        when(affiliateService.createAffiliate(any()))
                .thenReturn(affiliation);
        when(dependentRepository.save(any()))
                .thenReturn(affiliationDependent);
        when(affiliateRepository.findById(1L))
                .thenReturn(Optional.of(affiliate1));
        when(policyRepository.findByIdAffiliate(1L))
                .thenReturn(listPolicy);
        when(affiliateMercantileRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliateMercantile));

        service.affiliateData(null, listDependent, "Trabajador Independiente", affiliate1, 1L, 1L);
        verify(dependentRepository, times(1)).save(any());
    }

    @Test
    void affiliateDataIndependent__(){

        List<DataExcelIndependentDTO> listDependent = listIndependent()
                .stream()
                .peek(e -> {
                    LocalDate date = LocalDate.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                    String dateStart = date.format(formatter);
                    e.setEndDate(dateStart);
                })
                .toList();
        List<Municipality> allMunicipality = allMunicipality();
        List<EconomicActivity> allActivities = allActivities();
        List<Policy> listPolicy = listPolicy();
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        Affiliate affiliate1 = new Affiliate();
        Affiliate affiliation = new Affiliate();
        AffiliationDependent affiliationDependent = new AffiliationDependent();
        UserMain userMain = new UserMain();
        Smlmv smlmv =  new Smlmv();

        affiliate1.setIdAffiliate(1L);
        affiliate1.setFiledNumber("123");

        when(municipalityRepository.findAll())
                .thenReturn(allMunicipality);
        when(iEconomicActivityRepository.findAll())
                .thenReturn(allActivities);
        when(iUserPreRegisterRepository.findOne((Specification<UserMain>) any()))
                .thenReturn(Optional.of(userMain));
        when( smlmvRepository.findByValidDate(LocalDateTime.now()))
                .thenReturn(Optional.of(smlmv));
        when(riskFeeService.getFeeByRisk("1"))
                .thenReturn(BigDecimal.valueOf(1));
        when(affiliateService.createAffiliate(any()))
                .thenReturn(affiliation);
        when(dependentRepository.save(any()))
                .thenReturn(affiliationDependent);
        when(affiliateRepository.findById(1L))
                .thenReturn(Optional.of(affiliate1));
        when(policyRepository.findByIdAffiliate(1L))
                .thenReturn(listPolicy);
        when(affiliateMercantileRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliateMercantile));

        service.affiliateData(null, listDependent, "Trabajador Independiente", affiliate1, 1L, 1L);
        verify(dependentRepository, times(1)).save(any());
    }

    @Test
    void affiliateDataIndependent_Exception(){

        List<DataExcelIndependentDTO> listDependent = listIndependent()
                .stream()
                .peek(e -> e.setEndDate(LocalDate.now().toString()))
                .toList();
        List<Municipality> allMunicipality = allMunicipality();
        List<EconomicActivity> allActivities = allActivities();
        List<Policy> listPolicy = listPolicy();
        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        Affiliate affiliate1 = new Affiliate();
        Affiliate affiliation = new Affiliate();
        AffiliationDependent affiliationDependent = new AffiliationDependent();
        UserMain userMain = new UserMain();
        Smlmv smlmv =  new Smlmv();

        affiliate1.setIdAffiliate(1L);
        affiliate1.setFiledNumber("123");

        when(municipalityRepository.findAll())
                .thenReturn(allMunicipality);
        when(iEconomicActivityRepository.findAll())
                .thenReturn(allActivities);
        when(iUserPreRegisterRepository.findOne((Specification<UserMain>) any()))
                .thenReturn(Optional.of(userMain));
        when( smlmvRepository.findByValidDate(LocalDateTime.now()))
                .thenReturn(Optional.of(smlmv));
        when(riskFeeService.getFeeByRisk("1"))
                .thenReturn(BigDecimal.valueOf(1));
        when(affiliateService.createAffiliate(any()))
                .thenReturn(affiliation);
        when(dependentRepository.save(any()))
                .thenReturn(affiliationDependent);
        when(affiliateRepository.findById(1L))
                .thenReturn(Optional.of(affiliate1));
        when(policyRepository.findByIdAffiliate(1L))
                .thenReturn(listPolicy);
        when(affiliateMercantileRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliateMercantile));

        Exception ex = assertThrows(
                Exception.class,
                () -> service.affiliateData(null, listDependent, "Trabajador Independiente", affiliate1, 1L, 1L)
        );

        assertNotNull(ex.getMessage());
    }


    List<DataExcelDependentDTO> listDependent(){

        DataExcelDependentDTO dataExcelDependentDTO = new DataExcelDependentDTO();
        dataExcelDependentDTO.setIdentificationDocumentType("CC");
        dataExcelDependentDTO.setIdDepartment("1");
        dataExcelDependentDTO.setIdCity("1");
        dataExcelDependentDTO.setIdWorkModality("1");
        dataExcelDependentDTO.setSalary("12312");
        dataExcelDependentDTO.setDateOfBirth("2025/01/01");
        dataExcelDependentDTO.setHealthPromotingEntity("1");
        dataExcelDependentDTO.setPensionFundAdministrator("1");
        dataExcelDependentDTO.setEconomicActivityCode("111");
        dataExcelDependentDTO.setIdOccupation("1654");
        List<DataExcelDependentDTO> list = new ArrayList<>();
        list.add(dataExcelDependentDTO);
        return list;
     }

    List<DataExcelIndependentDTO> listIndependent(){

        LocalDate date = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

        String dateStart = date.format(formatter);
        String dateEnd = date.plusMonths(2).format(formatter);
        String coverage = date.format(formatter);

        DataExcelIndependentDTO dataExcelDependentDTO = new DataExcelIndependentDTO();
        dataExcelDependentDTO.setIdentificationDocumentType("CC");
        dataExcelDependentDTO.setIdDepartment("1");
        dataExcelDependentDTO.setIdCity("1");
        dataExcelDependentDTO.setDateOfBirth("2025/01/01");
        dataExcelDependentDTO.setHealthPromotingEntity("1");
        dataExcelDependentDTO.setPensionFundAdministrator("1");
        dataExcelDependentDTO.setIdOccupation("1654");
        dataExcelDependentDTO.setContractTotalValue("1");
        dataExcelDependentDTO.setStartDate(dateStart);
        dataExcelDependentDTO.setEndDate(dateEnd);
        dataExcelDependentDTO.setCoverageDate(coverage);
        dataExcelDependentDTO.setCodeActivityContract("1");
        dataExcelDependentDTO.setContractTotalValue("2000000");
        List<DataExcelIndependentDTO> list = new ArrayList<>();
        list.add(dataExcelDependentDTO);
        return list;
    }

    List<Municipality> allMunicipality(){
        Municipality municipality = new Municipality();
        municipality.setIdMunicipality(1L);
        List<Municipality> list = new ArrayList<>();
        list.add(municipality);
        return list;
    }

    List<EconomicActivity> allActivities(){
        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setClassRisk("1");
        economicActivity.setCodeCIIU("1");
        economicActivity.setAdditionalCode("1");
        List<EconomicActivity> list = new ArrayList<>();
        list.add(economicActivity);
        return list;
    }

    List<Policy> listPolicy(){
        Policy policy = new Policy();
        policy.setIdPolicyType(1L);
        List<Policy> list =  new ArrayList<>();
        list.add(policy);
        return list;
    }
}
