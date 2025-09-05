package com.gal.afiliaciones.infrastructure.dao.repository.dependent;

import com.gal.afiliaciones.domain.model.WorkModality;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkModalityRepository extends JpaRepository<WorkModality, Long> {
}
