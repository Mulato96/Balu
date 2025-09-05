package com.gal.afiliaciones.infrastructure.dao.repository.policy;

import com.gal.afiliaciones.domain.model.BillDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillDetailRepository extends JpaRepository<BillDetail, Long> {
}