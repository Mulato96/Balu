package com.gal.afiliaciones.infrastructure.dao.repository.risk;

import com.gal.afiliaciones.domain.model.RiskFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RiskFeeRepository extends JpaRepository<RiskFee, Long>, JpaSpecificationExecutor<RiskFee> {

    RiskFee findByRisk(String risk);

}
