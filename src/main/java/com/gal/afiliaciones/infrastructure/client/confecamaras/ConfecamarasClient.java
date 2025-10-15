package com.gal.afiliaciones.infrastructure.client.confecamaras;

import com.gal.afiliaciones.infrastructure.dto.wsconfecamaras.RecordResponseDTO;
import com.gal.afiliaciones.infrastructure.service.RegistraduriaKeycloakTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfecamarasClient {

    @Qualifier("confecamarasWebClient")
    private final WebClient webClient;
    
    private final RegistraduriaKeycloakTokenService registraduriaKeycloakTokenService;
    
    @Value("${confecamaras.api.url}")
    private String confecamarasApiUrl;

    /**
     * Consult company information from Confecamaras API
     * @param nit Company NIT to consult
     * @param dv Verification digit
     * @return Confecamaras response with company data
     */
    public Mono<RecordResponseDTO> consultCompany(String nit, String dv) {
        log.info("Starting Confecamaras consultation for NIT: {} DV: {}", nit, dv);
        
        try {
            String accessToken = registraduriaKeycloakTokenService.getAccessToken();
            log.debug("Access token obtained for Confecamaras consultation");
            
            // Build the URL with query parameters
            String url = confecamarasApiUrl + "?nit=" + nit + "&dv=" + dv;
            log.debug("Confecamaras API URL: {}", url);
            
            return webClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header("Cookie", "02fa53e49eb78f3f36d39bd20d4185ec=2f5056d9011834caa5fe905dd0be2b06; 2fbb6f2e6bac0f23fbba723c83e75b03=983e67bf02391a22388a71db3717a46b")
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), 
                        response -> {
                            log.error("Confecamaras API returned error status: {} for NIT: {}", response.statusCode(), nit);
                            return response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("Confecamaras API error response body: {}", body);
                                    return Mono.error(new RuntimeException("Confecamaras API error: " + response.statusCode() + " - " + body));
                                });
                        })
                    .bodyToMono(RecordResponseDTO.class)
                    .timeout(Duration.ofSeconds(60))
                    .doOnSuccess(response -> {
                        if (response.getError() != null) {
                            log.warn("Confecamaras API returned error for NIT {}: {} - {}", 
                                    nit, response.getError().getCode(), response.getError().getMessage());
                        } else {
                            log.info("Confecamaras consultation successful for NIT: {}", nit);
                        }
                    })
                    .doOnError(error -> {
                        if (error.getMessage().contains("timeout")) {
                            log.error("Confecamaras API timeout for NIT {}: {}", nit, error.getMessage());
                        } else {
                            log.error("Error in Confecamaras API call for NIT {}: {}", nit, error.getMessage());
                        }
                    });
                    
        } catch (Exception e) {
            log.error("Error obtaining access token or making API call for NIT {}: {}", nit, e.getMessage(), e);
            return Mono.error(new RuntimeException("Failed to consult Confecamaras for NIT " + nit + ": " + e.getMessage(), e));
        }
    }
}
