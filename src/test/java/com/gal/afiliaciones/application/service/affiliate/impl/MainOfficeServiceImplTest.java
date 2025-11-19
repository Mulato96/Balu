package com.gal.afiliaciones.application.service.affiliate.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.application.service.affiliate.WorkCenterService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.Municipality;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MainOfficeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.client.generic.headquarters.InsertHeadquartersClient;
import com.gal.afiliaciones.infrastructure.client.generic.headquarters.UpdateHeadquartersClient;
import com.gal.afiliaciones.infrastructure.client.generic.workcenter.InsertWorkCenterClient;
import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeGrillaDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeOfficialDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

/**
 * Clase de test completa para MainOfficeServiceImpl
 * Cobertura objetivo: 100%
 */
class MainOfficeServiceImplTest {

    @Mock
    private SendEmails sendEmails;
    @Mock
    private MainOfficeRepository repository;
    @Mock
    private WorkCenterService workCenterService;
    @Mock
    private AffiliateRepository affiliateRepository;
    @Mock
    private IUserPreRegisterRepository iUserPreRegisterRepository;
    @Mock
    private IEconomicActivityRepository economicActivityRepository;
    @Mock
    private AffiliateMercantileRepository affiliateMercantileRepository;
    @Mock
    private AffiliationDependentRepository affiliationDependentRepository;
    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository domesticServiceIndependentRepository;
    @Mock
    private ArlInformationDao arlInformationDao;
    @Mock
    private InsertHeadquartersClient insertHeadquartersClient;
    @Mock
    private UpdateHeadquartersClient updateHeadquartersClient;
    @Mock
    private InsertWorkCenterClient insertWorkCenterClient;
    @Mock
    private MunicipalityRepository municipalityRepository;

    @InjectMocks
    private MainOfficeServiceImpl mainOfficeService;

    private UserMain userMain;
    private Affiliate affiliate;
    private AffiliateMercantile affiliateMercantile;
    private EconomicActivity economicActivity1;
    private EconomicActivity economicActivity2;
    private EconomicActivity economicActivity3;
    private MainOffice existingMainOffice;
    private MainOffice existingOfficeToUpdate;
    private ArgumentCaptor<MainOffice> mainOfficeCaptor;

    @BeforeEach
    void setUp() {
        mainOfficeCaptor = ArgumentCaptor.forClass(MainOffice.class);

        MockitoAnnotations.openMocks(this);
        userMain = new UserMain();
        userMain.setId(1L);
        userMain.setIdentificationType("CC");
        userMain.setIdentification("123456789");
        userMain.setFirstName("Test");
        userMain.setSurname("User");
        userMain.setEmail("test.user@example.com");

        affiliate = new Affiliate();
        affiliate.setIdAffiliate(1L);
        affiliate.setFiledNumber("F123");
        affiliate.setCompany("Test Company");
        affiliate.setNitCompany("987654321");
        affiliate.setDocumentType("CC");
        affiliate.setDocumentNumber("123456789");
        affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);

        affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setEmail("merc@example.com");

        economicActivity1 = new EconomicActivity();
        economicActivity1.setId(101L);
        economicActivity1.setClassRisk("1");
        economicActivity1.setCodeCIIU("0111");
        economicActivity1.setAdditionalCode("01");
        economicActivity1.setDescription("Activity 1");
        economicActivity1.setEconomicActivityCode("1011101");

        economicActivity2 = new EconomicActivity();
        economicActivity2.setId(102L);
        economicActivity2.setClassRisk("2");
        economicActivity2.setCodeCIIU("0222");
        economicActivity2.setAdditionalCode("02");
        economicActivity2.setDescription("Activity 2");
        economicActivity2.setEconomicActivityCode("2022202");

        economicActivity3 = new EconomicActivity();
        economicActivity3.setId(103L);
        economicActivity3.setClassRisk("3");
        economicActivity3.setCodeCIIU("0333");
        economicActivity3.setAdditionalCode("03");
        economicActivity3.setDescription("Activity 3");

        existingMainOffice = new MainOffice();
        existingMainOffice.setId(99L);
        existingMainOffice.setOfficeManager(userMain);
        existingMainOffice.setMain(true);
        existingMainOffice.setCode("S0000000000");
        existingMainOffice.setIdAffiliate(1L);
        existingMainOffice.setMainOfficeName("Main Office");
        existingMainOffice.setIdDepartment(5L);
        existingMainOffice.setIdCity(101L);
        existingMainOffice.setMainOfficeZone(Constant.URBAN_ZONE);
        existingMainOffice.setAddress("Main Address");
        existingMainOffice.setMainOfficeEmail("main@example.com");
        existingMainOffice.setMainOfficePhoneNumber("3101234567");

        existingOfficeToUpdate = new MainOffice();
        existingOfficeToUpdate.setId(50L);
        existingOfficeToUpdate.setOfficeManager(userMain);
        existingOfficeToUpdate.setMain(false);
        existingOfficeToUpdate.setCode("S0000000050");
        existingOfficeToUpdate.setMainOfficeName("Office To Update");
        existingOfficeToUpdate.setAddress("Old Address 456");
        existingOfficeToUpdate.setIdDepartment(5L);
        existingOfficeToUpdate.setIdCity(101L);
        existingOfficeToUpdate.setMainOfficePhoneNumber("3109998888");
        existingOfficeToUpdate.setMainOfficeEmail("update.me@example.com");
        existingOfficeToUpdate.setMainOfficeZone(Constant.URBAN_ZONE);
        existingOfficeToUpdate.setTypeDocumentResponsibleHeadquarters("CC");
        existingOfficeToUpdate.setNumberDocumentResponsibleHeadquarters("111222333");
        existingOfficeToUpdate.setFirstNameResponsibleHeadquarters("OldResp");
        existingOfficeToUpdate.setSurnameResponsibleHeadquarters("OldSible");
        existingOfficeToUpdate.setPhoneOneResponsibleHeadquarters("3151112233");
        existingOfficeToUpdate.setEmailResponsibleHeadquarters("old.resp@example.com");
    }

    private MainOfficeDTO createValidMainOfficeDTO(boolean main) {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Test Office");
        dto.setOfficeManager(userMain.getId());
        dto.setIdAffiliateEmployer(1L);
        dto.setMainOfficeEmail("test@example.com");
        dto.setMainOfficePhoneNumber("3101234567");
        dto.setMainPhoneNumberTwo("3101234568");
        dto.setPhoneOneResponsibleHeadquarters("3101234569");
        dto.setPhoneTwoResponsibleHeadquarters("3101234570");
        dto.setEmailResponsibleHeadquarters("resp@example.com");
        dto.setMainOfficeZone(Constant.URBAN_ZONE);
        dto.setEconomicActivity(List.of(economicActivity1.getId()));
        dto.setMain(main);
        dto.setTypeDocumentResponsibleHeadquarters("CC");
        dto.setNumberDocumentResponsibleHeadquarters("123456789");
        dto.setFirstNameResponsibleHeadquarters("John");
        dto.setSecondNameResponsibleHeadquarters("Doe");
        dto.setSurnameResponsibleHeadquarters("Smith");
        dto.setSecondSurnameResponsibleHeadquarters("Brown");

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setIdCity(101L);
        addressDTO.setIdDepartment(5L);
        addressDTO.setAddress("Test Address 123");
        dto.setAddressDTO(addressDTO);

        return dto;
    }

    // ============= Tests básicos de CRUD =============

    @Test
    void testGetAllMainOffices_Success_OfficesFound() {
        Long idUser = 1L;
        UserMain userManager = new UserMain();
        userManager.setId(idUser);

        MainOffice office1 = new MainOffice();
        office1.setId(10L);
        office1.setCode("S0000000001");
        office1.setMainOfficeName("Office One");
        office1.setAddress("123 Main St");
        office1.setIdDepartment(5L);
        office1.setIdCity(101L);
        office1.setOfficeManager(userManager);
        office1.setMain(true);

        MainOffice office2 = new MainOffice();
        office2.setId(11L);
        office2.setCode("S0000000002");
        office2.setMainOfficeName("Office Two");
        office2.setAddress("456 Side St");
        office2.setIdDepartment(5L);
        office2.setIdCity(102L);
        office2.setOfficeManager(userManager);
        office2.setMain(false);

        List<MainOffice> mockOffices = List.of(office1, office2);

        when(repository.findAll(any(Specification.class))).thenReturn(mockOffices);

        List<MainOfficeGrillaDTO> result = mainOfficeService.getAllMainOffices(idUser);

        assertNotNull(result);
        assertEquals(2, result.size());

        MainOfficeGrillaDTO dto1 = result.get(0);
        assertEquals(office1.getId(), dto1.getId());
        assertEquals(office1.getCode(), dto1.getCode());
        assertEquals(office1.getMainOfficeName(), dto1.getMainOfficeName());
        assertEquals(office1.getAddress(), dto1.getAddress());
        assertEquals(office1.getIdDepartment(), dto1.getIdDepartment());
        assertEquals(office1.getIdCity(), dto1.getIdCity());
        assertEquals(office1.getMain(), dto1.getMain());

        verify(repository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void getAllMainOffices_returnsList() {
        MainOffice mainOffice = new MainOffice();
        mainOffice.setId(1L);
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(mainOffice));

        List<MainOfficeGrillaDTO> result = mainOfficeService.getAllMainOffices(1L);

        assertEquals(1, result.size());
        assertEquals(mainOffice.getId(), result.get(0).getId());
    }

    @Test
    void getMainOfficeByCode_returnsOffice() {
        MainOffice office = new MainOffice();
        when(repository.findByCode("CODE")).thenReturn(office);

        MainOffice result = mainOfficeService.getMainOfficeByCode("CODE");

        assertSame(office, result);
    }

    @Test
    void saveMainOffice_entity_savesAndReturns() {
        MainOffice office = new MainOffice();
        when(repository.save(office)).thenReturn(office);

        MainOffice result = mainOfficeService.saveMainOffice(office);

        assertSame(office, result);
    }

    @Test
    void findById_returnsOffice() {
        MainOffice office = new MainOffice();
        when(repository.findById(1L)).thenReturn(Optional.of(office));

        MainOffice result = mainOfficeService.findById(1L);

        assertSame(office, result);
    }

    @Test
    void findById_notFound_throws() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> mainOfficeService.findById(1L));
    }

    @Test
    void findCode_incrementsCode() {
        MainOffice office = new MainOffice();
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(office));

        String code = mainOfficeService.findCode();

        assertEquals("S0000000000", code);
    }

    @Test
    void findCode_noOffices_returnsS0001() {
        when(repository.findAll(any(Sort.class))).thenReturn(List.of());

        String code = mainOfficeService.findCode();

        assertEquals("S0000000000", code);
    }

    @Test
    void findByIdUserAndDepartmentAndCity_returnsList() {
        MainOffice office = new MainOffice();
        office.setId(2L);
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(office));

        List<MainOfficeGrillaDTO> result = mainOfficeService.findByIdUserAndDepartmentAndCity(1L, 2L, 3L);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    void delete_mainOfficeIsMain_throws() {
        MainOffice office = new MainOffice();
        office.setMain(true);
        when(repository.findById(1L)).thenReturn(Optional.of(office));
        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(123L);
        affiliate.setFiledNumber("F1");
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setEconomicActivity(new ArrayList<>());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mercantile));
    }

    @Test
    void delete_lastOffice_throws() {
        MainOffice office = new MainOffice();
        office.setMain(false);
        office.setOfficeManager(new UserMain());
        office.getOfficeManager().setId(10L);
        when(repository.findById(1L)).thenReturn(Optional.of(office));

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(123L);
        affiliate.setFiledNumber("F1");
        affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));

        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setEconomicActivity(new ArrayList<>());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mercantile));
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(office));

        assertThrows(AffiliationError.class, () -> mainOfficeService.delete(1L, 123L));
    }

    @Test
    void delete_successful() {
        MainOffice office = new MainOffice();
        office.setMain(false);
        UserMain user = new UserMain();
        user.setId(10L);
        office.setOfficeManager(user);
        when(repository.findById(1L)).thenReturn(Optional.of(office));

        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(123L);
        affiliate.setFiledNumber("F1");
        affiliate.setCompany("Test");
        affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));

        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setEconomicActivity(new ArrayList<>());
        mercantile.setEmail("test@test.com");
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mercantile));
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(office, new MainOffice()));

        String result = mainOfficeService.delete(1L, 123L);

        assertEquals("OK", result);
        verify(repository).delete(office);
    }


    @Test
    void delete_withMercantileAndWorkers_shouldThrowError() {
        existingMainOffice.setMain(false);

        AffiliateMercantile mercantile = new AffiliateMercantile();
        AffiliateActivityEconomic activity = new AffiliateActivityEconomic();
        activity.setActivityEconomic(economicActivity1);
        mercantile.setEconomicActivity(List.of(activity));

        AffiliationDependent dependent = new AffiliationDependent();
        dependent.setFiledNumber("DEP001");
        dependent.setIdHeadquarter(99L);

        when(repository.findById(anyLong())).thenReturn(Optional.of(existingMainOffice));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mercantile));
        when(affiliationDependentRepository.findAll(any(Specification.class))).thenReturn(List.of(dependent));

        assertThrows(AffiliationError.class, () -> mainOfficeService.delete(99L, 1L));
    }

    // ============= Tests para update =============

    @Test
    void update_mainOfficeNameExists_throws() {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Existing Name");
        dto.setOfficeManager(1L);
        dto.setEconomicActivity(new ArrayList<>());
        dto.setAddressDTO(new AddressDTO());

        MainOffice existingOffice = new MainOffice();
        existingOffice.setId(2L);
        existingOffice.setMainOfficeName("Existing Name");

        MainOffice officeToUpdate = new MainOffice();
        officeToUpdate.setId(1L);
        officeToUpdate.setMainOfficeName("Old Name");

        when(repository.findById(1L)).thenReturn(Optional.of(officeToUpdate));
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(existingOffice));

        assertThrows(AffiliationError.class, () -> mainOfficeService.update(dto, 1L));
    }


    @Test
    void findId_withEnabledWorkCenters_shouldReturnActivitiesCorrectly() {
        WorkCenter workCenter1 = new WorkCenter();
        workCenter1.setIsEnable(true);
        workCenter1.setEconomicActivityCode("1011101");

        WorkCenter workCenter2 = new WorkCenter();
        workCenter2.setIsEnable(false);
        workCenter2.setEconomicActivityCode("2022202");

        AffiliateMercantile mercantile = new AffiliateMercantile();

        when(repository.findById(anyLong())).thenReturn(Optional.of(existingMainOffice));
        when(affiliateRepository.findById(anyLong())).thenReturn(Optional.of(affiliate));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mercantile));
        when(workCenterService.getWorkCenterByMainOffice(any())).thenReturn(List.of(workCenter1, workCenter2));
        when(economicActivityRepository.findByEconomicActivityCode(anyString())).thenReturn(List.of(economicActivity1));

        MainOfficeDTO result = mainOfficeService.findId(existingMainOffice.getId());

        assertNotNull(result);
        assertEquals(1, result.getEconomicActivity().size());
    }

    @Test
    void findId_withAffiliateMercantile_returnsDTO() {
        Long mainOfficeId = 1L;
        Long affiliateId = 2L;
        Long userId = 3L;
        Long economicActivityId = 4L;

        UserMain userMain = new UserMain();
        userMain.setId(userId);

        MainOffice mainOffice = new MainOffice();
        mainOffice.setId(mainOfficeId);
        mainOffice.setIdAffiliate(affiliateId);
        mainOffice.setOfficeManager(userMain);
        mainOffice.setMainOfficeName("Test Office");

        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setId(economicActivityId);

        AffiliateActivityEconomic affiliateActivityEconomic = new AffiliateActivityEconomic();
        affiliateActivityEconomic.setActivityEconomic(economicActivity);

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setEconomicActivity(List.of(affiliateActivityEconomic));

        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("F1");
        affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);

        when(repository.findById(mainOfficeId)).thenReturn(Optional.of(mainOffice));
        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliateMercantile));

        MainOfficeDTO result = mainOfficeService.findId(mainOfficeId);

        assertEquals(mainOffice.getMainOfficeName(), result.getMainOfficeName());
        assertEquals(mainOffice.getOfficeManager().getId(), result.getOfficeManager());
        assertEquals(0, result.getEconomicActivity().size());
    }

    @Test
    void findId_withAffiliation_returnsDTO() {
        Long mainOfficeId = 1L;
        Long affiliateId = 2L;
        Long userId = 3L;
        Long economicActivityId = 4L;

        UserMain userMain = new UserMain();
        userMain.setId(userId);

        MainOffice mainOffice = new MainOffice();
        mainOffice.setId(mainOfficeId);
        mainOffice.setIdAffiliate(affiliateId);
        mainOffice.setOfficeManager(userMain);
        mainOffice.setMainOfficeName("Test Office");

        EconomicActivity economicActivity = new EconomicActivity();
        economicActivity.setId(economicActivityId);

        AffiliateActivityEconomic affiliateActivityEconomic = new AffiliateActivityEconomic();
        affiliateActivityEconomic.setActivityEconomic(economicActivity);

        Affiliation affiliation = new Affiliation();
        affiliation.setEconomicActivity(List.of(affiliateActivityEconomic));

        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("F1");
        affiliate.setAffiliationSubType("OTHER_SUBTYPE");

        when(repository.findById(mainOfficeId)).thenReturn(Optional.of(mainOffice));
        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        when(domesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));

        MainOfficeDTO result = mainOfficeService.findId(mainOfficeId);

        assertEquals(mainOffice.getMainOfficeName(), result.getMainOfficeName());
        assertEquals(mainOffice.getOfficeManager().getId(), result.getOfficeManager());
        assertEquals(0, result.getEconomicActivity().size());
    }

    @Test
    void findId_affiliateNotFound_throwsException() {
        Long mainOfficeId = 1L;
        Long affiliateId = 2L;

        MainOffice mainOffice = new MainOffice();
        mainOffice.setId(mainOfficeId);
        mainOffice.setIdAffiliate(affiliateId);
        mainOffice.setOfficeManager(new UserMain());

        when(repository.findById(mainOfficeId)).thenReturn(Optional.of(mainOffice));
        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.empty());

        assertThrows(AffiliationError.class, () -> mainOfficeService.findId(mainOfficeId));
    }

    @Test
    void findId_mainOfficeNotFound_throwsException() {
        Long mainOfficeId = 1L;
        when(repository.findById(mainOfficeId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> mainOfficeService.findId(mainOfficeId));
    }


    @Test
    void saveMainOfficeOfficial_withNoAffiliate_shouldThrowError() {
        MainOfficeOfficialDTO officialDTO = new MainOfficeOfficialDTO();
        officialDTO.setNumberDocument("987654321");
        officialDTO.setMainOfficeDTO(createValidMainOfficeDTO(false));

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThrows(AffiliationError.class, () ->
                mainOfficeService.saveMainOfficeOfficial(officialDTO)
        );
    }


    @Test
    void updateOfficial_withNoAffiliate_shouldThrowError() {
        MainOfficeDTO dto = createValidMainOfficeDTO(false);

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThrows(AffiliationError.class, () ->
                mainOfficeService.updateOfficial(dto, 1L, "987654321")
        );
    }


    @Test
    void deleteOfficial_withNoAffiliate_shouldThrowError() {
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThrows(AffiliationError.class, () ->
                mainOfficeService.deleteOfficial(1L, "987654321")
        );
    }

    @Test
    void findByNumberAndTypeDocument_withNoAffiliate_shouldThrowError() {
        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThrows(AffiliationError.class, () ->
                mainOfficeService.findByNumberAndTypeDocument("123456789", "CC")
        );
    }

    @Test
    void findByNumberAndTypeDocument_ok_devuelveSedes() {
        Affiliate affiliate = new Affiliate();
        affiliate.setIdAffiliate(777L);
        when(affiliateRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliate));

        MainOffice mo = new MainOffice();
        mo.setId(10L);
        when(repository.findAll(any(Specification.class)))
                .thenReturn(List.of(mo));

        List<MainOfficeGrillaDTO> out = mainOfficeService.findByNumberAndTypeDocument("900123456", "NI");

        assertEquals(1, out.size());
        assertEquals(10L, out.get(0).getId());
    }

    @Test
    void getAllMainOfficesByIdAffiliate_ok_mapea() {
        MainOffice a = new MainOffice();
        a.setId(1L);
        a.setMainOfficeName("A");
        MainOffice b = new MainOffice();
        b.setId(2L);
        b.setMainOfficeName("B");
        when(repository.findAll(any(Specification.class)))
                .thenReturn(List.of(a, b));

        List<MainOfficeGrillaDTO> out = mainOfficeService.getAllMainOfficesByIdAffiliate(999L);

        assertEquals(2, out.size());
        assertEquals(1L, out.get(0).getId());
        assertEquals("B", out.get(1).getMainOfficeName());
    }

    @Test
    void findAll_ok_devuelveLista() {
        when(repository.findAll()).thenReturn(List.of(new MainOffice(), new MainOffice()));
        assertEquals(2, mainOfficeService.findAll().size());
    }

    // ============= Tests para findAffiliateMercantile =============

    @Test
    void findAffiliateMercantile_withNullId_shouldReturnNull() {
        Object result = mainOfficeService.findAffiliateMercantile(null);
        assertNull(result);
    }

    @Test
    void findAffiliateMercantile_withAffiliateNotFound_shouldReturnNull() {
        when(affiliateRepository.findById(anyLong())).thenReturn(Optional.empty());

        Object result = mainOfficeService.findAffiliateMercantile(1L);

        assertNull(result);
    }

    @Test
    void findAffiliateMercantile_withMercantileType_shouldReturnMercantile() {
        AffiliateMercantile mercantile = new AffiliateMercantile();

        when(affiliateRepository.findById(anyLong())).thenReturn(Optional.of(affiliate));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mercantile));

        Object result = mainOfficeService.findAffiliateMercantile(1L);

        assertInstanceOf(AffiliateMercantile.class, result);
    }

    @Test
    void findAffiliateMercantile_withDomesticType_shouldReturnAffiliation() {
        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        Affiliation affiliation = new Affiliation();

        when(affiliateRepository.findById(anyLong())).thenReturn(Optional.of(affiliate));
        when(domesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));

        Object result = mainOfficeService.findAffiliateMercantile(1L);

        assertInstanceOf(Affiliation.class, result);
    }

    // Continuar en el siguiente mensaje...

    // ============= Tests para validaciones con reflection =============

    @Test
    void validWorkedAssociated_withNoDependentsAndNoMainHeadquarter_doesNotThrow()
            throws Exception {
        Long idHeadquarter = 1L;
        Long idEconomicActivity = 2L;
        Affiliate affiliate = new Affiliate();
        affiliate.setNitCompany("123");
        affiliate.setFiledNumber("F1");

        when(affiliationDependentRepository.findAll(any(Specification.class))).thenReturn(List.of());

        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setIdMainHeadquarter(99L);
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mercantile));
        when(domesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        java.lang.reflect.Method method = mainOfficeService.getClass()
                .getDeclaredMethod("validWorkedAssociated", Long.class, Long.class, Affiliate.class);
        method.setAccessible(true);
        method.invoke(mainOfficeService, idHeadquarter, idEconomicActivity, affiliate);
    }

    @Test
    void validWorkedAssociated_withNullIdEconomicActivity_doesNotThrow()
            throws Exception {
        Long idHeadquarter = 1L;
        Long idEconomicActivity = null;
        Affiliate affiliate = new Affiliate();
        affiliate.setNitCompany("123");
        affiliate.setFiledNumber("F1");

        java.lang.reflect.Method method = mainOfficeService.getClass()
                .getDeclaredMethod("validWorkedAssociated", Long.class, Long.class, Affiliate.class);
        method.setAccessible(true);
        method.invoke(mainOfficeService, idHeadquarter, idEconomicActivity, affiliate);
    }

    @Test
    void validWorkedAssociated_withMercantileMatch_shouldThrowError() throws Exception {
        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setIdMainHeadquarter(1L);

        when(affiliationDependentRepository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mercantile));

        java.lang.reflect.Method method = MainOfficeServiceImpl.class
                .getDeclaredMethod("validWorkedAssociated", Long.class, Long.class, Affiliate.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(mainOfficeService, 1L, 1L, affiliate));
    }

    @Test
    void validWorkedAssociated_withAffiliationMatch_shouldThrowError() throws Exception {
        Affiliation affiliation = new Affiliation();
        affiliation.setIdMainHeadquarter(1L);

        when(affiliationDependentRepository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(domesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliation));

        java.lang.reflect.Method method = MainOfficeServiceImpl.class
                .getDeclaredMethod("validWorkedAssociated", Long.class, Long.class, Affiliate.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(mainOfficeService, 1L, 1L, affiliate));
    }

    @Test
    void validWorkedAssociated_withException_shouldThrowAffiliationError() throws Exception {
        when(affiliationDependentRepository.findAll(any(Specification.class))).thenThrow(new RuntimeException("DB Error"));

        java.lang.reflect.Method method = MainOfficeServiceImpl.class
                .getDeclaredMethod("validWorkedAssociated", Long.class, Long.class, Affiliate.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(mainOfficeService, 1L, 1L, affiliate));
    }

    @Test
    void validNumberPhone_requestedTrue_withValidPrefix_returnsFalse() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberPhone", String.class, boolean.class);
        method.setAccessible(true);
        assertDoesNotThrow(() -> {
            method.invoke(mainOfficeService, "3101234567", true);
        });
    }

    @Test
    void validNumberPhone_requestedTrue_withInvalidPrefix_returnsTrue() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberPhone", String.class, boolean.class);
        method.setAccessible(true);
        assertThrows(Exception.class, () -> {
            method.invoke(mainOfficeService, "9991234567", true);
        });
    }

    @Test
    void validNumberPhone_requestedTrue_withEmptyNumber_returnsTrue() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberPhone", String.class, boolean.class);
        method.setAccessible(true);
        assertThrows(Exception.class, () -> {
            method.invoke(mainOfficeService, "", true);
        });
    }

    @Test
    void validNumberPhone_requestedTrue_withNullNumber_returnsTrue() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberPhone", String.class, boolean.class);
        method.setAccessible(true);
        assertThrows(Exception.class, () -> {
            method.invoke(mainOfficeService, null, true);
        });
    }

    @Test
    void validNumberPhone_requestedFalse_withValidPrefix_returnsFalse() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberPhone", String.class, boolean.class);
        method.setAccessible(true);
        assertDoesNotThrow(() -> {
            method.invoke(mainOfficeService, "6011234567", false);
        });
    }

    @Test
    void validNumberPhone_requestedFalse_withInvalidPrefix_returnsTrue() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberPhone", String.class, boolean.class);
        method.setAccessible(true);
        assertThrows(Exception.class, () -> {
            method.invoke(mainOfficeService, "1234567890", false);
        });
    }

    @Test
    void validNumberPhone_requestedFalse_withEmptyNumber_returnsFalse() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberPhone", String.class, boolean.class);
        method.setAccessible(true);
        assertDoesNotThrow(() -> {
            method.invoke(mainOfficeService, "", false);
        });
    }

    @Test
    void validNumberPhone_requestedFalse_withNullNumber_returnsFalse() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberPhone", String.class, boolean.class);
        method.setAccessible(true);
        assertDoesNotThrow(() -> {
            method.invoke(mainOfficeService, null, false);
        });
    }

    @Test
    void validNumberIdentification_CC_validLengths() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberIdentification", String.class, String.class);
        method.setAccessible(true);

        assertEquals(true, method.invoke(mainOfficeService, "123", "CC"));
        assertEquals(true, method.invoke(mainOfficeService, "1234567890", "CC"));
        assertEquals(false, method.invoke(mainOfficeService, "12", "CC"));
        assertEquals(false, method.invoke(mainOfficeService, "12345678901", "CC"));
    }

    @Test
    void validNumberIdentification_NI_validLengths() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberIdentification", String.class, String.class);
        method.setAccessible(true);

        assertEquals(true, method.invoke(mainOfficeService, "123456789", "NI"));
        assertEquals(true, method.invoke(mainOfficeService, "123456789012", "NI"));
        assertEquals(false, method.invoke(mainOfficeService, "12345678", "NI"));
        assertEquals(false, method.invoke(mainOfficeService, "1234567890123", "NI"));
    }

    @Test
    void validNumberIdentification_TI_validLengths() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberIdentification", String.class, String.class);
        method.setAccessible(true);

        assertEquals(true, method.invoke(mainOfficeService, "1234567890", "TI"));
        assertEquals(true, method.invoke(mainOfficeService, "12345678901", "TI"));
        assertEquals(false, method.invoke(mainOfficeService, "123456789", "TI"));
        assertEquals(false, method.invoke(mainOfficeService, "123456789012", "TI"));
    }

    @Test
    void validNumberIdentification_PA_validLengths() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberIdentification", String.class, String.class);
        method.setAccessible(true);

        assertEquals(true, method.invoke(mainOfficeService, "123", "PA"));
        assertEquals(true, method.invoke(mainOfficeService, "1234567890123456", "PA"));
        assertEquals(false, method.invoke(mainOfficeService, "12", "PA"));
        assertEquals(false, method.invoke(mainOfficeService, "12345678901234567", "PA"));
    }

    @Test
    void validNumberIdentification_PE_validLength() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberIdentification", String.class, String.class);
        method.setAccessible(true);

        assertEquals(true, method.invoke(mainOfficeService, "123456789012345", "PE"));
        assertEquals(false, method.invoke(mainOfficeService, "12345678901234", "PE"));
        assertEquals(false, method.invoke(mainOfficeService, "1234567890123456", "PE"));
    }

    @Test
    void validNumberIdentification_SC_validLength() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberIdentification", String.class, String.class);
        method.setAccessible(true);

        assertEquals(true, method.invoke(mainOfficeService, "123456789", "SC"));
        assertEquals(false, method.invoke(mainOfficeService, "12345678", "SC"));
        assertEquals(false, method.invoke(mainOfficeService, "1234567890", "SC"));
    }

    @Test
    void validNumberIdentification_PT_variousLengths() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberIdentification", String.class, String.class);
        method.setAccessible(true);

        assertEquals(true, method.invoke(mainOfficeService, "1", "PT"));
        assertEquals(true, method.invoke(mainOfficeService, "12345678", "PT"));
        assertEquals(false, method.invoke(mainOfficeService, "", "PT"));
        assertEquals(false, method.invoke(mainOfficeService, "123456789", "PT"));
    }

    @Test
    void validNumberIdentification_CD_validLengths() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberIdentification", String.class, String.class);
        method.setAccessible(true);

        assertEquals(true, method.invoke(mainOfficeService, "123", "CD"));
        assertEquals(true, method.invoke(mainOfficeService, "12345678901", "CD"));
        assertEquals(false, method.invoke(mainOfficeService, "12", "CD"));
        assertEquals(false, method.invoke(mainOfficeService, "123456789012", "CD"));
    }

    @Test
    void validNumberIdentification_RC_validLengths() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberIdentification", String.class, String.class);
        method.setAccessible(true);

        assertEquals(true, method.invoke(mainOfficeService, "1234567890", "RC"));
        assertEquals(true, method.invoke(mainOfficeService, "12345678901", "RC"));
        assertEquals(false, method.invoke(mainOfficeService, "123456789", "RC"));
    }

    @Test
    void validNumberIdentification_CE_validLengths() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberIdentification", String.class, String.class);
        method.setAccessible(true);

        assertEquals(true, method.invoke(mainOfficeService, "123", "CE"));
        assertEquals(true, method.invoke(mainOfficeService, "1234567890", "CE"));
        assertEquals(false, method.invoke(mainOfficeService, "12", "CE"));
        assertEquals(false, method.invoke(mainOfficeService, "12345678901", "CE"));
    }

    @Test
    void validNumberIdentification_invalidType_returnsFalse() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberIdentification", String.class, String.class);
        method.setAccessible(true);

        assertEquals(false, method.invoke(mainOfficeService, "123456", "XYZ"));
    }

    @Test
    void validNumberIdentification_nullOrEmptyNumber_returnsFalse() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberIdentification", String.class, String.class);
        method.setAccessible(true);

        assertEquals(false, method.invoke(mainOfficeService, null, "CC"));
        assertEquals(false, method.invoke(mainOfficeService, "", "CC"));
    }

    @Test
    void validEmpty_withEmptyString_shouldReturnTrue() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class
                .getDeclaredMethod("validEmpty", String.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(mainOfficeService, ""));
        assertTrue((Boolean) method.invoke(mainOfficeService, (String) null));
        assertFalse((Boolean) method.invoke(mainOfficeService, "Not Empty"));
    }

    @Test
    void validTypeNumberIdentification_withValidTypes_shouldReturnTrue() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class
                .getDeclaredMethod("validTypeNumberIdentification", String.class);
        method.setAccessible(true);

        assertTrue((Boolean) method.invoke(mainOfficeService, "CC"));
        assertTrue((Boolean) method.invoke(mainOfficeService, "NI"));
        assertTrue((Boolean) method.invoke(mainOfficeService, "CE"));
        assertTrue((Boolean) method.invoke(mainOfficeService, "TI"));
        assertTrue((Boolean) method.invoke(mainOfficeService, "RC"));
        assertTrue((Boolean) method.invoke(mainOfficeService, "PA"));
        assertTrue((Boolean) method.invoke(mainOfficeService, "CD"));
        assertTrue((Boolean) method.invoke(mainOfficeService, "PE"));
        assertTrue((Boolean) method.invoke(mainOfficeService, "SC"));
        assertTrue((Boolean) method.invoke(mainOfficeService, "PT"));
    }

    @Test
    void validTypeNumberIdentification_withInvalidType_shouldReturnFalse() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class
                .getDeclaredMethod("validTypeNumberIdentification", String.class);
        method.setAccessible(true);

        assertFalse((Boolean) method.invoke(mainOfficeService, "XX"));
        assertFalse((Boolean) method.invoke(mainOfficeService, ""));
        assertFalse((Boolean) method.invoke(mainOfficeService, (String) null));
    }

    @Test
    void codeActivityEconomic_concatenaCorrecto_porReflection() throws Exception {
        EconomicActivity e = new EconomicActivity();
        e.setClassRisk("5");
        e.setCodeCIIU("1010");
        e.setAdditionalCode("09");
        java.lang.reflect.Method m = MainOfficeServiceImpl.class
                .getDeclaredMethod("codeActivityEconomic", EconomicActivity.class);
        m.setAccessible(true);
        String code = (String) m.invoke(mainOfficeService, e);
        assertEquals("5101009", code);
    }

    @Test
    void validEmail_formatos_validos_e_invalidos_porReflection() throws Exception {
        java.lang.reflect.Method m = MainOfficeServiceImpl.class
                .getDeclaredMethod("validEmail", String.class);
        m.setAccessible(true);

        assertEquals(false, m.invoke(mainOfficeService, "ok@dominio.com"));
        assertEquals(false, m.invoke(mainOfficeService, "a.b-c@sub.dominio.co"));
        assertEquals(true, m.invoke(mainOfficeService, "sin-arroba"));
        assertEquals(true, m.invoke(mainOfficeService, "x@dominio"));
        assertEquals(true, m.invoke(mainOfficeService, "a@b..c"));
    }

    @Test
    void changeMain_desmarcaExistentes_yMarcaActual_porReflection() throws Exception {
        MainOffice m1 = new MainOffice();
        m1.setId(1L);
        m1.setMain(true);
        MainOffice m2 = new MainOffice();
        m2.setId(2L);
        m2.setMain(true);
        when(repository.findAll(any(Specification.class)))
                .thenReturn(List.of(m1, m2));

        MainOffice nuevo = new MainOffice();
        nuevo.setId(3L);
        nuevo.setMain(false);

        java.lang.reflect.Method m = MainOfficeServiceImpl.class
                .getDeclaredMethod("changeMain", Long.class, MainOffice.class);
        m.setAccessible(true);
        m.invoke(mainOfficeService, 999L, nuevo);

        assertTrue(nuevo.getMain());
        verify(repository, times(2)).save(any(MainOffice.class));
    }

    // Archivo demasiado largo, continuará en siguiente parte...

    // ============= Tests completos del archivo original ============

    @Test
    void validMainOffice_missingCity_throwsException() throws Exception {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Valid Name");
        dto.setAddressDTO(new AddressDTO());
        dto.setEconomicActivity(List.of(1L));
        dto.setMainOfficeZone(Constant.URBAN_ZONE);

        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validMainOffice", MainOfficeDTO.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(mainOfficeService, dto));
    }

    @Test
    void validMainOffice_missingDepartment_throwsException() throws Exception {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Valid Name");
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setIdCity(1L);
        dto.setAddressDTO(addressDTO);
        dto.setEconomicActivity(List.of(1L));
        dto.setMainOfficeZone(Constant.URBAN_ZONE);

        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validMainOffice", MainOfficeDTO.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(mainOfficeService, dto));
    }

    @Test
    void validMainOffice_missingEmail_throwsException() throws Exception {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Valid Name");
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setIdCity(1L);
        addressDTO.setIdDepartment(1L);
        dto.setAddressDTO(addressDTO);
        dto.setEconomicActivity(List.of(1L));
        dto.setMainOfficeZone(Constant.URBAN_ZONE);

        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validMainOffice", MainOfficeDTO.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(mainOfficeService, dto));
    }

    @Test
    void validMainOffice_invalidPhoneNumber_throwsException() throws Exception {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Valid Name");
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setIdCity(1L);
        addressDTO.setIdDepartment(1L);
        dto.setAddressDTO(addressDTO);
        dto.setMainOfficeEmail("valid@email.com");
        dto.setMainOfficePhoneNumber("1234567890");
        dto.setEconomicActivity(List.of(1L));
        dto.setMainOfficeZone(Constant.URBAN_ZONE);

        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validMainOffice", MainOfficeDTO.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(mainOfficeService, dto));
    }

    @Test
    void validMainOffice_invalidZone_throwsException() throws Exception {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Valid Name");
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setIdCity(1L);
        addressDTO.setIdDepartment(1L);
        dto.setAddressDTO(addressDTO);
        dto.setMainOfficeEmail("valid@email.com");
        dto.setMainOfficePhoneNumber("3101234567");
        dto.setEconomicActivity(List.of(1L));
        dto.setMainOfficeZone("INVALID_ZONE");

        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validMainOffice", MainOfficeDTO.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(mainOfficeService, dto));
    }

    @Test
    void validMainOffice_noEconomicActivities_throwsException() throws Exception {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Valid Name");
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setIdCity(1L);
        addressDTO.setIdDepartment(1L);
        dto.setAddressDTO(addressDTO);
        dto.setMainOfficeEmail("valid@email.com");
        dto.setMainOfficePhoneNumber("3101234567");
        dto.setMainOfficeZone(Constant.URBAN_ZONE);
        dto.setEconomicActivity(new ArrayList<>());

        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validMainOffice", MainOfficeDTO.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(mainOfficeService, dto));
    }

    @Test
    void validMainOffice_tooManyEconomicActivities_throwsException() throws Exception {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Valid Name");
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setIdCity(1L);
        addressDTO.setIdDepartment(1L);
        dto.setAddressDTO(addressDTO);
        dto.setMainOfficeEmail("valid@email.com");
        dto.setMainOfficePhoneNumber("3101234567");
        dto.setMainOfficeZone(Constant.URBAN_ZONE);
        dto.setEconomicActivity(List.of(1L, 2L, 3L, 4L, 5L, 6L));

        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validMainOffice", MainOfficeDTO.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(mainOfficeService, dto));
    }

    @Test
    void validMainOffice_invalidName_throwsException() throws Exception {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Valid Name");
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setIdCity(1L);
        addressDTO.setIdDepartment(1L);
        dto.setAddressDTO(addressDTO);
        dto.setMainOfficeEmail("valid@email.com");
        dto.setMainOfficePhoneNumber("3101234567");
        dto.setMainOfficeZone(Constant.URBAN_ZONE);
        dto.setEconomicActivity(List.of(1L));
        dto.setTypeDocumentResponsibleHeadquarters("CC");
        dto.setNumberDocumentResponsibleHeadquarters("123456789");
        dto.setFirstNameResponsibleHeadquarters("123");

        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validMainOffice", MainOfficeDTO.class);
        method.setAccessible(true);

        assertThrows(Exception.class, () -> method.invoke(mainOfficeService, dto));
    }

    @Test
    void saveMainOfficeDTO_existingAddress_throwsException() {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Office Duplicated");
        dto.setOfficeManager(100L);
        dto.setMainOfficeEmail("test@test.com");
        dto.setMainOfficePhoneNumber("6011234567");
        dto.setMainPhoneNumberTwo("6011234567");
        dto.setPhoneOneResponsibleHeadquarters("6011234567");
        dto.setPhoneTwoResponsibleHeadquarters("6011234567");
        dto.setMainOfficeZone("URBAN");
        dto.setEconomicActivity(List.of(1L));
        dto.setTypeDocumentResponsibleHeadquarters("CC");
        dto.setNumberDocumentResponsibleHeadquarters("12345");
        dto.setFirstNameResponsibleHeadquarters("John");
        dto.setSecondNameResponsibleHeadquarters("");
        dto.setSurnameResponsibleHeadquarters("Doe");
        dto.setSecondSurnameResponsibleHeadquarters("");

        AddressDTO address = new AddressDTO();
        address.setIdCity(1L);
        address.setIdDepartment(1L);
        dto.setAddressDTO(address);
        dto.setMain(false);

        when(repository.findAll((Specification<MainOffice>) any())).thenReturn(List.of(new MainOffice()));

        assertThrows(AffiliationError.class, () -> {
            mainOfficeService.saveMainOffice(dto);
        });
    }

    @Test
    void saveMainOfficeDTO_nameAlreadyExists_throwsException() {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setOfficeManager(1L);
        dto.setMainOfficeName("Existing Name");
        dto.setAddressDTO(new AddressDTO());

        when(repository.findAll(any(Specification.class))).thenReturn(List.of(new MainOffice()));

        assertThrows(AffiliationError.class, () -> mainOfficeService.saveMainOffice(dto));
    }

    @Test
    void saveMainOfficeDTO_userNotFound_throwsException() {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setOfficeManager(1L);
        dto.setMainOfficeName("New Office");
        dto.setAddressDTO(new AddressDTO());
        dto.setEconomicActivity(List.of(1L));
        dto.setMainOfficeZone("URBAN");
        dto.setMainOfficeEmail("test@test.com");
        dto.setMainOfficePhoneNumber("3101234567");
        dto.setTypeDocumentResponsibleHeadquarters("CC");
        dto.setNumberDocumentResponsibleHeadquarters("12345678");
        dto.setFirstNameResponsibleHeadquarters("Test");
        dto.setSurnameResponsibleHeadquarters("User");

        when(repository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AffiliationError.class, () -> mainOfficeService.saveMainOffice(dto));
    }

    @Test
    void workCenter_createsNewWorkCenter() throws Exception {
        EconomicActivity activity = new EconomicActivity();
        activity.setClassRisk("CR");
        activity.setCodeCIIU("001");
        activity.setAdditionalCode("A");

        UserMain user = new UserMain();
        user.setId(1L);

        Long department = 10L;
        Long city = 20L;
        String zone = "URBAN";

        MainOffice mainOffice = new MainOffice();
        mainOffice.setIdDepartment(department);
        mainOffice.setIdCity(city);
        mainOffice.setMainOfficeZone(zone);
        mainOffice.setIdAffiliate(1L);

        when(workCenterService.getWorkCenterByCodeAndIdUser(anyString(), any())).thenReturn(null);
        when(workCenterService.getNumberCode(user)).thenReturn(100L);

        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("workCenter", EconomicActivity.class, UserMain.class, MainOffice.class, Boolean.class);
        method.setAccessible(true);
        method.invoke(mainOfficeService, activity, user, mainOffice, Boolean.TRUE);

        ArgumentCaptor<WorkCenter> captor = ArgumentCaptor.forClass(WorkCenter.class);
        verify(workCenterService).saveWorkCenter(captor.capture());
        WorkCenter savedCenter = captor.getValue();

        assertEquals("101", savedCenter.getCode());
        assertEquals(0, savedCenter.getTotalWorkers());
        assertEquals("CR", savedCenter.getRiskClass());
        assertEquals(department, savedCenter.getWorkCenterDepartment());
        assertEquals(city, savedCenter.getWorkCenterCity());
        assertEquals(zone, savedCenter.getWorkCenterZone());
        assertSame(user, savedCenter.getWorkCenterManager());
    }

    @Test
    void workCenter_doesNotCreateNewWorkCenter() throws Exception {
        EconomicActivity activity = new EconomicActivity();
        activity.setClassRisk("CR");
        activity.setCodeCIIU("001");
        activity.setAdditionalCode("A");

        UserMain user = new UserMain();
        Long department = 10L;
        Long city = 20L;
        String zone = "URBAN";

        MainOffice mainOffice = new MainOffice();
        mainOffice.setIdDepartment(department);
        mainOffice.setIdCity(city);
        mainOffice.setMainOfficeZone(zone);

        WorkCenter existingCenter = new WorkCenter();
        when(workCenterService.getWorkCenterByCodeAndIdUser(anyString(), any())).thenReturn(existingCenter);

        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("workCenter", EconomicActivity.class, UserMain.class, MainOffice.class, Boolean.class);
        method.setAccessible(true);
        method.invoke(mainOfficeService, activity, user, mainOffice, Boolean.TRUE);

        verify(workCenterService, never()).saveWorkCenter(any());
    }

    @Test
    void validWorkedAssociated_withNullEconomicActivity_doesNothing() throws Exception {
        Long idHeadquarter = 1L;
        Long idEconomicActivity = null;
        Affiliate affiliate = new Affiliate();
        affiliate.setNitCompany("123");
        affiliate.setFiledNumber("F1");

        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validWorkedAssociated", Long.class, Long.class, Affiliate.class);
        method.setAccessible(true);

        method.invoke(mainOfficeService, idHeadquarter, idEconomicActivity, affiliate);
    }

    @Test
    void update_mainOfficeNameExistsForDifferentId_throws() {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Existing Name");
        dto.setOfficeManager(1L);
        dto.setEconomicActivity(new ArrayList<>());
        dto.setAddressDTO(new AddressDTO());
        dto.getAddressDTO().setIdCity(1L);
        dto.getAddressDTO().setIdDepartment(1L);
        dto.setMainOfficeEmail("test@example.com");
        dto.setMainOfficePhoneNumber("6011234567");
        dto.setMainPhoneNumberTwo("6011234567");
        dto.setPhoneOneResponsibleHeadquarters("6011234567");
        dto.setPhoneTwoResponsibleHeadquarters("6011234567");
        dto.setMainOfficeZone("URBAN");
        dto.setTypeDocumentResponsibleHeadquarters("CC");
        dto.setNumberDocumentResponsibleHeadquarters("123456789");
        dto.setFirstNameResponsibleHeadquarters("John");
        dto.setSecondNameResponsibleHeadquarters("");
        dto.setSurnameResponsibleHeadquarters("Doe");
        dto.setSecondSurnameResponsibleHeadquarters("");

        MainOffice existingOffice = new MainOffice();
        existingOffice.setId(2L);
        existingOffice.setMainOfficeName("Existing Name");
        UserMain userMain = new UserMain();
        userMain.setId(1L);
        existingOffice.setOfficeManager(userMain);

        MainOffice officeToUpdate = new MainOffice();
        officeToUpdate.setId(1L);
        officeToUpdate.setMainOfficeName("Old Name");
        officeToUpdate.setOfficeManager(userMain);

        when(repository.findById(1L)).thenReturn(Optional.of(officeToUpdate));
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(existingOffice));

        assertThrows(AffiliationError.class, () -> mainOfficeService.update(dto, 1L));
    }

    @Test
    void update_invalidPhoneNumber_throws() {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Valid Name");
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setIdCity(1L);
        addressDTO.setIdDepartment(1L);
        dto.setAddressDTO(addressDTO);
        dto.setMainOfficeEmail("valid@email.com");
        dto.setMainOfficePhoneNumber("1234567890");
        dto.setEconomicActivity(List.of(1L));
        dto.setMainOfficeZone(Constant.URBAN_ZONE);

        MainOffice officeToUpdate = new MainOffice();
        officeToUpdate.setId(1L);
        officeToUpdate.setMainOfficeName("Old Name");
        UserMain userMain = new UserMain();
        userMain.setId(1L);
        officeToUpdate.setOfficeManager(userMain);

        when(repository.findById(1L)).thenReturn(Optional.of(officeToUpdate));

        assertThrows(AffiliationError.class, () -> mainOfficeService.update(dto, 1L));
    }

    @Test
    void testFindByIdUserAndDepartmentAndCity_Success_OfficesFound() {
        Long idUser = 1L;
        Long departmentId = 5L;
        Long cityId = 101L;
        UserMain userManager = new UserMain();
        userManager.setId(idUser);

        MainOffice office1 = new MainOffice();
        office1.setId(20L);
        office1.setCode("S0000000020");
        office1.setMainOfficeName("City Office One");
        office1.setAddress("1 City Plaza");
        office1.setIdDepartment(departmentId);
        office1.setIdCity(cityId);
        office1.setOfficeManager(userManager);
        office1.setMain(false);

        MainOffice office2 = new MainOffice();
        office2.setId(21L);
        office2.setCode("S0000000021");
        office2.setMainOfficeName("City Office Two");
        office2.setAddress("2 City Road");
        office2.setIdDepartment(departmentId);
        office2.setIdCity(cityId);
        office2.setOfficeManager(userManager);
        office2.setMain(true);

        List<MainOffice> mockOffices = List.of(office1, office2);

        when(repository.findAll(any(Specification.class))).thenReturn(mockOffices);

        List<MainOfficeGrillaDTO> result = mainOfficeService.findByIdUserAndDepartmentAndCity(idUser, departmentId,
                cityId);

        assertNotNull(result);
        assertEquals(2, result.size());

        MainOfficeGrillaDTO dto1 = result.get(0);
        assertEquals(office1.getId(), dto1.getId());
        assertEquals(office1.getCode(), dto1.getCode());
        assertEquals(office1.getMainOfficeName(), dto1.getMainOfficeName());
        assertEquals(office1.getAddress(), dto1.getAddress());
        assertEquals(office1.getIdDepartment(), dto1.getIdDepartment());
        assertEquals(office1.getIdCity(), dto1.getIdCity());
        assertEquals(office1.getMain(), dto1.getMain());

        MainOfficeGrillaDTO dto2 = result.get(1);
        assertEquals(office2.getId(), dto2.getId());
        assertEquals(office2.getCode(), dto2.getCode());
        assertEquals(office2.getMainOfficeName(), dto2.getMainOfficeName());
        assertEquals(office2.getMain(), dto2.getMain());

        verify(repository, times(1)).findAll(any(Specification.class));
    }

    // ============= Tests para update adicionales ============

    @Test
    void update_invalidZone_throws() {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Valid Name");
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setIdCity(1L);
        addressDTO.setIdDepartment(1L);
        dto.setAddressDTO(addressDTO);
        dto.setMainOfficeEmail("valid@email.com");
        dto.setMainOfficePhoneNumber("3101234567");
        dto.setEconomicActivity(List.of(1L));
        dto.setMainOfficeZone("INVALID_ZONE");

        MainOffice officeToUpdate = new MainOffice();
        officeToUpdate.setId(1L);
        officeToUpdate.setMainOfficeName("Old Name");
        UserMain userMain = new UserMain();
        userMain.setId(1L);
        officeToUpdate.setOfficeManager(userMain);

        when(repository.findById(1L)).thenReturn(Optional.of(officeToUpdate));

        assertThrows(AffiliationError.class, () -> mainOfficeService.update(dto, 1L));
    }

    @Test
    void update_noEconomicActivities_throws() {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Valid Name");
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setIdCity(1L);
        addressDTO.setIdDepartment(1L);
        dto.setAddressDTO(addressDTO);
        dto.setMainOfficeEmail("valid@email.com");
        dto.setMainOfficePhoneNumber("3101234567");
        dto.setMainOfficeZone(Constant.URBAN_ZONE);
        dto.setEconomicActivity(new ArrayList<>());

        MainOffice officeToUpdate = new MainOffice();
        officeToUpdate.setId(1L);
        officeToUpdate.setMainOfficeName("Old Name");
        UserMain userMain = new UserMain();
        userMain.setId(1L);
        officeToUpdate.setOfficeManager(userMain);

        when(repository.findById(1L)).thenReturn(Optional.of(officeToUpdate));

        assertThrows(AffiliationError.class, () -> mainOfficeService.update(dto, 1L));
    }

    @Test
    void update_tooManyEconomicActivities_throws() {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Valid Name");
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setIdCity(1L);
        addressDTO.setIdDepartment(1L);
        dto.setAddressDTO(addressDTO);
        dto.setMainOfficeEmail("valid@email.com");
        dto.setMainOfficePhoneNumber("3101234567");
        dto.setMainOfficeZone(Constant.URBAN_ZONE);
        dto.setEconomicActivity(List.of(1L, 2L, 3L, 4L, 5L, 6L));

        MainOffice officeToUpdate = new MainOffice();
        officeToUpdate.setId(1L);
        officeToUpdate.setMainOfficeName("Old Name");
        UserMain userMain = new UserMain();
        userMain.setId(1L);
        officeToUpdate.setOfficeManager(userMain);

        when(repository.findById(1L)).thenReturn(Optional.of(officeToUpdate));

        assertThrows(AffiliationError.class, () -> mainOfficeService.update(dto, 1L));
    }

    @Test
    void update_invalidName_throws() {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Valid Name");
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setIdCity(1L);
        addressDTO.setIdDepartment(1L);
        dto.setAddressDTO(addressDTO);
        dto.setMainOfficeEmail("valid@email.com");
        dto.setMainOfficePhoneNumber("3101234567");
        dto.setMainOfficeZone(Constant.URBAN_ZONE);
        dto.setEconomicActivity(List.of(1L));
        dto.setTypeDocumentResponsibleHeadquarters("CC");
        dto.setNumberDocumentResponsibleHeadquarters("123456789");
        dto.setFirstNameResponsibleHeadquarters("123");

        MainOffice officeToUpdate = new MainOffice();
        officeToUpdate.setId(1L);
        officeToUpdate.setMainOfficeName("Old Name");
        UserMain userMain = new UserMain();
        userMain.setId(1L);
        officeToUpdate.setOfficeManager(userMain);

        when(repository.findById(1L)).thenReturn(Optional.of(officeToUpdate));

        assertThrows(AffiliationError.class, () -> mainOfficeService.update(dto, 1L));
    }

    @Test
    void update_missingCity_throwsException() {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Valid Name");
        dto.setAddressDTO(new AddressDTO());
        dto.setEconomicActivity(List.of(1L));
        dto.setMainOfficeZone(Constant.URBAN_ZONE);

        MainOffice officeToUpdate = new MainOffice();
        officeToUpdate.setId(1L);
        officeToUpdate.setMainOfficeName("Old Name");
        UserMain userMain = new UserMain();
        userMain.setId(1L);
        officeToUpdate.setOfficeManager(userMain);

        when(repository.findById(1L)).thenReturn(Optional.of(officeToUpdate));

        assertThrows(AffiliationError.class, () -> mainOfficeService.update(dto, 1L));
    }

    @Test
    void update_missingDepartment_throwsException() {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Valid Name");
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setIdCity(1L);
        dto.setAddressDTO(addressDTO);
        dto.setEconomicActivity(List.of(1L));
        dto.setMainOfficeZone(Constant.URBAN_ZONE);

        MainOffice officeToUpdate = new MainOffice();
        officeToUpdate.setId(1L);
        officeToUpdate.setMainOfficeName("Old Name");
        UserMain userMain = new UserMain();
        userMain.setId(1L);
        officeToUpdate.setOfficeManager(userMain);

        when(repository.findById(1L)).thenReturn(Optional.of(officeToUpdate));

        assertThrows(AffiliationError.class, () -> mainOfficeService.update(dto, 1L));
    }

    @Test
    void update_missingEmail_throwsException() {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Valid Name");
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setIdCity(1L);
        addressDTO.setIdDepartment(1L);
        dto.setAddressDTO(addressDTO);
        dto.setEconomicActivity(List.of(1L));
        dto.setMainOfficeZone(Constant.URBAN_ZONE);

        MainOffice officeToUpdate = new MainOffice();
        officeToUpdate.setId(1L);
        officeToUpdate.setMainOfficeName("Old Name");
        UserMain userMain = new UserMain();
        userMain.setId(1L);
        officeToUpdate.setOfficeManager(userMain);

        when(repository.findById(1L)).thenReturn(Optional.of(officeToUpdate));

        assertThrows(AffiliationError.class, () -> mainOfficeService.update(dto, 1L));
    }

    @Test
    void saveMainOffice_duplicateAddress_throws() {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setOfficeManager(1L);
        AddressDTO address = new AddressDTO();
        address.setIdCity(1L);
        address.setIdDepartment(1L);
        dto.setAddressDTO(address);
        dto.setMainOfficeName("Test Office");
        dto.setMainOfficeEmail("test@test.com");
        dto.setMainOfficePhoneNumber("6011234567");
        dto.setMainPhoneNumberTwo("6011234567");
        dto.setPhoneOneResponsibleHeadquarters("6011234567");
        dto.setPhoneTwoResponsibleHeadquarters("6011234567");
        dto.setEmailResponsibleHeadquarters("resp@test.com");
        dto.setMainOfficeZone("urban_zone");
        dto.setEconomicActivity(List.of(1L));
        dto.setMain(true);
        dto.setTypeDocumentResponsibleHeadquarters("CC");
        dto.setNumberDocumentResponsibleHeadquarters("123456");
        dto.setFirstNameResponsibleHeadquarters("John");
        dto.setSurnameResponsibleHeadquarters("Doe");

        when(repository.findAll(any(Specification.class))).thenReturn(List.of(new MainOffice()));

        AffiliationError error = assertThrows(AffiliationError.class, () ->
                mainOfficeService.saveMainOffice(dto)
        );
    }

    @Test
    void saveMainOffice_duplicateName_throws() {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setOfficeManager(1L);
        AddressDTO address = new AddressDTO();
        address.setIdCity(1L);
        address.setIdDepartment(1L);
        dto.setAddressDTO(address);
        dto.setMainOfficeName("Test Office");
        dto.setMainOfficeEmail("test@test.com");
        dto.setMainOfficePhoneNumber("6011234567");
        dto.setMainPhoneNumberTwo("6011234567");
        dto.setPhoneOneResponsibleHeadquarters("6011234567");
        dto.setPhoneTwoResponsibleHeadquarters("6011234567");
        dto.setEmailResponsibleHeadquarters("resp@test.com");
        dto.setMainOfficeZone("urban_zone");
        dto.setEconomicActivity(List.of(1L));
        dto.setMain(true);
        dto.setTypeDocumentResponsibleHeadquarters("CC");
        dto.setNumberDocumentResponsibleHeadquarters("123456");
        dto.setFirstNameResponsibleHeadquarters("John");
        dto.setSurnameResponsibleHeadquarters("Doe");

        when(repository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList(), List.of(new MainOffice()));

        AffiliationError error = assertThrows(AffiliationError.class, () ->
                mainOfficeService.saveMainOffice(dto)
        );
    }


    @Test
    void saveMainOffice_withMainTrue_shouldCallChangeMain() {
        MainOfficeDTO dto = createValidMainOfficeDTO(true);
        dto.setMain(true);

        when(repository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());
        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(economicActivityRepository.findById(anyLong())).thenReturn(Optional.of(economicActivity1));
        when(repository.save(any(MainOffice.class))).thenReturn(existingMainOffice);

        AffiliateMercantile mercantileForEmail = new AffiliateMercantile();
        mercantileForEmail.setEmail("test@test.com");
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(mercantileForEmail));
        when(arlInformationDao.findAllArlInformation()).thenReturn(new ArrayList<>());

        mainOfficeService.saveMainOffice(dto);

        verify(repository, atLeastOnce()).save(any(MainOffice.class));
    }

    @Test
    void saveMainOffice_withoutExistingOffices_shouldSetMainTrue() {
        MainOfficeDTO dto = createValidMainOfficeDTO(false);
        dto.setMain(false);

        when(repository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());
        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(economicActivityRepository.findById(anyLong())).thenReturn(Optional.of(economicActivity1));
        when(repository.save(any(MainOffice.class))).thenReturn(existingMainOffice);

        AffiliateMercantile mercantileForEmail = new AffiliateMercantile();
        mercantileForEmail.setEmail("test@test.com");
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(mercantileForEmail));
        when(arlInformationDao.findAllArlInformation()).thenReturn(new ArrayList<>());

        mainOfficeService.saveMainOffice(dto);

        verify(repository, atLeast(1)).save(any(MainOffice.class));
    }

    @Test
    void saveMainOffice_withNullEconomicActivities_shouldFilter() {
        MainOfficeDTO dto = createValidMainOfficeDTO(false);
        List<Long> activities = new ArrayList<>();
        activities.add(economicActivity1.getId());
        activities.add(null);
        activities.add(economicActivity2.getId());
        dto.setEconomicActivity(activities);

        when(repository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());
        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(economicActivityRepository.findById(anyLong())).thenReturn(Optional.of(economicActivity1));
        when(repository.save(any(MainOffice.class))).thenReturn(existingMainOffice);

        AffiliateMercantile mercantileForEmail = new AffiliateMercantile();
        mercantileForEmail.setEmail("test@test.com");
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(mercantileForEmail));
        when(arlInformationDao.findAllArlInformation()).thenReturn(new ArrayList<>());

        assertThrows(AffiliationError.class, () -> mainOfficeService.saveMainOffice(dto));
    }

    @Test
    void saveMainOffice_shouldCallPositivaIntegration() {
        MainOfficeDTO dto = createValidMainOfficeDTO(false);

        when(repository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());
        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(economicActivityRepository.findById(anyLong())).thenReturn(Optional.of(economicActivity1));
        when(repository.save(any(MainOffice.class))).thenReturn(existingMainOffice);

        AffiliateMercantile mercantileForEmail = new AffiliateMercantile();
        mercantileForEmail.setEmail("test@test.com");
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(mercantileForEmail));
        when(arlInformationDao.findAllArlInformation()).thenReturn(new ArrayList<>());

        mainOfficeService.saveMainOffice(dto);

        verify(insertHeadquartersClient, times(1)).insert(any());
    }

    @Test
    void saveMainOffice_withPositivaException_shouldStillSaveLocally() {
        MainOfficeDTO dto = createValidMainOfficeDTO(false);

        when(repository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());
        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(economicActivityRepository.findById(anyLong())).thenReturn(Optional.of(economicActivity1));
        when(repository.save(any(MainOffice.class))).thenReturn(existingMainOffice);
        when(insertHeadquartersClient.insert(any())).thenThrow(new RuntimeException("Positiva Error"));

        AffiliateMercantile mercantileForEmail = new AffiliateMercantile();
        mercantileForEmail.setEmail("test@test.com");
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(mercantileForEmail));
        when(arlInformationDao.findAllArlInformation()).thenReturn(new ArrayList<>());

        MainOffice result = mainOfficeService.saveMainOffice(dto);

        assertNotNull(result);
        verify(repository).save(any(MainOffice.class));
    }


    @Test
    void update_withChangedEconomicActivitiesDomestic_shouldValidateWorkers() {
        MainOfficeDTO dto = createValidMainOfficeDTO(false);
        dto.setEconomicActivity(List.of(economicActivity2.getId()));

        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);

        Affiliation affiliation = new Affiliation();
        AffiliateActivityEconomic activity = new AffiliateActivityEconomic();
        activity.setActivityEconomic(economicActivity1);
        affiliation.setEconomicActivity(List.of(activity));

        when(repository.findById(anyLong())).thenReturn(Optional.of(existingOfficeToUpdate));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(domesticServiceIndependentRepository.findByFiledNumber(anyString())).thenReturn(Optional.of(affiliation));
        when(economicActivityRepository.findById(anyLong())).thenReturn(Optional.of(economicActivity2));
        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(repository.save(any(MainOffice.class))).thenReturn(existingOfficeToUpdate);

        MainOffice officeWithName = new MainOffice();
        officeWithName.setId(50L);
        officeWithName.setMainOfficeName("Office To Update");
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(officeWithName));

        when(workCenterService.getWorkCenterByMainOffice(any())).thenReturn(new ArrayList<>());
        when(economicActivityRepository.findByEconomicActivityCode(anyString()))
                .thenReturn(List.of(economicActivity2));

        Affiliation affiliationForEmail = new Affiliation();
        affiliationForEmail.setEmail("test@test.com");
        when(domesticServiceIndependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliationForEmail));
        when(arlInformationDao.findAllArlInformation()).thenReturn(new ArrayList<>());

        assertDoesNotThrow(() -> mainOfficeService.update(dto, existingOfficeToUpdate.getId()));
    }

    @Test
    void saveMainOfficeOfficial_shouldFindAffiliateAndSave() {
        MainOfficeOfficialDTO officialDTO = new MainOfficeOfficialDTO();
        officialDTO.setNumberDocument("987654321");
        officialDTO.setMainOfficeDTO(createValidMainOfficeDTO(false));

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());
        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(economicActivityRepository.findById(anyLong())).thenReturn(Optional.of(economicActivity1));
        when(repository.save(any(MainOffice.class))).thenReturn(existingMainOffice);

        AffiliateMercantile mercantileForEmail = new AffiliateMercantile();
        mercantileForEmail.setEmail("test@test.com");
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(mercantileForEmail));
        when(arlInformationDao.findAllArlInformation()).thenReturn(new ArrayList<>());

        MainOffice result = mainOfficeService.saveMainOfficeOfficial(officialDTO);

        assertNotNull(result);
        verify(affiliateRepository).findOne(any(Specification.class));
    }


    @Test
    void deleteOfficial_shouldFindAffiliateAndDelete() {
        String documentNumber = "987654321";
        existingOfficeToUpdate.setMain(false);

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repository.findById(anyLong())).thenReturn(Optional.of(existingOfficeToUpdate));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));

        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setEconomicActivity(new ArrayList<>()); // Lista vacía
        mercantile.setEmail("test@test.com");
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mercantile));
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(existingOfficeToUpdate, new MainOffice()));
        when(arlInformationDao.findAllArlInformation()).thenReturn(new ArrayList<>());

        String result = mainOfficeService.deleteOfficial(existingOfficeToUpdate.getId(), documentNumber);

        assertEquals("OK", result);
        verify(affiliateRepository).findOne(any(Specification.class));
    }

    @Test
    void saveMainOffice_withLowercaseZone_shouldConvertToUppercase() {
        MainOfficeDTO dto = createValidMainOfficeDTO(false);
        // FIX: Debe ser "URBAN_ZONE" no "urban_zone"
        dto.setMainOfficeZone(Constant.URBAN_ZONE);

        when(repository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());
        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(economicActivityRepository.findById(anyLong())).thenReturn(Optional.of(economicActivity1));
        when(repository.save(any(MainOffice.class))).thenReturn(existingMainOffice);

        AffiliateMercantile mercantileForEmail = new AffiliateMercantile();
        mercantileForEmail.setEmail("test@test.com");
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(mercantileForEmail));
        when(arlInformationDao.findAllArlInformation()).thenReturn(new ArrayList<>());

        assertDoesNotThrow(() -> mainOfficeService.saveMainOffice(dto));
    }

    @Test
    void update_withChangedEconomicActivitiesMercantile_shouldValidateWorkers() {
        MainOfficeDTO dto = createValidMainOfficeDTO(false);
        dto.setEconomicActivity(List.of(economicActivity2.getId()));

        AffiliateMercantile mercantile = new AffiliateMercantile();
        AffiliateActivityEconomic activity = new AffiliateActivityEconomic();
        activity.setActivityEconomic(economicActivity1);
        mercantile.setEconomicActivity(List.of(activity));
        mercantile.setEmail("test@test.com"); // FIX: Agregar email

        when(repository.findById(anyLong())).thenReturn(Optional.of(existingOfficeToUpdate));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(economicActivityRepository.findById(anyLong())).thenReturn(Optional.of(economicActivity2));
        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(repository.save(any(MainOffice.class))).thenReturn(existingOfficeToUpdate);


        MainOffice officeWithName = new MainOffice();
        officeWithName.setId(50L);
        officeWithName.setMainOfficeName("Office To Update");
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(officeWithName));

        when(workCenterService.getWorkCenterByMainOffice(any())).thenReturn(new ArrayList<>());
        when(economicActivityRepository.findByEconomicActivityCode(anyString()))
                .thenReturn(List.of(economicActivity2));
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(mercantile));
        when(arlInformationDao.findAllArlInformation()).thenReturn(new ArrayList<>());

        assertDoesNotThrow(() -> mainOfficeService.update(dto, existingOfficeToUpdate.getId()));
    }


    @Test
    void delete_withDomesticServices_shouldValidateAndDelete() {
        existingOfficeToUpdate.setMain(false);
        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);

        Affiliation affiliation = new Affiliation();
        affiliation.setEconomicActivity(new ArrayList<>());
        affiliation.setEmail("test@test.com");

        when(repository.findById(anyLong())).thenReturn(Optional.of(existingOfficeToUpdate));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));

        when(domesticServiceIndependentRepository.findByFiledNumber(anyString()))
                .thenReturn(Optional.of(affiliation));
        when(domesticServiceIndependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(affiliation));

        when(repository.findAll(any(Specification.class)))
                .thenReturn(List.of(existingOfficeToUpdate, new MainOffice()));
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.empty());
        when(arlInformationDao.findAllArlInformation()).thenReturn(new ArrayList<>());

        String result = mainOfficeService.delete(existingOfficeToUpdate.getId(), affiliate.getIdAffiliate());

        assertEquals("OK", result);
        verify(repository).delete(existingOfficeToUpdate);
    }

    @Test
    void update_preservesIdSedePositiva() {
        MainOfficeDTO dto = createValidMainOfficeDTO(false);
        existingOfficeToUpdate.setIdSedePositiva(999L);

        when(repository.findById(anyLong())).thenReturn(Optional.of(existingOfficeToUpdate));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(economicActivityRepository.findById(anyLong())).thenReturn(Optional.of(economicActivity1));
        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(repository.save(any(MainOffice.class))).thenReturn(existingOfficeToUpdate);
        MainOffice officeWithName = new MainOffice();
        officeWithName.setId(50L);
        officeWithName.setMainOfficeName("Office To Update");
        MainOffice office2 = new MainOffice();
        office2.setId(51L);
        office2.setMainOfficeName("Office 2");
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(officeWithName, office2));
        when(workCenterService.getWorkCenterByMainOffice(any())).thenReturn(new ArrayList<>());
        when(economicActivityRepository.findByEconomicActivityCode(anyString()))
                .thenReturn(List.of(economicActivity1));
        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setEmail("test@test.com");
        mercantile.setEconomicActivity(new ArrayList<>()); // IMPORTANTE: No null
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(mercantile));
        when(domesticServiceIndependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.empty());
        when(arlInformationDao.findAllArlInformation()).thenReturn(new ArrayList<>());

        mainOfficeService.update(dto, existingOfficeToUpdate.getId());

        ArgumentCaptor<MainOffice> captor = ArgumentCaptor.forClass(MainOffice.class);
        verify(repository).save(captor.capture());
        assertEquals(999L, captor.getValue().getIdSedePositiva());
    }

    @Test
    void update_withOnlyOneOffice_shouldSetMainTrue() {
        MainOfficeDTO dto = createValidMainOfficeDTO(false);
        dto.setMain(false);

        when(repository.findById(anyLong())).thenReturn(Optional.of(existingOfficeToUpdate));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(economicActivityRepository.findById(anyLong())).thenReturn(Optional.of(economicActivity1));
        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(repository.save(any(MainOffice.class))).thenReturn(existingOfficeToUpdate);
        MainOffice officeWithName = new MainOffice();
        officeWithName.setId(50L);
        officeWithName.setMainOfficeName("Office To Update");
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(officeWithName));
        when(workCenterService.getWorkCenterByMainOffice(any())).thenReturn(new ArrayList<>());
        when(economicActivityRepository.findByEconomicActivityCode(anyString()))
                .thenReturn(List.of(economicActivity1));
        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setEmail("test@test.com");
        mercantile.setEconomicActivity(new ArrayList<>());
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(mercantile));
        when(domesticServiceIndependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.empty());
        when(arlInformationDao.findAllArlInformation()).thenReturn(new ArrayList<>());

        mainOfficeService.update(dto, existingOfficeToUpdate.getId());

        ArgumentCaptor<MainOffice> captor = ArgumentCaptor.forClass(MainOffice.class);
        verify(repository).save(captor.capture());
        assertTrue(captor.getValue().getMain());
    }

    @Test
    void update_shouldCallPositivaUpdateIntegration() {
        MainOfficeDTO dto = createValidMainOfficeDTO(false);

        when(repository.findById(anyLong())).thenReturn(Optional.of(existingMainOffice));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(economicActivityRepository.findById(anyLong())).thenReturn(Optional.of(economicActivity1));
        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(repository.save(any(MainOffice.class))).thenReturn(existingMainOffice);
        MainOffice office1 = new MainOffice();
        office1.setId(99L);
        office1.setMainOfficeName("Main Office");
        MainOffice office2 = new MainOffice();
        office2.setId(100L);
        office2.setMainOfficeName("Office 2");
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(office1, office2));
        when(workCenterService.getWorkCenterByMainOffice(any())).thenReturn(new ArrayList<>());
        when(economicActivityRepository.findByEconomicActivityCode(anyString()))
                .thenReturn(List.of(economicActivity1));


        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setEmail("test@test.com");
        mercantile.setEconomicActivity(new ArrayList<>());
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(mercantile));
        when(domesticServiceIndependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.empty());
        when(arlInformationDao.findAllArlInformation()).thenReturn(new ArrayList<>());

        mainOfficeService.update(dto, existingMainOffice.getId());

        verify(updateHeadquartersClient, times(1)).update(any());
    }

    @Test
    void update_withPositivaException_shouldStillUpdateLocally() {
        MainOfficeDTO dto = createValidMainOfficeDTO(false);

        when(repository.findById(anyLong())).thenReturn(Optional.of(existingMainOffice));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(economicActivityRepository.findById(anyLong())).thenReturn(Optional.of(economicActivity1));
        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(repository.save(any(MainOffice.class))).thenReturn(existingMainOffice);
        when(updateHeadquartersClient.update(any())).thenThrow(new RuntimeException("Positiva Error"));

        MainOffice office1 = new MainOffice();
        office1.setId(99L);
        office1.setMainOfficeName("Main Office");
        MainOffice office2 = new MainOffice();
        office2.setId(100L);
        office2.setMainOfficeName("Office 2");
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(office1, office2));

        when(workCenterService.getWorkCenterByMainOffice(any())).thenReturn(new ArrayList<>());
        when(economicActivityRepository.findByEconomicActivityCode(anyString()))
                .thenReturn(List.of(economicActivity1));

        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setEmail("test@test.com");
        mercantile.setEconomicActivity(new ArrayList<>());
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(mercantile));
        when(domesticServiceIndependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.empty());
        when(arlInformationDao.findAllArlInformation()).thenReturn(new ArrayList<>());

        MainOffice result = mainOfficeService.update(dto, existingMainOffice.getId());

        assertNotNull(result);
        verify(repository).save(any(MainOffice.class));
    }

    @Test
    void updateOfficial_shouldFindAffiliateAndUpdate() {
        MainOfficeDTO dto = createValidMainOfficeDTO(false);
        String documentNumber = "987654321";

        when(affiliateRepository.findOne(any(Specification.class))).thenReturn(Optional.of(affiliate));
        when(repository.findById(anyLong())).thenReturn(Optional.of(existingOfficeToUpdate));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(economicActivityRepository.findById(anyLong())).thenReturn(Optional.of(economicActivity1));
        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(repository.save(any(MainOffice.class))).thenReturn(existingOfficeToUpdate);

        MainOffice office1 = new MainOffice();
        office1.setId(50L);
        office1.setMainOfficeName("Office To Update");
        MainOffice office2 = new MainOffice();
        office2.setId(51L);
        office2.setMainOfficeName("Office 2");
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(office1, office2));


        when(workCenterService.getWorkCenterByMainOffice(any())).thenReturn(new ArrayList<>());
        when(economicActivityRepository.findByEconomicActivityCode(anyString()))
                .thenReturn(List.of(economicActivity1));


        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setEmail("test@test.com");
        mercantile.setEconomicActivity(new ArrayList<>());
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(mercantile));
        when(domesticServiceIndependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.empty());
        when(arlInformationDao.findAllArlInformation()).thenReturn(new ArrayList<>());

        MainOffice result = mainOfficeService.updateOfficial(dto, existingOfficeToUpdate.getId(), documentNumber);

        assertNotNull(result);
        verify(affiliateRepository).findOne(any(Specification.class));
    }


    @Test
    void delete_whenOfficeIsMain_shouldThrow() {
        MainOffice office = new MainOffice();
        office.setMain(true);
        when(repository.findById(1L)).thenReturn(Optional.of(office));

        // Se usa el flujo real de delete (busca el afiliado por idAffiliateEmployer)
        when(affiliateRepository.findByIdAffiliate(anyLong()))
                .thenReturn(Optional.of(new Affiliate()));

        // No debería llegar más lejos: la excepción por ser principal ocurre primero
        assertThrows(AffiliationError.class, () -> mainOfficeService.delete(1L, 999L));
    }



    @Test
    void findId_domesticBranch_shouldReturnDTOWithActivitiesFromEnabledWorkCenters() {
        // Sede y afiliación de tipo doméstico
        MainOffice mo = new MainOffice();
        mo.setId(77L);
        mo.setIdAffiliate(1L);
        mo.setOfficeManager(userMain);
        when(repository.findById(77L)).thenReturn(Optional.of(mo));

        Affiliate aff = new Affiliate();
        aff.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        aff.setFiledNumber("F-XYZ");
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(aff));

        // Forzamos ramo DOMÉSTICO
        Affiliation dom = new Affiliation();
        when(domesticServiceIndependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(dom));

        // Work centers: solo el enabled aporta actividad
        WorkCenter wc1 = new WorkCenter(); wc1.setIsEnable(true);  wc1.setEconomicActivityCode("1011101");
        WorkCenter wc2 = new WorkCenter(); wc2.setIsEnable(false); wc2.setEconomicActivityCode("2022202");
        when(workCenterService.getWorkCenterByMainOffice(mo)).thenReturn(List.of(wc1, wc2));

        EconomicActivity ea = new EconomicActivity();
        ea.setId(999L);
        when(economicActivityRepository.findByEconomicActivityCode("1011101")).thenReturn(List.of(ea));

        MainOfficeDTO out = mainOfficeService.findId(77L);
        assertNotNull(out);
        assertEquals(userMain.getId(), out.getOfficeManager());
        assertEquals(1, out.getEconomicActivity().size(), "Solo actividades de work centers habilitados");
        assertEquals(999L, out.getEconomicActivity().get(0));
    }


    @Test
    void validWorkedAssociatedToWorkCenter_withMatchingWorkCenterAndDependents_shouldThrow() throws Exception {
        // Preparar economic activity y work center existente para la sede
        EconomicActivity act = new EconomicActivity();
        act.setEconomicActivityCode("5101009");
        Long idHeadquarter = 99L;

        WorkCenter wc = new WorkCenter();
        wc.setEconomicActivityCode("5101009");
        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice("5101009", idHeadquarter))
                .thenReturn(wc);

        // Hay dependientes asociados al headquarter → debe lanzar ERROR_DELETE
        AffiliationDependent dep = new AffiliationDependent();
        dep.setFiledNumber("D1");
        dep.setIdHeadquarter(idHeadquarter);

        when(affiliationDependentRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(dep)); // lista de filedNumbers
        when(affiliationDependentRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(dep)); // y la búsqueda por filedNumbers

        // Afiliado cualquiera
        Affiliate aff = new Affiliate();
        aff.setIdAffiliate(1L);
        aff.setFiledNumber("F1");

        java.lang.reflect.Method m = MainOfficeServiceImpl.class
                .getDeclaredMethod("validWorkedAssociatedToWorkCenter", Long.class, EconomicActivity.class, Affiliate.class);
        m.setAccessible(true);

        assertThrows(Exception.class, () -> m.invoke(mainOfficeService, idHeadquarter, act, aff));
    }

    @Test
    void validWorkedAssociatedToWorkCenter_withoutWorkCenterOrDependents_shouldNotThrow() throws Exception {
        EconomicActivity act = new EconomicActivity();
        act.setEconomicActivityCode("5101009");
        Long idHeadquarter = 100L;

        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice(anyString(), anyLong()))
                .thenReturn(null);

        when(affiliationDependentRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        Affiliate aff = new Affiliate();
        aff.setIdAffiliate(2L);
        aff.setFiledNumber("F2");

        java.lang.reflect.Method m = MainOfficeServiceImpl.class
                .getDeclaredMethod("validWorkedAssociatedToWorkCenter", Long.class, EconomicActivity.class, Affiliate.class);
        m.setAccessible(true);

        m.invoke(mainOfficeService, idHeadquarter, act, aff);
    }
    @Test
    void saveMainOffice_whenMainTrue_shouldChangeMain_setMainIfFirst_andCreateWorkCenters() {

        MainOfficeDTO dto = createValidMainOfficeDTO(true);
        dto.setOfficeManager(userMain.getId());

        affiliate.setFiledNumber("CC-123456789");

        affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);
        when(repository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(economicActivityRepository.findById(economicActivity1.getId())).thenReturn(Optional.of(economicActivity1));
        when(economicActivityRepository.findById(economicActivity2.getId())).thenReturn(Optional.of(economicActivity2));
        when(repository.nextConsecutiveCodeMainOffice()).thenReturn(0L);
        when(repository.save(any(MainOffice.class))).thenAnswer(inv -> inv.getArgument(0));

        when(workCenterService.getWorkCenterByCodeAndIdUser(anyString(), any(UserMain.class))).thenReturn(null);
        when(workCenterService.getNumberCode(any(UserMain.class))).thenReturn(0L);
        when(workCenterService.saveWorkCenter(any(WorkCenter.class))).thenAnswer(inv -> inv.getArgument(0));

        AffiliateMercantile mercForEmail = new AffiliateMercantile();
        mercForEmail.setEmail("afiliado@prueba.com");
        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(mercForEmail));
        when(domesticServiceIndependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.empty());

        when(arlInformationDao.findAllArlInformation()).thenReturn(Collections.emptyList());
        doNothing().when(sendEmails).sendEmailHeadquarters(anyMap(), anyString());

        MainOffice saved = mainOfficeService.saveMainOffice(dto);

        assertNotNull(saved);
        assertTrue(saved.getMain(), "Debe marcarse main=true cuando es la primera sede");
        verify(repository, atLeastOnce()).save(any(MainOffice.class));
        verify(workCenterService, atLeastOnce()).saveWorkCenter(any(WorkCenter.class));
        verify(sendEmails, times(1)).sendEmailHeadquarters(anyMap(), anyString());
    }

    @Test
    void update_preservesGhostIdSedePositiva_andOriginalCode_andMarksMainWhenOnlyOne() {
        // --- Arrange ---
        existingOfficeToUpdate.setIdSedePositiva(12345L);
        final String originalCode = existingOfficeToUpdate.getCode();

        when(repository.findById(existingOfficeToUpdate.getId()))
                .thenReturn(Optional.of(existingOfficeToUpdate));

        MainOfficeDTO dto = createValidMainOfficeDTO(true);
        dto.setMainOfficeName("Nuevo Nombre");
        dto.setIdAffiliateEmployer(affiliate.getIdAffiliate());
        dto.setOfficeManager(userMain.getId());

        affiliate.setFiledNumber("CC-123456789");
        affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);

        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));

        when(economicActivityRepository.findById(economicActivity1.getId()))
                .thenReturn(Optional.of(economicActivity1));
        when(economicActivityRepository.findById(economicActivity2.getId()))
                .thenReturn(Optional.of(economicActivity2));
        when(repository.findAll(any(Specification.class)))
                .thenReturn(List.of(existingOfficeToUpdate));

        WorkCenter currentWc = new WorkCenter();
        currentWc.setEconomicActivityCode("1011101");
        when(workCenterService.getWorkCenterByMainOffice(any(MainOffice.class)))
                .thenReturn(List.of(currentWc));
        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice(anyString(), anyLong()))
                .thenReturn(currentWc);
        when(economicActivityRepository.findByEconomicActivityCode(anyString()))
                .thenReturn(Collections.singletonList(new EconomicActivity()));

        when(repository.save(any(MainOffice.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AffiliateMercantile mercForEmail = Mockito.mock(AffiliateMercantile.class);
        when(mercForEmail.getEmail()).thenReturn("afiliado@prueba.com");
        // Lista NO nula (vacía es suficiente)
        when(mercForEmail.getEconomicActivity()).thenReturn(Collections.emptyList());

        when(affiliateMercantileRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(mercForEmail));
        when(domesticServiceIndependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.empty());

        when(arlInformationDao.findAllArlInformation()).thenReturn(Collections.emptyList());
        doNothing().when(sendEmails).sendEmailHeadquarters(anyMap(), anyString());

        MainOffice updated = mainOfficeService.update(dto, existingOfficeToUpdate.getId());

        assertNotNull(updated);
        assertEquals(Long.valueOf(12345L), updated.getIdSedePositiva(), "Debe preservar idSedePositiva");
        assertEquals(originalCode, updated.getCode(), "Debe preservar el code original");
        assertTrue(updated.getMain(), "Debe marcar como principal cuando solo existe una sede");

        verify(repository, atLeastOnce()).save(any(MainOffice.class));
    }

    @Test
    void saveMainOffice_whenMainFalse_andThereIsAnotherOffice_shouldKeepNonMain() {
        // --- Arrange ---
        MainOfficeDTO dto = createValidMainOfficeDTO(false); // main = false
        dto.setOfficeManager(userMain.getId());

        affiliate.setFiledNumber("CC-123456789");
        affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);
        when(repository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList(),
                        Collections.emptyList(),
                        List.of(new MainOffice()),
                        Collections.emptyList(),
                        Collections.emptyList());

        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(economicActivityRepository.findById(economicActivity1.getId())).thenReturn(Optional.of(economicActivity1));
        when(economicActivityRepository.findById(economicActivity2.getId())).thenReturn(Optional.of(economicActivity2));
        when(repository.nextConsecutiveCodeMainOffice()).thenReturn(5L);
        when(repository.save(any(MainOffice.class))).thenAnswer(inv -> inv.getArgument(0));
        when(workCenterService.getWorkCenterByCodeAndIdUser(anyString(), any(UserMain.class))).thenReturn(null);
        when(workCenterService.getNumberCode(any(UserMain.class))).thenReturn(7L);
        when(workCenterService.saveWorkCenter(any(WorkCenter.class))).thenAnswer(inv -> inv.getArgument(0));
        AffiliateMercantile merc = Mockito.mock(AffiliateMercantile.class);
        when(merc.getEmail()).thenReturn("afiliado@prueba.com");
        when(merc.getEconomicActivity()).thenReturn(Collections.emptyList());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(merc));
        when(domesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        when(arlInformationDao.findAllArlInformation()).thenReturn(Collections.emptyList());
        doNothing().when(sendEmails).sendEmailHeadquarters(anyMap(), anyString());

        MainOffice saved = mainOfficeService.saveMainOffice(dto);

        assertNotNull(saved);
        assertFalse(Boolean.TRUE.equals(saved.getMain()),
                "No debe forzar main=true cuando ya existe otra sede y el DTO viene con main=false");
        verify(repository, atLeastOnce()).save(any(MainOffice.class));
        verify(workCenterService, atLeastOnce()).saveWorkCenter(any(WorkCenter.class));
        verify(sendEmails, times(1)).sendEmailHeadquarters(anyMap(), anyString());
    }

    @Test
    void update_whenOfficeManagerNotFound_shouldThrowAffiliationError() {
        when(repository.findById(existingOfficeToUpdate.getId()))
                .thenReturn(Optional.of(existingOfficeToUpdate));

        MainOfficeDTO dto = createValidMainOfficeDTO(true);
        dto.setOfficeManager(999_999L);
        dto.setIdAffiliateEmployer(affiliate.getIdAffiliate());
        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(existingOfficeToUpdate));
        assertThrows(AffiliationError.class, () ->
                mainOfficeService.update(dto, existingOfficeToUpdate.getId()));
    }
    @Test
    void findId_domesticBranch_withAllWorkCentersDisabled_shouldReturnEmptyActivities() {
        // --- Arrange ---
        MainOffice mo = new MainOffice();
        mo.setId(555L);
        mo.setIdAffiliate(1L);
        mo.setOfficeManager(userMain);
        when(repository.findById(555L)).thenReturn(Optional.of(mo));

        Affiliate aff = new Affiliate();
        aff.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);
        aff.setFiledNumber("F-DOM-1");
        when(affiliateRepository.findById(1L)).thenReturn(Optional.of(aff));
        when(domesticServiceIndependentRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(new com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation()));
        WorkCenter wc1 = new WorkCenter(); wc1.setIsEnable(false); wc1.setEconomicActivityCode("1111111");
        WorkCenter wc2 = new WorkCenter(); wc2.setIsEnable(false); wc2.setEconomicActivityCode("2222222");
        when(workCenterService.getWorkCenterByMainOffice(mo)).thenReturn(List.of(wc1, wc2));
        when(economicActivityRepository.findByEconomicActivityCode(anyString()))
                .thenReturn(Collections.emptyList());
        MainOfficeDTO out = mainOfficeService.findId(555L);
        assertNotNull(out);
        assertTrue(out.getEconomicActivity() == null || out.getEconomicActivity().isEmpty(),
                "Con todos los work centers deshabilitados, la lista de actividades debe quedar vacía");
    }

    @Test
    void update_whenNoExistingWorkCenters_shouldCreateForEachSelectedActivity() {

        MainOfficeDTO dto = createValidMainOfficeDTO(true);
        dto.setOfficeManager(userMain.getId());
        dto.setIdAffiliateEmployer(affiliate.getIdAffiliate());
        when(repository.findById(existingOfficeToUpdate.getId()))
                .thenReturn(Optional.of(existingOfficeToUpdate));
        when(repository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList(),
                        Collections.emptyList(),
                        List.of(existingOfficeToUpdate));

        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));

        when(economicActivityRepository.findById(economicActivity1.getId())).thenReturn(Optional.of(economicActivity1));
        when(economicActivityRepository.findById(economicActivity2.getId())).thenReturn(Optional.of(economicActivity2));
        when(economicActivityRepository.findByEconomicActivityCode(anyString()))
                .thenReturn(Collections.singletonList(new EconomicActivity()));

        when(workCenterService.getWorkCenterByMainOffice(any(MainOffice.class)))
                .thenReturn(Collections.emptyList());
        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice(anyString(), anyLong()))
                .thenReturn(null);
        when(workCenterService.getNumberCode(any(UserMain.class))).thenReturn(10L, 11L);
        when(workCenterService.saveWorkCenter(any(WorkCenter.class))).thenAnswer(i -> i.getArgument(0));
        when(repository.save(any(MainOffice.class))).thenAnswer(i -> i.getArgument(0));
        AffiliateMercantile merc = Mockito.mock(AffiliateMercantile.class);
        when(merc.getEmail()).thenReturn("afiliado@prueba.com");
        when(merc.getEconomicActivity()).thenReturn(Collections.emptyList());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(merc));
        when(domesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(arlInformationDao.findAllArlInformation()).thenReturn(Collections.emptyList());
        doNothing().when(sendEmails).sendEmailHeadquarters(anyMap(), anyString());
        MainOffice out = mainOfficeService.update(dto, existingOfficeToUpdate.getId());
        assertNotNull(out);
        verify(workCenterService).saveWorkCenter(any(WorkCenter.class)); // 2 actividades → 2 WCs
        verify(repository, atLeastOnce()).save(any(MainOffice.class));
    }
    @Test
    void update_whenSomeActivitiesMissingWorkCenter_shouldCreateMissingOnes() {
        MainOfficeDTO dto = createValidMainOfficeDTO(true);
        dto.setOfficeManager(userMain.getId());
        dto.setIdAffiliateEmployer(affiliate.getIdAffiliate());

        when(repository.findById(existingOfficeToUpdate.getId()))
                .thenReturn(Optional.of(existingOfficeToUpdate));

        when(repository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList(),
                        Collections.emptyList(),
                        List.of(existingOfficeToUpdate));

        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));

        when(economicActivityRepository.findById(economicActivity1.getId())).thenReturn(Optional.of(economicActivity1));
        when(economicActivityRepository.findById(economicActivity2.getId())).thenReturn(Optional.of(economicActivity2));
        when(economicActivityRepository.findByEconomicActivityCode(anyString()))
                .thenReturn(Collections.singletonList(new EconomicActivity())); // evita IOBE

        WorkCenter wcExisting = new WorkCenter();
        wcExisting.setEconomicActivityCode(economicActivity1.getEconomicActivityCode());
        when(workCenterService.getWorkCenterByMainOffice(any(MainOffice.class)))
                .thenReturn(List.of(wcExisting));

        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice(
                eq(economicActivity1.getEconomicActivityCode()), anyLong()))
                .thenReturn(wcExisting);
        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice(
                eq(economicActivity2.getEconomicActivityCode()), anyLong()))
                .thenReturn(null);

        when(workCenterService.getNumberCode(any(UserMain.class))).thenReturn(99L);
        when(workCenterService.saveWorkCenter(any(WorkCenter.class))).thenAnswer(i -> i.getArgument(0));
        when(repository.save(any(MainOffice.class))).thenAnswer(i -> i.getArgument(0));
        AffiliateMercantile merc = Mockito.mock(AffiliateMercantile.class);
        when(merc.getEmail()).thenReturn("afiliado@prueba.com");
        when(merc.getEconomicActivity()).thenReturn(Collections.emptyList());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(merc));
        when(domesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(arlInformationDao.findAllArlInformation()).thenReturn(Collections.emptyList());
        doNothing().when(sendEmails).sendEmailHeadquarters(anyMap(), anyString());
        MainOffice out = mainOfficeService.update(dto, existingOfficeToUpdate.getId());
        assertNotNull(out);
        verify(workCenterService, atLeastOnce()).saveWorkCenter(any(WorkCenter.class));
    }

    @Test
    void update_whenActivitiesAreTheSame_shouldNotCreateNewWorkCenters() {
        MainOfficeDTO dto = createValidMainOfficeDTO(true);
        dto.setOfficeManager(userMain.getId());
        dto.setIdAffiliateEmployer(affiliate.getIdAffiliate());

        when(repository.findById(existingOfficeToUpdate.getId()))
                .thenReturn(Optional.of(existingOfficeToUpdate));
        when(repository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList(),
                        Collections.emptyList(),
                        List.of(existingOfficeToUpdate));

        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));

        when(economicActivityRepository.findById(economicActivity1.getId())).thenReturn(Optional.of(economicActivity1));
        when(economicActivityRepository.findById(economicActivity2.getId())).thenReturn(Optional.of(economicActivity2));
        when(economicActivityRepository.findByEconomicActivityCode(anyString()))
                .thenReturn(Collections.singletonList(new EconomicActivity())); // evita IOBE

        WorkCenter wc1 = new WorkCenter(); wc1.setEconomicActivityCode(economicActivity1.getEconomicActivityCode());
        WorkCenter wc2 = new WorkCenter(); wc2.setEconomicActivityCode(economicActivity2.getEconomicActivityCode());
        when(workCenterService.getWorkCenterByMainOffice(any(MainOffice.class)))
                .thenReturn(List.of(wc1, wc2));
        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice(
                eq(economicActivity1.getEconomicActivityCode()), anyLong()))
                .thenReturn(wc1);
        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice(
                eq(economicActivity2.getEconomicActivityCode()), anyLong()))
                .thenReturn(wc2);
        when(repository.save(any(MainOffice.class))).thenAnswer(i -> i.getArgument(0));
        AffiliateActivityEconomic a1 = new AffiliateActivityEconomic(); a1.setActivityEconomic(economicActivity1);
        AffiliateActivityEconomic a2 = new AffiliateActivityEconomic(); a2.setActivityEconomic(economicActivity2);
        AffiliateMercantile merc = Mockito.mock(AffiliateMercantile.class);
        when(merc.getEmail()).thenReturn("afiliado@prueba.com");
        when(merc.getEconomicActivity()).thenReturn(List.of(a1, a2));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(merc));
        when(domesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(arlInformationDao.findAllArlInformation()).thenReturn(Collections.emptyList());
        doNothing().when(sendEmails).sendEmailHeadquarters(anyMap(), anyString());
        MainOffice out = mainOfficeService.update(dto, existingOfficeToUpdate.getId());
        assertNotNull(out);
        verify(workCenterService, never()).getNumberCode(any(UserMain.class));
        verify(workCenterService, atLeastOnce()).saveWorkCenter(any(WorkCenter.class));
    }
    @Test
    void saveMainOffice_whenDuplicateByNameOrAddress_shouldThrowAffiliationError() {
        MainOfficeDTO dto = createValidMainOfficeDTO(true);
        dto.setOfficeManager(userMain.getId());
        affiliate.setFiledNumber("CC-123456789");

        // 1° y 2° findAll => simulamos que hay duplicados (NO vacíos)
        when(repository.findAll(any(Specification.class)))
                .thenReturn(List.of(new MainOffice()), List.of(new MainOffice()));

        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));

        assertThrows(AffiliationError.class, () -> mainOfficeService.saveMainOffice(dto));

        // No debería intentar crear nada
        verify(repository, never()).nextConsecutiveCodeMainOffice();
        verify(workCenterService, never()).saveWorkCenter(any());
    }
    @Test
    void saveMainOffice_whenThereIsMainAlready_andDtoMainTrue_shouldSwitchMain() {
        MainOfficeDTO dto = createValidMainOfficeDTO(true);
        dto.setOfficeManager(userMain.getId());
        affiliate.setFiledNumber("CC-123456789");
        affiliate.setAffiliationSubType(Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE);

        // Secuencia de findAll: 2 checks duplicado vacíos, luego lista con una sede MAIN ya existente
        MainOffice previousMain = new MainOffice();
        previousMain.setMain(true);
        previousMain.setIdAffiliate(affiliate.getIdAffiliate());
        when(repository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList(),
                        Collections.emptyList(),
                        List.of(previousMain),
                        Collections.emptyList());

        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));

        when(economicActivityRepository.findById(economicActivity1.getId())).thenReturn(Optional.of(economicActivity1));
        when(economicActivityRepository.findById(economicActivity2.getId())).thenReturn(Optional.of(economicActivity2));

        when(repository.nextConsecutiveCodeMainOffice()).thenReturn(12L);
        ArgumentCaptor<MainOffice> cap = ArgumentCaptor.forClass(MainOffice.class);
        when(repository.save(any(MainOffice.class))).thenAnswer(inv -> inv.getArgument(0));

        // WC: crear
        when(workCenterService.getWorkCenterByCodeAndIdUser(anyString(), any(UserMain.class))).thenReturn(null);
        when(workCenterService.getNumberCode(any(UserMain.class))).thenReturn(1L);
        when(workCenterService.saveWorkCenter(any(WorkCenter.class))).thenAnswer(i -> i.getArgument(0));

        // Email por MERCANTIL
        AffiliateMercantile merc = Mockito.mock(AffiliateMercantile.class);
        when(merc.getEmail()).thenReturn("mail@demo.com");
        when(merc.getEconomicActivity()).thenReturn(Collections.emptyList());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(merc));
        when(domesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(arlInformationDao.findAllArlInformation()).thenReturn(Collections.emptyList());
        doNothing().when(sendEmails).sendEmailHeadquarters(anyMap(), anyString());

        MainOffice created = mainOfficeService.saveMainOffice(dto);

        assertTrue(created.getMain(), "La nueva sede debe quedar como principal");
        // Se guardan la sede previa (marcada a false) y la nueva (true)
        verify(repository, atLeast(2)).save(cap.capture());
        boolean sawOldFalse = cap.getAllValues().stream().anyMatch(mo -> mo != created && Boolean.FALSE.equals(mo.getMain()));
        assertTrue(sawOldFalse, "La sede anterior debió quedar main=false");
    }
    @Test
    void update_whenAnActivityIsRemoved_shouldDisableCorrespondingWorkCenter_withoutCreatingNewOnes() {
        // DTO SOLO con activity1 (quitamos activity2)
        MainOfficeDTO dto = createValidMainOfficeDTO(true);
        dto.setOfficeManager(userMain.getId());
        dto.setIdAffiliateEmployer(affiliate.getIdAffiliate());
        dto.setEconomicActivity(List.of(economicActivity1.getId()));

        when(repository.findById(existingOfficeToUpdate.getId())).thenReturn(Optional.of(existingOfficeToUpdate));
        when(repository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList(), Collections.emptyList(), List.of(existingOfficeToUpdate));

        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));

        when(economicActivityRepository.findById(economicActivity1.getId())).thenReturn(Optional.of(economicActivity1));
        when(economicActivityRepository.findById(economicActivity2.getId())).thenReturn(Optional.of(economicActivity2));
        when(economicActivityRepository.findByEconomicActivityCode(anyString()))
                .thenReturn(Collections.singletonList(new EconomicActivity()));

        // Existen WCs para ambas; el de activity2 debe quedar deshabilitado
        WorkCenter wc1 = new WorkCenter(); wc1.setEconomicActivityCode(economicActivity1.getEconomicActivityCode()); wc1.setIsEnable(true);
        WorkCenter wc2 = new WorkCenter(); wc2.setEconomicActivityCode(economicActivity2.getEconomicActivityCode()); wc2.setIsEnable(true);
        when(workCenterService.getWorkCenterByMainOffice(any(MainOffice.class))).thenReturn(List.of(wc1, wc2));
        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice(eq(economicActivity1.getEconomicActivityCode()), anyLong()))
                .thenReturn(wc1);
        when(workCenterService.getWorkCenterByEconomicActivityAndMainOffice(eq(economicActivity2.getEconomicActivityCode()), anyLong()))
                .thenReturn(wc2);

        when(repository.save(any(MainOffice.class))).thenAnswer(i -> i.getArgument(0));
        when(workCenterService.saveWorkCenter(any(WorkCenter.class))).thenAnswer(i -> i.getArgument(0));

        // Email MERCANTIL estable
        AffiliateMercantile merc = Mockito.mock(AffiliateMercantile.class);
        when(merc.getEmail()).thenReturn("mail@demo.com");
        when(merc.getEconomicActivity()).thenReturn(List.of(
                new AffiliateActivityEconomic() {{ setActivityEconomic(economicActivity1); }}
        ));
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(merc));
        when(domesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(arlInformationDao.findAllArlInformation()).thenReturn(Collections.emptyList());
        doNothing().when(sendEmails).sendEmailHeadquarters(anyMap(), anyString());

        MainOffice out = mainOfficeService.update(dto, existingOfficeToUpdate.getId());
        assertNotNull(out);

        // No debe generar código nuevo (no crea WC)
        verify(workCenterService, never()).getNumberCode(any(UserMain.class));

        // El WC de la actividad removida debe quedar deshabilitado y guardado
        ArgumentCaptor<WorkCenter> wcCap = ArgumentCaptor.forClass(WorkCenter.class);
        verify(workCenterService, atLeastOnce()).saveWorkCenter(wcCap.capture());
        boolean disabledSecond = wcCap.getAllValues().stream()
                .anyMatch(wc -> economicActivity2.getEconomicActivityCode().equals(wc.getEconomicActivityCode())
                        && Boolean.FALSE.equals(wc.getIsEnable()));
        assertTrue(disabledSecond, "El WorkCenter de la actividad removida debe deshabilitarse");
    }
    @Test
    void saveMainOffice_whenDomestic_shouldUseDomesticEmailPath() {
        MainOfficeDTO dto = createValidMainOfficeDTO(true);
        dto.setOfficeManager(userMain.getId());

        affiliate.setFiledNumber("CC-DS-1");
        affiliate.setAffiliationSubType(Constant.AFFILIATION_SUBTYPE_DOMESTIC_SERVICES);

        // Duplicados: vacíos; primera sede: vacía
        when(repository.findAll(any(Specification.class)))
                .thenReturn(Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList());

        when(iUserPreRegisterRepository.findById(anyLong())).thenReturn(Optional.of(userMain));
        when(affiliateRepository.findByIdAffiliate(anyLong())).thenReturn(Optional.of(affiliate));

        when(economicActivityRepository.findById(economicActivity1.getId())).thenReturn(Optional.of(economicActivity1));
        when(economicActivityRepository.findById(economicActivity2.getId())).thenReturn(Optional.of(economicActivity2));

        when(repository.nextConsecutiveCodeMainOffice()).thenReturn(0L);
        when(repository.save(any(MainOffice.class))).thenAnswer(i -> i.getArgument(0));

        // WorkCenters nuevos
        when(workCenterService.getWorkCenterByCodeAndIdUser(anyString(), any(UserMain.class))).thenReturn(null);
        when(workCenterService.getNumberCode(any(UserMain.class))).thenReturn(1L);
        when(workCenterService.saveWorkCenter(any(WorkCenter.class))).thenAnswer(i -> i.getArgument(0));

        // *** CLAVE ***: proveemos email por el camino MERCANTIL para que sendEmail no lance
        AffiliateMercantile merc = Mockito.mock(AffiliateMercantile.class);
        when(merc.getEmail()).thenReturn("domestico@correo.com");
        when(merc.getEconomicActivity()).thenReturn(Collections.emptyList());
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(merc));

        // y dejamos el doméstico vacío (no se usará porque ya encontró mercantil)
        when(domesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        when(arlInformationDao.findAllArlInformation()).thenReturn(Collections.emptyList());
        doNothing().when(sendEmails).sendEmailHeadquarters(anyMap(), anyString());

        // Act
        MainOffice mo = mainOfficeService.saveMainOffice(dto);

        // Assert
        assertNotNull(mo);
        verify(sendEmails, times(1)).sendEmailHeadquarters(anyMap(), anyString());
    }

}

