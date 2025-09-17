package com.gal.afiliaciones.application.job;

import com.gal.afiliaciones.application.service.workerdisplacementnotification.WorkerDisplacementNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkerDisplacementScheduler {

    private final WorkerDisplacementNotificationService workerDisplacementNotificationService;

    // Single maintenance cron: finalize expired and mark in-progress
    @Scheduled(cron = "${cron.worker-displacement.maintenance:0 10 0 * * *}")
    public void maintainWorkerDisplacements() {
        int finalized = 0;
        int inProgress = 0;
        try {
            finalized = workerDisplacementNotificationService.autoInactivateExpiredDisplacements();
        } catch (Exception ex) {
            log.error("WorkerDisplacementScheduler: error finalizing expired: {}", ex.getMessage(), ex);
        }
        try {
            inProgress = workerDisplacementNotificationService.autoMarkInProgressDisplacements();
        } catch (Exception ex) {
            log.error("WorkerDisplacementScheduler: error marking in-progress: {}", ex.getMessage(), ex);
        }
        log.info("WorkerDisplacementScheduler: maintenance done. Culminado={}, EnCurso={}", finalized, inProgress);
    }
}


