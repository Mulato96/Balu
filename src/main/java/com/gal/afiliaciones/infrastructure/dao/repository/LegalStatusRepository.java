package com.gal.afiliaciones.infrastructure.dao.repository;

import com.gal.afiliaciones.domain.model.LegalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegalStatusRepository extends JpaRepository<LegalStatus, Long> {

}
