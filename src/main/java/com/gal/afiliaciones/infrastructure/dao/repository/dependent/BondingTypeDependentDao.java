package com.gal.afiliaciones.infrastructure.dao.repository.dependent;

import com.gal.afiliaciones.domain.model.BondingTypeDependent;

import java.util.List;

public interface BondingTypeDependentDao {

    List<BondingTypeDependent> findAll();

}
