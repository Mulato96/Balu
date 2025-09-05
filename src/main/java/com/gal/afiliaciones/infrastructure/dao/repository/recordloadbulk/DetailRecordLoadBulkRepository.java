package com.gal.afiliaciones.infrastructure.dao.repository.recordloadbulk;

import com.gal.afiliaciones.domain.model.affiliate.DetailRecordLoadBulk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DetailRecordLoadBulkRepository extends JpaRepository<DetailRecordLoadBulk, Long>, JpaSpecificationExecutor<DetailRecordLoadBulk> {
}
