package com.gal.afiliaciones.application.service.independentcontractor.impl;

import com.gal.afiliaciones.application.service.independentcontractor.IndependentContractorService;
import com.gal.afiliaciones.domain.model.independentcontractor.ContractQuality;
import com.gal.afiliaciones.domain.model.independentcontractor.ContractType;
import com.gal.afiliaciones.domain.model.independentcontractor.IndependentContractor;
import com.gal.afiliaciones.infrastructure.dao.repository.independentcontractor.IndependentContractorDao;
import com.gal.afiliaciones.infrastructure.dto.independentcontractor.IndependentContractorDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IndependentContractorServiceImpl implements IndependentContractorService {

    private final IndependentContractorDao independentContractorDao;

    @Override
    public Optional<IndependentContractorDTO> findById(Long id) {
        return independentContractorDao.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    public List<IndependentContractorDTO> findAll() {
        return independentContractorDao.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    // Métodos privados para convertir entre entidad y DTO
    private IndependentContractorDTO convertToDTO(IndependentContractor entity) {
        return IndependentContractorDTO.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .build();
    }

    @Override
    public List<ContractQuality> findAllContractQuality(){
        return independentContractorDao.findAllContractQuality();
    }

    @Override
    public List<ContractType> findAllContractType(){
        return independentContractorDao.findAllContractType();
    }

}
