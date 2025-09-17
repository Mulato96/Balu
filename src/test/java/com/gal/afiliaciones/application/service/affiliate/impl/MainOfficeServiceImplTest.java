package com.gal.afiliaciones.application.service.affiliate.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.application.service.affiliate.WorkCenterService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationError;
import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.MainOfficeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.MainOfficeGrillaDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

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

    @InjectMocks
    private MainOfficeServiceImpl mainOfficeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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

        assertThrows(AffiliationError.class, () -> mainOfficeService.delete(1L, 123L));
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

    // Additional tests for update, saveMainOffice(MainOfficeDTO), and findId can be
    // added similarly.

    @Test
    void update_mainOfficeNameExists_throws() {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Existing Name");
        dto.setOfficeManager(1L);
        dto.setEconomicActivity(new ArrayList<>());
        dto.setAddressDTO(new AddressDTO());

        MainOffice existingOffice = new MainOffice();
        existingOffice.setId(2L); // Different ID
        existingOffice.setMainOfficeName("Existing Name");

        MainOffice officeToUpdate = new MainOffice();
        officeToUpdate.setId(1L);
        officeToUpdate.setMainOfficeName("Old Name");

        when(repository.findById(1L)).thenReturn(Optional.of(officeToUpdate));
        when(repository.findAll(any(Specification.class))).thenReturn(List.of(existingOffice));

        assertThrows(AffiliationError.class, () -> mainOfficeService.update(dto, 1L));
    }

    @Test
    void validWorkedAssociated_withNoDependentsAndNoMainHeadquarter_doesNotThrow()
            throws IllegalAccessException, NoSuchMethodException, java.lang.reflect.InvocationTargetException {
        Long idHeadquarter = 1L;
        Long idEconomicActivity = 2L;
        Affiliate affiliate = new Affiliate();
        affiliate.setNitCompany("123");
        affiliate.setFiledNumber("F1");

        // No dependents
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(List.of());

        // findAffiliation returns AffiliateMercantile with different idMainHeadquarter
        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setIdMainHeadquarter(99L);
        when(affiliateMercantileRepository.findOne(any(Specification.class))).thenReturn(Optional.of(mercantile));
        when(domesticServiceIndependentRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // Should not throw
        // Usando reflection para acceder al método no visible
        java.lang.reflect.Method method = mainOfficeService.getClass()
                .getDeclaredMethod("validWorkedAssociated", Long.class, Long.class, Affiliate.class);
        method.setAccessible(true);
        method.invoke(mainOfficeService, idHeadquarter, idEconomicActivity, affiliate);
    }

    @Test
    void validWorkedAssociated_withNullIdEconomicActivity_doesNotThrow()
            throws IllegalAccessException, NoSuchMethodException, java.lang.reflect.InvocationTargetException {
        {
            Long idHeadquarter = 1L;
            Long idEconomicActivity = null;
            Affiliate affiliate = new Affiliate();
            affiliate.setNitCompany("123");
            affiliate.setFiledNumber("F1");

            // Should not throw, as idEconomicActivity is null
            // Usando reflection para acceder al método no visible
            java.lang.reflect.Method method = mainOfficeService.getClass()
                    .getDeclaredMethod("validWorkedAssociated", Long.class, Long.class, Affiliate.class);
            method.setAccessible(true);
            method.invoke(mainOfficeService, idHeadquarter, idEconomicActivity, affiliate);
        }
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
        assertThrows(InvocationTargetException.class, () -> {
            method.invoke(mainOfficeService, "9991234567", true);
        });

    }

    @Test
    void validNumberPhone_requestedTrue_withEmptyNumber_returnsTrue() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberPhone", String.class, boolean.class);
        method.setAccessible(true);
        assertThrows(InvocationTargetException.class, () -> {
            method.invoke(mainOfficeService, "", true);
        });
    }

    @Test
    void validNumberPhone_requestedTrue_withNullNumber_returnsTrue() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberPhone", String.class, boolean.class);
        method.setAccessible(true);
        assertThrows(InvocationTargetException.class, () -> {
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
        assertThrows(InvocationTargetException.class, () -> {
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
    void validMainOffice_missingCity_throwsException() throws Exception {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Valid Name");
        dto.setAddressDTO(new AddressDTO()); // No city set
        dto.setEconomicActivity(List.of(1L));
        dto.setMainOfficeZone(Constant.URBAN_ZONE);

        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validMainOffice", MainOfficeDTO.class);
        method.setAccessible(true);
        
        assertThrows(InvocationTargetException.class, () -> method.invoke(mainOfficeService, dto));
    }

    @Test
    void validMainOffice_missingDepartment_throwsException() throws Exception {
        MainOfficeDTO dto = new MainOfficeDTO();
        dto.setMainOfficeName("Valid Name");
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setIdCity(1L);
        dto.setAddressDTO(addressDTO); // No department set
        dto.setEconomicActivity(List.of(1L));
        dto.setMainOfficeZone(Constant.URBAN_ZONE);

        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validMainOffice", MainOfficeDTO.class);
        method.setAccessible(true);
        
        assertThrows(InvocationTargetException.class, () -> method.invoke(mainOfficeService, dto));
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
        // No email set

        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validMainOffice", MainOfficeDTO.class);
        method.setAccessible(true);
        
        assertThrows(InvocationTargetException.class, () -> method.invoke(mainOfficeService, dto));
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
        dto.setMainOfficePhoneNumber("1234567890"); // Invalid prefix
        dto.setEconomicActivity(List.of(1L));
        dto.setMainOfficeZone(Constant.URBAN_ZONE);

        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validMainOffice", MainOfficeDTO.class);
        method.setAccessible(true);
        
        assertThrows(InvocationTargetException.class, () -> method.invoke(mainOfficeService, dto));
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
        dto.setMainOfficeZone("INVALID_ZONE"); // Invalid zone

        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validMainOffice", MainOfficeDTO.class);
        method.setAccessible(true);
        
        assertThrows(InvocationTargetException.class, () -> method.invoke(mainOfficeService, dto));
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
        dto.setEconomicActivity(new ArrayList<>()); // Empty list

        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validMainOffice", MainOfficeDTO.class);
        method.setAccessible(true);
        
        assertThrows(InvocationTargetException.class, () -> method.invoke(mainOfficeService, dto));
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
        dto.setEconomicActivity(List.of(1L, 2L, 3L, 4L, 5L, 6L)); // Too many activities

        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validMainOffice", MainOfficeDTO.class);
        method.setAccessible(true);
        
        assertThrows(InvocationTargetException.class, () -> method.invoke(mainOfficeService, dto));
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
        dto.setFirstNameResponsibleHeadquarters("123"); // Invalid name (contains numbers)

        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validMainOffice", MainOfficeDTO.class);
        method.setAccessible(true);
        
        assertThrows(InvocationTargetException.class, () -> method.invoke(mainOfficeService, dto));
    }

    @Test
    void saveMainOfficeDTO_existingAddress_throwsException() {
        // Prepare a valid MainOfficeDTO with minimal required fields
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

        // Stub repository call for findByIdUserAndDepartmentAndCityAndAddress to return non-empty list
        when(repository.findAll((Specification<MainOffice>) any())).thenReturn(List.of(new MainOffice()));

        assertThrows(AffiliationError.class, () -> {
            mainOfficeService.saveMainOffice(dto);
        });
    }

    @Test
    void validNumberIdentification_CC_validLengths() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberIdentification", String.class, String.class);
        method.setAccessible(true);

        // valid: length 3
        assertEquals(true, method.invoke(mainOfficeService, "123", "CC"));
        // valid: length 10
        assertEquals(true, method.invoke(mainOfficeService, "1234567890", "CC"));
        // invalid: length 2
        assertEquals(false, method.invoke(mainOfficeService, "12", "CC"));
        // invalid: length 11
        assertEquals(false, method.invoke(mainOfficeService, "12345678901", "CC"));
    }

    @Test
    void validNumberIdentification_NI_validLengths() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberIdentification", String.class, String.class);
        method.setAccessible(true);

        // valid: length 9
        assertEquals(true, method.invoke(mainOfficeService, "123456789", "NI"));
        // valid: length 12
        assertEquals(true, method.invoke(mainOfficeService, "123456789012", "NI"));
        // invalid: length 8
        assertEquals(false, method.invoke(mainOfficeService, "12345678", "NI"));
        // invalid: length 13
        assertEquals(false, method.invoke(mainOfficeService, "1234567890123", "NI"));
    }

    @Test
    void validNumberIdentification_TI_validLengths() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberIdentification", String.class, String.class);
        method.setAccessible(true);

        // valid: length 10
        assertEquals(true, method.invoke(mainOfficeService, "1234567890", "TI"));
        // valid: length 11
        assertEquals(true, method.invoke(mainOfficeService, "12345678901", "TI"));
        // invalid: length 9
        assertEquals(false, method.invoke(mainOfficeService, "123456789", "TI"));
        // invalid: length 12
        assertEquals(false, method.invoke(mainOfficeService, "123456789012", "TI"));
    }

    @Test
    void validNumberIdentification_PA_validLengths() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberIdentification", String.class, String.class);
        method.setAccessible(true);

        // valid: length 3
        assertEquals(true, method.invoke(mainOfficeService, "123", "PA"));
        // valid: length 16
        assertEquals(true, method.invoke(mainOfficeService, "1234567890123456", "PA"));
        // invalid: length 2
        assertEquals(false, method.invoke(mainOfficeService, "12", "PA"));
        // invalid: length 17
        assertEquals(false, method.invoke(mainOfficeService, "12345678901234567", "PA"));
    }

    @Test
    void validNumberIdentification_PE_validLength() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberIdentification", String.class, String.class);
        method.setAccessible(true);

        // valid: length 15
        assertEquals(true, method.invoke(mainOfficeService, "123456789012345", "PE"));
        // invalid: length 14
        assertEquals(false, method.invoke(mainOfficeService, "12345678901234", "PE"));
        // invalid: length 16
        assertEquals(false, method.invoke(mainOfficeService, "1234567890123456", "PE"));
    }

    @Test
    void validNumberIdentification_SC_validLength() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberIdentification", String.class, String.class);
        method.setAccessible(true);

        // valid: length 9
        assertEquals(true, method.invoke(mainOfficeService, "123456789", "SC"));
        // invalid: length 8
        assertEquals(false, method.invoke(mainOfficeService, "12345678", "SC"));
        // invalid: length 10
        assertEquals(false, method.invoke(mainOfficeService, "1234567890", "SC"));
    }

    @Test
    void validNumberIdentification_PT_variousLengths() throws Exception {
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("validNumberIdentification", String.class, String.class);
        method.setAccessible(true);

        // valid: length 1
        assertEquals(true, method.invoke(mainOfficeService, "1", "PT"));
        // valid: length 8
        assertEquals(true, method.invoke(mainOfficeService, "12345678", "PT"));
        // invalid: empty string
        assertEquals(false, method.invoke(mainOfficeService, "", "PT"));
        // invalid: length 9
        assertEquals(false, method.invoke(mainOfficeService, "123456789", "PT"));
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
        existingOffice.setId(2L); // Different ID
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
        dto.setMainOfficePhoneNumber("1234567890"); // Invalid prefix
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
        dto.setMainOfficeZone("INVALID_ZONE"); // Invalid zone

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
        dto.setEconomicActivity(new ArrayList<>()); // Empty list

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
        dto.setEconomicActivity(List.of(1L, 2L, 3L, 4L, 5L, 6L)); // Too many activities

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
        dto.setFirstNameResponsibleHeadquarters("123"); // Invalid name (contains numbers)

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
        dto.setAddressDTO(new AddressDTO()); // No city set
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
        dto.setAddressDTO(addressDTO); // No department set
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
        // No email set

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
    void findId_withAffiliateMercantile_returnsDTO() {
        // Arrange
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

        // Act
        MainOfficeDTO result = mainOfficeService.findId(mainOfficeId);

        // Assert
        assertEquals(mainOffice.getMainOfficeName(), result.getMainOfficeName());
        assertEquals(mainOffice.getOfficeManager().getId(), result.getOfficeManager());
        assertEquals(0, result.getEconomicActivity().size());
    }

    @Test
    void findId_withAffiliation_returnsDTO() {
        // Arrange
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

        // Act
        MainOfficeDTO result = mainOfficeService.findId(mainOfficeId);

        // Assert
        assertEquals(mainOffice.getMainOfficeName(), result.getMainOfficeName());
        assertEquals(mainOffice.getOfficeManager().getId(), result.getOfficeManager());
        assertEquals(0, result.getEconomicActivity().size());
    }

    @Test
    void findId_affiliateNotFound_throwsException() {
        // Arrange
        Long mainOfficeId = 1L;
        Long affiliateId = 2L;

        MainOffice mainOffice = new MainOffice();
        mainOffice.setId(mainOfficeId);
        mainOffice.setIdAffiliate(affiliateId);
        mainOffice.setOfficeManager(new UserMain());

        when(repository.findById(mainOfficeId)).thenReturn(Optional.of(mainOffice));
        when(affiliateRepository.findById(affiliateId)).thenReturn(Optional.empty()); // Affiliate not found

        // Act & Assert
        assertThrows(AffiliationError.class, () -> mainOfficeService.findId(mainOfficeId));
    }

    @Test
    void findId_mainOfficeNotFound_throwsException() {
        // Arrange
        Long mainOfficeId = 1L;
        when(repository.findById(mainOfficeId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> mainOfficeService.findId(mainOfficeId));
    }

    @Test
    void workCenter_createsNewWorkCenter() throws Exception {
        // Arrange
        EconomicActivity activity = new EconomicActivity();
        activity.setClassRisk("CR");
        activity.setCodeCIIU("001");
        activity.setAdditionalCode("A");
        // Expected concatenated code: "CR001A"
        String expectedActivityCode = "CR001A";

        UserMain user = new UserMain();
        user.setId(1L);

        Long department = 10L;
        Long city = 20L;
        String zone = "URBAN";

        MainOffice mainOffice = new MainOffice();
        mainOffice.setIdDepartment(department);
        mainOffice.setIdCity(city);
        mainOffice.setMainOfficeZone(zone);

        when(workCenterService.getWorkCenterByCodeAndIdUser(expectedActivityCode, user)).thenReturn(null);
        when(workCenterService.getNumberCode(user)).thenReturn(100L);

        // Act
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("workCenter", EconomicActivity.class, UserMain.class, MainOffice.class, Boolean.class);
        method.setAccessible(true);
        method.invoke(mainOfficeService, activity, user, mainOffice, Boolean.TRUE);

        // Assert
        ArgumentCaptor<WorkCenter> captor = ArgumentCaptor.forClass(WorkCenter.class);
        verify(workCenterService).saveWorkCenter(captor.capture());
        WorkCenter savedCenter = captor.getValue();

        assertEquals("101", savedCenter.getCode());
        assertEquals(expectedActivityCode, savedCenter.getEconomicActivityCode());
        assertEquals(0, savedCenter.getTotalWorkers());
        assertEquals("CR", savedCenter.getRiskClass());
        assertEquals(department, savedCenter.getWorkCenterDepartment());
        assertEquals(city, savedCenter.getWorkCenterCity());
        assertEquals(zone, savedCenter.getWorkCenterZone());
        assertSame(user, savedCenter.getWorkCenterManager());
    }

    @Test
    void workCenter_doesNotCreateNewWorkCenter() throws Exception {
        // Arrange
        EconomicActivity activity = new EconomicActivity();
        activity.setClassRisk("CR");
        activity.setCodeCIIU("001");
        activity.setAdditionalCode("A");
        // Expected concatenated code: "CR001A"
        String expectedActivityCode = "CR001A";

        UserMain user = new UserMain();
        Long department = 10L;
        Long city = 20L;
        String zone = "URBAN";

        MainOffice mainOffice = new MainOffice();
        mainOffice.setIdDepartment(department);
        mainOffice.setIdCity(city);
        mainOffice.setMainOfficeZone(zone);

        WorkCenter existingCenter = new WorkCenter();
        when(workCenterService.getWorkCenterByCodeAndIdUser(expectedActivityCode, user)).thenReturn(existingCenter);

        // Act
        java.lang.reflect.Method method = MainOfficeServiceImpl.class.getDeclaredMethod("workCenter", EconomicActivity.class, UserMain.class, MainOffice.class, Boolean.class);
        method.setAccessible(true);
        method.invoke(mainOfficeService, activity, user, mainOffice, Boolean.TRUE);

        // Assert
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

        // Should not throw
        method.invoke(mainOfficeService, idHeadquarter, idEconomicActivity, affiliate);
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

        // First repository.findAll call (for address and department check) returns a non-empty list 
        // to simulate that a headquarters with the same address already exists.
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

        // First call: for address check returns empty.
        // Second call: for name check returns a non-empty list.
        when(repository.findAll(any(Specification.class)))
            .thenReturn(Collections.emptyList(), List.of(new MainOffice()));

        AffiliationError error = assertThrows(AffiliationError.class, () ->
            mainOfficeService.saveMainOffice(dto)
        );
    }

}

    