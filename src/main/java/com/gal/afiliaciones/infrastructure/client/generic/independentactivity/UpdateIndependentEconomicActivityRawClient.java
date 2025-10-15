package com.gal.afiliaciones.infrastructure.client.generic.independentactivity;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Update Independent Economic Activity Raw Client with automatic telemetry.
 * Returns raw JSON response without parsing.
 * Part of integrations v2 architecture.
 * 
 * HTTP tracking happens automatically via WebClient interceptor.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateIndependentEconomicActivityRawClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Mono<String> updateRaw(UpdateIndependentEconomicActivityRequest request) {
        String url = properties.getIndependentEconomicActivityUpdateUrl();
        log.info("Calling update independent economic activity endpoint (raw): {}", url);
        return busTokenService.exchangeRaw(HttpMethod.PUT, url, request);
    }
}

