package com.gal.afiliaciones.application.service.affiliatecompany.excel;

import com.gal.afiliaciones.infrastructure.dto.affiliatecompany.AffiliateCompanyV2ResponseDTO;

import java.util.List;

/**
 * V2 Service for querying affiliate company data from Excel temporary tables.
 * Used as a fallback when BALU database doesn't have the required data.
 * Returns V2 DTOs with JSON2 field structure.
 */
public interface ExcelAffiliateCompanyV2Service {
    
    /**
     * Finds affiliate company data from Excel temporary tables by document.
     * Returns V2 DTOs with JSON2 field structure.
     * 
     * @param documentType The document type (CC, CE, etc.)
     * @param documentNumber The document number
     * @return List of affiliate company V2 records from Excel tables
     */
    List<AffiliateCompanyV2ResponseDTO> findByDocument(String documentType, String documentNumber);
}
