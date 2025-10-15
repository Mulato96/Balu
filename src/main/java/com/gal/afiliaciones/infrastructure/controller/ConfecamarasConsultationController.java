package com.gal.afiliaciones.infrastructure.controller;

import com.gal.afiliaciones.infrastructure.dto.wsconfecamaras.RecordResponseDTO;
import com.gal.afiliaciones.infrastructure.service.ConfecamarasConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/confecamaras")
@RequiredArgsConstructor
@Validated
@Tag(name = "Confecamaras Consultation", description = "Endpoints for consulting company information from Confecamaras")
public class ConfecamarasConsultationController {

    private final ConfecamarasConsultationService confecamarasConsultationService;

    @PostMapping("/consult")
    @Operation(summary = "Consult company by NIT and verification digit",
               description = "Consult company information from Confecamaras API using NIT and verification digit")
    public Mono<ResponseEntity<RecordResponseDTO>> consultCompany(
            @Parameter(description = "Company NIT", required = true, example = "815004589")
            @RequestParam(required = true) 
            @Pattern(regexp = "^[0-9]{1,15}$", message = "NIT must contain only numbers and be between 1-15 digits")
            String nit,
            
            @Parameter(description = "Verification digit", required = true, example = "0")
            @RequestParam(required = true)
            @Pattern(regexp = "^[0-9]{1}$", message = "Verification digit must be a single digit")
            String dv) {

        log.info("Received Confecamaras consultation request for NIT: {} DV: {}", nit, dv);

        // Validate parameters manually
        if (nit == null || nit.trim().isEmpty()) {
            log.warn("NIT parameter is missing or empty");
            return Mono.just(ResponseEntity.badRequest().build());
        }
        
        if (dv == null || dv.trim().isEmpty()) {
            log.warn("DV parameter is missing or empty");
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return confecamarasConsultationService.consultCompany(nit, dv)
                .map(response -> {
                    if (response.getError() != null) {
                        log.warn("Confecamaras consultation returned error for NIT {}: {} - {}", 
                                nit, response.getError().getCode(), response.getError().getMessage());
                    } else {
                        log.info("Confecamaras consultation completed successfully for NIT: {}", nit);
                        if (response.getRegistros() != null) {
                            log.info("Found {} company records for NIT: {}", response.getRegistros().size(), nit);
                        }
                    }
                    // Always return 200 OK with the response (success or error)
                    return ResponseEntity.ok(response);
                });
    }

    @PostMapping("/consult/{nit}/{dv}")
    @Operation(summary = "Consult company by NIT and verification digit (path variables)",
               description = "Consult company information from Confecamaras API using NIT and verification digit as path variables")
    public Mono<ResponseEntity<RecordResponseDTO>> consultCompanyByPath(
            @Parameter(description = "Company NIT", required = true, example = "815004589")
            @PathVariable
            @Pattern(regexp = "^[0-9]{1,15}$", message = "NIT must contain only numbers and be between 1-15 digits")
            String nit,
            
            @Parameter(description = "Verification digit", required = true, example = "0")
            @PathVariable
            @Pattern(regexp = "^[0-9]{1}$", message = "Verification digit must be a single digit")
            String dv) {

        log.info("Received Confecamaras consultation request (path) for NIT: {} DV: {}", nit, dv);

        // Validate parameters manually
        if (nit == null || nit.trim().isEmpty()) {
            log.warn("NIT parameter is missing or empty");
            return Mono.just(ResponseEntity.badRequest().build());
        }
        
        if (dv == null || dv.trim().isEmpty()) {
            log.warn("DV parameter is missing or empty");
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return confecamarasConsultationService.consultCompany(nit, dv)
                .map(response -> {
                    if (response.getError() != null) {
                        log.warn("Confecamaras consultation returned error for NIT {}: {} - {}", 
                                nit, response.getError().getCode(), response.getError().getMessage());
                    } else {
                        log.info("Confecamaras consultation completed successfully for NIT: {}", nit);
                        if (response.getRegistros() != null) {
                            log.info("Found {} company records for NIT: {}", response.getRegistros().size(), nit);
                        }
                    }
                    // Always return 200 OK with the response (success or error)
                    return ResponseEntity.ok(response);
                });
    }
}
