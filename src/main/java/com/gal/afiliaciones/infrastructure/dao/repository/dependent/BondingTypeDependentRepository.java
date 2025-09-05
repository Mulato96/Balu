package com.gal.afiliaciones.infrastructure.dao.repository.dependent;

import com.gal.afiliaciones.domain.model.BondingTypeDependent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BondingTypeDependentRepository extends JpaRepository<BondingTypeDependent, Long> {
}
