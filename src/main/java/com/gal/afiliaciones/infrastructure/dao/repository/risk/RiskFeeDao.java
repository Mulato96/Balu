package com.gal.afiliaciones.infrastructure.dao.repository.risk;

import com.gal.afiliaciones.domain.model.RiskFee;

import java.util.List;

public interface RiskFeeDao {

    RiskFee getFeeByRisk(String risk);
    List<RiskFee> findAll();

}
