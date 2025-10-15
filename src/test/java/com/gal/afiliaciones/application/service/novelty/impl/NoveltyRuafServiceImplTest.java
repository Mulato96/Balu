package com.gal.afiliaciones.application.service.novelty.impl;


import com.gal.afiliaciones.application.service.IUserRegisterService;
import com.gal.afiliaciones.application.service.ruaf.RuafFilesHelper;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.domain.model.noveltyruaf.NoveltyRuaf;
import com.gal.afiliaciones.domain.model.noveltyruaf.RuafFiles;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.noveltyruaf.NoveltyRuafRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.retirement.RetirementRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ruaf.RuafFilesRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ruaf.UsersInArrears;
import com.gal.afiliaciones.infrastructure.dto.noveltyruaf.NoveltyRuafDTO;
import com.gal.afiliaciones.infrastructure.dto.ruaf.RuafTypes;
import com.gal.afiliaciones.infrastructure.utils.ByteArrayToMultipartFile;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NoveltyRuafServiceImpl Tests")
class NoveltyRuafServiceImplTest {

    @Mock private RuafFilesRepository ruafFilesRepository;
    @Mock private NoveltyRuafRepository noveltyRuafRepository;
    @Mock private RetirementRepository retirementRepository;
    @Mock private AffiliateRepository affiliateRepository;
    @Mock private AffiliationDependentRepository affiliationDependentRepository;
    @Mock private IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;
    @Mock private ArlInformationDao arlInformationDao;
    @Mock private AffiliateMercantileRepository mercantileRepository;
    @Mock private IUserRegisterService iUserRegisterService;
    @Mock private RuafFilesHelper ruafFilesHelper;
    @Mock private CollectProperties properties;

    @InjectMocks
    private NoveltyRuafServiceImpl noveltyRuafService;

    private NoveltyRuafDTO noveltyRuafDTO;
    private NoveltyRuaf noveltyRuaf;
    private Retirement retirement;
    private Affiliate affiliate;
    private AffiliationDependent affiliationDependent;
    private Affiliation affiliation;
    private AffiliateMercantile affiliateMercantile;
    private ArlInformation arlInformation;

    @BeforeEach
    void setUp() {
        noveltyRuafDTO = new NoveltyRuafDTO();
        noveltyRuafDTO.setIdentificationType("CC");
        noveltyRuafDTO.setIdentificationNumber("12345678");
        noveltyRuafDTO.setNoveltyCode("R01");
        noveltyRuafDTO.setIdentificationTypeContributor("NIT");
        noveltyRuafDTO.setIdentificationNumberContributor("900123456");

        noveltyRuaf = new NoveltyRuaf();
        noveltyRuaf.setId(1L);
        noveltyRuaf.setIdentificationType("CC");
        noveltyRuaf.setIdentificationNumber("12345678");
        noveltyRuaf.setNoveltyCode("R01");
        noveltyRuaf.setArlCode("250");
        noveltyRuaf.setFirstName("Juan");
        noveltyRuaf.setSecondName("Carlos");
        noveltyRuaf.setSurname("Perez");
        noveltyRuaf.setSecondSurname("Lopez");
        noveltyRuaf.setIdentificationTypeContributor("NIT");
        noveltyRuaf.setIdentificationNumberContributor("900123456");
        noveltyRuaf.setDvContributor(7);
        noveltyRuaf.setDisassociationDateWithContributor(LocalDate.now());
        noveltyRuaf.setNoveltyDate(LocalDate.now());
        noveltyRuaf.setRetirmentCausal(1);

        retirement = new Retirement();
        retirement.setId(1L);
        retirement.setIdAffiliate(1L);
        retirement.setIdRetirementReason(2L);
        retirement.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);

        affiliate = new Affiliate();
        affiliate.setIdAffiliate(1L);
        affiliate.setFiledNumber("123456");
        affiliate.setNitCompany("900123456");
        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);

        affiliationDependent = new AffiliationDependent();
        affiliationDependent.setIdentificationDocumentType("CC");
        affiliationDependent.setIdentificationDocumentNumber("12345678");
        affiliationDependent.setFirstName("Juan");
        affiliationDependent.setSecondName("Carlos");
        affiliationDependent.setSurname("Perez");
        affiliationDependent.setSecondSurname("Lopez");

        affiliation = new Affiliation();
        affiliation.setIdentificationDocumentType("CC");
        affiliation.setIdentificationDocumentNumber("12345678");
        affiliation.setFirstName("Juan");
        affiliation.setSecondName("Carlos");
        affiliation.setSurname("Perez");
        affiliation.setSecondSurname("Lopez");

        affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setTypeDocumentIdentification("NIT");
        affiliateMercantile.setDigitVerificationDV(7);

        arlInformation = new ArlInformation();
        arlInformation.setCode("250");
        arlInformation.setNit("900123456789");
    }

    @Test
    @DisplayName("Should create new novelty when no duplicate exists")
    void createNovelty_ShouldCreateNewNoveltyWhenNoDuplicateExists() {
        when(noveltyRuafRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(noveltyRuafRepository.save(any(NoveltyRuaf.class))).thenReturn(noveltyRuaf);

        NoveltyRuaf result = noveltyRuafService.createNovelty(noveltyRuafDTO);

        assertNotNull(result);
        assertEquals(noveltyRuaf.getId(), result.getId());
        verify(noveltyRuafRepository).findOne(any(Specification.class));
        verify(noveltyRuafRepository).save(any(NoveltyRuaf.class));
    }

    @Test
    @DisplayName("Should return existing novelty when duplicate exists")
    void createNovelty_ShouldReturnExistingNoveltyWhenDuplicateExists() {
        when(noveltyRuafRepository.findOne(any(Specification.class))).thenReturn(Optional.of(noveltyRuaf));

        NoveltyRuaf result = noveltyRuafService.createNovelty(noveltyRuafDTO);

        assertNotNull(result);
        assertEquals(noveltyRuaf.getId(), result.getId());
        verify(noveltyRuafRepository).findOne(any(Specification.class));
        verify(noveltyRuafRepository, never()).save(any(NoveltyRuaf.class));
    }


    @Test
    @DisplayName("Should execute worker retirement successfully for dependent affiliation")
    void executeWorkerRetirement_ShouldExecuteSuccessfullyForDependentAffiliation() {
        List<Retirement> retirementList = List.of(retirement);
        List<Affiliate> affiliateList = List.of(affiliate);

        when(retirementRepository.findWorkerRetirement()).thenReturn(retirementList);
        when(affiliateRepository.findById(retirement.getIdAffiliate())).thenReturn(Optional.of(affiliate));
        when(affiliationDependentRepository.findByFiledNumber(affiliate.getFiledNumber()))
                .thenReturn(Optional.of(affiliationDependent));
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(affiliateList);
        when(affiliationRepository.findByFiledNumber(affiliate.getFiledNumber())).thenReturn(Optional.of(affiliation));
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(noveltyRuafRepository.findByIdAffiliate(anyLong())).thenReturn(null);
        when(noveltyRuafRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(noveltyRuafRepository.save(any(NoveltyRuaf.class))).thenReturn(noveltyRuaf);

        Boolean result = noveltyRuafService.executeWorkerRetirement();

        assertTrue(result);
        verify(retirementRepository).findWorkerRetirement();
        verify(affiliateRepository).findById(retirement.getIdAffiliate());
        verify(affiliationDependentRepository).findByFiledNumber(affiliate.getFiledNumber());
        verify(noveltyRuafRepository).save(any(NoveltyRuaf.class));
    }

    @Test
    @DisplayName("Should execute worker retirement successfully for independent affiliation")
    void executeWorkerRetirement_ShouldExecuteSuccessfullyForIndependentAffiliation() {
        retirement.setAffiliationType("INDEPENDENT");
        List<Retirement> retirementList = List.of(retirement);
        List<Affiliate> affiliateList = List.of(affiliate);

        when(retirementRepository.findWorkerRetirement()).thenReturn(retirementList);
        when(affiliateRepository.findById(retirement.getIdAffiliate())).thenReturn(Optional.of(affiliate));
        when(affiliationRepository.findByFiledNumber(affiliate.getFiledNumber())).thenReturn(Optional.of(affiliation));
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(affiliateList);
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(noveltyRuafRepository.findByIdAffiliate(anyLong())).thenReturn(null);
        when(noveltyRuafRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(noveltyRuafRepository.save(any(NoveltyRuaf.class))).thenReturn(noveltyRuaf);

        Boolean result = noveltyRuafService.executeWorkerRetirement();

        assertTrue(result);
        verify(retirementRepository).findWorkerRetirement();
        verify(affiliateRepository).findById(retirement.getIdAffiliate());
        verify(affiliationRepository, atLeastOnce()).findByFiledNumber(affiliate.getFiledNumber());
        verify(noveltyRuafRepository).save(any(NoveltyRuaf.class));
    }

    @Test
    @DisplayName("Should return true when no retirements to process")
    void executeWorkerRetirement_ShouldReturnTrueWhenNoRetirementsToProcess() {
        when(retirementRepository.findWorkerRetirement()).thenReturn(Collections.emptyList());

        Boolean result = noveltyRuafService.executeWorkerRetirement();

        assertTrue(result);
        verify(retirementRepository).findWorkerRetirement();
        verify(affiliateRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Should throw exception when affiliate not found")
    void executeWorkerRetirement_ShouldThrowExceptionWhenAffiliateNotFound() {
        List<Retirement> retirementList = List.of(retirement);
        when(retirementRepository.findWorkerRetirement()).thenReturn(retirementList);
        when(affiliateRepository.findById(retirement.getIdAffiliate())).thenReturn(Optional.empty());

        assertThrows(AffiliateNotFound.class, () -> noveltyRuafService.executeWorkerRetirement());

        verify(retirementRepository).findWorkerRetirement();
        verify(affiliateRepository).findById(retirement.getIdAffiliate());
    }

    @Test
    @DisplayName("Should throw exception when dependent affiliation not found")
    void executeWorkerRetirement_ShouldThrowExceptionWhenDependentAffiliationNotFound() {
        List<Retirement> retirementList = List.of(retirement);
        when(retirementRepository.findWorkerRetirement()).thenReturn(retirementList);
        when(affiliateRepository.findById(retirement.getIdAffiliate())).thenReturn(Optional.of(affiliate));
        when(affiliationDependentRepository.findByFiledNumber(affiliate.getFiledNumber())).thenReturn(Optional.empty());

        assertThrows(AffiliationNotFoundError.class, () -> noveltyRuafService.executeWorkerRetirement());

        verify(affiliationDependentRepository).findByFiledNumber(affiliate.getFiledNumber());
    }

    @Test
    @DisplayName("Should throw exception when independent affiliation not found")
    void executeWorkerRetirement_ShouldThrowExceptionWhenIndependentAffiliationNotFound() {
        retirement.setAffiliationType("INDEPENDENT");
        List<Retirement> retirementList = List.of(retirement);
        when(retirementRepository.findWorkerRetirement()).thenReturn(retirementList);
        when(affiliateRepository.findById(retirement.getIdAffiliate())).thenReturn(Optional.of(affiliate));
        when(affiliationRepository.findByFiledNumber(affiliate.getFiledNumber())).thenReturn(Optional.empty());

        assertThrows(AffiliationNotFoundError.class, () -> noveltyRuafService.executeWorkerRetirement());

        verify(affiliationRepository).findByFiledNumber(affiliate.getFiledNumber());
    }

    @Test
    @DisplayName("Should not create novelty when it already exists for affiliate")
    void executeWorkerRetirement_ShouldNotCreateNoveltyWhenItAlreadyExists() {
        List<Retirement> retirementList = List.of(retirement);
        when(retirementRepository.findWorkerRetirement()).thenReturn(retirementList);
        when(affiliateRepository.findById(retirement.getIdAffiliate())).thenReturn(Optional.of(affiliate));
        when(affiliationDependentRepository.findByFiledNumber(affiliate.getFiledNumber()))
                .thenReturn(Optional.of(affiliationDependent));
        when(noveltyRuafRepository.findByIdAffiliate(anyLong())).thenReturn(noveltyRuaf);

        Boolean result = noveltyRuafService.executeWorkerRetirement();

        assertTrue(result);
        verify(noveltyRuafRepository).findByIdAffiliate(anyLong());
        verify(noveltyRuafRepository, never()).save(any(NoveltyRuaf.class));
    }

    @Test
    @DisplayName("Should throw exception when employer affiliate not found")
    void executeWorkerRetirement_ShouldThrowExceptionWhenEmployerAffiliateNotFound() {
        List<Retirement> retirementList = List.of(retirement);
        when(retirementRepository.findWorkerRetirement()).thenReturn(retirementList);
        when(affiliateRepository.findById(retirement.getIdAffiliate())).thenReturn(Optional.of(affiliate));
        when(affiliationDependentRepository.findByFiledNumber(affiliate.getFiledNumber()))
                .thenReturn(Optional.of(affiliationDependent));
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(noveltyRuafRepository.findByIdAffiliate(anyLong())).thenReturn(null);
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        assertThrows(AffiliateNotFound.class, () -> noveltyRuafService.executeWorkerRetirement());
    }

    @Test
    @DisplayName("Should handle mercantile affiliation for contributor data")
    void executeWorkerRetirement_ShouldHandleMercantileAffiliationForContributorData() {
        affiliate.setAffiliationSubType("OTHER");
        List<Retirement> retirementList = List.of(retirement);
        List<Affiliate> affiliateList = List.of(affiliate);

        when(retirementRepository.findWorkerRetirement()).thenReturn(retirementList);
        when(affiliateRepository.findById(retirement.getIdAffiliate())).thenReturn(Optional.of(affiliate));
        when(affiliationDependentRepository.findByFiledNumber(affiliate.getFiledNumber()))
                .thenReturn(Optional.of(affiliationDependent));
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(affiliateList);
        when(mercantileRepository.findByFiledNumber(affiliate.getFiledNumber())).thenReturn(Optional.of(affiliateMercantile));
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(noveltyRuafRepository.findByIdAffiliate(anyLong())).thenReturn(null);
        when(noveltyRuafRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(noveltyRuafRepository.save(any(NoveltyRuaf.class))).thenReturn(noveltyRuaf);

        Boolean result = noveltyRuafService.executeWorkerRetirement();

        assertTrue(result);
        verify(mercantileRepository).findByFiledNumber(affiliate.getFiledNumber());
        verify(noveltyRuafRepository).save(any(NoveltyRuaf.class));
    }

    @Test
    @DisplayName("Should test homologation causal for death reason")
    void executeWorkerRetirement_ShouldTestHomologationCausalForDeathReason() {
        retirement.setIdRetirementReason(2L);
        List<Retirement> retirementList = List.of(retirement);
        List<Affiliate> affiliateList = List.of(affiliate);

        when(retirementRepository.findWorkerRetirement()).thenReturn(retirementList);
        when(affiliateRepository.findById(retirement.getIdAffiliate())).thenReturn(Optional.of(affiliate));
        when(affiliationDependentRepository.findByFiledNumber(affiliate.getFiledNumber()))
                .thenReturn(Optional.of(affiliationDependent));
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(affiliateList);
        when(affiliationRepository.findByFiledNumber(affiliate.getFiledNumber())).thenReturn(Optional.of(affiliation));
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(noveltyRuafRepository.findByIdAffiliate(anyLong())).thenReturn(null);
        when(noveltyRuafRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(noveltyRuafRepository.save(any(NoveltyRuaf.class))).thenReturn(noveltyRuaf);

        Boolean result = noveltyRuafService.executeWorkerRetirement();

        assertTrue(result);
        verify(noveltyRuafRepository).save(any(NoveltyRuaf.class));
    }

    @Test
    @DisplayName("Should test homologation causal for pension reason")
    void executeWorkerRetirement_ShouldTestHomologationCausalForPensionReason() {
        retirement.setIdRetirementReason(4L);
        List<Retirement> retirementList = List.of(retirement);
        List<Affiliate> affiliateList = List.of(affiliate);

        when(retirementRepository.findWorkerRetirement()).thenReturn(retirementList);
        when(affiliateRepository.findById(retirement.getIdAffiliate())).thenReturn(Optional.of(affiliate));
        when(affiliationDependentRepository.findByFiledNumber(affiliate.getFiledNumber()))
                .thenReturn(Optional.of(affiliationDependent));
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(affiliateList);
        when(affiliationRepository.findByFiledNumber(affiliate.getFiledNumber())).thenReturn(Optional.of(affiliation));
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(noveltyRuafRepository.findByIdAffiliate(anyLong())).thenReturn(null);
        when(noveltyRuafRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(noveltyRuafRepository.save(any(NoveltyRuaf.class))).thenReturn(noveltyRuaf);

        Boolean result = noveltyRuafService.executeWorkerRetirement();

        assertTrue(result);
        verify(noveltyRuafRepository).save(any(NoveltyRuaf.class));
    }

    @Test
    @DisplayName("Should test homologation causal for default reason")
    void executeWorkerRetirement_ShouldTestHomologationCausalForDefaultReason() {
        retirement.setIdRetirementReason(99L);
        List<Retirement> retirementList = List.of(retirement);
        List<Affiliate> affiliateList = List.of(affiliate);

        when(retirementRepository.findWorkerRetirement()).thenReturn(retirementList);
        when(affiliateRepository.findById(retirement.getIdAffiliate())).thenReturn(Optional.of(affiliate));
        when(affiliationDependentRepository.findByFiledNumber(affiliate.getFiledNumber()))
                .thenReturn(Optional.of(affiliationDependent));
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(affiliateList);
        when(affiliationRepository.findByFiledNumber(affiliate.getFiledNumber())).thenReturn(Optional.of(affiliation));
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(noveltyRuafRepository.findByIdAffiliate(anyLong())).thenReturn(null);
        when(noveltyRuafRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(noveltyRuafRepository.save(any(NoveltyRuaf.class))).thenReturn(noveltyRuaf);

        Boolean result = noveltyRuafService.executeWorkerRetirement();

        assertTrue(result);
        verify(noveltyRuafRepository).save(any(NoveltyRuaf.class));
    }

    @Test
    @DisplayName("Should test mercantile affiliation not found in findDataContributor")
    void executeWorkerRetirement_ShouldTestMercantileAffiliationNotFoundInFindDataContributor() {
        affiliate.setAffiliationSubType("OTHER");
        List<Retirement> retirementList = List.of(retirement);
        List<Affiliate> affiliateList = List.of(affiliate);

        when(retirementRepository.findWorkerRetirement()).thenReturn(retirementList);
        when(affiliateRepository.findById(retirement.getIdAffiliate())).thenReturn(Optional.of(affiliate));
        when(affiliationDependentRepository.findByFiledNumber(affiliate.getFiledNumber()))
                .thenReturn(Optional.of(affiliationDependent));
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(affiliateList);
        when(mercantileRepository.findByFiledNumber(affiliate.getFiledNumber())).thenReturn(Optional.empty());
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(noveltyRuafRepository.findByIdAffiliate(anyLong())).thenReturn(null);

        assertThrows(AffiliationNotFoundError.class, () -> noveltyRuafService.executeWorkerRetirement());

        verify(mercantileRepository).findByFiledNumber(affiliate.getFiledNumber());
    }

    @Test
    @DisplayName("Should test domestic services affiliation not found in findDataContributor")
    void executeWorkerRetirement_ShouldTestDomesticServicesAffiliationNotFoundInFindDataContributor() {
        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        List<Retirement> retirementList = List.of(retirement);
        List<Affiliate> affiliateList = List.of(affiliate);

        when(retirementRepository.findWorkerRetirement()).thenReturn(retirementList);
        when(affiliateRepository.findById(retirement.getIdAffiliate())).thenReturn(Optional.of(affiliate));
        when(affiliationDependentRepository.findByFiledNumber(affiliate.getFiledNumber()))
                .thenReturn(Optional.of(affiliationDependent));
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(affiliateList);
        when(affiliationRepository.findByFiledNumber(affiliate.getFiledNumber())).thenReturn(Optional.empty());
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(noveltyRuafRepository.findByIdAffiliate(anyLong())).thenReturn(null);

        assertThrows(AffiliationNotFoundError.class, () -> noveltyRuafService.executeWorkerRetirement());

        verify(affiliationRepository).findByFiledNumber(affiliate.getFiledNumber());
    }



    @Test
    @DisplayName("Should generate RNRA file successfully when file doesn't exist")
    void generateFileRNRA_ShouldGenerateFileSuccessfullyWhenFileDoesNotExist() throws Exception {
        List<NoveltyRuaf> noveltyList = List.of(noveltyRuaf);
        when(ruafFilesRepository.findByFileNameAndIsSuccessful(anyString(), eq(true)))
                .thenReturn(Optional.empty());
        when(noveltyRuafRepository.findAll()).thenReturn(noveltyList);
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(properties.getFolderIdRuafRnra()).thenReturn("folder-id");
        when(ruafFilesHelper.uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString()))
                .thenReturn("alfresco-id");

        String result = noveltyRuafService.generateFileRNRA();

        assertEquals("", result);
        verify(ruafFilesRepository).findByFileNameAndIsSuccessful(anyString(), eq(true));
        verify(noveltyRuafRepository).findAll();
        verify(ruafFilesHelper).uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString());
        verify(ruafFilesHelper).saveRuafFile(anyString(), eq("alfresco-id"), eq(true), eq(RuafTypes.RNRA));
    }

    @Test
    @DisplayName("Should return empty string when RNRA file already exists")
    void generateFileRNRA_ShouldReturnEmptyStringWhenFileAlreadyExists() throws Exception {
        RuafFiles existingFile = new RuafFiles();
        when(ruafFilesRepository.findByFileNameAndIsSuccessful(anyString(), eq(true)))
                .thenReturn(Optional.of(existingFile));
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));

        String result = noveltyRuafService.generateFileRNRA();

        assertEquals("", result);
        verify(ruafFilesRepository).findByFileNameAndIsSuccessful(anyString(), eq(true));
        verify(ruafFilesHelper, never()).uploadAlfrescoFile(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Should handle IOException during RNRA file generation (propagates)")
    void generateFileRNRA_ShouldHandleIOExceptionDuringFileGeneration() throws Exception {
        List<NoveltyRuaf> noveltyList = List.of(noveltyRuaf);
        when(ruafFilesRepository.findByFileNameAndIsSuccessful(anyString(), eq(true)))
                .thenReturn(Optional.empty());
        when(noveltyRuafRepository.findAll()).thenReturn(noveltyList);
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(properties.getFolderIdRuafRnra()).thenReturn("folder-id");
        when(ruafFilesHelper.uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString()))
                .thenThrow(new RuntimeException("Upload failed"));

        assertThrows(RuntimeException.class, () -> noveltyRuafService.generateFileRNRA());
    }

    @Test
    @DisplayName("Should test buildRegistersTypeTwo with null values")
    void generateFileRNRA_ShouldTestBuildRegistersTypeTwoWithNullValues() throws Exception {
        NoveltyRuaf noveltyWithNulls = new NoveltyRuaf();
        noveltyWithNulls.setArlCode("250");
        noveltyWithNulls.setIdentificationType("CC");
        noveltyWithNulls.setIdentificationNumber("12345678");
        noveltyWithNulls.setFirstName("Juan");
        noveltyWithNulls.setSecondName(null);
        noveltyWithNulls.setSurname("Perez");
        noveltyWithNulls.setSecondSurname(null);
        noveltyWithNulls.setNoveltyCode("R01");
        noveltyWithNulls.setIdentificationTypeContributor(null);
        noveltyWithNulls.setIdentificationNumberContributor(null);
        noveltyWithNulls.setDvContributor(null);
        noveltyWithNulls.setDisassociationDateWithContributor(null);
        noveltyWithNulls.setNoveltyDate(null);
        noveltyWithNulls.setRetirmentCausal(null);
        noveltyWithNulls.setPensionRecognitionDate(null);
        noveltyWithNulls.setDeathDate(null);

        List<NoveltyRuaf> noveltyList = List.of(noveltyWithNulls);
        when(ruafFilesRepository.findByFileNameAndIsSuccessful(anyString(), eq(true))).thenReturn(Optional.empty());
        when(noveltyRuafRepository.findAll()).thenReturn(noveltyList);
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(properties.getFolderIdRuafRnra()).thenReturn("folder-id");
        when(ruafFilesHelper.uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString()))
                .thenReturn("alfresco-id");

        String result = noveltyRuafService.generateFileRNRA();

        assertEquals("", result);
        verify(noveltyRuafRepository).findAll();
        verify(ruafFilesHelper).uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString());
    }

    @Test
    @DisplayName("Should test all novelty fields with complete data")
    void generateFileRNRA_ShouldTestAllNoveltyFieldsWithCompleteData() throws Exception {
        NoveltyRuaf completeNovelty = new NoveltyRuaf();
        completeNovelty.setArlCode("250");
        completeNovelty.setIdentificationType("CC");
        completeNovelty.setIdentificationNumber("12345678");
        completeNovelty.setFirstName("Juan");
        completeNovelty.setSecondName("Carlos");
        completeNovelty.setSurname("Perez");
        completeNovelty.setSecondSurname("Lopez");
        completeNovelty.setNoveltyCode("R01");
        completeNovelty.setIdentificationTypeContributor("NIT");
        completeNovelty.setIdentificationNumberContributor("900123456");
        completeNovelty.setDvContributor(7);
        completeNovelty.setDisassociationDateWithContributor(LocalDate.now());
        completeNovelty.setNoveltyDate(LocalDate.now());
        completeNovelty.setRetirmentCausal(1);
        completeNovelty.setPensionRecognitionDate(LocalDate.now().minusDays(30));
        completeNovelty.setDeathDate(LocalDate.now().minusDays(15));

        List<NoveltyRuaf> noveltyList = List.of(completeNovelty);
        when(ruafFilesRepository.findByFileNameAndIsSuccessful(anyString(), eq(true))).thenReturn(Optional.empty());
        when(noveltyRuafRepository.findAll()).thenReturn(noveltyList);
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(properties.getFolderIdRuafRnra()).thenReturn("folder-id");
        when(ruafFilesHelper.uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString()))
                .thenReturn("alfresco-id");

        String result = noveltyRuafService.generateFileRNRA();

        assertEquals("", result);
        verify(noveltyRuafRepository).findAll();
        verify(ruafFilesHelper).uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString());
    }

    @Test
    @DisplayName("Should retry generating RNRE file")
    void retryGeneratingFileRNRE_ShouldRetryGeneratingFile() throws Exception {
        Set<UsersInArrears> usersInArrears = new LinkedHashSet<>();
        UsersInArrears user = mock(UsersInArrears.class);
        when(user.getTypeDocument()).thenReturn("CC");
        when(user.getNumberDocument()).thenReturn("12345678");
        when(user.getFirstSurname()).thenReturn("Perez");
        when(user.getSecondSurname()).thenReturn("Lopez");
        when(user.getFirstName()).thenReturn("Juan");
        when(user.getSecondName()).thenReturn("Carlos");
        when(user.getStatusAffiliation()).thenReturn("ACTIVE");
        when(user.getTypeDocumentAffiliate()).thenReturn("NIT");
        when(user.getNumberDocumentAffiliate()).thenReturn("900123456");
        when(user.getNameCompany()).thenReturn("Company Test");
        when(user.getArrears()).thenReturn("50000");
        usersInArrears.add(user);

        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(ruafFilesRepository.findUsersInArrears(anyList())).thenReturn(usersInArrears);
        when(properties.getFolderIdRuafRnre()).thenReturn("folder-id");
        when(ruafFilesHelper.uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString()))
                .thenReturn("alfresco-id");
        when(iUserRegisterService.calculateModulo11DV(anyString())).thenReturn(7);

        ByteArrayToMultipartFile result = noveltyRuafService.retryGeneratingFileRNRE();

        assertNotNull(result);
        verify(ruafFilesRepository).findUsersInArrears(anyList());
        verify(ruafFilesHelper).uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString());
        verify(ruafFilesHelper).saveRuafFile(anyString(), eq("alfresco-id"), eq(true), eq(RuafTypes.RNRE));
    }


    @Test
    @DisplayName("Should execute generateFileRNRECRON scheduled method")
    void generateFileRNRECRON_ShouldExecuteScheduledMethod() throws Exception {
        Set<UsersInArrears> usersInArrears = new LinkedHashSet<>();
        UsersInArrears user = mock(UsersInArrears.class);
        when(user.getTypeDocument()).thenReturn("CC");
        when(user.getNumberDocument()).thenReturn("12345678");
        when(user.getFirstSurname()).thenReturn("Perez");
        when(user.getSecondSurname()).thenReturn("Lopez");
        when(user.getFirstName()).thenReturn("Juan");
        when(user.getSecondName()).thenReturn("Carlos");
        when(user.getStatusAffiliation()).thenReturn("ACTIVE");
        when(user.getTypeDocumentAffiliate()).thenReturn("NIT");
        when(user.getNumberDocumentAffiliate()).thenReturn("900123456");
        when(user.getNameCompany()).thenReturn("Company Test");
        when(user.getArrears()).thenReturn("50000");
        usersInArrears.add(user);

        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(ruafFilesRepository.findUsersInArrears(anyList())).thenReturn(usersInArrears);
        when(properties.getFolderIdRuafRnre()).thenReturn("folder-id");
        when(ruafFilesHelper.uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString()))
                .thenReturn("alfresco-id");
        when(iUserRegisterService.calculateModulo11DV(anyString())).thenReturn(7);

        noveltyRuafService.generateFileRNRECRON();

        verify(ruafFilesRepository).findUsersInArrears(anyList());
        verify(ruafFilesHelper).uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString());
        verify(ruafFilesHelper).saveRuafFile(anyString(), eq("alfresco-id"), eq(true), eq(RuafTypes.RNRE));
    }

    @Test
    @DisplayName("Should generate RNRE file successfully")
    void generateFileRNRE_ShouldGenerateFileSuccessfully() throws Exception {
        Set<UsersInArrears> usersInArrears = new LinkedHashSet<>();
        UsersInArrears user = mock(UsersInArrears.class);
        when(user.getTypeDocument()).thenReturn("CC");
        when(user.getNumberDocument()).thenReturn("12345678");
        when(user.getFirstSurname()).thenReturn("Perez");
        when(user.getSecondSurname()).thenReturn("Lopez");
        when(user.getFirstName()).thenReturn("Juan");
        when(user.getSecondName()).thenReturn("Carlos");
        when(user.getStatusAffiliation()).thenReturn("ACTIVE");
        when(user.getTypeDocumentAffiliate()).thenReturn("NIT");
        when(user.getNumberDocumentAffiliate()).thenReturn("900123456");
        when(user.getNameCompany()).thenReturn("Company Test");
        when(user.getArrears()).thenReturn("50000");
        usersInArrears.add(user);

        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(ruafFilesRepository.findUsersInArrears(anyList())).thenReturn(usersInArrears);
        when(properties.getFolderIdRuafRnre()).thenReturn("folder-id");
        when(ruafFilesHelper.uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString()))
                .thenReturn("alfresco-id");
        when(iUserRegisterService.calculateModulo11DV(anyString())).thenReturn(7);

        ByteArrayToMultipartFile result = noveltyRuafService.generateFileRNRE();

        assertNotNull(result);
        verify(arlInformationDao, atLeastOnce()).findAllArlInformation();
        verify(ruafFilesRepository).findUsersInArrears(anyList());
        verify(ruafFilesHelper).uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString());
        verify(ruafFilesHelper).saveRuafFile(anyString(), eq("alfresco-id"), eq(true), eq(RuafTypes.RNRE));
        verify(iUserRegisterService).calculateModulo11DV(anyString());
    }

    @Test
    @DisplayName("Should return exception when repository fails in RNRE (propagates)")
    void generateFileRNRE_ShouldReturnNullWhenExceptionOccurs() throws Exception {
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(ruafFilesRepository.findUsersInArrears(anyList())).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> noveltyRuafService.generateFileRNRE());
    }


    @Test
    @DisplayName("Should handle null and empty values in user data for RNRE file")
    void generateFileRNRE_ShouldHandleNullAndEmptyValuesInUserData() throws Exception {
        Set<UsersInArrears> usersInArrears = new LinkedHashSet<>();
        UsersInArrears user = mock(UsersInArrears.class);
        when(user.getTypeDocument()).thenReturn(null);
        when(user.getNumberDocument()).thenReturn("");
        when(user.getFirstSurname()).thenReturn("Perez@#$");
        when(user.getSecondSurname()).thenReturn(null);
        when(user.getFirstName()).thenReturn("Juan!@#");
        when(user.getSecondName()).thenReturn("");
        when(user.getStatusAffiliation()).thenReturn("ACTIVE");
        when(user.getTypeDocumentAffiliate()).thenReturn(null);
        when(user.getNumberDocumentAffiliate()).thenReturn(null);
        when(user.getNameCompany()).thenReturn("Company Test$%&");
        when(user.getArrears()).thenReturn("50000");
        usersInArrears.add(user);

        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(ruafFilesRepository.findUsersInArrears(anyList())).thenReturn(usersInArrears);
        when(properties.getFolderIdRuafRnre()).thenReturn("folder-id");
        when(ruafFilesHelper.uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString()))
                .thenReturn("alfresco-id");

        ByteArrayToMultipartFile result = noveltyRuafService.generateFileRNRE();

        assertNotNull(result);
        verify(ruafFilesHelper).uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString());
        verify(ruafFilesHelper).saveRuafFile(anyString(), eq("alfresco-id"), eq(true), eq(RuafTypes.RNRE));
    }

    @Test
    @DisplayName("Should test dv calculation with null document number")
    void generateFileRNRE_ShouldTestDvCalculationWithNullDocumentNumber() throws Exception {
        Set<UsersInArrears> usersInArrears = new LinkedHashSet<>();
        UsersInArrears user = mock(UsersInArrears.class);
        when(user.getTypeDocument()).thenReturn("CC");
        when(user.getNumberDocument()).thenReturn("12345678");
        when(user.getFirstSurname()).thenReturn("Perez");
        when(user.getSecondSurname()).thenReturn("Lopez");
        when(user.getFirstName()).thenReturn("Juan");
        when(user.getSecondName()).thenReturn("Carlos");
        when(user.getStatusAffiliation()).thenReturn("ACTIVE");
        when(user.getTypeDocumentAffiliate()).thenReturn("NIT");
        when(user.getNumberDocumentAffiliate()).thenReturn(null);
        when(user.getNameCompany()).thenReturn("Company Test");
        when(user.getArrears()).thenReturn("50000");
        usersInArrears.add(user);

        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(ruafFilesRepository.findUsersInArrears(anyList())).thenReturn(usersInArrears);
        when(properties.getFolderIdRuafRnre()).thenReturn("folder-id");
        when(ruafFilesHelper.uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString()))
                .thenReturn("alfresco-id");

        ByteArrayToMultipartFile result = noveltyRuafService.generateFileRNRE();

        assertNotNull(result);
        verify(iUserRegisterService, never()).calculateModulo11DV(anyString());
    }

    @Test
    @DisplayName("Should test dv calculation with empty document number")
    void generateFileRNRE_ShouldTestDvCalculationWithEmptyDocumentNumber() throws Exception {
        Set<UsersInArrears> usersInArrears = new LinkedHashSet<>();
        UsersInArrears user = mock(UsersInArrears.class);
        when(user.getTypeDocument()).thenReturn("CC");
        when(user.getNumberDocument()).thenReturn("12345678");
        when(user.getFirstSurname()).thenReturn("Perez");
        when(user.getSecondSurname()).thenReturn("Lopez");
        when(user.getFirstName()).thenReturn("Juan");
        when(user.getSecondName()).thenReturn("Carlos");
        when(user.getStatusAffiliation()).thenReturn("ACTIVE");
        when(user.getTypeDocumentAffiliate()).thenReturn("NIT");
        when(user.getNumberDocumentAffiliate()).thenReturn("");
        when(user.getNameCompany()).thenReturn("Company Test");
        when(user.getArrears()).thenReturn("50000");
        usersInArrears.add(user);

        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(ruafFilesRepository.findUsersInArrears(anyList())).thenReturn(usersInArrears);
        when(properties.getFolderIdRuafRnre()).thenReturn("folder-id");
        when(ruafFilesHelper.uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString()))
                .thenReturn("alfresco-id");

        ByteArrayToMultipartFile result = noveltyRuafService.generateFileRNRE();

        assertNotNull(result);
        verify(iUserRegisterService, never()).calculateModulo11DV(anyString());
    }

    @Test
    @DisplayName("Should test validCharacter method with special characters")
    void generateFileRNRE_ShouldTestValidCharacterMethodWithSpecialCharacters() throws Exception {
        Set<UsersInArrears> usersInArrears = new LinkedHashSet<>();
        UsersInArrears user = mock(UsersInArrears.class);
        when(user.getTypeDocument()).thenReturn("CC");
        when(user.getNumberDocument()).thenReturn("12345678");
        when(user.getFirstSurname()).thenReturn("Perez@#$%^&*()");
        when(user.getSecondSurname()).thenReturn("Lopez!@#$%");
        when(user.getFirstName()).thenReturn("Juan{}[]|");
        when(user.getSecondName()).thenReturn("Carlos<>?/");
        when(user.getStatusAffiliation()).thenReturn("ACTIVE");
        when(user.getTypeDocumentAffiliate()).thenReturn("NIT");
        when(user.getNumberDocumentAffiliate()).thenReturn("900123456");
        when(user.getNameCompany()).thenReturn("Company Test & Co.");
        when(user.getArrears()).thenReturn("50000");
        usersInArrears.add(user);

        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(ruafFilesRepository.findUsersInArrears(anyList())).thenReturn(usersInArrears);
        when(properties.getFolderIdRuafRnre()).thenReturn("folder-id");
        when(ruafFilesHelper.uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString()))
                .thenReturn("alfresco-id");
        when(iUserRegisterService.calculateModulo11DV(anyString())).thenReturn(7);

        ByteArrayToMultipartFile result = noveltyRuafService.generateFileRNRE();

        assertNotNull(result);
        verify(ruafFilesHelper).uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString());
    }

    @Test
    @DisplayName("Should handle multiple users in arrears")
    void generateFileRNRE_ShouldHandleMultipleUsersInArrears() throws Exception {
        Set<UsersInArrears> usersInArrears = new LinkedHashSet<>();

        UsersInArrears user1 = mock(UsersInArrears.class);
        when(user1.getTypeDocument()).thenReturn("CC");
        when(user1.getNumberDocument()).thenReturn("12345678");
        when(user1.getFirstSurname()).thenReturn("Perez");
        when(user1.getSecondSurname()).thenReturn("Lopez");
        when(user1.getFirstName()).thenReturn("Juan");
        when(user1.getSecondName()).thenReturn("Carlos");
        when(user1.getStatusAffiliation()).thenReturn("ACTIVE");
        when(user1.getTypeDocumentAffiliate()).thenReturn("NIT");
        when(user1.getNumberDocumentAffiliate()).thenReturn("900123456");
        when(user1.getNameCompany()).thenReturn("Company Test 1");
        when(user1.getArrears()).thenReturn("50000");

        UsersInArrears user2 = mock(UsersInArrears.class);
        when(user2.getTypeDocument()).thenReturn("CC");
        when(user2.getNumberDocument()).thenReturn("87654321");
        when(user2.getFirstSurname()).thenReturn("Garcia");
        when(user2.getSecondSurname()).thenReturn("Martinez");
        when(user2.getFirstName()).thenReturn("Maria");
        when(user2.getSecondName()).thenReturn("Elena");
        when(user2.getStatusAffiliation()).thenReturn("ACTIVE");
        when(user2.getTypeDocumentAffiliate()).thenReturn("NIT");
        when(user2.getNumberDocumentAffiliate()).thenReturn("900654321");
        when(user2.getNameCompany()).thenReturn("Company Test 2");
        when(user2.getArrears()).thenReturn("75000");

        usersInArrears.add(user1);
        usersInArrears.add(user2);

        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(ruafFilesRepository.findUsersInArrears(anyList())).thenReturn(usersInArrears);
        when(properties.getFolderIdRuafRnre()).thenReturn("folder-id");
        when(ruafFilesHelper.uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString()))
                .thenReturn("alfresco-id");
        when(iUserRegisterService.calculateModulo11DV("900123456")).thenReturn(7);
        when(iUserRegisterService.calculateModulo11DV("900654321")).thenReturn(3);

        ByteArrayToMultipartFile result = noveltyRuafService.generateFileRNRE();

        assertNotNull(result);
        verify(ruafFilesHelper).uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString());
        verify(iUserRegisterService).calculateModulo11DV("900123456");
        verify(iUserRegisterService).calculateModulo11DV("900654321");
    }

    @Test
    @DisplayName("Should test empty users in arrears")
    void generateFileRNRE_ShouldTestEmptyUsersInArrears() throws Exception {
        Set<UsersInArrears> emptyUsersInArrears = new LinkedHashSet<>();

        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(ruafFilesRepository.findUsersInArrears(anyList())).thenReturn(emptyUsersInArrears);
        when(properties.getFolderIdRuafRnre()).thenReturn("folder-id");
        when(ruafFilesHelper.uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString()))
                .thenReturn("alfresco-id");

        ByteArrayToMultipartFile result = noveltyRuafService.generateFileRNRE();

        assertNotNull(result);
        verify(ruafFilesHelper).uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString());
        verify(ruafFilesHelper).saveRuafFile(anyString(), eq("alfresco-id"), eq(true), eq(RuafTypes.RNRE));
    }

    @Test
    @DisplayName("Should test findUsersInArrears stages parameter")
    void generateFileRNRE_ShouldTestFindUsersInArrearsStagesParameter() throws Exception {
        Set<UsersInArrears> usersInArrears = new LinkedHashSet<>();
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arlInformation));
        when(ruafFilesRepository.findUsersInArrears(List.of("Coactiva", "Persuasiva"))).thenReturn(usersInArrears);
        when(properties.getFolderIdRuafRnre()).thenReturn("folder-id");
        when(ruafFilesHelper.uploadAlfrescoFile(anyString(), any(ByteArrayToMultipartFile.class), anyString()))
                .thenReturn("alfresco-id");

        ByteArrayToMultipartFile result = noveltyRuafService.generateFileRNRE();

        assertNotNull(result);
        verify(ruafFilesRepository).findUsersInArrears(List.of("Coactiva", "Persuasiva"));
    }
}
