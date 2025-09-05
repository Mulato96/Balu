package com.gal.afiliaciones.infrastructure.dao.repository.workingday;

import com.gal.afiliaciones.domain.model.WorkingDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WorkingDayRepository extends JpaRepository<WorkingDay, Long>, JpaSpecificationExecutor<WorkingDay> {
}
