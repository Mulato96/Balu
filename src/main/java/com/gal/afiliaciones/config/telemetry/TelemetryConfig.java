package com.gal.afiliaciones.config.telemetry;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for integrations v2 telemetry system.
 * Provides dedicated thread pool for async HTTP call logging.
 */
@Configuration
@ConfigurationProperties(prefix = "integrations.v2.telemetry")
@Data
public class TelemetryConfig {
    
    /**
     * Maximum body size to store (in bytes). Default: 64KB
     */
    private int maxBodySize = 64 * 1024;
    
    /**
     * Whether telemetry is enabled. Default: true
     */
    private boolean enabled = true;
    
    /**
     * Thread pool configuration
     */
    private ThreadPool threadPool = new ThreadPool();
    
    @Data
    public static class ThreadPool {
        private int coreSize = 1;
        private int maxSize = 4;
        private int keepAliveSeconds = 30;
        private int queueCapacity = 1000;
    }
    
    /**
     * Dedicated executor for telemetry operations.
     * Uses discard-oldest policy to never block application threads.
     */
    @Bean(name = "telemetryExecutor")
    public Executor telemetryExecutor() {
        return new ThreadPoolExecutor(
            threadPool.getCoreSize(),
            threadPool.getMaxSize(),
            threadPool.getKeepAliveSeconds(),
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(threadPool.getQueueCapacity()),
            r -> {
                Thread t = new Thread(r, "telemetry-worker");
                t.setDaemon(true); // Don't prevent JVM shutdown
                return t;
            },
            new ThreadPoolExecutor.DiscardOldestPolicy() // Never block app threads
        );
    }
}
