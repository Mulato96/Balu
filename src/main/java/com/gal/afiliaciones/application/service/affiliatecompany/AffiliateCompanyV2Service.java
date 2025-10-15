package com.gal.afiliaciones.application.service.affiliatecompany;

import com.gal.afiliaciones.infrastructure.dto.affiliatecompany.AffiliateCompanyV2ResponseDTO;

import java.util.List;

/**
 * Service interface for V2 affiliate company consultation.
 * Follows the same pattern as AffiliateCompanyLocalService but returns V2 DTOs
 * with JSON2 field structure.
 */
public interface AffiliateCompanyV2Service {

    /**
     * Find affiliates by document without specifying app source.
     * Uses default fallback behavior (BALU → Excel).
     */
    List<AffiliateCompanyV2ResponseDTO> findByDocument(String idTipoDoc, String idAfiliado);
    
    /**
     * Find affiliates by document with optional app source specification.
     * Supports forced source selection (BALU, EXCEL) or default fallback behavior.
     */
    List<AffiliateCompanyV2ResponseDTO> findByDocument(String idTipoDoc, String idAfiliado, String appSource);
}
