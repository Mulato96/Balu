package com.gal.afiliaciones.infrastructure.dao.repository.retirementreason.impl;

import com.gal.afiliaciones.domain.model.affiliate.RetirementReason;
import com.gal.afiliaciones.infrastructure.dao.repository.retirementreason.RetirementReasonDao;
import com.gal.afiliaciones.infrastructure.dao.repository.retirementreason.RetirementReasonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RetirementReasonDaoImpl implements RetirementReasonDao {

    private final RetirementReasonRepository repository;

    @Override
    public List<RetirementReason> getAllRetriementReason() {
        return repository.findAll();
    }
}
