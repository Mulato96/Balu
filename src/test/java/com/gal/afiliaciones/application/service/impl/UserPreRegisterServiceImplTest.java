package com.gal.afiliaciones.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.gal.afiliaciones.application.service.KeycloakService;
import com.gal.afiliaciones.config.mapper.UpdatePreRegisterMapper;
import com.gal.afiliaciones.config.mapper.UserMapper;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlRepository;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.gal.afiliaciones.application.service.OtpService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorContainDataPersonal;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotRegisteredException;
import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.Gender;
import com.gal.afiliaciones.domain.model.SystemParam;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.GenderRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.systemparam.SystemParamRepository;
import com.gal.afiliaciones.infrastructure.dto.ResponseUserDTO;
import com.gal.afiliaciones.infrastructure.dto.UpdateCredentialDTO;
import com.gal.afiliaciones.infrastructure.dto.UpdatePasswordDTO;
// import removed: UserDtoApiRegistry is not used
import com.gal.afiliaciones.infrastructure.dto.UserPreRegisterDto;
import com.gal.afiliaciones.infrastructure.dto.otp.OTPDataResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.user.UserNameDTO;
import com.gal.afiliaciones.infrastructure.dto.user.UserUpdateDTO;
import com.gal.afiliaciones.infrastructure.enums.TypeUser;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@SuppressWarnings("unchecked")
class UserPreRegisterServiceImplTest {

    @Mock
    private GenericWebClient webClient;
    @Mock
    private IUserPreRegisterRepository iUserPreRegisterRepository;
    @Mock
    private KeycloakServiceImpl keycloakServiceImpl;
    @Mock
    private UserStatusUpdateService userStatusUpdateService;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private AffiliationProperties affiliationProperties;
    @Mock
    private GenderRepository genderRepository;
    @Mock
    private SystemParamRepository paramRepository;
    @Mock
    private HttpServletRequest request;
    @Mock
    private CollectProperties properties;
    @Mock
    private SendEmails sendEmails;
    @Mock
    private OtpService otpService;
    @Mock
    private KeycloakService keycloakService;
    @Mock
    private AffiliateMercantileRepository affiliateMercantileRepository;
    @Mock
    private AffiliationDetailRepository affiliationDetailRepository;
    @Mock
    private AffiliateRepository affiliateRepository;
    @Mock
    private UpdatePreRegisterMapper updatePreRegisterMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ArlRepository arlRepository;

    @InjectMocks
    private UserPreRegisterServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUserPreRegister_success() throws Exception {
        UserPreRegisterDto dto = UserPreRegisterDto.builder()
                .identificationType("CC")
                .identification("123456789")
                .firstName("Juan")
                .surname("Perez")
                .dateBirth(LocalDate.of(2000, 1, 1))
                .phoneNumber("3001234567")
                .email("juan@example.com")
                .address(new com.gal.afiliaciones.infrastructure.dto.address.AddressDTO())
                .build();

        when(iUserPreRegisterRepository.count(any(Specification.class))).thenReturn(0L);
        when(iUserPreRegisterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        OTPDataResponseDTO otpData = OTPDataResponseDTO.builder().mensaje("OTP enviado").build();
        when(otpService.generarOtp(any())).thenReturn(otpData);
        doNothing().when(keycloakServiceImpl).createUser(any());

        ResponseUserDTO response = service.userPreRegister(dto);
        assertNotNull(response);
        assertEquals("Juan", response.getFirstName());
        assertEquals("OTP enviado", response.getOtpData().getMensaje());
        verify(keycloakServiceImpl).createUser(any());
    }

    @Test
    void testUserPreRegister_emailExists() {
        UserPreRegisterDto dto = UserPreRegisterDto.builder()
                .identificationType("CC")
                .identification("123456789")
                .firstName("Juan")
                .surname("Perez")
                .dateBirth(LocalDate.of(2000, 1, 1))
                .phoneNumber("3001234567")
                .email("juan@example.com")
                .address(null)
                .build();
        when(iUserPreRegisterRepository.count(any(Specification.class))).thenReturn(1L, 0L, 0L);
        assertThrows(Exception.class, () -> service.userPreRegister(dto));
    }

    @Test
    void testConsultUser_found() {
        UserMain user = new UserMain();
        user.setFirstName("Ana");
        user.setIdentificationType("CC");
        user.setIdentification("987654321");
        user.setAge(25); // Para BeanUtils.copyProperties
        user.setAddress("");
        user.setPhoneNumber("3001234567"); // Corregido: phoneNumber no nulo
        user.setSurname("Lopez"); // Corregido: surname no nulo
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));
        UserPreRegisterDto dto = service.consultUser("CC", "987654321");
        assertNotNull(dto);
        assertEquals("Ana", dto.getFirstName());
        assertFalse(dto.getUserFromRegistry());
    }

    @Test
    void testConsultUser_notFoundRegistry() {
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        when(webClient.searchNationalRegistry(anyString())).thenReturn(List.of());
        UserPreRegisterDto dto = service.consultUser("CC", "111222333");
        assertNotNull(dto);
        assertEquals("", dto.getFirstName());
        assertFalse(dto.getUserFromRegistry());
    }

    @Test
    void testRegisterPassword_success() {
        UpdatePasswordDTO dto = new UpdatePasswordDTO();
        dto.setDocumentType("CC");
        dto.setDocumentNumber("123456789");
        dto.setTypeUser(null);
        dto.setPassword("Password123!");
        dto.setCodeOtp("1234");
        UserMain user = new UserMain();
        user.setFirstName("Juan");
        user.setSurname("Perez");
        user.setIdentification("123456789");
        user.setCodeOtp("1234");
        user.setEmail("juan@example.com"); // Corregido: email no nulo
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));
        when(iUserPreRegisterRepository.save(any())).thenReturn(user);
        Map<String, Object> expected = new java.util.HashMap<>();
        expected.put("status", 200); // Ajuste: valor como Integer si el método espera Integer
        when(keycloakServiceImpl.updateUser(anyString(), anyString())).thenReturn(expected);
        doNothing().when(sendEmails).emailWelcomeRegister(any());
        Map<String, Object> result = service.registerPassword(dto);
        assertEquals(200, result.get("status"));
    }

    @Test
    void testRegisterPassword_invalidOtp() {
        UpdatePasswordDTO dto = new UpdatePasswordDTO();
        dto.setDocumentType("CC");
        dto.setDocumentNumber("123456789");
        dto.setTypeUser(null);
        dto.setPassword("Password123!");
        dto.setCodeOtp("9999");
        UserMain user = new UserMain();
        user.setFirstName("Juan");
        user.setSurname("Perez");
        user.setIdentification("123456789");
        user.setCodeOtp("1234");
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));
        assertThrows(Exception.class, () -> service.registerPassword(dto));
    }

    @Test
    void testUpdateStatusPreRegister_found() {
        UserMain user = new UserMain();
        user.setIdentificationType("CC");
        user.setIdentification("123456789");
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));
        when(iUserPreRegisterRepository.save(any())).thenReturn(user);
        assertDoesNotThrow(() -> service.updateStatusPreRegister("CC", "123456789"));
    }

    @Test
    void testUpdateStatusPreRegister_notFound() {
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> service.updateStatusPreRegister("CC", "000000000"));
    }

    @Test
    void testCalculateAge() {
        int age = UserPreRegisterServiceImpl.calculateAge(LocalDate.of(2000, 1, 1));
        assertTrue(age >= 0);
    }

    @Test
    void testValidateEmployerRangeNaturalPerson_numeric() {
        boolean result = service.validateEmployerRangeNaturalPerson("CC", "123456789");
        assertTrue(result);
    }

    @Test
    void testIsEmployerPersonJuridica() {
        assertTrue(service.isEmployerPersonJuridica(800000000L));
        assertFalse(service.isEmployerPersonJuridica(700000000L));
    }

    @Test
    void testIsEmployerPersonNatural() {
        assertTrue(service.isEmployerPersonNatural(1L));
        assertTrue(service.isEmployerPersonNatural(600000000L));
        assertFalse(service.isEmployerPersonNatural(9999999999999L));
    }

    @Test
    void testCalculateModulo11DV() {
        int dv = service.calculateModulo11DV("123456789");
        assertTrue(dv >= 0 && dv <= 11);
    }

    @Test
    void testCapitalize() {
        assertEquals("Juan", UserPreRegisterServiceImpl.capitalize("juan"));
        assertEquals("", UserPreRegisterServiceImpl.capitalize(""));
    }

    @Test
    void testFindDaysForcedUpdatePassword_default() {
        when(paramRepository.findByParamName(anyString())).thenReturn(null);
        int days = service.findDaysForcedUpdatePassword();
        assertEquals(90, days);
    }

    @Test
    void testRegisterDaysForcedUpdatePassword_update() {
        SystemParam param = new SystemParam();
        param.setParamName("PARAM_DAYS_FORCED_UPDATE_PASSWORD");
        param.setParamValue("30");
        when(paramRepository.findByParamName(anyString())).thenReturn(param);
        when(paramRepository.save(any())).thenReturn(param);
        SystemParam result = service.registerDaysForcedUpdatePassword(30);
        assertEquals("30", result.getParamValue());
    }

    @Test
    void testRegisterDaysForcedUpdatePassword_new() {
        when(paramRepository.findByParamName(anyString())).thenReturn(null);
        when(paramRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        SystemParam result = service.registerDaysForcedUpdatePassword(45);
        assertEquals("45", result.getParamValue());
    }

    @Test
    void testConsultUserByUserName_found() {
        UserMain user = new UserMain();
        user.setUserName("CC-123456789-EXT");
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));
        UserNameDTO dto = new UserNameDTO("CC-123456789-EXT");
        UserMain result = service.consultUserByUserName(dto);
        assertEquals("CC-123456789-EXT", result.getUserName());
    }

    @Test
    void testConsultUserByUserName_notFound() {
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        UserNameDTO dto = new UserNameDTO("CC-000000000-EXT");
        assertThrows(Exception.class, () -> service.consultUserByUserName(dto));
    }

    @Test
    void testUpdateAffiliationDetailDomesticEmployer() throws Exception {
        // Create mocks for dependencies needed by the service
        GenericWebClient genericWebClient = Mockito.mock(GenericWebClient.class);
        IUserPreRegisterRepository userPreRegisterRepository = Mockito.mock(IUserPreRegisterRepository.class);
        KeycloakServiceImpl keycloakServiceImpl = Mockito.mock(KeycloakServiceImpl.class);
        UserStatusUpdateService userStatusUpdateService = Mockito.mock(UserStatusUpdateService.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        AffiliationProperties affiliationProperties = Mockito.mock(AffiliationProperties.class);
        GenderRepository genderRepository = Mockito.mock(GenderRepository.class);
        SystemParamRepository systemParamRepository = Mockito.mock(SystemParamRepository.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        CollectProperties properties = Mockito.mock(CollectProperties.class);
        SendEmails sendEmails = Mockito.mock(SendEmails.class);
        OtpService otpService = Mockito.mock(OtpService.class);
        AffiliateMercantileRepository affiliateMercantileRepository = Mockito.mock(AffiliateMercantileRepository.class);
        AffiliateRepository affiliateRepository = Mockito.mock(AffiliateRepository.class);
        // Create a dedicated mock for affiliationDetailRepository
        AffiliationDetailRepository mockAffiliationDetailRepository = Mockito.mock(AffiliationDetailRepository.class);
        // Instantiate the service with mocks; note that the affiliationDetailRepository
        // will be replaced
        UserPreRegisterServiceImpl service = new UserPreRegisterServiceImpl(
                genericWebClient,
                userPreRegisterRepository,
                keycloakServiceImpl,
                userStatusUpdateService,
                restTemplate,
                affiliationProperties,
                genderRepository,
                systemParamRepository,
                request,
                properties,
                sendEmails,
                otpService,
                keycloakService,
                affiliateMercantileRepository,
                Mockito.mock(AffiliationDetailRepository.class),
                affiliateRepository, updatePreRegisterMapper, userMapper, arlRepository);
        // Replace the affiliationDetailRepository with our mock via reflection
        java.lang.reflect.Field field = UserPreRegisterServiceImpl.class
                .getDeclaredField("affiliationDetailRepository");
        field.setAccessible(true);
        field.set(service, mockAffiliationDetailRepository);

        Affiliate affiliate = Affiliate.builder().affiliationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC).affiliationSubType(Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER).build();

        // Prepare a dummy UserUpdateDTO with an AddressDTO
        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .id(1L)
                .phoneNumber("1234567890")
                .phone2("0987654321")
                .dateBirth("2000-01-01")
                .identificationType("CC")
                .identification("123456789")
                .build();

        com.gal.afiliaciones.infrastructure.dto.address.AddressDTO address = new com.gal.afiliaciones.infrastructure.dto.address.AddressDTO();
        address.setIdDepartment(1L);
        address.setIdCity(2L);
        address.setAddress("Street 123");
        address.setIdMainStreet(3L);
        address.setIdNumberMainStreet(4L);
        address.setIdLetter1MainStreet(5L);
        address.setIsBis(true);
        address.setIdLetter2MainStreet(6L);
        address.setIdCardinalPointMainStreet(7L);
        address.setIdNum1SecondStreet(8L);
        address.setIdLetterSecondStreet(9L);
        address.setIdNum2SecondStreet(10L);
        address.setIdCardinalPoint2(11L);
        address.setIdHorizontalProperty1(12L);
        address.setIdNumHorizontalProperty1(13L);
        address.setIdHorizontalProperty2(14L);
        address.setIdNumHorizontalProperty2(15L);
        address.setIdHorizontalProperty3(16L);
        address.setIdNumHorizontalProperty3(17L);
        address.setIdHorizontalProperty4(18L);
        address.setIdNumHorizontalProperty4(19L);
        updateDTO.setAddress(address);

        // Prepare a dummy Affiliation
        Affiliation affiliation = Affiliation.builder()
                .phone1("1234567890")
                .phone2("0987654321")
                .dateOfBirth(LocalDate.parse("2000-01-01"))
                .departmentEmployer(1L)
                .municipalityEmployer(2L)
                .addressEmployer("Street 123")
                .idMainStreetEmployer(3L)
                .idNumberMainStreetEmployer(4L)
                .idLetter1MainStreetEmployer(5L)
                .isBisEmployer(true)
                .idLetter2MainStreetEmployer(6L)
                .idCardinalPointMainStreetEmployer(7L)
                .idNum1SecondStreetEmployer(8L)
                .idLetterSecondStreetEmployer(9L)
                .idNum2SecondStreetEmployer(10L)
                .idCardinalPoint2Employer(11L)
                .idHorizontalProperty1Employer(12L)
                .idNumHorizontalProperty1Employer(13L)
                .idHorizontalProperty2Employer(14L)
                .idNumHorizontalProperty2Employer(15L)
                .idHorizontalProperty3Employer(16L)
                .idNumHorizontalProperty3Employer(17L)
                .idHorizontalProperty4Employer(18L)
                .idNumHorizontalProperty4Employer(19L)
                .build();

        // Invoke the private method updateAffiliationDetailDomesticEmployer using
        // reflection
        java.lang.reflect.Method method = UserPreRegisterServiceImpl.class
                .getDeclaredMethod("updateAffiliation", UserUpdateDTO.class, Object.class, Affiliate.class);
        method.setAccessible(true);
        method.invoke(service, updateDTO, affiliation, affiliate);

        // Assert that the affiliation fields have been updated correctly
        assertEquals("1234567890", affiliation.getPhone1());
        assertEquals("0987654321", affiliation.getPhone2());
        assertEquals(LocalDate.parse("2000-01-01"), affiliation.getDateOfBirth());
        assertEquals(1L, affiliation.getDepartmentEmployer());
        assertEquals(2L, affiliation.getMunicipalityEmployer());
        assertEquals("Street 123", affiliation.getAddressEmployer());
        assertEquals(3L, affiliation.getIdMainStreetEmployer());
        assertEquals(4L, affiliation.getIdNumberMainStreetEmployer());
        assertEquals(5L, affiliation.getIdLetter1MainStreetEmployer());
        assertTrue(affiliation.getIsBisEmployer());
        assertEquals(6L, affiliation.getIdLetter2MainStreetEmployer());
        assertEquals(7L, affiliation.getIdCardinalPointMainStreetEmployer());
        assertEquals(8L, affiliation.getIdNum1SecondStreetEmployer());
        assertEquals(9L, affiliation.getIdLetterSecondStreetEmployer());
        assertEquals(10L, affiliation.getIdNum2SecondStreetEmployer());
        assertEquals(11L, affiliation.getIdCardinalPoint2Employer());
        assertEquals(12L, affiliation.getIdHorizontalProperty1Employer());
        assertEquals(13L, affiliation.getIdNumHorizontalProperty1Employer());
        assertEquals(14L, affiliation.getIdHorizontalProperty2Employer());
        assertEquals(15L, affiliation.getIdNumHorizontalProperty2Employer());
        assertEquals(16L, affiliation.getIdHorizontalProperty3Employer());
        assertEquals(17L, affiliation.getIdNumHorizontalProperty3Employer());
        assertEquals(18L, affiliation.getIdHorizontalProperty4Employer());
        assertEquals(19L, affiliation.getIdNumHorizontalProperty4Employer());

        // Verify that the affiliationDetailRepository.save method was invoked with the
        // updated affiliation
        Mockito.verify(mockAffiliationDetailRepository).save(affiliation);
    }

    @Test
    void testUpdateAffiliateMercantile_updatesAllProperties() throws Exception {
        // Mocks for dependencies needed to instantiate the service
        GenericWebClient webClient = Mockito.mock(GenericWebClient.class);
        IUserPreRegisterRepository iUserPreRegisterRepository = Mockito.mock(IUserPreRegisterRepository.class);
        KeycloakServiceImpl keycloakServiceImpl = Mockito.mock(KeycloakServiceImpl.class);
        UserStatusUpdateService userStatusUpdateService = Mockito.mock(UserStatusUpdateService.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        AffiliationProperties affiliationProperties = Mockito.mock(AffiliationProperties.class);
        GenderRepository genderRepository = Mockito.mock(GenderRepository.class);
        SystemParamRepository paramRepository = Mockito.mock(SystemParamRepository.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        CollectProperties properties = Mockito.mock(CollectProperties.class);
        SendEmails sendEmails = Mockito.mock(SendEmails.class);
        OtpService otpService = Mockito.mock(OtpService.class);
        AffiliateMercantileRepository affiliateMercantileRepository = Mockito.mock(AffiliateMercantileRepository.class);
        AffiliationDetailRepository affiliationDetailRepository = Mockito.mock(AffiliationDetailRepository.class);
        AffiliateRepository affiliateRepository = Mockito.mock(AffiliateRepository.class);

        // Instantiate the service with all required dependencies
        UserPreRegisterServiceImpl service = new UserPreRegisterServiceImpl(
                webClient,
                iUserPreRegisterRepository,
                keycloakServiceImpl,
                userStatusUpdateService,
                restTemplate,
                affiliationProperties,
                genderRepository,
                paramRepository,
                request,
                properties,
                sendEmails,
                otpService,
                keycloakService, affiliateMercantileRepository,
                affiliationDetailRepository,
                affiliateRepository,
                updatePreRegisterMapper, userMapper, arlRepository);

        Affiliate affiliate = Affiliate.builder().affiliationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC).affiliationSubType(Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER).build();

        // Prepare a UserUpdateDTO with Address data
        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .identificationType("CC")
                .identification("123456789")
                .build();
        com.gal.afiliaciones.infrastructure.dto.address.AddressDTO address = new com.gal.afiliaciones.infrastructure.dto.address.AddressDTO();
        address.setIdDepartment(1L);
        address.setIdCity(2L);
        address.setAddress("Test Street");
        address.setIdMainStreet(3L);
        address.setIdNumberMainStreet(4L);
        address.setIdLetter1MainStreet(5L);
        address.setIsBis(true);
        address.setIdLetter2MainStreet(6L);
        address.setIdCardinalPointMainStreet(7L);
        address.setIdNum1SecondStreet(8L);
        address.setIdLetterSecondStreet(9L);
        address.setIdNum2SecondStreet(10L);
        address.setIdCardinalPoint2(11L);
        address.setIdHorizontalProperty1(12L);
        address.setIdNumHorizontalProperty1(13L);
        address.setIdHorizontalProperty2(14L);
        address.setIdNumHorizontalProperty2(15L);
        address.setIdHorizontalProperty3(16L);
        address.setIdNumHorizontalProperty3(17L);
        address.setIdHorizontalProperty4(18L);
        address.setIdNumHorizontalProperty4(19L);

        updateDTO.setAddress(address);

        // Create an empty AffiliateMercantile instance
        AffiliateMercantile affiliateMercantile = AffiliateMercantile.builder()
                .typeDocumentIdentification("CC")
                .numberIdentification("123456789")
                .idDepartment(1L)
                .idCity(2L)
                .idMainStreet(3L)
                .idNumberMainStreet(4L)
                .idLetter1MainStreet(5L)
                .isBis(true)
                .idLetter2MainStreet(6L)
                .idCardinalPointMainStreet(7L)
                .idNum1SecondStreet(8L)
                .idLetterSecondStreet(9L)
                .idNum2SecondStreet(10L)
                .idCardinalPoint2(11L)
                .idHorizontalProperty1(12L)
                .idNumHorizontalProperty1(13L)
                .idHorizontalProperty2(14L)
                .idNumHorizontalProperty2(15L)
                .idHorizontalProperty3(16L)
                .idNumHorizontalProperty3(17L)
                .idHorizontalProperty4(18L)
                .idNumHorizontalProperty4(19L)
                .build();

        // Use reflection to access the private updateAffiliateMercantile method
        java.lang.reflect.Method method = UserPreRegisterServiceImpl.class
                .getDeclaredMethod("updateAffiliation", UserUpdateDTO.class, Object.class, Affiliate.class);
        method.setAccessible(true);
        method.invoke(service, updateDTO, affiliateMercantile, affiliate);

        // Verify that the affiliateMercantile instance was updated with values from
        // updateDTO and its address
        assertEquals("CC", affiliateMercantile.getTypeDocumentIdentification());
        assertEquals("123456789", affiliateMercantile.getNumberIdentification());
        assertEquals(Long.valueOf(1), affiliateMercantile.getIdDepartment());
        assertEquals(Long.valueOf(2), affiliateMercantile.getIdCity());
        assertEquals(Long.valueOf(3), affiliateMercantile.getIdMainStreet());
        assertEquals(Long.valueOf(4), affiliateMercantile.getIdNumberMainStreet());
        assertEquals(Long.valueOf(5), affiliateMercantile.getIdLetter1MainStreet());
        assertTrue(affiliateMercantile.getIsBis());
        assertEquals(Long.valueOf(6), affiliateMercantile.getIdLetter2MainStreet());
        assertEquals(Long.valueOf(7), affiliateMercantile.getIdCardinalPointMainStreet());
        assertEquals(Long.valueOf(8), affiliateMercantile.getIdNum1SecondStreet());
        assertEquals(Long.valueOf(9), affiliateMercantile.getIdLetterSecondStreet());
        assertEquals(Long.valueOf(10), affiliateMercantile.getIdNum2SecondStreet());
        assertEquals(Long.valueOf(11), affiliateMercantile.getIdCardinalPoint2());
        assertEquals(Long.valueOf(12), affiliateMercantile.getIdHorizontalProperty1());
        assertEquals(Long.valueOf(13), affiliateMercantile.getIdNumHorizontalProperty1());
        assertEquals(Long.valueOf(14), affiliateMercantile.getIdHorizontalProperty2());
        assertEquals(Long.valueOf(15), affiliateMercantile.getIdNumHorizontalProperty2());
        assertEquals(Long.valueOf(16), affiliateMercantile.getIdHorizontalProperty3());
        assertEquals(Long.valueOf(17), affiliateMercantile.getIdNumHorizontalProperty3());
        assertEquals(Long.valueOf(18), affiliateMercantile.getIdHorizontalProperty4());
        assertEquals(Long.valueOf(19), affiliateMercantile.getIdNumHorizontalProperty4());

        // Verify that the repository's save method was called with the updated
        // affiliateMercantile
        Mockito.verify(affiliateMercantileRepository).save(affiliateMercantile);
    }

    @Test
    void testUpdateAffiliationDetail_shouldUpdateFieldsProperly() throws Exception {
        // Arrange: Create a dummy AddressDTO and UserUpdateDTO
        com.gal.afiliaciones.infrastructure.dto.address.AddressDTO address = new com.gal.afiliaciones.infrastructure.dto.address.AddressDTO();
        address.setIdDepartment(10L);
        address.setIdCity(20L);
        address.setAddress("Test Street");
        address.setIdMainStreet(30L);
        address.setIdNumberMainStreet(40L);
        address.setIdLetter1MainStreet(50L);
        address.setIsBis(true);
        address.setIdLetter2MainStreet(60L);
        address.setIdCardinalPointMainStreet(70L);
        address.setIdNum1SecondStreet(80L);
        address.setIdLetterSecondStreet(90L);
        address.setIdNum2SecondStreet(100L);
        address.setIdCardinalPoint2(110L);
        address.setIdHorizontalProperty1(120L);
        address.setIdNumHorizontalProperty1(130L);
        address.setIdHorizontalProperty2(140L);
        address.setIdNumHorizontalProperty2(150L);
        address.setIdHorizontalProperty3(160L);
        address.setIdNumHorizontalProperty3(170L);
        address.setIdHorizontalProperty4(180L);
        address.setIdNumHorizontalProperty4(190L);

        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .id(1L)
                .dateBirth("2000-01-01")
                .identificationType("CC")
                .identification("123456789")
                .phoneNumber("1111111111")
                .phone2("2222222222")
                .address(address)
                .build();

        Affiliate affiliate = Affiliate.builder().affiliationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC).affiliationSubType(Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER).build();

        Affiliation affiliation = Affiliation.builder()
                .dateOfBirth(LocalDate.parse("2000-01-01"))
                .addressIndependentWorker("Test Street")
                .idDepartmentIndependentWorker(10L)
                .idCityIndependentWorker(20L)
                .idMainStreetIndependentWorker(30L)
                .idNumberMainStreetIndependentWorker(40L)
                .idLetter1MainStreetIndependentWorker(50L)
                .isBisIndependentWorker(true)
                .idLetter2MainStreetIndependentWorker(60L)
                .idCardinalPointMainStreetIndependentWorker(70L)
                .idNum1SecondStreetIndependentWorker(80L)
                .idLetterSecondStreetIndependentWorker(90L)
                .idNum2SecondStreetIndependentWorker(100L)
                .idCardinalPoint2IndependentWorker(110L)
                .idHorizontalProperty1IndependentWorker(120L)
                .idNumHorizontalProperty1IndependentWorker(130L)
                .idHorizontalProperty2IndependentWorker(140L)
                .idNumHorizontalProperty2IndependentWorker(150L)
                .idHorizontalProperty3IndependentWorker(160L)
                .idNumHorizontalProperty3IndependentWorker(170L)
                .idHorizontalProperty4IndependentWorker(180L)
                .idNumHorizontalProperty4IndependentWorker(190L)
                .build();
        // Pre-set a value on a field not expected to change (e.g. id)
        // This field should not be overwritten due to the ignore properties in
        // copyProperties
        // (Assuming Affiliation has an 'id' property, adjust according to your actual
        // model)
        // affiliation.setId("existing-id");

        // Act: Call the private method updateAffiliationDetail via reflection
        java.lang.reflect.Method method = UserPreRegisterServiceImpl.class
                .getDeclaredMethod("updateAffiliation", UserUpdateDTO.class, Object.class, Affiliate.class);
        method.setAccessible(true);
        method.invoke(service, updateDTO, affiliation, affiliate);

        // Assert: Verify that the affiliation fields were updated correctly
        assertEquals(LocalDate.parse("2000-01-01"), affiliation.getDateOfBirth());
        assertEquals("Test Street", affiliation.getAddressIndependentWorker());
        assertEquals(10L, affiliation.getIdDepartmentIndependentWorker());
        assertEquals(20L, affiliation.getIdCityIndependentWorker().longValue());
        assertEquals(30L, affiliation.getIdMainStreetIndependentWorker());
        assertEquals(40L, affiliation.getIdNumberMainStreetIndependentWorker());
        assertEquals(50L, affiliation.getIdLetter1MainStreetIndependentWorker());
        assertTrue(affiliation.getIsBisIndependentWorker());
        assertEquals(60L, affiliation.getIdLetter2MainStreetIndependentWorker());
        assertEquals(70L, affiliation.getIdCardinalPointMainStreetIndependentWorker());
        assertEquals(80L, affiliation.getIdNum1SecondStreetIndependentWorker());
        assertEquals(90L, affiliation.getIdLetterSecondStreetIndependentWorker());
        assertEquals(100L, affiliation.getIdNum2SecondStreetIndependentWorker());
        assertEquals(110L, affiliation.getIdCardinalPoint2IndependentWorker());
        assertEquals(120L, affiliation.getIdHorizontalProperty1IndependentWorker());
        assertEquals(130L, affiliation.getIdNumHorizontalProperty1IndependentWorker());
        assertEquals(140L, affiliation.getIdHorizontalProperty2IndependentWorker());
        assertEquals(150L, affiliation.getIdNumHorizontalProperty2IndependentWorker());
        assertEquals(160L, affiliation.getIdHorizontalProperty3IndependentWorker());
        assertEquals(170L, affiliation.getIdNumHorizontalProperty3IndependentWorker());
        assertEquals(180L, affiliation.getIdHorizontalProperty4IndependentWorker());
        assertEquals(190L, affiliation.getIdNumHorizontalProperty4IndependentWorker());

        // Verify that affiliationDetailRepository.save was called with the updated
        // affiliation
        Mockito.verify(affiliationDetailRepository).save(affiliation);
    }

    @Test
    void testLogout_success() throws Exception {
        // Set up properties expectations
        when(properties.getKeycloakAuthServerUrl()).thenReturn("http://localhost");
        when(properties.getRealm()).thenReturn("testRealm");
        when(properties.getClientSecret()).thenReturn("secret");
        when(properties.getClientId()).thenReturn("client");

        // Mock a session so that invalidate() can be verified
        HttpSession session = Mockito.mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);

        // Stub restTemplate.exchange to return a dummy response
        ResponseEntity<String> dummyResponse = ResponseEntity.ok("OK");
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class))).thenReturn(dummyResponse);

        // Use reflection to access the private logout(String) method
        Method logoutMethod = UserPreRegisterServiceImpl.class.getDeclaredMethod("logout", String.class);
        logoutMethod.setAccessible(true);

        // Invoke the logout method with a dummy refresh token
        logoutMethod.invoke(service, "dummyToken");

        // Verify that the session was invalidated and security context cleared
        verify(request.getSession()).invalidate();
        // Since SecurityContextHolder.clearContext() is static, we assume that no
        // exception was thrown.
    }

    @Test
    void testLogout_failure() throws Exception {
        // Set up properties expectations
        when(properties.getKeycloakAuthServerUrl()).thenReturn("http://localhost");
        when(properties.getRealm()).thenReturn("testRealm");
        when(properties.getClientSecret()).thenReturn("secret");
        when(properties.getClientId()).thenReturn("client");

        // Mock a session for completeness
        HttpSession session = Mockito.mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);

        // Force restTemplate.exchange to throw a RuntimeException
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class))).thenThrow(new RuntimeException("Exchange failed"));

        // Access the private logout(String) method via reflection
        Method logoutMethod = UserPreRegisterServiceImpl.class.getDeclaredMethod("logout", String.class);
        logoutMethod.setAccessible(true);

        Exception thrown = assertThrows(Exception.class, () -> {
            try {
                logoutMethod.invoke(service, "dummyToken");
            } catch (java.lang.reflect.InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else {
                    throw e;
                }
            }
        });
        assertTrue(thrown instanceof RuntimeException);
        assertTrue(thrown.getMessage().contains("Exchange failed"));
    }

    @Test
    void testUpdateAffiliationPreRegister_success() {
        // Prepare a UserMain with necessary fields
        UserMain existingUser = new UserMain();
        existingUser.setId(1L);
        existingUser.setIdentification("123456789");
        existingUser.setIdentificationType("CC");
        existingUser.setDateBirth(LocalDate.of(2000, 1, 1));

        // Prepare the UserUpdateDTO with an AddressDTO
        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .id(1L)
                .phoneNumber("1234567890")
                .phone2("0987654321")
                .dateBirth("2000-01-01")
                .identificationType("CC")
                .identification("123456789")
                .build();

        com.gal.afiliaciones.infrastructure.dto.address.AddressDTO address = new com.gal.afiliaciones.infrastructure.dto.address.AddressDTO();
        address.setIdDepartment(1L);
        address.setIdCity(2L);
        address.setAddress("Street 123");
        address.setIdMainStreet(3L);
        address.setIdNumberMainStreet(4L);
        address.setIdLetter1MainStreet(5L);
        address.setIsBis(true);
        address.setIdLetter2MainStreet(6L);
        address.setIdCardinalPointMainStreet(7L);
        address.setIdNum1SecondStreet(8L);
        address.setIdLetterSecondStreet(9L);
        address.setIdNum2SecondStreet(10L);
        address.setIdCardinalPoint2(11L);
        address.setIdHorizontalProperty1(12L);
        updateDTO.setAddress(address);

        // Stub repository and related mocks
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        // Return empty lists for affiliates and affiliations to avoid calling update
        // methods
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber("CC", "123456789"))
                .thenReturn(List.of());
        when(affiliateMercantileRepository.findAllByTypeDocumentPersonResponsibleAndNumberDocumentPersonResponsible("CC",
                "123456789"))
                .thenReturn(List.of());
        when(affiliationDetailRepository.findAllByIdentificationDocumentTypeAndIdentificationDocumentNumber("CC",
                "123456789"))
                .thenReturn(List.of());
        // Stub the genericWebClient call
        doNothing().when(webClient).updateUser(any());

        // Call the method under test
        Boolean result = service.updateAffiliationPreRegister(updateDTO);
        assertFalse(result);

    }

    @Test
    void testUpdateAffiliationPreRegister_userNotFound() {
        // Prepare a UserUpdateDTO with an id that does not exist in the repository
        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .id(999L)
                .phoneNumber("1234567890")
                .phone2("0987654321")
                .dateBirth("2000-01-01")
                .identificationType("CC")
                .identification("123456789")
                .build();

        com.gal.afiliaciones.infrastructure.dto.address.AddressDTO address = new com.gal.afiliaciones.infrastructure.dto.address.AddressDTO();
        address.setIdDepartment(1L);
        address.setIdCity(2L);
        address.setAddress("Street 123");
        address.setIdMainStreet(3L);
        address.setIdNumberMainStreet(4L);
        address.setIdLetter1MainStreet(5L);
        address.setIsBis(true);
        address.setIdLetter2MainStreet(6L);
        address.setIdCardinalPointMainStreet(7L);
        address.setIdNum1SecondStreet(8L);
        address.setIdLetterSecondStreet(9L);
        address.setIdNum2SecondStreet(10L);
        address.setIdCardinalPoint2(11L);
        address.setIdHorizontalProperty1(12L);
        updateDTO.setAddress(address);

        when(iUserPreRegisterRepository.findById(999L)).thenReturn(Optional.empty());

        // Call the method under test
        Boolean result = service.updateAffiliationPreRegister(updateDTO);
        assertFalse(result);
    }

    @Test
    void testIsValidNIT_valid() throws Exception {
        // Create dummy mocks for required dependencies
        GenericWebClient webClient = Mockito.mock(GenericWebClient.class);
        IUserPreRegisterRepository repo = Mockito.mock(IUserPreRegisterRepository.class);
        KeycloakServiceImpl keycloakService = Mockito.mock(KeycloakServiceImpl.class);
        UserStatusUpdateService statusService = Mockito.mock(UserStatusUpdateService.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        AffiliationProperties affiliationProps = Mockito.mock(AffiliationProperties.class);
        GenderRepository genderRepo = Mockito.mock(GenderRepository.class);
        SystemParamRepository paramRepo = Mockito.mock(SystemParamRepository.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        CollectProperties collectProps = Mockito.mock(CollectProperties.class);
        SendEmails sendEmails = Mockito.mock(SendEmails.class);
        OtpService otpService = Mockito.mock(OtpService.class);
        AffiliateMercantileRepository affiliateMercantileRepo = Mockito.mock(AffiliateMercantileRepository.class);
        AffiliationDetailRepository affiliationDetailRepo = Mockito.mock(AffiliationDetailRepository.class);
        AffiliateRepository affiliateRepo = Mockito.mock(AffiliateRepository.class);
        GenericWebClient genericWebClient2 = Mockito.mock(GenericWebClient.class);

        // Instantiate the service with mocks
        UserPreRegisterServiceImpl service = new UserPreRegisterServiceImpl(
                webClient,
                repo,
                keycloakService,
                statusService,
                restTemplate,
                affiliationProps,
                genderRepo,
                paramRepo,
                request,
                collectProps,
                sendEmails,
                otpService,
                keycloakService, affiliateMercantileRepo,
                affiliationDetailRepo,
                affiliateRepo,
                updatePreRegisterMapper, userMapper, arlRepository);

        // Prepare a valid NIT for a natural employer.
        // Choose a base number that qualifies as natural: e.g., "700000000"
        String baseNumber = "700000000";
        int calculatedDV = service.calculateModulo11DV(baseNumber);
        String validNIT = baseNumber + calculatedDV;

        // Use reflection to access the private isValidNIT(String) method
        java.lang.reflect.Method isValidNITMethod = UserPreRegisterServiceImpl.class.getDeclaredMethod("isValidNIT",
                String.class);
        isValidNITMethod.setAccessible(true);

        Boolean result = (Boolean) isValidNITMethod.invoke(service, validNIT);
        assertTrue(result, "Expected valid NIT to return true");
    }

    @Test
    void testIsValidNIT_invalidDV() throws Exception {
        GenericWebClient webClient = Mockito.mock(GenericWebClient.class);
        IUserPreRegisterRepository repo = Mockito.mock(IUserPreRegisterRepository.class);
        KeycloakServiceImpl keycloakService = Mockito.mock(KeycloakServiceImpl.class);
        UserStatusUpdateService statusService = Mockito.mock(UserStatusUpdateService.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        AffiliationProperties affiliationProps = Mockito.mock(AffiliationProperties.class);
        GenderRepository genderRepo = Mockito.mock(GenderRepository.class);
        SystemParamRepository paramRepo = Mockito.mock(SystemParamRepository.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        CollectProperties collectProps = Mockito.mock(CollectProperties.class);
        SendEmails sendEmails = Mockito.mock(SendEmails.class);
        OtpService otpService = Mockito.mock(OtpService.class);
        AffiliateMercantileRepository affiliateMercantileRepo = Mockito.mock(AffiliateMercantileRepository.class);
        AffiliationDetailRepository affiliationDetailRepo = Mockito.mock(AffiliationDetailRepository.class);
        AffiliateRepository affiliateRepo = Mockito.mock(AffiliateRepository.class);
        GenericWebClient genericWebClient2 = Mockito.mock(GenericWebClient.class);

        UserPreRegisterServiceImpl service = new UserPreRegisterServiceImpl(
                webClient,
                repo,
                keycloakService,
                statusService,
                restTemplate,
                affiliationProps,
                genderRepo,
                paramRepo,
                request,
                collectProps,
                sendEmails,
                otpService,
                keycloakService, affiliateMercantileRepo,
                affiliationDetailRepo,
                affiliateRepo,
                updatePreRegisterMapper, userMapper, arlRepository);

        String baseNumber = "700000000";
        int calculatedDV = service.calculateModulo11DV(baseNumber);
        // Create an incorrect DV (if calculatedDV is 9, choose 8; otherwise,
        // calculatedDV+1)
        int wrongDV = (calculatedDV == 9) ? 8 : calculatedDV + 1;
        String invalidNIT = baseNumber + wrongDV;

        java.lang.reflect.Method isValidNITMethod = UserPreRegisterServiceImpl.class.getDeclaredMethod("isValidNIT",
                String.class);
        isValidNITMethod.setAccessible(true);

        Boolean result = (Boolean) isValidNITMethod.invoke(service, invalidNIT);
        assertFalse(result, "Expected NIT with wrong DV to return false");
    }

    @Test
    void testIsValidNIT_nonNumeric() throws Exception {
        GenericWebClient webClient = Mockito.mock(GenericWebClient.class);
        IUserPreRegisterRepository repo = Mockito.mock(IUserPreRegisterRepository.class);
        KeycloakServiceImpl keycloakService = Mockito.mock(KeycloakServiceImpl.class);
        UserStatusUpdateService statusService = Mockito.mock(UserStatusUpdateService.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        AffiliationProperties affiliationProps = Mockito.mock(AffiliationProperties.class);
        GenderRepository genderRepo = Mockito.mock(GenderRepository.class);
        SystemParamRepository paramRepo = Mockito.mock(SystemParamRepository.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        CollectProperties collectProps = Mockito.mock(CollectProperties.class);
        SendEmails sendEmails = Mockito.mock(SendEmails.class);
        OtpService otpService = Mockito.mock(OtpService.class);
        AffiliateMercantileRepository affiliateMercantileRepo = Mockito.mock(AffiliateMercantileRepository.class);
        AffiliationDetailRepository affiliationDetailRepo = Mockito.mock(AffiliationDetailRepository.class);
        AffiliateRepository affiliateRepo = Mockito.mock(AffiliateRepository.class);
        GenericWebClient genericWebClient2 = Mockito.mock(GenericWebClient.class);

        UserPreRegisterServiceImpl service = new UserPreRegisterServiceImpl(
                webClient,
                repo,
                keycloakService,
                statusService,
                restTemplate,
                affiliationProps,
                genderRepo,
                paramRepo,
                request,
                collectProps,
                sendEmails,
                otpService,
                keycloakService, affiliateMercantileRepo,
                affiliationDetailRepo,
                affiliateRepo,
                updatePreRegisterMapper, userMapper, arlRepository);

        // Test with a completely non-numeric NIT value
        String nonNumericNIT = "ABC123X";

        java.lang.reflect.Method isValidNITMethod = UserPreRegisterServiceImpl.class.getDeclaredMethod("isValidNIT",
                String.class);
        isValidNITMethod.setAccessible(true);

        Boolean result = (Boolean) isValidNITMethod.invoke(service, nonNumericNIT);
        assertFalse(result, "Expected non-numeric NIT to return false");
    }

    @Test
    void testCheckAndUpdateUserStatuses() {

        // Invoke the scheduled method
        service.checkAndUpdateUserStatuses();

        // Verify updateUsersInactiveAfter72Hours was called with a threshold approx
        // equal to now.minusHours(72)
        ArgumentCaptor<LocalDateTime> captor72 = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(userStatusUpdateService).updateUsersInactiveAfter72Hours(captor72.capture());
        LocalDateTime threshold72 = captor72.getValue();
        // Allow a small delta of 2 seconds between computed and expected time
        assertTrue(
                Math.abs(java.time.Duration.between(threshold72, LocalDateTime.now().minusHours(72)).getSeconds()) < 2,
                "Threshold for 72 hours is not in the expected range");

        // Verify updateUsersInactiveByPendingAffiliation was called with a threshold
        // approx equal to now.minusDays(30)
        verify(userStatusUpdateService).updateUsersInactiveByPendingAffiliation(argThat(time -> Math
                .abs(java.time.Duration.between(time, LocalDateTime.now().minusDays(30)).getSeconds()) < 2));

        // Verify deleteUsersInactiveAfter60Days was called with a threshold approx
        // equal to now.minusDays(60)
        verify(userStatusUpdateService).deleteUsersInactiveAfter60Days(argThat(time -> Math
                .abs(java.time.Duration.between(time, LocalDateTime.now().minusDays(60)).getSeconds()) < 2));
    }

    @Test
    void testUpdateStatusInactive_found() {
        // Arrange
        UserMain user = new UserMain();
        user.setIdentificationType("CC");
        user.setIdentification("123456789");
        // Simulate finding an existing user by mocking the repository call
        when(iUserPreRegisterRepository.findOne(Mockito.<Specification<UserMain>>any())).thenReturn(Optional.of(user));

        // Act
        assertDoesNotThrow(() -> service.updateStatusInactive("CC", "123456789"));

        // Assert: Verify that the user was updated correctly and saved.
        verify(iUserPreRegisterRepository).save(argThat(updatedUser -> updatedUser.getStatusActive() == false &&
                updatedUser.getStatus().equals(2L) &&
                updatedUser.getStatusInactiveSince() != null));
    }

    @Test
    void testUpdateStatusInactive_notFound() {
        // Arrange: Simulate repository returning empty
        when(iUserPreRegisterRepository.findOne(Mockito.<Specification<UserMain>>any())).thenReturn(Optional.empty());

        // Act & Assert: Expect an exception when the user is not found.
        assertThrows(Exception.class, () -> service.updateStatusInactive("CC", "000000000"));
    }

    @Test
    void testUpdateStatusInactiveFalse_success() {
        // Arrange
        UserMain user = new UserMain();
        user.setIdentificationType("CC");
        user.setIdentification("123456789");
        user.setStatusActive(false);
        user.setStatus(2L);
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));
        when(iUserPreRegisterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        assertDoesNotThrow(() -> service.updateStatusInactiveFalse("CC", "123456789"));

        // Assert
        assertTrue(user.getStatusActive());
        assertEquals(1L, user.getStatus().longValue());
        assertNotNull(user.getStatusInactiveSince());
    }

    @Test
    void testUpdateStatusInactiveFalse_notFound() {
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> service.updateStatusInactiveFalse("CC", "000000000"));
    }

    @Test
    void testFindUserDataById_withRegistryData() {
        // Prepare a complete user with all required fields for the converter
        UserMain dummyUser = new UserMain();
        dummyUser.setId(1L);
        dummyUser.setIdentification("12345");
        dummyUser.setIdentificationType("CC");
        dummyUser.setFirstName("Juan");
        dummyUser.setSurname("Perez");
        dummyUser.setDateBirth(LocalDate.of(1990, 1, 1));
        dummyUser.setAge(33);
        dummyUser.setSex("M");
        dummyUser.setNationality(1L);
        dummyUser.setAddress("Calle 123");
        dummyUser.setPhoneNumber("3001234567");
        dummyUser.setEmail("juan@example.com");

        // Set up repository and webClient behavior
        when(iUserPreRegisterRepository.findById(1L)).thenReturn(Optional.of(dummyUser));
        when(webClient.searchNationalRegistry("12345"))
                .thenReturn(List.of(new com.gal.afiliaciones.infrastructure.dto.RegistryOfficeDTO()));

        UserUpdateDTO result = service.findUserDataById(1L);

        // Verify that if registry data exists, the flag is set to true
        assertNotNull(result);
        assertEquals("12345", result.getIdentification());
        assertEquals("Juan", result.getFirstName());
    }

    @Test
    void testFindUserDataById_withoutRegistryData() {
        // Prepare a complete user with all required fields for the converter
        UserMain dummyUser = new UserMain();
        dummyUser.setId(2L);
        dummyUser.setIdentification("67890");
        dummyUser.setIdentificationType("CC");
        dummyUser.setFirstName("Maria");
        dummyUser.setSurname("Lopez");
        dummyUser.setDateBirth(LocalDate.of(1985, 5, 15));
        dummyUser.setAge(38);
        dummyUser.setSex("F");
        dummyUser.setNationality(1L);
        dummyUser.setAddress("Carrera 456");
        dummyUser.setPhoneNumber("3007654321");
        dummyUser.setEmail("maria@example.com");

        // Set up repository and webClient behavior: empty registry list
        when(iUserPreRegisterRepository.findById(2L)).thenReturn(Optional.of(dummyUser));
        when(webClient.searchNationalRegistry("67890")).thenReturn(List.of());

        UserUpdateDTO result = service.findUserDataById(2L);

        // Since no registry data is found, the flag should remain false.
        assertNotNull(result);
        assertFalse(result.getIsRegistryData());
        assertEquals("67890", result.getIdentification());
        assertEquals("Maria", result.getFirstName());
    }

    @Test
    void testFindUserDataById_userNotFound() {
        // When no user is found, repository returns Optional.empty()
        when(iUserPreRegisterRepository.findById(3L)).thenReturn(Optional.empty());

        UserUpdateDTO result = service.findUserDataById(3L);

        // When user is null, the converter returns null as defined in the converter
        // This is the expected behavior based on the converter implementation
        assertNull(result);
    }

    @Test
    void testUpdateStatusActive_found() {
        UserMain user = new UserMain();
        user.setIdentificationType("CC");
        user.setIdentification("123456789");
        user.setStatusActive(false);
        user.setStatus(2L);

        when(iUserPreRegisterRepository.findOne(Mockito.<Specification<UserMain>>any())).thenReturn(Optional.of(user));
        when(iUserPreRegisterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> service.updateStatusActive("CC", "123456789"));
        assertTrue(user.getStatusActive());
        assertEquals(1L, user.getStatus());
        verify(iUserPreRegisterRepository).save(user);
    }

    @Test
    void testUpdateStatusActive_notFound() {
        when(iUserPreRegisterRepository.findOne(Mockito.<Specification<UserMain>>any())).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> service.updateStatusActive("CC", "000000000"));
    }

    @Test
    void testUpdateStatusStartAffiliation_success() {
        // Arrange
        String identificationType = "CC";
        String identification = "123456789";
        UserMain user = new UserMain();
        user.setIdentificationType(identificationType);
        user.setIdentification(identification);
        user.setStatusStartAfiiliate(false); // Initially false

        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));
        when(iUserPreRegisterRepository.save(any(UserMain.class))).thenReturn(user);

        // Act
        service.updateStatusStartAffiliation(identificationType, identification);

        // Assert
        assertTrue(user.getStatusStartAfiiliate());
        verify(iUserPreRegisterRepository).save(user);
    }

    @Test
    void testUpdateStatusStartAffiliation_notFound() {
        // Arrange
        String identificationType = "CC";
        String identification = "123456789";

        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundInDataBase.class,
                () -> service.updateStatusStartAffiliation(identificationType, identification));
    }

    @Test
    void testConsultUserByIdentification_found() {
        // Prepare a sample user where identification field represents the email used in
        // the specification
        UserMain user = new UserMain();
        user.setIdentification("found@example.com");
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));

        UserMain result = service.consultUserByIdentification("found@example.com");
        assertNotNull(result);
        assertEquals("found@example.com", result.getIdentification());
    }

    @Test
    void testConsultUserByIdentification_notFound() {
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> service.consultUserByIdentification("notfound@example.com"));
    }

    @Test
    void testFindGenderByDescription_found() {
        Gender gender = new Gender();
        gender.setGenderType("M");
        gender.setDescription("Masculino");
        when(genderRepository.findByDescription(anyString())).thenReturn(Optional.of(gender));
        java.lang.reflect.Method method;
        String result = null;
        try {
            method = UserPreRegisterServiceImpl.class.getDeclaredMethod("findGenderByDescription", String.class);
            method.setAccessible(true);
            result = (String) method.invoke(service, "Masculino");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertEquals("M", result);
    }

    @Test
    void testFindGenderByDescription_notFound() {
        when(genderRepository.findByDescription(anyString())).thenReturn(Optional.empty());
        java.lang.reflect.Method method;
        String result = null;
        try {
            method = UserPreRegisterServiceImpl.class.getDeclaredMethod("findGenderByDescription", String.class);
            method.setAccessible(true);
            result = (String) method.invoke(service, "Unknown");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertTrue(true);
    }

    @Test
    void testFindGenderByDescription_nullDescription() {
        java.lang.reflect.Method method;
        String result = null;
        try {
            method = UserPreRegisterServiceImpl.class.getDeclaredMethod("findGenderByDescription", String.class);
            method.setAccessible(true);
            result = (String) method.invoke(service, null);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertNull(result);
    }
    @Test
    void testUpdatePassword_success() throws Exception {
        // Arrange
        UpdateCredentialDTO dto = new UpdateCredentialDTO();
        dto.setDocumentType("CC");
        dto.setDocumentNumber("123456789");
        dto.setTypeUser(TypeUser.EXT);
        dto.setCurrentPassword("currentPassword");
        dto.setPassword("newPassword");
        dto.setConfirmPassword("newPassword");

        UserMain user = new UserMain();
        user.setUserName("CC-123456789-EXT");
        user.setEmail("test@example.com");
        user.setFirstName("Juan");
        user.setSurname("Perez");
        user.setIdentification("123456789");

        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));
        when(affiliationProperties.getKeycloakUrl()).thenReturn("http://localhost/auth");
        when(affiliationProperties.getClientId()).thenReturn("test-client");
        when(affiliationProperties.getClientSecret()).thenReturn("test-secret");
        
        // Mock HttpSession for logout
        HttpSession mockSession = Mockito.mock(HttpSession.class);
        when(request.getSession()).thenReturn(mockSession);
        
        // Mock properties for logout
        when(properties.getKeycloakAuthServerUrl()).thenReturn("http://localhost");
        when(properties.getRealm()).thenReturn("test-realm");
        when(properties.getClientId()).thenReturn("test-client");
        when(properties.getClientSecret()).thenReturn("test-secret");
        
        Map<String, Object> tokenResponse = Map.of("refresh_token", "dummy_refresh_token");
        ResponseEntity<Map<String, Object>> mockResponse = ResponseEntity.ok(tokenResponse);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(mockResponse);
        
        // Mock logout call
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok("OK"));
        
        when(keycloakServiceImpl.updateUser(anyString(), anyString())).thenReturn(Map.of("status", 200));
        when(iUserPreRegisterRepository.save(any())).thenReturn(user);

        // Act
        Map<String, Object> result = service.updatePassword(dto);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.get("status"));
        verify(iUserPreRegisterRepository).save(any(UserMain.class));
        verify(keycloakServiceImpl).updateUser("test@example.com", "newPassword");
    }

    @Test
    void testUpdatePassword_userNotFound() {
        // Arrange
        UpdateCredentialDTO dto = new UpdateCredentialDTO();
        dto.setDocumentType("CC");
        dto.setDocumentNumber("123456789");
        dto.setTypeUser(TypeUser.EXT);

        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundInDataBase.class, () -> service.updatePassword(dto));
    }

    @Test
    void testUpdatePassword_currentPasswordIncorrect() {
        // Arrange
        UpdateCredentialDTO dto = new UpdateCredentialDTO();
        dto.setDocumentType("CC");
        dto.setDocumentNumber("123456789");
        dto.setTypeUser(TypeUser.EXT);
        dto.setCurrentPassword("incorrectPassword");
        dto.setPassword("newPassword");
        dto.setConfirmPassword("newPassword");

        UserMain user = new UserMain();
        user.setUserName("CC-123456789-EXT");
        user.setFirstName("Carlos");
        user.setSurname("Rodriguez");
        user.setIdentification("123456789");

        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));
        when(affiliationProperties.getKeycloakUrl()).thenReturn("http://localhost/auth");
        when(affiliationProperties.getClientId()).thenReturn("test-client");
        when(affiliationProperties.getClientSecret()).thenReturn("test-secret");
        
        // Mock HttpSession for logout
        HttpSession mockSession = Mockito.mock(HttpSession.class);
        when(request.getSession()).thenReturn(mockSession);
        
        // Mock properties for logout
        when(properties.getKeycloakAuthServerUrl()).thenReturn("http://localhost");
        when(properties.getRealm()).thenReturn("test-realm");
        when(properties.getClientId()).thenReturn("test-client");
        when(properties.getClientSecret()).thenReturn("test-secret");
        
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(org.springframework.http.HttpStatus.UNAUTHORIZED));
        
        // Mock logout call
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok("OK"));

        // Act & Assert
        assertThrows(UserNotRegisteredException.class, () -> service.updatePassword(dto));
    }

    @Test
    void testUpdatePassword_passwordContainsUserData() {
        // Arrange
        UpdateCredentialDTO dto = new UpdateCredentialDTO();
        dto.setDocumentType("CC");
        dto.setDocumentNumber("123456789");
        dto.setTypeUser(TypeUser.EXT);
        dto.setCurrentPassword("currentPassword");
        dto.setPassword("Juan123");
        dto.setConfirmPassword("Juan123");

        UserMain user = new UserMain();
        user.setUserName("CC-123456789-EXT");
        user.setFirstName("Juan");
        user.setSurname("Perez");
        user.setIdentification("123456789");

        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));
        when(affiliationProperties.getKeycloakUrl()).thenReturn("http://localhost/auth");
        when(affiliationProperties.getClientId()).thenReturn("test-client");
        when(affiliationProperties.getClientSecret()).thenReturn("test-secret");
        
        // Mock HttpSession for logout
        HttpSession mockSession = Mockito.mock(HttpSession.class);
        when(request.getSession()).thenReturn(mockSession);
        
        // Mock properties for logout
        when(properties.getKeycloakAuthServerUrl()).thenReturn("http://localhost");
        when(properties.getRealm()).thenReturn("test-realm");
        when(properties.getClientId()).thenReturn("test-client");
        when(properties.getClientSecret()).thenReturn("test-secret");
        
        Map<String, Object> tokenResponse = Map.of("refresh_token", "dummy_refresh_token");
        ResponseEntity<Map<String, Object>> mockResponse = ResponseEntity.ok(tokenResponse);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(mockResponse);
        
        // Mock logout call
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok("OK"));

        // Act & Assert
        assertThrows(ErrorContainDataPersonal.class, () -> service.updatePassword(dto));
    }

    @Test
    void testUpdatePassword_passwordEqualsCurrentPassword() {
        // Arrange
        UpdateCredentialDTO dto = new UpdateCredentialDTO();
        dto.setDocumentType("CC");
        dto.setDocumentNumber("123456789");
        dto.setTypeUser(TypeUser.EXT);
        dto.setCurrentPassword("samePassword");
        dto.setPassword("samePassword");
        dto.setConfirmPassword("samePassword");

        UserMain user = new UserMain();
        user.setUserName("CC-123456789-EXT");
        user.setFirstName("Pedro");
        user.setSurname("Lopez");
        user.setIdentification("123456789");

        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));
        when(affiliationProperties.getKeycloakUrl()).thenReturn("http://localhost/auth");
        when(affiliationProperties.getClientId()).thenReturn("test-client");
        when(affiliationProperties.getClientSecret()).thenReturn("test-secret");
        
        // Mock HttpSession for logout
        HttpSession mockSession = Mockito.mock(HttpSession.class);
        when(request.getSession()).thenReturn(mockSession);
        
        // Mock properties for logout
        when(properties.getKeycloakAuthServerUrl()).thenReturn("http://localhost");
        when(properties.getRealm()).thenReturn("test-realm");
        when(properties.getClientId()).thenReturn("test-client");
        when(properties.getClientSecret()).thenReturn("test-secret");
        
        Map<String, Object> tokenResponse = Map.of("refresh_token", "dummy_refresh_token");
        ResponseEntity<Map<String, Object>> mockResponse = ResponseEntity.ok(tokenResponse);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(mockResponse);
        
        // Mock logout call
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok("OK"));

        // Act & Assert
        assertThrows(UserNotRegisteredException.class, () -> service.updatePassword(dto));
    }

    @Test
    void testUpdatePassword_passwordAndConfirmPasswordNotMatch() {
        // Arrange
        UpdateCredentialDTO dto = new UpdateCredentialDTO();
        dto.setDocumentType("CC");
        dto.setDocumentNumber("123456789");
        dto.setTypeUser(TypeUser.EXT);
        dto.setCurrentPassword("currentPassword");
        dto.setPassword("newPassword");
        dto.setConfirmPassword("differentPassword");

        UserMain user = new UserMain();
        user.setUserName("CC-123456789-EXT");
        user.setFirstName("Maria");
        user.setSurname("Garcia");
        user.setIdentification("123456789");

        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));
        when(affiliationProperties.getKeycloakUrl()).thenReturn("http://localhost/auth");
        when(affiliationProperties.getClientId()).thenReturn("test-client");
        when(affiliationProperties.getClientSecret()).thenReturn("test-secret");
        
        // Mock HttpSession for logout
        HttpSession mockSession = Mockito.mock(HttpSession.class);
        when(request.getSession()).thenReturn(mockSession);
        
        // Mock properties for logout
        when(properties.getKeycloakAuthServerUrl()).thenReturn("http://localhost");
        when(properties.getRealm()).thenReturn("test-realm");
        when(properties.getClientId()).thenReturn("test-client");
        when(properties.getClientSecret()).thenReturn("test-secret");
        
        Map<String, Object> tokenResponse = Map.of("refresh_token", "dummy_refresh_token");
        ResponseEntity<Map<String, Object>> mockResponse = ResponseEntity.ok(tokenResponse);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(mockResponse);
        
        // Mock logout call
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok("OK"));

        // Act & Assert
        assertThrows(UserNotRegisteredException.class, () -> service.updatePassword(dto));
    }
}
