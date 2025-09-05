package com.gal.afiliaciones.application.service.employeeupdateinfo.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gal.afiliaciones.application.service.KeycloakService;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dto.employeeupdateinfo.UpdateInfoEmployeeIndependentRequest;
import com.gal.afiliaciones.infrastructure.dto.otp.email.EmailDataDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.utils.EmailService;

import jakarta.mail.MessagingException;

@ExtendWith(MockitoExtension.class)
class UpdateInfoEmployeeInfoServiceImplTest {

    @Mock
    private EmailService emailService;
    @Mock
    private AffiliateRepository affiliateRepository;
    @Mock
    private IUserPreRegisterRepository userMainRepository;
    @Mock
    private AffiliationDetailRepository affiliationDetailRepository;
    @Mock
    private KeycloakService keycloakService;

    @InjectMocks
    private UpdateInfoEmployeeInfoServiceImpl service;

    private UpdateInfoEmployeeIndependentRequest request;
    private UserMain user;
    private Affiliate affiliate;
    private Affiliation affiliationDetail;

    @BeforeEach
    void setUp() {
        request = new UpdateInfoEmployeeIndependentRequest();
        request.setTypeIdentification("CC");
        request.setIdentification("123456789");
        request.setEmail("new.email@example.com");
        request.setPrimaryPhone("3001234567");
        request.setFullAddress("New Address 123");
        request.setDepartment(1L);
        request.setCity(1L);
        request.setMainStreet(1L);
        request.setEps(1L);
        request.setAfp(1L);

        user = new UserMain();
        user.setIdentificationType("CC");
        user.setIdentification("123456789");
        user.setEmail("old.email@example.com");

        affiliate = new Affiliate();
        affiliate.setFiledNumber("FN123");

        affiliationDetail = new Affiliation();
        affiliationDetail.setFiledNumber("FN123");
        affiliationDetail.setEmail("old.email@example.com");
        affiliationDetail.setFirstName("John");
        affiliationDetail.setSecondName("Doe");
    }

    @Test
    @DisplayName("Should update employee info successfully when email does not change")
    void updateInfoEmployeeIndependet_Success_EmailNotChanged() throws MessagingException, IOException {
        // Arrange
        request.setEmail("old.email@example.com");
        user.setEmail("old.email@example.com");

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                request.getTypeIdentification(), request.getIdentification(), Constant.AFFILIATION_STATUS_ACTIVE,
                Constant.TYPE_AFFILLATE_INDEPENDENT))
                .thenReturn(List.of(affiliate));
        when(userMainRepository.findByIdentificationTypeAndIdentification(request.getTypeIdentification(),
                request.getIdentification()))
                .thenReturn(Optional.of(user));
        when(affiliationDetailRepository.findByFiledNumber(affiliate.getFiledNumber()))
                .thenReturn(Optional.of(affiliationDetail));
        doNothing().when(emailService).sendSimpleMessage(any(EmailDataDTO.class), anyString());

        // Act
        String result = service.updateInfoEmployeeIndependet(request);

        // Assert
        assertEquals(Constant.UPDATE_INFO_EMPLOYEE_INDEPENDENT, result);
        verify(userMainRepository).save(any(UserMain.class));
        verify(affiliationDetailRepository).save(any(Affiliation.class));
        verify(keycloakService, never()).updateEmailUser(any(), any());
        verify(emailService).sendSimpleMessage(any(EmailDataDTO.class), anyString());
    }

    @Test
    @DisplayName("Should throw AffiliateNotFound when user does not exist")
    void updateInfoEmployeeIndependet_ThrowsAffiliateNotFound_WhenUserNotFound() {
        // Arrange
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                request.getTypeIdentification(), request.getIdentification(), Constant.AFFILIATION_STATUS_ACTIVE,
                Constant.TYPE_AFFILLATE_INDEPENDENT))
                .thenReturn(List.of(affiliate));
        when(userMainRepository.findByIdentificationTypeAndIdentification(request.getTypeIdentification(),
                request.getIdentification()))
                .thenReturn(Optional.empty());

        // Act & Assert
        AffiliateNotFound exception = assertThrows(AffiliateNotFound.class,
                () -> service.updateInfoEmployeeIndependet(request));
    }

    @Test
    @DisplayName("Should throw AffiliateNotFound when no active independent affiliations are found")
    void updateInfoEmployeeIndependet_ThrowsAffiliateNotFound_WhenAffiliationsEmpty() {
        // Arrange
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                request.getTypeIdentification(), request.getIdentification(), Constant.AFFILIATION_STATUS_ACTIVE,
                Constant.TYPE_AFFILLATE_INDEPENDENT))
                .thenReturn(Collections.emptyList());
        when(userMainRepository.findByIdentificationTypeAndIdentification(request.getTypeIdentification(),
                request.getIdentification()))
                .thenReturn(Optional.of(user));

        // Act & Assert
        AffiliateNotFound exception = assertThrows(AffiliateNotFound.class,
                () -> service.updateInfoEmployeeIndependet(request));
    }

    @Test
    @DisplayName("Should throw AffiliateNotFound when affiliation detail is not found")
    void updateInfoEmployeeIndependet_NoAffiliationDetailFound() throws MessagingException, IOException {
        // Arrange
        request.setEmail("old.email@example.com");
        user.setEmail("old.email@example.com");

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                request.getTypeIdentification(), request.getIdentification(), Constant.AFFILIATION_STATUS_ACTIVE,
                Constant.TYPE_AFFILLATE_INDEPENDENT))
                .thenReturn(List.of(affiliate));
        when(userMainRepository.findByIdentificationTypeAndIdentification(request.getTypeIdentification(),
                request.getIdentification()))
                .thenReturn(Optional.of(user));
        when(affiliationDetailRepository.findByFiledNumber(affiliate.getFiledNumber()))
                .thenReturn(Optional.empty());

        // Act
        String result = service.updateInfoEmployeeIndependet(request);

        // Assert
        assertEquals(Constant.UPDATE_INFO_EMPLOYEE_INDEPENDENT, result);
        verify(userMainRepository).save(any(UserMain.class));
        verify(affiliationDetailRepository, never()).save(any(Affiliation.class));
        verify(emailService, never()).sendSimpleMessage(any(EmailDataDTO.class), anyString());
    }

    @Test
    @DisplayName("Should throw AffiliateNotFound when user is found but has no active independent affiliations")
    void updateInfoEmployeeIndependet_ThrowsAffiliateNotFound_WhenUserExistsButNoActiveAffiliations() {
        // Arrange
        when(userMainRepository.findByIdentificationTypeAndIdentification(request.getTypeIdentification(),
                request.getIdentification()))
                .thenReturn(Optional.of(user));
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumberAndAffiliationStatusAndAffiliationType(
                request.getTypeIdentification(), request.getIdentification(), Constant.AFFILIATION_STATUS_ACTIVE,
                Constant.TYPE_AFFILLATE_INDEPENDENT))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(AffiliateNotFound.class,
                () -> service.updateInfoEmployeeIndependet(request));
    }
}
