package com.gal.afiliaciones.infrastructure.controller.otp;

import com.gal.afiliaciones.application.service.OtpService;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.infrastructure.dto.otp.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/otp")
@Tag(name = "Codigo otp", description = "Codigo otp Management API")
@RequiredArgsConstructor
public class OtpController {
    
    private final OtpService otpService;

    @Operation(summary = "Generación de código de validación", description = "Generación de código de validación, si existe un código vigente se invalida y se crea uno nuevo")
    @PostMapping("/generar-otp")
    public ResponseEntity<OTPDataResponseDTO> generarOtp(@RequestBody OTPRequestDTO requestDTO) throws IllegalAccessException, IOException, MessagingException, AffiliationsExceptionBase {
        return ResponseEntity.ok(otpService.generarOtp(requestDTO));
    }

    @Operation(summary = "Verificación de código de validación", description = "Verificación de código de validación, por defecto cuenta con un tiempo de expiración de 5 minutos")
    @PostMapping("/validar-otp")
    public ResponseEntity<OTPDataDTO> validarOtp(@RequestBody OTPRequestDTO request) {
        return ResponseEntity.ok(otpService.validarOtp(request));
    }

    @PostMapping("/generar-otp/dependent")
    public ResponseEntity<OTPDataResponseDTO> generateOtpDependent(@RequestBody OtpDependentDataDTO otpDependentDataDTO) throws IllegalAccessException, IOException, MessagingException, AffiliationsExceptionBase {
        return ResponseEntity.ok(otpService.generateOtpDependent(otpDependentDataDTO));
    }

    @Operation(summary = "Verificación de código de validación", description = "Verificación de código de validación, por defecto cuenta con un tiempo de expiración de 5 minutos")
    @PostMapping("/validar-otp/dependent")
    public ResponseEntity<OTPDataDTO> validateOtpDependent(@RequestBody OTPRequestDependentDTO request) {
        return ResponseEntity.ok(otpService.validateOtpDependent(request));
    }
}

