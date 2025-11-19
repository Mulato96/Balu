package com.gal.afiliaciones.infrastructure.dao.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gal.afiliaciones.domain.model.Occupation;

public interface OccupationRepository extends JpaRepository<Occupation, Long> {

    Optional<Occupation> findByCodeOccupation(String codeOccupation);
    Optional<Occupation> findByNameOccupation(String nameOccupation);
    
    Optional<Occupation> findByNameOccupationIgnoreCase(String nameOccupation);

}
