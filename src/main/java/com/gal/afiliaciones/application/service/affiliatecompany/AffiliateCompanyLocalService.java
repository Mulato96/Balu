package com.gal.afiliaciones.application.service.affiliatecompany;

import com.gal.afiliaciones.infrastructure.dto.affiliatecompany.AffiliateCompanyDbApproxResponseDTO;

import java.util.List;

public interface AffiliateCompanyLocalService {

    List<AffiliateCompanyDbApproxResponseDTO> findByDocument(String idTipoDoc, String idAfiliado);
    
    List<AffiliateCompanyDbApproxResponseDTO> findByDocument(String idTipoDoc, String idAfiliado, String appSource);
}


