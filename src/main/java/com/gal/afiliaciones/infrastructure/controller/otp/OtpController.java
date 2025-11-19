package com.gal.afiliaciones.infrastructure.controller.otp;

import com.gal.afiliaciones.application.service.OtpService;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.infrastructure.dto.otp.*;
import com.gal.afiliaciones.infrastructure.security.TestEnvironmentValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/otp")
@Tag(name = "Codigo otp", description = "Codigo otp Management API")
@RequiredArgsConstructor
public class OtpController {
    
    private final OtpService otpService;
    private final TestEnvironmentValidator testEnvironmentValidator;

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

    /**
     * TEST ONLY ENDPOINT - Retrieves OTP code for testing purposes.
     * This endpoint is ONLY accessible from:
     * - localhost / 127.0.0.1
     * - gal-back-dev.linktic.com
     * - gal-back-qa.linktic.com
     * 
     * Host validation is done via HTTP Host header - NOT environment variables.
     * Any request from other hosts will be rejected with 403 Forbidden.
     * 
     * @param documentType Document type (e.g., "CC")
     * @param documentNumber Document number
     * @param typeUser User type (e.g., "EXT", or "null" for no type)
     * @param request HttpServletRequest to validate Host header
     * @return OTP code if authorized, 403 otherwise
     */
    @Operation(summary = "[TEST ONLY] Get OTP Code", 
               description = "Retrieves current OTP code for testing. Only works on localhost, dev, and QA environments. Example: /test/get-otp/CC/5892994214/EXT")
    @GetMapping("/test/get-otp/{documentType}/{documentNumber}/{typeUser}")
    public ResponseEntity<Map<String, Object>> getOtpForTesting(
            @PathVariable("documentType") String documentType,
            @PathVariable("documentNumber") String documentNumber,
            @PathVariable("typeUser") String typeUser,
            HttpServletRequest request) {
        
        // Validate environment - SECURITY CRITICAL
        if (!testEnvironmentValidator.isAllowedTestEnvironment(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                        "error", "Forbidden",
                        "message", "This endpoint is only available on localhost, dev, and QA environments",
                        "host", testEnvironmentValidator.extractHost(request)
                    ));
        }

        // Delegate to service
        return ResponseEntity.ok(otpService.getOtpForTesting(documentType, documentNumber, typeUser));
    }
}

