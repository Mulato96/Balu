package com.gal.afiliaciones.infrastructure.dao.repository.retirementreason;

import com.gal.afiliaciones.domain.model.affiliate.RetirementReasonWorker;

import java.util.List;

public interface RetirementReasonWorkerDao {
    List<RetirementReasonWorker> findAllRetirementReasonWorker();
}
