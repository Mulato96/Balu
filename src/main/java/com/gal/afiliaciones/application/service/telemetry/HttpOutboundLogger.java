package com.gal.afiliaciones.application.service.telemetry;

import com.gal.afiliaciones.config.telemetry.TelemetryConfig;
import com.gal.afiliaciones.infrastructure.dao.telemetry.HttpOutboundCallDao;
import com.gal.afiliaciones.infrastructure.dto.telemetry.HttpCallRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Asynchronous logger for HTTP outbound calls.
 * Part of integrations v2 architecture.
 * 
 * This service ensures that telemetry logging never blocks application threads
 * and never throws exceptions that could break business logic.
 */
@Service
@Slf4j
public class HttpOutboundLogger {

    private final HttpOutboundCallDao dao;
    private final TelemetryConfig config;
    private final Executor executor;

    public HttpOutboundLogger(HttpOutboundCallDao dao, 
                             TelemetryConfig config,
                             @Qualifier("telemetryExecutor") Executor executor) {
        this.dao = dao;
        this.config = config;
        this.executor = executor;
    }

    /**
     * Logs an HTTP call record asynchronously.
     * This method never blocks and never throws exceptions.
     * 
     * @param record The HTTP call record to log
     */
    public void logAsync(HttpCallRecord record) {
        log.debug("🚀 HttpOutboundLogger.logAsync() called");
        
        if (!config.isEnabled()) {
            log.debug("❌ Telemetry is DISABLED - config.enabled={}", config.isEnabled());
            return;
        }
        
        if (record == null) {
            log.debug("❌ Ignoring null HTTP call record");
            return;
        }
        
        log.debug("✅ Telemetry enabled, submitting record to async executor: {} {}", 
            record.getTargetMethod(), record.getTargetUrl());
        
        // Submit to async executor - fire and forget
        executor.execute(() -> {
            try {
                log.debug("🔄 Async executor processing HTTP call record");
                Map<String, Object> params = record.toSqlParams(config.getMaxBodySize());
                log.debug("📋 Converted record to SQL params, calling DAO.insert()");
                dao.insert(params);
                
                log.debug("✅ Successfully logged HTTP call: {} {}", 
                    record.getTargetMethod(), record.getTargetUrl());
            } catch (Exception e) {
                // Critical: Never let telemetry exceptions bubble up
                log.warn("❌ Failed to log HTTP outbound call: {}", e.getMessage());
                if (log.isDebugEnabled()) {
                    log.debug("HTTP outbound logging failure details", e);
                }
            }
        });
    }
    
    /**
     * Creates a new HTTP call record with basic service context.
     * 
     * @param serviceName Name of the service making the call
     * @param environment Current environment (dev, staging, prod)
     * @param appVersion Application version
     * @return New HTTP call record with context set
     */
    public HttpCallRecord createRecord(String serviceName, String environment, String appVersion) {
        HttpCallRecord record = new HttpCallRecord();
        record.setServiceName(serviceName);
        record.setEnvironment(environment);
        record.setAppVersion(appVersion);
        return record;
    }
}
