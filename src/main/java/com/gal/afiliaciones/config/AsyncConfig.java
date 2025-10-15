package com.gal.afiliaciones.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    @Bean(name = "webhookTaskExecutor")
    public Executor webhookTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Webhook-");
        executor.setKeepAliveSeconds(60);
        
        executor.setRejectedExecutionHandler((r, e) -> {
            log.warn("⚠️ Tarea webhook rechazada por límite de capacidad del pool");
        });
        
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        
        log.info("🚀 Configurado executor webhook con {} hilos core, {} máximos", 
                executor.getCorePoolSize(), executor.getMaxPoolSize());
        
        return executor;
    }
} 