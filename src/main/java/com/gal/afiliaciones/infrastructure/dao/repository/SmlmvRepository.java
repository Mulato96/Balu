package com.gal.afiliaciones.infrastructure.dao.repository;

import com.gal.afiliaciones.domain.model.Smlmv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SmlmvRepository extends JpaRepository<Smlmv, Long> {

    /**
     * Find the SMLMV value that is valid for a given date.
     * The date must be between fecha_desde and fecha_hasta.
     *
     * @param date the date to check
     * @return the SMLMV entity if found
     */
    @Query("SELECT s FROM Smlmv s WHERE :date BETWEEN s.fechaDesde AND s.fechaHasta")
    Optional<Smlmv> findByValidDate(@Param("date") LocalDateTime date);

    /**
     * Find the most recent SMLMV entry (fallback method).
     *
     * @return the most recent SMLMV entity
     */
    @Query("SELECT s FROM Smlmv s ORDER BY s.fechaDesde DESC LIMIT 1")
    Optional<Smlmv> findMostRecent();
}

