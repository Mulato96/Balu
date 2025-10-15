package com.gal.afiliaciones.infrastructure.controller.integration;

import com.gal.afiliaciones.application.service.affiliate.impl.ScheduledTimersAffiliationsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/cron-test")
@Tag(name = "Cron Test", description = "Endpoints to trigger scheduled jobs on demand for testing")
@RequiredArgsConstructor
public class CronTestController {

    private final ScheduledTimersAffiliationsServiceImpl scheduledTimersService;

    @PostMapping("/retirement/run-one")
    @Operation(summary = "Trigger retirement for one affiliate", description = "Runs the existing cron with a one-time filter for the provided affiliateId.")
    public ResponseEntity<String> triggerRetirementForOne(@RequestParam("affiliateId") Long affiliateId) {
        log.info("[CronTest] Triggering retirement cron with filter affiliateId={}", affiliateId);
        // Set one-time filter and invoke the existing cron method
        try {
            java.lang.reflect.Field f = scheduledTimersService.getClass().getDeclaredField("testFilterAffiliateId");
            f.setAccessible(true);
            java.util.concurrent.atomic.AtomicReference<Long> ref = (java.util.concurrent.atomic.AtomicReference<Long>) f.get(scheduledTimersService);
            ref.set(affiliateId);
        } catch (Exception e) {
            log.error("[CronTest] Failed to set test filter: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("ERROR: cannot set filter");
        }

        scheduledTimersService.retirement();
        return ResponseEntity.ok("OK");
    }
}


