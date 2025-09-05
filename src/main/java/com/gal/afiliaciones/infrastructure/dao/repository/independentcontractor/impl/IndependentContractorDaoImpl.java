package com.gal.afiliaciones.infrastructure.dao.repository.independentcontractor.impl;

import com.gal.afiliaciones.domain.model.independentcontractor.ContractQuality;
import com.gal.afiliaciones.domain.model.independentcontractor.ContractType;
import com.gal.afiliaciones.domain.model.independentcontractor.IndependentContractor;
import com.gal.afiliaciones.infrastructure.dao.repository.independentcontractor.ContractQualityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.independentcontractor.ContractTypeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.independentcontractor.IndependentContractorDao;
import com.gal.afiliaciones.infrastructure.dao.repository.independentcontractor.IndependentContractorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IndependentContractorDaoImpl implements IndependentContractorDao {

    private final IndependentContractorRepository independentContractorRepository;
    private final ContractQualityRepository contractQualityRepository;
    private final ContractTypeRepository contractTypeRepository;

    @Override
    public Optional<IndependentContractor> findById(Long id) {
        return independentContractorRepository.findById(id);
    }

    @Override
    public List<IndependentContractor> findAll() {
        return independentContractorRepository.findAll();
    }

    @Override
    public List<ContractQuality> findAllContractQuality() {
        return contractQualityRepository.findAll();
    }

    @Override
    public List<ContractType> findAllContractType() {
        return contractTypeRepository.findAll();
    }

}
