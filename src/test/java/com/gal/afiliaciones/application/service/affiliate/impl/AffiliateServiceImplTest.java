package com.gal.afiliaciones.application.service.affiliate.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
import com.gal.afiliaciones.application.service.affiliate.affiliationemployeractivitiesmercantile.AffiliationEmployerActivitiesMercantileService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.daily.DailyService;
import com.gal.afiliaciones.application.service.documentnamestandardization.DocumentNameStandardizationService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.generalnovelty.impl.GeneralNoveltyServiceImpl;
import com.gal.afiliaciones.application.service.policy.PolicyService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.config.mapper.AffiliateMapper;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.FamilyMember;
import com.gal.afiliaciones.domain.model.Health;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.Occupation;
import com.gal.afiliaciones.domain.model.OccupationDecree1563;
import com.gal.afiliaciones.domain.model.Policy;
import com.gal.afiliaciones.domain.model.Role;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.RequestChannel;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.AffiliationCancellationTimer;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.DataDocumentAffiliate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.client.generic.affiliatecompany.AffiliateCompanyResponse;
import com.gal.afiliaciones.infrastructure.client.generic.affiliatecompany.ConsultAffiliateCompanyClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.ConsultEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.InsertEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.independentrelationship.IndependentContractRelationshipClient;
import com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative.InsertLegalRepresentativeClient;
import com.gal.afiliaciones.infrastructure.client.generic.person.InsertPersonClient;
import com.gal.afiliaciones.infrastructure.client.generic.person.PersonResponse;
import com.gal.afiliaciones.infrastructure.client.generic.person.PersonlClient;
import com.gal.afiliaciones.infrastructure.client.generic.policy.InsertPolicyClient;
import com.gal.afiliaciones.infrastructure.client.generic.volunteer.VolunteerRelationshipClient;
import com.gal.afiliaciones.infrastructure.client.generic.workcenter.InsertWorkCenterClient;
import com.gal.afiliaciones.infrastructure.dao.repository.DateInterviewWebRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationCancellationTimerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IDataDocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.EmployerSizeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.FamilyMemberRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.RequestChannelRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.decree1563.OccupationDecree1563Repository;
import com.gal.afiliaciones.infrastructure.dao.repository.eps.HealthPromotingEntityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.dto.UserPreRegisterDto;
import com.gal.afiliaciones.infrastructure.dto.affiliate.AffiliationResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DataStatusAffiliationDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.EmployerAffiliationHistoryDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.IndividualWorkerAffiliationView;
import com.gal.afiliaciones.infrastructure.dto.affiliate.UserAffiliateDTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ReplacedDocumentDTO;
import com.gal.afiliaciones.infrastructure.dto.municipality.MunicipalityDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.dto.sat.AffiliationWorkerIndependentArlDTO;
import com.gal.afiliaciones.infrastructure.service.RegistraduriaUnifiedService;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.utils.KeyCloakProvider;

import jakarta.persistence.EntityManager;

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
    private GeneralNoveltyServiceImpl generalNoveltyServiceImpl;
    @Mock
    private AffiliateMapper affiliateMapper;
    @Mock
    private InsertPersonClient insertPersonClient;
    @Mock
    private InsertEmployerClient insertEmployerClient;
    @Mock
    private InsertLegalRepresentativeClient insertLegalRepresentativeClient;
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
    private InsertWorkCenterClient insertWorkCenterClient;
    @Mock
    private VolunteerRelationshipClient insertVolunteerClient;
    @Mock
    private FamilyMemberRepository familyMemberRepository;
    @Mock
    private OccupationDecree1563Repository occupationVolunteerRepository;
    @Mock
    private RegistraduriaUnifiedService registraduriaUnifiedService;
    @Mock
    private KeyCloakProvider keyCloakProvider;
    @Mock
    private EntityManager entityManager;
    @InjectMocks
    private AffiliateServiceImpl affiliateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        affiliateService = new AffiliateServiceImpl(
                rolesUserService, affiliateRepository, repositoryAffiliation, sendEmails,
                alfrescoService, properties, dataDocumentRepository, webClient, policyService,
                mercantileRepository, timerRepository, iUserPreRegisterRepository, dateInterviewWebRepository,
                dailyService, cardAffiliatedService, dependentRepository, requestChannelRepository,
                affiliationDetailRepository, affiliateMercantileRepository, keycloakService,
                documentNameStandardizationService, employerSizeRepository, consultAffiliateCompanyClient,
                filedService, arlInformationDao, municipalityRepository, healthPromotingEntityRepository,
                affiliationDependentRepository, policyRepository, iUserRegisterService, clientPerson,
                generalNoveltyServiceImpl, affiliateMapper, insertPersonClient, insertEmployerClient,
                insertLegalRepresentativeClient, independentContractClient, occupationRepository,
                insertPolicyClient, consultEmployerClient, mercantileService, insertWorkCenterClient,
                insertVolunteerClient, familyMemberRepository, occupationVolunteerRepository,
                registraduriaUnifiedService, keyCloakProvider, entityManager);
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
        when(employerSizeRepository.findIdByNumberOfWorkers(5)).thenReturn(Optional.of(1L));

        Long result = affiliateService.getEmployerSize(5);

        assertThat(result).isEqualTo(1L);
        verify(employerSizeRepository).findIdByNumberOfWorkers(5);
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
        when(requestChannelRepository.findByOrderByIdAsc()).thenReturn(List.of(rc2, rc1));

        List<RequestChannel> result = affiliateService.findAllRequestChannel();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        verify(requestChannelRepository).findByOrderByIdAsc();
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
        MunicipalityDTO employerMunicipality = new MunicipalityDTO();
        employerMunicipality.setIdMunicipality(10L);
        employerMunicipality.setDivipolaCode("11001");
        MunicipalityDTO workerMunicipality = new MunicipalityDTO();
        workerMunicipality.setIdMunicipality(20L);
        workerMunicipality.setDivipolaCode("76001");
        BodyResponseConfig<List<MunicipalityDTO>> municipalitiesResponse = new BodyResponseConfig<>();
        municipalitiesResponse.setData(List.of(employerMunicipality, workerMunicipality));
        when(webClient.getMunicipalities()).thenReturn(municipalitiesResponse);
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
        affiliation.setMunicipalityEmployer(99L);
        affiliation.setCityMunicipality(100L);
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
        try {
            java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod(
                    "singAffiliateMercantile", AffiliateMercantile.class);
            method.setAccessible(true);
            assertThatThrownBy(() -> {
                try {
                    method.invoke(affiliateService, merc);
                } catch (java.lang.reflect.InvocationTargetException e) {
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
        try {
            java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod(
                    "singAffiliateMercantile", AffiliateMercantile.class);
            method.setAccessible(true);
            assertThatThrownBy(() -> {
                try {
                    method.invoke(affiliateService, merc);
                } catch (java.lang.reflect.InvocationTargetException e) {
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
        try {
            java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod(
                    "singAffiliateMercantile", AffiliateMercantile.class);
            method.setAccessible(true);
            assertThatThrownBy(() -> {
                try {
                    method.invoke(affiliateService, merc);
                } catch (java.lang.reflect.InvocationTargetException e) {
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

        try {
            java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod(
                    "singAffiliateMercantile", AffiliateMercantile.class);
            method.setAccessible(true);
            assertThatThrownBy(() -> {
                try {
                    method.invoke(affiliateService, merc);
                } catch (java.lang.reflect.InvocationTargetException e) {
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
        assertThat(password).matches(".*[a-z].*");
        assertThat(password).matches(".*[A-Z].*");
        assertThat(password).matches(".*\\d.*");
        assertThat(password).matches(".*[@!#$&].*");
    }

    @Test
    void testGenerateTemporalPass_randomness() throws Exception {
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("generateTemporalPass");
        method.setAccessible(true);
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
        assertThat(AffiliateServiceImpl.map(0)).isEqualTo(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
        assertThat(AffiliateServiceImpl.map(6)).isEqualTo(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
        assertThat(AffiliateServiceImpl.map(10)).isEqualTo(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
        assertThat(AffiliateServiceImpl.map(11)).isEqualTo(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
        assertThat(AffiliateServiceImpl.map(13)).isEqualTo(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
        assertThat(AffiliateServiceImpl.map(37)).isEqualTo(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
        assertThat(AffiliateServiceImpl.map(38)).isEqualTo(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
        assertThat(AffiliateServiceImpl.map(42)).isEqualTo(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
        assertThat(AffiliateServiceImpl.map(43)).isEqualTo(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
        assertThat(AffiliateServiceImpl.map(1)).isEqualTo(Constant.BONDING_TYPE_INDEPENDENT);
        assertThat(AffiliateServiceImpl.map(3)).isEqualTo(Constant.BONDING_TYPE_DEPENDENT);
        assertThat(AffiliateServiceImpl.map(4)).isEqualTo(Constant.BONDING_TYPE_DEPENDENT);
        assertThat(AffiliateServiceImpl.map(9)).isEqualTo(Constant.BONDING_TYPE_DEPENDENT);
        assertThat(AffiliateServiceImpl.map(12)).isEqualTo(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER);
        assertThat(AffiliateServiceImpl.map(34)).isEqualTo(Constant.BONDING_TYPE_STUDENT);
        assertThat(AffiliateServiceImpl.map(35)).isEqualTo(Constant.BONDING_TYPE_APPRENTICE);
        assertThat(AffiliateServiceImpl.map(39)).isEqualTo(Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER);
    }

    @Test
    void testMap_returnsNullForUnknownId() {
        assertThat(AffiliateServiceImpl.map(99)).isNull();
        assertThat(AffiliateServiceImpl.map(null)).isNull();
    }

    @Test
    void testFindAffiliationsByTypeAndNumber_otherType() {
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType("OTHER_TYPE");
        affiliate.setFiledNumber("SOL_AFI_2025000001452");
        List<Affiliate> affiliates = List.of(affiliate);
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "789")).thenReturn(affiliates);
        List<UserAffiliateDTO> result = affiliateService.findAffiliationsByTypeAndNumber("CC", "789");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRetirementDate()).isEqualTo("No registra");
    }
    @Test
    void testFindAffiliationsByTypeAndNumber_independentWithNullEndDate() {
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setFiledNumber("SOL_AFI_2025000001450");
        List<Affiliate> affiliates = List.of(affiliate);

        Affiliation affiliation = new Affiliation();
        affiliation.setContractEndDate(null);

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "123")).thenReturn(affiliates);
        when(repositoryAffiliation.findByFiledNumber("SOL_AFI_2025000001450"))
                .thenReturn(Optional.of(affiliation));

        List<UserAffiliateDTO> result = affiliateService.findAffiliationsByTypeAndNumber("CC", "123");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRetirementDate()).isEqualTo("No registra");
    }

    @Test
    void testFindAffiliationsByTypeAndNumber_dependentWithNullEndDate() {
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setFiledNumber("SOL_AFI_2025000001451");
        List<Affiliate> affiliates = List.of(affiliate);

        AffiliationDependent dep = new AffiliationDependent();
        dep.setEndDate(null);

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "456")).thenReturn(affiliates);
        when(dependentRepository.findByFiledNumber("SOL_AFI_2025000001451")).thenReturn(Optional.of(dep));

        List<UserAffiliateDTO> result = affiliateService.findAffiliationsByTypeAndNumber("CC", "456");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRetirementDate()).isEqualTo("No registra");
    }
    @Test
    void testGetForeignPension_affiliationNotFound() {
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> affiliateService.getForeignPension("SOL_AFI_2025000001999"))
                .isInstanceOf(AffiliationError.class);

    }
    @Test
    void testGetForeignPension_success() {
        Affiliation affiliation = new Affiliation();
        affiliation.setIsForeignPension(true);
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));

        Boolean result = affiliateService.getForeignPension("SOL_AFI_2025000001450");

        assertThat(result).isTrue();
    }


    @Test
    void testSing_employerType_generatesEmployerPolicy() {
        String filedNumber = "SOL_AFI_2025000001450";
        Affiliation affiliation = new Affiliation();
        affiliation.setFiledNumber(filedNumber);
        affiliation.setTypeAffiliation(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        affiliation.setStageManagement("PREVIOUS_STAGE");
        affiliation.setIdentificationDocumentType("CC");
        affiliation.setIdentificationDocumentNumber("123456");

        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber(filedNumber);
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        affiliate.setAffiliationCancelled(false);
        affiliate.setStatusDocument(false);
        affiliate.setUserId(100L);
        affiliate.setIdAffiliate(1L);
        affiliate.setCompany("Test Company");

        Policy policy = new Policy();
        policy.setCode("POL123");
        policy.setId(1L);

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(mercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(rolesUserService.findByName(any())).thenReturn(new Role());
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(new UserMain()));
        when(policyService.createPolicy(any(), any(), any(), any(), any(), any(), any())).thenReturn(policy);
        affiliateService.sing(filedNumber);
        verify(rolesUserService).updateRoleUser(any(), any());
        verify(repositoryAffiliation).save(affiliation);
        verify(affiliateRepository).save(affiliate);
    }

    @Test
    void testCalculateIbcAmount_returnsMaxOfSmlmvAndCalculated() {
        SalaryDTO salaryDTO = new SalaryDTO();
        salaryDTO.setValue(1200L);
        when(webClient.getSmlmvByYear(anyInt())).thenReturn(salaryDTO);
        BigDecimal result = affiliateService.calculateIbcAmount(new BigDecimal("10000"), new BigDecimal("15"));
        assertThat(result.compareTo(new BigDecimal("1500"))).isEqualTo(0);
    }


    @Test
    void testGenerateRandomCharacter() throws Exception {
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("generateRandomCharacter", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(affiliateService, "abc");
        assertThat(result).hasSize(1);
        assertThat("abc").contains(result);
    }
    @Test
    void testGetEmployerSize_noEmployerSizes() {
        when(employerSizeRepository.findIdByNumberOfWorkers(5)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> affiliateService.getEmployerSize(5))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se encontró tamaño de empleador para 5 trabajadores");
    }

    @Test
    void testGetEmployerSize_multipleMatchingEmployerSizes() {
        when(employerSizeRepository.findIdByNumberOfWorkers(7)).thenReturn(Optional.of(2L));
        Long result = affiliateService.getEmployerSize(7);
        assertThat(result).isEqualTo(2L);
        verify(employerSizeRepository).findIdByNumberOfWorkers(7);
    }
    @Test
    void testNumericSuffix() throws Exception {
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("numericSuffix", String.class);
        method.setAccessible(true);
        long result = (long) method.invoke(affiliateService, "SOL_AFI_2025000001450");
        assertThat(result).isEqualTo(2025000001450L);
    }
    @Test
    void testNumericSuffix_differentFormat() throws Exception {
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("numericSuffix", String.class);
        method.setAccessible(true);
        long result = (long) method.invoke(affiliateService, "PREFIX_12345");
        assertThat(result).isEqualTo(12345L);
    }
    @Test
    void testGetDataStatusAffiliations_noDataDaily() {
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
        when(dateInterviewWebRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        List<DataStatusAffiliationDTO> result = affiliateService.getDataStatusAffiliations("789", "CC");
        assertThat(result).hasSize(1);
        DataStatusAffiliationDTO dto = result.get(0);
        assertThat(dto.getDataDailyDTO()).isNull();
    }
    @Test
    void testAssignRole_independentType() throws Exception {
        Role role = new Role();
        role.setId(1L);
        when(rolesUserService.findByName(Constant.BONDING_TYPE_INDEPENDENT)).thenReturn(role);
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("assignRole", Long.class, String.class);
        method.setAccessible(true);
        method.invoke(affiliateService, 100L, Constant.TYPE_AFFILLATE_INDEPENDENT);
        verify(rolesUserService).updateRoleUser(100L, 1L);
    }
    @Test
    void testAssignRole_employerType() throws Exception {
        Role role = new Role();
        role.setId(2L);
        when(rolesUserService.findByName(Constant.NAME_CONTRIBUTOR_TYPE_EMPLOYER)).thenReturn(role);
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("assignRole", Long.class, String.class);
        method.setAccessible(true);
        method.invoke(affiliateService, 100L, Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        verify(rolesUserService).updateRoleUser(100L, 2L);
    }
    @Test
    void testAssignRole_unknownType_throwsException() throws Exception {
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("assignRole", Long.class, String.class);
        method.setAccessible(true);
        assertThatThrownBy(() -> {
            try {
                method.invoke(affiliateService, 100L, "UNKNOWN_TYPE");
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw e.getCause();
            }
        }).isInstanceOf(UserNotFoundInDataBase.class);
    }

    @Test
    void testDeleteTimer() throws Exception {
        AffiliationCancellationTimer timer = new AffiliationCancellationTimer();
        when(timerRepository.findAll(any(Specification.class))).thenReturn(List.of(timer));
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("deleteTimer", String.class, String.class, char.class);
        method.setAccessible(true);
        method.invoke(affiliateService, "123456", "CC", 'D');

        verify(timerRepository).delete(timer);
    }

    @Test
    void testGenerateEmployerPolicy() throws Exception {
        Policy policy = new Policy();
        policy.setCode("POL123");
        when(policyService.createPolicy(any(), any(), any(), any(), any(), any(), any())).thenReturn(policy);
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod(
                "generateEmployerPolicy", String.class, String.class, Long.class, Long.class, String.class);
        method.setAccessible(true);
        method.invoke(affiliateService, "CC", "123456", 1L, 0L, "Test Company");
        verify(policyService, times(2)).createPolicy(any(), any(), any(), any(), any(), any(), any());
    }
    @Test
    void testGetDaneCodeByMunicipality_municipalityFound() throws Exception {
        MunicipalityDTO municipality = new MunicipalityDTO();
        municipality.setIdMunicipality(10L);
        municipality.setDivipolaCode("11001");
        BodyResponseConfig<List<MunicipalityDTO>> response = new BodyResponseConfig<>();
        response.setData(List.of(municipality));
        when(webClient.getMunicipalities()).thenReturn(response);
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("getDaneCodeByMunicipality", Long.class);
        method.setAccessible(true);
        String result = (String) method.invoke(affiliateService, 10L);
        assertThat(result).isEqualTo("11001");
    }

    @Test
    void testGetDaneCodeByMunicipality_municipalityNotFound() throws Exception {
        MunicipalityDTO municipality = new MunicipalityDTO();
        municipality.setIdMunicipality(10L);
        municipality.setDivipolaCode("11001");
        BodyResponseConfig<List<MunicipalityDTO>> response = new BodyResponseConfig<>();
        response.setData(List.of(municipality));
        when(webClient.getMunicipalities()).thenReturn(response);
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("getDaneCodeByMunicipality", Long.class);
        method.setAccessible(true);
        String result = (String) method.invoke(affiliateService, 99L);

        assertThat(result).isEqualTo("");
    }
    @Test
    void testGetDaneCodeByMunicipality_emptyList() throws Exception {
        BodyResponseConfig<List<MunicipalityDTO>> response = new BodyResponseConfig<>();
        response.setData(List.of());
        when(webClient.getMunicipalities()).thenReturn(response);
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("getDaneCodeByMunicipality", Long.class);
        method.setAccessible(true);
        String result = (String) method.invoke(affiliateService, 10L);
        assertThat(result).isEqualTo("");
    }

    @Test
    void testAffiliateBUs_employerNotExists() throws Exception {
        AffiliateCompanyResponse response = new AffiliateCompanyResponse();
        response.setIdPersona("123456789");
        response.setIdEmpresa("900123456");
        when(consultAffiliateCompanyClient.consultAffiliate("CC", "123456789")).thenReturn(reactor.core.publisher.Mono.just(List.of(response)));
        when(affiliateRepository.existsByNitCompanyAndAffiliationType("900123456", "Empleador")).thenReturn(false);
        boolean result = affiliateService.affiliateBUs("CC", "123456789");
        assertThat(result).isTrue();
        verify(filedService, never()).getNextFiledNumberAffiliation();
    }

    @Test
    void testAffiliateBUs_unknownBondingType() throws Exception {
        AffiliateCompanyResponse response = new AffiliateCompanyResponse();
        response.setIdPersona("123456789");
        response.setIdEmpresa("900123456");
        response.setIdTipoVinculado(999);
        when(consultAffiliateCompanyClient.consultAffiliate("CC", "123456789")).thenReturn(reactor.core.publisher.Mono.just(List.of(response)));
        when(affiliateRepository.existsByNitCompanyAndAffiliationType("900123456", "Empleador")).thenReturn(true);
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("SOL_AFI_2025000001450");
        boolean result = affiliateService.affiliateBUs("CC", "123456789");
        assertThat(result).isTrue();
        verify(affiliateRepository, never()).save(any());
    }
    @Test
    void testMapStatus_knownStatuses() {
        assertThat(AffiliateServiceImpl.mapStatus("Activo")).isEqualTo("Activa");
        assertThat(AffiliateServiceImpl.mapStatus("Inactivo")).isEqualTo("Inactiva");
        assertThat(AffiliateServiceImpl.mapStatus("Unknown")).isNull();
        assertThat(AffiliateServiceImpl.mapStatus(null)).isNull();
    }
    @Test
    void testGetEmployerAffiliationHistory() {
        String nitCompany = "900123456";
        String documentType = "CC";
        String documentNumber = "123456789";
        List<Affiliate> affiliates = List.of(new Affiliate());
        List<EmployerAffiliationHistoryDTO> expectedDTOs = List.of(new EmployerAffiliationHistoryDTO());
        when(affiliateRepository.findByNitCompanyAndDocumentTypeAndDocumentNumberAndAffiliationType(
                nitCompany, documentType, documentNumber, Constant.TYPE_AFFILLATE_EMPLOYER))
                .thenReturn(affiliates);
        when(affiliateMapper.toEmployerAffiliationHistoryDTOList(affiliates)).thenReturn(expectedDTOs);

        List<EmployerAffiliationHistoryDTO> result = affiliateService.getEmployerAffiliationHistory(
                nitCompany, documentType, documentNumber);

        assertThat(result).isEqualTo(expectedDTOs);
    }

    @Test
    void testGetIndividualWorkerAffiliation_notFound() {
        String nitCompany = "900123456";
        String documentType = "CC";
        String documentNumber = "123456789";
        when(affiliateRepository.findIndividualWorkerAffiliation(any(Affiliate.class)))
                .thenReturn(Optional.empty());
        IndividualWorkerAffiliationView result = affiliateService.getIndividualWorkerAffiliation(
                nitCompany, documentType, documentNumber);
        assertThat(result).isNull();
    }

    @Test
    void testDateUtilsSafeParse_validDate() {
        String validDate = "2024-01-01 00:00:00";
        LocalDate result = AffiliateServiceImpl.DateUtils.safeParse(validDate);

        assertThat(result).isEqualTo(LocalDate.of(2024, 1, 1));
    }
    @Test
    void testDateUtilsSafeParse_invalidDate() {
        String invalidDate = "invalid-date";
        LocalDate result = AffiliateServiceImpl.DateUtils.safeParse(invalidDate);
        assertThat(result).isNull();
    }

    @Test
    void testDateUtilsSafeParse_nullDate() {
        LocalDate result = AffiliateServiceImpl.DateUtils.safeParse(null);
        assertThat(result).isNull();
    }

    @Test
    void testDateUtilsFormatToIsoDate() {
        String rawDateTime = "1990/01/01 00:00:00";
        LocalDate result = AffiliateServiceImpl.DateUtils.formatToIsoDate(rawDateTime);
        assertThat(result).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    void testAgeCalculator_validBirthDate() {
        LocalDate birthDate = LocalDate.now().minusYears(25);
        String result = AffiliateServiceImpl.AgeCalculator.calculate(birthDate);
        assertThat(result).isEqualTo("25");
    }

    @Test
    void testAgeCalculator_nullBirthDate() {
        String result = AffiliateServiceImpl.AgeCalculator.calculate(null);
        assertThat(result).isNull();
    }

    @Test
    void testAgeCalculatorInt_validBirthDate() {
        LocalDate birthDate = LocalDate.now().minusYears(30);
        Integer result = AffiliateServiceImpl.AgeCalculatorInt.calculate(birthDate);
        assertThat(result).isEqualTo(30);
    }

    @Test
    void testAgeCalculatorInt_nullBirthDate() {
        Integer result = AffiliateServiceImpl.AgeCalculatorInt.calculate(null);
        assertThat(result).isEqualTo(0);
    }

    @Test
    void testSing_independentType_createsPolicyAndCard() {
        String filedNumber = "SOL_AFI_2025000001450";

        Affiliation affiliation = new Affiliation();
        affiliation.setFiledNumber(filedNumber);
        affiliation.setTypeAffiliation(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliation.setStageManagement("PREVIOUS_STAGE");
        affiliation.setIdentificationDocumentType("CC");
        affiliation.setIdentificationDocumentNumber("123456");

        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber(filedNumber);
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setAffiliationSubType(Constant.BONDING_TYPE_INDEPENDENT);
        affiliate.setAffiliationCancelled(false);
        affiliate.setStatusDocument(false);
        affiliate.setUserId(100L);
        affiliate.setIdAffiliate(1L);
        affiliate.setCompany("Test Company");
        affiliate.setDocumentType("CC");
        affiliate.setDocumentNumber("123456");

        Policy policy = new Policy();
        policy.setCode("POL123");
        policy.setId(1L);

        UserMain userMain = new UserMain();
        userMain.setIdentificationType("CC");
        userMain.setIdentification("123456");

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(mercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(rolesUserService.findByName(any())).thenReturn(new Role());
        when(policyService.createPolicy(any(), any(), any(), any(), any(), any(), any())).thenReturn(policy);
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(userMain));

        // act
        affiliateService.sing(filedNumber);

        // assert
        verify(policyService, times(1)).createPolicy(any(), any(), any(), any(), any(), any(), any());
        verify(cardAffiliatedService).createCardWithoutOtp(filedNumber);
        verify(rolesUserService).updateRoleUser(any(), any());
        verify(repositoryAffiliation).save(affiliation);
        verify(affiliateRepository).save(affiliate);
        verifyNoMoreInteractions(policyService);
    }


    @Test
    void testAffiliateBUs_responseFound_dependentType() throws Exception {
        AffiliateCompanyResponse response = new AffiliateCompanyResponse();
        response.setIdPersona("123456789");
        response.setTipoDoc("CC");
        response.setIdEmpresa("900123456");
        response.setRazonSocial("Test Company");
        response.setNomVinLaboral("Dependiente");
        response.setIdTipoVinculado(3);
        response.setEstadoRl("Activo");
        response.setFechaNacimiento("1990-01-01 00:00:00");
        response.setFechaAfiliacionEfectiva("2024-01-01 00:00:00");
        response.setFechaInicioVinculacion("2024-01-01 00:00:00");
        response.setIdDepartamento(11);
        response.setIdMunicipio(1);
        response.setIdActEconomica(1234L);
        response.setOcupacion("Engineer");
        response.setIdOcupacion(100);
        Municipality municipality = new Municipality();
        municipality.setIdMunicipality(1L);
        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setIdAffiliate(200L);
        employerAffiliate.setNitCompany("900123456");
        Policy employerPolicy = new Policy();
        employerPolicy.setIdPolicyType(Constant.ID_EMPLOYER_POLICY);
        employerPolicy.setCode("POL123");
        when(consultAffiliateCompanyClient.consultAffiliate("CC", "123456789")).thenReturn(reactor.core.publisher.Mono.just(List.of(response)));
        when(affiliateRepository.existsByNitCompanyAndAffiliationType("900123456", "Empleador")).thenReturn(true);
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("SOL_AFI_2025000001450");
        when(municipalityRepository.findByIdDepartmentAndMunicipalityCode(11L, "001")).thenReturn(Optional.of(municipality));
        when(healthPromotingEntityRepository.findByCodeEPS(any())).thenReturn(Optional.of(new Health()));
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(new ArlInformation()));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(employerAffiliate));
        when(policyRepository.findByIdAffiliate(200L)).thenReturn(List.of(employerPolicy));
        boolean result = affiliateService.affiliateBUs("CC", "123456789");
        assertThat(result).isTrue();
        verify(affiliateRepository).save(any(Affiliate.class));
        verify(affiliationDependentRepository).save(any(AffiliationDependent.class));
    }


    @Test
    void testStructureUsername() throws Exception {
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("structureUsername", String.class, String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(affiliateService, "CC", "123456789");
        assertThat(result).isEqualTo("CC-123456789-EXT");
    }

    @Test
    void testFindMainEconomicActivity() throws Exception {
        AffiliateActivityEconomic primary = new AffiliateActivityEconomic();
        primary.setIsPrimary(true);
        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setEconomicActivityCode("1234");
        primary.setActivityEconomic(economicActivity);
        AffiliateActivityEconomic secondary = new AffiliateActivityEconomic();
        secondary.setIsPrimary(false);
        List<AffiliateActivityEconomic> activities = List.of(primary, secondary);
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("findMainEconomicActivty", List.class);
        method.setAccessible(true);
        Long result = (Long) method.invoke(affiliateService, activities);
        assertThat(result).isEqualTo(1234L);
    }



    @Test
    void testConvertIdMunicipality() throws Exception {
        Municipality municipality = new Municipality();
        municipality.setMunicipalityCode("001");
        when(municipalityRepository.findById(1L)).thenReturn(Optional.of(municipality));
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("convertIdMunicipality", Long.class);
        method.setAccessible(true);
        Integer result = (Integer) method.invoke(affiliateService, 1L);
        assertThat(result).isEqualTo(1);
    }

    @Test
    void testConvertIdMunicipality_null() throws Exception {
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("convertIdMunicipality", Long.class);
        method.setAccessible(true);
        Integer result = (Integer) method.invoke(affiliateService, (Long) null);
        assertThat(result).isNull();
    }

    @Test
    void testFindEpsCode() throws Exception {
        Health eps = new Health();
        eps.setCodeEPS("EPS001");
        when(healthPromotingEntityRepository.findById(1L)).thenReturn(Optional.of(eps));
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("findEpsCode", Long.class);
        method.setAccessible(true);
        String result = (String) method.invoke(affiliateService, 1L);
        assertThat(result).isEqualTo("EPS001");
    }

    @Test
    void testFindEpsCode_null() throws Exception {
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("findEpsCode", Long.class);
        method.setAccessible(true);
        String result = (String) method.invoke(affiliateService, (Long) null);
        assertThat(result).isNull();
    }

    @Test
    void testConvertTipoVinculadoIndependent() throws Exception {
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("convertTipoVinculadoIndependent", String.class);
        method.setAccessible(true);
        Integer volunteer = (Integer) method.invoke(affiliateService, Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER);
        Integer taxi = (Integer) method.invoke(affiliateService, Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER);
        Integer other = (Integer) method.invoke(affiliateService, "OTHER");
        assertThat(volunteer).isEqualTo(12);
        assertThat(taxi).isEqualTo(39);
        assertThat(other).isEqualTo(0);
    }

    @Test
    void testConvertTransportSupply() throws Exception {
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("convertTransportSupply", Boolean.class);
        method.setAccessible(true);
        String yes = (String) method.invoke(affiliateService, Boolean.TRUE);
        String no = (String) method.invoke(affiliateService, Boolean.FALSE);
        String nullValue = (String) method.invoke(affiliateService, (Boolean) null);
        assertThat(yes).isEqualTo("S");
        assertThat(no).isEqualTo("N");
        assertThat(nullValue).isEqualTo("N");
    }

    @Test
    void testGetMonthsByDuration() throws Exception {
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("getMonthsByDuration", String.class);
        method.setAccessible(true);
        Integer months = (Integer) method.invoke(affiliateService, "Meses: 12");
        Integer noMatch = (Integer) method.invoke(affiliateService, "No match");
        Integer nullValue = (Integer) method.invoke(affiliateService, (String) null);
        assertThat(months).isEqualTo(12);
        assertThat(noMatch).isEqualTo(0);
        assertThat(nullValue).isEqualTo(0);
    }

    @Test
    void testConvertContractClass() throws Exception {
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("convertContractClass", String.class);
        method.setAccessible(true);
        Integer publicContract = (Integer) method.invoke(affiliateService, "Publico");
        Integer privateContract = (Integer) method.invoke(affiliateService, "Privado");
        Integer nullValue = (Integer) method.invoke(affiliateService, (String) null);
        assertThat(publicContract).isEqualTo(1);
        assertThat(privateContract).isEqualTo(2);
        assertThat(nullValue).isEqualTo(2);
    }

    @Test
    void testConvertContractType() throws Exception {
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("convertContractType", String.class);
        method.setAccessible(true);
        Integer admin = (Integer) method.invoke(affiliateService, Constant.CONTRACT_TYPE_ADMINISTRATIVE);
        Integer civil = (Integer) method.invoke(affiliateService, Constant.CONTRACT_TYPE_CIVIL);
        Integer commercial = (Integer) method.invoke(affiliateService, Constant.CONTRACT_TYPE_COMMERCIAL);
        Integer other = (Integer) method.invoke(affiliateService, "OTHER");
        assertThat(admin).isEqualTo(1);
        assertThat(civil).isEqualTo(2);
        assertThat(commercial).isEqualTo(3);
        assertThat(other).isEqualTo(1);
    }

    @Test
    void testFindIdSubEmployer() throws Exception {
        AffiliateMercantile affiliation = new AffiliateMercantile();
        affiliation.setDecentralizedConsecutive(5L);
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("findIdSubEmployer", AffiliateMercantile.class);
        method.setAccessible(true);
        Integer result = (Integer) method.invoke(affiliateService, affiliation);
        Integer nullResult = (Integer) method.invoke(affiliateService, (AffiliateMercantile) null);
        assertThat(result).isEqualTo(5);
        assertThat(nullResult).isEqualTo(0);
    }

    @Test
    void testFindEntityTypeByContractor() throws Exception {
        AffiliateMercantile publicEntity = new AffiliateMercantile();
        publicEntity.setLegalStatus("1");
        AffiliateMercantile privateEntity = new AffiliateMercantile();
        privateEntity.setLegalStatus("2");
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("findEntityTypeByContractor", AffiliateMercantile.class);
        method.setAccessible(true);
        Integer publicResult = (Integer) method.invoke(affiliateService, publicEntity);
        Integer privateResult = (Integer) method.invoke(affiliateService, privateEntity);
        Integer nullResult = (Integer) method.invoke(affiliateService, (AffiliateMercantile) null);
        assertThat(publicResult).isEqualTo(1);
        assertThat(privateResult).isEqualTo(2);
        assertThat(nullResult).isEqualTo(2);
    }

    @Test
    void testFindOccupationByDescription() throws Exception {
        Occupation occupation = new Occupation();
        occupation.setCodeOccupation("1234");
        when(occupationRepository.findByNameOccupation("ENGINEER")).thenReturn(Optional.of(occupation));
        when(occupationRepository.findByNameOccupation("UNKNOWN")).thenReturn(Optional.empty());
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("findOccupationByDescription", String.class);
        method.setAccessible(true);
        Integer found = (Integer) method.invoke(affiliateService, "Engineer");
        Integer notFound = (Integer) method.invoke(affiliateService, "Unknown");
        Integer nullValue = (Integer) method.invoke(affiliateService, (String) null);
        assertThat(found).isEqualTo(1234);
        assertThat(notFound).isNull();
        assertThat(nullValue).isNull();
    }


    @Test
    void testSearchEmployer() throws Exception {
        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setFiledNumber("SOL_AFI_123");
        Affiliate affiliate = new Affiliate();
        affiliate.setCompany("Test Company");
        affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);
        affiliate.setFiledNumber("SOL_AFI_123");
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(List.of(affiliate));
        when(affiliateMercantileRepository.findByFiledNumber("SOL_AFI_123")).thenReturn(Optional.of(mercantile));
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("searchEmployer", String.class, String.class, String.class);
        method.setAccessible(true);
        AffiliateMercantile result = (AffiliateMercantile) method.invoke(affiliateService, "NI", "123456", "Test Company");
        assertThat(result).isEqualTo(mercantile);
    }

    @Test
    void testSearchEmployer_notFound() throws Exception {
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(List.of());
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("searchEmployer", String.class, String.class, String.class);
        method.setAccessible(true);
        AffiliateMercantile result = (AffiliateMercantile) method.invoke(affiliateService, "NI", "123456", "Test Company");
        assertThat(result).isNull();
    }

    @Test
    void testResponseFoundAffiliate_foundMultiple() {
        when(affiliateRepository.count(any(Specification.class))).thenReturn(2L);
        Affiliate affiliate = new Affiliate();
        affiliate.setDocumentNumber("123456");
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        Object result = affiliateService.responseFoundAffiliate("CC", "123456");
        assertThat(result).isInstanceOf(Optional.class);
        Optional<Affiliate> optResult = (Optional<Affiliate>) result;
        assertThat(optResult).isPresent();
        assertThat(optResult.get().getDocumentNumber()).isEqualTo("123456");
    }

    @Test
    void testGetDataStatusAffiliations_multipleMercantileAffiliates() {
        Affiliate affiliate1 = new Affiliate();
        affiliate1.setAffiliationType("OTHER_TYPE");
        affiliate1.setFiledNumber("SOL_AFI_001");
        affiliate1.setIdAffiliate(1L);
        Affiliate affiliate2 = new Affiliate();
        affiliate2.setAffiliationType("OTHER_TYPE");
        affiliate2.setFiledNumber("SOL_AFI_002");
        affiliate2.setIdAffiliate(2L);
        AffiliateMercantile mercantile1 = new AffiliateMercantile();
        mercantile1.setFiledNumber("SOL_AFI_001");
        mercantile1.setStageManagement("ACTIVE");
        AffiliateMercantile mercantile2 = new AffiliateMercantile();
        mercantile2.setFiledNumber("SOL_AFI_002");
        mercantile2.setStageManagement("PENDING");
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "123")).thenReturn(List.of(affiliate1, affiliate2));
        when(repositoryAffiliation.findByFiledNumber("SOL_AFI_001")).thenReturn(Optional.empty());
        when(repositoryAffiliation.findByFiledNumber("SOL_AFI_002")).thenReturn(Optional.empty());
        when(mercantileRepository.findByFiledNumber("SOL_AFI_001")).thenReturn(Optional.of(mercantile1));
        when(mercantileRepository.findByFiledNumber("SOL_AFI_002")).thenReturn(Optional.of(mercantile2));
        when(dateInterviewWebRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        List<DataStatusAffiliationDTO> result = affiliateService.getDataStatusAffiliations("123", "CC");
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFiledNumber()).isEqualTo("SOL_AFI_001");
        assertThat(result.get(1).getFiledNumber()).isEqualTo("SOL_AFI_002");
    }
    @Test
    void testInsertPersonToClient_withAllFields() throws Exception {
        UserMain user = new UserMain();
        user.setIdentificationType("CC");
        user.setIdentification("123456789");
        user.setPensionFundAdministrator(1L);
        user.setIdDepartment(11L);
        user.setIdCity(1L);
        user.setHealthPromotingEntity(1L);
        user.setFirstName("John");
        user.setSecondName("Carlos");
        user.setSurname("Doe");
        user.setSecondSurname("Smith");
        user.setDateBirth(LocalDate.of(1990, 1, 1));
        user.setSex("M");
        user.setPhoneNumber("3001234567");
        user.setAddress("Calle 123 #45-67");
        user.setEmail("john@example.com");
        Municipality municipality = new Municipality();
        municipality.setMunicipalityCode("001");
        Health eps = new Health();
        eps.setCodeEPS("EPS001");
        when(municipalityRepository.findById(1L)).thenReturn(Optional.of(municipality));
        when(healthPromotingEntityRepository.findById(1L)).thenReturn(Optional.of(eps));
        when(insertPersonClient.insertPerson(any())).thenReturn("Success");
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("insertPersonToClient", UserMain.class);
        method.setAccessible(true);
        method.invoke(affiliateService, user);
        verify(insertPersonClient).insertPerson(any());
    }


    @Test
    void testInsertRLIndependenteClient_withAllFields() throws Exception {
        UserMain user = new UserMain();
        user.setIdentificationType("CC");
        user.setIdentification("123456789");
        Affiliation affiliation = new Affiliation();
        affiliation.setIdentificationDocumentTypeContractor("NI");
        affiliation.setIdentificationDocumentNumberContractor("900123456");
        affiliation.setCompanyName("Contractor Company");
        affiliation.setOccupation("Engineer");
        affiliation.setCodeMainEconomicActivity("1234");
        affiliation.setDepartment(11L);
        affiliation.setCityMunicipality(1L);
        affiliation.setContractStartDate(LocalDate.now());
        affiliation.setContractQuality("Publico");
        affiliation.setContractType(Constant.CONTRACT_TYPE_ADMINISTRATIVE);
        affiliation.setTransportSupply(true);
        affiliation.setContractDuration("Meses: 12");
        affiliation.setContractEndDate(LocalDate.now().plusMonths(12));
        affiliation.setContractTotalValue(new BigDecimal("50000000"));
        affiliation.setContractMonthlyValue(new BigDecimal("4000000"));
        affiliation.setContractIbcValue(new BigDecimal("3000000"));
        Municipality municipality = new Municipality();
        municipality.setMunicipalityCode("001");
        Occupation occupation = new Occupation();
        occupation.setCodeOccupation("1234");
        when(municipalityRepository.findById(1L)).thenReturn(Optional.of(municipality));
        when(occupationRepository.findByNameOccupation("ENGINEER")).thenReturn(Optional.of(occupation));
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(List.of());
        when(independentContractClient.insert(any())).thenReturn("Success");
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("insertRLIndependenteClient", UserMain.class, Affiliation.class, String.class);
        method.setAccessible(true);
        method.invoke(affiliateService, user, affiliation, Constant.BONDING_TYPE_INDEPENDENT);

        verify(independentContractClient).insert(any());
    }

    @Test
    void testInsertVolunteerToClient_withAllFields() throws Exception {
        Affiliation affiliation = new Affiliation();
        affiliation.setIdentificationDocumentType("CC");
        affiliation.setIdentificationDocumentNumber("123456789");
        affiliation.setFirstName("John");
        affiliation.setSecondName("Carlos");
        affiliation.setSurname("Doe");
        affiliation.setSecondSurname("Smith");
        affiliation.setDateOfBirth(LocalDate.of(1990, 1, 1));
        affiliation.setGender("M");
        affiliation.setEmail("john@example.com");
        affiliation.setDepartment(11L);
        affiliation.setCityMunicipality(1L);
        affiliation.setAddress("Calle 123 #45-67");
        affiliation.setPhone1("3001234567");
        affiliation.setHealthPromotingEntity(1L);
        affiliation.setPensionFundAdministrator(1L);
        affiliation.setContractIbcValue(new BigDecimal("2000000"));
        affiliation.setOccupation("Volunteer");
        affiliation.setIdFamilyMember(1L);
        Municipality municipality = new Municipality();
        municipality.setMunicipalityCode("001");
        Health eps = new Health();
        eps.setCodeEPS("EPS001");
        FamilyMember familyMember = new FamilyMember();
        familyMember.setIdDocumentTypeFamilyMember("CC");
        familyMember.setIdDocumentNumberFamilyMember("987654321");
        familyMember.setFirstNameFamilyMember("Jane");
        familyMember.setSecondNameFamilyMember("Maria");
        familyMember.setSurnameFamilyMember("Doe");
        familyMember.setSecondSurnameFamilyMember("Lopez");
        familyMember.setDepartment(11L);
        familyMember.setCityMunicipality(1L);
        familyMember.setPhone1FamilyMember("3009876543");
        OccupationDecree1563 occupation = new OccupationDecree1563();
        occupation.setCode(5678L);
        when(municipalityRepository.findById(1L)).thenReturn(Optional.of(municipality));
        when(healthPromotingEntityRepository.findById(1L)).thenReturn(Optional.of(eps));
        when(familyMemberRepository.findById(1L)).thenReturn(Optional.of(familyMember));
        when(occupationVolunteerRepository.findByOccupation("VOLUNTEER")).thenReturn(Optional.of(occupation));
        when(insertVolunteerClient.insert(any())).thenReturn("Success");
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("insertVolunteerToClient", Affiliation.class, LocalDate.class);
        method.setAccessible(true);
        method.invoke(affiliateService, affiliation, LocalDate.now());
        verify(insertVolunteerClient).insert(any());
    }

    @Test
    void testInsertWorkCentersMercantile_withMultipleActivities() throws Exception {
        EconomicActivity activity1 = new EconomicActivity();
        activity1.setEconomicActivityCode("1234");
        EconomicActivity activity2 = new EconomicActivity();
        activity2.setEconomicActivityCode("5678");

        AffiliateActivityEconomic primary = new AffiliateActivityEconomic();
        primary.setIsPrimary(true);
        primary.setActivityEconomic(activity1);

        AffiliateActivityEconomic secondary1 = new AffiliateActivityEconomic();
        secondary1.setIsPrimary(false);
        secondary1.setActivityEconomic(activity2);

        AffiliateActivityEconomic secondary2 = new AffiliateActivityEconomic();
        secondary2.setIsPrimary(false);
        secondary2.setActivityEconomic(activity1);

        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setTypeDocumentIdentification("NI");
        mercantile.setNumberIdentification("900123456");
        mercantile.setDecentralizedConsecutive(1L);
        mercantile.setEconomicActivity(List.of(primary, secondary1, secondary2));

        when(insertWorkCenterClient.insertWorkCenter(any())).thenReturn("Success");

        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("insertWorkCentersMercantile", AffiliateMercantile.class);
        method.setAccessible(true);
        method.invoke(affiliateService, mercantile);

        verify(insertWorkCenterClient, times(2)).insertWorkCenter(any());
    }

    @Test
    void testInsertEmployer723_withFullFlow() throws Exception {
        Affiliation affiliation = new Affiliation();
        affiliation.setIdentificationDocumentTypeContractor("CC");
        affiliation.setIdentificationDocumentNumberContractor("123456789");
        affiliation.setCompanyName("Test Company 723");
        affiliation.setIdDepartmentWorkDataCenter(11L);
        affiliation.setIdCityWorkDataCenter(1L);
        affiliation.setAddressWorkDataCenter("Address 723");
        affiliation.setPhone1WorkDataCenter("3001234567");
        affiliation.setEmailContractor("company723@example.com");
        affiliation.setIdentificationDocumentTypeLegalRepresentative("CC");
        affiliation.setIdentificationDocumentNumberContractorLegalRepresentative("987654321");
        affiliation.setFirstNameContractor("John");
        affiliation.setSecondNameContractor("Carlos");
        affiliation.setSurnameContractor("Doe");
        affiliation.setSecondSurnameContractor("Smith");
        affiliation.setContractQuality("Publico");

        Municipality municipality = new Municipality();
        municipality.setMunicipalityCode("001");

        when(consultEmployerClient.consult(any(), any(), anyInt())).thenReturn(reactor.core.publisher.Mono.just(List.of()));
        when(iUserRegisterService.calculateModulo11DV("123456789")).thenReturn(7);
        when(municipalityRepository.findById(1L)).thenReturn(Optional.of(municipality));
        when(insertPersonClient.insertPerson(any())).thenReturn("Success");
        when(insertEmployerClient.insertEmployer(any())).thenReturn("Success");
        when(insertLegalRepresentativeClient.insertLegalRepresentative(any())).thenReturn("Success");

        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("insertEmployer723", Affiliation.class);
        method.setAccessible(true);
        method.invoke(affiliateService, affiliation);

        verify(insertPersonClient).insertPerson(any());
        verify(insertEmployerClient).insertEmployer(any());
        verify(insertLegalRepresentativeClient).insertLegalRepresentative(any());
    }


    @Test
    void testAffiliateBUs_withNullFields() throws Exception {
        AffiliateCompanyResponse response = new AffiliateCompanyResponse();
        response.setIdPersona("123456789");
        response.setTipoDoc("CC");
        response.setIdEmpresa("900123456");
        response.setRazonSocial("Test Company");
        response.setNomVinLaboral("Independiente");
        response.setIdTipoVinculado(1);
        response.setEstadoRl("Activo");
        response.setFechaNacimiento("1990-01-01 00:00:00");
        response.setFechaAfiliacionEfectiva("2024-01-01 00:00:00");
        response.setFechaInicioVinculacion("2024-01-01 00:00:00");
        PersonResponse personResponse = new PersonResponse();
        personResponse.setIdTipoDoc("CC");
        personResponse.setIdPersona("123456789");
        personResponse.setNombre1("John");
        personResponse.setApellido1("Doe");
        personResponse.setFechaNacimiento("1990/01/01 00:00:00");
        personResponse.setSexo("M");
        personResponse.setEmailPersona(null);
        personResponse.setTelefonoPersona("3001234567");
        personResponse.setDireccionPersona("Calle 123");
        ArlInformation arlInformation = new ArlInformation();
        arlInformation.setCode("ARL001");
        when(consultAffiliateCompanyClient.consultAffiliate("CC", "123456789")).thenReturn(reactor.core.publisher.Mono.just(List.of(response)));
        when(affiliateRepository.existsByNitCompanyAndAffiliationType("900123456", "Empleador")).thenReturn(true);
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("SOL_AFI_2025000001450");
        when(clientPerson.consult("CC", "123456789")).thenReturn(reactor.core.publisher.Mono.just(List.of(personResponse)));
        when(iUserPreRegisterRepository.count(any(Specification.class))).thenReturn(0L);
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));

        boolean result = affiliateService.affiliateBUs("CC", "123456789");

        assertThat(result).isTrue();
    }
    @Test
    void testAffiliateBUs_withEndDate() throws Exception {
        AffiliateCompanyResponse response = new AffiliateCompanyResponse();
        response.setIdPersona("123456789");
        response.setTipoDoc("CC");
        response.setIdEmpresa("900123456");
        response.setRazonSocial("Test Company");
        response.setNomVinLaboral("Independiente");
        response.setIdTipoVinculado(1);
        response.setEstadoRl("Activo");
        response.setFechaNacimiento("1990-01-01 00:00:00");
        response.setFechaAfiliacionEfectiva("2024-01-01 00:00:00");
        response.setFechaInicioVinculacion("2024-01-01 00:00:00");
        response.setFechaFinVinculacion("2024-12-31 00:00:00");
        PersonResponse personResponse = new PersonResponse();
        personResponse.setIdTipoDoc("CC");
        personResponse.setIdPersona("123456789");
        personResponse.setNombre1("John");
        personResponse.setApellido1("Doe");
        personResponse.setFechaNacimiento("1990/01/01 00:00:00");
        personResponse.setSexo("M");
        personResponse.setEmailPersona("john@example.com");
        personResponse.setTelefonoPersona("3001234567");
        personResponse.setDireccionPersona("Calle 123");
        ArlInformation arlInformation = new ArlInformation();
        arlInformation.setCode("ARL001");
        when(consultAffiliateCompanyClient.consultAffiliate("CC", "123456789")).thenReturn(reactor.core.publisher.Mono.just(List.of(response)));
        when(affiliateRepository.existsByNitCompanyAndAffiliationType("900123456", "Empleador")).thenReturn(true);
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("SOL_AFI_2025000001450");
        when(clientPerson.consult("CC", "123456789")).thenReturn(reactor.core.publisher.Mono.just(List.of(personResponse)));
        when(iUserPreRegisterRepository.count(any(Specification.class))).thenReturn(0L);
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));

        boolean result = affiliateService.affiliateBUs("CC", "123456789");

        assertThat(result).isTrue();
    }

    @Test
    void testSing_withVolunteerSubtype() {
        String filedNumber = "SOL_AFI_2025000001460";

        Affiliation affiliation = new Affiliation();
        affiliation.setFiledNumber(filedNumber);
        affiliation.setTypeAffiliation(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliation.setStageManagement("PREVIOUS_STAGE");
        affiliation.setIdentificationDocumentType("CC");
        affiliation.setIdentificationDocumentNumber("123456");

        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber(filedNumber);
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER);
        affiliate.setAffiliationCancelled(false);
        affiliate.setStatusDocument(false);
        affiliate.setUserId(100L);
        affiliate.setIdAffiliate(1L);
        affiliate.setCompany("Test Company");
        affiliate.setDocumentType("CC");
        affiliate.setDocumentNumber("123456");

        Policy policy = new Policy();
        policy.setCode("POL123");
        policy.setId(1L);

        UserMain userMain = new UserMain();
        userMain.setIdentificationType("CC");
        userMain.setIdentification("123456");

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(mercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(rolesUserService.findByName(any())).thenReturn(new Role());
        when(policyService.createPolicy(any(), any(), any(), any(), any(), any(), any())).thenReturn(policy);
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(userMain));

        // act
        affiliateService.sing(filedNumber);

        // assert
        verify(policyService, times(1)).createPolicy(any(), any(), any(), any(), any(), any(), any());
        verify(cardAffiliatedService).createCardWithoutOtp(filedNumber);
        verify(rolesUserService).updateRoleUser(any(), any());
        verifyNoMoreInteractions(policyService);
    }


    @Test
    void testGenerateTemporalPass_multipleAttempts() throws Exception {
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("generateTemporalPass");
        method.setAccessible(true);

        for (int i = 0; i < 5; i++) {
            String password = (String) method.invoke(affiliateService);
            assertThat(password).isNotNull();
            assertThat(password.length()).isBetween(8, 12);
        }
    }

    @Test
    void testResponseFoundAffiliate_withExactlyOneMatch() {
        when(affiliateRepository.count(any(Specification.class))).thenReturn(1L);

        Affiliate affiliate = new Affiliate();
        affiliate.setDocumentNumber("123456");
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        Object result = affiliateService.responseFoundAffiliate("CC", "123456");

        assertThat(result).isInstanceOf(Optional.class);
        Optional<Affiliate> optResult = (Optional<Affiliate>) result;
        assertThat(optResult).isPresent();
    }

    @Test
    void testAffiliateBUs_dependentBondingType() throws Exception {
        AffiliateCompanyResponse response = new AffiliateCompanyResponse();
        response.setIdPersona("123456789");
        response.setTipoDoc("CC");
        response.setIdEmpresa("900123456");
        response.setRazonSocial("Test Company");
        response.setNomVinLaboral("Estudiante");
        response.setIdTipoVinculado(34);
        response.setEstadoRl("Activo");
        response.setFechaNacimiento("1990-01-01 00:00:00");
        response.setFechaAfiliacionEfectiva("2024-01-01 00:00:00");
        response.setFechaInicioVinculacion("2024-01-01 00:00:00");
        response.setIdDepartamento(11);
        response.setIdMunicipio(1);
        response.setIdActEconomica(1234L);
        response.setOcupacion("Student");
        response.setIdOcupacion(100);

        Municipality municipality = new Municipality();
        municipality.setIdMunicipality(1L);
        Health eps = new Health();
        eps.setId(1L);
        ArlInformation arl = new ArlInformation();
        arl.setCode("ARL001");

        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setIdAffiliate(200L);
        Policy employerPolicy = new Policy();
        employerPolicy.setIdPolicyType(Constant.ID_EMPLOYER_POLICY);
        employerPolicy.setCode("POL123");

        when(consultAffiliateCompanyClient.consultAffiliate("CC", "123456789")).thenReturn(reactor.core.publisher.Mono.just(List.of(response)));
        when(affiliateRepository.existsByNitCompanyAndAffiliationType("900123456", "Empleador")).thenReturn(true);
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("SOL_AFI_2025000001450");
        when(municipalityRepository.findByIdDepartmentAndMunicipalityCode(11L, "001")).thenReturn(Optional.of(municipality));
        when(healthPromotingEntityRepository.findByCodeEPS(any())).thenReturn(Optional.of(eps));
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arl));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(employerAffiliate));
        when(policyRepository.findByIdAffiliate(200L)).thenReturn(List.of(employerPolicy));

        boolean result = affiliateService.affiliateBUs("CC", "123456789");

        assertThat(result).isTrue();
    }

    @Test
    void testAffiliateBUs_apprenticeBondingType() throws Exception {
        AffiliateCompanyResponse response = new AffiliateCompanyResponse();
        response.setIdPersona("123456789");
        response.setTipoDoc("CC");
        response.setIdEmpresa("900123456");
        response.setRazonSocial("Test Company");
        response.setNomVinLaboral("Aprendiz");
        response.setIdTipoVinculado(35);
        response.setEstadoRl("Activo");
        response.setFechaNacimiento("1990-01-01 00:00:00");
        response.setFechaAfiliacionEfectiva("2024-01-01 00:00:00");
        response.setFechaInicioVinculacion("2024-01-01 00:00:00");
        response.setIdDepartamento(11);
        response.setIdMunicipio(1);
        response.setIdActEconomica(1234L);
        response.setOcupacion("Apprentice");
        response.setIdOcupacion(100);
        Municipality municipality = new Municipality();
        municipality.setIdMunicipality(1L);
        Health eps = new Health();
        eps.setId(1L);
        ArlInformation arl = new ArlInformation();
        arl.setCode("ARL001");
        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setIdAffiliate(200L);
        Policy employerPolicy = new Policy();
        employerPolicy.setIdPolicyType(Constant.ID_EMPLOYER_POLICY);
        employerPolicy.setCode("POL123");
        when(consultAffiliateCompanyClient.consultAffiliate("CC", "123456789")).thenReturn(reactor.core.publisher.Mono.just(List.of(response)));
        when(affiliateRepository.existsByNitCompanyAndAffiliationType("900123456", "Empleador")).thenReturn(true);
        when(filedService.getNextFiledNumberAffiliation()).thenReturn("SOL_AFI_2025000001450");
        when(municipalityRepository.findByIdDepartmentAndMunicipalityCode(11L, "001")).thenReturn(Optional.of(municipality));
        when(healthPromotingEntityRepository.findByCodeEPS(any())).thenReturn(Optional.of(eps));
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arl));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(employerAffiliate));
        when(policyRepository.findByIdAffiliate(200L)).thenReturn(List.of(employerPolicy));
        boolean result = affiliateService.affiliateBUs("CC", "123456789");
        assertThat(result).isTrue();
    }

    @Test
    void testFindOccupationVolunteer_withException() throws Exception {
        when(occupationVolunteerRepository.findByOccupation("INVALID")).thenThrow(new RuntimeException("Database error"));
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("findOccupationVolunteer", String.class);
        method.setAccessible(true);
        int result = (int) method.invoke(affiliateService, "Invalid");
        assertThat(result).isEqualTo(0);
    }

    @Test
    void testInsertWorkCentersDomestic_fullFlow() throws Exception {
        Affiliation affiliation = new Affiliation();
        affiliation.setIdentificationDocumentType("CC");
        affiliation.setIdentificationDocumentNumber("123456789");
        when(insertWorkCenterClient.insertWorkCenter(any())).thenReturn("Success");
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("insertWorkCentersDomestic", Affiliation.class);
        method.setAccessible(true);
        method.invoke(affiliateService, affiliation);
        verify(insertWorkCenterClient, times(3)).insertWorkCenter(any());
    }

    @Test
    void testGetEmployerAffiliationHistory_multipleResults() {
        String nitCompany = "900123456";
        String documentType = "CC";
        String documentNumber = "123456789";
        Affiliate affiliate1 = new Affiliate();
        affiliate1.setIdAffiliate(1L);
        Affiliate affiliate2 = new Affiliate();
        affiliate2.setIdAffiliate(2L);
        List<Affiliate> affiliates = List.of(affiliate1, affiliate2);
        EmployerAffiliationHistoryDTO dto1 = new EmployerAffiliationHistoryDTO();
        EmployerAffiliationHistoryDTO dto2 = new EmployerAffiliationHistoryDTO();
        List<EmployerAffiliationHistoryDTO> expectedDTOs = List.of(dto1, dto2);

        when(affiliateRepository.findByNitCompanyAndDocumentTypeAndDocumentNumberAndAffiliationType(
                nitCompany, documentType, documentNumber, Constant.TYPE_AFFILLATE_EMPLOYER))
                .thenReturn(affiliates);
        when(affiliateMapper.toEmployerAffiliationHistoryDTOList(affiliates)).thenReturn(expectedDTOs);
        List<EmployerAffiliationHistoryDTO> result = affiliateService.getEmployerAffiliationHistory(
                nitCompany, documentType, documentNumber);
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expectedDTOs);
    }

    @Test
    void testGetUserMain_withNullSecondaryFields() throws Exception {
        PersonResponse responsePerson = new PersonResponse();
        responsePerson.setIdTipoDoc("CC");
        responsePerson.setIdPersona("123456");
        responsePerson.setNombre1("Juan");
        responsePerson.setNombre2(null);
        responsePerson.setApellido1("Perez");
        responsePerson.setApellido2(null);
        responsePerson.setFechaNacimiento("1990/01/01 00:00:00");
        responsePerson.setSexo("M");
        responsePerson.setTelefonoPersona("3001234567");
        responsePerson.setDireccionPersona("Calle 1 #2-3");
        responsePerson.setEmailPersona("juan.perez@example.com");
        LocalDate dateBirth = LocalDate.of(1990, 1, 1);
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("getUserMain", PersonResponse.class, LocalDate.class);
        method.setAccessible(true);
        UserMain userMain = (UserMain) method.invoke(null, responsePerson, dateBirth);
        assertThat(userMain.getSecondName()).isNull();
        assertThat(userMain.getSecondSurname()).isNull();
        assertThat(userMain.getFirstName()).isEqualTo("Juan");
    }

    @Test
    void testBuildAffiliateResponse_withNullStatus() throws Exception {
        AffiliateCompanyResponse response = new AffiliateCompanyResponse();
        response.setRazonSocial("Test Company");
        response.setIdEmpresa("123456789");
        response.setIdPersona("987654321");
        response.setTipoDoc("CC");
        response.setNomVinLaboral("Dependiente");
        response.setIdTipoVinculado(3);
        response.setEstadoRl(null);
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("buildAffiliateResponse", AffiliateCompanyResponse.class);
        method.setAccessible(true);
        Affiliate affiliate = (Affiliate) method.invoke(null, response);
        assertThat(affiliate.getAffiliationStatus()).isNull();
        assertThat(affiliate.getAffiliationType()).isEqualTo("Trabajador Dependiente");
    }
    @Test
    void testRegularizationDocuments_withAlternativeUpload() throws IOException {
        String filedNumber = "SOL_AFI_2025000001461";
        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber(filedNumber);
        affiliate.setAffiliationCancelled(false);
        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        affiliate.setIdAffiliate(1L);
        affiliate.setDocumentNumber("123456");
        Affiliation affiliation = new Affiliation();
        affiliation.setFiledNumber(filedNumber);
        affiliation.setIdentificationDocumentNumber("123456");
        affiliation.setTypeAffiliation(Constant.TYPE_AFFILLATE_INDEPENDENT);
        MultipartFile mockFile = mock(MultipartFile.class);
        List<MultipartFile> documents = List.of(mockFile);
        com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO alfrescoResponse = new com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO();
        alfrescoResponse.setIdNewFolder("folder123");
        alfrescoResponse.setDocuments(List.of());
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(properties.getDocumentFolderId()).thenReturn("folderId");
        when(alfrescoService.uploadAffiliationDocuments(any(), any(), any()))
                .thenThrow(new RuntimeException("First attempt failed"))
                .thenReturn(alfrescoResponse);
        com.gal.afiliaciones.infrastructure.dto.affiliate.RegularizationDTO result = affiliateService.regularizationDocuments(filedNumber, documents);
        assertThat(result).isNotNull();
        verify(alfrescoService, times(2)).uploadAffiliationDocuments(any(), any(), any());
    }
    @Test
    void testFindMainEconomicActivity_withEmptyList() throws Exception {
        List<AffiliateActivityEconomic> emptyList = List.of();
        java.lang.reflect.Method method = AffiliateServiceImpl.class.getDeclaredMethod("findMainEconomicActivty", List.class);
        method.setAccessible(true);
        Long result = (Long) method.invoke(affiliateService, emptyList);
        assertThat(result).isNull();
    }


    @Test
    void testAffiliateBUs_dependentWorkerAffiliation_success() throws Exception {
        // Arrange
        String idTipoDoc = "CC";
        String idAfiliado = "123456789";
        String nitEmpresa = "900123456";
        String filedNumber = "SOL_AFI_2025000009999";

        AffiliateCompanyResponse response = new AffiliateCompanyResponse();
        response.setIdPersona(idAfiliado);
        response.setTipoDoc(idTipoDoc);
        response.setIdEmpresa(nitEmpresa);
        response.setRazonSocial("Test Employer Inc.");
        response.setNomVinLaboral("Dependiente");
        response.setIdTipoVinculado(3); // Dependiente
        response.setEstadoRl("Activo");
        response.setFechaNacimiento("1995-03-15 00:00:00");
        response.setFechaAfiliacionEfectiva("2023-01-10 00:00:00");
        response.setFechaInicioVinculacion("2023-01-01 00:00:00");
        response.setIdDepartamento(5);
        response.setIdMunicipio(1);
        response.setEps("EPS001");
        response.setIdActEconomica(9876L);
        response.setOcupacion("Developer");
        response.setIdOcupacion(42);

        when(consultAffiliateCompanyClient.consultAffiliate(idTipoDoc, idAfiliado))
                .thenReturn(reactor.core.publisher.Mono.just(List.of(response)));
        when(affiliateRepository.existsByNitCompanyAndAffiliationType(nitEmpresa, "Empleador")).thenReturn(true);
        when(filedService.getNextFiledNumberAffiliation()).thenReturn(filedNumber);

        Municipality mockMunicipality = new Municipality();
        mockMunicipality.setIdMunicipality(1L);
        when(municipalityRepository.findByIdDepartmentAndMunicipalityCode(5L, "001"))
                .thenReturn(Optional.of(mockMunicipality));

        Health mockHealth = new Health();
        mockHealth.setId(1L);
        mockHealth.setCodeEPS("EPS001");
        when(healthPromotingEntityRepository.findByCodeEPS("EPS001")).thenReturn(Optional.of(mockHealth));

        Occupation mockOccupation = new Occupation();
        mockOccupation.setIdOccupation(42L);
        mockOccupation.setNameOccupation("Developer");


        ArlInformation mockArl = new ArlInformation();
        mockArl.setCode("ARL_CODE");
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(mockArl));

        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setIdAffiliate(10L);
        employerAffiliate.setNitCompany(nitEmpresa);
        employerAffiliate.setAffiliationType("Empleador");

        when(affiliateRepository.save(any(Affiliate.class))).thenAnswer(invocation -> {
            Affiliate affiliate = invocation.getArgument(0);
            if (affiliate.getIdAffiliate() == null) {
                affiliate.setIdAffiliate(999L); // Asignar ID al afiliado guardado
            }
            return affiliate;
        });

        // Mockear findOne para retornar el empleador primero
        when(affiliateRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(employerAffiliate));

        Policy employerPolicy = new Policy();
        employerPolicy.setCode("EMP_POLICY_123");
        employerPolicy.setIdPolicyType(Constant.ID_EMPLOYER_POLICY);
        when(policyRepository.findByIdAffiliate(10L)).thenReturn(List.of(employerPolicy));



        // Act
        boolean result = affiliateService.affiliateBUs(idTipoDoc, idAfiliado);

        // Assert
        assertThat(result).isTrue();

        ArgumentCaptor<AffiliationDependent> dependentCaptor = ArgumentCaptor.forClass(AffiliationDependent.class);
        verify(affiliationDependentRepository).save(dependentCaptor.capture());
        AffiliationDependent savedDependent = dependentCaptor.getValue();
        assertThat(savedDependent.getFiledNumber()).isEqualTo(filedNumber);
        assertThat(savedDependent.getIdentificationDocumentNumber()).isEqualTo(idAfiliado);
        assertThat(savedDependent.getIdBondingType()).isEqualTo(1L);

        ArgumentCaptor<Affiliate> affiliateCaptor = ArgumentCaptor.forClass(Affiliate.class);
        verify(affiliateRepository).save(affiliateCaptor.capture());
        Affiliate savedAffiliate = affiliateCaptor.getValue();
        assertThat(savedAffiliate.getFiledNumber()).isEqualTo(filedNumber);
        assertThat(savedAffiliate.getAffiliationType()).isEqualTo("Trabajador Dependiente");
        assertThat(savedAffiliate.getAffiliationSubType()).isEqualTo(Constant.BONDING_TYPE_DEPENDENT);
        assertThat(savedAffiliate.getIdAffiliate()).isEqualTo(999L); // Verificar que tiene ID

        verify(policyService).createPolicyDependent(eq(idTipoDoc), eq(idAfiliado), any(LocalDate.class), eq(999L), eq("EMP_POLICY_123"), eq("Test Employer Inc."));
        verify(cardAffiliatedService).createCardDependent(any(Affiliate.class), any(), isNull(), any(), isNull());
    }

    @Test
    void testAffiliateBUs_handlesMissingEpsAndMunicipalityGracefully() throws Exception {
        // Arrange
        String idTipoDoc = "CC";
        String idAfiliado = "123456789";
        String nitEmpresa = "900123456";
        String filedNumber = "SOL_AFI_2025000009998";

        AffiliateCompanyResponse response = new AffiliateCompanyResponse();
        response.setIdPersona(idAfiliado);
        response.setTipoDoc(idTipoDoc);
        response.setIdEmpresa(nitEmpresa);
        response.setRazonSocial("Test Employer Inc.");
        response.setNomVinLaboral("Dependiente");
        response.setIdTipoVinculado(3);
        response.setEstadoRl("Activo");
        response.setFechaNacimiento("1995-03-15 00:00:00");
        response.setFechaAfiliacionEfectiva("2023-01-10 00:00:00");
        response.setFechaInicioVinculacion("2023-01-01 00:00:00");
        response.setIdDepartamento(5);
        response.setIdMunicipio(1);
        response.setEps("EPS001");
        response.setIdOcupacion(100);
        response.setSalario(3000000.0);

        when(consultAffiliateCompanyClient.consultAffiliate(idTipoDoc, idAfiliado))
                .thenReturn(reactor.core.publisher.Mono.just(List.of(response)));
        when(affiliateRepository.existsByNitCompanyAndAffiliationType(nitEmpresa, "Empleador")).thenReturn(true);
        when(filedService.getNextFiledNumberAffiliation()).thenReturn(filedNumber);

        Municipality mockMunicipality = new Municipality();
        mockMunicipality.setIdMunicipality(1L);
        mockMunicipality.setMunicipalityName("Bogotá");
        mockMunicipality.setMunicipalityCode("1");
        when(municipalityRepository.findByIdDepartmentAndMunicipalityCode(any(), any()))
                .thenReturn(Optional.of(mockMunicipality));

        Health mockEps = new Health();
        mockEps.setId(1L);
        mockEps.setCodeEPS("EPS001");
        mockEps.setNameEPS("Test EPS");
        when(healthPromotingEntityRepository.findByCodeEPS("EPS001"))
                .thenReturn(Optional.of(mockEps));

        Occupation mockOccupation = new Occupation();
        mockOccupation.setIdOccupation(100L);
        mockOccupation.setNameOccupation("Empleado");

        Affiliate mockAffiliate = new Affiliate();
        mockAffiliate.setIdAffiliate(999L);
        mockAffiliate.setDocumentNumber(idAfiliado);
        mockAffiliate.setFiledNumber(filedNumber);
        mockAffiliate.setAffiliationType("Dependiente");
        mockAffiliate.setNitCompany(nitEmpresa);

        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(mockAffiliate);
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mockAffiliate));


        ArlInformation mockArl = new ArlInformation();
        mockArl.setCode("ARL_CODE");
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(mockArl));

        Policy mockPolicy = new Policy();
        mockPolicy.setId(1L);
        mockPolicy.setIdAffiliate(999L);
        mockPolicy.setNumPolicyClient(2L);
        mockPolicy.setStatus("ACTIVE");
        mockPolicy.setIssueDate(LocalDate.now());

        // Mockear que existe una póliza activa

        when(policyRepository.findByIdAffiliate(999L)).thenReturn(List.of(mockPolicy));


        // Act
        boolean result = affiliateService.affiliateBUs(idTipoDoc, idAfiliado);

        // Assert
        assertThat(result).isTrue();

        ArgumentCaptor<AffiliationDependent> dependentCaptor = ArgumentCaptor.forClass(AffiliationDependent.class);
        verify(affiliationDependentRepository).save(dependentCaptor.capture());
        AffiliationDependent savedDependent = dependentCaptor.getValue();
        assertThat(savedDependent.getIdCity()).isEqualTo(1L);
        assertThat(savedDependent.getHealthPromotingEntity()).isEqualTo(1L);

        verify(affiliateRepository, atLeastOnce()).save(any(Affiliate.class));
    }

    @Test
    void testAffiliateBUs_newUserCreationFlow() throws Exception {
        // Arrange
        String idTipoDoc = "CC";
        String idAfiliado = "111222333";
        String nitEmpresa = "900123456";
        String filedNumber = "SOL_AFI_2025000009997";

        AffiliateCompanyResponse affiliateResponse = new AffiliateCompanyResponse();
        affiliateResponse.setIdPersona(idAfiliado);
        affiliateResponse.setTipoDoc(idTipoDoc);
        affiliateResponse.setIdEmpresa(nitEmpresa);
        affiliateResponse.setRazonSocial("Test Employer Inc.");
        affiliateResponse.setNomVinLaboral("Independiente");
        affiliateResponse.setIdTipoVinculado(1);
        affiliateResponse.setEstadoRl("Activo");
        affiliateResponse.setFechaNacimiento("1990-01-01 00:00:00");
        affiliateResponse.setFechaAfiliacionEfectiva("2023-01-10 00:00:00");
        affiliateResponse.setFechaInicioVinculacion("2023-01-01 00:00:00");

        affiliateResponse.setIdDepartamento(5);
        affiliateResponse.setIdMunicipio(1);
        affiliateResponse.setEps("EPS001");
        affiliateResponse.setIdOcupacion(100);
        affiliateResponse.setSalario(3000000.0);

        PersonResponse personResponse = new PersonResponse();
        personResponse.setIdTipoDoc(idTipoDoc);
        personResponse.setIdPersona(idAfiliado);
        personResponse.setNombre1("Nuevo");
        personResponse.setApellido1("Usuario");
        personResponse.setFechaNacimiento("1990/01/01 00:00:00");
        personResponse.setSexo("F");
        personResponse.setEmailPersona("nuevo.usuario@example.com");
        personResponse.setTelefonoPersona("3001234567");

        personResponse.setDireccionPersona("Calle 123 # 45-67");
        personResponse.setIdDepartamento(1);
        personResponse.setIdMunicipio(1);

        when(consultAffiliateCompanyClient.consultAffiliate(idTipoDoc, idAfiliado))
                .thenReturn(reactor.core.publisher.Mono.just(List.of(affiliateResponse)));
        when(affiliateRepository.existsByNitCompanyAndAffiliationType(nitEmpresa, "Empleador")).thenReturn(true);
        when(filedService.getNextFiledNumberAffiliation()).thenReturn(filedNumber);
        when(clientPerson.consult(idTipoDoc, idAfiliado)).thenReturn(reactor.core.publisher.Mono.just(List.of(personResponse)));

        // Simulate user does not exist
        when(iUserPreRegisterRepository.count(any(Specification.class))).thenReturn(0L);

        UserMain createdUser = new UserMain();
        createdUser.setId(500L);
        createdUser.setFirstName("Nuevo");
        createdUser.setSurname("Usuario");
        createdUser.setEmail("nuevo.usuario@example.com");

        // Simulate user is found after creation
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(createdUser));

        ArlInformation mockArl = new ArlInformation();
        mockArl.setCode("ARL_CODE");
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(mockArl));

        Health mockEps = new Health();
        mockEps.setId(1L);
        mockEps.setCodeEPS("EPS001");
        when(healthPromotingEntityRepository.findByCodeEPS(any())).thenReturn(Optional.of(mockEps));

        Municipality mockMunicipality = new Municipality();
        mockMunicipality.setIdMunicipality(1L);
        mockMunicipality.setMunicipalityName("Bogotá");
        when(municipalityRepository.findByIdDepartmentAndMunicipalityCode(any(), any()))
                .thenReturn(Optional.of(mockMunicipality));

        Occupation mockOccupation = new Occupation();
        mockOccupation.setIdOccupation(100L);


        Affiliate mockAffiliate = new Affiliate();
        mockAffiliate.setIdAffiliate(999L);
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(mockAffiliate);


        Policy mockPolicy = new Policy();
        mockPolicy.setId(1L);


        when(policyService.createPolicy(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(mockPolicy);

        // Act
        boolean result = affiliateService.affiliateBUs(idTipoDoc, idAfiliado);

        // Assert
        assertThat(result).isTrue();

        // Verify that the user pre-registration service was called
        verify(iUserRegisterService).userPreRegister(any(UserPreRegisterDto.class));

        // Verify that roles were assigned to the newly created user
        verify(webClient).assignRolesToUser(eq(500L), anyList());

        // Verify affiliation details are saved
        verify(affiliationDetailRepository).save(any(Affiliation.class));
        verify(affiliateRepository).save(any(Affiliate.class));
    }

    @Test
    void testSing_independentAffiliation_success() {
        // Arrange
        String filedNumber = "SOL_AFI_2025000008888";
        Affiliation affiliation = new Affiliation();
        affiliation.setFiledNumber(filedNumber);
        affiliation.setTypeAffiliation(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliation.setStageManagement("DOCUMENTAL_REVIEW");
        affiliation.setIdentificationDocumentType("CC");
        affiliation.setIdentificationDocumentNumber("11223344");

        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber(filedNumber);
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setAffiliationSubType(Constant.BONDING_TYPE_INDEPENDENT);
        affiliate.setAffiliationCancelled(false);
        affiliate.setStatusDocument(false);
        affiliate.setUserId(99L);
        affiliate.setIdAffiliate(88L);
        affiliate.setCompany("Independent Worker Co.");

        UserMain userMain = new UserMain();
        userMain.setIdentificationType("CC");
        userMain.setIdentification("11223344");

        // SOLUCIÓN: Crear y mockear el timer
        AffiliationCancellationTimer timer = new AffiliationCancellationTimer();
        timer.setNumberDocument(filedNumber);

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(mercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(rolesUserService.findByName(Constant.BONDING_TYPE_INDEPENDENT)).thenReturn(new Role());
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(userMain));
        when(policyService.createPolicy(any(), any(), any(), isNull(), anyLong(), anyLong(), anyString())).thenReturn(new Policy());

        // SOLUCIÓN: Mockear findAll para que retorne el timer
        when(timerRepository.findAll(any(Specification.class))).thenReturn(List.of(timer));

        // Act
        affiliateService.sing(filedNumber);

        // Assert
        verify(repositoryAffiliation).save(affiliation);
        assertThat(affiliation.getStageManagement()).isEqualTo(Constant.ACCEPT_AFFILIATION);

        verify(affiliateRepository).save(affiliate);
        assertThat(affiliate.getAffiliationStatus()).isEqualTo(Constant.AFFILIATION_STATUS_ACTIVE);
        assertThat(affiliate.getCoverageStartDate()).isEqualTo(LocalDate.now().plusDays(1));

        verify(rolesUserService).updateRoleUser(99L, null);
        verify(cardAffiliatedService).createCardWithoutOtp(filedNumber);
        verify(policyService).createPolicy(eq("CC"), eq("11223344"), any(LocalDate.class), isNull(), eq(88L), eq(0L), eq("Independent Worker Co."));

        // Verificar tanto findAll como delete
        verify(timerRepository).findAll(any(Specification.class));
        verify(timerRepository).delete(any(AffiliationCancellationTimer.class));

        verify(generalNoveltyServiceImpl).saveGeneralNovelty(any());
        verify(sendEmails).welcome(any(), anyLong(), anyString(), anyString());
    }

    @Test
    void testSing_mercantileAffiliation_success() {
        // Arrange
        String filedNumber = "SOL_AFI_2025000007777";

        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setFiledNumber(filedNumber);
        mercantile.setStageManagement("DOCUMENTAL_REVIEW");
        mercantile.setAffiliationCancelled(false);
        mercantile.setStatusDocument(false);
        mercantile.setIdUserPreRegister(101L);
        mercantile.setTypeDocumentIdentification("NI");
        mercantile.setNumberIdentification("900800700");
        mercantile.setDecentralizedConsecutive(0L);
        mercantile.setBusinessName("Mercantile Corp");

        mercantile.setEconomicActivity(new ArrayList<>());

        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber(filedNumber);
        affiliate.setIdAffiliate(77L);
        affiliate.setCompany("Mercantile Corp");

        UserMain userMain = new UserMain();
        userMain.setFirstName("Legal");
        userMain.setSurname("Rep");

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(mercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mercantile));
        when(iUserPreRegisterRepository.findById(101L)).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(rolesUserService.findByName(Constant.NAME_CONTRIBUTOR_TYPE_EMPLOYER)).thenReturn(new Role());
        when(policyService.createPolicy(any(), any(), any(), any(), any(), any(), any())).thenReturn(new Policy());

        // Act
        affiliateService.sing(filedNumber);

        // Assert
        verify(mercantileRepository).save(mercantile);
        assertThat(mercantile.getStageManagement()).isEqualTo(Constant.ACCEPT_AFFILIATION);
        assertThat(mercantile.getAffiliationStatus()).isEqualTo(Constant.AFFILIATION_STATUS_ACTIVE);

        verify(affiliateRepository).save(affiliate);
        assertThat(affiliate.getAffiliationStatus()).isEqualTo(Constant.AFFILIATION_STATUS_ACTIVE);
        assertThat(affiliate.getCoverageStartDate()).isEqualTo(LocalDate.now().plusDays(1));

        verify(rolesUserService).updateRoleUser(101L, null);
        verify(policyService, times(2)).createPolicy(eq("NI"), eq("900800700"), any(LocalDate.class), any(), eq(77L), eq(0L), eq("Mercantile Corp"));
        verify(sendEmails).welcomeMercantile(any());
    }

    @Test
    void testRegularizationDocuments_success() throws IOException {
        // Arrange
        String filedNumber = "SOL_AFI_2025000006666";
        List<MultipartFile> documents = List.of(mock(MultipartFile.class));

        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber(filedNumber);
        affiliate.setAffiliationCancelled(false);
        affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
        affiliate.setIdAffiliate(66L);
        affiliate.setDocumentNumber("666666");

        Affiliation affiliation = new Affiliation();
        affiliation.setFiledNumber(filedNumber);
        affiliation.setIdentificationDocumentNumber("666666");

        DataDocumentAffiliate existingDoc1 = new DataDocumentAffiliate();
        existingDoc1.setId(1L);
        existingDoc1.setIdAffiliate(66L);
        existingDoc1.setName("old_document_1.pdf");

        DataDocumentAffiliate existingDoc2 = new DataDocumentAffiliate();
        existingDoc2.setId(2L);
        existingDoc2.setIdAffiliate(66L);
        existingDoc2.setName("old_document_2.pdf");

        AffiliationCancellationTimer timer = new AffiliationCancellationTimer();
        timer.setNumberDocument(filedNumber);

        com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO alfrescoResponse = new com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO();
        alfrescoResponse.setIdNewFolder("newFolderId");
        alfrescoResponse.setDocuments(List.of(new ReplacedDocumentDTO("docId", "docName.pdf")));

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(properties.getAffiliationProvisionServicesFolderId()).thenReturn("provisionServicesFolderId");
        when(alfrescoService.uploadAffiliationDocuments(anyString(), anyString(), anyList())).thenReturn(alfrescoResponse);
        when(documentNameStandardizationService.getName(anyString(), anyString(), anyString())).thenReturn("standardized_docName.pdf");
        when(timerRepository.findAll(any(Specification.class))).thenReturn(List.of(timer));

        when(dataDocumentRepository.findByIdAffiliate(66L)).thenReturn(List.of(existingDoc1, existingDoc2));

        // Act
        com.gal.afiliaciones.infrastructure.dto.affiliate.RegularizationDTO result = affiliateService.regularizationDocuments(filedNumber, documents);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIdAffiliation()).isEqualTo(affiliation.getId());
        assertThat(result.getIdFolderAlfresco()).isEqualTo("newFolderId");

        verify(repositoryAffiliation).save(affiliation);
        assertThat(affiliation.getIdFolderAlfresco()).isEqualTo("newFolderId");
        assertThat(affiliation.getStageManagement()).isEqualTo(Constant.STAGE_MANAGEMENT_DOCUMENTAL_REVIEW);
        assertThat(affiliation.getDateRegularization()).isNotNull();

        verify(affiliateRepository).save(affiliate);
        assertThat(affiliate.getStatusDocument()).isFalse();

        verify(timerRepository).findAll(any(Specification.class));
        verify(timerRepository).delete(any(AffiliationCancellationTimer.class));

        verify(dataDocumentRepository).findByIdAffiliate(66L);
        verify(dataDocumentRepository, times(2)).delete(any(DataDocumentAffiliate.class)); // Se eliminan 2 documentos
        verify(dataDocumentRepository).save(any(DataDocumentAffiliate.class));
    }
}