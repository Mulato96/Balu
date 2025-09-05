package com.gal.afiliaciones.application.service.identificationlegalnature.impl;

import com.gal.afiliaciones.application.service.identificationlegalnature.IdentificationLegalNatureService;
import com.gal.afiliaciones.domain.model.IdentificationLegalNature;
import com.gal.afiliaciones.infrastructure.dao.repository.IdentificationLegalNatureRepository;
import com.gal.afiliaciones.infrastructure.dto.IdentificationLegalNatureDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IdentificationLegalNatureServiceImpl implements IdentificationLegalNatureService {

    private final IdentificationLegalNatureRepository identificationLegalNatureRepository;

    @Override
    public List<IdentificationLegalNatureDTO> create(List<IdentificationLegalNatureDTO> list) {

        List<IdentificationLegalNature> listNature = list
                .stream()
                .map(data -> {
                    IdentificationLegalNature identificationLegalNature = new IdentificationLegalNature();
                    BeanUtils.copyProperties(data, identificationLegalNature);
                    return identificationLegalNature;
                })
                .toList();
        return identificationLegalNatureRepository.saveAll(listNature)
                .stream()
                .map(data -> {
                    IdentificationLegalNatureDTO legalNatureDTO = new IdentificationLegalNatureDTO();
                    BeanUtils.copyProperties(data, legalNatureDTO);
                    return legalNatureDTO;
                })
                .toList();
    }

    @Override
    public boolean findByNit(String nit) {
        return identificationLegalNatureRepository.findByNit(nit).isPresent();
    }
}
