package com.gal.afiliaciones.application.service.risk.impl;

import com.gal.afiliaciones.application.service.risk.RiskFeeService;
import com.gal.afiliaciones.domain.model.RiskFee;
import com.gal.afiliaciones.infrastructure.dao.repository.risk.RiskFeeDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RiskFeeServiceImp implements RiskFeeService {

    private final RiskFeeDao riskFeeDao;

    @Override
    public BigDecimal getFeeByRisk(String risk){
        RiskFee riskFee = riskFeeDao.getFeeByRisk(risk);
        if(riskFee!=null)
            return riskFee.getFee();

        return new BigDecimal(0);
    }

    @Override
    public List<RiskFee> getAllRiskFee(){
        return riskFeeDao.findAll();
    }

}
