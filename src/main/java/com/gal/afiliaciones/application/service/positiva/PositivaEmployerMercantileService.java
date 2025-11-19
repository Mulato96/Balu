package com.gal.afiliaciones.application.service.positiva;

import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.PositivaEmployerMercantileDTO;

import java.util.List;

public interface PositivaEmployerMercantileService {
    List<PositivaEmployerMercantileDTO> findEmployers(String idTipoDoc, String idEmpresa, Integer idSubEmpresa);
}


