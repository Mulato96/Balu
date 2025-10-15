package com.gal.afiliaciones.application.service.affiliatecompany.excel;

import com.gal.afiliaciones.infrastructure.dto.affiliatecompany.AffiliateCompanyDbApproxResponseDTO;

import java.util.List;

/**
 * Service for querying affiliate company data from Excel temporary tables.
 * Used as a fallback when BALU database doesn't have the required data.
 */
public interface ExcelAffiliateCompanyService {
    
    /**
     * Finds affiliate company data from Excel temporary tables by document.
     * 
     * @param documentType The document type (CC, CE, etc.)
     * @param documentNumber The document number
     * @return List of affiliate company records from Excel tables
     */
    List<AffiliateCompanyDbApproxResponseDTO> findByDocument(String documentType, String documentNumber);
}
