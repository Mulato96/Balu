package com.gal.afiliaciones.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Example;

import com.gal.afiliaciones.application.service.OtpService;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorNumberAttemptsExceeded;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorValidateCode;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.infrastructure.dao.repository.ICertificateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dto.certificate.ValidCodeCertificateDTO;
import com.gal.afiliaciones.infrastructure.dto.otp.OTPRequestDependentDTO;

class ConsultCertificateByUserServiceImplTest {

    @Mock
    private OtpService otpService;

    @Mock
    private AffiliateRepository affiliateRepository;

    @Mock
    private ICertificateRepository iCertificateRepository;

    @Mock
    private AffiliationDependentRepository dependentRepository;

    @Mock
    private IUserPreRegisterRepository iUserPreRegisterRepository;

    @InjectMocks
    private ConsultCertificateByUserServiceImpl consultCertificateByUserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void consultUser_shouldThrowUserNotFoundWhenNoAffiliate() {
        when(affiliateRepository.findAll(any(Example.class))).thenReturn(Collections.emptyList());

        assertThrows(UserNotFoundInDataBase.class, () -> {
            consultCertificateByUserService.consultUser("CC", "12345");
        });
    }

    @Test
    void consultUser_shouldThrowErrorNumberAttemptsExceeded() {
        when(affiliateRepository.findAll(any(Example.class))).thenReturn(Collections.emptyList());
        
        assertThrows(UserNotFoundInDataBase.class, () -> {
            consultCertificateByUserService.consultUser("CC", "12345");
        });
        assertThrows(UserNotFoundInDataBase.class, () -> {
            consultCertificateByUserService.consultUser("CC", "12345");
        });
        assertThrows(ErrorNumberAttemptsExceeded.class, () -> {
            consultCertificateByUserService.consultUser("CC", "12345");
        });
    }

    @Test
    void validateCodeOTPCertificate_shouldThrowErrorValidateCodeOnInvalidCode() {
        ValidCodeCertificateDTO consultUserDTO = new ValidCodeCertificateDTO();
        consultUserDTO.setIdentificationType("CC");
        consultUserDTO.setCode("12345");
        consultUserDTO.setIdentification("123456");
        doThrow(new RuntimeException("Invalid code")).when(otpService).validateOtpDependent(any(OTPRequestDependentDTO.class));

        assertThrows(ErrorValidateCode.class, () -> {
            consultCertificateByUserService.validateCodeOTPCertificate(consultUserDTO);
        });
    }

}
