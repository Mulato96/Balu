package com.gal.afiliaciones.infrastructure.dao.repository.arl.impl;

import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ArlInformationDaoImpl implements ArlInformationDao {

    private final ArlInformationRepository repository;

    @Override
    public List<ArlInformation> findAllArlInformation(){
        return repository.findAll();
    }
}
