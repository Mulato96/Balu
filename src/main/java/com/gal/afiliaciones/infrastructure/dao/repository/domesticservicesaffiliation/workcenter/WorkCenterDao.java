package com.gal.afiliaciones.infrastructure.dao.repository.domesticservicesaffiliation.workcenter;

import com.gal.afiliaciones.domain.model.affiliate.WorkCenter;

public interface WorkCenterDao {
    WorkCenter findWorkCenterById(Long id);
}