package com.gal.afiliaciones.application.job;

import com.gal.afiliaciones.application.service.billing.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class BillingScheduler {
    private final BillingService billingService;

    @Scheduled(cron = "#{@affiliationProperties.cronExpression}")
    public void generateBillingInfo() {
        log.info("Iniciando la generación de información de facturación para el mes anterior...");
        billingService.generateBilling();
        log.info("Proceso de generación de facturación finalizado.");
    }
}
