package com.gal.afiliaciones.application.service.webhook;

import com.gal.afiliaciones.infrastructure.client.generic.userportal.UserPortalResponse;
import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerResponseDTO;

import java.util.List;

public interface WebhookEmployerService {
    
    /**
     * Procesa la afiliación de empleadores usando el webhook
     * @param documentType Tipo de documento del usuario
     * @param username Nombre de usuario/documento
     * @return Lista de respuestas del webhook
     */
    List<WebhookEmployerResponseDTO> processEmployerAffiliation(String documentType, String username);
    
    /**
     * Procesa una lista de empleadores directamente
     * @param employers Lista de empleadores a procesar
     * @return Lista de respuestas del webhook
     */
    List<WebhookEmployerResponseDTO> processEmployersList(List<WebhookEmployerRequestDTO> employers);
    
    /**
     * Convierte UserPortalResponse a WebhookEmployerRequestDTO
     * @param userPortal UserPortalResponse a convertir
     * @return WebhookEmployerRequestDTO
     */
    WebhookEmployerRequestDTO convertToWebhookRequest(UserPortalResponse userPortal);
} 