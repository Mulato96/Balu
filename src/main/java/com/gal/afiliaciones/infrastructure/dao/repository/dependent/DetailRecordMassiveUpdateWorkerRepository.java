package com.gal.afiliaciones.infrastructure.dao.repository.dependent;

import com.gal.afiliaciones.domain.model.affiliate.DetailRecordMassiveUpdateWorker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DetailRecordMassiveUpdateWorkerRepository extends JpaRepository<DetailRecordMassiveUpdateWorker, Long>,
        JpaSpecificationExecutor<DetailRecordMassiveUpdateWorker> {
}
