package com.gal.afiliaciones.infrastructure.dao.repository.retirementreason.impl;

import com.gal.afiliaciones.domain.model.affiliate.RetirementReasonWorker;
import com.gal.afiliaciones.infrastructure.dao.repository.retirementreason.RetirementReasonWorkerDao;
import com.gal.afiliaciones.infrastructure.dao.repository.retirementreason.RetirementReasonWorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RetirementReasonWorkerDaoImpl implements RetirementReasonWorkerDao {

    private final RetirementReasonWorkerRepository repository;

    @Override
    public List<RetirementReasonWorker> findAllRetirementReasonWorker(){
        return repository.findAll();
    }
}
