package com.gal.afiliaciones.application.service;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.infrastructure.dto.otp.*;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.util.Map;

public interface OtpService {
    OTPDataResponseDTO generarOtp(OTPRequestDTO requestDTO) throws IllegalAccessException, MessagingException, IOException, AffiliationsExceptionBase;

    OTPDataDTO validarOtp(OTPRequestDTO request);

    OTPDataResponseDTO generateOtpDependent(OtpDependentDataDTO otpDependentDataDTO) throws IllegalAccessException, MessagingException, IOException, AffiliationsExceptionBase;

    OTPDataDTO validateOtpDependent(OTPRequestDependentDTO request);

    /**
     * TEST ONLY - Retrieves OTP code for testing purposes.
     * Should only be called after host validation in controller.
     * 
     * @param documentType Document type (e.g., "CC")
     * @param documentNumber Document number
     * @param typeUser User type (can be null)
     * @return Map containing OTP code and user information
     */
    Map<String, Object> getOtpForTesting(String documentType, String documentNumber, String typeUser);
}
