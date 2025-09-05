package com.gal.afiliaciones.infrastructure.dao.repository.novelty;

import com.gal.afiliaciones.domain.model.novelty.ContributorType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContributorTypeRepository extends JpaRepository<ContributorType, Long> {
    Optional<ContributorType> findByCode(String code);
}
