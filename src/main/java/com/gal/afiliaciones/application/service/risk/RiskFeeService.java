package com.gal.afiliaciones.application.service.risk;

import com.gal.afiliaciones.domain.model.RiskFee;

import java.math.BigDecimal;
import java.util.List;

public interface RiskFeeService {

    BigDecimal getFeeByRisk(String risk);
    List<RiskFee> getAllRiskFee();

}
