package com.gal.afiliaciones.infrastructure.dao.repository.recordloadbulk;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gal.afiliaciones.domain.model.affiliate.RecordLoadBulk;

public interface RecordLoadBulkRepository extends JpaRepository<RecordLoadBulk, Long>, JpaSpecificationExecutor<RecordLoadBulk> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update RecordLoadBulk r set r.status = :status where r.id = :id")
    int updateStatusById(@Param("id") Long id, @Param("status") String status);

    @Query("SELECT COUNT(r) FROM RecordLoadBulk r WHERE r.status IN :statuses AND r.dateLoad < :thresholdTime")
    int countByStatusInAndDateLoadBefore(
        @Param("statuses") List<String> statuses,
        @Param("thresholdTime") LocalDateTime thresholdTime
    );
    
    @Modifying
    @Query("UPDATE RecordLoadBulk r SET r.status = :newStatus WHERE r.status IN :statuses AND r.dateLoad < :thresholdTime")
    int updateStatusByStatusInAndDateLoadBefore(
        @Param("newStatus") String newStatus,
        @Param("statuses") List<String> statuses,
        @Param("thresholdTime") LocalDateTime thresholdTime
    );
}
