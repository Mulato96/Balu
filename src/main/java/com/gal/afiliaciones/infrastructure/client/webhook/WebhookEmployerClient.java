package com.gal.afiliaciones.infrastructure.client.webhook;

import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerResponseDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookEmployerClient {
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Value("${webhook.employer.url:https://n8n.srv891132.hstgr.cloud/webhook/sync-employer-v2}")
    private String webhookUrl;
    
    @Value("${webhook.timeout.seconds:30}")
    private int timeoutSeconds;
    
    /**
     * Envía un solo empleador al webhook (POST JSON plano) y espera una lista de respuesta
     */
    public Mono<WebhookEmployerResponseDTO> syncEmployer(WebhookEmployerRequestDTO employer) {
        log.info("🔄 Enviando empleador al webhook: {}", webhookUrl);
        log.info("Payload enviado al webhook: {}", employer);

        return webClient
                .post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(employer)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {}) // capturamos como lista de mapas
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .map(rawList -> {
                    WebhookEmployerResponseDTO result = new WebhookEmployerResponseDTO();

                    for (Map<String, Object> entry : rawList) {
                        if (entry.containsKey("empleador")) {
                            result.setEmpleador(objectMapper.convertValue(entry.get("empleador"), WebhookEmployerResponseDTO.EmployerData.class));
                        } else if (entry.containsKey("empleados")) {
                            result.setEmpleados(objectMapper.convertValue(entry.get("empleados"), new TypeReference<List<WebhookEmployerResponseDTO.Dependiente>>() {}));
                        }
                    }

                    return result;
                })
                .doOnSuccess(response -> log.info("✅ Webhook procesado correctamente para empleador: {}", response.getEmpleador()))
                .doOnError(error -> {
                    if (error instanceof WebClientResponseException ex) {
                        log.error("❌ Error en webhook: {} - Body: {}", ex.getMessage(), ex.getResponseBodyAsString());
                    } else {
                        log.error("❌ Error inesperado en webhook: {}", error.getMessage(), error);
                    }
                })
                .onErrorResume(throwable -> {
                    log.error("❌ Error procesando webhook, retornando vacío: {}", throwable.getMessage());
                    return Mono.empty();
                });
    }


    /**
     * (Obsoleto) Envía una lista de empleadores como array JSON (no usar para este webhook)
     */
    @Deprecated
    public Mono<List<WebhookEmployerResponseDTO>> syncEmployers(List<WebhookEmployerRequestDTO> employers) {
        log.warn("⚠️ Este método está obsoleto. El webhook solo acepta un objeto por vez. Usar syncEmployer para cada empleador.");
        return webClient
                .post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(employers), new ParameterizedTypeReference<List<WebhookEmployerRequestDTO>>() {})
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(json -> log.info("[DEBUG] JSON crudo recibido del webhook: {}", json))
                .map(json -> {
                    try {
                        List<WebhookEmployerResponseDTO> list = objectMapper.readValue(json, new TypeReference<List<WebhookEmployerResponseDTO>>() {});
                        // Forzar inicialización del array empleados como vacío si es null
                        for (WebhookEmployerResponseDTO dto : list) {
                            if (dto.getEmpleados() == null) {
                                dto.setEmpleados(new java.util.ArrayList<>());
                            }
                        }
                        return list;
                    } catch (Exception e) {
                        log.error("❌ Error deserializando JSON del webhook: {}", e.getMessage(), e);
                        return new java.util.ArrayList<WebhookEmployerResponseDTO>();
                    }
                })
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .doOnSuccess(response -> log.info("✅ Webhook procesado exitosamente. Respuesta: {} elementos", 
                        response != null ? response.size() : 0))
                .doOnError(error -> log.error("❌ Error en webhook: {}", error.getMessage(), error))
                .onErrorResume(throwable -> {
                    log.error("❌ Error procesando webhook, retornando lista vacía: {}", throwable.getMessage());
                    return Mono.just(new java.util.ArrayList<WebhookEmployerResponseDTO>());
                });
    }
} 