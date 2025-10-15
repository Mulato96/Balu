package com.gal.afiliaciones.infrastructure.client.generic.independentoccupation;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Update Independent Occupation Raw Client with automatic telemetry.
 * Returns raw JSON response without parsing.
 * Part of integrations v2 architecture.
 * 
 * HTTP tracking happens automatically via WebClient interceptor.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateIndependentOccupationRawClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Mono<String> updateRaw(UpdateIndependentOccupationRequest request) {
        String url = properties.getIndependentOccupationUpdateUrl();
        log.info("Calling update independent occupation endpoint (raw): {}", url);
        return busTokenService.exchangeRaw(HttpMethod.PUT, url, request);
    }
}

