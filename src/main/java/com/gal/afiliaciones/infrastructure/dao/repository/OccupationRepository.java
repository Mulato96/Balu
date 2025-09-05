package com.gal.afiliaciones.infrastructure.dao.repository;

import com.gal.afiliaciones.domain.model.Occupation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OccupationRepository extends JpaRepository<Occupation, Long> {

    Optional<Occupation> findByCodeOccupation(String codeOccupation);
    Optional<Occupation> findByNameOccupation(String nameOccupation);

}
