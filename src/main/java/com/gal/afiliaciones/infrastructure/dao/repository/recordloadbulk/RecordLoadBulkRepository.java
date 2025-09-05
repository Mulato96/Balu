package com.gal.afiliaciones.infrastructure.dao.repository.recordloadbulk;

import com.gal.afiliaciones.domain.model.affiliate.RecordLoadBulk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RecordLoadBulkRepository extends JpaRepository<RecordLoadBulk, Long>, JpaSpecificationExecutor<RecordLoadBulk> {
}
