package com.gal.afiliaciones.infrastructure.dao.repository.billing;

import com.gal.afiliaciones.domain.model.BillingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingHistoryRepository extends JpaRepository<BillingHistory, Long> {
}
