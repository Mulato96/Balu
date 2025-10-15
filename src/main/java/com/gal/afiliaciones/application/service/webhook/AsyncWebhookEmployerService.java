package com.gal.afiliaciones.application.service.webhook;

import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncWebhookEmployerService {

    private final WebhookEmployerService webhookEmployerService;

    /**
     * Ejecuta la afiliación de empleadores de forma asíncrona
     * @param documentType Tipo de documento del usuario
     * @param username Nombre de usuario/documento
     * @return CompletableFuture con la lista de respuestas del webhook
     */
    @Async("webhookTaskExecutor")
    public CompletableFuture<List<WebhookEmployerResponseDTO>> processEmployerAffiliationAsync(
            String documentType, String username) {
        
        log.info("🔄 Iniciando procesamiento asíncrono de afiliación webhook para: {} - {}", 
                documentType, username);
        
        try {
            List<WebhookEmployerResponseDTO> results = webhookEmployerService
                    .processEmployerAffiliation(documentType, username);
            
            log.info("✅ Procesamiento asíncrono completado para: {} - {}. Resultados: {}", 
                    documentType, username, results.size());
            
            return CompletableFuture.completedFuture(results);
            
        } catch (Exception e) {
            log.error("❌ Error en procesamiento asíncrono para: {} - {}: {}", 
                    documentType, username, e.getMessage(), e);
            return CompletableFuture.completedFuture(List.of());
        }
    }

    /**
     * Ejecuta el procesamiento de una lista de empleadores de forma asíncrona (ahora uno por uno)
     * @param employers Lista de empleadores a procesar
     * @return CompletableFuture con la lista de respuestas del webhook
     */
    @Async("webhookTaskExecutor")
    public CompletableFuture<List<WebhookEmployerResponseDTO>> processEmployersListAsync(
            List<WebhookEmployerRequestDTO> employers) {
        
        log.info("🔄 Iniciando procesamiento asíncrono de lista de empleadores: {} elementos", 
                employers.size());
        
        try {
            List<WebhookEmployerResponseDTO> results = webhookEmployerService
                    .processEmployersList(employers);
            
            log.info("✅ Procesamiento asíncrono de lista completado. Resultados: {}", 
                    results.size());
            
            return CompletableFuture.completedFuture(results);
            
        } catch (Exception e) {
            log.error("❌ Error en procesamiento asíncrono de lista: {}", e.getMessage(), e);
            return CompletableFuture.completedFuture(List.of());
        }
    }
} 