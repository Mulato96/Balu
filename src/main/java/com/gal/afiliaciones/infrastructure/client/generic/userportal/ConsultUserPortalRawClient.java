package com.gal.afiliaciones.infrastructure.client.generic.userportal;

import com.gal.afiliaciones.config.util.AffiliationProperties;
import com.gal.afiliaciones.infrastructure.security.BusTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Consult User Portal Raw Client with automatic telemetry.
 * Returns raw JSON response without parsing.
 * Part of integrations v2 architecture.
 * 
 * HTTP tracking happens automatically via WebClient interceptor.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConsultUserPortalRawClient {
    private final BusTokenService busTokenService;
    private final AffiliationProperties properties;

    public Mono<String> consultRaw(String idTipoDocPersona, String idPersona) {
        String url = properties.getUserPortalUrl()
                + "?idTipoDocPersona=" + idTipoDocPersona
                + "&idPersona=" + idPersona;
        
        log.info("Calling consult user portal endpoint (raw): {}", url);
        return busTokenService.getRaw(url);
    }
}

