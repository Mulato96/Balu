package com.gal.afiliaciones.infrastructure.controller.affiliatecompany;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.application.service.affiliatecompany.AffiliateCompanyLocalService;
import com.gal.afiliaciones.application.service.affiliatecompany.AffiliateCompanyV2Service;
import com.gal.afiliaciones.application.service.positiva.PositivaEmployerMercantileService;
import com.gal.afiliaciones.infrastructure.client.generic.affiliatecompany.ConsultAffiliateCompanyRawClient;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.PositivaEmployerMercantileDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliatecompany.AffiliateCompanyDbApproxResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliatecompany.AffiliateCompanyV2ResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("")
@Tag(name = "AffiliateCompanyLocal", description = "Local replica of consultaAfiliadoEmpresa from DB")
@RequiredArgsConstructor
@Slf4j
public class AffiliateCompanyLocalController {

    private final AffiliateCompanyLocalService service;
    private final AffiliateCompanyV2Service serviceV2;
    private final PositivaEmployerMercantileService positivaEmployerMercantileService;
    private final ConsultAffiliateCompanyRawClient consultAffiliateCompanyRawClient;
    private final ObjectMapper objectMapper;

    private static final String DEPENDENT_UPPER = "DEPENDIENTE";
    private static final String INDEPENDENT_UPPER = "INDEPENDIENTE";
    private static final String BUSINESSMAN_UPPER = "EMPRESARIO";
    private static final String EMPLOYER_UPPER = "EMPLEADOR";
    private static final String DEPENDENT_CAPITALIZED = "Dependiente";
    private static final String INDEPENDENT_CAPITALIZED = "Independiente";
    private static final String EMPLOYER_CAPITALIZED = "Empleador";

    @GetMapping("/v1/positiva/scp/afiliado/consultaAfiliadoEmpresa")
    @Operation(summary = "Consulta afiliado empresa (BUS integration-first with DB fallback)")
    public ResponseEntity<String> consultaAfiliadoEmpresa(
            @RequestParam("idTipoDoc") String idTipoDoc,
            @RequestParam("idAfiliado") String idAfiliado,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "appSource", required = false) String appSource,
            @RequestParam(value = "showAppSource", required = false, defaultValue = "false") boolean showAppSource,
            @RequestHeader(name = "Authorization", required = false) String authorization
    ) {
        log.info("[AffiliateCompanyLocalController] consultaAfiliadoEmpresa idTipoDoc={} idAfiliado={} tipo={} appSource={} showAppSource={}", 
            idTipoDoc, idAfiliado, tipo, appSource, showAppSource);

        // If appSource is provided, skip integration and go directly to local service
        if (appSource != null && !appSource.isBlank()) {
            List<AffiliateCompanyDbApproxResponseDTO> result = service.findByDocument(idTipoDoc, idAfiliado, appSource);

            if (tipo != null && !tipo.isBlank()) {
                String t = tipo.trim().toUpperCase(java.util.Locale.ROOT);
                if (DEPENDENT_UPPER.equals(t)) {
                    result = result.stream()
                            .filter(r -> DEPENDENT_CAPITALIZED.equalsIgnoreCase(r.getNomVinLaboral()))
                            .toList();
                } else if (INDEPENDENT_UPPER.equals(t)) {
                    result = result.stream()
                            .filter(r -> INDEPENDENT_CAPITALIZED.equalsIgnoreCase(r.getNomVinLaboral()))
                            .toList();
                } else if (EMPLOYER_UPPER.equals(t) || BUSINESSMAN_UPPER.equals(t)) {
                    result = result.stream()
                            .filter(r -> EMPLOYER_CAPITALIZED.equalsIgnoreCase(r.getNomVinLaboral()))
                            .toList();
                }
            }

            if (!showAppSource) {
                result.forEach(r -> r.setAppSource(null));
            }

            try {
                String dtoJson = objectMapper.writeValueAsString(result);
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(dtoJson);
            } catch (Exception ex) {
                log.error("[AffiliateCompanyLocalController] Failed to serialize DTO list, returning empty array: {}", ex.getMessage());
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("[]");
            }
        }

        // ============ TEMPORARY CHANGE: ONLY USE SIARP, NO FALLBACKS ============
        // Return ONLY the raw SIARP response, even if it's empty, error, or 500
        String raw = null;
        try {
            raw = consultAffiliateCompanyRawClient.consultRawV1(idTipoDoc, idAfiliado).block();
            log.info("[AffiliateCompanyLocalController] SIARP V1 raw response (NO FALLBACK MODE): {}", raw != null ? raw.substring(0, Math.min(100, raw.length())) : "null");
            // Return whatever SIARP returns, no conditions
            if (raw != null) {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(raw);
            } else {
                log.warn("[AffiliateCompanyLocalController] SIARP V1 returned null, returning empty array (NO FALLBACK MODE)");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("[]");
            }
        } catch (Exception ex) {
            log.error("[AffiliateCompanyLocalController] SIARP V1 call failed (NO FALLBACK MODE), returning error as-is: {}", ex.getMessage());
            // Return error response instead of falling back
            return ResponseEntity.status(500).contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"SIARP call failed\",\"message\":\"" + ex.getMessage().replace("\"", "'") + "\"}");
        }

        /* ============ FALLBACK CODE TEMPORARILY DISABLED ============
        // Default: return RAW JSON from BUS integration (v1) if non-empty array; fallback to DTO list
        String raw = null;
        try {
            raw = consultAffiliateCompanyRawClient.consultRawV1(idTipoDoc, idAfiliado).block();
        } catch (Exception ex) {
            log.warn("[AffiliateCompanyLocalController] BUS RAW call failed, falling back to DTO: {}", ex.getMessage());
        }

        if (raw != null) {
            String trimmed = raw.trim();
            if (!("[]".equals(trimmed) || "[ ]".equals(trimmed))) {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(raw);
            }
        }

        // Fallback to local service
        List<AffiliateCompanyDbApproxResponseDTO> result = service.findByDocument(idTipoDoc, idAfiliado, appSource);

        if (tipo != null && !tipo.isBlank()) {
            String t = tipo.trim().toUpperCase(java.util.Locale.ROOT);
            if (DEPENDENT_UPPER.equals(t)) {
                result = result.stream()
                        .filter(r -> DEPENDENT_CAPITALIZED.equalsIgnoreCase(r.getNomVinLaboral()))
                        .toList();
            } else if (INDEPENDENT_UPPER.equals(t)) {
                result = result.stream()
                        .filter(r -> INDEPENDENT_CAPITALIZED.equalsIgnoreCase(r.getNomVinLaboral()))
                        .toList();
            } else if (EMPLOYER_UPPER.equals(t) || BUSINESSMAN_UPPER.equals(t)) {
                result = result.stream()
                        .filter(r -> EMPLOYER_CAPITALIZED.equalsIgnoreCase(r.getNomVinLaboral()))
                        .toList();
            }
        }
        
        // Hide APP_SOURCE field if showAppSource is false
        if (!showAppSource) {
            result.forEach(r -> r.setAppSource(null));
        }
        
        try {
            String dtoJson = objectMapper.writeValueAsString(result);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(dtoJson);
        } catch (Exception ex) {
            log.error("[AffiliateCompanyLocalController] Failed to serialize DTO list, returning empty array: {}", ex.getMessage());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("[]");
        }
        ============ END OF DISABLED FALLBACK CODE ============ */
    }

    @GetMapping("/v1/positiva/scp/afiliado/consultaAfiliadoEmpresa/balu")
    @Operation(summary = "Consulta afiliado empresa V1 - ONLY BALU (no fallbacks)")
    public ResponseEntity<String> consultaAfiliadoEmpresaBaluOnlyV1(
            @RequestParam("idTipoDoc") String idTipoDoc,
            @RequestParam("idAfiliado") String idAfiliado,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "showAppSource", required = false, defaultValue = "false") boolean showAppSource
    ) {
        log.info("[AffiliateCompanyLocalController] consultaAfiliadoEmpresaBaluOnlyV1 idTipoDoc={} idAfiliado={} tipo={}", 
            idTipoDoc, idAfiliado, tipo);

        // Force BALU source only - no SIARP, no Excel fallback
        List<AffiliateCompanyDbApproxResponseDTO> result = service.findByDocument(idTipoDoc, idAfiliado, "BALU");

        // Apply tipo filter if provided
        if (tipo != null && !tipo.isBlank()) {
            String t = tipo.trim().toUpperCase(java.util.Locale.ROOT);
            if (DEPENDENT_UPPER.equals(t)) {
                result = result.stream()
                        .filter(r -> DEPENDENT_CAPITALIZED.equalsIgnoreCase(r.getNomVinLaboral()))
                        .toList();
            } else if (INDEPENDENT_UPPER.equals(t)) {
                result = result.stream()
                        .filter(r -> INDEPENDENT_CAPITALIZED.equalsIgnoreCase(r.getNomVinLaboral()))
                        .toList();
            } else if (EMPLOYER_UPPER.equals(t) || BUSINESSMAN_UPPER.equals(t)) {
                result = result.stream()
                        .filter(r -> EMPLOYER_CAPITALIZED.equalsIgnoreCase(r.getNomVinLaboral()))
                        .toList();
            }
        }

        // Hide APP_SOURCE field if showAppSource is false
        if (!showAppSource) {
            result.forEach(r -> r.setAppSource(null));
        }

        try {
            String dtoJson = objectMapper.writeValueAsString(result);
            log.info("[AffiliateCompanyLocalController] Returning {} BALU-only V1 records", result.size());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(dtoJson);
        } catch (Exception ex) {
            log.error("[AffiliateCompanyLocalController] Failed to serialize BALU-only DTO list, returning empty array: {}", ex.getMessage());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("[]");
        }
    }

    @GetMapping("/v1/positiva/scp/afiliaciones/empleador")
    @Operation(summary = "Consulta empleador mercantil desde vista local v_positiva_empleador_mercantile")
    public ResponseEntity<String> consultaEmpleadorMercantil(
            @RequestParam("idTipoDoc") String idTipoDoc,
            @RequestParam("idEmpresa") String idEmpresa,
            @RequestParam("idSubEmpresa") Integer idSubEmpresa
    ) {
        log.info("[AffiliateCompanyLocalController] consultaEmpleadorMercantil idTipoDoc={} idEmpresa={} idSubEmpresa={}",
                idTipoDoc, idEmpresa, idSubEmpresa);
        try {
            List<PositivaEmployerMercantileDTO> result =
                    positivaEmployerMercantileService.findEmployers(idTipoDoc, idEmpresa, idSubEmpresa);
            String dtoJson = objectMapper.writeValueAsString(result);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(dtoJson);
        } catch (Exception ex) {
            log.error("[AffiliateCompanyLocalController] Failed to query/serialize empleador mercantil: {}", ex.getMessage());
            return ResponseEntity.status(500).contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Query failed\",\"message\":\"" + ex.getMessage().replace("\"", "'") + "\"}");
        }
    }

    @GetMapping("/v2/positiva/scp/afiliado/consultaAfiliadoEmpresa")
    @Operation(summary = "Consulta afiliado empresa V2 (BUS integration-first with DB fallback)")
    public ResponseEntity<String> consultaAfiliadoEmpresaV2(
            @RequestParam("idTipoDoc") String idTipoDoc,
            @RequestParam("idAfiliado") String idAfiliado,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "appSource", required = false) String appSource,
            @RequestParam(value = "showAppSource", required = false, defaultValue = "false") boolean showAppSource,
            @RequestHeader(name = "Authorization", required = false) String authorization
    ) {
        log.info("[AffiliateCompanyLocalController] consultaAfiliadoEmpresaV2 idTipoDoc={} idAfiliado={} tipo={} appSource={} showAppSource={}", 
            idTipoDoc, idAfiliado, tipo, appSource, showAppSource);

        // If appSource is provided, skip integration and go directly to local V2 service
        if (appSource != null && !appSource.isBlank()) {
            List<AffiliateCompanyV2ResponseDTO> result = serviceV2.findByDocument(idTipoDoc, idAfiliado, appSource);

            if (tipo != null && !tipo.isBlank()) {
                String t = tipo.trim().toUpperCase(java.util.Locale.ROOT);
                if (DEPENDENT_UPPER.equals(t)) {
                    result = result.stream()
                            .filter(r -> DEPENDENT_CAPITALIZED.equalsIgnoreCase(r.getNomVinLaboral()))
                            .toList();
                } else if (INDEPENDENT_UPPER.equals(t)) {
                    result = result.stream()
                            .filter(r -> INDEPENDENT_CAPITALIZED.equalsIgnoreCase(r.getNomVinLaboral()))
                            .toList();
                } else if (EMPLOYER_UPPER.equals(t) || BUSINESSMAN_UPPER.equals(t)) {
                    result = result.stream()
                            .filter(r -> EMPLOYER_CAPITALIZED.equalsIgnoreCase(r.getNomVinLaboral()))
                            .toList();
                }
            }

            if (!showAppSource) {
                result.forEach(r -> r.setAppSource(null));
            }

            try {
                String dtoJson = objectMapper.writeValueAsString(result);
                log.info("[AffiliateCompanyLocalController] Returning {} V2 records as JSON (skip integration due to appSource)", result.size());
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(dtoJson);
            } catch (Exception ex) {
                log.error("[AffiliateCompanyLocalController] Failed to serialize V2 DTO list, returning empty array: {}", ex.getMessage());
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("[]");
            }
        }

        // ============ TEMPORARY CHANGE: ONLY USE SIARP, NO FALLBACKS ============
        // Return ONLY the raw SIARP response, even if it's empty, error, or 500
        String raw = null;
        try {
            raw = consultAffiliateCompanyRawClient.consultRawV2(idTipoDoc, idAfiliado).block();
            log.info("[AffiliateCompanyLocalController] SIARP V2 raw response (NO FALLBACK MODE): {}", raw != null ? raw.substring(0, Math.min(100, raw.length())) : "null");
            // Return whatever SIARP returns, no conditions
            if (raw != null) {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(raw);
            } else {
                log.warn("[AffiliateCompanyLocalController] SIARP V2 returned null, returning empty array (NO FALLBACK MODE)");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("[]");
            }
        } catch (Exception ex) {
            log.error("[AffiliateCompanyLocalController] SIARP V2 call failed (NO FALLBACK MODE), returning error as-is: {}", ex.getMessage());
            // Return error response instead of falling back
            return ResponseEntity.status(500).contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"SIARP call failed\",\"message\":\"" + ex.getMessage().replace("\"", "'") + "\"}");
        }

        /* ============ FALLBACK CODE TEMPORARILY DISABLED ============
        // Default: return RAW JSON from BUS integration (v2) if non-empty array; fallback to DTO list
        String raw = null;
        try {
            raw = consultAffiliateCompanyRawClient.consultRawV2(idTipoDoc, idAfiliado).block();
        } catch (Exception ex) {
            log.warn("[AffiliateCompanyLocalController] BUS RAW call failed for V2, falling back to DTO: {}", ex.getMessage());
        }

        if (raw != null) {
            String trimmed = raw.trim();
            if (!("[]".equals(trimmed) || "[ ]".equals(trimmed))) {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(raw);
            }
        }

        // Fallback to local service V2
        List<AffiliateCompanyV2ResponseDTO> result = serviceV2.findByDocument(idTipoDoc, idAfiliado, appSource);

        // Apply same filtering logic as V1 endpoint
        if (tipo != null && !tipo.isBlank()) {
            String t = tipo.trim().toUpperCase(java.util.Locale.ROOT);
            if (DEPENDENT_UPPER.equals(t)) {
                result = result.stream()
                        .filter(r -> DEPENDENT_CAPITALIZED.equalsIgnoreCase(r.getNomVinLaboral()))
                        .toList();
            } else if (INDEPENDENT_UPPER.equals(t)) {
                result = result.stream()
                        .filter(r -> INDEPENDENT_CAPITALIZED.equalsIgnoreCase(r.getNomVinLaboral()))
                        .toList();
            } else if (EMPLOYER_UPPER.equals(t) || BUSINESSMAN_UPPER.equals(t)) {
                result = result.stream()
                        .filter(r -> EMPLOYER_CAPITALIZED.equalsIgnoreCase(r.getNomVinLaboral()))
                        .toList();
            }
        }
        
        // Hide APP_SOURCE field if showAppSource is false
        if (!showAppSource) {
            result.forEach(r -> r.setAppSource(null));
        }
        
        try {
            String dtoJson = objectMapper.writeValueAsString(result);
            log.info("[AffiliateCompanyLocalController] Returning {} V2 records as JSON", result.size());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(dtoJson);
        } catch (Exception ex) {
            log.error("[AffiliateCompanyLocalController] Failed to serialize V2 DTO list, returning empty array: {}", ex.getMessage());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("[]");
        }
        ============ END OF DISABLED FALLBACK CODE ============ */
    }

    @GetMapping("/v2/positiva/scp/afiliado/consultaAfiliadoEmpresa/balu")
    @Operation(summary = "Consulta afiliado empresa V2 - ONLY BALU (no fallbacks)")
    public ResponseEntity<String> consultaAfiliadoEmpresaBaluOnlyV2(
            @RequestParam("idTipoDoc") String idTipoDoc,
            @RequestParam("idAfiliado") String idAfiliado,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "showAppSource", required = false, defaultValue = "false") boolean showAppSource
    ) {
        log.info("[AffiliateCompanyLocalController] consultaAfiliadoEmpresaBaluOnlyV2 idTipoDoc={} idAfiliado={} tipo={}", 
            idTipoDoc, idAfiliado, tipo);

        // Force BALU source only - no SIARP, no Excel fallback
        List<AffiliateCompanyV2ResponseDTO> result = serviceV2.findByDocument(idTipoDoc, idAfiliado, "BALU");

        // Apply tipo filter if provided
        if (tipo != null && !tipo.isBlank()) {
            String t = tipo.trim().toUpperCase(java.util.Locale.ROOT);
            if (DEPENDENT_UPPER.equals(t)) {
                result = result.stream()
                        .filter(r -> DEPENDENT_CAPITALIZED.equalsIgnoreCase(r.getNomVinLaboral()))
                        .toList();
            } else if (INDEPENDENT_UPPER.equals(t)) {
                result = result.stream()
                        .filter(r -> INDEPENDENT_CAPITALIZED.equalsIgnoreCase(r.getNomVinLaboral()))
                        .toList();
            } else if (EMPLOYER_UPPER.equals(t) || BUSINESSMAN_UPPER.equals(t)) {
                result = result.stream()
                        .filter(r -> EMPLOYER_CAPITALIZED.equalsIgnoreCase(r.getNomVinLaboral()))
                        .toList();
            }
        }

        // Hide APP_SOURCE field if showAppSource is false
        if (!showAppSource) {
            result.forEach(r -> r.setAppSource(null));
        }

        try {
            String dtoJson = objectMapper.writeValueAsString(result);
            log.info("[AffiliateCompanyLocalController] Returning {} BALU-only V2 records", result.size());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(dtoJson);
        } catch (Exception ex) {
            log.error("[AffiliateCompanyLocalController] Failed to serialize BALU-only V2 DTO list, returning empty array: {}", ex.getMessage());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body("[]");
        }
    }
}


