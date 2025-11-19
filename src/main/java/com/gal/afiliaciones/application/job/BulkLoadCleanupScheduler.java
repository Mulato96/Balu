package com.gal.afiliaciones.application.job;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.gal.afiliaciones.config.BulkLoadCleanupProperties;
import com.gal.afiliaciones.infrastructure.dao.repository.recordloadbulk.RecordLoadBulkRepository;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BulkLoadCleanupScheduler {
    
    private final RecordLoadBulkRepository recordLoadBulkRepository;
    private final BulkLoadCleanupProperties properties;
    
    @Scheduled(cron = "${bulkload.cleanup.cron:0 0 */1 * * ?}")
    @Transactional
    public void cleanupStalledBulkLoads() {
        log.info("Iniciando limpieza de cargues masivos bloqueados...");
        
        LocalDateTime thresholdTime = LocalDateTime.now().minusHours(properties.getThresholdHours());
        List<String> activeStatuses = List.of(
            Constant.BULKLOAD_STATUS_PROCESSING, 
            Constant.BULKLOAD_STATUS_ASYNC_RUNNING
        );
        
        int stalledCount = recordLoadBulkRepository.countByStatusInAndDateLoadBefore(
            activeStatuses, thresholdTime
        );
        
        if (stalledCount > 0) {
            log.warn("Encontrados {} cargues masivos bloqueados por más de {} horas", 
                    stalledCount, properties.getThresholdHours());
            
            int cleanedCount = recordLoadBulkRepository.updateStatusByStatusInAndDateLoadBefore(
                Constant.BULKLOAD_STATUS_FAILED, 
                activeStatuses, 
                thresholdTime
            );
            
            log.info("Limpieza completada. {} cargues liberados.", cleanedCount);
        } else {
            log.info("No se encontraron cargues masivos bloqueados.");
        }
    }
}