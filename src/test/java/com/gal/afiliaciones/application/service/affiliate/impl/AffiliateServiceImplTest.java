package com.gal.afiliaciones.application.service.affiliate.impl;

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
import com.gal.afiliaciones.config.ex.NotFoundException;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.affiliation.ResponseMessageAffiliation;
import com.gal.afiliaciones.config.ex.affiliation.WSConsultIndependentWorkerFound;
import com.gal.afiliaciones.config.ex.alfresco.ErrorFindDocumentsAlfresco;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.Policy;
import com.gal.afiliaciones.domain.model.Role;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.EmployerSize;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.client.generic.affiliatecompany.AffiliateCompanyResponse;
import com.gal.afiliaciones.infrastructure.client.generic.affiliatecompany.ConsultAffiliateCompanyClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.ConsultEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.InsertEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.independentrelationship.IndependentContractRelationshipClient;
import com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative.InsertLegalRepresentativeClient;
import com.gal.afiliaciones.infrastructure.client.generic.person.InsertPersonClient;
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
import com.gal.afiliaciones.infrastructure.dto.affiliate.DataStatusAffiliationDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.RegularizationDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.UserAffiliateDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent.ResponseConsultWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.alfresco.ResponseUploadOrReplaceFilesDTO;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.SaveGeneralNoveltyRequest;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.service.RegistraduriaUnifiedService;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.utils.KeyCloakProvider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

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
    private ConsultAffiliateCompanyClient consult;
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
    private com.gal.afiliaciones.config.mapper.AffiliateMapper affiliateMapper;
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

    @Spy
    @InjectMocks
    private AffiliateServiceImpl affiliateService;

    private Affiliate affiliate;
    private Affiliation affiliation;
    private static final String DOCUMENT_TYPE = "CC";
    private static final String DOCUMENT_NUMBER = "123456789";
    private static final String FILED_NUMBER = "2023000001";

    @BeforeEach
    void setUp() {
        affiliate = new Affiliate();
        affiliate.setIdAffiliate(1L);
        affiliate.setDocumentType(DOCUMENT_TYPE);
        affiliate.setDocumentNumber(DOCUMENT_NUMBER);
        affiliate.setFiledNumber(FILED_NUMBER);
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES);
        affiliate.setAffiliationCancelled(false);
        affiliate.setStatusDocument(false);
        affiliate.setUserId(1L);

        affiliation = new Affiliation();
        affiliation.setId(1L);
        affiliation.setFiledNumber(FILED_NUMBER);
        affiliation.setStageManagement("Some Stage");
        affiliation.setTypeAffiliation(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliation.setIdentificationDocumentType(DOCUMENT_TYPE);
        affiliation.setIdentificationDocumentNumber(DOCUMENT_NUMBER);
    }

    @Test
    void findAffiliationsByTypeAndNumber_IndependentAffiliate_ShouldReturnDTO() {
        // Arrange
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        List<Affiliate> affiliateList = Collections.singletonList(affiliate);
        Affiliation affiliation = new Affiliation();
        affiliation.setContractEndDate(LocalDate.now());

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER)).thenReturn(affiliateList);
        when(repositoryAffiliation.findByFiledNumber(affiliate.getFiledNumber())).thenReturn(Optional.of(affiliation));

        // Act
        List<UserAffiliateDTO> result = affiliateService.findAffiliationsByTypeAndNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(affiliation.getContractEndDate().toString(), result.get(0).getRetirementDate());
    }

    @Test
    void findAffiliationsByTypeAndNumber_DependentAffiliate_ShouldReturnDTO() {
        // Arrange
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        List<Affiliate> affiliateList = Collections.singletonList(affiliate);
        AffiliationDependent affiliationDependent = new AffiliationDependent();
        affiliationDependent.setEndDate(LocalDate.now());

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER)).thenReturn(affiliateList);
        when(dependentRepository.findByFiledNumber(affiliate.getFiledNumber())).thenReturn(Optional.of(affiliationDependent));

        // Act
        List<UserAffiliateDTO> result = affiliateService.findAffiliationsByTypeAndNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(affiliationDependent.getEndDate().toString(), result.get(0).getRetirementDate());
    }

    @Test
    void findAffiliationsByTypeAndNumber_OtherAffiliationType_ShouldReturnNoRetirementDate() {
        // Arrange
        affiliate.setAffiliationType("OTHER");
        List<Affiliate> affiliateList = Collections.singletonList(affiliate);

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER)).thenReturn(affiliateList);

        // Act
        List<UserAffiliateDTO> result = affiliateService.findAffiliationsByTypeAndNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("No registra", result.get(0).getRetirementDate());
    }

    @Test
    void findAffiliationsByTypeAndNumber_NullFiledNumber_ShouldBeIgnored() {
        // Arrange
        affiliate.setFiledNumber(null);
        List<Affiliate> affiliateList = Collections.singletonList(affiliate);

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER)).thenReturn(affiliateList);

        // Act
        List<UserAffiliateDTO> result = affiliateService.findAffiliationsByTypeAndNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findAffiliationsByTypeAndNumber_NoAffiliatesFound_ShouldReturnEmptyList() {
        // Arrange
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER)).thenReturn(Collections.emptyList());

        // Act
        List<UserAffiliateDTO> result = affiliateService.findAffiliationsByTypeAndNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findAffiliationsByTypeAndNumber_IndependentAffiliationNotFound_ShouldThrowException() {
        // Arrange
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        List<Affiliate> affiliateList = Collections.singletonList(affiliate);

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER)).thenReturn(affiliateList);
        when(repositoryAffiliation.findByFiledNumber(affiliate.getFiledNumber())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AffiliationNotFoundError.class, () -> {
            affiliateService.findAffiliationsByTypeAndNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER);
        });
    }

    @Test
    void findAffiliationsByTypeAndNumber_DependentAffiliationNotFound_ShouldThrowException() {
        // Arrange
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        List<Affiliate> affiliateList = Collections.singletonList(affiliate);

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER)).thenReturn(affiliateList);
        when(dependentRepository.findByFiledNumber(affiliate.getFiledNumber())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AffiliationNotFoundError.class, () -> {
            affiliateService.findAffiliationsByTypeAndNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER);
        });
    }

    @Test
    void createAffiliate_ShouldReturnSavedAffiliate() {
        // Arrange
        when(affiliateRepository.save(any(Affiliate.class))).thenReturn(affiliate);

        // Act
        Affiliate result = affiliateService.createAffiliate(affiliate);

        // Assert
        assertNotNull(result);
        assertEquals(affiliate, result);
    }

    @Test
    void findAll_ShouldReturnListOfAffiliates() {
        // Arrange
        List<Affiliate> affiliateList = Collections.singletonList(affiliate);
        when(affiliateRepository.findAll()).thenReturn(affiliateList);

        // Act
        List<Affiliate> result = affiliateService.findAll();

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getDataStatusAffiliations_WithIndependentAffiliate_ShouldReturnData() {
        // Arrange
        List<Affiliate> affiliateList = Collections.singletonList(affiliate);
        affiliation.setStageManagement("Some Stage");
        doReturn(affiliateList).when(affiliateService).findAffiliatesByTypeAndNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER);
        when(repositoryAffiliation.findByFiledNumber(affiliate.getFiledNumber())).thenReturn(Optional.of(affiliation));

        // Act
        List<DataStatusAffiliationDTO> result = affiliateService.getDataStatusAffiliations(DOCUMENT_NUMBER, DOCUMENT_TYPE);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Some Stage", result.get(0).getStageManagement());
    }

    @Test
    void getDataStatusAffiliations_WithMercantileAffiliate_ShouldReturnData() {
        // Arrange
        List<Affiliate> affiliateList = Collections.singletonList(affiliate);
        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setStageManagement("Mercantile Stage");
        mercantile.setFiledNumber(affiliate.getFiledNumber());
        doReturn(affiliateList).when(affiliateService).findAffiliatesByTypeAndNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER);
        when(repositoryAffiliation.findByFiledNumber(affiliate.getFiledNumber())).thenReturn(Optional.empty());
        when(mercantileRepository.findByFiledNumber(affiliate.getFiledNumber())).thenReturn(Optional.of(mercantile));

        // Act
        List<DataStatusAffiliationDTO> result = affiliateService.getDataStatusAffiliations(DOCUMENT_NUMBER, DOCUMENT_TYPE);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Mercantile Stage", result.get(0).getStageManagement());
    }

    @Test
    void getDataStatusAffiliations_NoAffiliatesFound_ShouldReturnEmptyList() {
        // Arrange
        doReturn(Collections.emptyList()).when(affiliateService).findAffiliatesByTypeAndNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER);

        // Act
        List<DataStatusAffiliationDTO> result = affiliateService.getDataStatusAffiliations(DOCUMENT_NUMBER, DOCUMENT_TYPE);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getDataStatusAffiliations_AffiliateNotFoundException_ShouldReturnEmptyList() {
        // Arrange
        doThrow(new AffiliateNotFoundException("Not Found")).when(affiliateService).findAffiliatesByTypeAndNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER);

        // Act
        List<DataStatusAffiliationDTO> result = affiliateService.getDataStatusAffiliations(DOCUMENT_NUMBER, DOCUMENT_TYPE);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getDataStatusAffiliations_NullFiledNumber_ShouldBeSkipped() {
        // Arrange
        affiliate.setFiledNumber(null);
        List<Affiliate> affiliateList = Collections.singletonList(affiliate);
        doReturn(affiliateList).when(affiliateService).findAffiliatesByTypeAndNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER);

        // Act
        List<DataStatusAffiliationDTO> result = affiliateService.getDataStatusAffiliations(DOCUMENT_NUMBER, DOCUMENT_TYPE);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void sing_AffiliationNotFound_ShouldThrowException() {
        // Arrange
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(mercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AffiliateNotFoundException.class, () -> affiliateService.sing(FILED_NUMBER));
    }

    @Test
    void sing_IndependentAffiliation_Success() {
        // Arrange
        Role role = new Role();
        role.setId(1L);
        Policy policy = new Policy();
        policy.setCode("POLICY123");
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(rolesUserService.findByName(anyString())).thenReturn(role);
        when(policyService.createPolicy(any(), any(), any(), any(), any(), any(), any())).thenReturn(policy);
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(new UserMain()));


        // Act
        affiliateService.sing(FILED_NUMBER);

        // Assert
        verify(repositoryAffiliation).save(affiliation);
        verify(affiliateRepository).save(affiliate);
        verify(rolesUserService).updateRoleUser(anyLong(), anyLong());
        verify(policyService).createPolicy(any(), any(), any(), any(), any(), any(), any());
        verify(timerRepository, atLeastOnce()).delete(any(com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.AffiliationCancellationTimer.class));
    }

    @Test
    void sing_DomesticAffiliation_Success() {
        // Arrange
        Role role = new Role();
        role.setId(1L);
        Policy policy = new Policy();
        policy.setCode("POLICY123");
        affiliation.setTypeAffiliation(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);

        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(rolesUserService.findByName(anyString())).thenReturn(role);
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(new UserMain()));

        // Act
        affiliateService.sing(FILED_NUMBER);

        // Assert
        verify(repositoryAffiliation).save(affiliation);
        verify(affiliateRepository).save(affiliate);
        verify(rolesUserService).updateRoleUser(anyLong(), anyLong());
        verify(policyService).createPolicy(any(), any(), any(), any(), any(), any(), any());
        verify(timerRepository, atLeastOnce()).delete(any(com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.AffiliationCancellationTimer.class));
        verify(generalNoveltyServiceImpl).saveGeneralNovelty(any(SaveGeneralNoveltyRequest.class));
        verify(sendEmails).welcome(any(), any(), any(), any());
        verify(generalNoveltyServiceImpl).saveGeneralNovelty(any(SaveGeneralNoveltyRequest.class));
        verify(sendEmails).welcome(any(), any(), any(), any());
        verify(cardAffiliatedService).createCardWithoutOtp(FILED_NUMBER);
    }

    @Test
    void sing_AffiliationCancelled_ShouldThrowError() {
        // Arrange
        affiliate.setAffiliationCancelled(true);
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        // Act & Assert
        assertThrows(AffiliationError.class, () -> affiliateService.sing(FILED_NUMBER));
    }

    @Test
    void sing_AffiliationInRegularization_ShouldThrowError() {
        // Arrange
        affiliation.setStageManagement(Constant.REGULARIZATION);
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        // Act & Assert
        assertThrows(AffiliationError.class, () -> affiliateService.sing(FILED_NUMBER));
    }

    @Test
    void regularizationDocuments_Success() throws IOException {
        // Arrange
        List<MultipartFile> documents = new ArrayList<>();
        ResponseUploadOrReplaceFilesDTO responseAlfresco = new ResponseUploadOrReplaceFilesDTO();
        responseAlfresco.setIdNewFolder("newFolderId");
        responseAlfresco.setDocuments(new ArrayList<>());

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(properties.getAffiliationProvisionServicesFolderId()).thenReturn("folderId");
        when(alfrescoService.uploadAffiliationDocuments(anyString(), anyString(), anyList())).thenReturn(responseAlfresco);

        // Act
        RegularizationDTO result = affiliateService.regularizationDocuments(FILED_NUMBER, documents);

        // Assert
        assertNotNull(result);
        assertEquals("newFolderId", result.getIdFolderAlfresco());
        verify(repositoryAffiliation).save(affiliation);
        verify(affiliateRepository).save(affiliate);
        verify(timerRepository, atLeastOnce()).delete(any(com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.AffiliationCancellationTimer.class));
        verify(alfrescoService).uploadAffiliationDocuments("folderId", FILED_NUMBER, documents);
    }

    @Test
    void regularizationDocuments_AffiliationCancelled_ShouldThrowError() {
        // Arrange
        affiliate.setAffiliationCancelled(true);
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        // Act & Assert
        assertThrows(AffiliationError.class, () -> affiliateService.regularizationDocuments(FILED_NUMBER, new ArrayList<>()));
    }

    @Test
    void regularizationDocuments_AffiliationNotFound_ShouldThrowError() {
        // Arrange
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AffiliationError.class, () -> affiliateService.regularizationDocuments(FILED_NUMBER, new ArrayList<>()));
    }

    @Test
    void regularizationDocuments_AlfrescoFolderNotFound_ShouldThrowError() {
        // Arrange
        affiliate.setAffiliationSubType("UNKNOWN_TYPE");
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));

        // Act & Assert
        assertThrows(ErrorFindDocumentsAlfresco.class, () -> affiliateService.regularizationDocuments(FILED_NUMBER, new ArrayList<>()));
    }

    @Test
    void regularizationDocuments_IOException_ShouldThrowError() throws IOException {
        // Arrange
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));
        when(properties.getAffiliationProvisionServicesFolderId()).thenReturn("folderId");
        when(alfrescoService.uploadAffiliationDocuments(anyString(), anyString(), anyList())).thenThrow(new IOException());

        // Act & Assert
        assertThrows(ErrorFindDocumentsAlfresco.class, () -> affiliateService.regularizationDocuments(FILED_NUMBER, new ArrayList<>()));
    }

    @Test
    void responseFoundAffiliate_AffiliateExists_ShouldReturnAffiliate() {
        // Arrange
        when(affiliateRepository.count(any(Specification.class))).thenReturn(1L);
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        // Act
        Object result = affiliateService.responseFoundAffiliate(DOCUMENT_TYPE, DOCUMENT_NUMBER);

        // Assert
        assertNotNull(result);
        assertEquals(affiliate, result);
    }

    @Test
    void responseFoundAffiliate_WorkerInAnotherARL_ShouldThrowException() {
        // Arrange
        ResponseConsultWorkerDTO response = new ResponseConsultWorkerDTO();
        response.setCausal(1L);
        when(affiliateRepository.count(any(Specification.class))).thenReturn(0L);
        when(webClient.consultWorkerDTO(any())).thenReturn(response);

        // Act & Assert
        assertThrows(WSConsultIndependentWorkerFound.class, () -> affiliateService.responseFoundAffiliate(DOCUMENT_TYPE, DOCUMENT_NUMBER));
    }

    @Test
    void responseFoundAffiliate_AffiliateNotFound_ShouldThrowException() {
        // Arrange
        when(affiliateRepository.count(any(Specification.class))).thenReturn(0L);
        when(webClient.consultWorkerDTO(any())).thenReturn(null);

        // Act & Assert
        assertThrows(ResponseMessageAffiliation.class, () -> affiliateService.responseFoundAffiliate(DOCUMENT_TYPE, DOCUMENT_NUMBER));
    }

    @Test
    void getForeignPension_AffiliationFound_ShouldReturnPensionStatus() {
        // Arrange
        affiliation.setIsForeignPension(true);
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));

        // Act
        Boolean result = affiliateService.getForeignPension(FILED_NUMBER);

        // Assert
        assertTrue(result);
    }

    @Test
    void getForeignPension_AffiliationNotFound_ShouldThrowError() {
        // Arrange
        when(repositoryAffiliation.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AffiliationError.class, () -> affiliateService.getForeignPension(FILED_NUMBER));
    }

    @Test
    void findAffiliatesByTypeAndNumber_ShouldReturnList() {
        // Arrange
        List<Affiliate> affiliateList = Collections.singletonList(affiliate);
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER)).thenReturn(affiliateList);

        // Act
        List<Affiliate> result = affiliateService.findAffiliatesByTypeAndNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void numericSuffix_Test() throws Exception {
        // Arrange
        Method method = AffiliateServiceImpl.class.getDeclaredMethod("numericSuffix", String.class);
        method.setAccessible(true);

        // Act
        long result = (long) method.invoke(affiliateService, "SOL_AFI_2025000001450");

        // Assert
        assertEquals(1450L, result);
    }

    @Test
    void findAffiliate_NotFound_ShouldThrowException() {
        // Arrange
        when(affiliationDetailRepository.findFirstByIdentificationDocumentTypeAndIdentificationDocumentNumberOrderByFiledNumberDesc(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(anyString(), anyString()))
                .thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(NotFoundException.class, () -> affiliateService.findAffiliate(DOCUMENT_TYPE, DOCUMENT_NUMBER));
    }

    @Test
    void findAffiliate_OnlyAffiliationDetail_ShouldReturnId() {
        // Arrange
        when(affiliationDetailRepository.findFirstByIdentificationDocumentTypeAndIdentificationDocumentNumberOrderByFiledNumberDesc(anyString(), anyString()))
                .thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(anyString(), anyString()))
                .thenReturn(Optional.empty());
        // Act
        Long result = affiliateService.findAffiliate(DOCUMENT_TYPE, DOCUMENT_NUMBER);
        // Assert
        assertEquals(affiliation.getId(), result);
    }

    @Test
    void findAffiliate_OnlyMercantile_ShouldReturnId() {
        // Arrange
        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setId(2L);
        when(affiliationDetailRepository.findFirstByIdentificationDocumentTypeAndIdentificationDocumentNumberOrderByFiledNumberDesc(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(affiliateMercantileRepository.findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(anyString(), anyString()))
                .thenReturn(Optional.of(mercantile));
        // Act
        Long result = affiliateService.findAffiliate(DOCUMENT_TYPE, DOCUMENT_NUMBER);
        // Assert
        assertEquals(mercantile.getId(), result);
    }

    @Test
    void findAffiliate_MercantileIsNewer_ShouldReturnMercantileId() {
        // Arrange
        affiliation.setFiledNumber("SOL_AFI_2025000001433");
        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setId(2L);
        mercantile.setFiledNumber("SOL_AFI_2025000001450");
        when(affiliationDetailRepository.findFirstByIdentificationDocumentTypeAndIdentificationDocumentNumberOrderByFiledNumberDesc(anyString(), anyString()))
                .thenReturn(Optional.of(affiliation));
        when(affiliateMercantileRepository.findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(anyString(), anyString()))
                .thenReturn(Optional.of(mercantile));

        // Act
        Long result = affiliateService.findAffiliate(DOCUMENT_TYPE, DOCUMENT_NUMBER);
        // Assert
        assertEquals(mercantile.getId(), result);
    }

    @Test
    void getEmployerSize_ShouldReturnCorrectSize() {
        // Arrange
        EmployerSize size1 = new EmployerSize();
        size1.setId(1L);
        size1.setMinNumberWorker(1);
        size1.setMaxNumberWorker(10);

        EmployerSize size2 = new EmployerSize();
        size2.setId(2L);
        size2.setMinNumberWorker(11);
        size2.setMaxNumberWorker(50);

        when(employerSizeRepository.findAll()).thenReturn(Arrays.asList(size1, size2));

        // Act
        Long result = affiliateService.getEmployerSize(5);

        // Assert
        assertEquals(1L, result.longValue());
    }

    @Test
    void calculateIbcAmount_BelowMinimumWage_ShouldReturnMinimumWage() {
        // Arrange
        SalaryDTO salaryDTO = new SalaryDTO();
        salaryDTO.setValue("1000000");
        when(webClient.getSmlmvByYear(anyInt())).thenReturn(salaryDTO);

        BigDecimal monthlyContractValue = new BigDecimal("1000000");
        BigDecimal ibcPercentage = new BigDecimal("40");

        // Act
        BigDecimal result = affiliateService.calculateIbcAmount(monthlyContractValue, ibcPercentage);

        // Assert
        assertEquals(new BigDecimal("1000000"), result);
    }

    @Test
    void calculateIbcAmount_AboveMinimumWage_ShouldReturnCalculatedAmount() {
        // Arrange
        SalaryDTO salaryDTO = new SalaryDTO();
        salaryDTO.setValue("1000000");
        when(webClient.getSmlmvByYear(anyInt())).thenReturn(salaryDTO);

        BigDecimal monthlyContractValue = new BigDecimal("3000000");
        BigDecimal ibcPercentage = new BigDecimal("40");

        // Act
        BigDecimal result = affiliateService.calculateIbcAmount(monthlyContractValue, ibcPercentage);

        // Assert
        assertEquals(new BigDecimal("1200000.00"), result);
    }


    @Test
    void affiliateBUs_NoAffiliateInfo_ShouldReturnFalse() throws IOException {
        // Arrange
        when(consult.consultAffiliate(anyString(), anyString())).thenReturn(Mono.empty());

        // Act
        Boolean result = affiliateService.affiliateBUs(DOCUMENT_TYPE, DOCUMENT_NUMBER);

        // Assert
        assertFalse(result);
        verify(consult).consultAffiliate(DOCUMENT_TYPE, DOCUMENT_NUMBER);
    }

    @Test
    void getDataStatusAffiliations_WithOfficial_ShouldReturnDataWithName() {
        // Arrange
        affiliate.setIdOfficial(1L);
        List<Affiliate> affiliateList = Collections.singletonList(affiliate);
        Affiliation affiliation = new Affiliation();
        affiliation.setStageManagement("Some Stage");
        UserMain userMain = new UserMain();
        userMain.setFirstName("John");
        userMain.setSurname("Doe");

        doReturn(affiliateList).when(affiliateService).findAffiliatesByTypeAndNumber(DOCUMENT_TYPE, DOCUMENT_NUMBER);
        when(repositoryAffiliation.findByFiledNumber(affiliate.getFiledNumber())).thenReturn(Optional.of(affiliation));
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.of(userMain));

        // Act
        List<DataStatusAffiliationDTO> result = affiliateService.getDataStatusAffiliations(DOCUMENT_NUMBER, DOCUMENT_TYPE);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals("John Doe", result.get(0).getNameOfficial());
    }

    @Test
    void affiliateBUs_EmptyAffiliateInfo_ShouldReturnFalse() throws IOException {
        // Arrange
        when(consult.consultAffiliate(anyString(), anyString())).thenReturn(Mono.just(Collections.emptyList()));

        // Act
        Boolean result = affiliateService.affiliateBUs(DOCUMENT_TYPE, DOCUMENT_NUMBER);

        // Assert
        assertFalse(result);
        verify(consult).consultAffiliate(DOCUMENT_TYPE, DOCUMENT_NUMBER);
    }

    @Test
    void dateUtils_safeParse_ValidDate() {
        // Act
        LocalDate result = affiliateService.new DateUtils().safeParse("2023-01-01 10:00:00");
        // Assert
        assertEquals(LocalDate.of(2023, 1, 1), result);
    }

    @Test
    void ageCalculator_calculate_ValidDate() {
        // Act
        String result = affiliateService.new AgeCalculator().calculate(LocalDate.of(1990, 1, 1));
        // Assert
        assertNotNull(result);
    }

    @Test
    void ageCalculatorInt_calculate_ValidDate() {
        // Act
        Integer result = affiliateService.new AgeCalculatorInt().calculate(LocalDate.of(1990, 1, 1));
        // Assert
        assertNotNull(result);
    }
}