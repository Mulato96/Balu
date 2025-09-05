package com.gal.afiliaciones.infrastructure.dao.repository;

import com.gal.afiliaciones.domain.model.IdentificationLegalNature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface IdentificationLegalNatureRepository extends JpaRepository<IdentificationLegalNature, Long>, JpaSpecificationExecutor<IdentificationLegalNature> {

    Optional<IdentificationLegalNature> findByNit(String nit);
}
