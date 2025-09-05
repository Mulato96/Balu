package com.gal.afiliaciones.infrastructure.dao.repository.dependent.impl;

import com.gal.afiliaciones.domain.model.BondingTypeDependent;
import com.gal.afiliaciones.infrastructure.dao.repository.dependent.BondingTypeDependentDao;
import com.gal.afiliaciones.infrastructure.dao.repository.dependent.BondingTypeDependentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BondingTypeDependentDaoImpl implements BondingTypeDependentDao {

    private final BondingTypeDependentRepository repository;

    @Override
    public List<BondingTypeDependent> findAll() {
        return repository.findAll();
    }

}
