package com.gal.afiliaciones.application.service.workingday;

import com.gal.afiliaciones.domain.model.WorkingDay;

import java.util.List;

public interface WorkingDayService {

    WorkingDay findByCode(Long code);
    List<WorkingDay> findAll();
}
