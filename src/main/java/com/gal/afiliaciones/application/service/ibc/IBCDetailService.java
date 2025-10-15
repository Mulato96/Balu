package com.gal.afiliaciones.application.service.ibc;

import com.gal.afiliaciones.infrastructure.dto.ibc.IBCDetailDTO;

public interface IBCDetailService {

    IBCDetailDTO calculateAndSaveIBC(IBCDetailDTO ibcDetailDTO);

}
