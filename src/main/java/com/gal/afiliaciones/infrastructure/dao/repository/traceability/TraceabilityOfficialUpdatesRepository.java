package com.gal.afiliaciones.infrastructure.dao.repository.traceability;

import com.gal.afiliaciones.domain.model.affiliate.TraceabilityOfficialUpdates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraceabilityOfficialUpdatesRepository extends JpaRepository <TraceabilityOfficialUpdates, Long> {
}
