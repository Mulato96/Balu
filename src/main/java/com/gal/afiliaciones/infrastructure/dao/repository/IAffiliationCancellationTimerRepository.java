package com.gal.afiliaciones.infrastructure.dao.repository;

import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.AffiliationCancellationTimer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IAffiliationCancellationTimerRepository extends JpaRepository<AffiliationCancellationTimer, Long>, JpaSpecificationExecutor<AffiliationCancellationTimer> {
}
