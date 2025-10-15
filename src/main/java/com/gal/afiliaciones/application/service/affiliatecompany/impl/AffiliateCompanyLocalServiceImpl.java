package com.gal.afiliaciones.application.service.affiliatecompany.impl;

import com.gal.afiliaciones.application.service.affiliatecompany.AffiliateCompanyLocalService;
import com.gal.afiliaciones.application.service.affiliatecompany.data.AffiliateDataService;
import com.gal.afiliaciones.application.service.affiliatecompany.excel.ExcelAffiliateCompanyService;
import com.gal.afiliaciones.application.service.affiliatecompany.mapper.DependentAffiliateMapper;
import com.gal.afiliaciones.application.service.affiliatecompany.mapper.IndependentAffiliateMapper;
import com.gal.afiliaciones.infrastructure.dto.affiliatecompany.AffiliateCompanyDbApproxResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of affiliate company local service.
 * Refactored to follow Clean Architecture principles with separated concerns.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AffiliateCompanyLocalServiceImpl implements AffiliateCompanyLocalService {

    private static final String SOURCE_BALU = "BALU";
    private static final String SOURCE_EXCEL = "EXCEL";

    private final AffiliateDataService dataService;
    private final DependentAffiliateMapper dependentMapper;
    private final IndependentAffiliateMapper independentMapper;
    private final ExcelAffiliateCompanyService excelService;

    @Override
    public List<AffiliateCompanyDbApproxResponseDTO> findByDocument(String documentTypeId, String affiliateId) {
        return findByDocument(documentTypeId, affiliateId, null);
    }
    
    @Override
    public List<AffiliateCompanyDbApproxResponseDTO> findByDocument(String documentTypeId, String affiliateId, String appSource) {
        if (appSource != null) {
            String src = appSource.trim().toUpperCase(Locale.ROOT);
            if (SOURCE_BALU.equals(src)) {
                log.info("[AffiliateCompanyLocal] Forced {} source for idTipoDoc={}, idAfiliado={}", SOURCE_BALU, documentTypeId, affiliateId);
                List<AffiliateCompanyDbApproxResponseDTO> result = queryFromBalu(documentTypeId, affiliateId);
                result.forEach(dto -> dto.setAppSource(SOURCE_BALU));
                return result;
            }
            if (SOURCE_EXCEL.equals(src)) {
                log.info("[AffiliateCompanyLocal] Forced {} source for idTipoDoc={}, idAfiliado={}", SOURCE_EXCEL, documentTypeId, affiliateId);
                List<AffiliateCompanyDbApproxResponseDTO> result = excelService.findByDocument(documentTypeId, affiliateId);
                result.forEach(dto -> dto.setAppSource(SOURCE_EXCEL));
                return result;
            }
        }
        
        // Default behavior: Async BALU → Excel fallback (like EmployerEmployeeQueryService)
        return queryWithFallback(documentTypeId, affiliateId);
    }

    /**
     * Queries with BALU → Excel fallback pattern using async execution.
     * Follows the same pattern as EmployerEmployeeQueryServiceImpl.
     */
    private List<AffiliateCompanyDbApproxResponseDTO> queryWithFallback(String documentTypeId, String affiliateId) {
        log.info("[AffiliateCompanyLocal] Starting async query with fallback for idTipoDoc={}, idAfiliado={}", documentTypeId, affiliateId);

        CompletableFuture<List<AffiliateCompanyDbApproxResponseDTO>> baluFuture = CompletableFuture.supplyAsync(() -> {
            try {
                List<AffiliateCompanyDbApproxResponseDTO> result = queryFromBalu(documentTypeId, affiliateId);
                log.debug("[AffiliateCompanyLocal] BALU query completed with {} results", result.size());
                return result;
            } catch (Exception e) {
                log.warn("[AffiliateCompanyLocal] BALU query failed: {}", e.getMessage());
                return new ArrayList<>();
            }
        });

        CompletableFuture<List<AffiliateCompanyDbApproxResponseDTO>> excelFuture = CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("[AffiliateCompanyLocal] Launching Excel query in parallel");
                List<AffiliateCompanyDbApproxResponseDTO> result = excelService.findByDocument(documentTypeId, affiliateId);
                log.debug("[AffiliateCompanyLocal] Excel query completed with {} results", result.size());
                return result;
            } catch (Exception e) {
                log.warn("[AffiliateCompanyLocal] Excel query failed: {}", e.getMessage());
                return new ArrayList<>();
            }
        });

        // Priority: BALU → Excel (same pattern as EmployerEmployeeQueryService)
        List<AffiliateCompanyDbApproxResponseDTO> baluResult = baluFuture.join();
        if (!baluResult.isEmpty()) {
            log.info("[AffiliateCompanyLocal] Returning {} response with {} records", SOURCE_BALU, baluResult.size());
            baluResult.forEach(dto -> dto.setAppSource(SOURCE_BALU));
            return baluResult;
        }

        List<AffiliateCompanyDbApproxResponseDTO> excelResult = excelFuture.join();
        if (!excelResult.isEmpty()) {
            log.info("[AffiliateCompanyLocal] {} empty. Returning {} response with {} records", SOURCE_BALU, SOURCE_EXCEL, excelResult.size());
            excelResult.forEach(dto -> dto.setAppSource(SOURCE_EXCEL));
            return excelResult;
        }

        log.info("[AffiliateCompanyLocal] No records found in BALU or Excel");
        return new ArrayList<>();
    }
    
    private List<AffiliateCompanyDbApproxResponseDTO> queryFromBalu(String documentTypeId, String affiliateId) {
        String documentType = normalizeDocumentType(documentTypeId);
        String documentNumber = normalizeDocumentNumber(affiliateId);
        
        long t0 = System.currentTimeMillis();
        log.debug("[BALU][V1] queryFromBalu start tDoc={} id={} thread={}", documentType, documentNumber, Thread.currentThread().getName());

        // Run dependents and independents in parallel, then combine deterministically
        CompletableFuture<List<AffiliateCompanyDbApproxResponseDTO>> dependentsFuture = CompletableFuture.supplyAsync(() -> {
            long sd = System.currentTimeMillis();
            log.debug("[BALU][V1] dependents fetch start id={} thread={}", documentNumber, Thread.currentThread().getName());
            List<AffiliateCompanyDbApproxResponseDTO> list = new ArrayList<>();
            dataService.findDependentsByDocument(documentType, documentNumber)
                .forEach(dependent -> {
                    long md0 = System.currentTimeMillis();
                    var employerAffiliate = dataService.findEmployerAffiliate(dependent.getIdAffiliateEmployer()).orElse(null);
                    long beforeMap = System.currentTimeMillis();
                    log.debug("[BALU][V1] dependent pre-map employerLookupMs={}", (beforeMap - md0));
                    list.add(dependentMapper.map(employerAffiliate, dependent));
                    if (log.isDebugEnabled()) {
                        log.debug("[BALU][V1] mapped dependent idPersona={} mapOnlyMs={} totalMs={}", 
                                dependent.getIdentificationDocumentNumber(), 
                                (System.currentTimeMillis() - beforeMap),
                                (System.currentTimeMillis() - md0));
                    }
                });
            log.debug("[BALU][V1] dependents fetch done count={} in {}ms", list.size(), (System.currentTimeMillis() - sd));
            return list;
        });

        CompletableFuture<List<AffiliateCompanyDbApproxResponseDTO>> independentsFuture = CompletableFuture.supplyAsync(() -> {
            long si = System.currentTimeMillis();
            log.debug("[BALU][V1] independents fetch start id={} thread={}", documentNumber, Thread.currentThread().getName());
            List<AffiliateCompanyDbApproxResponseDTO> list = new ArrayList<>();
            dataService.findIndependentsByDocument(documentType, documentNumber)
                .forEach(independent -> {
                    long mi0 = System.currentTimeMillis();
                    var affiliate = dataService.findAffiliateByFiledNumber(independent.getFiledNumber()).orElse(null);
                    list.add(independentMapper.map(affiliate, independent));
                    if (log.isDebugEnabled()) {
                        log.debug("[BALU][V1] mapped independent idPersona={} in {}ms", independent.getIdentificationDocumentNumber(), (System.currentTimeMillis() - mi0));
                    }
                });
            log.debug("[BALU][V1] independents fetch done count={} in {}ms", list.size(), (System.currentTimeMillis() - si));
            return list;
        });

        CompletableFuture.allOf(dependentsFuture, independentsFuture).join();

        List<AffiliateCompanyDbApproxResponseDTO> results = new ArrayList<>();
        // preserve previous ordering: dependents first, then independents
        results.addAll(dependentsFuture.join());
        results.addAll(independentsFuture.join());
        log.debug("[BALU][V1] queryFromBalu done total={} in {}ms", results.size(), (System.currentTimeMillis() - t0));
        return results;
    }
    
    private String normalizeDocumentType(String documentType) {
        return documentType == null ? null : documentType.trim().toUpperCase(Locale.ROOT);
    }
    
    private String normalizeDocumentNumber(String documentNumber) {
        return documentNumber == null ? null : documentNumber.trim();
    }
}
