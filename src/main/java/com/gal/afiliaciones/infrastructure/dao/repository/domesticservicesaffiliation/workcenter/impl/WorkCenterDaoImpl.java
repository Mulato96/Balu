package com.gal.afiliaciones.infrastructure.dao.repository.domesticservicesaffiliation.workcenter.impl;

import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.WorkCenterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.domesticservicesaffiliation.workcenter.WorkCenterDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WorkCenterDaoImpl implements WorkCenterDao {

    private final WorkCenterRepository workCenterRepository;

    @Override
    public WorkCenter findWorkCenterById(Long id) {
        return workCenterRepository.findById(id).orElse(null);
    }
}