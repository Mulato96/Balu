package com.gal.afiliaciones.infrastructure.client.generic.independentactivity;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Update Independent Economic Activity Client with automatic telemetry.
 * Part of integrations v2 architecture.
 * 
 * HTTP tracking happens automatically via WebClient interceptor.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateIndependentEconomicActivityClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Object update(UpdateIndependentEconomicActivityRequest request) {
        String url = properties.getIndependentEconomicActivityUpdateUrl();
        log.info("Calling update independent economic activity endpoint: {}", url);
        log.debug("Sending independent economic activity update request payload: {}", safe(request));
        
        try {
            Object response = busTokenService.exchange(
                    HttpMethod.PUT,
                    url,
                    request,
                    Object.class
            ).block();
            log.debug("Success response: {}", safe(response));
            // Telemetry is handled by WebClient interceptor; no explicit logging needed
            return response;
        } catch (Exception ex) {
            String code = ex instanceof WebClientResponseException wcre ? String.valueOf(wcre.getStatusCode().value()) : "EX";
            String message = ex instanceof WebClientResponseException wcre ? wcre.getResponseBodyAsString() : ex.getMessage();
            log.warn("Error calling {}: code={}, message={}", url, code, message);
            throw ex;
        }
    }

    private String safe(Object o) {
        try { return String.valueOf(o); } catch (Exception e) { return "<unserializable>"; }
    }
}

