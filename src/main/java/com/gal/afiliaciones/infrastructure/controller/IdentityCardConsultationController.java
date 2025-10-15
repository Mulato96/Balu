package com.gal.afiliaciones.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gal.afiliaciones.infrastructure.dto.registraduria.IdentityCardConsultationRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.registraduria.RegistraduriaResponseDTO;
import com.gal.afiliaciones.infrastructure.service.IdentityCardConsultationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/registraduria")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Identity Card Consultation", description = "Endpoints for consulting identity card information from Registraduria")
public class IdentityCardConsultationController {

    private final IdentityCardConsultationService identityCardConsultationService;

    @PostMapping("/consult")
    @Operation(summary = "Consult identity card by document number",
               description = "Consult identity card information from Registraduria SOAP service")
    public Mono<ResponseEntity<RegistraduriaResponseDTO>> consultIdentityCard(
            @Valid @RequestBody IdentityCardConsultationRequestDTO request) {

        log.info("Received identity card consultation request for document: {}", request.getDocumentNumber());

        return identityCardConsultationService.consultIdentityCard(request.getDocumentNumber())
                .map(response -> {
                    log.info("Identity card consultation completed successfully for document: {}", request.getDocumentNumber());
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(error -> {
                    log.error("Error in identity card consultation for document {}: {}", request.getDocumentNumber(), error.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    @GetMapping("/consult/{documentNumber}")
    @Operation(summary = "Consult identity card by document number (GET)",
               description = "Consult identity card information from Registraduria SOAP service using GET method")
    public Mono<ResponseEntity<RegistraduriaResponseDTO>> consultIdentityCardByGet(
            @PathVariable String documentNumber) {

        log.info("Received identity card consultation request (GET) for document: {}", documentNumber);

        return identityCardConsultationService.consultIdentityCard(documentNumber)
                .map(response -> {
                    log.info("Identity card consultation completed successfully for document: {}", documentNumber);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(error -> {
                    log.error("Error in identity card consultation for document {}: {}", documentNumber, error.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
} 