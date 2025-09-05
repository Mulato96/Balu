package com.gal.afiliaciones.infrastructure.dao.repository.retirementreason;

import com.gal.afiliaciones.domain.model.affiliate.RetirementReason;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RetirementReasonRepository extends JpaRepository<RetirementReason, Long> {
}
