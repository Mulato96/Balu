package com.gal.afiliaciones.infrastructure.dao.repository.billing;

import com.gal.afiliaciones.domain.model.BillDetailHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillDetailHistoryRepository extends JpaRepository<BillDetailHistory, Long> {
}
