package com.gal.afiliaciones.application.service.inactiveusers.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.application.service.OtpService;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorValidateCode;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dto.otp.OTPRequestDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;


class InactiveUsersServiceImplTest {

    @Mock
    private IUserPreRegisterRepository userPreRegisterRepository;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private InactiveUsersServiceImpl inactiveUsersService;

    @Captor
    private ArgumentCaptor<UserMain> userCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void validOtpAndActiveAccount_success() {
        OTPRequestDTO request = new OTPRequestDTO();
        request.setCedula("123456");

        UserMain user = new UserMain();
        user.setStatusActive(false);
        user.setStatus(0L);
        user.setInactiveByPendingAffiliation(true);

        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.of(user));
        when(userPreRegisterRepository.save(any(UserMain.class))).thenReturn(user);

        String result = inactiveUsersService.validOtpAndActiveAccount(request);

        assertEquals("Ya puedes continuar tu proceso de afiliación", result);
        assertTrue(user.getStatusActive());
        assertEquals(1L, user.getStatus());
        assertFalse(user.getInactiveByPendingAffiliation());
        verify(otpService).validarOtp(any(OTPRequestDTO.class));
        verify(userPreRegisterRepository).save(user);
    }

    @Test
    void validOtpAndActiveAccount_otpValidationFailsWithExpiredCode() {
        OTPRequestDTO request = new OTPRequestDTO();
        request.setCedula("123456");

        doThrow(new RuntimeException(Constant.VALIDATION_CODE_HAS_EXPIRED))
                .when(otpService).validarOtp(any(OTPRequestDTO.class));

        assertThrows(ErrorValidateCode.class, () ->
                inactiveUsersService.validOtpAndActiveAccount(request)
        );
        verify(otpService).validarOtp(any(OTPRequestDTO.class));
        verify(userPreRegisterRepository, never()).save(any());
    }

    @Test
    void validOtpAndActiveAccount_otpValidationFailsWithOtherError() {
        OTPRequestDTO request = new OTPRequestDTO();
        request.setCedula("123456");

        doThrow(new RuntimeException("Some other error"))
                .when(otpService).validarOtp(any(OTPRequestDTO.class));

        assertThrows(ErrorValidateCode.class, () ->
                inactiveUsersService.validOtpAndActiveAccount(request)
        );
        verify(otpService).validarOtp(any(OTPRequestDTO.class));
        verify(userPreRegisterRepository, never()).save(any());
    }

    @Test
    void validOtpAndActiveAccount_userNotFound() {
        OTPRequestDTO request = new OTPRequestDTO();
        request.setCedula("123456");

        when(userPreRegisterRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // otpService.validarOtp should not throw
        // No need to stub doNothing() for void methods, as it's the default behavior in Mockito

        assertThrows(UserNotFoundInDataBase.class, () ->
                inactiveUsersService.validOtpAndActiveAccount(request)
        );
        verify(otpService).validarOtp(any(OTPRequestDTO.class));
        verify(userPreRegisterRepository, never()).save(any());
    }
}