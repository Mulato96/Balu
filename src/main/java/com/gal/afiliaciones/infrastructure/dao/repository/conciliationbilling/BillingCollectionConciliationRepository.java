package com.gal.afiliaciones.infrastructure.dao.repository.conciliationbilling;

import com.gal.afiliaciones.domain.model.BillingCollectionConciliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface BillingCollectionConciliationRepository extends JpaRepository<BillingCollectionConciliation, Long>, JpaSpecificationExecutor<BillingCollectionConciliation> {
    Optional<BillingCollectionConciliation> findByBillingId(Long billingId);

}
