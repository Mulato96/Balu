package com.gal.afiliaciones.infrastructure.dao.repository.independentcontractor;

import com.gal.afiliaciones.domain.model.independentcontractor.ContractQuality;
import com.gal.afiliaciones.domain.model.independentcontractor.ContractType;
import com.gal.afiliaciones.domain.model.independentcontractor.IndependentContractor;

import java.util.List;
import java.util.Optional;

public interface IndependentContractorDao {

    Optional<IndependentContractor> findById(Long id);
    List<IndependentContractor> findAll();
    List<ContractQuality> findAllContractQuality();
    List<ContractType> findAllContractType();

}
