package com.gal.afiliaciones.infrastructure.dao.repository.dependent.impl;

import com.gal.afiliaciones.domain.model.WorkModality;
import com.gal.afiliaciones.infrastructure.dao.repository.dependent.WorkModalityDao;
import com.gal.afiliaciones.infrastructure.dao.repository.dependent.WorkModalityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class WorkModalityDaoImpl implements WorkModalityDao {

    private final WorkModalityRepository repository;

    @Override
    public List<WorkModality> findAll() {
        return repository.findAll();
    }

}
