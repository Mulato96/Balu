package com.gal.afiliaciones.infrastructure.dao.repository.process;

import com.gal.afiliaciones.domain.model.Process;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessRepository extends JpaRepository<Process, Long> {
}
