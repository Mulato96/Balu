package com.gal.afiliaciones.infrastructure.client.generic.headquarters;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Insert Headquarters Raw Client with automatic telemetry.
 * Returns raw JSON response without parsing.
 * Part of integrations v2 architecture.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InsertHeadquartersRawClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Mono<String> insertRaw(InsertHeadquartersRequest request) {
        String url = properties.getInsertHeadquartersUrl();
        log.info("Calling insert headquarters endpoint (raw): {}", url);
        return busTokenService.exchangeRaw(HttpMethod.POST, url, request);
    }
}

