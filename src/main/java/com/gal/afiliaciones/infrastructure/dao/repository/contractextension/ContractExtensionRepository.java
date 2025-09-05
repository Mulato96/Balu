package com.gal.afiliaciones.infrastructure.dao.repository.contractextension;

import com.gal.afiliaciones.domain.model.ContractExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ContractExtensionRepository  extends JpaRepository<ContractExtension, Long> {

    @Query("select a from ContractExtension a where a.idAfiliationMercatil = ?1")
    Optional<ContractExtension> findByIdAfiliationMercatil(Long idAfiliationMercatil);

    @Query("select a from ContractExtension a where a.idAfiliationDetal = ?1")
    Optional<ContractExtension> findByIdAfiliationDetal(Long idAfiliationDetal);

    Optional<ContractExtension> findByIdAfiliation(Long idAfiliation);

}
