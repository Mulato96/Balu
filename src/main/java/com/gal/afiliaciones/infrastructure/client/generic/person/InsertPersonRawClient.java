package com.gal.afiliaciones.infrastructure.client.generic.person;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Insert Person Raw Client with automatic telemetry.
 * Returns raw JSON response without parsing.
 * Part of integrations v2 architecture.
 * 
 * HTTP tracking happens automatically via WebClient interceptor.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InsertPersonRawClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Mono<String> insertPersonRaw(PersonRequest request) {
        String url = properties.getBusUrlPerson();
        log.info("Calling insert person endpoint (raw): {}", url);
        return busTokenService.exchangeRaw(HttpMethod.POST, url, request);
    }
}

