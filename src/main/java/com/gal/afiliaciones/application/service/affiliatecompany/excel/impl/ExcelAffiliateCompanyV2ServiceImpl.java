package com.gal.afiliaciones.application.service.affiliatecompany.excel.impl;

import com.gal.afiliaciones.application.service.affiliatecompany.excel.ExcelAffiliateCompanyV2Service;
import com.gal.afiliaciones.application.service.affiliatecompany.excel.ExcelAffiliateCompanyV2EnrichmentService;
import com.gal.afiliaciones.application.service.affiliatecompany.excel.mapper.ExcelDependentV2Mapper;
import com.gal.afiliaciones.application.service.affiliatecompany.excel.mapper.ExcelIndependentV2Mapper;
import com.gal.afiliaciones.infrastructure.dao.repository.tmp.ExcelDependentTmpRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.tmp.ExcelIndependentTmpRepository;
import com.gal.afiliaciones.infrastructure.dto.affiliatecompany.AffiliateCompanyV2ResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * V2 Implementation of Excel affiliate company service.
 * Queries Excel temporary tables (tmp_excel_dependientes, tmp_excel_independientes) 
 * using async parallel execution pattern and returns V2 DTOs with JSON2 structure.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelAffiliateCompanyV2ServiceImpl implements ExcelAffiliateCompanyV2Service {

    private final ExcelDependentTmpRepository dependentRepository;
    private final ExcelIndependentTmpRepository independentRepository;
    private final ExcelDependentV2Mapper dependentMapper;
    private final ExcelIndependentV2Mapper independentMapper;
    private final ExcelAffiliateCompanyV2EnrichmentService enrichmentService;

    @Override
    @Transactional(readOnly = true)
    public List<AffiliateCompanyV2ResponseDTO> findByDocument(String documentType, String documentNumber) {
        log.info("[ExcelAffiliateCompanyV2] Searching Excel tables for documentType={}, documentNumber={}", documentType, documentNumber);
        
        String normalizedDocType = documentType == null ? null : documentType.trim().toUpperCase(Locale.ROOT);
        String normalizedDocNumber = documentNumber == null ? null : documentNumber.trim();

        // Async parallel execution like ExcelAffiliateCompanyServiceImpl
        CompletableFuture<List<AffiliateCompanyV2ResponseDTO>> dependentsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                List<AffiliateCompanyV2ResponseDTO> results = dependentRepository
                    .findByDocumentTypeAndDocumentNumber(normalizedDocType, normalizedDocNumber)
                    .stream()
                    .map(dependentMapper::map)
                    .toList();
                log.debug("[ExcelAffiliateCompanyV2] Found {} dependents in Excel", results.size());
                return results;
            } catch (Exception e) {
                log.warn("[ExcelAffiliateCompanyV2] Error querying Excel dependents: {}", e.getMessage());
                return new ArrayList<>();
            }
        });

        CompletableFuture<List<AffiliateCompanyV2ResponseDTO>> independentsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                List<AffiliateCompanyV2ResponseDTO> results = independentRepository
                    .findByDocumentTypeAndDocumentNumber(normalizedDocType, normalizedDocNumber)
                    .stream()
                    .map(independentMapper::map)
                    .toList();
                log.debug("[ExcelAffiliateCompanyV2] Found {} independents in Excel", results.size());
                return results;
            } catch (Exception e) {
                log.warn("[ExcelAffiliateCompanyV2] Error querying Excel independents: {}", e.getMessage());
                return new ArrayList<>();
            }
        });

        // Wait for both futures and combine results
        CompletableFuture.allOf(dependentsFuture, independentsFuture).join();
        
        List<AffiliateCompanyV2ResponseDTO> result = new ArrayList<>();
        result.addAll(dependentsFuture.join());
        result.addAll(independentsFuture.join());
        
        log.info("[ExcelAffiliateCompanyV2] Found {} total records in Excel tables", result.size());
        
        // Enrich with descriptive names (same pattern as V1)
        if (!result.isEmpty()) {
            log.debug("[ExcelAffiliateCompanyV2] Enriching Excel records with descriptive names");
            enrichmentService.enrichDescriptions(result);
            log.debug("[ExcelAffiliateCompanyV2] Enrichment completed");
        }
        
        return result;
    }
}
