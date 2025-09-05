package com.gal.afiliaciones.application.service.workingday.impl;

import com.gal.afiliaciones.application.service.workingday.WorkingDayService;
import com.gal.afiliaciones.domain.model.WorkingDay;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.WorkingDaySpecification;
import com.gal.afiliaciones.infrastructure.dao.repository.workingday.WorkingDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkingDayServiceImpl implements WorkingDayService {

    private final WorkingDayRepository workingDayRepository;

    @Override
    public WorkingDay findByCode(Long code) {

        Specification<WorkingDay> spec = WorkingDaySpecification.hasCode(code);
        return  workingDayRepository.findOne(spec).orElse(null);
    }

    @Override
    public List<WorkingDay> findAll() {
        return workingDayRepository.findAll();
    }
}
