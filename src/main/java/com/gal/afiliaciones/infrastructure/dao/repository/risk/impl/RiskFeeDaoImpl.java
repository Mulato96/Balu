package com.gal.afiliaciones.infrastructure.dao.repository.risk.impl;

import com.gal.afiliaciones.domain.model.RiskFee;
import com.gal.afiliaciones.infrastructure.dao.repository.risk.RiskFeeDao;
import com.gal.afiliaciones.infrastructure.dao.repository.risk.RiskFeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RiskFeeDaoImpl implements RiskFeeDao {

    private final RiskFeeRepository repository;

    @Override
    public RiskFee getFeeByRisk(String risk){
        return repository.findByRisk(risk);
    }

    @Override
    public List<RiskFee> findAll(){
        return repository.findAll();
    }

}
