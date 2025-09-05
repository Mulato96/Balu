package com.gal.afiliaciones.infrastructure.dao.repository.retirementreason;

import com.gal.afiliaciones.domain.model.affiliate.RetirementReason;

import java.util.List;

public interface RetirementReasonDao {
    List<RetirementReason> getAllRetriementReason();
}
