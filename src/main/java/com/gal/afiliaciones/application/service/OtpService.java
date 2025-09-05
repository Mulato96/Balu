package com.gal.afiliaciones.application.service;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.infrastructure.dto.otp.*;
import jakarta.mail.MessagingException;

import java.io.IOException;

public interface OtpService {
    OTPDataResponseDTO generarOtp(OTPRequestDTO requestDTO) throws IllegalAccessException, MessagingException, IOException, AffiliationsExceptionBase;

    OTPDataDTO validarOtp(OTPRequestDTO request);

    OTPDataResponseDTO generateOtpDependent(OtpDependentDataDTO otpDependentDataDTO) throws IllegalAccessException, MessagingException, IOException, AffiliationsExceptionBase;

    OTPDataDTO validateOtpDependent(OTPRequestDependentDTO request);
}
