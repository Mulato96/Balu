package com.gal.afiliaciones.infrastructure.dao.repository.dependent;

import com.gal.afiliaciones.domain.model.affiliate.RecordMassiveUpdateWorker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RecordMassiveUpdateWorkerRepository extends JpaRepository<RecordMassiveUpdateWorker, Long>,
        JpaSpecificationExecutor<RecordMassiveUpdateWorker> {
}
