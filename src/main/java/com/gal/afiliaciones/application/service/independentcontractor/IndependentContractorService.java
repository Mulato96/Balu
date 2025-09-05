package com.gal.afiliaciones.application.service.independentcontractor;

import com.gal.afiliaciones.domain.model.independentcontractor.ContractQuality;
import com.gal.afiliaciones.domain.model.independentcontractor.ContractType;
import com.gal.afiliaciones.infrastructure.dto.independentcontractor.IndependentContractorDTO;

import java.util.List;
import java.util.Optional;

public interface IndependentContractorService {

    Optional<IndependentContractorDTO> findById(Long id);
    List<IndependentContractorDTO> findAll();
    List<ContractQuality> findAllContractQuality();
    List<ContractType> findAllContractType();

}
