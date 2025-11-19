package com.gal.afiliaciones.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "bulkload.cleanup")
@Data
public class BulkLoadCleanupProperties {
    private String cron;
    private int thresholdHours = 1;
}