package com.gal.afiliaciones.application.service.usernotification.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dto.usernotification.UserNotificationDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

class UserNotificationServiceImplTest {

    private AffiliateRepository affiliateRepository;
    private IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;
    private AffiliateMercantileRepository mercantileRepository;
    private IUserPreRegisterRepository userPreRegisterRepository;
    private UserNotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        affiliateRepository = mock(AffiliateRepository.class);
        affiliationRepository = mock(IAffiliationEmployerDomesticServiceIndependentRepository.class);
        mercantileRepository = mock(AffiliateMercantileRepository.class);
        userPreRegisterRepository = mock(IUserPreRegisterRepository.class);
        service = new UserNotificationServiceImpl(
                affiliateRepository,
                affiliationRepository,
                mercantileRepository,
                userPreRegisterRepository
        );
    }

    @Test
    void findAllAffiliatedUser_returnsEmptyList_whenNoAffiliates() {
        when(affiliateRepository.findAllByAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE))
                .thenReturn(Collections.emptyList());

        List<UserNotificationDTO> result = service.findAllAffiliatedUser();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllAffiliatedUser_returnsUserNotificationDTO_forDomesticOrIndependent() {
        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("FN123");
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC);
        affiliate.setAffiliationSubType("SUBTYPE");

        when(affiliateRepository.findAllByAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE))
                .thenReturn(List.of(affiliate));

        Affiliation affiliation = new Affiliation();
        affiliation.setIdentificationDocumentType("CC");
        affiliation.setIdentificationDocumentNumber("123456");
        affiliation.setFirstName("John");
        affiliation.setSecondName("A.");
        affiliation.setSurname("Doe");
        affiliation.setSecondSurname("Smith");
        affiliation.setAddress("Address 1");
        affiliation.setPhone1("555-1234");
        affiliation.setEmail("john@example.com");

        when(affiliationRepository.findByFiledNumber("FN123"))
                .thenReturn(Optional.of(affiliation));

        UserMain userMain = new UserMain();
        userMain.setId(10L);

        when(userPreRegisterRepository.findOne(any(Specification.class)))
                .thenReturn(Optional.of(userMain));

        List<UserNotificationDTO> result = service.findAllAffiliatedUser();

        assertEquals(1, result.size());
        UserNotificationDTO dto = result.get(0);
        assertEquals("CC", dto.getIdentificationType());
        assertEquals("123456", dto.getIdentificationNumber());
        assertEquals("John A. Doe Smith", dto.getCompleteName());
        assertEquals(10L, dto.getIdUser());
        assertEquals("Address 1", dto.getAddress());
        assertEquals("555-1234", dto.getPhone());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals(Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC, dto.getAffiliationType());
        assertEquals("SUBTYPE", dto.getAffiliationSubtype());
    }

    @Test
    void findAllAffiliatedUser_returnsUserNotificationDTO_forMercantile() {
        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber("FN456");
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        affiliate.setAffiliationSubType("SUBTYPE2");

        when(affiliateRepository.findAllByAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE))
                .thenReturn(List.of(affiliate));

        AffiliateMercantile mercantile = new AffiliateMercantile();
        mercantile.setTypeDocumentPersonResponsible("NIT");
        mercantile.setNumberDocumentPersonResponsible("789012");
        mercantile.setBusinessName("ACME Corp");
        mercantile.setIdUserPreRegister(20L);
        mercantile.setAddress("Business Address");
        mercantile.setPhoneOne("555-5678");
        mercantile.setEmail("acme@example.com");

        when(mercantileRepository.findByFiledNumber("FN456"))
                .thenReturn(Optional.of(mercantile));

        List<UserNotificationDTO> result = service.findAllAffiliatedUser();

        assertEquals(1, result.size());
        UserNotificationDTO dto = result.get(0);
        assertEquals("NIT", dto.getIdentificationType());
        assertEquals("789012", dto.getIdentificationNumber());
        assertEquals("ACME Corp", dto.getCompleteName());
        assertEquals(20L, dto.getIdUser());
        assertEquals("Business Address", dto.getAddress());
        assertEquals("555-5678", dto.getPhone());
        assertEquals("acme@example.com", dto.getEmail());
        assertEquals(Constant.TYPE_AFFILLATE_EMPLOYER, dto.getAffiliationType());
        assertEquals("SUBTYPE2", dto.getAffiliationSubtype());
    }

    @Test
    void findAllAffiliatedUser_skipsAffiliateWithNullFiledNumber() {
        Affiliate affiliate = new Affiliate();
        affiliate.setFiledNumber(null);

        when(affiliateRepository.findAllByAffiliationStatus(Constant.AFFILIATION_STATUS_ACTIVE))
                .thenReturn(List.of(affiliate));

        List<UserNotificationDTO> result = service.findAllAffiliatedUser();

        assertTrue(result.isEmpty());
    }

    @Test
    void convertDomesticOrIndependent_returnsEmptyDTO_whenAffiliationNotFound() {
        when(affiliationRepository.findByFiledNumber("FN999"))
                .thenReturn(Optional.empty());

        UserNotificationDTO dto = invokeConvertDomesticOrIndependent("FN999");

        assertNull(dto.getIdentificationType());
        assertNull(dto.getIdentificationNumber());
    }

    @Test
    void convertMercantile_returnsEmptyDTO_whenMercantileNotFound() {
        when(mercantileRepository.findByFiledNumber("FN888"))
                .thenReturn(Optional.empty());

        UserNotificationDTO dto = invokeConvertMercantile("FN888");

        assertNull(dto.getIdentificationType());
        assertNull(dto.getIdentificationNumber());
    }

    @Test
    void concatCompleteName_worksWithAllFields() {
        String result = invokeConcatCompleteName("Jane", "B.", "Roe", "Johnson");
        assertEquals("Jane B. Roe Johnson", result);
    }

    @Test
    void concatCompleteName_worksWithBlankSecondNameAndSecondSurname() {
        String result = invokeConcatCompleteName("Jane", "", "Roe", "");
        assertEquals("Jane Roe", result);
    }

    // Helper methods to access private methods via reflection
    private UserNotificationDTO invokeConvertDomesticOrIndependent(String filedNumber) {
        try {
            var method = UserNotificationServiceImpl.class.getDeclaredMethod("convertDomesticOrIndependent", String.class);
            method.setAccessible(true);
            return (UserNotificationDTO) method.invoke(service, filedNumber);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private UserNotificationDTO invokeConvertMercantile(String filedNumber) {
        try {
            var method = UserNotificationServiceImpl.class.getDeclaredMethod("convertMercantile", String.class);
            method.setAccessible(true);
            return (UserNotificationDTO) method.invoke(service, filedNumber);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String invokeConcatCompleteName(String firstname, String secondname, String surname, String secondsurname) {
        try {
            var method = UserNotificationServiceImpl.class.getDeclaredMethod("concatCompleteName", String.class, String.class, String.class, String.class);
            method.setAccessible(true);
            return (String) method.invoke(service, firstname, secondname, surname, secondsurname);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}