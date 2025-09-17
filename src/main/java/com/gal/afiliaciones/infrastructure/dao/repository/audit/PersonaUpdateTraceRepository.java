package com.gal.afiliaciones.infrastructure.dao.repository.audit;

import com.gal.afiliaciones.domain.model.audit.PersonaUpdateTrace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaUpdateTraceRepository extends JpaRepository<PersonaUpdateTrace, Long> {}
