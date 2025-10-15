package com.gal.afiliaciones.application.service.affiliatecompany.excel.impl;

import com.gal.afiliaciones.application.service.affiliatecompany.excel.ExcelAffiliateCompanyService;
import com.gal.afiliaciones.application.service.affiliatecompany.excel.ExcelAffiliateCompanyEnrichmentService;
import com.gal.afiliaciones.application.service.affiliatecompany.excel.mapper.ExcelDependentMapper;
import com.gal.afiliaciones.application.service.affiliatecompany.excel.mapper.ExcelIndependentMapper;
import com.gal.afiliaciones.infrastructure.dao.repository.tmp.ExcelDependentTmpRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.tmp.ExcelIndependentTmpRepository;
import com.gal.afiliaciones.infrastructure.dto.affiliatecompany.AffiliateCompanyDbApproxResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of Excel affiliate company service.
 * Queries Excel temporary tables (tmp_excel_dependientes, tmp_excel_independientes) 
 * using async parallel execution pattern.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelAffiliateCompanyServiceImpl implements ExcelAffiliateCompanyService {

    private final ExcelDependentTmpRepository dependentRepository;
    private final ExcelIndependentTmpRepository independentRepository;
    private final ExcelDependentMapper dependentMapper;
    private final ExcelIndependentMapper independentMapper;
    private final ExcelAffiliateCompanyEnrichmentService enrichmentService;

    @Override
    @Transactional(readOnly = true)
    public List<AffiliateCompanyDbApproxResponseDTO> findByDocument(String documentType, String documentNumber) {
        log.info("[ExcelAffiliateCompany] Searching Excel tables for documentType={}, documentNumber={}", documentType, documentNumber);
        
        String normalizedDocType = documentType == null ? null : documentType.trim().toUpperCase(Locale.ROOT);
        String normalizedDocNumber = documentNumber == null ? null : documentNumber.trim();

        // Async parallel execution like EmployerEmployeeQueryServiceImpl
        CompletableFuture<List<AffiliateCompanyDbApproxResponseDTO>> dependentsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                List<AffiliateCompanyDbApproxResponseDTO> results = dependentRepository
                    .findByDocumentTypeAndDocumentNumber(normalizedDocType, normalizedDocNumber)
                    .stream()
                    .map(dependentMapper::map)
                    .toList();
                log.debug("[ExcelAffiliateCompany] Found {} dependents in Excel", results.size());
                return results;
            } catch (Exception e) {
                log.warn("[ExcelAffiliateCompany] Error querying Excel dependents: {}", e.getMessage());
                return new ArrayList<>();
            }
        });

        CompletableFuture<List<AffiliateCompanyDbApproxResponseDTO>> independentsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                List<AffiliateCompanyDbApproxResponseDTO> results = independentRepository
                    .findByDocumentTypeAndDocumentNumber(normalizedDocType, normalizedDocNumber)
                    .stream()
                    .map(independentMapper::map)
                    .toList();
                log.debug("[ExcelAffiliateCompany] Found {} independents in Excel", results.size());
                return results;
            } catch (Exception e) {
                log.warn("[ExcelAffiliateCompany] Error querying Excel independents: {}", e.getMessage());
                return new ArrayList<>();
            }
        });

        // Wait for both futures and combine results
        CompletableFuture.allOf(dependentsFuture, independentsFuture).join();
        
        List<AffiliateCompanyDbApproxResponseDTO> result = new ArrayList<>();
        result.addAll(dependentsFuture.join());
        result.addAll(independentsFuture.join());
        
        log.info("[ExcelAffiliateCompany] Found {} total records in Excel tables", result.size());
        
        // Enrich with descriptive names (same pattern as EmployerEmployeeQueryServiceImpl)
        if (!result.isEmpty()) {
            log.debug("[ExcelAffiliateCompany] Enriching Excel records with descriptive names");
            enrichmentService.enrichDescriptions(result);
            log.debug("[ExcelAffiliateCompany] Enrichment completed");
        }
        
        return result;
    }
}
