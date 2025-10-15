package com.gal.afiliaciones.infrastructure.client.generic.headquarters;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Update Headquarters Raw Client with automatic telemetry.
 * Returns raw JSON response without parsing.
 * Part of integrations v2 architecture.
 * 
 * HTTP tracking happens automatically via WebClient interceptor.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateHeadquartersRawClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Mono<String> updateRaw(UpdateHeadquartersRequest request) {
        String url = properties.getInsertHeadquartersUrl(); // Same URL as insert, different HTTP method (PUT)
        log.info("Calling update headquarters endpoint (raw): {}", url);
        return busTokenService.exchangeRaw(HttpMethod.PUT, url, request);
    }
}

