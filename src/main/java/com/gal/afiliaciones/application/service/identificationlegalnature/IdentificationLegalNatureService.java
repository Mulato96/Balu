package com.gal.afiliaciones.application.service.identificationlegalnature;

import com.gal.afiliaciones.infrastructure.dto.IdentificationLegalNatureDTO;

import java.util.List;

public interface IdentificationLegalNatureService {

    List<IdentificationLegalNatureDTO> create(List<IdentificationLegalNatureDTO> list);
    boolean findByNit(String nit);

}
