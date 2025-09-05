package com.gal.afiliaciones.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.domain.model.ContributionCorrection;
import com.gal.afiliaciones.domain.model.RequestCollectionReturn;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.RequestCollectionRequestRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.RequestCorrectionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.utils.Constant;

class UserStatusUpdateServiceTest {

    private IUserPreRegisterRepository iUserPreRegisterRepository;
    private RequestCorrectionRepository requestCorrectionRepository;
    private RequestCollectionRequestRepository requestCollectionRequestRepository;
    private AffiliateRepository affiliateRepository;
    private IAffiliationEmployerDomesticServiceIndependentRepository affiliationDomesticRepository;
    private AffiliateMercantileRepository affiliateMercantileRepository;

    private UserStatusUpdateService service;

    @BeforeEach
    void setUp() {
        iUserPreRegisterRepository = mock(IUserPreRegisterRepository.class);
        requestCorrectionRepository = mock(RequestCorrectionRepository.class);
        requestCollectionRequestRepository = mock(RequestCollectionRequestRepository.class);
        affiliateRepository = mock(AffiliateRepository.class);
        affiliationDomesticRepository = mock(IAffiliationEmployerDomesticServiceIndependentRepository.class);
        affiliateMercantileRepository = mock(AffiliateMercantileRepository.class);

        service = new UserStatusUpdateService(iUserPreRegisterRepository, requestCorrectionRepository,
                requestCollectionRequestRepository, affiliateRepository, affiliationDomesticRepository,
                affiliateMercantileRepository);
    }

    @Test
    void updateUsersInactiveAfter72Hours_updatesUsersCorrectly() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(72);

        UserMain user = new UserMain();
        user.setId(1L);
        user.setIdentification("ID123");
        user.setEmail("email@example.com");
        user.setStatusActive(true);
        user.setStatus(1L);
        user.setInactiveByPendingAffiliation(false);

        when(iUserPreRegisterRepository.findAll(any(Specification.class))).thenReturn(List.of(user));
        when(iUserPreRegisterRepository.save(any(UserMain.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateUsersInactiveAfter72Hours(threshold);

        ArgumentCaptor<UserMain> captor = ArgumentCaptor.forClass(UserMain.class);
        verify(iUserPreRegisterRepository, times(1)).save(captor.capture());
        UserMain savedUser = captor.getValue();

        assertFalse(savedUser.getStatusActive());
        assertEquals(2L, savedUser.getStatus());
        assertNotNull(savedUser.getStatusInactiveSince());
        assertTrue(savedUser.getInactiveByPendingAffiliation());
    }

    @Test
    void updateUsersInactiveByPendingAffiliation_updatesUsersCorrectly() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);

        UserMain user = new UserMain();
        user.setId(1L);
        user.setIdentification("ID123");
        user.setEmail("email@example.com");
        user.setStatusActive(true);
        user.setInactiveByPendingAffiliation(false);

        when(iUserPreRegisterRepository.findAll(any(Specification.class))).thenReturn(List.of(user));
        when(iUserPreRegisterRepository.save(any(UserMain.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateUsersInactiveByPendingAffiliation(threshold);

        ArgumentCaptor<UserMain> captor = ArgumentCaptor.forClass(UserMain.class);
        verify(iUserPreRegisterRepository, times(1)).save(captor.capture());
        UserMain savedUser = captor.getValue();

        assertFalse(savedUser.getStatusActive());
        assertNotNull(savedUser.getStatusInactiveSince());
        assertTrue(savedUser.getInactiveByPendingAffiliation());
    }

    @Test
    void hasPendingAffiliations_returnsUsersWithIncompleteAffiliations() {
        UserMain user1 = new UserMain();
        user1.setId(1L);
        UserMain user2 = new UserMain();
        user2.setId(2L);

        Affiliate affiliateCompleted = new Affiliate();
        affiliateCompleted.setAffiliationType("other");
        affiliateCompleted.setFiledNumber("filedCompleted");

        Affiliate affiliateNotCompleted = new Affiliate();
        affiliateNotCompleted.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        affiliateNotCompleted.setFiledNumber("filedNotCompleted");

        when(affiliateRepository.findByUserId(1L)).thenReturn(List.of(affiliateCompleted));
        when(affiliateRepository.findByUserId(2L)).thenReturn(List.of(affiliateNotCompleted));

        when(affiliateMercantileRepository.findByFiledNumber("filedNotCompleted")).thenReturn(Optional.empty());

        List<UserMain> result = service.hasPendingAffiliations(List.of(user1, user2));

        assertTrue(result.contains(user2));
        assertFalse(result.contains(user1));
    }

    @Test
    void isAffiliationCompleted_returnsTrueForDefault() throws Exception {
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType("unknownType");

        // Use reflection to test private method
        var method = UserStatusUpdateService.class.getDeclaredMethod("isAffiliationCompleted", Affiliate.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(service, affiliate);
        assertTrue(result);
    }

    
    @Test
    void updateUsersInactiveAfter72Hours_noUsersFound_doesNotSave() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(72);

        when(iUserPreRegisterRepository.findAll(any(Specification.class))).thenReturn(List.of());

        service.updateUsersInactiveAfter72Hours(threshold);

        verify(iUserPreRegisterRepository, never()).save(any());
    }

    @Test
    void deleteUsersInactiveAfter60Days_deletesOnlyUsersWithoutCollectionProcessAndWithPendingAffiliations() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(60);

        UserMain user1 = new UserMain();
        user1.setId(1L);
        user1.setIdentification("ID1");
        user1.setEmail("email1@example.com");

        UserMain user2 = new UserMain();
        user2.setId(2L);
        user2.setIdentification("ID2");
        user2.setEmail("email2@example.com");

        when(iUserPreRegisterRepository.findAll(any(Specification.class))).thenReturn(List.of(user1, user2));

        // user1 has no collection process, user2 has collection process
        when(requestCorrectionRepository.findByUser_Id(1L)).thenReturn(Optional.empty());
        when(requestCollectionRequestRepository.findByUser_Id(1L)).thenReturn(Optional.empty());
        when(requestCorrectionRepository.findByUser_Id(2L)).thenReturn(Optional.of(mock(ContributionCorrection.class)));
        when(requestCollectionRequestRepository.findByUser_Id(2L)).thenReturn(Optional.empty());

        // user1 has affiliates, user2 no affiliates
        Affiliate affiliate = new Affiliate();
        affiliate.setAffiliationType(Constant.TYPE_AFFILLATE_EMPLOYER);
        affiliate.setFiledNumber("filed1");

        when(affiliateRepository.findByUserId(1L)).thenReturn(List.of(affiliate));
        when(affiliateRepository.findByUserId(2L)).thenReturn(List.of());

        // affiliate is not completed (simulate)
        when(affiliateMercantileRepository.findByFiledNumber("filed1")).thenReturn(Optional.empty());

        service.deleteUsersInactiveAfter60Days(threshold);

        // user1 should be deleted, user2 not
        ArgumentCaptor<List<UserMain>> captor = ArgumentCaptor.forClass(List.class);
        verify(iUserPreRegisterRepository).deleteAll(captor.capture());
        List<UserMain> deletedUsers = captor.getValue();

        assertTrue(deletedUsers.contains(user1));
        assertFalse(deletedUsers.contains(user2));
    }

    @Test
    void hasCollectionProcess_returnsFalseWhenNoProcesses() {
        Long userId = 1L;

        when(requestCorrectionRepository.findByUser_Id(userId)).thenReturn(Optional.empty());
        when(requestCollectionRequestRepository.findByUser_Id(userId)).thenReturn(Optional.empty());

        assertFalse(service.hasCollectionProcess(userId));
    }

    @Test
    void hasCollectionProcess_returnsTrueWhenRequestCorrectionExists() {
        Long userId = 1L;

        when(requestCorrectionRepository.findByUser_Id(userId)).thenReturn(Optional.of(mock(ContributionCorrection.class)));
        when(requestCollectionRequestRepository.findByUser_Id(userId)).thenReturn(Optional.empty());

        assertTrue(service.hasCollectionProcess(userId));
    }

    @Test
    void hasCollectionProcess_returnsTrueWhenRequestCollectionRequestExists() {
        Long userId = 1L;

        when(requestCorrectionRepository.findByUser_Id(userId)).thenReturn(Optional.empty());
        when(requestCollectionRequestRepository.findByUser_Id(userId)).thenReturn(Optional.of(mock(RequestCollectionReturn.class)));

        assertTrue(service.hasCollectionProcess(userId));
    }
}
