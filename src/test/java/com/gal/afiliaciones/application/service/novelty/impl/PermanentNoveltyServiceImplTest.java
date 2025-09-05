package com.gal.afiliaciones.application.service.novelty.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import com.gal.afiliaciones.application.service.affiliationdependent.AffiliationDependentService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.affiliationindependentpila.AffiliationIndependentPilaService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.application.service.novelty.PilaRetirementEventManagementService;
import com.gal.afiliaciones.application.service.retirement.RetirementService;
import com.gal.afiliaciones.application.service.risk.RiskFeeService;
import com.gal.afiliaciones.config.ex.NoveltyException;
import com.gal.afiliaciones.domain.model.Department;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.Occupation;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.RequestChannel;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.novelty.ContributorType;
import com.gal.afiliaciones.domain.model.novelty.NoveltyStatus;
import com.gal.afiliaciones.domain.model.novelty.NoveltyStatusCausal;
import com.gal.afiliaciones.domain.model.novelty.PermanentNovelty;
import com.gal.afiliaciones.domain.model.novelty.SubContributorType;
import com.gal.afiliaciones.domain.model.novelty.Traceability;
import com.gal.afiliaciones.domain.model.novelty.TypeOfContributor;
import com.gal.afiliaciones.domain.model.novelty.TypeOfUpdate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.DepartmentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.OccupationRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MainOfficeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.RequestChannelRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.eps.HealthPromotingEntityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.ContributorTypeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.NoveltyStatusCausalRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.NoveltyStatusRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.PermanentNoveltyDao;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.SubContributorTypeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.TraceabilityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.TypeOfContributorRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.novelty.TypeOfUpdateRepository;
import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.AffiliationDependentDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationdependent.DependentWorkerDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.CreatePermanentNoveltyDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.FilterConsultNoveltyDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyDependentRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyDetailDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyGeneralDataDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyIndependent45RequestDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyIndependentRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.RequestApplyNoveltyDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.ResponseValidationNoveltyDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

class PermanentNoveltyServiceImplTest {

    @Mock
    private PermanentNoveltyDao permanentNoveltyDao;
    @Mock
    private TypeOfUpdateRepository noveltyTypeRepository;
    @Mock
    private RequestChannelRepository channelRepository;
    @Mock
    private ContributorTypeRepository contributorTypeRepository;
    @Mock
    private TypeOfContributorRepository contributantTypeRepository;
    @Mock
    private SubContributorTypeRepository subContributanTypeRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private MunicipalityRepository municipalityRepository;
    @Mock
    private HealthPromotingEntityRepository epsRepository;
    @Mock
    private ArlRepository arlRepository;
    @Mock
    private IEconomicActivityRepository economicActivityRepository;
    @Mock
    private NoveltyStatusRepository noveltyStatusRepository;
    @Mock
    private NoveltyStatusCausalRepository causalRepository;
    @Mock
    private AffiliateRepository affiliateRepository;
    @Mock
    private AffiliationDependentService affiliationDependentService;
    @Mock
    private MainOfficeRepository mainOfficeRepository;
    @Mock
    private RiskFeeService riskFeeService;
    @Mock
    private OccupationRepository occupationRepository;
    @Mock
    private SendEmails sendEmail;
    @Mock
    private FiledService filedService;
    @Mock
    private AffiliationIndependentPilaService independentPilaService;
    @Mock
    private IUserPreRegisterRepository iUserPreRegisterRepository;
    @Mock
    private PilaRetirementEventManagementService pilaRetirementEventManagementService;
    @Mock
    private TraceabilityRepository traceabilityRepository;
    @Mock
    private RetirementService retirementService;
    @Mock
    private AffiliationDependentRepository affiliationDependentRepository;
    @Mock
    private GenericWebClient genericWebClient;

    @InjectMocks
    private PermanentNoveltyServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createPermanentNovelty_shouldThrowException_whenNoveltyTypeNotFound() {
        CreatePermanentNoveltyDTO dto = new CreatePermanentNoveltyDTO();
        dto.setNoveltyTypeId(1L);
        when(noveltyTypeRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NoveltyException.class, () -> service.createPermanentNovelty(dto));
    }

    @Test
    void getNoveltyTypes_shouldReturnList() {
        List<TypeOfUpdate> types = List.of(new TypeOfUpdate());
        when(noveltyTypeRepository.findAll()).thenReturn(types);
        List<TypeOfUpdate> result = service.getNoveltyTypes();
        assertEquals(types, result);
    }

    @Test
    void getNoveltyStatus_shouldReturnList() {
        List<NoveltyStatus> statuses = List.of(new NoveltyStatus());
        when(noveltyStatusRepository.findAll()).thenReturn(statuses);
        List<NoveltyStatus> result = service.getNoveltyStatus();
        assertEquals(statuses, result);
    }

    @Test
    void getConsultByFilter_shouldReturnPage() {
        FilterConsultNoveltyDTO filter = new FilterConsultNoveltyDTO();
        PermanentNovelty novelty = mock(PermanentNovelty.class);
        when(novelty.getId()).thenReturn(1L);
        when(novelty.getChannel()).thenReturn(mock(RequestChannel.class));
        when(novelty.getRegistryDate()).thenReturn(null);
        when(novelty.getContributorIdentificationType()).thenReturn("CC");
        when(novelty.getContributorIdentification()).thenReturn("123");
        when(novelty.getNameOrCompanyName()).thenReturn("Empresa");
        when(novelty.getContributantIdentificationType()).thenReturn("CC");
        when(novelty.getContributantIdentification()).thenReturn("456");
        when(novelty.getContributantFirstName()).thenReturn("Juan");
        when(novelty.getContributantSecondName()).thenReturn("Carlos");
        when(novelty.getContributantSurname()).thenReturn("Perez");
        when(novelty.getContributantSecondSurname()).thenReturn("Gomez");
        when(novelty.getNoveltyType()).thenReturn(mock(TypeOfUpdate.class));
        when(novelty.getStatus()).thenReturn(mock(NoveltyStatus.class));
        when(novelty.getCausal()).thenReturn(mock(NoveltyStatusCausal.class));
        Page<PermanentNovelty> page = new PageImpl<>(List.of(novelty));
        when(permanentNoveltyDao.findByFilters(filter)).thenReturn(page);
        Page<NoveltyGeneralDataDTO> result = service.getConsultByFilter(filter);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void applyOrNotApplyNovelty_shouldReturnFalse_onException() {
        RequestApplyNoveltyDTO request = new RequestApplyNoveltyDTO();
        when(permanentNoveltyDao.findById(anyLong())).thenThrow(new RuntimeException("DB error"));
        Boolean result = service.applyOrNotApplyNovelty(request);
        assertFalse(result);
    }

    @Test
    void capitalize_shouldReturnCapitalizedString() {
        String result = PermanentNoveltyServiceImpl.capitalize("test");
        assertEquals("Test", result);
        assertEquals("", PermanentNoveltyServiceImpl.capitalize(""));
        assertEquals("", PermanentNoveltyServiceImpl.capitalize(null));
    }

    @Test
    void export_shouldReturnExportDocumentsDTO() {
        TypeOfUpdate typeOfUpdate = new TypeOfUpdate();
        FilterConsultNoveltyDTO filter = new FilterConsultNoveltyDTO();
        ExportDocumentsDTO exportDTO = new ExportDocumentsDTO();

        typeOfUpdate.setDescription("PR");

        when(genericWebClient.exportDataGrid(any())).thenReturn(Optional.of(exportDTO));
        when(permanentNoveltyDao.exportAllData(filter)).thenReturn(List.of(PermanentNovelty.builder()
                .channel(RequestChannel.builder().name("PR").build())
                .registryDate(LocalDateTime.now())
                .contributorIdentification("123456789")
                .nameOrCompanyName("Empresa XYZ")
                .contributantIdentification("987654321")
                .noveltyType(typeOfUpdate)
                .status(NoveltyStatus.builder().status("Aprobado").build())
                .causal(NoveltyStatusCausal.builder().causal("Error administrativo").build())
                .build()));
        ExportDocumentsDTO result = service.export("pdf", filter);
        assertEquals(exportDTO, result);
    }

    @Test
    void createPermanentNovelty_shouldThrowException_whenChannelNotFound() {
        CreatePermanentNoveltyDTO dto = new CreatePermanentNoveltyDTO();
        dto.setNoveltyTypeId(1L);
        when(noveltyTypeRepository.findById(anyLong())).thenReturn(Optional.of(new TypeOfUpdate()));
        when(channelRepository.findByName(any())).thenReturn(Optional.empty());
        assertThrows(NoveltyException.class, () -> service.createPermanentNovelty(dto));
    }

    @Test
    void createPermanentNovelty_shouldThrowException_whenContributorTypeNotFound() {
        CreatePermanentNoveltyDTO dto = new CreatePermanentNoveltyDTO();
        dto.setNoveltyTypeId(1L);
        when(noveltyTypeRepository.findById(anyLong())).thenReturn(Optional.of(new TypeOfUpdate()));
        when(channelRepository.findByName(any())).thenReturn(Optional.of(new RequestChannel()));
        when(contributorTypeRepository.findByCode(any())).thenReturn(Optional.empty());
        assertThrows(NoveltyException.class, () -> service.createPermanentNovelty(dto));
    }

    @Test
    void createPermanentNovelty_shouldThrowException_whenContributantTypeNotFound() {
        CreatePermanentNoveltyDTO dto = new CreatePermanentNoveltyDTO();
        dto.setNoveltyTypeId(1L);
        dto.setContributantTypeCode(1);
        when(noveltyTypeRepository.findById(anyLong())).thenReturn(Optional.of(new TypeOfUpdate()));
        when(channelRepository.findByName(any())).thenReturn(Optional.of(new RequestChannel()));
        when(contributorTypeRepository.findByCode(any()))
                .thenReturn(Optional.of(new ContributorType()));
        when(contributantTypeRepository.findByCode(any(Short.class))).thenReturn(Optional.empty());
        assertThrows(NoveltyException.class, () -> service.createPermanentNovelty(dto));
    }

    @Test
    void createPermanentNovelty_shouldThrowException_whenSubContributantTypeNotFound() {
        CreatePermanentNoveltyDTO dto = new CreatePermanentNoveltyDTO();
        dto.setNoveltyTypeId(1L);
        dto.setContributantTypeCode(1);
        dto.setContributantSubtypeCode(1);
        when(noveltyTypeRepository.findById(anyLong())).thenReturn(Optional.of(new TypeOfUpdate()));
        when(channelRepository.findByName(any())).thenReturn(Optional.of(new RequestChannel()));
        when(contributorTypeRepository.findByCode(any()))
                .thenReturn(Optional.of(new ContributorType()));
        when(contributantTypeRepository.findByCode(any(Short.class)))
                .thenReturn(Optional.of(new TypeOfContributor()));
        when(subContributanTypeRepository.findByCode(any())).thenReturn(Optional.empty());
        assertThrows(NoveltyException.class, () -> service.createPermanentNovelty(dto));
    }

    @Test
    void createPermanentNovelty_shouldThrowException_whenDepartmentNotFound() {
        CreatePermanentNoveltyDTO dto = new CreatePermanentNoveltyDTO();
        dto.setNoveltyTypeId(1L);
        dto.setContributantTypeCode(1);
        dto.setContributantSubtypeCode(1);
        when(noveltyTypeRepository.findById(anyLong())).thenReturn(Optional.of(new TypeOfUpdate()));
        when(channelRepository.findByName(any())).thenReturn(Optional.of(new RequestChannel()));
        when(contributorTypeRepository.findByCode(any()))
                .thenReturn(Optional.of(new ContributorType()));
        when(contributantTypeRepository.findByCode(any(Short.class)))
                .thenReturn(Optional.of(new TypeOfContributor()));
        when(subContributanTypeRepository.findByCode(any()))
                .thenReturn(Optional.of(new SubContributorType()));
        when(departmentRepository.findByDepartmentCode(any())).thenReturn(Optional.empty());
        assertThrows(NoveltyException.class, () -> service.createPermanentNovelty(dto));
    }

    @Test
    void createPermanentNovelty_shouldThrowException_whenMunicipalityNotFound() {
        CreatePermanentNoveltyDTO dto = new CreatePermanentNoveltyDTO();
        dto.setNoveltyTypeId(1L);
        dto.setContributantTypeCode(1);
        dto.setContributantSubtypeCode(1);
        dto.setDepartmentCode("05");
        when(noveltyTypeRepository.findById(anyLong())).thenReturn(Optional.of(new TypeOfUpdate()));
        when(channelRepository.findByName(any())).thenReturn(Optional.of(new RequestChannel()));
        when(contributorTypeRepository.findByCode(any()))
                .thenReturn(Optional.of(new ContributorType()));
        when(contributantTypeRepository.findByCode(any(Short.class)))
                .thenReturn(Optional.of(new TypeOfContributor()));
        when(subContributanTypeRepository.findByCode(any()))
                .thenReturn(Optional.of(new SubContributorType()));
        when(departmentRepository.findByDepartmentCode(any()))
                .thenReturn(Optional.of(new com.gal.afiliaciones.domain.model.Department()));
        when(municipalityRepository.findByDivipolaCode(any())).thenReturn(Optional.empty());
        assertThrows(NoveltyException.class, () -> service.createPermanentNovelty(dto));
    }

    @Test
    void createPermanentNovelty_shouldThrowException_whenArlNotFound() {
        CreatePermanentNoveltyDTO dto = new CreatePermanentNoveltyDTO();
        dto.setNoveltyTypeId(1L);
        dto.setContributantTypeCode(1);
        dto.setContributantSubtypeCode(1);
        dto.setDepartmentCode("05");
        when(noveltyTypeRepository.findById(anyLong())).thenReturn(Optional.of(new TypeOfUpdate()));
        when(channelRepository.findByName(any())).thenReturn(Optional.of(new RequestChannel()));
        when(contributorTypeRepository.findByCode(any()))
                .thenReturn(Optional.of(new ContributorType()));
        when(contributantTypeRepository.findByCode(any(Short.class)))
                .thenReturn(Optional.of(new TypeOfContributor()));
        when(subContributanTypeRepository.findByCode(any()))
                .thenReturn(Optional.of(new SubContributorType()));
        when(departmentRepository.findByDepartmentCode(any()))
                .thenReturn(Optional.of(new com.gal.afiliaciones.domain.model.Department()));
        when(municipalityRepository.findByDivipolaCode(any()))
                .thenReturn(Optional.of(new com.gal.afiliaciones.domain.model.Municipality()));
        when(arlRepository.findByCodeARL(any())).thenReturn(Optional.empty());
        assertThrows(NoveltyException.class, () -> service.createPermanentNovelty(dto));
    }

    @Test
    void createPermanentNovelty_shouldThrowException_whenEconomicActivityNotFound() {
        CreatePermanentNoveltyDTO dto = new CreatePermanentNoveltyDTO();
        dto.setNoveltyTypeId(1L);
        dto.setContributantTypeCode(1);
        dto.setContributantSubtypeCode(1);
        dto.setDepartmentCode("05");
        when(noveltyTypeRepository.findById(anyLong())).thenReturn(Optional.of(new TypeOfUpdate()));
        when(channelRepository.findByName(any())).thenReturn(Optional.of(new RequestChannel()));
        when(contributorTypeRepository.findByCode(any()))
                .thenReturn(Optional.of(new ContributorType()));
        when(contributantTypeRepository.findByCode(any(Short.class)))
                .thenReturn(Optional.of(new TypeOfContributor()));
        when(subContributanTypeRepository.findByCode(any()))
                .thenReturn(Optional.of(new SubContributorType()));
        when(departmentRepository.findByDepartmentCode(any()))
                .thenReturn(Optional.of(new com.gal.afiliaciones.domain.model.Department()));
        when(municipalityRepository.findByDivipolaCode(any()))
                .thenReturn(Optional.of(new com.gal.afiliaciones.domain.model.Municipality()));
        when(arlRepository.findByCodeARL(any())).thenReturn(Optional.of(new com.gal.afiliaciones.domain.model.Arl()));
        when(economicActivityRepository.findByEconomicActivityCode(any())).thenReturn(Collections.emptyList());
        assertThrows(NoveltyException.class, () -> service.createPermanentNovelty(dto));
    }

    @Test
    void createPermanentNovelty_shouldThrowException_whenNoveltyStatusNotFound() {
        CreatePermanentNoveltyDTO dto = new CreatePermanentNoveltyDTO();
        dto.setNoveltyTypeId(1L);
        dto.setContributantTypeCode(1);
        dto.setContributantSubtypeCode(1);
        dto.setDepartmentCode("05");
        when(noveltyTypeRepository.findById(anyLong())).thenReturn(Optional.of(new TypeOfUpdate()));
        when(channelRepository.findByName(any())).thenReturn(Optional.of(new RequestChannel()));
        when(contributorTypeRepository.findByCode(any()))
                .thenReturn(Optional.of(new ContributorType()));
        when(contributantTypeRepository.findByCode(any(Short.class)))
                .thenReturn(Optional.of(new TypeOfContributor()));
        when(subContributanTypeRepository.findByCode(any()))
                .thenReturn(Optional.of(new SubContributorType()));
        when(departmentRepository.findByDepartmentCode(any()))
                .thenReturn(Optional.of(new com.gal.afiliaciones.domain.model.Department()));
        when(municipalityRepository.findByDivipolaCode(any()))
                .thenReturn(Optional.of(new com.gal.afiliaciones.domain.model.Municipality()));
        when(arlRepository.findByCodeARL(any())).thenReturn(Optional.of(new com.gal.afiliaciones.domain.model.Arl()));
        when(economicActivityRepository.findByEconomicActivityCode(any()))
                .thenReturn(List.of(new com.gal.afiliaciones.domain.model.EconomicActivity()));
        when(noveltyStatusRepository.findByStatus(any())).thenReturn(Optional.empty());
        assertThrows(NoveltyException.class, () -> service.createPermanentNovelty(dto));
    }

    @Test
    void createPermanentNovelty_shouldThrowException_whenNoveltyStatusCausalNotFound() {
        CreatePermanentNoveltyDTO dto = new CreatePermanentNoveltyDTO();
        dto.setNoveltyTypeId(1L);
        dto.setContributantTypeCode(1);
        dto.setContributantSubtypeCode(1);
        dto.setDepartmentCode("05");
        when(noveltyTypeRepository.findById(anyLong())).thenReturn(Optional.of(new TypeOfUpdate()));
        when(channelRepository.findByName(any())).thenReturn(Optional.of(new RequestChannel()));
        when(contributorTypeRepository.findByCode(any()))
                .thenReturn(Optional.of(new ContributorType()));
        when(contributantTypeRepository.findByCode(any(Short.class)))
                .thenReturn(Optional.of(new TypeOfContributor()));
        when(subContributanTypeRepository.findByCode(any()))
                .thenReturn(Optional.of(new SubContributorType()));
        when(departmentRepository.findByDepartmentCode(any()))
                .thenReturn(Optional.of(new com.gal.afiliaciones.domain.model.Department()));
        when(municipalityRepository.findByDivipolaCode(any()))
                .thenReturn(Optional.of(new com.gal.afiliaciones.domain.model.Municipality()));
        when(arlRepository.findByCodeARL(any())).thenReturn(Optional.of(new com.gal.afiliaciones.domain.model.Arl()));
        when(economicActivityRepository.findByEconomicActivityCode(any()))
                .thenReturn(List.of(new com.gal.afiliaciones.domain.model.EconomicActivity()));
        when(noveltyStatusRepository.findByStatus(any())).thenReturn(Optional.of(new NoveltyStatus()));
        when(causalRepository.findByStatus(any())).thenReturn(Optional.empty());
        assertThrows(NoveltyException.class, () -> service.createPermanentNovelty(dto));
    }

    @Test
    void validateFields_shouldSetNotApplyStatus_whenEmployerAffiliateIsNull() throws Exception {
        PermanentNovelty novelty = new PermanentNovelty();
        ContributorType contributorType = new ContributorType();
        contributorType.setCode(Constant.CODE_CONTRIBUTOR_TYPE_EMPLOYER);
        novelty.setContributorType(contributorType);
        TypeOfContributor typeOfContributor = new TypeOfContributor();
        typeOfContributor.setCode((short) 1);
        novelty.setContributantType(typeOfContributor);

        NoveltyStatus notApplyStatus = new NoveltyStatus();
        notApplyStatus.setStatus(Constant.NOVELTY_NOT_APPLY_STATUS);
        NoveltyStatusCausal causal = new NoveltyStatusCausal();
        causal.setId(Constant.CAUSAL_EMPLOYER_NOT_AFFILIATE);

        when(noveltyStatusRepository
                .findByStatus(Constant.NOVELTY_NOT_APPLY_STATUS))
                .thenReturn(Optional.of(notApplyStatus));
        when(causalRepository
                .findById(Constant.CAUSAL_EMPLOYER_NOT_AFFILIATE))
                .thenReturn(Optional.of(causal));

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("validateFields",
                PermanentNovelty.class, Affiliate.class);
        method.setAccessible(true);

        ResponseValidationNoveltyDTO result = (ResponseValidationNoveltyDTO) method
                .invoke(service, novelty, null);

        assertEquals(Constant.NOVELTY_NOT_APPLY_STATUS,
                result.getStatus().getStatus());
        assertEquals(Constant.CAUSAL_EMPLOYER_NOT_AFFILIATE,
                result.getCausal().getId());
    }

    @Test
    void validateFields_shouldSetNotApplyStatus_whenEmployerAffiliateIsInactive() throws Exception {
        PermanentNovelty novelty = new PermanentNovelty();
        ContributorType contributorType = new ContributorType();
        contributorType.setCode(Constant.CODE_CONTRIBUTOR_TYPE_EMPLOYER);
        novelty.setContributorType(contributorType);
        TypeOfContributor typeOfContributor = new TypeOfContributor();
        typeOfContributor.setCode((short) 1);
        novelty.setContributantType(typeOfContributor);

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_INACTIVE);

        NoveltyStatus notApplyStatus = new NoveltyStatus();
        notApplyStatus.setStatus(Constant.NOVELTY_NOT_APPLY_STATUS);
        NoveltyStatusCausal causal = new NoveltyStatusCausal();
        causal.setId(Constant.CAUSAL_EMPLOYER_INACTIVE);

        when(noveltyStatusRepository
                .findByStatus(Constant.NOVELTY_NOT_APPLY_STATUS))
                .thenReturn(Optional.of(notApplyStatus));
        when(causalRepository.findById(Constant.CAUSAL_EMPLOYER_INACTIVE))
                .thenReturn(Optional.of(causal));

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("validateFields",
                PermanentNovelty.class, Affiliate.class);
        method.setAccessible(true);

        ResponseValidationNoveltyDTO result = (ResponseValidationNoveltyDTO) method
                .invoke(service, novelty, affiliate);

        assertEquals(Constant.NOVELTY_NOT_APPLY_STATUS,
                result.getStatus().getStatus());
        assertEquals(Constant.CAUSAL_EMPLOYER_INACTIVE,
                result.getCausal().getId());
    }

    @Test
    void validateFields_shouldSetNotApplyStatus_whenContributantIsDomesticAndContributorIsCompany() throws Exception {
        com.gal.afiliaciones.domain.model.novelty.PermanentNovelty novelty = new PermanentNovelty();
        ContributorType contributorType = new ContributorType();
        contributorType.setCode("01");
        novelty.setContributorType(contributorType);
        TypeOfContributor typeOfContributor = new TypeOfContributor();
        typeOfContributor.setCode(Constant.CODE_CONTRIBUTANT_TYPE_DOMESTIC.shortValue());
        novelty.setContributantType(typeOfContributor);
        novelty.setContributorIdentificationType(Constant.NI);

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);

        NoveltyStatus notApplyStatus = new NoveltyStatus();
        notApplyStatus.setStatus(Constant.NOVELTY_NOT_APPLY_STATUS);
        NoveltyStatusCausal causal = new NoveltyStatusCausal();
        causal.setId(Constant.CAUSAL_CONTRIBUTANT_DOMESTIC);

        when(noveltyStatusRepository
                .findByStatus(Constant.NOVELTY_NOT_APPLY_STATUS))
                .thenReturn(Optional.of(notApplyStatus));
        when(causalRepository.findById(Constant.CAUSAL_CONTRIBUTANT_DOMESTIC))
                .thenReturn(Optional.of(causal));

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("validateFields",
                PermanentNovelty.class, Affiliate.class);
        method.setAccessible(true);

        ResponseValidationNoveltyDTO result = (ResponseValidationNoveltyDTO) method
                .invoke(service, novelty, affiliate);

        assertEquals(Constant.NOVELTY_NOT_APPLY_STATUS,
                result.getStatus().getStatus());
        assertEquals(Constant.CAUSAL_CONTRIBUTANT_DOMESTIC,
                result.getCausal().getId());
    }

    @Test
    void validateFields_shouldSetReviewStatus_whenNoveltyValueIsC() throws Exception {
        PermanentNovelty novelty = new PermanentNovelty();
        ContributorType contributorType = new ContributorType();
        contributorType.setCode("01");
        novelty.setContributorType(contributorType);
        TypeOfContributor typeOfContributor = new TypeOfContributor();
        typeOfContributor.setCode((short) 1);
        novelty.setContributantType(typeOfContributor);
        novelty.setContributorIdentificationType("CC");
        novelty.setNoveltyValue(Constant.NOVELTY_VALUE_C);

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);

        NoveltyStatus reviewStatus = new NoveltyStatus();
        reviewStatus.setStatus(Constant.NOVELTY_REVIEW_STATUS);
        NoveltyStatusCausal causal = new NoveltyStatusCausal();
        causal.setId(Constant.CAUSAL_COMPENSATION_FUND);

        when(noveltyStatusRepository
                .findByStatus(Constant.NOVELTY_REVIEW_STATUS))
                .thenReturn(Optional.of(reviewStatus));
        when(causalRepository.findById(Constant.CAUSAL_COMPENSATION_FUND))
                .thenReturn(Optional.of(causal));

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("validateFields",
                PermanentNovelty.class, Affiliate.class);
        method.setAccessible(true);

        ResponseValidationNoveltyDTO result = (ResponseValidationNoveltyDTO) method
                .invoke(service, novelty, affiliate);

        assertEquals(Constant.NOVELTY_REVIEW_STATUS,
                result.getStatus().getStatus());
        assertEquals(Constant.CAUSAL_COMPENSATION_FUND,
                result.getCausal().getId());
    }

    @Test
    void validateFields_shouldSetReviewStatus_whenInitNoveltyDateIsNull() throws Exception {
        PermanentNovelty novelty = new PermanentNovelty();
        ContributorType contributorType = new ContributorType();
        contributorType.setCode("01");
        novelty.setContributorType(contributorType);
        TypeOfContributor typeOfContributor = new TypeOfContributor();
        typeOfContributor.setCode((short) 1);
        novelty.setContributantType(typeOfContributor);
        novelty.setContributorIdentificationType("CC");
        novelty.setNoveltyValue("A");
        novelty.setInitNoveltyDate(null);

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);

        NoveltyStatus reviewStatus = new NoveltyStatus();
        reviewStatus.setStatus(Constant.NOVELTY_REVIEW_STATUS);
        NoveltyStatusCausal causal = new NoveltyStatusCausal();
        causal.setId(Constant.CAUSAL_NOT_DATE_NOVELTY);

        when(noveltyStatusRepository
                .findByStatus(Constant.NOVELTY_REVIEW_STATUS))
                .thenReturn(Optional.of(reviewStatus));
        when(causalRepository.findById(Constant.CAUSAL_NOT_DATE_NOVELTY))
                .thenReturn(Optional.of(causal));

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("validateFields",
                PermanentNovelty.class, Affiliate.class);
        method.setAccessible(true);

        ResponseValidationNoveltyDTO result = (ResponseValidationNoveltyDTO) method
                .invoke(service, novelty, affiliate);

        assertEquals(Constant.NOVELTY_REVIEW_STATUS,
                result.getStatus().getStatus());
        assertEquals(Constant.CAUSAL_NOT_DATE_NOVELTY,
                result.getCausal().getId());
    }

    @Test
    void validateFields_shouldSetNotApplyStatus_whenInitNoveltyDateIsBeforeAffiliationDate() throws Exception {
        PermanentNovelty novelty = new PermanentNovelty();
        ContributorType contributorType = new ContributorType();
        contributorType.setCode("01");
        novelty.setContributorType(contributorType);
        TypeOfContributor typeOfContributor = new TypeOfContributor();
        typeOfContributor.setCode((short) 1);
        novelty.setContributantType(typeOfContributor);
        novelty.setContributorIdentificationType("CC");
        novelty.setNoveltyValue("A");
        novelty.setInitNoveltyDate(java.time.LocalDate.now().minusDays(5));

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        affiliate.setAffiliationDate(java.time.LocalDateTime.now());

        NoveltyStatus notApplyStatus = new NoveltyStatus();
        notApplyStatus.setStatus(Constant.NOVELTY_NOT_APPLY_STATUS);
        NoveltyStatusCausal causal = new NoveltyStatusCausal();
        causal.setId(Constant.CAUSAL_LESS_DATE);

        when(noveltyStatusRepository
                .findByStatus(Constant.NOVELTY_NOT_APPLY_STATUS))
                .thenReturn(Optional.of(notApplyStatus));
        when(causalRepository.findById(Constant.CAUSAL_LESS_DATE))
                .thenReturn(Optional.of(causal));

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("validateFields",
                PermanentNovelty.class, Affiliate.class);
        method.setAccessible(true);

        ResponseValidationNoveltyDTO result = (ResponseValidationNoveltyDTO) method
                .invoke(service, novelty, affiliate);

        assertEquals(Constant.NOVELTY_NOT_APPLY_STATUS,
                result.getStatus().getStatus());
        assertEquals(Constant.CAUSAL_LESS_DATE, result.getCausal().getId());
    }

    @Test
    void validateFields_shouldSetPendingStatus_whenAllValidationsPass() throws Exception {
        PermanentNovelty novelty = new PermanentNovelty();
        ContributorType contributorType = new ContributorType();
        contributorType.setCode(Constant.CODE_CONTRIBUTOR_TYPE_INDEPENDENT);
        novelty.setContributorType(contributorType);
        TypeOfContributor typeOfContributor = new TypeOfContributor();
        typeOfContributor.setCode((short) 1);
        novelty.setContributantType(typeOfContributor);
        novelty.setContributorIdentificationType("CC");
        novelty.setNoveltyValue("A");
        novelty.setInitNoveltyDate(java.time.LocalDate.now());

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        affiliate.setAffiliationDate(java.time.LocalDateTime.now().minusDays(2));

        NoveltyStatus pendingStatus = new NoveltyStatus();
        pendingStatus.setStatus(Constant.NOVELTY_PENDING_STATUS);
        NoveltyStatusCausal causal = new NoveltyStatusCausal();
        causal.setId(Constant.CAUSAL_PENDING);

        when(noveltyStatusRepository
                .findByStatus(Constant.NOVELTY_PENDING_STATUS))
                .thenReturn(Optional.of(pendingStatus));
        when(causalRepository.findById(Constant.CAUSAL_PENDING))
                .thenReturn(Optional.of(causal));

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("validateFields",
                PermanentNovelty.class, Affiliate.class);
        method.setAccessible(true);

        ResponseValidationNoveltyDTO result = (ResponseValidationNoveltyDTO) method
                .invoke(service, novelty, affiliate);

        assertEquals(Constant.NOVELTY_PENDING_STATUS,
                result.getStatus().getStatus());
        assertEquals(Constant.CAUSAL_PENDING, result.getCausal().getId());
    }

    @Test
    void affiliateIndependentRisk4and5_nonHighRisk_shouldSetReviewStatus() {
        // Arrange
        PermanentNovelty novelty = new PermanentNovelty();
        novelty.setRisk("2"); // "2" is not considered high risk

        NoveltyIndependent45RequestDTO request = new NoveltyIndependent45RequestDTO();
        request.setNovelty(novelty);
        request.setIdBondingType(3L);
        LocalDate coverageDate = LocalDate.now();
        request.setCoverageDate(coverageDate);
        request.setIdHeadquarter(1L);
        request.setIdDepartment(1L);
        request.setIdCity(1L);
        request.setAddressContributor("Test Address");

        // Create a dummy affiliate as required.
        Affiliate mockAffiliate = mock(Affiliate.class);
        when(mockAffiliate.getDocumentType()).thenReturn("CC");
        when(mockAffiliate.getDocumentNumber()).thenReturn("123");
        request.setAffiliate(mockAffiliate);

        // For non-high risk, the affiliationDependentService is not used.
        // Setup status and causal for review branch.
        NoveltyStatus reviewStatus = new NoveltyStatus();
        reviewStatus.setStatus(Constant.NOVELTY_REVIEW_STATUS);
        when(noveltyStatusRepository.findByStatus(Constant.NOVELTY_REVIEW_STATUS))
                .thenReturn(Optional.of(reviewStatus));

        NoveltyStatusCausal causalReview = new NoveltyStatusCausal();
        causalReview.setCausal("Review");
        when(causalRepository.findById(Constant.CAUSAL_CONTRIBUTANT_123)).thenReturn(Optional.of(causalReview));

        // Save the novelty and return one with a filed number.
        novelty.setFiledNumber("F456");
        when(permanentNoveltyDao.createNovelty(any(PermanentNovelty.class))).thenReturn(novelty);

        // Act via reflection.
        String filedNumber = (String) ReflectionTestUtils.invokeMethod(service, "affiliateIndependentRisk4and5",
                request);

        // Assert
        assertEquals("F456", filedNumber);
    }

    @Test
    void getNoveltyDetail_shouldReturnCorrectDTO() {
        // Arrange
        Long noveltyId = 1L;
        PermanentNovelty novelty = new PermanentNovelty();
        novelty.setId(noveltyId);
        novelty.setContributorDv(5);
        novelty.setInitNoveltyDate(LocalDate.of(2023, 1, 1));
        novelty.setContributantFirstName("JOHN");
        novelty.setContributantSecondName("FITZGERALD");
        novelty.setContributantSurname("KENNEDY");
        novelty.setContributantSecondSurname("ROOSEVELT");
        novelty.setNoveltyValue("SomeValue");

        TypeOfUpdate typeOfUpdate = new TypeOfUpdate();
        typeOfUpdate.setCode("ING");
        typeOfUpdate.setDescription("Ingreso");
        typeOfUpdate.setGroup("Transaccional");
        novelty.setNoveltyType(typeOfUpdate);

        ContributorType contributorType = new ContributorType();
        contributorType.setId(1L);
        contributorType.setDescription("Empleador");
        novelty.setContributorType(contributorType);

        TypeOfContributor typeOfContributor = new TypeOfContributor();
        typeOfContributor.setCode((short) 1);
        typeOfContributor.setDescription("Dependiente");
        novelty.setContributantType(typeOfContributor);

        SubContributorType subContributorType = new SubContributorType();
        subContributorType.setCode("01");
        subContributorType.setDescription("Subtipo Dependiente");
        novelty.setContributantSubtype(subContributorType);

        com.gal.afiliaciones.domain.model.Department department = new com.gal.afiliaciones.domain.model.Department();
        department.setDepartmentCode("05");
        department.setDepartmentName("ANTIOQUIA");
        novelty.setDepartment(department);

        com.gal.afiliaciones.domain.model.Municipality municipality = new com.gal.afiliaciones.domain.model.Municipality();
        municipality.setMunicipalityCode("001");
        municipality.setMunicipalityName("MEDELLIN");
        novelty.setMunicipality(municipality);

        com.gal.afiliaciones.domain.model.Health health = new com.gal.afiliaciones.domain.model.Health();
        health.setCodeEPS("EPS001");
        health.setNameEPS("SURA");
        novelty.setHealthPromotingEntity(health);

        com.gal.afiliaciones.domain.model.Arl arl = new com.gal.afiliaciones.domain.model.Arl();
        arl.setCodeARL("ARL001");
        arl.setAdministrator("POSITIVA");
        novelty.setOccupationalRiskManager(arl);

        NoveltyStatusCausal causal = new NoveltyStatusCausal();
        causal.setCausal("Causal de prueba");
        novelty.setCausal(causal);

        com.gal.afiliaciones.domain.model.EconomicActivity economicActivity = new com.gal.afiliaciones.domain.model.EconomicActivity();
        economicActivity.setEconomicActivityCode("1234");
        novelty.setEconomicActivity(economicActivity);

        NoveltyStatus status = new NoveltyStatus();
        status.setStatus(Constant.NOVELTY_REVIEW_STATUS);
        novelty.setStatus(status);

        when(permanentNoveltyDao.findById(noveltyId)).thenReturn(novelty);

        // Act
        NoveltyDetailDTO result = service.getNoveltyDetail(noveltyId);

        // Assert
        assertEquals("ING - Ingreso: SomeValue", result.getNoveltyType());
        assertEquals("1 - Empleador", result.getContributorType());
        assertEquals("JOHN FITZGERALD KENNEDY ROOSEVELT", result.getCompleteContributantName());
        assertEquals("1 - Dependiente", result.getContributantType());
        assertEquals("01 - Subtipo Dependiente", result.getContributantSubtype());
        assertEquals("05 - ANTIOQUIA", result.getDepartment());
        assertEquals("001 - MEDELLIN", result.getMunicipality());
        assertEquals("EPS001 - SURA", result.getHealthPromotingEntity());
        assertEquals("ARL001 - POSITIVA", result.getOccupationalRiskManager());
        assertEquals("01/01/2023", result.getInitNoveltyDate());
        assertEquals("Causal de prueba", result.getCausal());
        assertEquals(5, result.getContributorDv());
        assertEquals("1234", result.getEconomicActivity());
        assertEquals(true, result.getIsReview());
        assertEquals("Transaccional", result.getNoveltyIdentity());
    }

    @Test
    void getNoveltyDetail_shouldHandleNullAndBlankValues() {
        // Arrange
        Long noveltyId = 1L;
        PermanentNovelty novelty = new PermanentNovelty();
        novelty.setId(noveltyId);
        novelty.setContributorDv(null);
        novelty.setInitNoveltyDate(null);
        novelty.setContributantFirstName("JOHN");
        novelty.setContributantSecondName(null);
        novelty.setContributantSurname("KENNEDY");
        novelty.setContributantSecondSurname(" ");
        novelty.setNoveltyValue("SomeValue");

        TypeOfUpdate typeOfUpdate = new TypeOfUpdate();
        typeOfUpdate.setCode("ING");
        typeOfUpdate.setDescription("Ingreso");
        typeOfUpdate.setGroup("Transaccional");
        novelty.setNoveltyType(typeOfUpdate);

        ContributorType contributorType = new ContributorType();
        contributorType.setId(1L);
        contributorType.setDescription("Empleador");
        novelty.setContributorType(contributorType);

        TypeOfContributor typeOfContributor = new TypeOfContributor();
        typeOfContributor.setCode((short) 1);
        typeOfContributor.setDescription("Dependiente");
        novelty.setContributantType(typeOfContributor);

        SubContributorType subContributorType = new SubContributorType();
        subContributorType.setCode("01");
        subContributorType.setDescription("Subtipo Dependiente");
        novelty.setContributantSubtype(subContributorType);

        com.gal.afiliaciones.domain.model.Department department = new com.gal.afiliaciones.domain.model.Department();
        department.setDepartmentCode("05");
        department.setDepartmentName("ANTIOQUIA");
        novelty.setDepartment(department);

        com.gal.afiliaciones.domain.model.Municipality municipality = new com.gal.afiliaciones.domain.model.Municipality();
        municipality.setMunicipalityCode("001");
        municipality.setMunicipalityName("MEDELLIN");
        novelty.setMunicipality(municipality);

        com.gal.afiliaciones.domain.model.Health health = new com.gal.afiliaciones.domain.model.Health();
        health.setCodeEPS("EPS001");
        health.setNameEPS("SURA");
        novelty.setHealthPromotingEntity(health);

        com.gal.afiliaciones.domain.model.Arl arl = new com.gal.afiliaciones.domain.model.Arl();
        arl.setCodeARL("ARL001");
        arl.setAdministrator("POSITIVA");
        novelty.setOccupationalRiskManager(arl);

        NoveltyStatusCausal causal = new NoveltyStatusCausal();
        causal.setCausal("Causal de prueba");
        novelty.setCausal(causal);

        com.gal.afiliaciones.domain.model.EconomicActivity economicActivity = new com.gal.afiliaciones.domain.model.EconomicActivity();
        economicActivity.setEconomicActivityCode("1234");
        novelty.setEconomicActivity(economicActivity);

        NoveltyStatus status = new NoveltyStatus();
        status.setStatus(Constant.NOVELTY_APPLY_STATUS);
        novelty.setStatus(status);

        when(permanentNoveltyDao.findById(noveltyId)).thenReturn(novelty);

        // Act
        NoveltyDetailDTO result = service.getNoveltyDetail(noveltyId);

        // Assert
        assertEquals("JOHN KENNEDY", result.getCompleteContributantName());
        assertEquals("Sin información", result.getInitNoveltyDate());
        assertEquals(0, result.getContributorDv());
        assertEquals(false, result.getIsReview());
    }

    @Test
    void convertNoveltyToDto_shouldMapFieldsUsingFosyga_whenHealthPromotingEntityNull() throws Exception {
        // Prepare novelty with various fields set
        PermanentNovelty novelty = new PermanentNovelty();
        novelty.setContributantIdentificationType("IDTYPE");
        novelty.setContributantIdentification("123");
        novelty.setContributantFirstName("John");
        novelty.setContributantSecondName("Doe");
        novelty.setContributantSurname("Smith");
        novelty.setContributantSecondSurname("Jones");
        // department and municipality as mocks to control getIdDepartment and
        // getIdMunicipality
        com.gal.afiliaciones.domain.model.Department department = mock(
                com.gal.afiliaciones.domain.model.Department.class);
        when(department.getIdDepartment()).thenReturn(5);
        novelty.setDepartment(department);
        com.gal.afiliaciones.domain.model.Municipality municipality = mock(
                com.gal.afiliaciones.domain.model.Municipality.class);
        when(municipality.getIdMunicipality()).thenReturn(10L);
        novelty.setMunicipality(municipality);
        novelty.setAddressContributor("Addr");
        novelty.setPhoneContributor("555");
        novelty.setEmailContributor("email@test.com");
        // use initNoveltyDate so registryDate is not needed
        java.time.LocalDate initDate = java.time.LocalDate.of(2021, 1, 1);
        novelty.setInitNoveltyDate(initDate);
        // salary and economic activity
        novelty.setSalary(new java.math.BigDecimal("1000"));
        com.gal.afiliaciones.domain.model.EconomicActivity econ = mock(
                com.gal.afiliaciones.domain.model.EconomicActivity.class);
        when(econ.getEconomicActivityCode()).thenReturn("ECODE");
        novelty.setEconomicActivity(econ);
        // risk and services stubbing
        novelty.setRisk("RISK");
        when(riskFeeService.getFeeByRisk("RISK")).thenReturn(new java.math.BigDecimal("2"));
        // contributor and contributant types
        com.gal.afiliaciones.domain.model.novelty.ContributorType contributorType = new com.gal.afiliaciones.domain.model.novelty.ContributorType();
        contributorType.setCode("CT");
        novelty.setContributorType(contributorType);
        com.gal.afiliaciones.domain.model.novelty.TypeOfContributor toc = new com.gal.afiliaciones.domain.model.novelty.TypeOfContributor();
        toc.setCode((short) 2);
        novelty.setContributantType(toc);
        com.gal.afiliaciones.domain.model.novelty.SubContributorType sct = new com.gal.afiliaciones.domain.model.novelty.SubContributorType();
        sct.setCode("3");
        novelty.setContributantSubtype(sct);
        // stub FOSYGA lookup
        com.gal.afiliaciones.domain.model.Health fosyga = new com.gal.afiliaciones.domain.model.Health();
        fosyga.setId(99L);
        when(epsRepository.findByCodeEPS(Constant.FOSYGA_CODE_EPS)).thenReturn(Optional.of(fosyga));

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class
                .getDeclaredMethod("convertNoveltyToDto", PermanentNovelty.class);
        method.setAccessible(true);
        com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyIndependentRequestDTO dto = (com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyIndependentRequestDTO) method
                .invoke(service, novelty);

        assertEquals("IDTYPE", dto.getIdentificationDocumentType());
        assertEquals("123", dto.getIdentificationDocumentNumber());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getSecondName());
        assertEquals("Smith", dto.getSurname());
        assertEquals("Jones", dto.getSecondSurname());
        assertEquals(99L, dto.getHealthPromotingEntity());
        assertEquals(5L, dto.getDepartment());
        assertEquals(10L, dto.getCityMunicipality());
        assertEquals("Addr", dto.getAddress());
        assertEquals("555", dto.getPhone1());
        assertEquals("email@test.com", dto.getEmail());
        assertEquals(initDate, dto.getStartDate());
        assertEquals(initDate.plusMonths(1), dto.getEndDate());
        assertEquals(new java.math.BigDecimal("1000"), dto.getContractMonthlyValue());
        assertEquals("ECODE", dto.getCodeMainEconomicActivity());
        assertEquals("RISK", dto.getRisk());
        assertEquals(new java.math.BigDecimal("200"), dto.getPrice());
        assertEquals("CT", dto.getContributorTypeCode());
        assertEquals(2, dto.getContributantTypeCode());
        assertEquals(3, dto.getContributantSubtypeCode());
    }

    @Test
    void convertNoveltyToDto_shouldMapFieldsUsingNoveltyHealthEntity_whenHealthPromotingEntityNotNull()
            throws Exception {
        PermanentNovelty novelty = new PermanentNovelty();
        novelty.setContributantIdentificationType("ZZ");
        novelty.setContributantIdentification("999");
        novelty.setContributantFirstName("A");
        novelty.setContributantSecondName("B");
        novelty.setContributantSurname("C");
        novelty.setContributantSecondSurname("D");
        // mocked department and municipality
        com.gal.afiliaciones.domain.model.Department department = mock(
                com.gal.afiliaciones.domain.model.Department.class);
        when(department.getIdDepartment()).thenReturn(7);
        novelty.setDepartment(department);
        com.gal.afiliaciones.domain.model.Municipality municipality = mock(
                com.gal.afiliaciones.domain.model.Municipality.class);
        when(municipality.getIdMunicipality()).thenReturn(8L);
        novelty.setMunicipality(municipality);
        novelty.setAddressContributor("X Addr");
        novelty.setPhoneContributor("777");
        novelty.setEmailContributor("foo@bar.com");
        java.time.LocalDate initDate = java.time.LocalDate.of(2022, 2, 2);
        novelty.setInitNoveltyDate(initDate);
        novelty.setSalary(new java.math.BigDecimal("500"));
        com.gal.afiliaciones.domain.model.EconomicActivity econ = mock(
                com.gal.afiliaciones.domain.model.EconomicActivity.class);
        when(econ.getEconomicActivityCode()).thenReturn("CODE2");
        novelty.setEconomicActivity(econ);
        novelty.setRisk("R2");
        when(riskFeeService.getFeeByRisk("R2")).thenReturn(new java.math.BigDecimal("3"));
        com.gal.afiliaciones.domain.model.novelty.ContributorType contributorType = new com.gal.afiliaciones.domain.model.novelty.ContributorType();
        contributorType.setCode("ZC");
        novelty.setContributorType(contributorType);
        com.gal.afiliaciones.domain.model.novelty.TypeOfContributor toc = new com.gal.afiliaciones.domain.model.novelty.TypeOfContributor();
        toc.setCode((short) 4);
        novelty.setContributantType(toc);
        com.gal.afiliaciones.domain.model.novelty.SubContributorType sct = new com.gal.afiliaciones.domain.model.novelty.SubContributorType();
        sct.setCode("5");
        novelty.setContributantSubtype(sct);
        // set a non-null HealthPromotingEntity
        com.gal.afiliaciones.domain.model.Health he = new com.gal.afiliaciones.domain.model.Health();
        he.setId(123L);
        novelty.setHealthPromotingEntity(he);

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class
                .getDeclaredMethod("convertNoveltyToDto", PermanentNovelty.class);
        method.setAccessible(true);
        com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyIndependentRequestDTO dto = (com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyIndependentRequestDTO) method
                .invoke(service, novelty);

        assertEquals("ZZ", dto.getIdentificationDocumentType());
        assertEquals("999", dto.getIdentificationDocumentNumber());
        assertEquals("A", dto.getFirstName());
        assertEquals("B", dto.getSecondName());
        assertEquals("C", dto.getSurname());
        assertEquals("D", dto.getSecondSurname());
        assertEquals(123L, dto.getHealthPromotingEntity());
        assertEquals(7L, dto.getDepartment());
        assertEquals(8L, dto.getCityMunicipality());
        assertEquals("X Addr", dto.getAddress());
        assertEquals("777", dto.getPhone1());
        assertEquals("foo@bar.com", dto.getEmail());
        assertEquals(initDate, dto.getStartDate());
        assertEquals(initDate.plusMonths(1), dto.getEndDate());
        assertEquals(new java.math.BigDecimal("500"), dto.getContractMonthlyValue());
        assertEquals("CODE2", dto.getCodeMainEconomicActivity());
        assertEquals("R2", dto.getRisk());
        assertEquals(new java.math.BigDecimal("300"), dto.getPrice());
        assertEquals("ZC", dto.getContributorTypeCode());
        assertEquals(4, dto.getContributantTypeCode());
        assertEquals(5, dto.getContributantSubtypeCode());
    }

    @Test
    void applyNovelty_shouldCallAffiliateDependent_whenContributorIsEmployerAndBondingTypeIsNot4() throws Exception {
        PermanentNovelty novelty = new PermanentNovelty();
        ContributorType contributorType = new ContributorType();
        contributorType.setCode(Constant.CODE_CONTRIBUTOR_TYPE_EMPLOYER);
        novelty.setContributorType(contributorType);
        TypeOfContributor contributantType = new TypeOfContributor();
        contributantType.setCode((short) 1); // Dependent
        novelty.setContributantType(contributantType);
        novelty.setRegistryDate(LocalDateTime.now());
        com.gal.afiliaciones.domain.model.Department department = new com.gal.afiliaciones.domain.model.Department();
        department.setIdDepartment(1);
        novelty.setDepartment(department);
        com.gal.afiliaciones.domain.model.Municipality municipality = new com.gal.afiliaciones.domain.model.Municipality();
        municipality.setIdMunicipality(1L);
        novelty.setMunicipality(municipality);
        novelty.setEconomicActivity(new com.gal.afiliaciones.domain.model.EconomicActivity());
        novelty.setSalary(BigDecimal.ZERO);
        novelty.setRisk("1");

        Affiliate affiliate = new Affiliate();
        affiliate.setDocumentType("CC");
        affiliate.setDocumentNumber("123");

        AffiliationDependent affiliationDependent = new AffiliationDependent();
        affiliationDependent.setFiledNumber("12345");

        when(affiliationDependentService.createAffiliation(any())).thenReturn(affiliationDependent);
        when(affiliateRepository.findByFiledNumber(any())).thenReturn(Optional.of(new Affiliate()));
        when(noveltyStatusRepository.findByStatus(Constant.NOVELTY_APPLY_STATUS))
                .thenReturn(Optional.of(new NoveltyStatus()));
        when(causalRepository.findById(Constant.CAUSAL_APPLY)).thenReturn(Optional.of(new NoveltyStatusCausal()));
        when(permanentNoveltyDao.createNovelty(any(PermanentNovelty.class))).thenReturn(novelty);

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("applyNovelty",
                PermanentNovelty.class, Affiliate.class);
        method.setAccessible(true);

        method.invoke(service, novelty, affiliate);

        verify(affiliationDependentService).createAffiliation(any());
    }

    @Test
    void applyNovelty_shouldCallAffiliateIndependentRisk4and5_whenContributorIsEmployerAndBondingTypeIs4()
            throws Exception {
        PermanentNovelty novelty = new PermanentNovelty();
        ContributorType contributorType = new ContributorType();
        contributorType.setCode(Constant.CODE_CONTRIBUTOR_TYPE_EMPLOYER);
        novelty.setContributorType(contributorType);
        TypeOfContributor contributantType = new TypeOfContributor();
        contributantType.setCode((short) 3); // Independent risk 4 and 5
        novelty.setContributantType(contributantType);
        novelty.setRegistryDate(LocalDateTime.now());
        com.gal.afiliaciones.domain.model.Department department = new com.gal.afiliaciones.domain.model.Department();
        department.setIdDepartment(1);
        novelty.setDepartment(department);
        com.gal.afiliaciones.domain.model.Municipality municipality = new com.gal.afiliaciones.domain.model.Municipality();
        municipality.setIdMunicipality(1L);
        novelty.setMunicipality(municipality);
        com.gal.afiliaciones.domain.model.EconomicActivity economicActivity = new com.gal.afiliaciones.domain.model.EconomicActivity();
        novelty.setEconomicActivity(economicActivity);
        novelty.setSalary(BigDecimal.TEN);
        novelty.setRisk("4");

        Affiliate affiliate = new Affiliate();
        affiliate.setDocumentType("CC");
        affiliate.setDocumentNumber("123");

        AffiliationDependent affiliationDependent = new AffiliationDependent();
        affiliationDependent.setId(1L);
        affiliationDependent.setFiledNumber("12345");

        when(affiliationDependentService.createAffiliationIndependentStep1(any())).thenReturn(affiliationDependent);
        when(affiliationDependentService.createAffiliationIndependentStep2(any())).thenReturn(affiliationDependent);
        when(riskFeeService.getFeeByRisk(any())).thenReturn(BigDecimal.ONE);
        when(affiliateRepository.findByFiledNumber(any())).thenReturn(Optional.of(new Affiliate()));
        when(noveltyStatusRepository.findByStatus(Constant.NOVELTY_APPLY_STATUS))
                .thenReturn(Optional.of(new NoveltyStatus()));
        when(causalRepository.findById(Constant.CAUSAL_APPLY)).thenReturn(Optional.of(new NoveltyStatusCausal()));
        when(permanentNoveltyDao.createNovelty(any(PermanentNovelty.class))).thenReturn(novelty);

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("applyNovelty",
                PermanentNovelty.class, Affiliate.class);
        method.setAccessible(true);

        method.invoke(service, novelty, affiliate);

        verify(affiliationDependentService).createAffiliationIndependentStep1(any());
        verify(affiliationDependentService).createAffiliationIndependentStep2(any());
    }

    @Test
    void applyNovelty_shouldReturnEmptyString_whenContributorTypeIsNeitherIndependentNorEmployer() throws Exception {
        PermanentNovelty novelty = new PermanentNovelty();
        ContributorType contributorType = new ContributorType();
        contributorType.setCode("UNKNOWN");
        novelty.setContributorType(contributorType);
        novelty.setRegistryDate(LocalDateTime.now());
        com.gal.afiliaciones.domain.model.Department department = new com.gal.afiliaciones.domain.model.Department();
        department.setIdDepartment(1);
        novelty.setDepartment(department);
        com.gal.afiliaciones.domain.model.Municipality municipality = new com.gal.afiliaciones.domain.model.Municipality();
        municipality.setIdMunicipality(1L);
        novelty.setMunicipality(municipality);

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("applyNovelty",
                PermanentNovelty.class, Affiliate.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, novelty, null);

        assertEquals("", result);
    }

    @Test
    void noveltyManage_shouldThrowException_whenStatusNotProcessable() {
        RequestApplyNoveltyDTO request = new RequestApplyNoveltyDTO();
        request.setId(1L);
        request.setApply(true);
        // prepare a novelty with status id != 4 and type RET
        PermanentNovelty novelty = new PermanentNovelty();
        NoveltyStatus status = new NoveltyStatus();
        status.setId(1L);
        novelty.setStatus(status);
        TypeOfUpdate typeOfUpdate = new TypeOfUpdate();
        typeOfUpdate.setCode(Constant.NOVELTY_RET);
        novelty.setNoveltyType(typeOfUpdate);
        when(permanentNoveltyDao.findById(anyLong())).thenReturn(novelty);

        assertThrows(NoveltyException.class,
                () -> service.applyOrNotApplyNovelty(request));
    }

    @Test
    void noveltyManage_shouldThrowException_whenMultipleAffiliations() {
        RequestApplyNoveltyDTO request = new RequestApplyNoveltyDTO();
        request.setId(1L);
        request.setApply(true);
        // novelty with status id = 4 and type RET
        PermanentNovelty novelty = new PermanentNovelty();
        NoveltyStatus status = new NoveltyStatus();
        status.setId(4L);
        novelty.setStatus(status);
        TypeOfUpdate typeOfUpdate = new TypeOfUpdate();
        typeOfUpdate.setCode(Constant.NOVELTY_RET);
        novelty.setNoveltyType(typeOfUpdate);
        novelty.setContributantIdentificationType("CC");
        novelty.setContributantIdentification("123");
        when(permanentNoveltyDao.findById(anyLong())).thenReturn(novelty);
        when(noveltyTypeRepository.findById(2L)).thenReturn(Optional.of(typeOfUpdate));
        when(noveltyStatusRepository.findByStatus("Aplicado")).thenReturn(Optional.of(new NoveltyStatus()));
        when(affiliateRepository.findAll(any(Example.class))).thenReturn(List.of(new Affiliate(), new Affiliate()));

        assertThrows(NoveltyException.class,
                () -> service.applyOrNotApplyNovelty(request));
    }

    @Test
    void noveltyManage_shouldThrowException_whenNoAffiliations() {
        RequestApplyNoveltyDTO request = new RequestApplyNoveltyDTO();
        request.setId(1L);
        request.setApply(true);
        PermanentNovelty novelty = new PermanentNovelty();
        NoveltyStatus status = new NoveltyStatus();
        status.setId(4L);
        novelty.setStatus(status);
        TypeOfUpdate typeOfUpdate = new TypeOfUpdate();
        typeOfUpdate.setCode(Constant.NOVELTY_RET);
        novelty.setNoveltyType(typeOfUpdate);
        novelty.setContributantIdentificationType("CC");
        novelty.setContributantIdentification("123");
        when(permanentNoveltyDao.findById(anyLong())).thenReturn(novelty);
        when(noveltyTypeRepository.findById(2L)).thenReturn(Optional.of(typeOfUpdate));
        when(noveltyStatusRepository.findByStatus("Aplicado")).thenReturn(Optional.of(new NoveltyStatus()));
        when(affiliateRepository.findAll(any(Example.class))).thenReturn(Collections.emptyList());

        assertThrows(NoveltyException.class,
                () -> service.applyOrNotApplyNovelty(request));
    }

    @Test
    void buildDependentWorkerDTO_shouldMapCorrectlyForDependent() throws Exception {
        // Arrange
        PermanentNovelty novelty = new PermanentNovelty();
        novelty.setContributantIdentificationType("CC");
        novelty.setContributantIdentification("12345");
        novelty.setContributantFirstName("JOHN");
        novelty.setContributantSecondName("FITZGERALD");
        novelty.setContributantSurname("KENNEDY");
        novelty.setContributantSecondSurname("ROOSEVELT");
        novelty.setHealthPromotingEntity(null);
        novelty.setDepartment(new com.gal.afiliaciones.domain.model.Department());
        novelty.getDepartment().setIdDepartment(5);
        novelty.setMunicipality(new com.gal.afiliaciones.domain.model.Municipality());
        novelty.getMunicipality().setIdMunicipality(5001L);
        novelty.setAddressContributor("Some Address");
        novelty.setPhoneContributor("1234567");
        novelty.setEmailContributor("test@test.com");
        novelty.setSalary(new BigDecimal("2000000"));

        com.gal.afiliaciones.domain.model.Health healthFosyga = new com.gal.afiliaciones.domain.model.Health();
        healthFosyga.setId(42L);
        when(epsRepository.findByCodeEPS(Constant.FOSYGA_CODE_EPS)).thenReturn(Optional.of(healthFosyga));

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("buildDependentWorkerDTO",
                PermanentNovelty.class, Long.class);
        method.setAccessible(true);

        // Act
        DependentWorkerDTO result = (DependentWorkerDTO) method.invoke(service, novelty, 1L);

        // Assert
        assertEquals("CC", result.getIdentificationDocumentType());
        assertEquals("12345", result.getIdentificationDocumentNumber());
        assertEquals("JOHN", result.getFirstName());
        assertEquals("FITZGERALD", result.getSecondName());
        assertEquals("KENNEDY", result.getSurname());
        assertEquals("ROOSEVELT", result.getSecondSurname());
        assertEquals(42L, result.getHealthPromotingEntity());
        assertEquals(new BigDecimal("2000000"), result.getSalary());
        assertEquals(Constant.OTHER_OCCUPATIONS_ID, result.getIdOccupation());
    }

    @Test
    void buildDependentWorkerDTO_shouldSetCorrectOccupationForStudent() throws Exception {
        // Arrange
        PermanentNovelty novelty = new PermanentNovelty();
        novelty.setContributantIdentificationType("CC");
        novelty.setContributantIdentification("12345");
        novelty.setContributantFirstName("JANE");
        novelty.setContributantSurname("DOE");
        novelty.setHealthPromotingEntity(new com.gal.afiliaciones.domain.model.Health());
        novelty.getHealthPromotingEntity().setId(1L);
        novelty.setDepartment(new com.gal.afiliaciones.domain.model.Department());
        novelty.getDepartment().setIdDepartment(5);
        novelty.setMunicipality(new com.gal.afiliaciones.domain.model.Municipality());
        novelty.getMunicipality().setIdMunicipality(5001L);
        novelty.setAddressContributor("Some Address");
        novelty.setPhoneContributor("1234567");
        novelty.setEmailContributor("test@test.com");
        novelty.setSalary(new BigDecimal("1000000"));

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("buildDependentWorkerDTO",
                PermanentNovelty.class, Long.class);
        method.setAccessible(true);

        // Act
        DependentWorkerDTO result = (DependentWorkerDTO) method.invoke(service, novelty, 2L);

        // Assert
        assertEquals(Constant.STUDENT_DECRE055_ID, result.getIdOccupation());
    }

    @Test
    void buildDependentWorkerDTO_shouldSetCorrectOccupationForApprentice() throws Exception {
        // Arrange
        PermanentNovelty novelty = new PermanentNovelty();
        novelty.setContributantIdentificationType("CC");
        novelty.setContributantIdentification("12345");
        novelty.setContributantFirstName("JANE");
        novelty.setContributantSurname("DOE");
        novelty.setHealthPromotingEntity(new com.gal.afiliaciones.domain.model.Health());
        novelty.getHealthPromotingEntity().setId(1L);
        novelty.setDepartment(new com.gal.afiliaciones.domain.model.Department());
        novelty.getDepartment().setIdDepartment(5);
        novelty.setMunicipality(new com.gal.afiliaciones.domain.model.Municipality());
        novelty.getMunicipality().setIdMunicipality(5001L);
        novelty.setAddressContributor("Some Address");
        novelty.setPhoneContributor("1234567");
        novelty.setEmailContributor("test@test.com");
        novelty.setSalary(new BigDecimal("1000000"));

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("buildDependentWorkerDTO",
                PermanentNovelty.class, Long.class);
        method.setAccessible(true);

        // Act
        DependentWorkerDTO result = (DependentWorkerDTO) method.invoke(service, novelty, 3L);

        // Assert
        assertEquals(Constant.APPRENTICE_SENA_ID, result.getIdOccupation());
    }

    @Test
    void affiliateDependent_shouldCreateAffiliationAndReturnFiledNumber() throws Exception {
        // Arrange
        PermanentNovelty novelty = new PermanentNovelty();
        novelty.setContributantIdentificationType("CC");
        novelty.setContributantIdentification("12345");
        novelty.setContributantFirstName("Test");
        novelty.setContributantSurname("User");
        novelty.setSalary(BigDecimal.valueOf(1000000));
        novelty.setRisk("1");
        novelty.setAddressContributor("Address");
        novelty.setPhoneContributor("1234567");
        novelty.setEmailContributor("test@test.com");

        com.gal.afiliaciones.domain.model.EconomicActivity economicActivity = new com.gal.afiliaciones.domain.model.EconomicActivity();
        economicActivity.setEconomicActivityCode("0111");
        novelty.setEconomicActivity(economicActivity);

        com.gal.afiliaciones.domain.model.Department department = new com.gal.afiliaciones.domain.model.Department();
        department.setIdDepartment(5);
        novelty.setDepartment(department);

        com.gal.afiliaciones.domain.model.Municipality municipality = new com.gal.afiliaciones.domain.model.Municipality();
        municipality.setIdMunicipality(1L);
        novelty.setMunicipality(municipality);

        com.gal.afiliaciones.domain.model.Health health = new com.gal.afiliaciones.domain.model.Health();
        health.setId(1L);
        novelty.setHealthPromotingEntity(health);

        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setDocumentType("NI");
        employerAffiliate.setDocumentNumber("98765");

        NoveltyDependentRequestDTO request = new NoveltyDependentRequestDTO(
                novelty, 1L, LocalDate.now(), 1L, 5L, 1L, "Address", employerAffiliate);

        AffiliationDependent createdAffiliation = new AffiliationDependent();
        createdAffiliation.setFiledNumber("AFD123");

        Affiliate newDependentAffiliate = new Affiliate();
        newDependentAffiliate.setIdAffiliate(99L);

        PermanentNovelty updatedNovelty = new PermanentNovelty();
        updatedNovelty.setFiledNumber("PN-AFD123");

        when(affiliationDependentService.createAffiliation(any(AffiliationDependentDTO.class)))
                .thenReturn(createdAffiliation);
        when(affiliateRepository.findByFiledNumber("AFD123")).thenReturn(Optional.of(newDependentAffiliate));
        when(noveltyStatusRepository.findByStatus(Constant.NOVELTY_APPLY_STATUS))
                .thenReturn(Optional.of(new NoveltyStatus()));
        when(causalRepository.findById(Constant.CAUSAL_APPLY)).thenReturn(Optional.of(new NoveltyStatusCausal()));
        when(permanentNoveltyDao.createNovelty(any(PermanentNovelty.class))).thenReturn(updatedNovelty);

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("affiliateDependent",
                NoveltyDependentRequestDTO.class);
        method.setAccessible(true);

        // Act
        String result = (String) method.invoke(service, request);

        // Assert
        assertEquals("PN-AFD123", result);
        verify(affiliationDependentService).createAffiliation(any(AffiliationDependentDTO.class));
        verify(permanentNoveltyDao).createNovelty(any(PermanentNovelty.class));
        assertEquals(99L, novelty.getIdAffiliate());
    }

    @Test
    void affiliateDependent_shouldSetPracticeEndDate_forStudentOrApprentice() throws Exception {
        // Arrange
        PermanentNovelty novelty = new PermanentNovelty();
        novelty.setContributantIdentificationType("CC");
        novelty.setContributantIdentification("12345");
        novelty.setContributantFirstName("Test");
        novelty.setContributantSurname("User");
        novelty.setSalary(BigDecimal.valueOf(1000000));
        novelty.setRisk("1");
        novelty.setAddressContributor("Address");
        novelty.setPhoneContributor("1234567");
        novelty.setEmailContributor("test@test.com");

        com.gal.afiliaciones.domain.model.EconomicActivity economicActivity = new com.gal.afiliaciones.domain.model.EconomicActivity();
        economicActivity.setEconomicActivityCode("0111");
        novelty.setEconomicActivity(economicActivity);

        com.gal.afiliaciones.domain.model.Department department = new com.gal.afiliaciones.domain.model.Department();
        department.setIdDepartment(5);
        novelty.setDepartment(department);

        com.gal.afiliaciones.domain.model.Municipality municipality = new com.gal.afiliaciones.domain.model.Municipality();
        municipality.setIdMunicipality(1L);
        novelty.setMunicipality(municipality);

        com.gal.afiliaciones.domain.model.Health health = new com.gal.afiliaciones.domain.model.Health();
        health.setId(1L);
        novelty.setHealthPromotingEntity(health);

        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setDocumentType("NI");
        employerAffiliate.setDocumentNumber("98765");

        LocalDate coverageDate = LocalDate.now();
        NoveltyDependentRequestDTO request = new NoveltyDependentRequestDTO(
                novelty, 2L, coverageDate, 1L, 5L, 1L, "Address", employerAffiliate); // Bonding type 2 = Student

        AffiliationDependent createdAffiliation = new AffiliationDependent();
        createdAffiliation.setFiledNumber("AFDS123");

        PermanentNovelty updatedNovelty = new PermanentNovelty();
        updatedNovelty.setFiledNumber("PN-AFDS123");

        when(affiliationDependentService.createAffiliation(any(AffiliationDependentDTO.class)))
                .thenAnswer(invocation -> {
                    AffiliationDependentDTO dto = invocation.getArgument(0);
                    assertEquals(coverageDate.plusYears(1), dto.getPracticeEndDate());
                    return createdAffiliation;
                });
        when(affiliateRepository.findByFiledNumber(anyString())).thenReturn(Optional.of(new Affiliate()));
        when(noveltyStatusRepository.findByStatus(anyString())).thenReturn(Optional.of(new NoveltyStatus()));
        when(causalRepository.findById(anyLong())).thenReturn(Optional.of(new NoveltyStatusCausal()));
        when(permanentNoveltyDao.createNovelty(any(PermanentNovelty.class))).thenReturn(updatedNovelty);

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("affiliateDependent",
                NoveltyDependentRequestDTO.class);
        method.setAccessible(true);

        // Act
        method.invoke(service, request);

        // Assert
        verify(affiliationDependentService).createAffiliation(any(AffiliationDependentDTO.class));
    }

    @Test
    void applyOrNotApplyNovelty_whenApplyIsTrue_withValidComment() {
        // Setup
        RequestApplyNoveltyDTO request = new RequestApplyNoveltyDTO();
        request.setId(1L);
        request.setApply(true);
        request.setComment("Valid comment");

        PermanentNovelty novelty = mock(PermanentNovelty.class);
        TypeOfUpdate typeOfUpdate = mock(TypeOfUpdate.class);
        when(typeOfUpdate.getCode()).thenReturn("OTHER");
        when(novelty.getNoveltyType()).thenReturn(typeOfUpdate);

        when(permanentNoveltyDao.findById(anyLong())).thenReturn(novelty);

        // Execute
        Boolean result = service.applyOrNotApplyNovelty(request);

        // Verify
        assertTrue(result);
    }

    @Test
    void applyOrNotApplyNovelty_whenApplyIsTrue_forTransitionalNovelty() {
        // Setup
        RequestApplyNoveltyDTO request = new RequestApplyNoveltyDTO();
        request.setId(1L);
        request.setApply(true);
        request.setComment("Valid comment");
        request.setIdOfficial(1L);

        PermanentNovelty novelty = mock(PermanentNovelty.class);
        TypeOfUpdate typeOfUpdate = mock(TypeOfUpdate.class);
        when(typeOfUpdate.getCode()).thenReturn("OTHER");
        when(typeOfUpdate.getGroup()).thenReturn(Constant.NOVELTY_TRANSITIONAL);
        when(novelty.getNoveltyType()).thenReturn(typeOfUpdate);

        NoveltyStatus status = mock(NoveltyStatus.class);
        when(noveltyStatusRepository.findByStatus(Constant.NOVELTY_APPLY_STATUS)).thenReturn(Optional.of(status));

        UserMain official = mock(UserMain.class);
        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(official));

        when(permanentNoveltyDao.findById(anyLong())).thenReturn(novelty);

        // Execute
        Boolean result = service.applyOrNotApplyNovelty(request);

        // Verify
        assertTrue(result);
        verify(novelty).setStatus(status);
        verify(novelty).setComment("Valid comment");
        verify(permanentNoveltyDao).createNovelty(novelty);
        verify(traceabilityRepository).save(any(Traceability.class));
    }

    @Test
    void applyOrNotApplyNovelty_whenApplyIsFalse_withCommentTooShort() {
        // Setup
        RequestApplyNoveltyDTO request = new RequestApplyNoveltyDTO();
        request.setId(1L);
        request.setApply(false);
        request.setComment("Short");

        when(permanentNoveltyDao.findById(anyLong())).thenReturn(mock(PermanentNovelty.class));

        // Execute & Verify
        assertThrows(NoveltyException.class, () -> service.applyOrNotApplyNovelty(request));
    }

    @Test
    void processIngNovelty_shouldProcessAsEmployer_andNotApply() throws Exception {
        PermanentNovelty novelty = new PermanentNovelty();
        ContributorType contributorType = new ContributorType();
        contributorType.setCode(Constant.CODE_CONTRIBUTOR_TYPE_EMPLOYER);
        novelty.setContributorType(contributorType);
        novelty.setContributorIdentification("123");
        novelty.setContributorIdentificationType(Constant.NI);

        TypeOfContributor typeOfContributor = new TypeOfContributor();
        typeOfContributor.setCode(Constant.CODE_CONTRIBUTANT_TYPE_DOMESTIC.shortValue());
        novelty.setContributantType(typeOfContributor);

        TypeOfUpdate noveltyType = new TypeOfUpdate();
        noveltyType.setCode("ING");
        noveltyType.setDescription("Ingreso");
        novelty.setNoveltyType(noveltyType);

        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));

        NoveltyStatus notApplyStatus = new NoveltyStatus();
        notApplyStatus.setStatus(Constant.NOVELTY_NOT_APPLY_STATUS);
        when(noveltyStatusRepository.findByStatus(Constant.NOVELTY_NOT_APPLY_STATUS))
                .thenReturn(Optional.of(notApplyStatus));

        NoveltyStatusCausal causal = new NoveltyStatusCausal();
        causal.setId(Constant.CAUSAL_CONTRIBUTANT_DOMESTIC);
        causal.setCausal("Causal description");
        when(causalRepository.findById(Constant.CAUSAL_CONTRIBUTANT_DOMESTIC)).thenReturn(Optional.of(causal));

        when(permanentNoveltyDao.createNovelty(any(PermanentNovelty.class))).thenReturn(novelty);

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("processIngNovelty",
                PermanentNovelty.class);
        method.setAccessible(true);

        method.invoke(service, novelty);

        assertEquals(Constant.NOVELTY_NOT_APPLY_STATUS, novelty.getStatus().getStatus());
        assertEquals(Constant.CAUSAL_CONTRIBUTANT_DOMESTIC, novelty.getCausal().getId());
    }

    @Test
    void applyOrNotApplyNovelty_shouldApplyIngNovelty_whenApplyIsTrue() {
        // Arrange
        RequestApplyNoveltyDTO request = new RequestApplyNoveltyDTO();
        request.setId(1L);
        request.setApply(true);
        request.setComment("This is a valid comment");
        request.setIdOfficial(1L);

        PermanentNovelty novelty = mock(PermanentNovelty.class);
        when(novelty.getId()).thenReturn(1L);
        when(novelty.getNoveltyType()).thenReturn(mock(TypeOfUpdate.class));
        when(novelty.getNoveltyType().getCode()).thenReturn(Constant.NOVELTY_ING);
        when(novelty.getNoveltyType().getGroup()).thenReturn("NON_TRANSITIONAL");
        when(novelty.getContributorType()).thenReturn(mock(ContributorType.class));
        when(novelty.getContributorType().getCode()).thenReturn(Constant.CODE_CONTRIBUTOR_TYPE_EMPLOYER);
        when(novelty.getContributorIdentification()).thenReturn("123456789");
        when(novelty.getRegistryDate()).thenReturn(LocalDateTime.now());

        UserMain official = mock(UserMain.class);
        Affiliate affiliate = mock(Affiliate.class);
        NoveltyStatus status = mock(NoveltyStatus.class);
        NoveltyStatusCausal causal = mock(NoveltyStatusCausal.class);

        when(permanentNoveltyDao.findById(1L)).thenReturn(novelty);
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.of(official));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(noveltyStatusRepository.findByStatus(Constant.NOVELTY_APPLY_STATUS)).thenReturn(Optional.of(status));
        when(causalRepository.findById(Constant.CAUSAL_APPLY)).thenReturn(Optional.of(causal));
        when(permanentNoveltyDao.createNovelty(any(PermanentNovelty.class))).thenReturn(novelty);

        // Act
        service.applyOrNotApplyNovelty(request);

        // Assert
        verify(novelty).setInitNoveltyDate(any(LocalDate.class));
    }

    @Test
    void applyOrNotApplyNovelty_shouldUseInitNoveltyDate_whenItIsNotNull() {
        // Arrange
        RequestApplyNoveltyDTO request = new RequestApplyNoveltyDTO();
        request.setId(1L);
        request.setApply(true);
        request.setComment("This is a valid comment");
        request.setIdOfficial(1L);

        LocalDate initDate = LocalDate.now().minusDays(5);
        PermanentNovelty novelty = mock(PermanentNovelty.class);
        when(novelty.getId()).thenReturn(1L);
        when(novelty.getNoveltyType()).thenReturn(mock(TypeOfUpdate.class));
        when(novelty.getNoveltyType().getCode()).thenReturn(Constant.NOVELTY_ING);
        when(novelty.getNoveltyType().getGroup()).thenReturn("NON_TRANSITIONAL");
        when(novelty.getContributorType()).thenReturn(mock(ContributorType.class));
        when(novelty.getContributorType().getCode()).thenReturn(Constant.CODE_CONTRIBUTOR_TYPE_INDEPENDENT);
        when(novelty.getInitNoveltyDate()).thenReturn(initDate);

        UserMain official = mock(UserMain.class);
        NoveltyStatus status = mock(NoveltyStatus.class);
        NoveltyStatusCausal causal = mock(NoveltyStatusCausal.class);

        when(permanentNoveltyDao.findById(1L)).thenReturn(novelty);
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.of(official));
        when(noveltyStatusRepository.findByStatus(Constant.NOVELTY_APPLY_STATUS)).thenReturn(Optional.of(status));
        when(causalRepository.findById(Constant.CAUSAL_APPLY)).thenReturn(Optional.of(causal));
        when(permanentNoveltyDao.createNovelty(any(PermanentNovelty.class))).thenReturn(novelty);

        // Act
        Boolean result = service.applyOrNotApplyNovelty(request);

        // Assert
        verify(novelty).setInitNoveltyDate(initDate);
    }

    @Test
    void applyOrNotApplyNovelty_shouldUseRegistryDate_whenInitNoveltyDateIsNull() {
        // Arrange
        RequestApplyNoveltyDTO request = new RequestApplyNoveltyDTO();
        request.setId(1L);
        request.setApply(true);
        request.setComment("This is a valid comment");
        request.setIdOfficial(1L);

        LocalDateTime registryDate = LocalDateTime.now();
        PermanentNovelty novelty = mock(PermanentNovelty.class);
        when(novelty.getId()).thenReturn(1L);
        when(novelty.getNoveltyType()).thenReturn(mock(TypeOfUpdate.class));
        when(novelty.getNoveltyType().getCode()).thenReturn(Constant.NOVELTY_ING);
        when(novelty.getNoveltyType().getGroup()).thenReturn("NON_TRANSITIONAL");
        when(novelty.getContributorType()).thenReturn(mock(ContributorType.class));
        when(novelty.getContributorType().getCode()).thenReturn(Constant.CODE_CONTRIBUTOR_TYPE_INDEPENDENT);
        when(novelty.getInitNoveltyDate()).thenReturn(null);
        when(novelty.getRegistryDate()).thenReturn(registryDate);

        UserMain official = mock(UserMain.class);
        NoveltyStatus status = mock(NoveltyStatus.class);
        NoveltyStatusCausal causal = mock(NoveltyStatusCausal.class);

        when(permanentNoveltyDao.findById(1L)).thenReturn(novelty);
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.of(official));
        when(noveltyStatusRepository.findByStatus(Constant.NOVELTY_APPLY_STATUS)).thenReturn(Optional.of(status));
        when(causalRepository.findById(Constant.CAUSAL_APPLY)).thenReturn(Optional.of(causal));
        when(permanentNoveltyDao.createNovelty(any(PermanentNovelty.class))).thenReturn(novelty);

        // Act
        service.applyOrNotApplyNovelty(request);

        // Assert
        verify(novelty).setInitNoveltyDate(registryDate.toLocalDate());
    }

    @Test
    void applyOrNotApplyNovelty_shouldFindAffiliate_whenContributorTypeIsEmployer() {
        // Arrange
        RequestApplyNoveltyDTO request = new RequestApplyNoveltyDTO();
        request.setId(1L);
        request.setApply(true);
        request.setComment("This is a valid comment");
        request.setIdOfficial(1L);

        PermanentNovelty novelty = mock(PermanentNovelty.class);
        when(novelty.getId()).thenReturn(1L);
        when(novelty.getNoveltyType()).thenReturn(mock(TypeOfUpdate.class));
        when(novelty.getNoveltyType().getCode()).thenReturn(Constant.NOVELTY_ING);
        when(novelty.getNoveltyType().getGroup()).thenReturn("NON_TRANSITIONAL");
        when(novelty.getContributorType()).thenReturn(mock(ContributorType.class));
        when(novelty.getContributorType().getCode()).thenReturn(Constant.CODE_CONTRIBUTOR_TYPE_EMPLOYER);
        when(novelty.getContributorIdentification()).thenReturn("123456789");
        when(novelty.getRegistryDate()).thenReturn(LocalDateTime.now());

        Affiliate affiliate = mock(Affiliate.class);
        UserMain official = mock(UserMain.class);
        NoveltyStatus status = mock(NoveltyStatus.class);
        NoveltyStatusCausal causal = mock(NoveltyStatusCausal.class);

        when(permanentNoveltyDao.findById(1L)).thenReturn(novelty);
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.of(official));
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(noveltyStatusRepository.findByStatus(Constant.NOVELTY_APPLY_STATUS)).thenReturn(Optional.of(status));
        when(causalRepository.findById(Constant.CAUSAL_APPLY)).thenReturn(Optional.of(causal));
        when(permanentNoveltyDao.createNovelty(any(PermanentNovelty.class))).thenReturn(novelty);

        // Act
        service.applyOrNotApplyNovelty(request);

        // Assert
        verify(affiliateRepository).findOne(any(Specification.class));
    }

    @Test
    void applyOrNotApplyNovelty_shouldNotFindAffiliate_whenContributorTypeIsNotEmployer() {
        // Arrange
        RequestApplyNoveltyDTO request = new RequestApplyNoveltyDTO();
        request.setId(1L);
        request.setApply(true);
        request.setComment("This is a valid comment");
        request.setIdOfficial(1L);

        PermanentNovelty novelty = mock(PermanentNovelty.class);
        when(novelty.getId()).thenReturn(1L);
        when(novelty.getNoveltyType()).thenReturn(mock(TypeOfUpdate.class));
        when(novelty.getNoveltyType().getCode()).thenReturn(Constant.NOVELTY_ING);
        when(novelty.getNoveltyType().getGroup()).thenReturn("NON_TRANSITIONAL");
        when(novelty.getContributorType()).thenReturn(mock(ContributorType.class));
        when(novelty.getContributorType().getCode()).thenReturn(Constant.CODE_CONTRIBUTOR_TYPE_INDEPENDENT);
        when(novelty.getRegistryDate()).thenReturn(LocalDateTime.now());

        UserMain official = mock(UserMain.class);
        NoveltyStatus status = mock(NoveltyStatus.class);
        NoveltyStatusCausal causal = mock(NoveltyStatusCausal.class);

        when(permanentNoveltyDao.findById(1L)).thenReturn(novelty);
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.of(official));
        when(noveltyStatusRepository.findByStatus(Constant.NOVELTY_APPLY_STATUS)).thenReturn(Optional.of(status));
        when(causalRepository.findById(Constant.CAUSAL_APPLY)).thenReturn(Optional.of(causal));
        when(permanentNoveltyDao.createNovelty(any(PermanentNovelty.class))).thenReturn(novelty);

        // Act
        service.applyOrNotApplyNovelty(request);

        // Assert
        verify(affiliateRepository, times(0)).findOne(any(Specification.class));
    }

    @Test
    void getSubTypeAffiliation_shouldReturnTaxiDriver_forIndependentTaxiDriver() throws Exception {
        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("getSubTypeAffiliation",
                Short.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, Constant.CODE_CONTRIBUTANT_TYPE_INDEPENDENT.shortValue(),
                Constant.CODE_CONTRIBUTANT_SUBTYPE_TAXI_DRIVE);

        assertEquals(Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER, result);
    }

    @Test
    void getSubTypeAffiliation_shouldReturnTaxiDriver_forIndependentTaxiDriverWithoutPension() throws Exception {
        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("getSubTypeAffiliation",
                Short.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, Constant.CODE_CONTRIBUTANT_TYPE_INDEPENDENT.shortValue(),
                Constant.CODE_CONTRIBUTANT_SUBTYPE_TAXI_DRIVE_WITHOUT_PENSION);

        assertEquals(Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER, result);
    }

    @Test
    void getSubTypeAffiliation_shouldReturnVolunteer_forVolunteerType() throws Exception {
        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("getSubTypeAffiliation",
                Short.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, Constant.CODE_CONTRIBUTANT_TYPE_VOLUNTEER.shortValue(), "any");

        assertEquals(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER, result);
    }

    @Test
    void getSubTypeAffiliation_shouldReturnCouncillor_forCouncillorType() throws Exception {
        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("getSubTypeAffiliation",
                Short.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, Constant.CODE_CONTRIBUTANT_TYPE_COUNCILLOR.shortValue(), "any");

        assertEquals(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_COUNCILLOR, result);
    }

    @Test
    void getSubTypeAffiliation_shouldReturnCouncillor_forEdilType() throws Exception {
        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("getSubTypeAffiliation",
                Short.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, Constant.CODE_CONTRIBUTANT_TYPE_EDIL.shortValue(), "any");

        assertEquals(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_COUNCILLOR, result);
    }

    @Test
    void getSubTypeAffiliation_shouldReturnProvisionServices_forDefaultCase() throws Exception {
        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("getSubTypeAffiliation",
                Short.class, String.class);
        method.setAccessible(true);

        String result = (String) method.invoke(service, (short) 99, "any");

        assertEquals(Constant.SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES, result);
    }

    @Test
    void name_shouldReturnFullName_whenFirstAndSecondNamesPresent() {
        PermanentNovelty novelty = new PermanentNovelty();
        novelty.setContributantFirstName("John");
        novelty.setContributantSecondName("Doe");
        novelty.setNameOrCompanyName("Acme Corp"); // This should be ignored
        String result = (String) ReflectionTestUtils.invokeMethod(service, "name", novelty);
        assertEquals("John Doe", result);
    }

    @Test
    void name_shouldReturnNameOrCompanyName_whenFirstAndSecondNamesNotProvided() {
        PermanentNovelty novelty = new PermanentNovelty();
        novelty.setContributantFirstName("");
        novelty.setContributantSecondName("");
        novelty.setNameOrCompanyName("Acme Corp");
        String result = (String) ReflectionTestUtils.invokeMethod(service, "name", novelty);
        assertEquals("Acme Corp", result);
    }

    @Test
    void name_shouldReturnNameOrCompanyName_whenOnlyOneNameMissing() {
        PermanentNovelty novelty = new PermanentNovelty();
        novelty.setContributantFirstName("John");
        novelty.setContributantSecondName(""); // Second name missing
        novelty.setNameOrCompanyName("Acme Corp");
        String result = (String) ReflectionTestUtils.invokeMethod(service, "name", novelty);
        assertEquals("Acme Corp", result);
    }

    @Test
    void name_shouldReturnEmpty_whenNoNamesProvided() {
        PermanentNovelty novelty = new PermanentNovelty();
        novelty.setContributantFirstName(null);
        novelty.setContributantSecondName(null);
        novelty.setNameOrCompanyName("");
        String result = (String) ReflectionTestUtils.invokeMethod(service, "name", novelty);
        assertEquals("", result);
    }

    @Test
    void noveltyRisk4and5Dependent_shouldReturnTrue_whenRiskIs4AndMatchingIndependentAffiliateExists()
            throws Exception {
        PermanentNovelty novelty = new PermanentNovelty();
        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setClassRisk("4");
        novelty.setEconomicActivity(economicActivity);
        novelty.setContributantIdentificationType("CC");
        novelty.setContributantIdentification("12345");

        Affiliate matchingAffiliate = new Affiliate();
        matchingAffiliate.setDocumentType("CC");
        matchingAffiliate.setDocumentNumber("12345");
        matchingAffiliate.setAffiliationSubType(Constant.BONDING_TYPE_INDEPENDENT);
        matchingAffiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);

        List<Affiliate> dependentList = List.of(matchingAffiliate);

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod(
                "noveltyRisk4and5Dependent",
                PermanentNovelty.class, List.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(service, novelty, dependentList);

        assertTrue(result);
    }

    @Test
    void noveltyRisk4and5Dependent_shouldReturnTrue_whenRiskIs5AndMatchingIndependentAffiliateExists()
            throws Exception {
        PermanentNovelty novelty = new PermanentNovelty();
        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setClassRisk("5");
        novelty.setEconomicActivity(economicActivity);
        novelty.setContributantIdentificationType("CC");
        novelty.setContributantIdentification("12345");

        Affiliate matchingAffiliate = new Affiliate();
        matchingAffiliate.setDocumentType("CC");
        matchingAffiliate.setDocumentNumber("12345");
        matchingAffiliate.setAffiliationSubType(Constant.BONDING_TYPE_INDEPENDENT);
        matchingAffiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);

        List<Affiliate> dependentList = List.of(matchingAffiliate);

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod(
                "noveltyRisk4and5Dependent",
                PermanentNovelty.class, List.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(service, novelty, dependentList);

        assertTrue(result);
    }

    @Test
    void noveltyRisk4and5Dependent_shouldReturnFalse_whenRiskIsLessThan4() throws Exception {
        PermanentNovelty novelty = new PermanentNovelty();
        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setClassRisk("3");
        novelty.setEconomicActivity(economicActivity);

        List<Affiliate> dependentList = Collections.emptyList();

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod(
                "noveltyRisk4and5Dependent",
                PermanentNovelty.class, List.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(service, novelty, dependentList);

        assertFalse(result);
    }

    @Test
    void noveltyRisk4and5Dependent_shouldReturnFalse_whenNoMatchingAffiliateExists() throws Exception {
        PermanentNovelty novelty = new PermanentNovelty();
        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setClassRisk("4");
        novelty.setEconomicActivity(economicActivity);
        novelty.setContributantIdentificationType("CC");
        novelty.setContributantIdentification("12345");

        Affiliate nonMatchingAffiliate = new Affiliate();
        nonMatchingAffiliate.setDocumentType("CC");
        nonMatchingAffiliate.setDocumentNumber("67890");
        nonMatchingAffiliate.setAffiliationSubType(Constant.BONDING_TYPE_INDEPENDENT);
        nonMatchingAffiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);

        List<Affiliate> dependentList = List.of(nonMatchingAffiliate);

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod(
                "noveltyRisk4and5Dependent",
                PermanentNovelty.class, List.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(service, novelty, dependentList);

        assertFalse(result);
    }

    @Test
    void noveltyRisk4and5Dependent_shouldReturnFalse_whenMatchingAffiliateIsNotIndependent() throws Exception {
        PermanentNovelty novelty = new PermanentNovelty();
        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setClassRisk("4");
        novelty.setEconomicActivity(economicActivity);
        novelty.setContributantIdentificationType("CC");
        novelty.setContributantIdentification("12345");

        Affiliate matchingAffiliate = new Affiliate();
        matchingAffiliate.setDocumentType("CC");
        matchingAffiliate.setDocumentNumber("12345");
        matchingAffiliate.setAffiliationSubType("Dependiente"); // Not independent
        matchingAffiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);

        List<Affiliate> dependentList = List.of(matchingAffiliate);

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod(
                "noveltyRisk4and5Dependent",
                PermanentNovelty.class, List.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(service, novelty, dependentList);

        assertFalse(result);
    }

    @Test
    void noveltyRisk4and5Dependent_shouldReturnFalse_whenDependentListIsEmpty() throws Exception {
        PermanentNovelty novelty = new PermanentNovelty();
        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setClassRisk("5");
        novelty.setEconomicActivity(economicActivity);

        List<Affiliate> dependentList = Collections.emptyList();

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod(
                "noveltyRisk4and5Dependent",
                PermanentNovelty.class, List.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(service, novelty, dependentList);

        assertFalse(result);
    }

    @Test
    void processNovelty_whenNoveltyRetirementIncomeIsTrue_shouldCallPilaRetirementEventManagement() {
        // Arrange
        PermanentNovelty novelty = mock(PermanentNovelty.class);

        // Act
        ReflectionTestUtils.invokeMethod(service, "processNovelty", novelty, true);

        // Assert
        verify(pilaRetirementEventManagementService).pilaRetirementEventManagement(novelty, true);
        verify(permanentNoveltyDao, never()).createNovelty(any());
    }

    @Test
    void processNovelty_whenStatusIsPendingAndNoveltyTypeIsRet_shouldCallPilaRetirementEventManagement() {
        // Arrange
        PermanentNovelty novelty = mock(PermanentNovelty.class);
        NoveltyStatus status = mock(NoveltyStatus.class);
        TypeOfUpdate typeOfUpdate = mock(TypeOfUpdate.class);

        when(novelty.getStatus()).thenReturn(status);
        when(status.getStatus()).thenReturn(Constant.NOVELTY_PENDING_STATUS);
        when(novelty.getNoveltyType()).thenReturn(typeOfUpdate);
        when(typeOfUpdate.getCode()).thenReturn(Constant.NOVELTY_RET);

        // Act
        ReflectionTestUtils.invokeMethod(service, "processNovelty", novelty, false);

        // Assert
        verify(pilaRetirementEventManagementService).pilaRetirementEventManagement(novelty, false);
        verify(permanentNoveltyDao, never()).createNovelty(any());
    }

    @Test
    void processNovelty_whenStatusIsNotPending_shouldDoNothing() {
        // Arrange
        PermanentNovelty novelty = mock(PermanentNovelty.class);
        NoveltyStatus status = mock(NoveltyStatus.class);

        when(novelty.getStatus()).thenReturn(status);
        when(status.getStatus()).thenReturn(Constant.NOVELTY_APPLY_STATUS);

        // Act
        ReflectionTestUtils.invokeMethod(service, "processNovelty", novelty, false);

        // Assert
        verify(pilaRetirementEventManagementService, never()).pilaRetirementEventManagement(any(), anyBoolean());
        verify(permanentNoveltyDao, never()).createNovelty(any());
    }

    @Test
    void affiliateProvisionServicePILA_shouldCreateAffiliationAndApplyNovelty() throws Exception {
        // Given
        PermanentNovelty novelty = new PermanentNovelty();
        novelty.setContributantIdentificationType("CC");
        novelty.setContributantIdentification("12345");
        novelty.setContributantFirstName("John");
        novelty.setContributantSecondName("Fitzgerald");
        novelty.setContributantSurname("Kennedy");
        novelty.setContributantSecondSurname("Doe");
        novelty.setHealthPromotingEntity(null);
        Department department = new Department();
        department.setIdDepartment(1);
        novelty.setDepartment(department);
        Municipality municipality = new Municipality();
        municipality.setIdMunicipality(1L);
        novelty.setMunicipality(municipality);
        novelty.setAddressContributor("123 Main St");
        novelty.setPhoneContributor("555-1234");
        novelty.setEmailContributor("test@test.com");
        novelty.setRegistryDate(LocalDateTime.now());
        novelty.setSalary(new BigDecimal("2000000"));
        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setEconomicActivityCode("0111");
        novelty.setEconomicActivity(economicActivity);
        novelty.setRisk("1");
        ContributorType contributorType = new ContributorType();
        contributorType.setCode("02");
        novelty.setContributorType(contributorType);
        TypeOfContributor typeOfContributor = new TypeOfContributor();
        typeOfContributor.setCode((short) 59);
        novelty.setContributantType(typeOfContributor);
        SubContributorType subContributorType = new SubContributorType();
        subContributorType.setCode("0");
        novelty.setContributantSubtype(subContributorType);

        Occupation occupation = new Occupation();
        occupation.setNameOccupation("Other");
        when(occupationRepository.findById(Constant.OTHER_OCCUPATIONS_ID)).thenReturn(Optional.of(occupation));

        when(riskFeeService.getFeeByRisk(anyString())).thenReturn(BigDecimal.valueOf(0.00522));

        Long mockAffiliateId = 123L;
        when(independentPilaService.createAffiliationProvisionServicePila(any(NoveltyIndependentRequestDTO.class)))
                .thenReturn(mockAffiliateId);

        NoveltyStatus applyStatus = new NoveltyStatus();
        applyStatus.setStatus(Constant.NOVELTY_APPLY_STATUS);
        when(noveltyStatusRepository.findByStatus(Constant.NOVELTY_APPLY_STATUS)).thenReturn(Optional.of(applyStatus));

        NoveltyStatusCausal applyCausal = new NoveltyStatusCausal();
        applyCausal.setId(Constant.CAUSAL_APPLY);
        when(causalRepository.findById(Constant.CAUSAL_APPLY)).thenReturn(Optional.of(applyCausal));

        when(permanentNoveltyDao.createNovelty(any(PermanentNovelty.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod(
                "affiliateProvisionServicePILA",
                PermanentNovelty.class);
        method.setAccessible(true);
        String filedNumber = (String) method.invoke(service, novelty);

        // Then
        verify(independentPilaService, times(1))
                .createAffiliationProvisionServicePila(any(NoveltyIndependentRequestDTO.class));
        assertEquals(mockAffiliateId, novelty.getIdAffiliate());
        assertEquals(Constant.NOVELTY_APPLY_STATUS, novelty.getStatus().getStatus());
        assertEquals(Constant.CAUSAL_APPLY, novelty.getCausal().getId());
        verify(permanentNoveltyDao, times(1)).createNovelty(novelty);
    }

    @Test
    void affiliateVolunteerPILA_shouldCreateAffiliationAndReturnFiledNumber() throws Exception {
        // Given
        PermanentNovelty novelty = new PermanentNovelty();
        novelty.setContributantIdentificationType("CC");
        novelty.setContributantIdentification("12345");
        novelty.setContributantFirstName("John");
        novelty.setContributantSurname("Doe");
        novelty.setHealthPromotingEntity(null);
        Department department = new Department();
        department.setIdDepartment(5);
        novelty.setDepartment(department);
        Municipality municipality = new Municipality();
        municipality.setIdMunicipality(1L);
        novelty.setMunicipality(municipality);
        novelty.setAddressContributor("Street 123");
        novelty.setPhoneContributor("3001234567");
        novelty.setEmailContributor("test@test.com");
        novelty.setRegistryDate(LocalDateTime.now());
        novelty.setSalary(new BigDecimal("1500000"));
        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setEconomicActivityCode("0111");
        novelty.setEconomicActivity(economicActivity);
        novelty.setRisk("1");
        ContributorType contributorType = new ContributorType();
        contributorType.setCode("IND");
        novelty.setContributorType(contributorType);
        TypeOfContributor typeOfContributor = new TypeOfContributor();
        typeOfContributor.setCode((short) 42);
        novelty.setContributantType(typeOfContributor);
        SubContributorType subContributorType = new SubContributorType();
        subContributorType.setCode("0");
        novelty.setContributantSubtype(subContributorType);

        Occupation occupation = new Occupation();
        occupation.setNameOccupation("Other");

        PermanentNovelty savedNovelty = new PermanentNovelty();
        savedNovelty.setFiledNumber("FN-VOLUNTEER-001");

        when(occupationRepository.findById(Constant.OTHER_OCCUPATIONS_ID)).thenReturn(Optional.of(occupation));
        when(riskFeeService.getFeeByRisk(anyString())).thenReturn(BigDecimal.valueOf(0.00522));
        when(independentPilaService.createAffiliationVolunteerPila(any(NoveltyIndependentRequestDTO.class)))
                .thenReturn(1L);
        when(noveltyStatusRepository.findByStatus(Constant.NOVELTY_APPLY_STATUS))
                .thenReturn(Optional.of(new NoveltyStatus()));
        when(causalRepository.findById(Constant.CAUSAL_APPLY)).thenReturn(Optional.of(new NoveltyStatusCausal()));
        when(permanentNoveltyDao.createNovelty(any(PermanentNovelty.class))).thenReturn(savedNovelty);

        java.lang.reflect.Method method = PermanentNoveltyServiceImpl.class.getDeclaredMethod("affiliateVolunteerPILA",
                PermanentNovelty.class);
        method.setAccessible(true);

        // When
        String filedNumber = (String) method.invoke(service, novelty);

        // Then
        assertEquals("FN-VOLUNTEER-001", filedNumber);
        verify(independentPilaService, times(1))
                .createAffiliationVolunteerPila(any(NoveltyIndependentRequestDTO.class));
        verify(permanentNoveltyDao, times(1)).createNovelty(any(PermanentNovelty.class));
        assertEquals(1L, novelty.getIdAffiliate());
    }

}
