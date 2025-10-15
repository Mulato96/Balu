package com.gal.afiliaciones.infrastructure.dao.repository.positiva;

import com.gal.afiliaciones.domain.model.PositivaInsertionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PositivaInsertionLogRepository extends JpaRepository<PositivaInsertionLog, Long> {
    List<PositivaInsertionLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}


