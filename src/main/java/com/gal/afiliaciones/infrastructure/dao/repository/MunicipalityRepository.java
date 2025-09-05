package com.gal.afiliaciones.infrastructure.dao.repository;

import com.gal.afiliaciones.domain.model.Municipality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MunicipalityRepository extends JpaRepository<Municipality, Long>, JpaSpecificationExecutor<Municipality> {
    Optional<Municipality> findByDivipolaCode(String divipolaCode);
    Optional<Municipality> findByMunicipalityName(String municipalityName);
    Optional<Municipality> findByIdDepartmentAndMunicipalityCode(Long idDepartment, String municipalityCode);
}
