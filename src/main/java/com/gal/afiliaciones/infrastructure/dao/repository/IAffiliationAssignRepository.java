package com.gal.afiliaciones.infrastructure.dao.repository;

import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.AffiliationAssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;


public interface IAffiliationAssignRepository extends JpaRepository<AffiliationAssignmentHistory,Long>, JpaSpecificationExecutor<AffiliationAssignmentHistory> {

    // Historial completo de una afiliación, ordenado por fecha de asignación descendente
    List<AffiliationAssignmentHistory> findByAffiliationIdOrderByAssignmentDateDesc(Long affiliationId);

    // Registro actual de una afiliación
    Optional<AffiliationAssignmentHistory> findByAffiliationIdAndIsCurrentTrue(Long affiliationId);

    // Todas las afiliaciones asignadas actualmente a un usuario
    List<AffiliationAssignmentHistory> findByUsuarioIdAndIsCurrentTrue(Long usuarioId);


}
