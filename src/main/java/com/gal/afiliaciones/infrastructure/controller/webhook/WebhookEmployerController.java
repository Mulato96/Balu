package com.gal.afiliaciones.infrastructure.controller.webhook;

import com.gal.afiliaciones.application.service.webhook.AsyncWebhookEmployerService;
import com.gal.afiliaciones.application.service.webhook.WebhookEmployerService;
import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/webhook/employer")
@RequiredArgsConstructor
@Slf4j
public class WebhookEmployerController {

    private final WebhookEmployerService webhookEmployerService;
    private final AsyncWebhookEmployerService asyncWebhookEmployerService;

    /**
     * Endpoint para procesar afiliación de empleadores de forma síncrona
     */
    @PostMapping("/sync")
    public ResponseEntity<List<WebhookEmployerResponseDTO>> processEmployerAffiliationSync(
            @RequestParam String documentType,
            @RequestParam String username) {
        
        log.info("📡 Endpoint síncrono llamado para: {} - {}", documentType, username);
        
        try {
            List<WebhookEmployerResponseDTO> results = webhookEmployerService
                    .processEmployerAffiliation(documentType, username);
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("❌ Error en endpoint síncrono: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint para procesar afiliación de empleadores de forma asíncrona
     */
    @PostMapping("/async")
    public ResponseEntity<String> processEmployerAffiliationAsync(
            @RequestParam String documentType,
            @RequestParam String username) {
        
        log.info("📡 Endpoint asíncrono llamado para: {} - {}", documentType, username);
        
        try {
            CompletableFuture<List<WebhookEmployerResponseDTO>> future = asyncWebhookEmployerService
                    .processEmployerAffiliationAsync(documentType, username);
            
            return ResponseEntity.ok("Procesamiento asíncrono iniciado para: " + documentType + " - " + username);
            
        } catch (Exception e) {
            log.error("❌ Error iniciando procesamiento asíncrono: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint para procesar una lista de empleadores de forma síncrona
     */
    @PostMapping("/sync/list")
    public ResponseEntity<List<WebhookEmployerResponseDTO>> processEmployersListSync(
            @RequestBody List<WebhookEmployerRequestDTO> employers) {
        
        log.info("📡 Endpoint síncrono de lista llamado con {} empleadores", employers.size());
        
        try {
            List<WebhookEmployerResponseDTO> results = webhookEmployerService
                    .processEmployersList(employers);
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            log.error("❌ Error en endpoint síncrono de lista: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint para procesar una lista de empleadores de forma asíncrona
     */
    @PostMapping("/async/list")
    public ResponseEntity<String> processEmployersListAsync(
            @RequestBody List<WebhookEmployerRequestDTO> employers) {
        
        log.info("📡 Endpoint asíncrono de lista llamado con {} empleadores", employers.size());
        
        try {
            CompletableFuture<List<WebhookEmployerResponseDTO>> future = asyncWebhookEmployerService
                    .processEmployersListAsync(employers);
            
            // No esperamos el resultado, solo confirmamos que se inició
            return ResponseEntity.ok("Procesamiento asíncrono de lista iniciado para " + employers.size() + " empleadores");
            
        } catch (Exception e) {
            log.error("❌ Error iniciando procesamiento asíncrono de lista: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint de salud para verificar que el servicio esté funcionando
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Webhook Employer Service is running");
    }
} 