package com.gal.afiliaciones.infrastructure.dao.repository.decree1563;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gal.afiliaciones.domain.model.OccupationDecree1563;

public interface OccupationDecree1563Repository extends JpaRepository<OccupationDecree1563, Long> {

    Optional<OccupationDecree1563> findByCode(Long code);
    Optional<OccupationDecree1563> findByOccupation(String occupation);
    Optional<OccupationDecree1563> findByOccupationIgnoreCase(String occupation);
    
    @Query(value = "SELECT * FROM occupation_decree_1563 WHERE LOWER(unaccent(descripcion)) = LOWER(unaccent(:occupation))", nativeQuery = true)
    Optional<OccupationDecree1563> findByOccupationIgnoreCaseAndAccents(@Param("occupation") String occupation);
}
