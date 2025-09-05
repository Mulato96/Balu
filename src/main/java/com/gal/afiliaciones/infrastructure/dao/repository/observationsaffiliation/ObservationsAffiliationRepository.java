package com.gal.afiliaciones.infrastructure.dao.repository.observationsaffiliation;

import com.gal.afiliaciones.domain.model.ObservationsAffiliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ObservationsAffiliationRepository extends JpaRepository<ObservationsAffiliation, Long>, JpaSpecificationExecutor<ObservationsAffiliation> {
}
