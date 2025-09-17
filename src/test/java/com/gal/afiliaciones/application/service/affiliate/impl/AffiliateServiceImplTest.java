package com.gal.afiliaciones.application.service.affiliate.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.gal.afiliaciones.application.service.affiliate.affiliationemployeractivitiesmercantile.AffiliationEmployerActivitiesMercantileService;
import com.gal.afiliaciones.infrastructure.client.generic.employer.ConsultEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.InsertEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.independentrelationship.IndependentContractRelationshipClient;
import com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative.InsertLegalRepresentativeClient;
import com.gal.afiliaciones.infrastructure.client.generic.person.InsertPersonClient;
import com.gal.afiliaciones.infrastructure.client.generic.policy.InsertPolicyClient;
import com.gal.afiliaciones.infrastructure.client.generic.volunteer.VolunteerRelationshipClient;
import com.gal.afiliaciones.infrastructure.client.generic.workcenter.InsertWorkCenterClient;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.FamilyMemberRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.decree1563.OccupationDecree1563Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.IUserRegisterService;
import com.gal.afiliaciones.application.service.KeycloakService;
import com.gal.afiliaciones.application.service.RolesUserService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.daily.DailyService;
import com.gal.afiliaciones.application.service.documentnamestandardization.DocumentNameStandardizationService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.generalnovelty.impl.GeneralNoveltyServiceImpl;
import com.gal.afiliaciones.application.service.policy.PolicyService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.mapper.AffiliateMapper;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.Health;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.Policy;
import com.gal.afiliaciones.domain.model.Role;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.EmployerSize;
import com.gal.afiliaciones.domain.model.affiliate.RequestChannel;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.client.generic.affiliatecompany.AffiliateCompanyResponse;
import com.gal.afiliaciones.infrastructure.client.generic.affiliatecompany.ConsultAffiliateCompanyClient;
import com.gal.afiliaciones.infrastructure.client.generic.person.PersonResponse;
import com.gal.afiliaciones.infrastructure.client.generic.person.PersonlClient;
import com.gal.afiliaciones.infrastructure.dao.repository.DateInterviewWebRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationCancellationTimerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IDataDocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.EmployerSizeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.RequestChannelRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.eps.HealthPromotingEntityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.dto.UserPreRegisterDto;
import com.gal.afiliaciones.infrastructure.dto.affiliate.AffiliationResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DataStatusAffiliationDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.UserAffiliateDTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ReplacedDocumentDTO;
import com.gal.afiliaciones.infrastructure.dto.municipality.MunicipalityDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.dto.sat.AffiliationWorkerIndependentArlDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.utils.KeyCloakProvider;

@ExtendWith(MockitoExtension.class)
class AffiliateServiceImplTest {

    @Mock
    private RolesUserService rolesUserService;
    @Mock
    private AffiliateRepository affiliateRepository;
    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    @Mock
    private SendEmails sendEmails;
    @Mock
    private AlfrescoService alfrescoService;
    @Mock
    private CollectProperties properties;
    @Mock
    private IDataDocumentRepository dataDocumentRepository;
    @Mock
    private GenericWebClient webClient;
    @Mock
    private PolicyService policyService;
    @Mock
    private AffiliateMercantileRepository mercantileRepository;
    @Mock
    private IAffiliationCancellationTimerRepository timerRepository;
    @Mock
    private IUserPreRegisterRepository iUserPreRegisterRepository;
    @Mock
    private DateInterviewWebRepository dateInterviewWebRepository;
    @Mock
    private DailyService dailyService;
    @Mock
    private GenerateCardAffiliatedService cardAffiliatedService;
    @Mock
    private AffiliationDependentRepository dependentRepository;
    @Mock
    private RequestChannelRepository requestChannelRepository;
    @Mock
    private AffiliationDetailRepository affiliationDetailRepository;
    @Mock
    private AffiliateMercantileRepository affiliateMercantileRepository;
    @Mock
    private KeycloakService keycloakService;
    @Mock
    private DocumentNameStandardizationService documentNameStandardizationService;
    @Mock
    private EmployerSizeRepository employerSizeRepository;
    @Mock
    private KeyCloakProvider keyCloakProvider;
    @InjectMocks
    private AffiliateServiceImpl affiliateService;
    @Mock
    private ConsultAffiliateCompanyClient consultAffiliateCompanyClient;
    @Mock
    private FiledService filedService;
    @Mock
    private ArlInformationDao arlInformationDao;
    @Mock
    private MunicipalityRepository municipalityRepository;
    @Mock
    private HealthPromotingEntityRepository healthPromotingEntityRepository;
    @Mock
    private AffiliationDependentRepository affiliationDependentRepository;
    @Mock
    private PolicyRepository policyRepository;
    @Mock
    private IUserRegisterService iUserRegisterService;
    @Mock
    private PersonlClient clientPerson;
    @Mock
    private AffiliateMapper affiliateMapper;
    @Mock
    private InsertPersonClient insertPersonClient;
    @Mock
    private InsertEmployerClient insertEmployerClient;
    @Mock
    public InsertLegalRepresentativeClient insertLegalRepresentativeClient;
    @Mock
    private IndependentContractRelationshipClient independentContractClient;
    @Mock
    private OccupationRepository occupationRepository;
    @Mock
    private InsertPolicyClient insertPolicyClient;
    @Mock
    private ConsultEmployerClient consultEmployerClient;
    @Mock
    private AffiliationEmployerActivitiesMercantileService mercantileService;
    @Mock
    private  InsertWorkCenterClient insertWorkCenterClient;
    @Mock
    private VolunteerRelationshipClient insertVolunteerClient;
    @Mock
    private FamilyMemberRepository familyMemberRepository;
    private OccupationDecree1563Repository occupationVolunteerRepository;
    @Mock
    private GeneralNoveltyServiceImpl generalNoveltyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        affiliateService = new AffiliateServiceImpl(
                rolesUserService, affiliateRepository, repositoryAffiliation, sendEmails,
                alfrescoService,
                properties, dataDocumentRepository, webClient, policyService, mercantileRepository,
                timerRepository,
                iUserPreRegisterRepository, dateInterviewWebRepository, dailyService,
                cardAffiliatedService,
                dependentRepository, requestChannelRepository, affiliationDetailRepository,
                affiliateMercantileRepository,
                keycloakService, documentNameStandardizationService, employerSizeRepository,
                consultAffiliateCompanyClient, filedService, arlInformationDao, municipalityRepository,
                healthPromotingEntityRepository,
                affiliationDependentRepository, policyRepository, iUserRegisterService, clientPerson,
                generalNoveltyService, affiliateMapper, insertPersonClient, insertEmployerClient,
                insertLegalRepresentativeClient, independentContractClient, occupationRepository, insertPolicyClient,
                consultEmployerClient, mercantileService, insertWorkCenterClient, insertVolunteerClient,
                familyMemberRepository, occupationVolunteerRepository, keyCloakProvider);
    }

    @Test
    void testFindAffiliationsByTypeAndNumber_independent() {
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setFiledNumber("SOL_AFI_2025000001450");
        List<Affiliate> affiliates = List.of(affiliate);

        Affiliation affiliation = new Affiliation();
        affiliation.setContractEndDate(LocalDate.of(2024, 6, 1));

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "123")).thenReturn(affiliates);
        when(repositoryAffiliation.findByFiledNumber("SOL_AFI_2025000001450"))
                .thenReturn(Optional.of(affiliation));

        List<UserAffiliateDTO> result = affiliateService.findAffiliationsByTypeAndNumber("CC", "123");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRetirementDate()).isEqualTo("2024-06-01");
    }

    @Test
    void testFindAffiliationsByTypeAndNumber_dependent() {
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setFiledNumber("SOL_AFI_2025000001451");
        List<Affiliate> affiliates = List.of(affiliate);

        AffiliationDependent dep = new AffiliationDependent();
        dep.setEndDate(LocalDate.of(2024, 7, 1));

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "456")).thenReturn(affiliates);
        when(dependentRepository.findByFiledNumber("SOL_AFI_2025000001451")).thenReturn(Optional.of(dep));

        List<UserAffiliateDTO> result = affiliateService.findAffiliationsByTypeAndNumber("CC", "456");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRetirementDate()).isEqualTo("2024-07-01");
    }

    @Test
    void testCreateAffiliate() {
        Affiliate affiliate = new Affiliate();
        when(affiliateRepository.save(affiliate)).thenReturn(affiliate);

        Affiliate result = affiliateService.createAffiliate(affiliate);

        assertThat(result).isSameAs(affiliate);
        verify(affiliateRepository).save(affiliate);
    }

    @Test
    void testFindAll() {
        List<Affiliate> affiliates = List.of(new Affiliate(), new Affiliate());
        when(affiliateRepository.findAll()).thenReturn(affiliates);

        List<Affiliate> result = affiliateService.findAll();

        assertThat(result).hasSize(2);
        verify(affiliateRepository).findAll();
    }

    @Test
    void testGetEmployerSize() {
        EmployerSize size = new EmployerSize();
        size.setId(1L);
        size.setMinNumberWorker(1);
        size.setMaxNumberWorker(10);
        when(employerSizeRepository.findAll()).thenReturn(List.of(size));

        Long result = affiliateService.getEmployerSize(5);

        assertThat(result).isEqualTo(1L);
    }

    @Test
    void testCalculateIbcAmount_returnsSmlmvIfBelow() {
        SalaryDTO salaryDTO = new SalaryDTO();
        salaryDTO.setValue(1000L);
        when(webClient.getSmlmvByYear(anyInt())).thenReturn(salaryDTO);

        BigDecimal result = affiliateService.calculateIbcAmount(new BigDecimal("500"), new BigDecimal("10"));

        assertThat(result).isEqualTo(new BigDecimal("1000"));
    }

    @Test
    void testCalculateIbcAmount_returnsCalculatedIfAboveSmlmv() {
        SalaryDTO salaryDTO = new SalaryDTO();
        salaryDTO.setValue(100L);
        when(webClient.getSmlmvByYear(anyInt())).thenReturn(salaryDTO);

        BigDecimal result = affiliateService.calculateIbcAmount(new BigDecimal("1000"), new BigDecimal("20"));

        assertThat(result).isGreaterThan(new BigDecimal("100"));
    }

    @Test
    void testFindAffiliate_affiliationWins() {
        Affiliation aff = new Affiliation();
        aff.setId(10L);
        aff.setFiledNumber("SOL_AFI_2025000001450");
        AffiliateMercantile merc = new AffiliateMercantile();
        merc.setId(20L);
        merc.setFiledNumber("SOL_AFI_2025000001433");

        when(affiliationDetailRepository
                .findFirstByIdentificationDocumentTypeAndIdentificationDocumentNumberOrderByFiledNumberDesc(
                        "CC", "123"))
                .thenReturn(Optional.of(aff));
        when(affiliateMercantileRepository
                .findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(
                        "CC", "123"))
                .thenReturn(Optional.of(merc));

        Long result = affiliateService.findAffiliate("CC", "123");

        assertThat(result).isEqualTo(10L);
    }

    @Test
    void testFindAffiliate_mercantileWins() {
        Affiliation aff = new Affiliation();
        aff.setId(10L);
        aff.setFiledNumber("SOL_AFI_2025000001450");
        AffiliateMercantile merc = new AffiliateMercantile();
        merc.setId(20L);
        merc.setFiledNumber("SOL_AFI_2025000001455");

        when(affiliationDetailRepository
                .findFirstByIdentificationDocumentTypeAndIdentificationDocumentNumberOrderByFiledNumberDesc(
                        "CC", "123"))
                .thenReturn(Optional.of(aff));
        when(affiliateMercantileRepository
                .findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(
                        "CC", "123"))
                .thenReturn(Optional.of(merc));

        Long result = affiliateService.findAffiliate("CC", "123");

        assertThat(result).isEqualTo(20L);
    }

    @Test
    void testFindAffiliate_affiliationOnly() {
        Affiliation aff = new Affiliation();
        aff.setId(10L);
        aff.setFiledNumber("SOL_AFI_2025000001450");

        when(affiliationDetailRepository
                .findFirstByIdentificationDocumentTypeAndIdentificationDocumentNumberOrderByFiledNumberDesc(
                        "CC", "123"))
                .thenReturn(Optional.of(aff));
        when(affiliateMercantileRepository
                .findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(
                        "CC", "123"))
                .thenReturn(Optional.empty());

        Long result = affiliateService.findAffiliate("CC", "123");

        assertThat(result).isEqualTo(10L);
    }

    @Test
    void testFindAffiliate_mercantileOnly() {
        AffiliateMercantile merc = new AffiliateMercantile();
        merc.setId(20L);
        merc.setFiledNumber("SOL_AFI_2025000001455");

        when(affiliationDetailRepository
                .findFirstByIdentificationDocumentTypeAndIdentificationDocumentNumberOrderByFiledNumberDesc(
                        "CC", "123"))
                .thenReturn(Optional.empty());
        when(affiliateMercantileRepository
                .findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(
                        "CC", "123"))
                .thenReturn(Optional.of(merc));

        Long result = affiliateService.findAffiliate("CC", "123");

        assertThat(result).isEqualTo(20L);
    }

    @Test
    void testFindAffiliate_noneFound_throws() {
        when(affiliationDetailRepository
                .findFirstByIdentificationDocumentTypeAndIdentificationDocumentNumberOrderByFiledNumberDesc(
                        "CC", "123"))
                .thenReturn(Optional.empty());
        when(affiliateMercantileRepository
                .findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(
                        "CC", "123"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> affiliateService.findAffiliate("CC", "123"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testFindAllRequestChannel_sorted() {
        RequestChannel rc1 = new RequestChannel();
        rc1.setId(2L);
        RequestChannel rc2 = new RequestChannel();
        rc2.setId(1L);
        when(requestChannelRepository.findAll()).thenReturn(List.of(rc1, rc2));

        List<RequestChannel> result = affiliateService.findAllRequestChannel();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    void testSing_affiliationIndependent_success() {
        String filedNumber = "SOL_AFI_2025000001450";
        Affiliation affiliation = mock(Affiliation.class);
        when(affiliation.getFiledNumber()).thenReturn(filedNumber);
        when(affiliation.getTypeAffiliation()).thenReturn(Constant.TYPE_AFFILLATE_INDEPENDENT);
        when(affiliation.getStageManagement()).thenReturn("PREVIOUS_STAGE");
        when(affiliation.getIdentificationDocumentType()).thenReturn("CC");
        when(affiliation.getIdentificationDocumentNumber()).thenReturn("123");
        Affiliate affiliate = mock(Affiliate.class);
        when(affiliate.getFiledNumber()).thenReturn(filedNumber);
        when(affiliate.getAffiliationType()).thenReturn(Constant.TYPE_AFFILLATE_INDEPENDENT);
        when(affiliate.getAffiliationCancelled()).thenReturn(false);
        when(affiliate.getStatusDocument()).thenReturn(false);
        when(affiliate.getAffiliationSubType()).thenReturn("SUBTYPE");
        when(affiliate.getUserId()).thenReturn(1L);
        when(affiliate.getIdAffiliate()).thenReturn(2L);
        when(affiliate.getCompany()).thenReturn("COMPANY");
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(affiliate.getAffiliationType()).thenReturn(Constant.TYPE_AFFILLATE_INDEPENDENT);
        when(affiliate.getAffiliationSubType()).thenReturn("SUBTYPE");
        when(affiliate.getUserId()).thenReturn(1L);
        when(affiliate.getIdAffiliate()).thenReturn(2L);
        when(affiliate.getCompany()).thenReturn("COMPANY");
        when(affiliate.getAffiliationCancelled()).thenReturn(false);
        when(affiliate.getStatusDocument()).thenReturn(false);
        when(affiliation.getStageManagement()).thenReturn("PREVIOUS_STAGE");
        when(affiliation.getTypeAffiliation()).thenReturn(Constant.TYPE_AFFILLATE_INDEPENDENT);
        when(mercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        Role role = new Role();
        role.setId(1L);
        role.setCode("ROLE");
        role.setRoleName("ROLE");
        when(rolesUserService.findByName(any())).thenReturn(role);

        UserMain userMain = new UserMain();
        userMain.setFirstName("John");
        userMain.setSurname("Doe");
        userMain.setUserType(2L);
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(userMain));

        Policy policy = new Policy();
        policy.setCode("24902500000000021088");
        policy.setNumPolicyClient(70000001L);
        when(policyService.createPolicy(any(), any(), any(), any(), any(), any(), any())).thenReturn(policy);

        affiliateService.sing(filedNumber);

        verify(policyService).createPolicy(eq("CC"), eq("123"), any(LocalDate.class), isNull(), eq(2L), eq(0L),
                eq("COMPANY"));
        verify(repositoryAffiliation).save(affiliation);
        verify(rolesUserService).updateRoleUser(eq(1L), any());
        verify(affiliateRepository).save(affiliate);
        verify(cardAffiliatedService).createCardWithoutOtp(filedNumber);
        verify(sendEmails).welcome(eq(affiliation), eq(2L), eq(Constant.TYPE_AFFILLATE_INDEPENDENT),
                eq("SUBTYPE"));
    }

    @Test
    void testSing_affiliationEmployerDomestic_success() {
        String filedNumber = "SOL_AFI_2025000001451";
        Affiliation affiliation = mock(Affiliation.class);
        when(affiliation.getFiledNumber()).thenReturn(filedNumber);
        when(affiliation.getTypeAffiliation()).thenReturn(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        when(affiliation.getStageManagement()).thenReturn("PREVIOUS_STAGE");
        when(affiliation.getIdentificationDocumentType()).thenReturn("CC");
        when(affiliation.getIdentificationDocumentNumber()).thenReturn("456");
        Affiliate affiliate = mock(Affiliate.class);
        when(affiliate.getFiledNumber()).thenReturn(filedNumber);
        when(affiliate.getAffiliationType()).thenReturn(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        when(affiliate.getAffiliationCancelled()).thenReturn(false);
        when(affiliate.getStatusDocument()).thenReturn(false);
        when(affiliate.getAffiliationSubType()).thenReturn("SUBTYPE");
        when(affiliate.getUserId()).thenReturn(1L);
        when(affiliate.getIdAffiliate()).thenReturn(2L);
        when(affiliate.getCompany()).thenReturn("COMPANY");
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(affiliate.getAffiliationType()).thenReturn(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        when(affiliate.getAffiliationSubType()).thenReturn("SUBTYPE");
        when(affiliate.getUserId()).thenReturn(1L);
        when(affiliate.getIdAffiliate()).thenReturn(2L);
        when(affiliate.getCompany()).thenReturn("COMPANY");
        when(affiliate.getAffiliationCancelled()).thenReturn(false);
        when(affiliate.getStatusDocument()).thenReturn(false);
        when(affiliation.getStageManagement()).thenReturn("PREVIOUS_STAGE");
        when(affiliation.getTypeAffiliation()).thenReturn(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        when(mercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        Role role = new Role();
        role.setId(1L);
        role.setCode("ROLE");
        role.setRoleName("ROLE");
        when(rolesUserService.findByName(any())).thenReturn(role);

        UserMain userMain = new UserMain();
        userMain.setId(10L);
        userMain.setFirstName("John");
        userMain.setSurname("Doe");
        userMain.setUserType(2L);
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(userMain));

        Policy policy = new Policy();
        policy.setCode("24902500000000021088");
        policy.setNumPolicyClient(70000001L);
        when(policyService.createPolicy(any(), any(), any(), any(), any(), any(), any())).thenReturn(policy);

        affiliateService.sing(filedNumber);

        verify(policyService, times(2)).createPolicy(any(), any(), any(), any(), any(), any(), any());
        verify(repositoryAffiliation).save(affiliation);
        verify(rolesUserService).updateRoleUser(eq(1L), any());
        verify(affiliateRepository).save(affiliate);
        verify(sendEmails).welcome(eq(affiliation), eq(2L), eq(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC),
                eq("SUBTYPE"));
    }

    @Test
    void testSing_affiliationNotFound_throws() {
        String filedNumber = "SOL_AFI_2025000001499";
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(mercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> affiliateService.sing(filedNumber))
                .isInstanceOf(com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException.class);
    }

    @Test
    void testSing_affiliationCancelled_throws() {
        String filedNumber = "SOL_AFI_2025000001452";
        Affiliation affiliation = new Affiliation();
        affiliation.setFiledNumber(filedNumber);
        affiliation.setTypeAffiliation(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliation.setStageManagement("PREVIOUS_STAGE");
        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber(filedNumber);
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setAffiliationCancelled(true);
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        // when(affiliate.getAffiliationCancelled()).thenReturn(true);
        when(mercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> affiliateService.sing(filedNumber))
                .isInstanceOf(com.gal.afiliaciones.config.ex.affiliation.AffiliationError.class);
    }

    @Test
    void testSing_affiliationStatusDocument_throws() {
        String filedNumber = "SOL_AFI_2025000001453";
        Affiliation affiliation = new Affiliation();
        affiliation.setFiledNumber(filedNumber);
        affiliation.setTypeAffiliation(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliation.setStageManagement("PREVIOUS_STAGE");
        Affiliate affiliate = mock(Affiliate.class);
        when(affiliate.getAffiliationCancelled()).thenReturn(false);
        when(affiliate.getStatusDocument()).thenReturn(true);
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(affiliate.getAffiliationCancelled()).thenReturn(Boolean.FALSE);
        when(affiliate.getStatusDocument()).thenReturn(Boolean.TRUE);
        when(mercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> affiliateService.sing(filedNumber))
                .isInstanceOf(com.gal.afiliaciones.config.ex.affiliation.AffiliationError.class);
    }

    @Test
    void testSing_affiliationRegularization_throws() {
        String filedNumber = "SOL_AFI_2025000001454";
        Affiliation affiliation = mock(Affiliation.class);
        when(affiliation.getFiledNumber()).thenReturn(filedNumber);
        when(affiliation.getStageManagement()).thenReturn(Constant.REGULARIZATION);

        Affiliate affiliate = mock(Affiliate.class);
        when(affiliate.getAffiliationCancelled()).thenReturn(false);
        when(affiliate.getStatusDocument()).thenReturn(false);
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(affiliate.getAffiliationCancelled()).thenReturn(Boolean.FALSE);
        when(affiliate.getStatusDocument()).thenReturn(Boolean.FALSE);
        when(affiliation.getStageManagement()).thenReturn(Constant.REGULARIZATION);
        when(mercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> affiliateService.sing(filedNumber))
                .isInstanceOf(com.gal.afiliaciones.config.ex.affiliation.AffiliationError.class);
    }

    @Test
    void testSing_affiliateMercantile_branch() {
        String filedNumber = "SOL_AFI_2025000001500";
        Affiliation affiliation = mock(Affiliation.class);
        when(affiliation.getFiledNumber()).thenReturn(filedNumber);
        when(affiliation.getTypeAffiliation()).thenReturn(Constant.TYPE_AFFILLATE_INDEPENDENT);
        when(affiliation.getStageManagement()).thenReturn("PREVIOUS_STAGE");
        Affiliate affiliate = mock(Affiliate.class);
        when(affiliate.getFiledNumber()).thenReturn(filedNumber);
        when(affiliate.getAffiliationType()).thenReturn(Constant.TYPE_AFFILLATE_INDEPENDENT);
        when(affiliate.getAffiliationCancelled()).thenReturn(false);
        when(affiliate.getStatusDocument()).thenReturn(false);
        AffiliateMercantile merc = new AffiliateMercantile();
        merc.setFiledNumber(filedNumber);
        merc.setAffiliationCancelled(false);
        merc.setStatusDocument(false);
        merc.setStageManagement("PREVIOUS_STAGE");
        merc.setIdUserPreRegister(10L);
        merc.setTypeAffiliation(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        merc.setTypeDocumentIdentification("CC");
        merc.setNumberIdentification("789");
        merc.setDecentralizedConsecutive(0L);
        merc.setBusinessName("BUSINESS");
        merc.setDateInterview(LocalDateTime.now());
        merc.setEconomicActivity(new ArrayList<>());
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliate.getAffiliationCancelled()).thenReturn(Boolean.FALSE);
        when(affiliate.getStatusDocument()).thenReturn(Boolean.FALSE);
        when(affiliation.getStageManagement()).thenReturn("PREVIOUS_STAGE");
        when(mercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(merc));
        UserMain userMain = new UserMain();
        userMain.setId(10L);
        userMain.setFirstName("John");
        userMain.setSurname("Doe");
        userMain.setUserType(2L);
        when(iUserPreRegisterRepository.findById(10L)).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        Role role = new Role();
        role.setId(1L);
        role.setCode("ROLE");
        role.setRoleName("ROLE");
        when(rolesUserService.findByName(any())).thenReturn(role);
        when(affiliate.getIdAffiliate()).thenReturn(2L);
        when(affiliate.getCompany()).thenReturn("COMPANY");
        when(affiliate.getAffiliationSubType()).thenReturn(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(userMain));

        Policy policy = new Policy();
        policy.setCode("24902500000000021088");
        policy.setNumPolicyClient(70000001L);
        when(policyService.createPolicy(any(), any(), any(), any(), any(), any(), any())).thenReturn(policy);

        affiliateService.sing(filedNumber);

        verify(mercantileRepository).save(any(AffiliateMercantile.class));
        verify(rolesUserService).updateRoleUser(eq(10L), any());
        verify(affiliateRepository, atLeastOnce()).save(affiliate);
        verify(sendEmails).welcomeMercantile(any());
    }

    @Test
    void testSendInformationAffilliationVolunteerToSat_sendsCorrectRequest() {
        Affiliation affiliation = new Affiliation();
        affiliation.setIdentificationDocumentType("CC");
        affiliation.setIdentificationDocumentNumber("123456");
        affiliation.setFirstName("John");
        affiliation.setSurname("Doe");
        affiliation.setMunicipalityEmployer(10L);
        affiliation.setAddressEmployer("Employer Address");
        affiliation.setSecondaryPhone1("3216549870");
        affiliation.setSecondaryEmail("employer@email.com");
        affiliation.setCityMunicipality(20L);
        affiliation.setAddress("Worker Address");
        affiliation.setPhone1("9876543210");
        affiliation.setEmail("worker@email.com");
        affiliation.setContractIbcValue(new BigDecimal("1500000"));

        // Mock webClient.getMunicipalities()
        MunicipalityDTO employerMunicipality = new MunicipalityDTO();
        employerMunicipality.setIdMunicipality(10L);
        employerMunicipality.setDivipolaCode("11001");
        MunicipalityDTO workerMunicipality = new MunicipalityDTO();
        workerMunicipality.setIdMunicipality(20L);
        workerMunicipality.setDivipolaCode("76001");
        BodyResponseConfig<List<MunicipalityDTO>> municipalitiesResponse = new BodyResponseConfig<>();
        municipalitiesResponse.setData(List.of(employerMunicipality, workerMunicipality));
        when(webClient.getMunicipalities()).thenReturn(municipalitiesResponse);

        // Mock webClient.sendAffiliationIndependentToSat
        // doNothing().when(webClient).sendAffiliationIndependentToSat(any(AffiliationWorkerIndependentArlDTO.class));

        // Call private method via reflection
        try {
            java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod(
                    "sendInformationAffilliationVolunteerToSat", Affiliation.class, String.class);
            method.setAccessible(true);
            method.invoke(affiliateService, affiliation, "POL123");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ArgumentCaptor<AffiliationWorkerIndependentArlDTO> captor = ArgumentCaptor
                .forClass(AffiliationWorkerIndependentArlDTO.class);
        verify(webClient).sendAffiliationIndependentToSat(captor.capture());
        AffiliationWorkerIndependentArlDTO dto = captor.getValue();

        assertThat(dto.getPolicyNumber()).isEqualTo("POL123");
        assertThat(dto.getResponsiblePersonTypeAffiliation()).isEqualTo(Constant.LEGAL_ENTITY);
        assertThat(dto.getResponsiblePersonDocumentTypeAffiliation()).isEqualTo(Constant.NI);
        assertThat(dto.getResponsiblePersonDocumentNumberAffiliation()).isEqualTo(Constant.NIT_CONTRACT_VOLUNTEER);
        assertThat(dto.getResponsiblePersonSocialReasonOrNameAffiliation())
                .isEqualTo(Constant.COMPANY_NAME_CONTRACT_VOLUNTEER);
        assertThat(dto.getWorkerDocumentType()).isEqualTo("CC");
        assertThat(dto.getWorkerDocumentNumber()).isEqualTo("123456");
        assertThat(dto.getWorkerFirstName()).isEqualTo("John");
        assertThat(dto.getWorkerLastName()).isEqualTo("Doe");
        assertThat(dto.getResponsibleAffiliationMunicipalityLocation()).isEqualTo("11001");
        assertThat(dto.getWorkerMunicipalityWork()).isEqualTo("76001");
        assertThat(dto.getResponsibleAffiliationAddressLocation()).isEqualTo("Employer Address");
        assertThat(dto.getResponsibleAffiliationPhoneFixedOrMobile()).isEqualTo(3216549870L);
        assertThat(dto.getResponsibleAffiliationEmail()).isEqualTo("employer@email.com");
        assertThat(dto.getWorkerAddressWork()).isEqualTo("Worker Address");
        assertThat(dto.getWorkerPhoneFixedOrMobile()).isEqualTo(9876543210L);
        assertThat(dto.getWorkerEmail()).isEqualTo("worker@email.com");
        assertThat(dto.getBaseContributionIncome()).isEqualTo(1500000L);
        assertThat(dto.getAffiliationArlDate()).isNotNull();
        assertThat(dto.getWorkerContributorType()).isEqualTo(Constant.INDEPENDENT_VOLUNTEER_CODE_SAT);
        assertThat(dto.getWorkerContributorSubtype()).isEqualTo(Constant.INDEPENDENT_SUBTYPE_CODE_SAT);
    }

    @Test
    void testSendInformationAffilliationVolunteerToSat_handlesMissingMunicipalityGracefully() {
        Affiliation affiliation = new Affiliation();
        affiliation.setMunicipalityEmployer(99L); // Not in mocked list
        affiliation.setCityMunicipality(100L); // Not in mocked list
        affiliation.setIdentificationDocumentType("CC");
        affiliation.setIdentificationDocumentNumber("123456");
        affiliation.setFirstName("John");
        affiliation.setSurname("Doe");
        affiliation.setAddressEmployer("Employer Address");
        affiliation.setSecondaryPhone1("3216549870");
        affiliation.setSecondaryEmail("employer@email.com");
        affiliation.setAddress("Worker Address");
        affiliation.setPhone1("9876543210");
        affiliation.setEmail("worker@email.com");
        affiliation.setContractIbcValue(new BigDecimal("1500000"));

        MunicipalityDTO municipality = new MunicipalityDTO();
        municipality.setIdMunicipality(10L);
        municipality.setDivipolaCode("11001");
        BodyResponseConfig<List<MunicipalityDTO>> municipalitiesResponse = new BodyResponseConfig<>();
        municipalitiesResponse.setData(List.of(municipality));
        when(webClient.getMunicipalities()).thenReturn(municipalitiesResponse);

        // doNothing().when(webClient).sendAffiliationIndependentToSat(any(AffiliationWorkerIndependentArlDTO.class));

        try {
            java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod(
                    "sendInformationAffilliationVolunteerToSat", Affiliation.class, String.class);
            method.setAccessible(true);
            method.invoke(affiliateService, affiliation, "POL123");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ArgumentCaptor<AffiliationWorkerIndependentArlDTO> captor = ArgumentCaptor
                .forClass(AffiliationWorkerIndependentArlDTO.class);
        verify(webClient).sendAffiliationIndependentToSat(captor.capture());
        AffiliationWorkerIndependentArlDTO dto = captor.getValue();

        assertThat(dto.getResponsibleAffiliationMunicipalityLocation()).isEqualTo("");
        assertThat(dto.getWorkerMunicipalityWork()).isEqualTo("");
    }

    @Test
    void testSingAffiliateMercantile_success() {
        AffiliateMercantile merc = new AffiliateMercantile();
        merc.setIdUserPreRegister(10L);
        merc.setFiledNumber("SOL_AFI_2025000001600");
        merc.setAffiliationCancelled(false);
        merc.setStatusDocument(false);
        merc.setStageManagement("PREVIOUS_STAGE");
        merc.setTypeDocumentIdentification("CC");
        merc.setNumberIdentification("123456");
        merc.setDecentralizedConsecutive(5L);
        merc.setBusinessName("BUSINESS");
        merc.setTypeAffiliation(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        merc.setDateInterview(LocalDateTime.now());
        merc.setEconomicActivity(new ArrayList<>());

        UserMain userMain = new UserMain();
        userMain.setId(10L);
        userMain.setFirstName("Jane");
        userMain.setSurname("Smith");
        userMain.setUserType(2L);

        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("SOL_AFI_2025000001600");
        affiliate.setIdAffiliate(20L);
        affiliate.setCompany("COMPANY");

        when(iUserPreRegisterRepository.findById(10L)).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        Role role = new Role();
        role.setId(1L);
        role.setCode("ROLE");
        role.setRoleName("ROLE");
        when(rolesUserService.findByName(any())).thenReturn(role);

        Policy policy = new Policy();
        policy.setCode("24902500000000021088");
        policy.setNumPolicyClient(70000001L);
        when(policyService.createPolicy(any(), any(), any(), any(), any(), any(), any())).thenReturn(policy);

        // Use reflection to invoke the non-public method
        try {
            java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod(
                    "singAffiliateMercantile", AffiliateMercantile.class);
            method.setAccessible(true);
            method.invoke(affiliateService, merc);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        verify(mercantileRepository).save(merc);
        verify(rolesUserService).updateRoleUser(eq(10L), any());
        verify(policyService, times(2)).createPolicy(any(), any(), any(), any(), any(), any(), any());
        verify(affiliateRepository).save(affiliate);
        verify(sendEmails).welcomeMercantile(any());
    }

    @Test
    void testSingAffiliateMercantile_affiliationCancelled_throws() {
        AffiliateMercantile merc = new AffiliateMercantile();
        merc.setIdUserPreRegister(10L);
        merc.setFiledNumber("SOL_AFI_2025000001601");
        merc.setAffiliationCancelled(true);
        merc.setStatusDocument(false);
        merc.setStageManagement("PREVIOUS_STAGE");

        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("SOL_AFI_2025000001601");

        when(iUserPreRegisterRepository.findById(10L)).thenReturn(Optional.of(new UserMain()));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        // Use reflection to invoke the non-public method
        try {
            java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod(
                    "singAffiliateMercantile", AffiliateMercantile.class);
            method.setAccessible(true);
            assertThatThrownBy(() -> {
                try {
                    method.invoke(affiliateService, merc);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    // Propagate the real exception thrown by the method
                    throw e.getCause();
                }
            }).isInstanceOf(com.gal.afiliaciones.config.ex.affiliation.AffiliationError.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSingAffiliateMercantile_statusDocument_throws() {
        AffiliateMercantile merc = new AffiliateMercantile();
        merc.setIdUserPreRegister(10L);
        merc.setFiledNumber("SOL_AFI_2025000001602");
        merc.setAffiliationCancelled(false);
        merc.setStatusDocument(true);
        merc.setStageManagement("PREVIOUS_STAGE");

        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("SOL_AFI_2025000001602");

        when(iUserPreRegisterRepository.findById(10L)).thenReturn(Optional.of(new UserMain()));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        // Use reflection to invoke the non-public method
        try {
            java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod(
                    "singAffiliateMercantile", AffiliateMercantile.class);
            method.setAccessible(true);
            assertThatThrownBy(() -> {
                try {
                    method.invoke(affiliateService, merc);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    // Propagate the real exception thrown by the method
                    throw e.getCause();
                }
            }).isInstanceOf(com.gal.afiliaciones.config.ex.affiliation.AffiliationError.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSingAffiliateMercantile_stageManagementRegularization_throws() {
        AffiliateMercantile merc = new AffiliateMercantile();
        merc.setIdUserPreRegister(10L);
        merc.setFiledNumber("SOL_AFI_2025000001603");
        merc.setAffiliationCancelled(false);
        merc.setStatusDocument(false);
        merc.setStageManagement(Constant.REGULARIZATION);

        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("SOL_AFI_2025000001603");

        when(iUserPreRegisterRepository.findById(10L)).thenReturn(Optional.of(new UserMain()));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        // Use reflection to invoke the non-public method
        try {
            java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod(
                    "singAffiliateMercantile", AffiliateMercantile.class);
            method.setAccessible(true);
            assertThatThrownBy(() -> {
                try {
                    method.invoke(affiliateService, merc);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    // Propagate the real exception thrown by the method
                    throw e.getCause();
                }
            }).isInstanceOf(com.gal.afiliaciones.config.ex.affiliation.AffiliationError.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSingAffiliateMercantile_stageManagementInterviewWeb_throws() {
        AffiliateMercantile merc = new AffiliateMercantile();
        merc.setIdUserPreRegister(10L);
        merc.setFiledNumber("SOL_AFI_2025000001604");
        merc.setAffiliationCancelled(false);
        merc.setStatusDocument(false);
        merc.setStageManagement("entrevista web");

        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("SOL_AFI_2025000001604");

        when(iUserPreRegisterRepository.findById(10L)).thenReturn(Optional.of(new UserMain()));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        // Usando reflection para invocar el método privado/protegido si no es público
        try {
            java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod(
                    "singAffiliateMercantile", AffiliateMercantile.class);
            method.setAccessible(true);
            assertThatThrownBy(() -> {
                try {
                    method.invoke(affiliateService, merc);
                } catch (java.lang.reflect.InvocationTargetException e) {
                    // Propaga la excepción real lanzada por el método
                    throw e.getCause();
                }
            }).isInstanceOf(com.gal.afiliaciones.config.ex.affiliation.AffiliationError.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetDataStatusAffiliations_returnsEmptyList_whenNoAffiliates() {
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "123")).thenReturn(List.of());

        List<DataStatusAffiliationDTO> result = affiliateService.getDataStatusAffiliations("123", "CC");

        assertThat(result).isEmpty();
    }

    @Test
    void testGetDataStatusAffiliations_returnsEmptyList_whenOnlyDependents() {
        Affiliate dependent = new Affiliate();
        dependent.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "123")).thenReturn(List.of(dependent));

        List<DataStatusAffiliationDTO> result = affiliateService.getDataStatusAffiliations("123", "CC");

        assertThat(result).isEmpty();
    }

    @Test
    void testGetDataStatusAffiliations_returnsAffiliationData_whenIndependent() {
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setFiledNumber("SOL_AFI_2025000001450");
        affiliate.setIdAffiliate(1L);
        affiliate.setDateAffiliateSuspend(LocalDateTime.of(2024, 6, 1, 0, 0, 0));
        affiliate.setIdOfficial(10L);

        Affiliation affiliation = new Affiliation();
        affiliation.setFiledNumber("SOL_AFI_2025000001450");
        affiliation.setStageManagement("ACTIVE");
        affiliation.setContractEndDate(LocalDate.of(2024, 12, 31));

        UserMain userMain = new UserMain();
        userMain.setFirstName("Jane");
        userMain.setSurname("Smith");

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "123")).thenReturn(List.of(affiliate));
        when(repositoryAffiliation.findByFiledNumber("SOL_AFI_2025000001450")).thenReturn(Optional.of(affiliation));
        when(iUserPreRegisterRepository.findById(10L)).thenReturn(Optional.of(userMain));

        List<DataStatusAffiliationDTO> result = affiliateService.getDataStatusAffiliations("123", "CC");

        assertThat(result).hasSize(1);
        DataStatusAffiliationDTO dto = result.get(0);
        assertThat(dto.getFiledNumber()).isEqualTo("SOL_AFI_2025000001450");
        assertThat(dto.getStageManagement()).isEqualTo("ACTIVE");
        assertThat(dto.getNameOfficial()).isEqualTo("Jane Smith");
    }

    @Test
    void testGetDataStatusAffiliations_returnsAffiliationData_whenMercantile() {
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType("OTHER_TYPE");
        affiliate.setFiledNumber("SOL_AFI_2025000001455");
        affiliate.setIdAffiliate(2L);

        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setFiledNumber("SOL_AFI_2025000001455");
        mercantile.setStageManagement("ACTIVE");

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "789")).thenReturn(List.of(affiliate));
        when(repositoryAffiliation.findByFiledNumber("SOL_AFI_2025000001455")).thenReturn(Optional.empty());
        when(mercantileRepository.findByFiledNumber("SOL_AFI_2025000001455")).thenReturn(Optional.of(mercantile));

        List<DataStatusAffiliationDTO> result = affiliateService.getDataStatusAffiliations("789", "CC");

        assertThat(result).hasSize(1);
        DataStatusAffiliationDTO dto = result.get(0);
        assertThat(dto.getFiledNumber()).isEqualTo("SOL_AFI_2025000001455");
        assertThat(dto.getStageManagement()).isEqualTo("ACTIVE");
    }

    @Test
    void testGetDataStatusAffiliations_filtersOutNullStageManagement() {
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setFiledNumber("SOL_AFI_2025000001450");

        Affiliation affiliation = new Affiliation();
        affiliation.setFiledNumber("SOL_AFI_2025000001450");
        affiliation.setStageManagement(null);

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "123")).thenReturn(List.of(affiliate));
        when(repositoryAffiliation.findByFiledNumber("SOL_AFI_2025000001450")).thenReturn(Optional.of(affiliation));

        List<DataStatusAffiliationDTO> result = affiliateService.getDataStatusAffiliations("123", "CC");

        assertThat(result).isEmpty();
    }

    @Test
    void testAffiliateBUs_noAffiliateFound() throws Exception {
        String idTipoDoc = "CC";
        String idAfiliado = "000000000";

        when(consultAffiliateCompanyClient.consultAffiliate(idTipoDoc, idAfiliado))
                .thenReturn(reactor.core.publisher.Mono.empty());

        boolean result = affiliateService.affiliateBUs(idTipoDoc, idAfiliado);

        assertThat(result).isFalse();
        verify(filedService, times(0)).getNextFiledNumberAffiliation();
    }

    @Test
    void testAffiliateBUs_independentAffiliate_existingUser() throws Exception {
        String idTipoDoc = "CC";
        String idAfiliado = "123456789";

        AffiliateCompanyResponse companyResponse = new AffiliateCompanyResponse();
        companyResponse.setIdPersona(idAfiliado);
        companyResponse.setTipoDoc(idTipoDoc);
        companyResponse.setNomVinLaboral("Independiente");
        companyResponse.setIdTipoVinculado(1);
        companyResponse.setFechaNacimiento("1990-01-15 00:00:00");
        companyResponse.setEps("EPS001");
        companyResponse.setIdDepartamento(5);
        companyResponse.setIdMunicipio(1);
        companyResponse.setFechaAfiliacionEfectiva("2023-01-01 10:00:00");
        companyResponse.setFechaInicioVinculacion("2023-01-01 10:00:00");
        companyResponse.setRazonSocial("Test Company");
        companyResponse.setIdEmpresa("987654321");
        companyResponse.setEstadoRl("Activo");

        PersonResponse personResponse = new PersonResponse();
        personResponse.setIdTipoDoc(idTipoDoc);
        personResponse.setIdPersona(idAfiliado);
        personResponse.setFechaNacimiento("1990/01/15 00:00:00");

        when(consultAffiliateCompanyClient.consultAffiliate(idTipoDoc, idAfiliado))
                .thenReturn(reactor.core.publisher.Mono.just(List.of(companyResponse)));
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("SOL_AFI_2025000009999");
        when(healthPromotingEntityRepository.findByCodeEPS("EPS001")).thenReturn(Optional.of(new Health()));
        when(municipalityRepository.findByIdDepartmentAndMunicipalityCode(5L, "001"))
                .thenReturn(Optional.of(new Municipality()));
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(new ArlInformation()));
        when(clientPerson.consult(idTipoDoc, idAfiliado)).thenReturn(reactor.core.publisher.Mono.just(List.of(personResponse)));
        when(iUserPreRegisterRepository.count(any(Specification.class))).thenReturn(1L); // User exists

        boolean result = affiliateService.affiliateBUs(idTipoDoc, idAfiliado);

        assertThat(result).isTrue();
        verify(iUserRegisterService, times(0)).userPreRegister(any(UserPreRegisterDto.class)); // Not called
        verify(webClient, times(0)).assignRolesToUser(any(), any()); // Not called
        verify(affiliationDetailRepository, times(1)).save(any(Affiliation.class));
        verify(affiliateRepository, times(1)).save(any(Affiliate.class));
    }

    @Test
    void testRegularizationDocuments_affiliateCancelled_throwsError() {
        String filedNumber = "SOL_AFI_2025000001456";
        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber(filedNumber);
        affiliate.setAffiliationCancelled(true);

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        assertThatThrownBy(() -> affiliateService.regularizationDocuments(filedNumber, List.of()))
                .isInstanceOf(AffiliationError.class);
    }

    @Test
    void testRegularizationDocuments_affiliationNotFound_throwsError() {
        String filedNumber = "SOL_AFI_2025000001457";
        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber(filedNumber);
        affiliate.setAffiliationCancelled(false);

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> affiliateService.regularizationDocuments(filedNumber, List.of()))
                .isInstanceOf(AffiliationError.class);
    }

    @Test
    void testRegularizationDocuments_invalidIdNode_throwsError() {
        String filedNumber = "SOL_AFI_2025000001458";
        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber(filedNumber);
        affiliate.setAffiliationCancelled(false);
        affiliate.setAffiliationSubType("UNKNOWN_SUBTYPE");

        Affiliation affiliation = new Affiliation();
        affiliation.setFiledNumber(filedNumber);

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));

        assertThatThrownBy(() -> affiliateService.regularizationDocuments(filedNumber, List.of()))
                .isInstanceOf(com.gal.afiliaciones.config.ex.alfresco.ErrorFindDocumentsAlfresco.class);
    }

    @Test
    void testRegularizationDocuments_alfrescoIOException_throwsError() throws IOException {
        String filedNumber = "SOL_AFI_2025000001459";
        List<MultipartFile> documents = List.of(mock(MultipartFile.class));

        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber(filedNumber);
        affiliate.setAffiliationCancelled(false);
        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);

        Affiliation affiliation = new Affiliation();
        affiliation.setFiledNumber(filedNumber);
        affiliation.setIdentificationDocumentNumber("12345");

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(properties.getDocumentFolderId()).thenReturn("folderId");
        when(alfrescoService.uploadAffiliationDocuments(any(), any(), any())).thenThrow(new IOException("Alfresco down"));

        assertThatThrownBy(() -> affiliateService.regularizationDocuments(filedNumber, documents))
                .isInstanceOf(com.gal.afiliaciones.config.ex.alfresco.ErrorFindDocumentsAlfresco.class);
    }

    @Test
    void testUpdateDataDocuments_replacesOldDocumentsWithNewOnes() {
        // Arrange
        Long idAffiliate = 123L;
        String numberDocument = "987654";
        // Old documents in DB
        DataDocumentAffiliate oldDoc1 = new DataDocumentAffiliate();
        oldDoc1.setIdAffiliate(idAffiliate);
        oldDoc1.setIdAlfresco("old1");
        DataDocumentAffiliate oldDoc2 = new DataDocumentAffiliate();
        oldDoc2.setIdAffiliate(idAffiliate);
        oldDoc2.setIdAlfresco("old2");
        List<DataDocumentAffiliate> oldDocs = List.of(oldDoc1, oldDoc2);

        // New documents to save
        ReplacedDocumentDTO newDoc1 = new ReplacedDocumentDTO();
        newDoc1.setDocumentId("new1");
        newDoc1.setDocumentName("doc1.pdf");
        ReplacedDocumentDTO newDoc2 = new ReplacedDocumentDTO();
        newDoc2.setDocumentId("new2");
        newDoc2.setDocumentName("doc2.pdf");
        List<ReplacedDocumentDTO> newDocs = List.of(newDoc1, newDoc2);

        when(dataDocumentRepository.findByIdAffiliate(idAffiliate)).thenReturn(oldDocs);
        // documentNameStandardizationService.getName returns the name as is for simplicity
        when(documentNameStandardizationService.getName(any(), any(), any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        // Use reflection to call private method
        try {
            var method = AffiliateServiceImpl.class.getDeclaredMethod("updateDataDocuments", List.class, Long.class, String.class);
            method.setAccessible(true);
            method.invoke(affiliateService, newDocs, idAffiliate, numberDocument);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Assert
        verify(dataDocumentRepository).findByIdAffiliate(idAffiliate);
        verify(dataDocumentRepository).delete(oldDoc1);
        verify(dataDocumentRepository).delete(oldDoc2);
        verify(dataDocumentRepository, times(2)).save(any(DataDocumentAffiliate.class));
        verify(documentNameStandardizationService, times(2)).getName(any(), any(), eq(numberDocument));
    }

    @Test
    void testUpdateDataDocuments_doesNothingIfNoNewDocuments() {
        Long idAffiliate = 123L;
        String numberDocument = "987654";
        List<ReplacedDocumentDTO> emptyDocs = List.of();

        // Act
        try {
            var method = AffiliateServiceImpl.class.getDeclaredMethod("updateDataDocuments", List.class, Long.class, String.class);
            method.setAccessible(true);
            method.invoke(affiliateService, emptyDocs, idAffiliate, numberDocument);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Assert
        verify(dataDocumentRepository, never()).findByIdAffiliate(any());
        verify(dataDocumentRepository, never()).save(any());
        verify(documentNameStandardizationService, never()).getName(any(), any(), any());
    }
    @Test
    void testBuildAffiliateResponseIndependiente() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Arrange
        AffiliateCompanyResponse response = new AffiliateCompanyResponse();
        response.setRazonSocial("Test Company");
        response.setIdEmpresa("123456789");
        response.setIdPersona("987654321");
        response.setTipoDoc("CC");
        response.setNomVinLaboral("Independiente");
        response.setIdTipoVinculado(1); // Expected to map to Constant.BONDING_TYPE_INDEPENDENT
        response.setEstadoRl("Activo"); // Expected to be mapped to "Activa" by mapStatus

        // Act
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("buildAffiliateResponse", AffiliateCompanyResponse.class);
        method.setAccessible(true);
        Affiliate affiliate = (Affiliate) method.invoke(null, response);

        // Assert
        assertThat(affiliate.getCompany()).isEqualTo("Test Company");
        assertThat(affiliate.getNitCompany()).isEqualTo("123456789");
        assertThat(affiliate.getDocumentNumber()).isEqualTo("987654321");
        assertThat(affiliate.getDocumentType()).isEqualTo("CC");
        // For "Independiente", the affiliation type should be set as "Trabajador Independiente"
        assertThat(affiliate.getAffiliationType()).isEqualTo("Trabajador Independiente");
        // Verify that the bonding subtype was mapped correctly
        assertThat(affiliate.getAffiliationSubType()).isEqualTo(com.gal.afiliaciones.infrastructure.utils.Constant.BONDING_TYPE_INDEPENDENT);
        assertThat(affiliate.getAffiliationCancelled()).isFalse();
        assertThat(affiliate.getStatusDocument()).isFalse();
        // affiliationStatus is overriden by mapStatus response: "Activo" -> "Activa"
        assertThat(affiliate.getAffiliationStatus()).isEqualTo("Activa");
        assertThat(affiliate.getNoveltyType()).isEqualTo(com.gal.afiliaciones.infrastructure.utils.Constant.NOVELTY_TYPE_AFFILIATION);
        assertThat(affiliate.getRequestChannel()).isEqualTo(com.gal.afiliaciones.infrastructure.utils.Constant.REQUEST_CHANNEL_PORTAL);
    }

    @Test
    void testBuildAffiliateResponseDependiente() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Arrange
        AffiliateCompanyResponse response = new AffiliateCompanyResponse();
        response.setRazonSocial("Dependent Company");
        response.setIdEmpresa("22334455");
        response.setIdPersona("55443322");
        response.setTipoDoc("TI");
        // For a non "Independiente" value, the affiliation type should be "Trabajador Dependiente"
        response.setNomVinLaboral("Dependiente");
        response.setIdTipoVinculado(4); // Expected to map to Constant.BONDING_TYPE_DEPENDENT
        response.setEstadoRl("Inactivo"); // Expected to be mapped to "Inactiva" by mapStatus

        // Act
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("buildAffiliateResponse", AffiliateCompanyResponse.class);
        method.setAccessible(true);
        Affiliate affiliate = (Affiliate) method.invoke(null, response);

        // Assert
        assertThat(affiliate.getCompany()).isEqualTo("Dependent Company");
        assertThat(affiliate.getNitCompany()).isEqualTo("22334455");
        assertThat(affiliate.getDocumentNumber()).isEqualTo("55443322");
        assertThat(affiliate.getDocumentType()).isEqualTo("TI");
        // For non "Independiente", the affiliation type should be "Trabajador Dependiente"
        assertThat(affiliate.getAffiliationType()).isEqualTo("Trabajador Dependiente");
        // Verify bonding subtype mapping for value 4
        assertThat(affiliate.getAffiliationSubType()).isEqualTo(com.gal.afiliaciones.infrastructure.utils.Constant.BONDING_TYPE_DEPENDENT);
        assertThat(affiliate.getAffiliationCancelled()).isFalse();
        assertThat(affiliate.getStatusDocument()).isFalse();
        // affiliationStatus is set based on mapStatus: "Inactivo" -> "Inactiva"
        assertThat(affiliate.getAffiliationStatus()).isEqualTo("Inactiva");
        assertThat(affiliate.getNoveltyType()).isEqualTo(com.gal.afiliaciones.infrastructure.utils.Constant.NOVELTY_TYPE_AFFILIATION);
        assertThat(affiliate.getRequestChannel()).isEqualTo(com.gal.afiliaciones.infrastructure.utils.Constant.REQUEST_CHANNEL_PORTAL);
    }

    @Test
    void testAssignTemporalPass_success() {
        String email = "test@example.com";
        UserMain userMain = new UserMain();
        userMain.setEmail(email);

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId("keycloak-id");
        userRepresentation.setUsername(email);

        org.keycloak.admin.client.resource.UsersResource usersResourceMock = mock(
                org.keycloak.admin.client.resource.UsersResource.class);
        org.keycloak.admin.client.resource.UserResource userResourceMock = mock(
                org.keycloak.admin.client.resource.UserResource.class);

        when(iUserPreRegisterRepository.findByEmail(email)).thenReturn(Optional.of(userMain));
        when(keycloakService.searchUserByUsername(email)).thenReturn(List.of(userRepresentation));
        when(keyCloakProvider.getUserResource()).thenReturn(usersResourceMock);
        when(usersResourceMock.get("keycloak-id")).thenReturn(userResourceMock);

        String temporalPass = affiliateService.assignTemporalPass(email);

        assertThat(temporalPass).isNotNull().isNotEmpty();
        assertThat(temporalPass.length()).isGreaterThanOrEqualTo(8);

        ArgumentCaptor<UserMain> userMainCaptor = ArgumentCaptor.forClass(UserMain.class);
        verify(iUserPreRegisterRepository).save(userMainCaptor.capture());
        assertThat(userMainCaptor.getValue().getIsTemporalPassword()).isTrue();
        assertThat(userMainCaptor.getValue().getCreatedAtTemporalPassword()).isEqualTo(LocalDate.now());

        ArgumentCaptor<UserRepresentation> userRepCaptor = ArgumentCaptor.forClass(UserRepresentation.class);
        verify(userResourceMock).update(userRepCaptor.capture());
        assertThat(userRepCaptor.getValue().getCredentials()).hasSize(1);
        assertThat(userRepCaptor.getValue().getCredentials().get(0).getValue()).isEqualTo(temporalPass);
    }

    @Test
    void testAssignTemporalPass_userNotFoundInDb_throwsException() {
        String email = "notfound@example.com";
        when(iUserPreRegisterRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> affiliateService.assignTemporalPass(email))
                .isInstanceOf(com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase.class);
    }

    @Test
    void testAssignTemporalPass_userNotFoundInKeycloak_throwsException() {
        String email = "test@example.com";
        UserMain userMain = new UserMain();
        userMain.setEmail(email);

        when(iUserPreRegisterRepository.findByEmail(email)).thenReturn(Optional.of(userMain));
        when(keycloakService.searchUserByUsername(email)).thenReturn(List.of());

        assertThatThrownBy(() -> affiliateService.assignTemporalPass(email))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(Constant.USER_NOT_FOUND);
    }
    @Test
    void testGetUserMain_setsAllFieldsCorrectly() {
        PersonResponse responsePerson = new PersonResponse();
        responsePerson.setIdTipoDoc("CC");
        responsePerson.setIdPersona("123456");
        responsePerson.setNombre1("Juan");
        responsePerson.setNombre2("Carlos");
        responsePerson.setApellido1("Perez");
        responsePerson.setApellido2("Gomez");
        responsePerson.setFechaNacimiento("1990/01/01 00:00:00");
        responsePerson.setSexo("M");
        responsePerson.setTelefonoPersona("3001234567");
        responsePerson.setDireccionPersona("Calle 1 #2-3");
        responsePerson.setEmailPersona("juan.perez@example.com");

        LocalDate dateBirth = LocalDate.of(1990, 1, 1);

        // Use reflection to call the non-public getUserMain method
        UserMain userMain;
        try {
            java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("getUserMain", PersonResponse.class, LocalDate.class);
            method.setAccessible(true);
            userMain = (UserMain) method.invoke(null, responsePerson, dateBirth);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThat(userMain.getIdentificationType()).isEqualTo("CC");
        assertThat(userMain.getIdentification()).isEqualTo("123456");
        assertThat(userMain.getFirstName()).isEqualTo("Juan");
        assertThat(userMain.getSecondName()).isEqualTo("Carlos");
        assertThat(userMain.getSurname()).isEqualTo("Perez");
        assertThat(userMain.getSecondSurname()).isEqualTo("Gomez");
        assertThat(userMain.getDateBirth()).isEqualTo(dateBirth);
        assertThat(userMain.getSex()).isEqualTo("M");
        assertThat(userMain.getPhoneNumber()).isEqualTo("3001234567");
        assertThat(userMain.getAddress()).isEqualTo("Calle 1 #2-3");
        assertThat(userMain.getEmail()).isEqualTo("juan.perez@example.com");
        assertThat(userMain.getIsImport()).isTrue();
        assertThat(userMain.getUserName()).isEqualTo("CC-123456-EXT");
    }

    @Test
    void testGetUserMain_setsDefaultEmailIfNull() {
        PersonResponse responsePerson = new PersonResponse();
        responsePerson.setIdTipoDoc("TI");
        responsePerson.setIdPersona("78910");
        responsePerson.setNombre1("Ana");
        responsePerson.setApellido1("Lopez");
        responsePerson.setFechaNacimiento("2000/05/10 00:00:00");
        responsePerson.setSexo("F");
        responsePerson.setTelefonoPersona("3109876543");
        responsePerson.setDireccionPersona("Carrera 5 #10-20");
        responsePerson.setEmailPersona(null);

        LocalDate dateBirth = LocalDate.of(2000, 5, 10);

        UserMain userMain;
        try {
            java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("getUserMain", PersonResponse.class, LocalDate.class);
            method.setAccessible(true);
            userMain = (UserMain) method.invoke(null, responsePerson, dateBirth);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThat(userMain.getEmail()).isEqualTo("correonoasignado@gmail.com");
    }

    @Test
    void testResponseFoundAffiliate_foundInRepository() {
        String docType = "CC";
        String docNumber = "12345";
        Affiliate affiliate = new Affiliate();
        affiliate.setDocumentNumber(docNumber);
        affiliate.setDocumentType(docType);

        when(affiliateRepository.count(any(Specification.class))).thenReturn(1L);
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        Object result = affiliateService.responseFoundAffiliate(docType, docNumber);

        assertThat(result).isInstanceOf(Optional.class);
        Optional<Affiliate> resultOpt = (Optional<Affiliate>) result;
        assertThat(resultOpt).isPresent();
        assertThat(resultOpt.get()).isEqualTo(affiliate);

        verify(webClient, never()).consultWorkerDTO(any());
    }

    @Test
    void testResponseFoundAffiliate_foundInWebClient_throwsException() {
        String docType = "CC";
        String docNumber = "12345";

        when(affiliateRepository.count(any(Specification.class))).thenReturn(0L);

        com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ResponseConsultWorkerDTO responseDto = new com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ResponseConsultWorkerDTO();
        responseDto.setCausal(1L); // Causal that triggers the exception

        when(webClient.consultWorkerDTO(any())).thenReturn(responseDto);

        assertThatThrownBy(() -> affiliateService.responseFoundAffiliate(docType, docNumber))
                .isInstanceOf(com.gal.afiliaciones.config.ex.affiliation.WSConsultIndependentWorkerFound.class);
    }

    @Test
    void testResponseFoundAffiliate_notFoundInWebClient_causalNotMatching() {
        String docType = "CC";
        String docNumber = "12345";

        when(affiliateRepository.count(any(Specification.class))).thenReturn(0L);

        com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ResponseConsultWorkerDTO responseDto = new com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ResponseConsultWorkerDTO();
        responseDto.setCausal(5L); // Causal that does NOT trigger the exception

        when(webClient.consultWorkerDTO(any())).thenReturn(responseDto);

        assertThatThrownBy(() -> affiliateService.responseFoundAffiliate(docType, docNumber))
                .isInstanceOf(com.gal.afiliaciones.config.ex.affiliation.ResponseMessageAffiliation.class);
    }

    @Test
    void testResponseFoundAffiliate_notFoundAnywhere() {
        String docType = "CC";
        String docNumber = "12345";

        when(affiliateRepository.count(any(Specification.class))).thenReturn(0L);
        when(webClient.consultWorkerDTO(any())).thenReturn(null);

        assertThatThrownBy(() -> affiliateService.responseFoundAffiliate(docType, docNumber))
                .isInstanceOf(com.gal.afiliaciones.config.ex.affiliation.ResponseMessageAffiliation.class);
    }
    @Test
    void testGenerateTemporalPass_meetsCriteria() throws Exception {
        // Use reflection to access the private method generateTemporalPass()
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("generateTemporalPass");
        method.setAccessible(true);
        String password = (String) method.invoke(affiliateService);

        // The generated password length should be between 8 and 12 characters
        assertThat(password.length()).isBetween(8, 12);

        // It should contain at least one lowercase letter, one uppercase letter, one digit, and one special character (@!#$&)
        assertThat(password).matches(".*[a-z].*");
        assertThat(password).matches(".*[A-Z].*");
        assertThat(password).matches(".*\\d.*");
        assertThat(password).matches(".*[@!#$&].*");
    }

    @Test
    void testGenerateTemporalPass_randomness() throws Exception {
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("generateTemporalPass");
        method.setAccessible(true);
        // Generate several passwords and ensure that not all are identical (simple randomness check)
        java.util.Set<String> passwords = new java.util.HashSet<>();
        for (int i = 0; i < 10; i++) {
            String pwd = (String) method.invoke(affiliateService);
            passwords.add(pwd);
        }
        assertThat(passwords.size()).isGreaterThan(1);
    }

    @Test
    void testAssignPolicy_successful() {
        Long idAffiliate = 100L;
        String nitEmployer = "900123456";
        String identificationTypeDependent = "CC";
        String identificationNumberDependent = "123456789";
        Long idPolicyType = 2L;
        String nameCompany = "EMPRESA S.A.";

        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setIdAffiliate(200L);

        Policy employerPolicy = new Policy();
        employerPolicy.setIdPolicyType(idPolicyType);
        employerPolicy.setCode("POL123");

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(employerAffiliate));
        when(policyRepository.findByIdAffiliate(200L)).thenReturn(List.of(employerPolicy));

        // No exception expected
        // Use reflection to invoke the (possibly non-public) assignPolicy method
        try {
            java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod(
                    "assignPolicy", Long.class, String.class, String.class, String.class, Long.class, String.class);
            method.setAccessible(true);
            method.invoke(affiliateService, idAffiliate, nitEmployer, identificationTypeDependent, identificationNumberDependent, idPolicyType, nameCompany);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        verify(policyService).createPolicyDependent(
                eq(identificationTypeDependent),
                eq(identificationNumberDependent),
                any(LocalDate.class),
                eq(idAffiliate),
                eq("POL123"),
                eq(nameCompany)
        );
    }

    @Test
    void testAssignPolicy_employerNotFound_throwsAffiliateNotFound() {
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            try {
                java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod(
                        "assignPolicy", Long.class, String.class, String.class, String.class, Long.class, String.class);
                method.setAccessible(true);
                method.invoke(affiliateService, 1L, "900123456", "CC", "123456789", 2L, "EMPRESA S.A.");
            } catch (java.lang.reflect.InvocationTargetException e) {
                // Propagate the real exception thrown by the method
                throw e.getCause();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).isInstanceOf(com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound.class);
    }

    @Test
    void testAssignPolicy_policyListEmpty_throwsPolicyException() {
        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setIdAffiliate(200L);

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(employerAffiliate));
        when(policyRepository.findByIdAffiliate(200L)).thenReturn(List.of());

        assertThatThrownBy(() -> {
            try {
                java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod(
                        "assignPolicy", Long.class, String.class, String.class, String.class, Long.class, String.class);
                method.setAccessible(true);
                method.invoke(affiliateService, 1L, "900123456", "CC", "123456789", 2L, "EMPRESA S.A.");
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw e.getCause();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).isInstanceOf(com.gal.afiliaciones.config.ex.PolicyException.class);
    }
    @Test
    void testFindUserAffiliate_mercantileFound() {
        String docType = Constant.NI;
        String docNum = "900123456";
        Integer dv = 7;

        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setId(1L);
        mercantile.setBusinessName("Test Mercantile");
        mercantile.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);

        when(mercantileRepository.findByTypeDocumentIdentificationAndNumberIdentificationAndDigitVerificationDV(docType,
                docNum, dv)).thenReturn(Optional.of(mercantile));

        AffiliationResponseDTO result = affiliateService.findUserAffiliate(docType, docNum, dv);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Mercantile");
        verify(affiliateRepository, never()).findAllByDocumentTypeAndDocumentNumber(any(), any());
    }

    @Test
    void testFindUserAffiliate_mercantileFoundButInactive() {
        String docType = Constant.NI;
        String docNum = "900123456";
        Integer dv = 7;

        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setId(1L);
        mercantile.setBusinessName("Test Mercantile");
        mercantile.setAffiliationStatus("Inactive");

        when(mercantileRepository.findByTypeDocumentIdentificationAndNumberIdentificationAndDigitVerificationDV(docType,
                docNum, dv)).thenReturn(Optional.of(mercantile));

        AffiliationResponseDTO result = affiliateService.findUserAffiliate(docType, docNum, dv);

        assertThat(result).isNull();
    }

    @Test
    void testFindUserAffiliate_mercantileNotFound() {
        String docType = Constant.NI;
        String docNum = "900123456";
        Integer dv = 7;

        when(mercantileRepository.findByTypeDocumentIdentificationAndNumberIdentificationAndDigitVerificationDV(docType,
                docNum, dv)).thenReturn(Optional.empty());

        AffiliationResponseDTO result = affiliateService.findUserAffiliate(docType, docNum, dv);

        assertThat(result).isNull();
    }

    @Test
    void testFindUserAffiliate_regularAffiliateFound() {
        String docType = "CC";
        String docNum = "123456789";

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(2L);
        affiliate.setCompany("Test Company");
        affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(docType, docNum)).thenReturn(List.of(affiliate));

        AffiliationResponseDTO result = affiliateService.findUserAffiliate(docType, docNum, null);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Test Company");
        verify(mercantileRepository, never()).findByTypeDocumentIdentificationAndNumberIdentificationAndDigitVerificationDV(any(), any(), any());
    }

    @Test
    void testFindUserAffiliate_regularAffiliateFoundButInactive() {
        String docType = "CC";
        String docNum = "123456789";

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(2L);
        affiliate.setCompany("Test Company");
        affiliate.setAffiliationStatus("Inactive");

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(docType, docNum)).thenReturn(List.of(affiliate));

        AffiliationResponseDTO result = affiliateService.findUserAffiliate(docType, docNum, null);

        assertThat(result).isNull();
    }

    @Test
    void testFindUserAffiliate_regularAffiliateNotFound() {
        String docType = "CC";
        String docNum = "123456789";

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(docType, docNum)).thenReturn(Collections.emptyList());

        AffiliationResponseDTO result = affiliateService.findUserAffiliate(docType, docNum, null);

        assertThat(result).isNull();
    }

    @Test
    void testFindUserAffiliate_multipleRegularAffiliates_returnsFirstActive() {
        String docType = "CC";
        String docNum = "123456789";

        Affiliate inactiveAffiliate = new Affiliate();
        inactiveAffiliate.setIdAffiliate(1L);
        inactiveAffiliate.setCompany("Inactive Company");
        inactiveAffiliate.setAffiliationStatus("Inactive");

        Affiliate activeAffiliate = new Affiliate();
        activeAffiliate.setIdAffiliate(2L);
        activeAffiliate.setCompany("Active Company");
        activeAffiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);

        Affiliate anotherActiveAffiliate = new Affiliate();
        anotherActiveAffiliate.setIdAffiliate(3L);
        anotherActiveAffiliate.setCompany("Another Active Company");
        anotherActiveAffiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(docType, docNum))
                .thenReturn(List.of(inactiveAffiliate, activeAffiliate, anotherActiveAffiliate));

        AffiliationResponseDTO result = affiliateService.findUserAffiliate(docType, docNum, null);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getName()).isEqualTo("Active Company");
    }

    @Test
    void testGetIdNodeByAffiliationType_DomesticServices() {
        String expectedFolderId = "DOC_FOLDER";
        when(properties.getDocumentFolderId()).thenReturn(expectedFolderId);
        String result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(affiliateService, "getIdNodeByAffiliationType", Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        assertThat(result).isEqualTo(expectedFolderId);
    }

    @Test
    void testGetIdNodeByAffiliationType_EmployerMercantile() {
        String expectedFolderId = "MER_FOLDER";
        when(properties.getFolderIdMercantile()).thenReturn(expectedFolderId);
        String result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(affiliateService, "getIdNodeByAffiliationType", Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);
        assertThat(result).isEqualTo(expectedFolderId);
    }

    @Test
    void testGetIdNodeByAffiliationType_IndependentVolunteer() {
        String expectedFolderId = "VOL_FOLDER";
        when(properties.getAffiliationVolunteerFolderId()).thenReturn(expectedFolderId);
        String result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(affiliateService, "getIdNodeByAffiliationType", Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER);
        assertThat(result).isEqualTo(expectedFolderId);
    }

    @Test
    void testGetIdNodeByAffiliationType_TaxiDriver() {
        String expectedFolderId = "TAXI_FOLDER";
        when(properties.getAffiliationTaxiDriverFolderId()).thenReturn(expectedFolderId);
        String result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(affiliateService, "getIdNodeByAffiliationType", Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER);
        assertThat(result).isEqualTo(expectedFolderId);
    }

    @Test
    void testGetIdNodeByAffiliationType_ProvisionServices() {
        String expectedFolderId = "PROV_FOLDER";
        when(properties.getAffiliationProvisionServicesFolderId()).thenReturn(expectedFolderId);
        String result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(affiliateService, "getIdNodeByAffiliationType", Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
        assertThat(result).isEqualTo(expectedFolderId);
    }

    @Test
    void testGetIdNodeByAffiliationType_IndependentCouncillor() {
        String expectedFolderId = "COUN_FOLDER";
        when(properties.getAffiliationCouncillorFolderId()).thenReturn(expectedFolderId);
        String result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(affiliateService, "getIdNodeByAffiliationType", Constant.SUBTYPE_AFFILIATE_INDEPENDENT_COUNCILLOR);
        assertThat(result).isEqualTo(expectedFolderId);
    }

    @Test
    void testGetIdNodeByAffiliationType_InvalidType() {
        String result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(affiliateService, "getIdNodeByAffiliationType", "INVALID_TYPE");
        assertThat(result).isEqualTo("");
    }
    @Test
    void testMap_returnsCorrectSubtypeForKnownIds() {
        // Provision services
        assertThat(AffiliateServiceImpl.map(0)).isEqualTo(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
        assertThat(AffiliateServiceImpl.map(6)).isEqualTo(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
        assertThat(AffiliateServiceImpl.map(10)).isEqualTo(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
        assertThat(AffiliateServiceImpl.map(11)).isEqualTo(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
        assertThat(AffiliateServiceImpl.map(13)).isEqualTo(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
        assertThat(AffiliateServiceImpl.map(37)).isEqualTo(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
        assertThat(AffiliateServiceImpl.map(38)).isEqualTo(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
        assertThat(AffiliateServiceImpl.map(42)).isEqualTo(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
        assertThat(AffiliateServiceImpl.map(43)).isEqualTo(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);

        // Independent
        assertThat(AffiliateServiceImpl.map(1)).isEqualTo(Constant.BONDING_TYPE_INDEPENDENT);

        // Dependent
        assertThat(AffiliateServiceImpl.map(3)).isEqualTo(Constant.BONDING_TYPE_DEPENDENT);
        assertThat(AffiliateServiceImpl.map(4)).isEqualTo(Constant.BONDING_TYPE_DEPENDENT);
        assertThat(AffiliateServiceImpl.map(9)).isEqualTo(Constant.BONDING_TYPE_DEPENDENT);

        // Volunteer
        assertThat(AffiliateServiceImpl.map(12)).isEqualTo(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER);

        // Student
        assertThat(AffiliateServiceImpl.map(34)).isEqualTo(Constant.BONDING_TYPE_STUDENT);

        // Apprentice
        assertThat(AffiliateServiceImpl.map(35)).isEqualTo(Constant.BONDING_TYPE_APPRENTICE);

        // Taxi driver
        assertThat(AffiliateServiceImpl.map(39)).isEqualTo(Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER);
    }

    @Test
    void testMap_returnsNullForUnknownId() {
        assertThat(AffiliateServiceImpl.map(99)).isNull();
        assertThat(AffiliateServiceImpl.map(null)).isNull();
    }
}
