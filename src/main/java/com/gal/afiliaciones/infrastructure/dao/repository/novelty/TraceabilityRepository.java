package com.gal.afiliaciones.infrastructure.dao.repository.novelty;

import com.gal.afiliaciones.domain.model.novelty.Traceability;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TraceabilityRepository extends JpaRepository<Traceability, Long> {
}
