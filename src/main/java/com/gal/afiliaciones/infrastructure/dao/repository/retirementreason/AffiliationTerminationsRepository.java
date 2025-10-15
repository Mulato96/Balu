package com.gal.afiliaciones.infrastructure.dao.repository.retirementreason;

import com.gal.afiliaciones.domain.model.AffiliationTerminations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AffiliationTerminationsRepository extends JpaRepository<AffiliationTerminations, Long> {

    @Query("select (count(a) > 0) from AffiliationTerminations a where a.idAfiliationMercatil = ?1")
    boolean existsByIdAfiliationMercatil(Long idAfiliationMercatil);

    @Query("select (count(a) > 0) from AffiliationTerminations a where a.idAfiliationDetal = ?1")
    boolean existsByIdAfiliationDetal(Long idAfiliationDetal);

    @Query("select a from AffiliationTerminations a where a.idAfiliationMercatil = ?1")
    Optional<AffiliationTerminations> findByIdAfiliationMercatil(Long idAfiliationMercatil);

    @Query("select a from AffiliationTerminations a where a.idAfiliationDetal = ?1")
    Optional<AffiliationTerminations> findByIdAfiliationDetal(Long idAfiliationDetal);


}
