package com.gal.afiliaciones.application.service.affiliatecompany.impl;

import com.gal.afiliaciones.application.service.affiliatecompany.AffiliateCompanyV2Service;
import com.gal.afiliaciones.application.service.affiliatecompany.data.AffiliateDataService;
import com.gal.afiliaciones.application.service.affiliatecompany.excel.ExcelAffiliateCompanyV2Service;
import com.gal.afiliaciones.application.service.affiliatecompany.mapper.DependentAffiliateV2Mapper;
import com.gal.afiliaciones.application.service.affiliatecompany.mapper.IndependentAffiliateV2Mapper;
import com.gal.afiliaciones.infrastructure.dto.affiliatecompany.AffiliateCompanyV2ResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * V2 Implementation of affiliate company service.
 * Follows the same pattern as AffiliateCompanyLocalServiceImpl but returns V2 DTOs
 * with JSON2 field structure and async fallback logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AffiliateCompanyV2ServiceImpl implements AffiliateCompanyV2Service {

    private static final String SOURCE_BALU = "BALU";
    private static final String SOURCE_EXCEL = "EXCEL";

    private final AffiliateDataService dataService;
    private final DependentAffiliateV2Mapper dependentMapper;
    private final IndependentAffiliateV2Mapper independentMapper;
    private final ExcelAffiliateCompanyV2Service excelService;

    @Override
    public List<AffiliateCompanyV2ResponseDTO> findByDocument(String documentTypeId, String affiliateId) {
        return findByDocument(documentTypeId, affiliateId, null);
    }
    
    @Override
    public List<AffiliateCompanyV2ResponseDTO> findByDocument(String documentTypeId, String affiliateId, String appSource) {
        if (appSource != null) {
            String src = appSource.trim().toUpperCase(Locale.ROOT);
            if (SOURCE_BALU.equals(src)) {
                log.info("[AffiliateCompanyV2] Forced {} source for idTipoDoc={}, idAfiliado={}", SOURCE_BALU, documentTypeId, affiliateId);
                List<AffiliateCompanyV2ResponseDTO> result = queryFromBalu(documentTypeId, affiliateId);
                result.forEach(dto -> dto.setAppSource(SOURCE_BALU));
                return result;
            }
            if (SOURCE_EXCEL.equals(src)) {
                log.info("[AffiliateCompanyV2] Forced {} source for idTipoDoc={}, idAfiliado={}", SOURCE_EXCEL, documentTypeId, affiliateId);
                List<AffiliateCompanyV2ResponseDTO> result = excelService.findByDocument(documentTypeId, affiliateId);
                result.forEach(dto -> dto.setAppSource(SOURCE_EXCEL));
                return result;
            }
        }
        
        // Default behavior: Async BALU → Excel fallback (same pattern as V1)
        return queryWithFallback(documentTypeId, affiliateId);
    }

    /**
     * Queries with BALU → Excel fallback pattern using async execution.
     * Follows the same pattern as AffiliateCompanyLocalServiceImpl.
     */
    private List<AffiliateCompanyV2ResponseDTO> queryWithFallback(String documentTypeId, String affiliateId) {
        log.info("[AffiliateCompanyV2] Starting async query with fallback for idTipoDoc={}, idAfiliado={}", documentTypeId, affiliateId);

        CompletableFuture<List<AffiliateCompanyV2ResponseDTO>> baluFuture = CompletableFuture.supplyAsync(() -> {
            try {
                List<AffiliateCompanyV2ResponseDTO> result = queryFromBalu(documentTypeId, affiliateId);
                log.debug("[AffiliateCompanyV2] BALU query completed with {} results", result.size());
                return result;
            } catch (Exception e) {
                log.warn("[AffiliateCompanyV2] BALU query failed: {}", e.getMessage());
                return new ArrayList<>();
            }
        });

        CompletableFuture<List<AffiliateCompanyV2ResponseDTO>> excelFuture = CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("[AffiliateCompanyV2] Launching Excel query in parallel");
                List<AffiliateCompanyV2ResponseDTO> result = excelService.findByDocument(documentTypeId, affiliateId);
                log.debug("[AffiliateCompanyV2] Excel query completed with {} results", result.size());
                return result;
            } catch (Exception e) {
                log.warn("[AffiliateCompanyV2] Excel query failed: {}", e.getMessage());
                return new ArrayList<>();
            }
        });

        // Priority: BALU → Excel (same pattern as V1)
        List<AffiliateCompanyV2ResponseDTO> baluResult = baluFuture.join();
        if (!baluResult.isEmpty()) {
            log.info("[AffiliateCompanyV2] Returning {} response with {} records", SOURCE_BALU, baluResult.size());
            baluResult.forEach(dto -> dto.setAppSource(SOURCE_BALU));
            return baluResult;
        }

        List<AffiliateCompanyV2ResponseDTO> excelResult = excelFuture.join();
        if (!excelResult.isEmpty()) {
            log.info("[AffiliateCompanyV2] {} empty. Returning {} response with {} records", SOURCE_BALU, SOURCE_EXCEL, excelResult.size());
            excelResult.forEach(dto -> dto.setAppSource(SOURCE_EXCEL));
            return excelResult;
        }

        log.info("[AffiliateCompanyV2] No records found in BALU or Excel");
        return new ArrayList<>();
    }
    
    private List<AffiliateCompanyV2ResponseDTO> queryFromBalu(String documentTypeId, String affiliateId) {
        String documentType = normalizeDocumentType(documentTypeId);
        String documentNumber = normalizeDocumentNumber(affiliateId);
        
        long t0 = System.currentTimeMillis();
        log.debug("[BALU][V2] queryFromBalu start tDoc={} id={} thread={}", documentType, documentNumber, Thread.currentThread().getName());

        // Run dependents and independents in parallel, then combine deterministically
        CompletableFuture<List<AffiliateCompanyV2ResponseDTO>> dependentsFuture = CompletableFuture.supplyAsync(() -> {
            long sd = System.currentTimeMillis();
            log.debug("[BALU][V2] dependents fetch start id={} thread={}", documentNumber, Thread.currentThread().getName());
            List<AffiliateCompanyV2ResponseDTO> list = new ArrayList<>();
            dataService.findDependentsByDocument(documentType, documentNumber)
                .forEach(dependent -> {
                    long md0 = System.currentTimeMillis();
                    var employerAffiliate = dataService.findEmployerAffiliate(dependent.getIdAffiliateEmployer()).orElse(null);
                    long beforeMap = System.currentTimeMillis();
                    log.debug("[BALU][V2] dependent pre-map employerLookupMs={}", (beforeMap - md0));
                    list.add(dependentMapper.map(employerAffiliate, dependent));
                    if (log.isDebugEnabled()) {
                        log.debug("[BALU][V2] mapped dependent idPersona={} mapOnlyMs={} totalMs={}", 
                                dependent.getIdentificationDocumentNumber(), 
                                (System.currentTimeMillis() - beforeMap),
                                (System.currentTimeMillis() - md0));
                    }
                });
            log.debug("[BALU][V2] dependents fetch done count={} in {}ms", list.size(), (System.currentTimeMillis() - sd));
            return list;
        });

        CompletableFuture<List<AffiliateCompanyV2ResponseDTO>> independentsFuture = CompletableFuture.supplyAsync(() -> {
            long si = System.currentTimeMillis();
            log.debug("[BALU][V2] independents fetch start id={} thread={}", documentNumber, Thread.currentThread().getName());
            List<AffiliateCompanyV2ResponseDTO> list = new ArrayList<>();
            dataService.findIndependentsByDocument(documentType, documentNumber)
                .forEach(independent -> {
                    long mi0 = System.currentTimeMillis();
                    var affiliate = dataService.findAffiliateByFiledNumber(independent.getFiledNumber()).orElse(null);
                    list.add(independentMapper.map(affiliate, independent));
                    if (log.isDebugEnabled()) {
                        log.debug("[BALU][V2] mapped independent idPersona={} in {}ms", independent.getIdentificationDocumentNumber(), (System.currentTimeMillis() - mi0));
                    }
                });
            log.debug("[BALU][V2] independents fetch done count={} in {}ms", list.size(), (System.currentTimeMillis() - si));
            return list;
        });

        CompletableFuture.allOf(dependentsFuture, independentsFuture).join();

        List<AffiliateCompanyV2ResponseDTO> results = new ArrayList<>();
        // preserve previous ordering: dependents first, then independents
        results.addAll(dependentsFuture.join());
        results.addAll(independentsFuture.join());
        log.debug("[BALU][V2] queryFromBalu done total={} in {}ms", results.size(), (System.currentTimeMillis() - t0));
        return results;
    }
    
    private String normalizeDocumentType(String documentType) {
        return documentType == null ? null : documentType.trim().toUpperCase(Locale.ROOT);
    }
    
    private String normalizeDocumentNumber(String documentNumber) {
        return documentNumber == null ? null : documentNumber.trim();
    }
}
