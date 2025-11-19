package com.gal.afiliaciones.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.application.service.ConsultCertificateByUserService;
import com.gal.afiliaciones.application.service.OtpService;
import com.gal.afiliaciones.config.ex.card.ErrorGeneratedCard;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorFindCard;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorNumberAttemptsExceeded;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.Card;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ICardRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dto.card.ResponseGrillaCardsDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.UserNotAffiliatedDTO;
import com.gal.afiliaciones.infrastructure.dto.certificate.ValidCodeCertificateDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

class GenerateCardAffiliatedServiceImplTest {

    @Mock
    private IUserPreRegisterRepository iUserPreRegisterRepository;
    @Mock
    private OtpService otpService;
    @Mock
    private AffiliateRepository affiliateRepository;
    @Mock
    private ICardRepository iCardRepository;
    @Mock
    private GenericWebClient genericWebClient;
    @Mock
    private ArlInformationDao arlInformationDao;
    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;
    @Mock
    private AffiliationDependentRepository dependentRepository;
    @Mock
    private AffiliateMercantileRepository affiliateMercantileRepository;
    @Mock
    private ConsultCertificateByUserService consultCertificateByUserService;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    AffiliationDetailRepository affiliationDetailRepository;

    @InjectMocks
    private GenerateCardAffiliatedServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void consultUserCard_shouldThrowUserNotFoundInDataBase_whenNoActiveAffiliate() {
        when(affiliateRepository.findAll(any(Example.class))).thenReturn(Collections.emptyList());

        UserNotFoundInDataBase ex = assertThrows(UserNotFoundInDataBase.class,
                () -> service.consultUserCard("123", "CC"));
    }

    @Test
    void createCardUser_shouldReturnListOfResponseGrillaCardsDTO() {
        ValidCodeCertificateDTO dto = new ValidCodeCertificateDTO();
        dto.setIdentificationType("CC");
        dto.setIdentification("123");
        dto.setCode("code");
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        affiliate.setFiledNumber("F123");
        affiliate.setDocumentNumber("123");
        affiliate.setDocumentType("CC");
        affiliate.setCompany("Company");
        affiliate.setNitCompany("NIT");
        affiliate.setAffiliationDate(LocalDateTime.now());
        when(affiliateRepository.findAll(any(Example.class))).thenReturn(List.of(affiliate));
        Affiliation affiliation = new Affiliation();
        affiliation.setContractEndDate(LocalDate.now());
        affiliation.setFirstName("John");
        affiliation.setSecondSurname("Doe");
        affiliation.setSurname("Smith");
        when(affiliationRepository.findByFiledNumber(any())).thenReturn(Optional.of(affiliation));
        when(iCardRepository.findAll(any(Example.class))).thenReturn(Collections.emptyList());
        ArlInformation arl = new ArlInformation();
        arl.setName("ARL");
        arl.setEmail("email");
        arl.setAddress("address");
        arl.setWebsite("web");
        arl.setPhoneNumber("12345");
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arl));
        when(iCardRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(iCardRepository.findAll(any(Example.class))).thenReturn(List.of(new Card()));

        List<ResponseGrillaCardsDTO> result = service.createCardUser(dto);

        assertNotNull(result);
    }

    @Test
    void consultCard_shouldThrowErrorFindCard_whenCardNotFound() {
        when(iCardRepository.findOne(any(Example.class))).thenReturn(Optional.empty());

        assertThrows(ErrorFindCard.class, () -> service.consultCard("1"));
    }

    @Test
    void createCardWithoutOtp_shouldThrowAffiliateNotFoundException_whenAffiliateNotFound() {
        when(affiliateRepository.findByFiledNumber(any())).thenReturn(Optional.empty());

        assertThrows(AffiliateNotFoundException.class, () -> service.createCardWithoutOtp("F123"));
    }

    @Test
    void createCardDependent_shouldReturnResponseEntityWithCard() {
        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("F123");
        affiliate.setDocumentNumber("123");
        affiliate.setDocumentType("CC");
        affiliate.setAffiliationSubType("DEPENDENT");
        affiliate.setCompany("Company");
        affiliate.setNitCompany("NIT");
        affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        affiliate.setAffiliationDate(LocalDateTime.now());
        ArlInformation arl = new ArlInformation();
        arl.setName("ARL");
        arl.setEmail("email");
        arl.setAddress("address");
        arl.setWebsite("web");
        arl.setPhoneNumber("12345");
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arl));
        Card card = new Card();
        card.setId(1L);
        card.setCompany("Company");
        card.setDateAffiliation(LocalDate.now());
        card.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        when(iCardRepository.save(any())).thenReturn(card);

        ResponseEntity<ResponseGrillaCardsDTO> response = service.createCardDependent(affiliate, "John", "A", "Doe",
                "B");

        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void consultCardByAffiliate_shouldThrowErrorFindCard_whenNoCardFound() {
        when(iCardRepository.findAll(any(Example.class))).thenReturn(Collections.emptyList());

        assertThrows(ErrorFindCard.class, () -> service.consultCardByAffiliate("F123"));
    }

    @Test
    void validUser_shouldThrowErrorNumberAttemptsExceeded_whenAttemptsExceeded() {
        String docNum = "123";
        try {
            java.lang.reflect.Field cacheField = service.getClass().getDeclaredField("usersNotAffiliationCache");
            cacheField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, UserNotAffiliatedDTO> cache = (Map<String, UserNotAffiliatedDTO>) cacheField.get(service);
            cache.put(docNum, new UserNotAffiliatedDTO(LocalTime.now(), "CC", 2));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThrows(ErrorNumberAttemptsExceeded.class, () -> {
            service.consultUserCard(docNum, "CC");
        });
    }

    @Test
    void createCardWithoutOtp_shouldThrowUserNotFoundInDataBase_whenUserNotFound() {
        Affiliate affiliate = new Affiliate();
        affiliate.setDocumentNumber("123");
        affiliate.setDocumentType("CC");
        when(affiliateRepository.findByFiledNumber(any())).thenReturn(Optional.of(affiliate));
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        assertThrows(UserNotFoundInDataBase.class, () -> service.createCardWithoutOtp("F123"));
    }

    @Test
    void createCardWithoutOtp_shouldThrowErrorGeneratedCard_whenUserIsCompany() {
        Affiliate affiliate = new Affiliate();
        affiliate.setDocumentNumber("123");
        affiliate.setDocumentType("NI");
        when(affiliateRepository.findByFiledNumber(any())).thenReturn(Optional.of(affiliate));
        com.gal.afiliaciones.domain.model.UserMain user = new com.gal.afiliaciones.domain.model.UserMain();
        user.setIdentificationType("NI");
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));

        assertThrows(ErrorGeneratedCard.class, () -> service.createCardWithoutOtp("F123"));
    }

    @Test
    void createCardWithoutOtp_shouldThrowAffiliationError_whenUserIsNotActive() {
        Affiliate affiliate = new Affiliate();
        affiliate.setDocumentNumber("123");
        affiliate.setDocumentType("CC");
        when(affiliateRepository.findByFiledNumber(any())).thenReturn(Optional.of(affiliate));
        com.gal.afiliaciones.domain.model.UserMain user = new com.gal.afiliaciones.domain.model.UserMain();
        user.setIdentificationType("CC");
        user.setStatusPreRegister(false);
        user.setStatusActive(true);
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));

        assertThrows(com.gal.afiliaciones.config.ex.affiliation.AffiliationError.class,
                () -> service.createCardWithoutOtp("F123"));
    }

    @Test
    void createCardWithoutOtp_shouldThrowAffiliationNotFoundError_whenIndependentAffiliationNotFound() {
        Affiliate affiliate = new Affiliate();
        affiliate.setDocumentNumber("123");
        affiliate.setDocumentType("CC");
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        when(affiliateRepository.findByFiledNumber(any())).thenReturn(Optional.of(affiliate));
        com.gal.afiliaciones.domain.model.UserMain user = new com.gal.afiliaciones.domain.model.UserMain();
        user.setIdentificationType("CC");
        user.setStatusPreRegister(true);
        user.setStatusActive(true);
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));
        when(affiliationRepository.findByFiledNumber(any())).thenReturn(Optional.empty());

        assertThrows(com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError.class,
                () -> service.createCardWithoutOtp("F123"));
    }

    @Test
    void createCardWithoutOtp_shouldThrowAffiliationNotFoundError_whenDependentAffiliationNotFound() {
        Affiliate affiliate = new Affiliate();
        affiliate.setDocumentNumber("123");
        affiliate.setDocumentType("CC");
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        when(affiliateRepository.findByFiledNumber(any())).thenReturn(Optional.of(affiliate));
        com.gal.afiliaciones.domain.model.UserMain user = new com.gal.afiliaciones.domain.model.UserMain();
        user.setIdentificationType("CC");
        user.setStatusPreRegister(true);
        user.setStatusActive(true);
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));
        when(dependentRepository.findByFiledNumber(any())).thenReturn(Optional.empty());

        assertThrows(com.gal.afiliaciones.config.ex.affiliation.AffiliationNotFoundError.class,
                () -> service.createCardWithoutOtp("F123"));
    }

    @Test
    void createCardWithoutOtp_shouldReturnCardList_forIndependentAffiliate() {
        Affiliate affiliate = new Affiliate();
        affiliate.setDocumentNumber("123");
        affiliate.setDocumentType("CC");
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        affiliate.setCompany("Company");
        affiliate.setNitCompany("NIT");
        affiliate.setAffiliationDate(LocalDateTime.now());
        when(affiliateRepository.findByFiledNumber(any())).thenReturn(Optional.of(affiliate));

        com.gal.afiliaciones.domain.model.UserMain user = new com.gal.afiliaciones.domain.model.UserMain();
        user.setIdentification("123");
        user.setIdentificationType("CC");
        user.setStatusPreRegister(true);
        user.setStatusActive(true);
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));

        Affiliation affiliation = new Affiliation();
        affiliation.setContractEndDate(LocalDate.now());
        affiliation.setFirstName("John");
        affiliation.setSurname("Doe");
        when(affiliationRepository.findByFiledNumber(any())).thenReturn(Optional.of(affiliation));

        ArlInformation arl = new ArlInformation();
        arl.setName("ARL");
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arl));
        when(iCardRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList())
                .thenReturn(List.of(new Card()));

        List<ResponseGrillaCardsDTO> result = service.createCardWithoutOtp("F123");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void createCardWithoutOtp_shouldReturnCardList_forDependentAffiliate() {
        Affiliate affiliate = new Affiliate();
        affiliate.setDocumentNumber("123");
        affiliate.setDocumentType("CC");
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        affiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        affiliate.setCompany("Company");
        affiliate.setNitCompany("NIT");
        affiliate.setAffiliationDate(LocalDateTime.now());
        when(affiliateRepository.findByFiledNumber(any())).thenReturn(Optional.of(affiliate));

        com.gal.afiliaciones.domain.model.UserMain user = new com.gal.afiliaciones.domain.model.UserMain();
        user.setIdentification("123");
        user.setIdentificationType("CC");
        user.setStatusPreRegister(true);
        user.setStatusActive(true);
        when(iUserPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));

        com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent affiliationDependent = new com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent();
        affiliationDependent.setEndDate(LocalDate.now());
        affiliationDependent.setFirstName("John");
        affiliationDependent.setSurname("Doe");
        when(dependentRepository.findByFiledNumber(any())).thenReturn(Optional.of(affiliationDependent));

        ArlInformation arl = new ArlInformation();
        arl.setName("ARL");
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arl));
        when(iCardRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList())
                .thenReturn(List.of(new Card()));

        List<ResponseGrillaCardsDTO> result = service.createCardWithoutOtp("F123");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getUserCardDTO_shouldReturnCorrectDTO_whenRetirementDateIsProvided() throws Exception {
        // Prepare an affiliate with necessary fields and a retirement date
        Affiliate affiliate = new Affiliate();
        affiliate.setDocumentNumber("7891011");
        affiliate.setDocumentType("CC");
        affiliate.setAffiliationDate(LocalDateTime.of(2023, 9, 1, 8, 30));
        affiliate.setAffiliationType("DEPENDENT");
        affiliate.setCompany("OtherCompany");
        affiliate.setNitCompany("NIT789");
        affiliate.setAffiliationStatus("Inactive");
        LocalDateTime retirementDate = LocalDateTime.of(2023, 12, 31, 0, 0);

        // Prepare a dummy ArlInformation to be returned by getArlInformation()
        ArlInformation arl = new ArlInformation();
        arl.setName("OtherARL");
        arl.setEmail("other@arl.com");
        arl.setAddress("Other Address");
        arl.setWebsite("other.com");
        arl.setPhoneNumber("67890");
        when(arlInformationDao.findAllArlInformation()).thenReturn(List.of(arl));

        // Invoke the private getUserCardDTO method using reflection
        java.lang.reflect.Method method = service.getClass().getDeclaredMethod("getUserCardDTO",
                String.class, String.class, String.class, String.class, Affiliate.class);
        method.setAccessible(true);
        Object result = method.invoke(service, "Alice", null, "Wonder", "Land", affiliate);

        // Validate the returned UserCardDTO
        com.gal.afiliaciones.infrastructure.dto.card.UserCardDTO dto = (com.gal.afiliaciones.infrastructure.dto.card.UserCardDTO) result;
        // Expect only non-null parts to be concatenated
        assertEquals("Alice Wonder Land", dto.getFullNameWorked());
        assertEquals("7891011", dto.getNumberDocumentWorker());
        assertEquals("CC", dto.getTypeDocumentWorker());
        assertEquals(LocalDate.of(2023, 9, 1), dto.getDateAffiliation());
        assertEquals("DEPENDENT", dto.getTypeAffiliation());
        assertEquals("OtherARL", dto.getNameARL());
        assertEquals("other@arl.com", dto.getEmailARL());
        assertEquals("Other Address", dto.getAddressARL());
        assertEquals("other.com", dto.getPageWebARL());
        assertEquals("OtherCompany", dto.getCompany());
        assertEquals("NIT789", dto.getNitCompany());
        assertEquals("Inactive", dto.getAffiliationStatus());
    }

    @Test
    void consultUserCard_shouldThrowAffiliationError_whenAffiliateIsEmployer() {
        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        employerAffiliate.setAffiliationType(Constant.NAME_CONTRIBUTOR_TYPE_EMPLOYER);
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(List.of(employerAffiliate));

        assertThrows(com.gal.afiliaciones.config.ex.affiliation.AffiliationError.class,
                () -> service.consultUserCard("123", "CC"));
    }

    @Test
    void consultUserCard_shouldReturnDTO_whenActiveAffiliateFound() {
        Affiliate activeAffiliate = new Affiliate();
        activeAffiliate.setAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE);
        activeAffiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(List.of(activeAffiliate));

        ValidCodeCertificateDTO result = service.consultUserCard("123", "CC");

        assertNotNull(result);
        assertEquals("123", result.getIdentification());
        assertEquals("CC", result.getIdentificationType());
        verify(consultCertificateByUserService).findUser(any(ValidCodeCertificateDTO.class),
                eq(Constant.TYPE_AFFILLATE_INDEPENDENT));
    }

    @Test
    void consultUserCard_shouldThrowUserNotFoundInDataBase_whenOnlyInactiveAffiliatesFound() {
        Affiliate inactiveAffiliate = new Affiliate();
        inactiveAffiliate.setAffiliationStatus("Inactive");
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(List.of(inactiveAffiliate));

        assertThrows(UserNotFoundInDataBase.class, () -> service.consultUserCard("123", "CC"));
    }

    @Test
    void consultUserCard_shouldHandleFirstAttemptForNonAffiliatedUser() throws Exception {
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        assertThrows(UserNotFoundInDataBase.class, () -> service.consultUserCard("456", "CE"));

        java.lang.reflect.Field cacheField = service.getClass().getDeclaredField("usersNotAffiliationCache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, UserNotAffiliatedDTO> cache = (Map<String, UserNotAffiliatedDTO>) cacheField.get(service);

        UserNotAffiliatedDTO cachedUser = cache.get("456");
        assertNotNull(cachedUser);
        assertEquals(1, cachedUser.getNumberAttemps());
    }

    @Test
    void findDocumentTypeEmployer_shouldReturnNI_whenNoAffiliateEmployerFound() throws Exception {
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        java.lang.reflect.Method method = service.getClass().getDeclaredMethod("findDocumentTypeEmployer",
                String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(service, "123456789");

        assertEquals(Constant.NI, result);
    }

    @Test
    void findDocumentTypeEmployer_shouldReturnNI_whenAffiliateIsEmployerType() throws Exception {
        Affiliate employerAffiliate = new Affiliate();
        employerAffiliate.setAffiliationType(Constant.TYPE_AFFILIATE_EMPLOYER);
        employerAffiliate.setFiledNumber("F123");
        employerAffiliate.setNitCompany("123456789");
        employerAffiliate.setDocumenTypeCompany(Constant.NI);
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(List.of(employerAffiliate));

        java.lang.reflect.Method method = service.getClass().getDeclaredMethod("findDocumentTypeEmployer",
                String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(service, "123456789");

        assertEquals(Constant.NI, result);
    }

    @Test
    void findDocumentTypeEmployer_shouldReturnDocumentType_whenAffiliateIsNotEmployerAndAffiliationExists()
            throws Exception {
        Affiliate nonEmployerAffiliate = new Affiliate();
        nonEmployerAffiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        nonEmployerAffiliate.setFiledNumber("F123");
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(List.of(nonEmployerAffiliate));

        Affiliation affiliation = new Affiliation();
        affiliation.setIdentificationDocumentType("CC");
        when(affiliationRepository.findByFiledNumber("F123")).thenReturn(Optional.of(affiliation));

        java.lang.reflect.Method method = service.getClass().getDeclaredMethod("findDocumentTypeEmployer",
                String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(service, "123456789");

        assertEquals("CC", result);
    }

    @Test
    void findDocumentTypeEmployer_shouldReturnEmptyString_whenAffiliateIsNotEmployerAndAffiliationNotFound()
            throws Exception {
        Affiliate nonEmployerAffiliate = new Affiliate();
        nonEmployerAffiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        nonEmployerAffiliate.setFiledNumber("F123");
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(List.of(nonEmployerAffiliate));

        when(affiliationRepository.findByFiledNumber("F123")).thenReturn(Optional.empty());

        java.lang.reflect.Method method = service.getClass().getDeclaredMethod("findDocumentTypeEmployer",
                String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(service, "123456789");

        assertEquals("", result);
    }

    @Test
    void findDocumentTypeEmployer_shouldReturnDocumentType_whenMultipleAffiliatesFoundAndFirstIsNotEmployer()
            throws Exception {
        Affiliate firstAffiliate = new Affiliate();
        firstAffiliate.setAffiliationType(Constant.TYPE_AFFILLATE_DEPENDENT);
        firstAffiliate.setFiledNumber("F456");

        Affiliate secondAffiliate = new Affiliate();
        secondAffiliate.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);

        when(affiliateRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(firstAffiliate, secondAffiliate));

        Affiliation affiliation = new Affiliation();
        affiliation.setIdentificationDocumentType("TI");
        when(affiliationRepository.findByFiledNumber("F456")).thenReturn(Optional.of(affiliation));

        java.lang.reflect.Method method = service.getClass().getDeclaredMethod("findDocumentTypeEmployer",
                String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(service, "987654321");

        assertEquals("TI", result);
    }

    @Test
    void findDocumentTypeEmployer_shouldUseCorrectSpecification_whenCalled() throws Exception {
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        java.lang.reflect.Method method = service.getClass().getDeclaredMethod("findDocumentTypeEmployer",
                String.class);
        method.setAccessible(true);
        method.invoke(service, "555666777");

        ArgumentCaptor<Specification<Affiliate>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(affiliateRepository).findAll(specCaptor.capture());

        // Verify that the specification was called (we can't easily verify the exact
        // specification content)
        assertNotNull(specCaptor.getValue());
    }

    @Test
    void findDocumentTypeEmployer_shouldHandleNullFiledNumber_whenAffiliateFound() throws Exception {
        Affiliate affiliateWithNullFiledNumber = new Affiliate();
        affiliateWithNullFiledNumber.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        affiliateWithNullFiledNumber.setFiledNumber(null);
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(List.of(affiliateWithNullFiledNumber));

        when(affiliationRepository.findByFiledNumber(null)).thenReturn(Optional.empty());

        java.lang.reflect.Method method = service.getClass().getDeclaredMethod("findDocumentTypeEmployer",
                String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(service, "123456789");

        assertEquals("", result);
    }

    @Test
    void findDocumentTypeEmployer_shouldReturnNullDocumentType_whenAffiliationHasNullDocumentType() throws Exception {
        Affiliate nonEmployerAffiliate = new Affiliate();
        nonEmployerAffiliate.setAffiliationType(Constant.TYPE_AFFILLATE_INDEPENDENT);
        nonEmployerAffiliate.setFiledNumber("F789");
        when(affiliateRepository.findAll(any(Specification.class))).thenReturn(List.of(nonEmployerAffiliate));

        Affiliation affiliationWithNullDocType = new Affiliation();
        affiliationWithNullDocType.setIdentificationDocumentType(null);
        when(affiliationRepository.findByFiledNumber("F789")).thenReturn(Optional.of(affiliationWithNullDocType));

        java.lang.reflect.Method method = service.getClass().getDeclaredMethod("findDocumentTypeEmployer",
                String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(service, "123456789");

        assertNull(result);
    }

    @Test
    void consultCard_exception_affiliation_not_found() throws JsonProcessingException {

        Card card =  new Card();

        when(iCardRepository.findOne((Specification<Card>) any())).thenReturn(Optional.of(card));

        String fakeResponse = "{\"pdf\":\"fake-pdf-content\"}";

        ObjectNode jsonNode = mock(ObjectNode.class);

        when(objectMapper.readTree(fakeResponse)).thenReturn(jsonNode);
        when(jsonNode.path("pdf")).thenReturn(new ObjectMapper().readTree("\"fake-pdf-content\""));


        AffiliationNotFoundError ex = assertThrows(
                AffiliationNotFoundError.class,
                () -> service.consultCard("123")
        );

        assertNotNull(ex);

    }

    @Test
    void consultCard_exception_affiliation_not_found2() throws JsonProcessingException {

        Card card =  new Card();
        card.setFiledNumber("123");

        when(iCardRepository.findOne((Specification<Card>) any()))
                .thenReturn(Optional.of(card));
        when(affiliationDetailRepository.findByFiledNumber("123"))
                .thenReturn(Optional.empty());

        String fakeResponse = "{\"pdf\":\"fake-pdf-content\"}";

        ObjectNode jsonNode = mock(ObjectNode.class);

        when(objectMapper.readTree(fakeResponse)).thenReturn(jsonNode);
        when(jsonNode.path("pdf")).thenReturn(new ObjectMapper().readTree("\"fake-pdf-content\""));


        AffiliationNotFoundError ex = assertThrows(
                AffiliationNotFoundError.class,
                () -> service.consultCard("123")
        );

        assertNotNull(ex);

    }

    @Test
    void consultCard_exception_affiliation_not_found3() throws JsonProcessingException {

        Card card =  new Card();
        card.setFiledNumber("123");
        card.setTypeAffiliation(Constant.BONDING_TYPE_DEPENDENT);

        when(iCardRepository.findOne((Specification<Card>) any()))
                .thenReturn(Optional.of(card));
        when(affiliationDetailRepository.findByFiledNumber("123"))
                .thenReturn(Optional.empty());

        String fakeResponse = "{\"pdf\":\"fake-pdf-content\"}";

        ObjectNode jsonNode = mock(ObjectNode.class);

        when(objectMapper.readTree(fakeResponse)).thenReturn(jsonNode);
        when(jsonNode.path("pdf")).thenReturn(new ObjectMapper().readTree("\"fake-pdf-content\""));


        AffiliationNotFoundError ex = assertThrows(
                AffiliationNotFoundError.class,
                () -> service.consultCard("123")
        );

        assertNotNull(ex);

    }

    @Test
    void consultCard_exception_affiliation_not_found4() throws JsonProcessingException {

        Card card =  new Card();
        card.setFiledNumber("123");
        card.setTypeAffiliation(Constant.BONDING_TYPE_DEPENDENT);
        AffiliationDependent affiliationDependent = new AffiliationDependent();

        when(dependentRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliationDependent));

        when(iCardRepository.findOne((Specification<Card>) any()))
                .thenReturn(Optional.of(card));
        when(affiliationDetailRepository.findByFiledNumber("123"))
                .thenReturn(Optional.empty());

        String fakeResponse = "{\"pdf\":\"fake-pdf-content\"}";

        ObjectNode jsonNode = mock(ObjectNode.class);

        when(objectMapper.readTree(fakeResponse)).thenReturn(jsonNode);
        when(jsonNode.path("pdf")).thenReturn(new ObjectMapper().readTree("\"fake-pdf-content\""));


        AffiliationNotFoundError ex = assertThrows(
                AffiliationNotFoundError.class,
                () -> service.consultCard("123")
        );

        assertNotNull(ex);

    }

    @Test
    void consultCard_exception_affiliation_not_found5() throws JsonProcessingException {

        Card card =  new Card();
        card.setFiledNumber("123");
        card.setTypeAffiliation(Constant.BONDING_TYPE_DEPENDENT);
        AffiliationDependent affiliationDependent = new AffiliationDependent();
        affiliationDependent.setIdAffiliateEmployer(1L);

        when(dependentRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliationDependent));

        when(iCardRepository.findOne((Specification<Card>) any()))
                .thenReturn(Optional.of(card));
        when(affiliationDetailRepository.findByFiledNumber("123"))
                .thenReturn(Optional.empty());

        String fakeResponse = "{\"pdf\":\"fake-pdf-content\"}";

        ObjectNode jsonNode = mock(ObjectNode.class);

        when(objectMapper.readTree(fakeResponse)).thenReturn(jsonNode);
        when(jsonNode.path("pdf")).thenReturn(new ObjectMapper().readTree("\"fake-pdf-content\""));
        when(affiliateRepository.findByIdAffiliate(1L))
                .thenReturn(Optional.empty());

        AffiliationNotFoundError ex = assertThrows(
                AffiliationNotFoundError.class,
                () -> service.consultCard("123")
        );

        assertNotNull(ex);

    }

    @Test
    void consultCard_exception_affiliation_not_found6() throws JsonProcessingException {

        Card card =  new Card();
        card.setFiledNumber("123");
        card.setTypeAffiliation(Constant.BONDING_TYPE_DEPENDENT);
        AffiliationDependent affiliationDependent = new AffiliationDependent();
        affiliationDependent.setIdAffiliateEmployer(1L);
        Affiliate affiliate1 = new Affiliate();

        when(dependentRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliationDependent));

        when(iCardRepository.findOne((Specification<Card>) any()))
                .thenReturn(Optional.of(card));
        when(affiliationDetailRepository.findByFiledNumber("123"))
                .thenReturn(Optional.empty());

        String fakeResponse = "{\"pdf\":\"fake-pdf-content\"}";

        ObjectNode jsonNode = mock(ObjectNode.class);

        when(objectMapper.readTree(fakeResponse)).thenReturn(jsonNode);
        when(jsonNode.path("pdf")).thenReturn(new ObjectMapper().readTree("\"fake-pdf-content\""));
        when(affiliateRepository.findByIdAffiliate(1L))
                .thenReturn(Optional.of(affiliate1));

        AffiliationNotFoundError ex = assertThrows(
                AffiliationNotFoundError.class,
                () -> service.consultCard("123")
        );

        assertNotNull(ex);

    }

    @Test
    void consultCard_exception() throws JsonProcessingException {

        Card card =  new Card();
        card.setFiledNumber("123");

        Affiliation affiliation = new Affiliation();
        affiliation.setIsVip(true);

        List<ArlInformation> list =  listArl();

        when(iCardRepository.findOne((Specification<Card>) any()))
                .thenReturn(Optional.of(card));
        when(affiliationDetailRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliation));
        when(genericWebClient.generateAffiliateCard(any()))
                .thenReturn("");
        when(arlInformationDao.findAllArlInformation())
                .thenReturn(list);

        String fakeResponse = "";

        ObjectNode jsonNode = mock(ObjectNode.class);

        when(objectMapper.readTree(fakeResponse)).thenThrow(new JsonProcessingException("Invalid JSON") {});
        when(jsonNode.path("pdf")).thenReturn(new ObjectMapper().readTree("\"fake-pdf-content\""));

        RuntimeException ex =  assertThrows(
                RuntimeException.class,
                () -> service.consultCard("123")
        );

        assertNotNull(ex);

    }

    @Test
    void consultCard() throws JsonProcessingException {

        Card card =  new Card();
        card.setFiledNumber("123");

        Affiliation affiliation = new Affiliation();
        affiliation.setIsVip(true);

        List<ArlInformation> list =  listArl();

        when(iCardRepository.findOne((Specification<Card>) any()))
                .thenReturn(Optional.of(card));
        when(affiliationDetailRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliation));
        when(genericWebClient.generateAffiliateCard(any()))
                .thenReturn("");
        when(arlInformationDao.findAllArlInformation())
                .thenReturn(list);

        String fakeResponse = "";

        ObjectNode jsonNode = mock(ObjectNode.class);

        when(objectMapper.readTree(fakeResponse)).thenReturn(jsonNode);
        when(jsonNode.path("pdf")).thenReturn(new ObjectMapper().readTree("\"fake-pdf-content\""));


        Map<String, String> response =  service.consultCard("123");

        assertNotNull(response);

    }

    @Test
    void consultCard_2() throws JsonProcessingException {

        Card card =  new Card();
        card.setFiledNumber("123");
        card.setNitCompany("123");
        card.setDocumentTypeEmployer("CC");

        Affiliation affiliation = new Affiliation();
        affiliation.setIsVip(true);


        List<ArlInformation> list =  listArl();
        List<AffiliateMercantile> listMercantile = new ArrayList<>();

        when(iCardRepository.findOne((Specification<Card>) any()))
                .thenReturn(Optional.of(card));
        when(affiliationDetailRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliation));
        when(genericWebClient.generateAffiliateCard(any()))
                .thenReturn("");
        when(arlInformationDao.findAllArlInformation())
                .thenReturn(list);
        when(affiliateMercantileRepository.findAll((Specification<AffiliateMercantile>) any()))
                .thenReturn(listMercantile);

        String fakeResponse = "";

        ObjectNode jsonNode = mock(ObjectNode.class);

        when(objectMapper.readTree(fakeResponse)).thenReturn(jsonNode);
        when(jsonNode.path("pdf")).thenReturn(new ObjectMapper().readTree("\"fake-pdf-content\""));


        Map<String, String> response =  service.consultCard("123");

        assertNotNull(response);

    }

    @Test
    void consultCard_3() throws JsonProcessingException {

        Card card =  new Card();
        card.setFiledNumber("123");
        card.setNitCompany("123");
        card.setDocumentTypeEmployer("CC");

        Affiliation affiliation = new Affiliation();
        affiliation.setIsVip(true);


        List<ArlInformation> list =  listArl();
        List<AffiliateMercantile> listMercantile = listMercantile();

        when(iCardRepository.findOne((Specification<Card>) any()))
                .thenReturn(Optional.of(card));
        when(affiliationDetailRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliation));
        when(genericWebClient.generateAffiliateCard(any()))
                .thenReturn("");
        when(arlInformationDao.findAllArlInformation())
                .thenReturn(list);
        when(affiliateMercantileRepository.findAll((Specification<AffiliateMercantile>) any()))
                .thenReturn(listMercantile);

        String fakeResponse = "";

        ObjectNode jsonNode = mock(ObjectNode.class);

        when(objectMapper.readTree(fakeResponse)).thenReturn(jsonNode);
        when(jsonNode.path("pdf")).thenReturn(new ObjectMapper().readTree("\"fake-pdf-content\""));


        Map<String, String> response =  service.consultCard("123");

        assertNotNull(response);

    }

    @Test
    void consultCard_4() throws JsonProcessingException {

        Card card =  new Card();
        card.setNitCompany("123");
        card.setDocumentTypeEmployer("CC");
        card.setTypeAffiliation(Constant.BONDING_TYPE_DEPENDENT);

        Affiliation affiliation = new Affiliation();
        affiliation.setIsVip(true);
        AffiliationDependent affiliationDependent = new AffiliationDependent();
        affiliationDependent.setIdAffiliateEmployer(1L);
        Affiliate affiliate1 = new Affiliate();
        affiliate1.setNitCompany("123");
        affiliate1.setDocumenTypeCompany("CC");

        List<ArlInformation> list =  listArl();
        List<AffiliateMercantile> listMercantile = listMercantile();

        when(iCardRepository.findOne((Specification<Card>) any()))
                .thenReturn(Optional.of(card));
        when(affiliationDetailRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliation));
        when(genericWebClient.generateAffiliateCard(any()))
                .thenReturn("");
        when(arlInformationDao.findAllArlInformation())
                .thenReturn(list);
        when(affiliateMercantileRepository.findAll((Specification<AffiliateMercantile>) any()))
                .thenReturn(listMercantile);
        when(dependentRepository.findByFiledNumber("123"))
                .thenReturn(Optional.of(affiliationDependent));
        when(affiliateRepository.findByIdAffiliate(1L))
                .thenReturn(Optional.of(affiliate1));

        String fakeResponse = "";

        ObjectNode jsonNode = mock(ObjectNode.class);

        when(objectMapper.readTree(fakeResponse)).thenReturn(jsonNode);
        when(jsonNode.path("pdf")).thenReturn(new ObjectMapper().readTree("\"fake-pdf-content\""));


        Map<String, String> response =  service.consultCard("123");

        assertNotNull(response);

    }


    List<ArlInformation> listArl(){

        ArlInformation arlInformation =  new ArlInformation();
        arlInformation.setOtherPhoneNumbers("123456789");
        List<ArlInformation> list =  new ArrayList<>();
        list.add(arlInformation);
        return list;
    }

    List<AffiliateMercantile> listMercantile(){

        AffiliateMercantile affiliateMercantile = new AffiliateMercantile();
        affiliateMercantile.setIsVip(true);
        List<AffiliateMercantile> list = new ArrayList<>();
        list.add(affiliateMercantile);
        return list;
    }
}
