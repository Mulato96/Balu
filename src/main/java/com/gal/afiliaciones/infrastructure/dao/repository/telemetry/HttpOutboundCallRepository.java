package com.gal.afiliaciones.infrastructure.dao.repository.telemetry;

import com.gal.afiliaciones.domain.model.HttpOutboundCall;
import com.gal.afiliaciones.infrastructure.dto.telemetry.PositivaLogExportDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HttpOutboundCallRepository extends JpaRepository<HttpOutboundCall, Long> {
    
    @Query("SELECT new com.gal.afiliaciones.infrastructure.dto.telemetry.PositivaLogExportDTO(" +
           "h.id, h.createdAt, h.targetPath, h.targetMethod, h.targetUrl, " +
           "SUBSTRING(h.requestBody, 1, 30000), h.targetQuery, SUBSTRING(h.responseBody, 1, 30000), h.responseStatus) " +
           "FROM HttpOutboundCall h " +
           "WHERE h.targetHost = :targetHost " +
           "AND h.createdAt BETWEEN :startDate AND :endDate " +
           "AND h.targetMethod != 'GET' " +
           "ORDER BY h.createdAt DESC")
    List<PositivaLogExportDTO> findPositivaLogsByDateRange(
            @Param("targetHost") String targetHost,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}

